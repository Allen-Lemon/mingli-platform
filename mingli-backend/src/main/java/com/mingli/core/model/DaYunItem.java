package com.mingli.core.model;

import com.mingli.core.JiaZi;

import java.util.ArrayList;
import java.util.List;

/** 大运一柱 */
public class DaYunItem {

    /** 大运序号，从 1 开始 */
    public int seq;
    /** 六十甲子序号 */
    public int index;
    /** 起运虚岁（实数，含小数年） */
    public double startAge;
    /** 起运公历年 */
    public int startYear;
    /** 结束公历年 */
    public int endYear;
    /** 天干十神 */
    public String ganShiShen;
    /** 藏干十神 */
    public List<String> zangGanShiShen = new ArrayList<String>();
    /** 大限柱位（年/月/日/时） */
    public String daXian;

    public String getGanZhi() { return JiaZi.name(index); }
    public String getGan() { return JiaZi.name(index).substring(0, 1); }
    public String getZhi() { return JiaZi.name(index).substring(1, 2); }
    public String getNaYin() { return JiaZi.naYin(index); }
}
