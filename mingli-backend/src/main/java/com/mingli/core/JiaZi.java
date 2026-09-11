package com.mingli.core;

/**
 * 六十甲子工具
 * 序号 0 = 甲子，59 = 癸亥
 */
public final class JiaZi {

    private JiaZi() {}

    /** 60 甲子名称表 */
    private static final String[] NAMES = new String[60];

    static {
        for (int i = 0; i < 60; i++) {
            NAMES[i] = TianGan.of(i).getCn() + DiZhi.of(i).getCn();
        }
    }

    public static String name(int index) {
        return NAMES[((index % 60) + 60) % 60];
    }

    public static int index(TianGan gan, DiZhi zhi) {
        int g = gan.index();
        int z = zhi.index();
        // 求 i 使 i%10==g 且 i%12==z
        for (int i = g; i < 60; i += 10) {
            if (i % 12 == z) {
                return i;
            }
        }
        return -1;
    }

    public static int normalize(int index) {
        return ((index % 60) + 60) % 60;
    }

    public static TianGan ganOf(int index) {
        return TianGan.of(normalize(index));
    }

    public static DiZhi zhiOf(int index) {
        return DiZhi.of(normalize(index));
    }

    /** 由干支名（如"甲子"）解析序号 */
    public static int parse(String name) {
        if (name == null || name.length() < 2) {
            throw new IllegalArgumentException("干支名非法: " + name);
        }
        return index(TianGan.of(name.substring(0, 1)), DiZhi.of(name.substring(1, 2)));
    }

    /** 纳音五行序号：1木 2金 3水 4火 5土 */
    public static int naYinWuXingNo(int index) {
        int g = normalize(index) % 10;
        int z = normalize(index) % 12;
        int ganNo = g / 2 + 1;          // 甲乙1 丙丁2 戊己3 庚辛4 壬癸5
        int zhiNo = (z % 6) / 2 + 1;    // 子丑午未1 寅卯申酉2 辰巳戌亥3
        int sum = ganNo + zhiNo;
        if (sum > 5) {
            sum -= 5;
        }
        return sum;
    }

    private static final WuXing[] NA_YIN_WX = {null, WuXing.WOOD, WuXing.METAL, WuXing.WATER, WuXing.FIRE, WuXing.EARTH};

    public static WuXing naYinWuXing(int index) {
        return NA_YIN_WX[naYinWuXingNo(index)];
    }

    /** 三十纳音名，每名管两对干支 */
    private static final String[] NA_YIN_NAMES = {
            "海中金", "炉中火", "大林木", "路旁土", "剑锋金",
            "山头火", "涧下水", "城头土", "白蜡金", "杨柳木",
            "泉中水", "屋上土", "霹雳火", "松柏木", "长流水",
            "沙中金", "山下火", "平地木", "壁上土", "金箔金",
            "覆灯火", "天河水", "大驿土", "钗钏金", "桑柘木",
            "大溪水", "沙中土", "天上火", "石榴木", "大海水"
    };

    public static String naYin(int index) {
        return NA_YIN_NAMES[normalize(index) / 2];
    }

    /** 生肖 */
    public static String shengXiao(int index) {
        return zhiOf(index).getShengXiao();
    }
}
