package com.book.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.R;
import com.book.backend.pojo.AiIntelligent;

import java.util.List;

/**
 * AI智能分析服务接口
 */
public interface AiIntelligentService extends IService<AiIntelligent> {

    /**
     * 调用AI接口获取推荐图书
     *
     * @param aiIntelligent AI分析请求
     * @return AI生成结果
     */
    R<String> getGenResult(AiIntelligent aiIntelligent);

    /**
     * 获取用户最近5条AI对话记录
     *
     * @param userId 用户ID
     * @return AI对话列表
     */
    R<List<AiIntelligent>> getAiInformationByUserId(Long userId);

    /**
     * 管理员调用AI接口获取数据分析建议
     *
     * @param aiIntelligent AI分析请求
     * @return AI生成结果
     */
    R<String> getAdminGenResult(AiIntelligent aiIntelligent);

    /**
     * 获取管理员最近5条AI对话记录
     *
     * @param adminId 管理员ID
     * @return AI对话列表
     */
    R<List<AiIntelligent>> getAiInformationByAdminId(Long adminId);
}