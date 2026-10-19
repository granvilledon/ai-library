package com.book.backend.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 借阅日期工具类
 */
public class BorrowDateUtil {

    /**
     * 获取最近4周的时间区间数组
     *
     * @param now 当前时间
     * @return 时间区间数组，格式为"yyyy-MM-dd-yyyy-MM-dd"
     */
    public static String[] getDateArray(LocalDateTime now) {
        String nowFormat = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime time1 = now.minusWeeks(1);
        LocalDateTime time2 = now.minusWeeks(2);
        LocalDateTime time3 = now.minusWeeks(3);
        LocalDateTime time4 = now.minusWeeks(4);
        String format1 = time1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String format2 = time2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String format3 = time3.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String format4 = time4.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String[] dateArrays = new String[4];
        dateArrays[3] = format1 + "-" + nowFormat;
        dateArrays[2] = format2 + "-" + format1;
        dateArrays[1] = format3 + "-" + format2;
        dateArrays[0] = format4 + "-" + format3;
        return dateArrays;
    }

}
