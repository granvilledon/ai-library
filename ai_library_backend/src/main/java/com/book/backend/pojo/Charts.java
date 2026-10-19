package com.book.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 图表信息实体类
 */
@TableName(value = "charts")
@Data
public class Charts implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 图表ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 原始数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 生成的图表
     */
    private String genChart;

    /**
     * 分析结论
     */
    private String genResult;

    /**
     * 状态(wait/running/succeed/failed)
     */
    private String status;

    /**
     * 执行信息
     */
    private String execMessage;

    /**
     * 创建管理员ID
     */
    private Long adminId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer isDelete;
}
