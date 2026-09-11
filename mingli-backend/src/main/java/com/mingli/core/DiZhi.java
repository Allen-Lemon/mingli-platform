package com.mingli.core;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 十二地支
 */
public enum DiZhi {
    ZI("子", WuXing.WATER, true, "鼠"),
    CHOU("丑", WuXing.EARTH, false, "牛"),
    YIN("寅", WuXing.WOOD, true, "虎"),
    MAO("卯", WuXing.WOOD, false, "兔"),
    CHEN("辰", WuXing.EARTH, true, "龙"),
    SI("巳", WuXing.FIRE, false, "蛇"),
    WU("午", WuXing.FIRE, true, "马"),
    WEI("未", WuXing.EARTH, false, "羊"),
    SHEN("申", WuXing.METAL, true, "猴"),
    YOU("酉", WuXing.METAL, false, "鸡"),
    XU("戌", WuXing.EARTH, true, "狗"),
    HAI("亥", WuXing.WATER, false, "猪");

    private final String cn;
    private final WuXing wuXing;
    private final boolean yang;
    private final String shengXiao;

    DiZhi(String cn, WuXing wuXing, boolean yang, String shengXiao) {
        this.cn = cn;
        this.wuXing = wuXing;
        this.yang = yang;
        this.shengXiao = shengXiao;
    }

    @JsonValue
    public String getCn() { return cn; }
    public WuXing getWuXing() { return wuXing; }
    public boolean isYang() { return yang; }
    public String getShengXiao() { return shengXiao; }
    public int index() { return ordinal(); }

    public static DiZhi of(int index) {
        int i = ((index % 12) + 12) % 12;
        return values()[i];
    }

    public static DiZhi of(String cn) {
        for (DiZhi z : values()) {
            if (z.cn.equals(cn)) {
                return z;
            }
        }
        throw new IllegalArgumentException("未知地支: " + cn);
    }

    /** 六冲：子午、丑未、寅申、卯酉、辰戌、巳亥 */
    public DiZhi chong() {
        return of(this.index() + 6);
    }

    /** 六合：子丑合、寅亥合、卯戌合、辰酉合、巳申合、午未合 */
    public DiZhi liuHe() {
        switch (this) {
            case ZI:   return CHOU;
            case CHOU: return ZI;
            case YIN:  return HAI;
            case HAI:  return YIN;
            case MAO:  return XU;
            case XU:   return MAO;
            case CHEN: return YOU;
            case YOU:  return CHEN;
            case SI:   return SHEN;
            case SHEN: return SI;
            case WU:   return WEI;
            case WEI:  return WU;
            default:   return null;
        }
    }

    /** 六害（穿）：子未、丑午、寅巳、卯辰、申亥、酉戌 */
    public DiZhi chuan() {
        switch (this) {
            case ZI:   return WEI;
            case WEI:  return ZI;
            case CHOU: return WU;
            case WU:   return CHOU;
            case YIN:  return SI;
            case SI:   return YIN;
            case MAO:  return CHEN;
            case CHEN: return MAO;
            case SHEN: return HAI;
            case HAI:  return SHEN;
            case YOU:  return XU;
            case XU:   return YOU;
            default:   return null;
        }
    }

    /** 三合局：申子辰水、亥卯未木、寅午戌火、巳酉丑金，返回同局之支 */
    public DiZhi[] sanHeJu() {
        int base = this.index() % 4;
        return new DiZhi[]{of(base), of(base + 4), of(base + 8)};
    }

    /** 三会方：寅卯辰木、巳午未火、申酉戌金、亥子丑水 */
    public DiZhi[] sanHuiFang() {
        int base = (this.index() / 3) * 3;
        return new DiZhi[]{of(base), of(base + 1), of(base + 2)};
    }

    /**
     * 三刑
     * 无恩之刑：寅刑巳、巳刑申、申刑寅
     * 恃势之刑：丑刑戌、戌刑未、未刑丑
     * 无礼之刑：子刑卯、卯刑子
     * 自刑：辰午酉亥
     */
    public DiZhi[] xing() {
        switch (this) {
            case YIN:  return new DiZhi[]{SI, SHEN};
            case SI:   return new DiZhi[]{SHEN, YIN};
            case SHEN: return new DiZhi[]{YIN, SI};
            case CHOU: return new DiZhi[]{XU, WEI};
            case XU:   return new DiZhi[]{WEI, CHOU};
            case WEI:  return new DiZhi[]{CHOU, XU};
            case ZI:   return new DiZhi[]{MAO};
            case MAO:  return new DiZhi[]{ZI};
            default:   return new DiZhi[0];
        }
    }

    /** 自刑：辰午酉亥 */
    public boolean isZiXing() {
        return this == CHEN || this == WU || this == YOU || this == HAI;
    }

    /** 四墓库：辰戌丑未 */
    public boolean isMuKu() {
        return this == CHEN || this == XU || this == CHOU || this == WEI;
    }

    /** 桃花（子午卯酉为四正沐浴之地） */
    public boolean isTaoHua() {
        return this == ZI || this == WU || this == MAO || this == YOU;
    }
}
