package com.mingli.core;

/**
 * 十神
 */
public enum ShiShen {
    BI_JIAN("比肩", "同我·同阴阳"),
    JIE_CAI("劫财", "同我·异阴阳"),
    SHI_SHEN("食神", "我生·同阴阳"),
    SHANG_GUAN("伤官", "我生·异阴阳"),
    PIAN_CAI("偏财", "我克·同阴阳"),
    ZHENG_CAI("正财", "我克·异阴阳"),
    QI_SHA("七杀", "克我·同阴阳"),
    ZHENG_GUAN("正官", "克我·异阴阳"),
    PIAN_YIN("偏印", "生我·同阴阳"),
    ZHENG_YIN("正印", "生我·异阴阳");

    private final String cn;
    private final String def;

    ShiShen(String cn, String def) {
        this.cn = cn;
        this.def = def;
    }

    public String getCn() { return cn; }
    public String getDef() { return def; }

    /**
     * 求日干对目标干的十神
     */
    public static ShiShen of(TianGan dayMaster, TianGan target) {
        WuXing me = dayMaster.getWuXing();
        WuXing other = target.getWuXing();
        boolean sameYinYang = (dayMaster.isYang() == target.isYang());
        String rel = me.relationTo(other);
        switch (rel) {
            case "同":
                return sameYinYang ? BI_JIAN : JIE_CAI;
            case "我生":
                return sameYinYang ? SHI_SHEN : SHANG_GUAN;
            case "我克":
                return sameYinYang ? PIAN_CAI : ZHENG_CAI;
            case "克我":
                return sameYinYang ? QI_SHA : ZHENG_GUAN;
            case "生我":
                return sameYinYang ? PIAN_YIN : ZHENG_YIN;
            default:
                return null;
        }
    }

    /** 是否为财星 */
    public boolean isCai() { return this == ZHENG_CAI || this == PIAN_CAI; }
    /** 是否为官杀 */
    public boolean isGuanSha() { return this == ZHENG_GUAN || this == QI_SHA; }
    /** 是否为印星 */
    public boolean isYin() { return this == ZHENG_YIN || this == PIAN_YIN; }
    /** 是否为食伤 */
    public boolean isShiShang() { return this == SHI_SHEN || this == SHANG_GUAN; }
    /** 是否为比劫 */
    public boolean isBiJie() { return this == BI_JIAN || this == JIE_CAI; }
}
