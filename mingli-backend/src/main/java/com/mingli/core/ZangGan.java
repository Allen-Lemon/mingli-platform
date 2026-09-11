package com.mingli.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 地支藏干（本气 / 中气 / 余气）
 */
public final class ZangGan {

    private ZangGan() {}

    /** 藏干表：本气为主，依次中气、余气 */
    private static final TianGan[][] TABLE = {
            {TianGan.GUI},                                  // 子
            {TianGan.JI, TianGan.GUI, TianGan.XIN},         // 丑
            {TianGan.JIA, TianGan.BING, TianGan.WU},        // 寅
            {TianGan.YI},                                   // 卯
            {TianGan.WU, TianGan.YI, TianGan.GUI},          // 辰
            {TianGan.BING, TianGan.WU, TianGan.GENG},       // 巳
            {TianGan.DING, TianGan.JI},                     // 午
            {TianGan.JI, TianGan.DING, TianGan.YI},         // 未
            {TianGan.GENG, TianGan.REN, TianGan.WU},        // 申
            {TianGan.XIN},                                  // 酉
            {TianGan.WU, TianGan.XIN, TianGan.DING},        // 戌
            {TianGan.REN, TianGan.JIA}                      // 亥
    };

    /** 藏干层级名 */
    private static final String[] LEVELS = {"本气", "中气", "余气"};

    public static TianGan[] of(DiZhi zhi) {
        return TABLE[zhi.index()];
    }

    /** 本气 */
    public static TianGan benQi(DiZhi zhi) {
        return TABLE[zhi.index()][0];
    }

    public static List<String> describe(DiZhi zhi) {
        TianGan[] arr = TABLE[zhi.index()];
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i].getCn() + "(" + LEVELS[i] + ")");
        }
        return list;
    }
}
