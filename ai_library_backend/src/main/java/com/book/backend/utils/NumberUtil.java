package com.book.backend.utils;

import java.math.BigDecimal;
import java.util.Random;

/**
 * 数字生成工具类
 */
public class NumberUtil {

    /**
     * 生成图书馆编号(0-2)
     *
     * @return 随机图书馆编号
     */
    public static Integer getLibraryInt() {
        return new Random().nextInt(3);
    }

    /**
     * 生成状态值(0-4)
     *
     * @return 随机状态值
     */
    public static Integer getStatusInt() {
        return new Random().nextInt(5);
    }

    /**
     * 生成年龄(10-59)
     *
     * @return 随机年龄
     */
    public static Integer getAgeInt() {
        return new Random().nextInt(50) + 10;
    }

    /**
     * 生成弹幕时间(5-9秒)
     *
     * @return 随机时间
     */
    public static Integer getBarrageTime() {
        return new Random().nextInt(5) + 5;
    }

    /**
     * 生成性别值(0或1)
     *
     * @return 随机性别值
     */
    public static Integer getSexInt() {
        return new Random().nextInt(2);
    }

    /**
     * 生成等级值(0-99)
     *
     * @return 随机等级值
     */
    public static Integer getLevelInt() {
        return new Random().nextInt(100);
    }

    /**
     * 生成账户金额
     *
     * @return 随机金额(保留4位小数)
     */
    public static BigDecimal accountDecimal() {
        float minF = 1000000.0f;
        float maxF = 1000.0f;
        BigDecimal bd = new BigDecimal(Math.random() * (maxF - minF) + minF);
        return bd.setScale(4, BigDecimal.ROUND_DOWN);
    }

    /**
     * 生成余额
     *
     * @return 随机余额(保留4位小数)
     */
    public static BigDecimal balanceDecimal() {
        float minF = 1000000.0f;
        float maxF = 1000.0f;
        BigDecimal bd = new BigDecimal(Math.random() * (maxF - minF) + minF);
        return bd.setScale(4, BigDecimal.ROUND_DOWN);
    }

    /**
     * 生成随机长整型数
     *
     * @return 随机长整型数
     */
    public static Long randomLong() {
        return Math.abs(new Random().nextLong());
    }

    /**
     * 生成指定长度的随机数字字符串
     *
     * @param num 数字字符串长度
     * @return 随机数字字符串
     */
    public static StringBuilder getNumber(int num) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            sb.append(random.nextInt(10));
        }
        return sb;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            Integer libraryInt = getLibraryInt();
            System.out.println(libraryInt);
        }
    }

}

