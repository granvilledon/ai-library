package com.book.backend.service.impl;

import cn.hutool.core.date.StopWatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.R;
import com.book.backend.common.exception.VueBookException;
import com.book.backend.manager.GuavaRateLimiterManager;
import com.book.backend.manager.SparkClient;
import com.book.backend.manager.model.SparkMessage;
import com.book.backend.manager.model.SparkSyncChatResponse;
import com.book.backend.manager.model.request.SparkRequest;
import com.book.backend.manager.model.response.SparkTextUsage;
import com.book.backend.manager.SparkProperties;
import com.book.backend.mapper.AiIntelligentMapper;
import com.book.backend.pojo.*;
import com.book.backend.service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * AI智能分析服务实现类
 */
@Service
public class AiIntelligentServiceImpl extends ServiceImpl<AiIntelligentMapper, AiIntelligent>
        implements AiIntelligentService {

    /**
     * 讯飞星火客户端实例
     */
    SparkClient sparkClient = new SparkClient();

    /**
     * 未配置星火凭证时的统一提示，避免直接抛 NPE 或发一次注定失败的请求。
     */
    private static final String SPARK_NOT_CONFIGURED =
            "AI 服务未配置：请在 application-local.yml 或环境变量中填写 SPARK_APP_ID / SPARK_API_KEY / SPARK_API_SECRET";

    @Resource
    private BooksService booksService;

    @Resource
    private GuavaRateLimiterManager guavaRateLimiterManager;

    @Resource
    private UserInterfaceInfoServiceImpl userInterfaceInfoService;

    @Resource
    @Lazy
    private AiIntelligentService aiIntelligentService;

    @Resource
    private SparkProperties sparkProperties;

    @Resource
    private UsersService usersService;

    @Resource
    private BooksBorrowService booksBorrowService;

    @Resource
    private ViolationService violationService;

    /**
     * 容器启动后把配置文件里的星火凭证装配到客户端实例上。
     * 凭证来源见 application.yml 的 spark 段与 SparkProperties 注释。
     */
    @PostConstruct
    public void initSparkClient() {
        sparkClient.appid = sparkProperties.getAppId();
        sparkClient.apiKey = sparkProperties.getApiKey();
        sparkClient.apiSecret = sparkProperties.getApiSecret();
    }

    /**
     * 用户获取AI图书推荐
     *
     * @param aiIntelligent 包含用户ID和输入消息的对象
     * @return AI生成的图书推荐结果
     */
    @Override
    public R<String> getGenResult(AiIntelligent aiIntelligent) {
        if (!sparkProperties.isConfigured()) {
            return R.error(SPARK_NOT_CONFIGURED);
        }
        String message = aiIntelligent.getInputMessage();
        if (StringUtils.isBlank(message)) {
            return R.error("文本不能为空");
        }
        if (message.length() > 128) {
            return R.error("文本字数过长");
        }
        Long user_id = aiIntelligent.getUserId();

        LambdaQueryWrapper<UserInterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInterfaceInfo::getUserId, user_id);
        queryWrapper.eq(UserInterfaceInfo::getInterfaceId, 1);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);

        if (userInterfaceInfo == null) {
            userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(user_id);
            userInterfaceInfo.setInterfaceId(1L);
            userInterfaceInfo.setTotalNum(0);
            userInterfaceInfo.setLeftNum(10);
            userInterfaceInfoService.save(userInterfaceInfo);
        }

        Integer leftNum = userInterfaceInfo.getLeftNum();
        Integer totalNum = userInterfaceInfo.getTotalNum();
        if (leftNum <= 0) {
            return R.error("AI接口次数不足，请联系管理员");
        }

        boolean rateLimit = guavaRateLimiterManager.doRateLimit(user_id);
        if (!rateLimit) {
            return R.error("请求次数过多，请稍后重试");
        }

        List<Books> list = booksService.list();
        StringBuilder stringBuilder = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>();
        String presetInformation = "请根据数据库内容、用户历史借阅记录和用户输入信息作出个性化图书推荐。" +
                "书籍优先选择数据库里面有的，如果用户喜欢的书籍数据库没有，可以根据自身知识推荐，但不可以超过三本书。" +
                "推荐时要结合用户的借阅偏好，优先推荐相同或相似类别的图书。如果用户问的问题与图书推荐无关，请拒绝回答！" +
                "请以纯文本格式回答，一定不要使用Markdown格式（如标题、列表、加粗等）。";

        stringBuilder.append(presetInformation).append("\n").append("数据库内容: ");
        for (Books books : list) {
            if (!hashSet.contains(books.getBookName())) {
                hashSet.add(books.getBookName());
                stringBuilder.append(books.getBookName());
                stringBuilder.append("(类别:").append(books.getBookType()).append(",");
                if (books.getBookAuthor() != null && !books.getBookAuthor().isEmpty()) {
                    stringBuilder.append("作者:").append(books.getBookAuthor()).append(",");
                }
                if (books.getBookLibrary() != null && !books.getBookLibrary().isEmpty()) {
                    stringBuilder.append("馆藏:").append(books.getBookLibrary()).append(",");
                }
                if (books.getBookLocation() != null && !books.getBookLocation().isEmpty()) {
                    stringBuilder.append("位置:").append(books.getBookLocation()).append(",");
                }
                if (books.getBookStatus() != null) {
                    stringBuilder.append("状态:").append(books.getBookStatus());
                }
                if (books.getBookDescription() != null && !books.getBookDescription().isEmpty()) {
                    stringBuilder.append(",描述:").append(books.getBookDescription());
                }
                stringBuilder.append("),");
            }
        }
        stringBuilder.append("\n");

        stringBuilder.append("用户历史借阅记录: ");
        LambdaQueryWrapper<BookBorrows> borrowQueryWrapper = new LambdaQueryWrapper<>();
        borrowQueryWrapper.eq(BookBorrows::getCardNumber, user_id);
        List<BookBorrows> borrowList = booksBorrowService.list(borrowQueryWrapper);

        if (borrowList != null && !borrowList.isEmpty()) {
            HashSet<String> borrowedBookNames = new HashSet<>();
            HashMap<String, Integer> borrowTypeCount = new HashMap<>();

            for (BookBorrows borrow : borrowList) {
                Books book = booksService.getBookByBookNumber(borrow.getBookNumber());
                if (book != null && !borrowedBookNames.contains(book.getBookName())) {
                    borrowedBookNames.add(book.getBookName());
                    stringBuilder.append(book.getBookName()).append("(").append(book.getBookType()).append("),");
                    borrowTypeCount.merge(book.getBookType(), 1, Integer::sum);
                }
            }

            stringBuilder.append("\n").append("用户借阅偏好分析: ");
            borrowTypeCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        stringBuilder.append(entry.getKey()).append("(")
                                .append(entry.getValue()).append("次), ");
                    });
            if (!borrowTypeCount.isEmpty()) {
                stringBuilder.setLength(stringBuilder.length() - 2);
            }
        } else {
            stringBuilder.append("该用户暂无借阅记录");
        }
        stringBuilder.append("\n");

        stringBuilder.append("用户输入信息: ").append(message).append("\n");

        R<List<AiIntelligent>> history = aiIntelligentService.getAiInformationByUserId(user_id);
        List<AiIntelligent> historyData = history.getData();
        String response;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<SparkMessage> messages = new ArrayList<>();

        if (historyData != null) {
            historyData.forEach(item -> {
                messages.add(SparkMessage.userContent(item.getInputMessage()));
                messages.add(SparkMessage.assistantContent(item.getAiResult()));
            });
        }
        messages.add(SparkMessage.userContent(stringBuilder.toString()));

        SparkRequest sparkRequest = SparkRequest.builder()
                .messages(messages)
                .maxTokens(2048)
                .temperature(0.2)
                .build();

        int timeout = 25;
        Future<String> future = executor.submit(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
                SparkTextUsage textUsage = chatResponse.getTextUsage();
                stopWatch.stop();
                long total = stopWatch.getTotal(TimeUnit.SECONDS);
                System.out.println("本次接口调用耗时:" + total + "秒");
                System.out.println("\n回答：" + chatResponse.getContent());
                System.out.println("\n提问tokens：" + textUsage.getPromptTokens()
                        + "，回答tokens：" + textUsage.getCompletionTokens()
                        + "，总消耗tokens：" + textUsage.getTotalTokens());
                return chatResponse.getContent();
            } catch (Exception exception) {
                throw new RuntimeException("遇到异常");
            }
        });

        try {
            response = future.get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            return R.error("服务器内部错误，请求超时，请稍后重试");
        }
        executor.shutdown();

        AiIntelligent saveResult = new AiIntelligent();
        saveResult.setInputMessage(aiIntelligent.getInputMessage());
        saveResult.setAiResult(response);
        saveResult.setUserId(user_id);
        boolean save = this.save(saveResult);
        if (!save) {
            throw new VueBookException("获取AI推荐信息失败");
        }

        userInterfaceInfo.setLeftNum(leftNum - 1);
        userInterfaceInfo.setTotalNum(totalNum + 1);
        boolean update = userInterfaceInfoService.updateById(userInterfaceInfo);
        if (!update) {
            return R.error("调用接口信息失败");
        }
        return R.success(response, "获取AI推荐信息成功");
    }

    /**
     * 获取用户与AI的聊天记录
     *
     * @param userId 用户ID
     * @return 用户最近5条与AI的聊天记录，按时间升序排列
     */
    @Override
    public R<List<AiIntelligent>> getAiInformationByUserId(Long userId) {
        QueryWrapper<AiIntelligent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null && userId >= 0, "user_id", userId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 5");
        List<AiIntelligent> list = this.list(queryWrapper);
        if (list.size() == 0) {
            return R.success(null, "用户暂时没有和AI的聊天记录");
        }
        Collections.reverse(list);
        return R.success(list, "获取和AI最近的5条聊天记录成功");
    }

    /**
     * 管理员获取AI数据分析结果
     *
     * @param aiIntelligent 包含管理员ID和分析问题的对象
     * @return AI生成的数据分析报告，包含图书统计、用户统计、借阅统计、违规统计等
     */
    @Override
    public R<String> getAdminGenResult(AiIntelligent aiIntelligent) {
        if (!sparkProperties.isConfigured()) {
            return R.error(SPARK_NOT_CONFIGURED);
        }
        String message = aiIntelligent.getInputMessage();
        if (StringUtils.isBlank(message)) {
            return R.error("文本不能为空");
        }
        if (message.length() > 256) {
            return R.error("文本字数过长");
        }
        Long adminId = aiIntelligent.getUserId();

        LambdaQueryWrapper<UserInterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInterfaceInfo::getUserId, adminId);
        UserInterfaceInfo interfaceInfo = userInterfaceInfoService.getOne(queryWrapper);

        if (interfaceInfo == null) {
            interfaceInfo = new UserInterfaceInfo();
            interfaceInfo.setUserId(adminId);
            interfaceInfo.setInterfaceId(0L);
            interfaceInfo.setTotalNum(0);
            interfaceInfo.setLeftNum(100000);
            userInterfaceInfoService.save(interfaceInfo);
        }

        Integer leftNum = interfaceInfo.getLeftNum();
        Integer totalNum = interfaceInfo.getTotalNum();
        if (leftNum <= 0) {
            return R.error("AI接口次数不足");
        }

        boolean rateLimit = guavaRateLimiterManager.doRateLimit(adminId);
        if (!rateLimit) {
            return R.error("请求次数过多，请稍后重试");
        }

        List<Books> booksList = booksService.list();
        List<Users> usersList = usersService.list();
        List<BookBorrows> borrowList = booksBorrowService.list();
        List<Violations> violationsList = violationService.list();

        StringBuilder stringBuilder = new StringBuilder();
        String presetInformation = "你是图书管理系统的智能分析助手。请根据以下提供的完整系统数据，回答管理员的问题。\n" +
                "\n" +
                "【数据说明】\n" +
                "1. 图书统计：包含图书总数、各类别图书数量、库存状态（已借出/可借阅）\n" +
                "2. 用户统计：包含用户总数、用户状态分布、借阅规则分布\n" +
                "3. 借阅统计：包含借阅记录总数、热门借阅图书、各类别借阅次数和借阅率\n" +
                "4. 违规统计：包含违规记录总数和违规类型分布\n" +
                "\n" +
                "【分析要求】\n" +
                "- 当管理员询问采购决策建议时，必须基于上述数据进行分析\n" +
                "- 重点关注：各类别图书的借阅热度（借阅次数）、借阅率（借阅次数/该类别图书数）、库存充足度\n" +
                "- 推荐需要补充采购的书籍类别，并明确说明推荐理由（如：借阅率高但库存少）\n" +
                "- 直接使用数据进行分析，不要说'如果能够获取到数据'之类的话\n" +
                "- 回答要简洁清晰，提供具体数据支持\n" +
                "- 请以纯文本格式回答，一定不要使用Markdown格式（如标题、列表、加粗等）。";

        stringBuilder.append(presetInformation);

        stringBuilder.append("【图书统计】\n");
        stringBuilder.append("- 图书总数: ").append(booksList.size()).append("\n");

        HashMap<String, Integer> typeCountMap = new HashMap<>();
        for (Books book : booksList) {
            typeCountMap.merge(book.getBookType(), 1, Integer::sum);
        }
        stringBuilder.append("- 图书类别数: ").append(typeCountMap.size()).append("\n");
        stringBuilder.append("  类别分布: ");
        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("(")
                    .append(entry.getValue()).append("本), ");
        }
        if (!typeCountMap.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("\n");

        int borrowedCount = 0, availableCount = 0;
        for (Books book : booksList) {
            if ("已借出".equals(book.getBookStatus())) {
                borrowedCount++;
            } else {
                availableCount++;
            }
        }
        stringBuilder.append("- 库存状态: 已借出 ").append(borrowedCount)
                .append("本, 可借阅 ").append(availableCount).append("本\n");

        HashMap<String, Integer> libraryCountMap = new HashMap<>();
        for (Books book : booksList) {
            libraryCountMap.merge(book.getBookLibrary(), 1, Integer::sum);
        }
        stringBuilder.append("- 图书馆分布: ");
        for (Map.Entry<String, Integer> entry : libraryCountMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("(")
                    .append(entry.getValue()).append("本), ");
        }
        if (!libraryCountMap.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("\n");

        stringBuilder.append("\n【用户统计】\n");
        stringBuilder.append("- 用户总数: ").append(usersList.size()).append("\n");

        int activeUserCount = 0, inactiveUserCount = 0;
        for (Users user : usersList) {
            if (user.getStatus() != null && user.getStatus() == 1) {
                activeUserCount++;
            } else {
                inactiveUserCount++;
            }
        }
        stringBuilder.append("- 用户状态: 正常 ").append(activeUserCount)
                .append("人, 禁用 ").append(inactiveUserCount).append("人\n");

        HashMap<Integer, Integer> ruleCountMap = new HashMap<>();
        for (Users user : usersList) {
            if (user.getRuleNumber() != null) {
                ruleCountMap.merge(user.getRuleNumber(), 1, Integer::sum);
            }
        }
        stringBuilder.append("- 借阅规则分布: ");
        for (Map.Entry<Integer, Integer> entry : ruleCountMap.entrySet()) {
            stringBuilder.append("规则").append(entry.getKey()).append("(")
                    .append(entry.getValue()).append("人), ");
        }
        if (!ruleCountMap.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("\n");

        stringBuilder.append("\n【借阅统计】\n");
        stringBuilder.append("- 借阅记录总数: ").append(borrowList.size()).append("\n");

        int borrowingCount = 0;
        for (BookBorrows borrow : borrowList) {
            if (borrow.getReturnDate() == null) {
                borrowingCount++;
            }
        }
        stringBuilder.append("- 当前借阅中: ").append(borrowingCount).append("\n");

        int returnedCount = borrowList.size() - borrowingCount;
        stringBuilder.append("- 已归还: ").append(returnedCount).append("\n");

        HashMap<Long, Integer> bookBorrowCount = new HashMap<>();
        for (BookBorrows borrow : borrowList) {
            if (borrow.getBookNumber() != null) {
                bookBorrowCount.merge(borrow.getBookNumber(), 1, Integer::sum);
            }
        }
        List<Map.Entry<Long, Integer>> allBorrowedBooks = bookBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toList());
        stringBuilder.append("- 全量图书借阅统计(按借阅次数降序): ");
        for (Map.Entry<Long, Integer> entry : allBorrowedBooks) {
            Books book = booksService.getBookByBookNumber(entry.getKey());
            if (book != null) {
                stringBuilder.append(book.getBookName()).append("(")
                        .append(book.getBookType()).append("类, ")
                        .append(entry.getValue()).append("次), ");
            }
        }
        if (!allBorrowedBooks.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("\n");

        int zeroBorrowCount = 0;
        StringBuilder zeroBorrowBooks = new StringBuilder();
        for (Books book : booksList) {
            Integer borrowCount = bookBorrowCount.get(book.getBookNumber());
            if (borrowCount == null || borrowCount == 0) {
                zeroBorrowCount++;
                if (zeroBorrowBooks.length() < 200) {
                    zeroBorrowBooks.append(book.getBookName()).append("(")
                            .append(book.getBookType()).append("), ");
                }
            }
        }
        stringBuilder.append("- 零借阅图书数量: ").append(zeroBorrowCount).append("本");
        if (zeroBorrowBooks.length() > 0) {
            zeroBorrowBooks.setLength(zeroBorrowBooks.length() - 2);
            stringBuilder.append("，包括: ").append(zeroBorrowBooks);
        }
        stringBuilder.append("\n");

        HashMap<String, Integer> borrowTypeCountMap = new HashMap<>();
        for (BookBorrows borrow : borrowList) {
            if (borrow.getBookNumber() != null) {
                Books book = booksService.getBookByBookNumber(borrow.getBookNumber());
                if (book != null && book.getBookType() != null) {
                    borrowTypeCountMap.merge(book.getBookType(), 1, Integer::sum);
                }
            }
        }
        stringBuilder.append("- 按类型借阅分布: ");
        for (Map.Entry<String, Integer> entry : borrowTypeCountMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("(")
                    .append(entry.getValue()).append("次), ");
        }
        if (!borrowTypeCountMap.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("\n");

        stringBuilder.append("- 各类别借阅率分析: ");
        for (Map.Entry<String, Integer> typeEntry : typeCountMap.entrySet()) {
            String type = typeEntry.getKey();
            int bookCount = typeEntry.getValue();
            int borrowCount = borrowTypeCountMap.getOrDefault(type, 0);
            double borrowRate = bookCount > 0 ? (double) borrowCount / bookCount : 0;
            stringBuilder.append(type).append("(")
                    .append(String.format("%.2f", borrowRate * 100)).append("%), ");
        }
        if (!typeCountMap.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("\n");

        List<Map.Entry<String, Integer>> sortedBorrowTypes = borrowTypeCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toList());
        stringBuilder.append("- 热门借阅类别排名与采购分析: ");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedBorrowTypes) {
            String type = entry.getKey();
            int borrowCount = entry.getValue();
            int typeBookCount = typeCountMap.getOrDefault(type, 0);
            double borrowRate = typeBookCount > 0 ? (double) borrowCount / typeBookCount : 0;
            int typeAvailableCount = 0;
            for (Books book : booksList) {
                if (type.equals(book.getBookType()) && !"已借出".equals(book.getBookStatus())) {
                    typeAvailableCount++;
                }
            }
            stringBuilder.append(rank).append(".").append(type)
                    .append("(").append(borrowCount).append("次, ");
            stringBuilder.append(String.format("借阅率%.1f", borrowRate * 100)).append("%, ");
            stringBuilder.append(typeBookCount).append("本库存, ");
            stringBuilder.append(typeAvailableCount).append("本可借)");
            if (borrowRate > 2.0 && typeAvailableCount < 3) {
                stringBuilder.append("[建议补充采购]");
            } else if (borrowRate < 0.1 && typeBookCount > 5) {
                stringBuilder.append("[建议减少库存]");
            }
            if (rank < sortedBorrowTypes.size()) {
                stringBuilder.append(", ");
            }
            rank++;
        }
        stringBuilder.append("\n");

        stringBuilder.append("\n【违规统计】\n");
        stringBuilder.append("- 违规记录总数: ").append(violationsList.size()).append("\n");

        HashMap<String, Integer> violationTypeMap = new HashMap<>();
        for (Violations violations : violationsList) {
            if (violations.getViolationMessage() != null) {
                violationTypeMap.merge(violations.getViolationMessage(), 1, Integer::sum);
            }
        }
        stringBuilder.append("- 违规类型分布: ");
        for (Map.Entry<String, Integer> entry : violationTypeMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("(")
                    .append(entry.getValue()).append("次), ");
        }
        if (!violationTypeMap.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("\n");

        stringBuilder.append("\n管理员问题: ").append(message).append("\n");

        R<List<AiIntelligent>> history = aiIntelligentService.getAiInformationByAdminId(adminId);
        List<AiIntelligent> historyData = history.getData();
        String response;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        List<SparkMessage> messages = new ArrayList<>();
        if (historyData != null) {
            historyData.forEach(item -> {
                messages.add(SparkMessage.userContent(item.getInputMessage()));
                messages.add(SparkMessage.assistantContent(item.getAiResult()));
            });
        }
        messages.add(SparkMessage.userContent(stringBuilder.toString()));

        SparkRequest sparkRequest = SparkRequest.builder()
                .messages(messages)
                .maxTokens(4096)
                .temperature(0.3)
                .build();

        int timeout = 30;
        Future<String> future = executor.submit(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
                SparkTextUsage textUsage = chatResponse.getTextUsage();
                stopWatch.stop();
                long total = stopWatch.getTotal(TimeUnit.SECONDS);
                System.out.println("管理员AI接口调用耗时:" + total + "秒");
                System.out.println("\n回答：" + chatResponse.getContent());
                return chatResponse.getContent();
            } catch (Exception exception) {
                throw new RuntimeException("遇到异常");
            }
        });

        try {
            response = future.get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            return R.error("服务器内部错误，请求超时，请稍后重试");
        }
        executor.shutdown();

        AiIntelligent saveResult = new AiIntelligent();
        saveResult.setInputMessage(aiIntelligent.getInputMessage());
        saveResult.setAiResult(response);
        saveResult.setUserId(adminId);
        boolean save = this.save(saveResult);
        if (!save) {
            throw new VueBookException("获取AI分析信息失败");
        }

        interfaceInfo.setLeftNum(leftNum - 1);
        interfaceInfo.setTotalNum(totalNum + 1);
        userInterfaceInfoService.updateById(interfaceInfo);

        return R.success(response, "获取AI分析信息成功");
    }

    /**
     * 获取管理员与AI的聊天记录
     *
     * @param adminId 管理员ID
     * @return 管理员最近5条与AI的聊天记录，按时间升序排列
     */
    @Override
    public R<List<AiIntelligent>> getAiInformationByAdminId(Long adminId) {
        QueryWrapper<AiIntelligent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(adminId != null && adminId >= 0, "user_id", adminId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 5");
        List<AiIntelligent> list = this.list(queryWrapper);
        if (list.size() == 0) {
            return R.success(null, "管理员暂时没有和AI的聊天记录");
        }
        Collections.reverse(list);
        return R.success(list, "获取管理员和AI最近的5条聊天记录成功");
    }
}