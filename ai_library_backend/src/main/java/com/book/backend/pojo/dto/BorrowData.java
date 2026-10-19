package com.book.backend.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 借阅统计数据，按周统计
 */
@Data
public class BorrowData implements Serializable {
    /**
     * 借阅时间段数组(每周为一个间隔)
     */
    public String[] borrowDates;

    /**
     * 借阅量数组(每个数值代表一周的借阅量)
     */
    public Integer[] borrowNumber;

    public BorrowData(String[] borrowDates, Integer[] borrowNumber) {
        this.borrowDates = borrowDates;
        this.borrowNumber = borrowNumber;
    }
}
