package com.book.backend.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.R;
import com.book.backend.common.exception.ErrorCode;
import com.book.backend.common.exception.ThrowUtils;
import com.book.backend.manager.GuavaRateLimiterManager;
import com.book.backend.manager.SparkClient;
import com.book.backend.manager.SparkProperties;
import com.book.backend.manager.model.SparkMessage;
import com.book.backend.manager.model.SparkSyncChatResponse;
import com.book.backend.manager.model.request.SparkRequest;
import com.book.backend.mapper.ChartMapper;
import com.book.backend.pojo.Admins;
import com.book.backend.pojo.Charts;
import com.book.backend.pojo.UserInterfaceInfo;
import com.book.backend.pojo.dto.chart.GenChartByAiRequest;
import com.book.backend.pojo.vo.BiResponse;
import com.book.backend.service.AdminsService;
import com.book.backend.service.ChartService;
import com.book.backend.utils.ExcelUtils;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图表服务实现类
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Charts>
        implements ChartService {

    /**
     * 讯飞星火客户端实例
     */
    private final SparkClient sparkClient = new SparkClient();

    /**
     * 未配置星火凭证时的统一提示，避免直接抛 NPE 或发一次注定失败的请求。
     */
    private static final String SPARK_NOT_CONFIGURED =
            "AI 服务未配置：请在 application-local.yml 或环境变量中填写 SPARK_APP_ID / SPARK_API_KEY / SPARK_API_SECRET";
    @Resource
    private AdminsService adminsService;
    @Resource
    private GuavaRateLimiterManager guavaRateLimiterManager;
    @Resource
    private UserInterfaceInfoServiceImpl userInterfaceInfoService;
    @Resource
    private SparkProperties sparkProperties;

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
     * 使用AI生成图表
     *
     * @param multipartFile       Excel文件（支持xlsx、xls格式，大小不超过1M）
     * @param genChartByAiRequest 图表生成请求参数（包含名称、目标、图表类型、管理员ID）
     * @return 图表生成结果，包含ECharts配置和数据分析结论
     */
    @Override
    public R<BiResponse> genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest) {
        if (!sparkProperties.isConfigured()) {
            return R.error(SPARK_NOT_CONFIGURED);
        }
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");

        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        Admins admin = adminsService.getById(genChartByAiRequest.getAdminId());
        LambdaQueryWrapper<UserInterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInterfaceInfo::getUserId, admin.getAdminId());
        UserInterfaceInfo interfaceInfo = userInterfaceInfoService.getOne(queryWrapper);

        if (interfaceInfo == null) {
            interfaceInfo = new UserInterfaceInfo();
            interfaceInfo.setUserId(admin.getAdminId());
            interfaceInfo.setInterfaceId(0L);
            interfaceInfo.setTotalNum(0);
            interfaceInfo.setLeftNum(100000);
            userInterfaceInfoService.save(interfaceInfo);
        }

        Integer leftNum = interfaceInfo.getLeftNum();
        Integer totalNum = interfaceInfo.getTotalNum();
        if (leftNum <= 0) {
            return R.error("AI接口次数不足，请明天再来");
        }

        boolean limit = guavaRateLimiterManager.doRateLimit(admin.getAdminId());
        if (!limit) {
            return R.error("请求次数过多，请稍后重试");
        }

        String csvData = ExcelUtils.excelToCsv(multipartFile);

        StringBuilder userInput = new StringBuilder();
        userInput.append("你是一个数据分析师和前端开发专家。\n");
        userInput.append("分析需求：").append(goal).append("\n");
        if (StringUtils.isNotBlank(chartType)) {
            userInput.append("请务必使用").append(chartType).append("进行展示。\n");
        }
        userInput.append("原始数据：\n").append(csvData).append("\n");
        userInput.append("\n请严格按照以下格式输出，不要包含任何 markdown 标记或额外解释：\n");
        userInput.append("输出格式要求：请以纯文本格式输出，不要使用任何Markdown格式（如标题、列表、加粗、斜体、代码块等）。\n");
        userInput.append("【【【【【\n");
        userInput.append("option = {这里是 ECharts V5 的 JSON 配置}\n");
        userInput.append("【【【【【\n");
        userInput.append("这里是详细的数据分析结论\n");

        String result;
        try {
            // 构建请求消息
            java.util.List<SparkMessage> messages = new java.util.ArrayList<>();
            messages.add(SparkMessage.userContent(userInput.toString()));

            // 构建请求参数
            SparkRequest sparkRequest = SparkRequest.builder()
                    .messages(messages)
                    .maxTokens(4096)
                    .temperature(0.5)
                    .build();

            // 调用 SparkClient 同步接口
            SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
            result = chatResponse.getContent();

            System.out.println("\n================== AI 原始返回 Start ==================");
            System.out.println(result);
            System.out.println("================== AI 原始返回 End ==================\n");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI 调用失败: " + e.getMessage(), e);
        }

        if (!result.contains("【【【【【")) {
            return R.error("AI 未按约定格式输出，请重试。AI 返回: " + result);
        }

        String[] splits = result.split("【【【【【");
        if (splits.length < 2) {
            return R.error("AI 输出内容不完整，请重试。");
        }

        String chartCodePart = splits[1].trim();
        String genChart = "";

        Pattern pattern = Pattern.compile("option\\s*=\\s*([\\s\\S]*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(chartCodePart);

        if (matcher.find()) {
            genChart = matcher.group(1).trim();
        } else {
            genChart = chartCodePart;
        }

        if (genChart.endsWith(";")) {
            genChart = genChart.substring(0, genChart.length() - 1).trim();
        }

        String genResult = "";
        if (splits.length >= 3 && StringUtils.isNotBlank(splits[2].trim())) {
            genResult = splits[2].trim();
        } else {
            genResult = "根据数据生成了图表，请查看图表内容进行分析。";
        }

        Gson gson = new Gson();
        Object objectChart;
        try {
            objectChart = gson.fromJson(genChart, Object.class);
        } catch (Exception e) {
            System.err.println("JSON 解析失败，尝试解析的内容: " + genChart);
            return R.error("生成的图表配置格式错误，无法解析。");
        }

        Charts charts = new Charts();
        charts.setName(name);
        charts.setGoal(goal);
        charts.setChartData(csvData);
        charts.setChartType(chartType);
        charts.setGenChart(genChart.trim());
        charts.setGenResult(genResult);
        charts.setAdminId(genChartByAiRequest.getAdminId());
        boolean saveResult = this.save(charts);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        interfaceInfo.setLeftNum(leftNum - 1);
        interfaceInfo.setTotalNum(totalNum + 1);
        boolean update = userInterfaceInfoService.updateById(interfaceInfo);
        if (!update) {
            return R.error("调用接口失败");
        }

        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(charts.getId());

        R<BiResponse> resultData = new R<>();
        resultData.add("genChart", objectChart);
        resultData.setData(biResponse);
        resultData.setMsg("图表生成成功");
        resultData.setStatus(200);
        return resultData;
    }
}