package com.mingli.core;

import com.mingli.core.LunarCalendar.JiePoint;
import com.mingli.core.model.BaziChart;
import com.mingli.core.model.DaYunItem;
import com.mingli.core.model.Pillar;
import com.mingli.core.model.ZangGanItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 八字排盘引擎。
 *
 * <p>流程：本地钟表时间 → 真太阳时校正 → 定节界（年柱/月柱）→ 定日柱（夜子时顺延）
 * → 定时柱（五鼠遁）→ 藏干十神 → 纳音 → 空亡 → 神煞 → 起运与大运。
 */
public final class BaziPaiPan {

    private BaziPaiPan() {}

    /** 宫位类象摘要（依据规则文档 1.5） */
    private static final String[] GONG_WEI = {
            "祖上/父母 · 1—18岁 · 远方 · 腿足 · 外部环境",      // 年
            "父母/兄弟 · 18—35岁 · 家乡 · 躯干 · 父母影响",       // 月
            "自己 · 内心世界 · 胸腹五脏 · 居所 · 35—55岁",        // 日支
            "儿女 · 55岁以后 · 门户远方 · 头面 · 交际影响力"      // 时
    };

    // ============================================================ 主入口

    public static BaziChart paiPan(BirthInput input) {
        BaziChart chart = new BaziChart();
        chart.name = input.name;
        chart.gender = input.gender;
        chart.calendarType = input.calendarType;
        chart.tzOffsetHours = input.tzOffsetHours;
        chart.longitude = input.longitude;
        chart.cityName = input.cityName;
        chart.useTrueSolarTime = input.useTrueSolarTime;

        int y = input.year;
        int m = input.month;
        int d = input.day;
        double hourMinute = input.hour + input.minute / 60.0;

        chart.birthTime = String.format("%04d-%02d-%02d %02d:%02d", y, m, d, input.hour, input.minute);

        // ---- 时刻转儒略日（UT）
        double jdn = LunarCalendar.jdn(y, m, d);
        double jdUt = jdn - 0.5 + (hourMinute - input.tzOffsetHours) / 24.0;

        // ---- 真太阳时
        double solarHour = input.useTrueSolarTime
                ? LunarCalendar.trueSolarHour(hourMinute, input.tzOffsetHours, input.longitude, jdUt)
                : hourMinute;
        chart.equationOfTimeMin = LunarCalendar.equationOfTime(jdUt);
        int sh = (int) Math.floor(((solarHour % 24.0) + 24.0) % 24.0);
        int smin = (int) Math.round((((solarHour % 24.0) + 24.0) % 24.0 - sh) * 60.0);
        if (smin >= 60) { smin -= 60; sh += 1; }
        chart.trueSolarTimeText = String.format("%02d:%02d（真太阳时，经度 %.2f°E，时差 %+.1f 分）",
                sh, smin, input.longitude, chart.equationOfTimeMin);

        // ---- 节界 → 年柱 / 月柱
        JiePoint jie = LunarCalendar.lastJieBefore(jdUt, y);
        JiePoint nextJie = LunarCalendar.nextJieAfter(jdUt, y);
        int mingLiYear = LunarCalendar.mingLiYearOf(jie);
        int yearIndex = LunarCalendar.yearGanZhiIndex(mingLiYear);
        int monthIndex = LunarCalendar.monthGanZhiIndex(yearIndex, jie.monthZhi);
        chart.mingLiYearName = JiaZi.name(yearIndex);
        chart.solarTermAtBirth = jie.term.getCn() + "后（" + jie.term.getCn() + " — " + nextJie.term.getCn() + "）";
        chart.prevJieText = LunarCalendar.jdToLocalString(jie.jd, input.tzOffsetHours) + " " + jie.term.getCn();
        chart.nextJieText = LunarCalendar.jdToLocalString(nextJie.jd, input.tzOffsetHours) + " " + nextJie.term.getCn();

        // ---- 节气临界提示（算法精度约 ±15 分钟，距节不足 30 分钟者给出两可方案）
        checkJieBoundary(chart, jdUt, jie, nextJie, yearIndex, monthIndex);

        // ---- 日柱（夜子时顺延一日）
        boolean lateZi = input.useTrueSolarTime && LunarCalendar.isLateZi(solarHour);
        int dayOffset = lateZi ? 1 : 0;
        int[] dmy = LunarCalendar.addDays(y, m, d, dayOffset);
        int dayIndex = LunarCalendar.dayGanZhiIndex(dmy[0], dmy[1], dmy[2]);

        // ---- 时柱（五鼠遁）
        DiZhi hourZhi = LunarCalendar.hourToZhi(solarHour);
        int hourIndex = LunarCalendar.hourGanZhiIndex(dayIndex, hourZhi);

        TianGan dayMaster = JiaZi.ganOf(dayIndex);
        chart.dayMaster = dayMaster;
        chart.dayMasterWuXing = dayMaster.getWuXing().getCn();
        chart.dayMasterYang = dayMaster.isYang();

        // ---- 四柱
        chart.year = buildPillar("年柱", yearIndex, dayMaster, GONG_WEI[0]);
        chart.year.shengXiao = JiaZi.zhiOf(yearIndex).getShengXiao();
        chart.month = buildPillar("月柱", monthIndex, dayMaster, GONG_WEI[1]);
        chart.day = buildPillar("日柱", dayIndex, dayMaster, GONG_WEI[2]);
        chart.day.ganShiShen = null; // 日干为日主，不标十神
        chart.hour = buildPillar("时柱", hourIndex, dayMaster, GONG_WEI[3]);

        // ---- 空亡（以日柱旬定）
        int xun = dayIndex / 10;
        int kw1 = 10 - 2 * xun;
        chart.kongWangXun = JiaZi.name(xun * 10) + "旬";
        chart.kongWang.add(DiZhi.of(kw1).getCn());
        chart.kongWang.add(DiZhi.of(kw1 + 1).getCn());
        for (Pillar p : chart.pillars()) {
            p.kongWang = (p.zhi.index() == ((kw1 % 12) + 12) % 12)
                    || (p.zhi.index() == (((kw1 + 1) % 12) + 12) % 12);
        }

        // ---- 神煞
        markShenSha(chart);

        // ---- 旺衰参考
        evaluateStrength(chart);

        // ---- 起运与大运
        buildDaYun(chart, input, jdUt, jie, nextJie, monthIndex);

        return chart;
    }

