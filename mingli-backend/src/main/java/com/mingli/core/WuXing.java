package com.mingli.core;

/**
 * 五行
 */
public enum WuXing {
    WOOD("木"),
    FIRE("火"),
    EARTH("土"),
    METAL("金"),
    WATER("水");

    private final String cn;

    WuXing(String cn) {
        this.cn = cn;
    }

    public String getCn() {
        return cn;
    }

    /** 我生者 */
    public WuXing sheng() {
        switch (this) {
            case WOOD:  return FIRE;
            case FIRE:  return EARTH;
            case EARTH: return METAL;
            case METAL: return WATER;
            case WATER: return WOOD;
            default:    return null;
        }
    }

    /** 生我者 */
    public WuXing shengBy() {
        switch (this) {
            case WOOD:  return WATER;
            case FIRE:  return WOOD;
            case EARTH: return FIRE;
            case METAL: return EARTH;
            case WATER: return METAL;
            default:    return null;
        }
    }

    /** 我克者 */
    public WuXing ke() {
        switch (this) {
            case WOOD:  return EARTH;
            case FIRE:  return METAL;
            case EARTH: return WATER;
            case METAL: return WOOD;
            case WATER: return FIRE;
            default:    return null;
        }
    }

    /** 克我者 */
    public WuXing keBy() {
        switch (this) {
            case WOOD:  return METAL;
            case FIRE:  return WATER;
            case EARTH: return WOOD;
            case METAL: return FIRE;
            case WATER: return EARTH;
            default:    return null;
        }
    }

    /** 生克关系判定：同 / 我生 / 生我 / 我克 / 克我 */
    public String relationTo(WuXing other) {
        if (this == other) {
            return "同";
        }
        if (this.sheng() == other) {
            return "我生";
        }
        if (this.shengBy() == other) {
            return "生我";
        }
        if (this.ke() == other) {
            return "我克";
        }
        return "克我";
    }
}
