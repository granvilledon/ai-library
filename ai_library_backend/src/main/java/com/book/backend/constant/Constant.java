package com.book.backend.constant;

/**
 * 系统常量类，统一管理常量值，避免魔法值
 */
public class Constant {

    /**
     * 字符串"null"
     */
    public static final String NULL = "null";

    /**
     * HTTP预检请求方法
     */
    public static final String OPTIONS = "OPTIONS";

    /**
     * 账号可用状态标识
     */
    public static final Integer AVAILABLE = 1;

    /**
     * 账号禁用状态标识
     */
    public static final Integer DISABLE = 0;

    /**
     * 图书已借出状态
     */
    public static final String BOOKDISABLE = "已借出";

    /**
     * 图书未借出状态
     */
    public static final String BOOKAVAILABLE = "未借出";

    /**
     * 用户可用状态
     */
    public static final String USERAVAILABLE = "可用";

    /**
     * 用户禁用状态
     */
    public static final String USERDISABLE = "禁用";

    /**
     * MD5加密密码长度阈值，超过此长度判定为已加密
     */
    public static final Integer MD5PASSWORD = 30;

    /**
     * 升序排序标识
     */
    public static final String SORT_ORDER_ASC = "ascend";

    /**
     * 降序排序标识
     */
    public static final String SORT_ORDER_DESC = "descend";

}
