package com.mingli.core.model;

import com.mingli.core.DiZhi;
import com.mingli.core.JiaZi;
import com.mingli.core.ShiShen;
import com.mingli.core.TianGan;

import java.util.ArrayList;
import java.util.List;

/** 四柱之一柱 */
public class Pillar {

    /** 柱位：年 / 月 / 日 / 时 */
    public String position;
    /** 六十甲子序号 */
    public int index;
    /** 天干 */
    public TianGan gan;
    /** 地支 */
    public DiZhi zhi;
    /** 天干十神（日柱为「日主」） */
    public ShiShen ganShiShen;
    /** 地支藏干 */
    public List<ZangGanItem> zangGan = new ArrayList<ZangGanItem>();
    /** 纳音 */
    public String naYin;
    /** 宫位类象（如「祖上/父母」「1—18 岁」） */
    public String gongWei;
    /** 是否落空亡 */
    public boolean kongWang;
    /** 神煞/星标（禄、刃、驿马、桃花…） */
    public List<String> marks = new ArrayList<String>();
    /** 生肖（仅年柱） */
    public String shengXiao;

    public String getGanZhi() { return JiaZi.name(index); }
    public String getGan() { return gan == null ? null : gan.getCn(); }
    public String getZhi() { return zhi == null ? null : zhi.getCn(); }
    public String getGanWuXing() { return gan == null ? null : gan.getWuXing().getCn(); }
    public String getZhiWuXing() { return zhi == null ? null : zhi.getWuXing().getCn(); }
    public String getGanShiShen() { return ganShiShen == null ? null : ganShiShen.getCn(); }
}
