package com.book.backend.pojo.dto;

import com.book.backend.pojo.Violations;
import lombok.Data;

import java.io.Serializable;

/**
 * 违章记录DTO，扩展管理员姓名和逾期天数
 */
@Data
public class ViolationsDTO extends Violations implements Serializable {
    /**
     * 处理管理员姓名
     */
    public String violationAdmin;

    /**
     * 逾期天数
     */
    public long expireDays;
}
