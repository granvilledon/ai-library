package com.book.backend.utils;

import java.util.Random;

/**
 * 随机昵称生成工具类
 */
public class RandomNameUtils {

    private final static String[] ADJECTIVES = {
            "可爱的", "温柔的", "帅气的", "漂亮的", "聪明的",
            "快乐的", "阳光的", "神秘的", "优雅的", "活泼的",
            "安静的", "勇敢的", "善良的", "迷人的", "梦幻的",
            "浪漫的", "清新的", "灵动的", "璀璨的", "绚丽的",
            "甜蜜的", "温暖的", "纯真的", "俏皮的", "呆萌的",
            "高冷的", "慵懒的", "傲娇的", "软萌的", "佛系的",
            "元气的", "治愈的", "文艺的", "森系的", "复古的",
            "潮流的", "酷炫的", "霸气的", "潇洒的", "随性的",
            "清澈的", "灿烂的", "悠然的", "静谧的", "祥和的"
    };

    private final static String[] NOUNS = {
            "小猫", "小狗", "兔子", "熊猫", "考拉",
            "狐狸", "小鹿", "松鼠", "仓鼠", "企鹅",
            "云朵", "星星", "月亮", "太阳", "彩虹",
            "花朵", "树叶", "蝴蝶", "蜜蜂", "萤火虫",
            "糖果", "蛋糕", "奶茶", "饼干", "冰淇淋",
            "精灵", "天使", "恶魔", "骑士", "魔法师",
            "探险家", "旅行家", "梦想家", "艺术家", "美食家",
            "小王子", "小公主", "甜心", "宝贝", "小可爱",
            "冒险家", "航海家", "追风者", "守护者", "追光者",
            "蒲公英", "向日葵", "薰衣草", "樱花", "玫瑰"
    };

    /**
     * 生成形容词+名词格式的随机昵称
     *
     * @return 随机昵称
     */
    public static String generateNickname() {
        Random random = new Random();
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        return adjective + noun;
    }

    /**
     * 生成带数字后缀的随机昵称
     *
     * @return 随机昵称（格式：形容词+名词+数字）
     */
    public static String generateNicknameWithNumber() {
        Random random = new Random();
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int num = random.nextInt(100);
        return adjective + noun + num;
    }
}