    /**
     * 节气临界检测：出生时刻距「节」不足 30 分钟时，给出另一套年柱 / 月柱供复核。
     */
    private static void checkJieBoundary(BaziChart c, double jdUt,
                                         JiePoint jie, JiePoint nextJie,
                                         int yearIndex, int monthIndex) {
        final double WINDOW = 30.0 / (24.0 * 60.0);   // 30 分钟（天）
        double afterPrev = jdUt - jie.jd;             // 距上一节已过
        double beforeNext = nextJie.jd - jdUt;        // 距下一节还有

        JiePoint alt = null;
        if (beforeNext < WINDOW) {
            alt = nextJie;                            // 可能已交下一节
        } else if (afterPrev < WINDOW) {
            alt = LunarCalendar.jieBefore(jie);       // 可能尚未交上一节
        }
        if (alt == null) {
            return;
        }
        int altYear = LunarCalendar.mingLiYearOf(alt);
        int altYearIndex = LunarCalendar.yearGanZhiIndex(altYear);
        int altMonthIndex = LunarCalendar.monthGanZhiIndex(altYearIndex, alt.monthZhi);
        c.altYearGz = JiaZi.name(altYearIndex);
        c.altMonthGz = JiaZi.name(altMonthIndex);
        c.boundaryWarning = String.format(
                "出生时刻距「%s」仅约 %d 分钟。节气时刻由天文算法推算，误差约 ±15 分钟，"
              + "故年柱 / 月柱存在两可：现取 %s %s，另一可能为 %s %s。建议以专业排盘或《天文年历》复核。",
                beforeNext < WINDOW ? nextJie.term.getCn() : jie.term.getCn(),
                Math.round((beforeNext < WINDOW ? beforeNext : afterPrev) * 24 * 60),
                JiaZi.name(yearIndex), JiaZi.name(monthIndex),
                c.altYearGz, c.altMonthGz);
    }

    // ============================================================ 构建一柱

