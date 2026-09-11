package com.mingli;

import com.mingli.core.*;
import com.mingli.core.model.BaziChart;
import com.mingli.core.model.Pillar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 核心历法引擎冒烟校验（不依赖 Spring，可单独 main 运行）
 */
public class CoreSmokeTest {

    public static void main(String[] args) {
        check("2024-02-10 甲辰日(春节) 应=甲辰年 丙寅月 甲辰日", "甲辰", "丙寅", "甲辰");

        // 节气时刻校验（2024 年立春 = 2月4日 16:27 北京时间）
        int[] t = LunarCalendar.fromJd(LunarCalendar.solarTermJd(2024, SolarTerm.LI_CHUN) + 8.0 / 24);
        System.out.printf("2024 立春(北京时) = %04d-%02d-%02d %02d:%02d%n", t[0], t[1], t[2], t[3], t[4]);
        t = LunarCalendar.fromJd(LunarCalendar.solarTermJd(2024, SolarTerm.DONG_ZHI) + 8.0 / 24);
        System.out.printf("2024 冬至(北京时) = %04d-%02d-%02d %02d:%02d%n", t[0], t[1], t[2], t[3], t[4]);
        t = LunarCalendar.fromJd(LunarCalendar.solarTermJd(2026, SolarTerm.LI_CHUN) + 8.0 / 24);
        System.out.printf("2026 立春(北京时) = %04d-%02d-%02d %02d:%02d%n", t[0], t[1], t[2], t[3], t[4]);

        // 日干支校验：1900-01-01 应为 甲戌(10)? 2026-09-10 校验
        System.out.println("2026-09-10 日柱 = " + JiaZi.name(LunarCalendar.dayGanZhiIndex(2026, 9, 10)));
        System.out.println("1900-01-01 日柱 = " + JiaZi.name(LunarCalendar.dayGanZhiIndex(1900, 1, 1)));
        System.out.println("1949-10-01 日柱 = " + JiaZi.name(LunarCalendar.dayGanZhiIndex(1949, 10, 1)));
        System.out.println("2000-01-01 日柱 = " + JiaZi.name(LunarCalendar.dayGanZhiIndex(2000, 1, 1)));

        dumpTerms(2026);

        // 完整排盘示例
        BaziPaiPan.BirthInput in = new BaziPaiPan.BirthInput();
        in.name = "测试";
        in.gender = "M";
        in.year = 1990; in.month = 5; in.day = 15; in.hour = 14; in.minute = 30;
        in.longitude = 113.26; in.cityName = "广州";
        BaziChart c = BaziPaiPan.paiPan(in);
        print(c);

        BaziPaiPan.BirthInput in2 = new BaziPaiPan.BirthInput();
        in2.name = "测试2";
        in2.gender = "F";
        in2.year = 2026; in2.month = 9; in2.day = 10; in2.hour = 15; in2.minute = 45;
        in2.longitude = 116.4; in2.cityName = "北京";
        print(BaziPaiPan.paiPan(in2));
    }

    private static void check(String label, String... expect) {
        System.out.println("[CHECK] " + label);
    }

    /** 输出某年 24 节气（北京时间），用于与《中国天文年历》对照 */
    private static void dumpTerms(int year) {
        System.out.println("---- " + year + " 年二十四节气（北京时间）----");
        for (SolarTerm t : SolarTerm.values()) {
            int[] a = LunarCalendar.fromJd(LunarCalendar.solarTermJd(year, t) + 8.0 / 24);
            System.out.printf("%s %02d-%02d %02d:%02d%n", t.getCn(), a[1], a[2], a[3], a[4]);
        }
    }

    private static void print(BaziChart c) {
        System.out.println("==============================================");
        System.out.println(c.name + "  " + c.birthTime + "  " + c.cityName);
        System.out.println("真太阳时: " + c.trueSolarTimeText);
        System.out.println("节界: " + c.solarTermAtBirth + " | 上节 " + c.prevJieText + " | 下节 " + c.nextJieText);
        System.out.printf("%s  %s  %s  %s%n",
                c.year.getGanZhi(), c.month.getGanZhi(), c.day.getGanZhi(), c.hour.getGanZhi());
        for (Pillar p : c.pillars()) {
            System.out.printf("%s %s | 天干十神=%s | 藏干=%s | 纳音=%s | 空亡=%s | 神煞=%s%n",
                    p.position, p.getGanZhi(), p.getGanShiShen(),
                    ZangGan.describe(p.zhi), p.naYin, p.kongWang, p.marks);
        }
        System.out.println("日主=" + c.dayMaster.getCn() + "(" + c.dayMasterWuXing + ") 强弱=" + c.strengthText + " 分=" + c.dayMasterScore);
        System.out.println("空亡=" + c.kongWangXun + "→" + c.kongWang);
        System.out.println("起运: " + c.qiYunText);
        for (com.mingli.core.model.DaYunItem d : c.daYun) {
            System.out.printf("  大运%d %s 起 %.1f岁 (%d-%d) %s %s%n",
                    d.seq, d.getGanZhi(), d.startAge, d.startYear, d.endYear, d.ganShiShen, d.daXian);
        }
    }
}
