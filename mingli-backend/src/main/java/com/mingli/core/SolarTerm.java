package com.mingli.core;

/**
 * 二十四节气（按年内顺序，起于小寒）
 */
public enum SolarTerm {
    XIAO_HAN("小寒", 285, true, DiZhi.CHOU),
    DA_HAN("大寒", 300, false, null),
    LI_CHUN("立春", 315, true, DiZhi.YIN),
    YU_SHUI("雨水", 330, false, null),
    JING_ZHE("惊蛰", 345, true, DiZhi.MAO),
    CHUN_FEN("春分", 0, false, null),
    QING_MING("清明", 15, true, DiZhi.CHEN),
    GU_YU("谷雨", 30, false, null),
    LI_XIA("立夏", 45, true, DiZhi.SI),
    XIAO_MAN("小满", 60, false, null),
    MANG_ZHONG("芒种", 75, true, DiZhi.WU),
    XIA_ZHI("夏至", 90, false, null),
    XIAO_SHU("小暑", 105, true, DiZhi.WEI),
    DA_SHU("大暑", 120, false, null),
    LI_QIU("立秋", 135, true, DiZhi.SHEN),
    CHU_SHU("处暑", 150, false, null),
    BAI_LU("白露", 165, true, DiZhi.YOU),
    QIU_FEN("秋分", 180, false, null),
    HAN_LU("寒露", 195, true, DiZhi.XU),
    SHUANG_JIANG("霜降", 210, false, null),
    LI_DONG("立冬", 225, true, DiZhi.HAI),
    XIAO_XUE("小雪", 240, false, null),
    DA_XUE("大雪", 255, true, DiZhi.ZI),
    DONG_ZHI("冬至", 270, false, null);

    private final String cn;
    /** 太阳视黄经（春分为 0°） */
    private final int degree;
    /** 是否为「节」（非中气），月柱与起运以节为界 */
    private final boolean jie;
    /** 若为节，该节所主之月支 */
    private final DiZhi monthZhi;

    SolarTerm(String cn, int degree, boolean jie, DiZhi monthZhi) {
        this.cn = cn;
        this.degree = degree;
        this.jie = jie;
        this.monthZhi = monthZhi;
    }

    public String getCn() { return cn; }
    public int getDegree() { return degree; }
    public boolean isJie() { return jie; }
    public DiZhi getMonthZhi() { return monthZhi; }

    public static SolarTerm of(String cn) {
        for (SolarTerm t : values()) {
            if (t.cn.equals(cn)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知节气: " + cn);
    }
}
