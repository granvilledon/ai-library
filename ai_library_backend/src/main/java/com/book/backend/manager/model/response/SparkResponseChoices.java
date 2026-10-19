package com.book.backend.manager.model.response;

import com.book.backend.manager.model.SparkMessage;

import java.io.Serializable;
import java.util.List;

/**
 * 响应选择器对象 ($.payload.choices)
 */
public class SparkResponseChoices implements Serializable {
    private static final long serialVersionUID = 3908073548592366629L;

    /**
     * 文本响应状态(0-首个结果, 1-中间结果, 2-最后结果)
     */
    private Integer status;

    /**
     * 返回数据序号(取值范围[0,9999999])
     */
    private Integer seq;

    /**
     * 消息列表
     */
    private List<SparkMessage> text;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public List<SparkMessage> getText() {
        return text;
    }

    public void setText(List<SparkMessage> text) {
        this.text = text;
    }
}