    private static Pillar buildPillar(String position, int index, TianGan dayMaster, String gongWei) {
        Pillar p = new Pillar();
        p.position = position;
        p.index = index;
        p.gan = JiaZi.ganOf(index);
        p.zhi = JiaZhiOf(index);
        p.ganShiShen = ShiShen.of(dayMaster, p.gan);
        p.naYin = JiaZi.naYin(index);
        p.gongWei = gongWei;

        TianGan[] zg = ZangGan.of(p.zhi);
        String[] levels = {"本气", "中气", "余气"};
        for (int i = 0; i < zg.length; i++) {
            p.zangGan.add(new ZangGanItem(zg[i], levels[i], ShiShen.of(dayMaster, zg[i])));
        }
        return p;
    }

    private static DiZhi JiaZhiOf(int index) {
        return JiaZi.zhiOf(index);
    }

    // ============================================================ 神煞

    private static void markShenSha(BaziChart c) {
        TianGan dm = c.dayMaster;

        // 禄神（规则 1.1）
        DiZhi lu = luShen(dm);
        // 羊刃（规则 1.2，仅阳干）
        DiZhi ren = yangRen(dm);
        // 驿马（规则 1.3，以年支与日支三合局定）
        DiZhi ma1 = yiMa(c.year.zhi);
        DiZhi ma2 = yiMa(c.day.zhi);
        // 桃花
        DiZhi th1 = taoHua(c.year.zhi);
        DiZhi th2 = taoHua(c.day.zhi);
        // 天乙贵人
        DiZhi[] gy = tianYi(dm);
        // 文昌
        DiZhi wc = wenChang(dm);
        // 华盖
        DiZhi hg1 = huaGai(c.year.zhi);
        DiZhi hg2 = huaGai(c.day.zhi);
        // 将星
        DiZhi jx1 = jiangXing(c.year.zhi);
        DiZhi jx2 = jiangXing(c.day.zhi);

        for (Pillar p : c.pillars()) {
            if (p.zhi == lu) p.marks.add("禄神");
            if (ren != null && p.zhi == ren) p.marks.add("羊刃");
            if (p.zhi == ma1 || p.zhi == ma2) p.marks.add("驿马");
            if (p.zhi == th1 || p.zhi == th2) p.marks.add("桃花");
            if (p.zhi == gy[0] || p.zhi == gy[1]) p.marks.add("天乙贵人");
            if (p.zhi == wc) p.marks.add("文昌贵人");
            if (p.zhi == hg1 || p.zhi == hg2) p.marks.add("华盖");
            if (p.zhi == jx1 || p.zhi == jx2) p.marks.add("将星");
            if (p.kongWang) p.marks.add("空亡");
        }
    }

    /** 禄神（1.1）：甲寅 乙卯 丙巳 丁午 戊巳 己午 庚申 辛酉 壬亥 癸子 */
    public static DiZhi luShen(TianGan gan) {
        switch (gan) {
            case JIA:  return DiZhi.YIN;
            case YI:   return DiZhi.MAO;
            case BING: return DiZhi.SI;
            case DING: return DiZhi.WU;
            case WU:   return DiZhi.SI;   // 丙戊同禄于巳
            case JI:   return DiZhi.WU;   // 丁己同禄于午
            case GENG: return DiZhi.SHEN;
            case XIN:  return DiZhi.YOU;
            case REN:  return DiZhi.HAI;
            case GUI:  return DiZhi.ZI;
            default:   return null;
        }
    }

    /** 羊刃（1.2）：仅阳干。甲卯 丙午 戊未（原文另说巳，存疑） 庚酉 壬子 */
    public static DiZhi yangRen(TianGan gan) {
        switch (gan) {
            case JIA:  return DiZhi.MAO;
            case BING: return DiZhi.WU;
            case WU:   return DiZhi.WEI;   // 存疑：原文另说巳
            case GENG: return DiZhi.YOU;
            case REN:  return DiZhi.ZI;
            default:   return null;         // 阴干无羊刃
        }
    }

