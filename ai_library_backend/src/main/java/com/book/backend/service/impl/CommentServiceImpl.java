package com.book.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.backend.common.R;
import com.book.backend.mapper.CommentMapper;
import com.book.backend.pojo.Comments;
import com.book.backend.pojo.dto.CommentDTO;
import com.book.backend.service.CommentService;
import com.book.backend.utils.NumberUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论弹幕服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comments>
        implements CommentService {

    /**
     * 获取弹幕评论列表
     *
     * @return 弹幕评论DTO列表，包含弹幕内容、时间和样式
     */
    @Override
    public R<List<CommentDTO>> getCommentList() {
        R<List<CommentDTO>> result = new R<>();
        List<Comments> list = this.list();
        if (list.isEmpty()) {
            result.setStatus(404);
            result.setMsg("弹幕列表为空");
            return result;
        }
        List<CommentDTO> commentDTOList = list.stream().map((item) -> {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(item.getCommentId());
            commentDTO.setMsg(item.getCommentMessage());
            commentDTO.setTime(item.getCommentTime());
            commentDTO.setBarrageStyle(item.getCommentBarrageStyle());
            return commentDTO;
        }).collect(Collectors.toList());
        result.setData(commentDTOList);
        result.setMsg("获取弹幕列表成功");
        result.setStatus(200);
        return result;
    }

    /**
     * 添加弹幕评论
     *
     * @param commentDTO 弹幕评论信息（包含弹幕内容）
     * @return 操作结果，成功返回"添加弹幕成功"，弹幕时间和样式自动生成
     */
    @Override
    public R<String> addComment(CommentDTO commentDTO) {
        String[] barrageStyleArrays = {"yibai", "erbai", "sanbai", "sibai", "wubai", "liubai", "qibai", "babai", "jiubai", "yiqian"};
        Comments comments = new Comments();
        comments.setCommentId(null);
        comments.setCommentTime(NumberUtil.getBarrageTime());
        comments.setCommentMessage(commentDTO.getMsg());
        long index = Math.round(Math.random() * 10);
        if (index == 10) {
            index = 9L;
        }
        String s = Long.toString(index);
        int newIndex = Integer.parseInt(s);
        comments.setCommentBarrageStyle(barrageStyleArrays[newIndex]);
        boolean flag = this.save(comments);
        if (!flag) {
            return R.error("添加弹幕失败");
        }
        return R.success(null, "添加弹幕成功");
    }
}