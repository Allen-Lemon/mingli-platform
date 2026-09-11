package com.mingli.core;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 十天干
 */
public enum TianGan {
    JIA("甲", WuXing.WOOD, true),
    YI("乙", WuXing.WOOD, false),
    BING("丙", WuXing.FIRE, true),
    DING("丁", WuXing.FIRE, false),
    WU("戊", WuXing.EARTH, true),
    JI("己", WuXing.EARTH, false),
    GENG("庚", WuXing.METAL, true),
    XIN("辛", WuXing.METAL, false),
    REN("壬", WuXing.WATER, true),
    GUI("癸", WuXing.WATER, false);

    private final String cn;
    private final WuXing wuXing;
    private final boolean yang;

    TianGan(String cn, WuXing wuXing, boolean yang) {
        this.cn = cn;
        this.wuXing = wuXing;
        this.yang = yang;
    }

    @JsonValue
    public String getCn() { return cn; }
    public WuXing getWuXing() { return wuXing; }
    public boolean isYang() { return yang; }
    public int index() { return ordinal(); }

    public static TianGan of(int index) {
        int i = ((index % 10) + 10) % 10;
        return values()[i];
    }

    public static TianGan of(String cn) {
        for (TianGan g : values()) {
            if (g.cn.equals(cn)) {
                return g;
            }
        }
        throw new IllegalArgumentException("未知天干: " + cn);
    }

    /** 五合：甲己合土、乙庚合金、丙辛合水、丁壬合木、戊癸合火 */
    public TianGan he() {
        switch (this) {
            case JIA:  return JI;
            case JI:   return JIA;
            case YI:   return GENG;
            case GENG: return YI;
            case BING: return XIN;
            case XIN:  return BING;
            case DING: return REN;
            case REN:  return DING;
            case WU:   return GUI;
            case GUI:  return WU;
            default:   return null;
        }
    }

    /** 相冲：甲庚、乙辛、丙壬、丁癸 */
    public TianGan chong() {
        switch (this) {
            case JIA:  return GENG;
            case GENG: return JIA;
            case YI:   return XIN;
            case XIN:  return YI;
            case BING: return REN;
            case REN:  return BING;
            case DING: return GUI;
            case GUI:  return DING;
            default:   return null;
        }
    }
}