    /** 驿马（1.3）：申子辰→寅，寅午戌→申，巳酉丑→亥，亥卯未→巳 */
    public static DiZhi yiMa(DiZhi zhi) {
        switch (zhi) {
            case SHEN: case ZI: case CHEN: return DiZhi.YIN;
            case YIN:  case WU: case XU:   return DiZhi.SHEN;
            case SI:   case YOU: case CHOU:return DiZhi.HAI;
            case HAI:  case MAO: case WEI: return DiZhi.SI;
            default: return null;
        }
    }

    /** 桃花：申子辰→酉，寅午戌→卯，巳酉丑→午，亥卯未→子 */
    public static DiZhi taoHua(DiZhi zhi) {
        switch (zhi) {
            case SHEN: case ZI: case CHEN: return DiZhi.YOU;
            case YIN:  case WU: case XU:   return DiZhi.MAO;
            case SI:   case YOU: case CHOU:return DiZhi.WU;
            case HAI:  case MAO: case WEI: return DiZhi.ZI;
            default: return null;
        }
    }

    /** 天乙贵人：甲戊庚牛羊，乙己鼠猴，丙丁猪鸡，壬癸兔蛇，六辛逢虎马 */
    public static DiZhi[] tianYi(TianGan gan) {
        switch (gan) {
            case JIA: case WU: case GENG: return new DiZhi[]{DiZhi.CHOU, DiZhi.WEI};
            case YI:  case JI:            return new DiZhi[]{DiZhi.ZI, DiZhi.SHEN};
            case BING: case DING:         return new DiZhi[]{DiZhi.HAI, DiZhi.YOU};
            case REN: case GUI:           return new DiZhi[]{DiZhi.MAO, DiZhi.SI};
            case XIN:                     return new DiZhi[]{DiZhi.YIN, DiZhi.WU};
            default: return new DiZhi[0];
        }
    }

    /** 文昌贵人：甲巳 乙午 丙戊申 丁己酉 庚亥 辛子 壬寅 癸卯 */
    public static DiZhi wenChang(TianGan gan) {
        switch (gan) {
            case JIA:  return DiZhi.SI;
            case YI:   return DiZhi.WU;
            case BING: return DiZhi.SHEN;
            case WU:   return DiZhi.SHEN;
            case DING: return DiZhi.YOU;
            case JI:   return DiZhi.YOU;
            case GENG: return DiZhi.HAI;
            case XIN:  return DiZhi.ZI;
            case REN:  return DiZhi.YIN;
            case GUI:  return DiZhi.MAO;
            default: return null;
        }
    }

    /** 华盖：寅午戌→戌，亥卯未→未，申子辰→辰，巳酉丑→丑 */
    public static DiZhi huaGai(DiZhi zhi) {
        switch (zhi) {
            case YIN: case WU: case XU:    return DiZhi.XU;
            case HAI: case MAO: case WEI:  return DiZhi.WEI;
            case SHEN: case ZI: case CHEN: return DiZhi.CHEN;
            default: return DiZhi.CHOU;
        }
    }

    /** 将星：寅午戌→午，亥卯未→卯，申子辰→子，巳酉丑→酉 */
    public static DiZhi jiangXing(DiZhi zhi) {
        switch (zhi) {
            case YIN: case WU: case XU:    return DiZhi.WU;
            case HAI: case MAO: case WEI:  return DiZhi.MAO;
            case SHEN: case ZI: case CHEN: return DiZhi.ZI;
            default: return DiZhi.YOU;
        }
    }

    // ============================================================ 旺衰参考

