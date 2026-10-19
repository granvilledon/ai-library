package com.book.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.R;
import com.book.backend.pojo.Comments;
import com.book.backend.pojo.dto.CommentDTO;

import java.util.List;

/**
 * 评论弹幕服务接口
 */
public interface CommentService extends IService<Comments> {

    /**
     * 获取弹幕列表
     *
     * @return 弹幕列表
     */
    R<List<CommentDTO>> getCommentList();

    /**
     * 添加弹幕
     *
     * @param commentDTO 弹幕信息
     * @return 操作结果
     */
    R<String> addComment(CommentDTO commentDTO);
}