    private static void evaluateStrength(BaziChart c) {
        WuXing me = c.dayMaster.getWuXing();
        WuXing ling = ZangGan.benQi(c.month.zhi).getWuXing();
        double score = 0;
        String rel = me.relationTo(ling);
        if ("同".equals(rel)) score += 4;
        else if ("生我".equals(rel)) score += 3;
        else if ("我生".equals(rel)) score -= 2;
        else if ("克我".equals(rel)) score -= 3;
        else score -= 1;   // 我克

        List<Pillar> all = c.pillars();
        for (Pillar p : all) {
            for (ZangGanItem it : p.zangGan) {
                ShiShen s = it.shiShen;
                if (s == null) continue;
                if (s.isBiJie()) score += 1.0;
                else if (s.isYin()) score += 0.8;
                else if (s.isShiShang()) score -= 0.6;
                else if (s.isCai()) score -= 0.8;
                else if (s.isGuanSha()) score -= 1.0;
            }
        }
        // 天干层面的比劫印（四支藏干已计，此处仅加权天干透出）
        for (Pillar p : all) {
            if (p == c.day) continue;
            ShiShen s = p.ganShiShen;
            if (s == null) continue;
            if (s.isBiJie()) score += 1.0;
            else if (s.isYin()) score += 0.8;
            else if (s.isShiShang()) score -= 0.6;
            else if (s.isCai()) score -= 0.8;
            else if (s.isGuanSha()) score -= 1.0;
        }

        c.dayMasterScore = (int) Math.round(score * 10);
        if (score >= 5) c.strengthText = "偏强";
        else if (score >= 1) c.strengthText = "略强";
        else if (score >= -3) c.strengthText = "中和";
        else if (score >= -7) c.strengthText = "略弱";
        else c.strengthText = "偏弱";
    }

    // ============================================================ 起运 / 大运

    private static void buildDaYun(BaziChart c, BirthInput input, double jdUt,
                                   JiePoint prevJie, JiePoint nextJie, int monthIndex) {
        boolean male = "M".equalsIgnoreCase(input.gender);
        boolean yangYear = JiaZi.ganOf(c.year.index).isYang();
        boolean forward = (yangYear && male) || (!yangYear && !male);   // 阳男阴女顺排
        c.daYunDirection = forward ? "顺排（阳男阴女）" : "逆排（阴男阳女）";

        double targetJd = forward ? nextJie.jd : prevJie.jd;
        double diffDays = Math.abs(targetJd - jdUt);

        // 三天折一岁，一日折四月，一时辰（2 小时）折十日
        int totalDays = (int) Math.floor(diffDays);
        double restDay = diffDays - totalDays;
        int restHours = (int) Math.round(restDay * 24.0);
        int years = totalDays / 3;
        int remDays = totalDays % 3;
        int months = remDays * 4 + (restHours / 2) * 10 / 30;
        int extraDays = (restHours / 2) * 10 % 30;

        c.qiYunAge = years + months / 12.0;
        c.qiYunText = String.format("%s，距%s约 %.2f 日 → 约 %d 岁 %d 个月起运",
                c.daYunDirection, forward ? "下一节(" + nextJie.term.getCn() + ")" : "上一节(" + prevJie.term.getCn() + ")",
                diffDays, years, months);

        int birthYear = input.year;
        List<DaYunItem> list = new ArrayList<DaYunItem>();
        for (int i = 1; i <= 10; i++) {
            DaYunItem item = new DaYunItem();
            item.seq = i;
            item.index = JiaZi.normalize(monthIndex + (forward ? i : -i));
            item.startAge = c.qiYunAge + (i - 1) * 10.0;
            if (item.startAge > 100) {
                break;
            }
            item.startYear = birthYear + (int) Math.floor(item.startAge);
            item.endYear = item.startYear + 9;
            item.ganShiShen = ShiShen.of(c.dayMaster, JiaZi.ganOf(item.index)).getCn();
            TianGan[] zg = ZangGan.of(JiaZi.zhiOf(item.index));
            String[] lv = {"本气", "中气", "余气"};
            for (int k = 0; k < zg.length; k++) {
                item.zangGanShiShen.add(zg[k].getCn() + "(" + lv[k] + "·" + ShiShen.of(c.dayMaster, zg[k]).getCn() + ")");
            }
            item.daXian = daXianOf(item.startAge);
            list.add(item);
        }
        c.daYun = list;
    }

    /** 大限柱位（规则 7.1）：年1—18，月18—35，日35—55，时55以后 */
    private static String daXianOf(double age) {
        if (age < 18) return "年柱大限";
        if (age < 35) return "月柱大限";
        if (age < 55) return "日柱大限";
        return "时柱大限";
    }

    // ============================================================ 输入结构

    public static class BirthInput {
        public String name;
        public String gender;           // M / F
        public int year;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public String calendarType = "SOLAR";
        public double tzOffsetHours = 8.0;
        public double longitude = 120.0;
        public String cityName;
        public boolean useTrueSolarTime = true;
    }
}
