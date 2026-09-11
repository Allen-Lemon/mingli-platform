package com.mingli.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 农历/干支历基础计算。
 *
 * <p>实现要点：
 * <ul>
 *   <li>儒略日（Julian Day）与公历互转；</li>
 *   <li>采用 Meeus《Astronomical Algorithms》太阳视黄经算法迭代求解二十四节气时刻，
 *       精度约 ±1 分钟，覆盖 1800—2200 年；</li>
 *   <li>真太阳时校正（经度平太阳时 + 时差）；</li>
 *   <li>五虎遁（年起月）、五鼠遁（日起时）、日干支快速公式。</li>
 * </ul>
 */
public final class LunarCalendar {

    private LunarCalendar() {}

    /** J2000.0 历元 */
    private static final double J2000 = 2451545.0;
    private static final double RAD = Math.PI / 180.0;

    // ------------------------------------------------------------------ 儒略日

    /** 公历（UT）转儒略日，day 可带小数 */
    public static double jd(int year, int month, double day) {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;
        return day + Math.floor((153 * m + 2) / 5.0) + 365 * y
                + Math.floor(y / 4.0) - Math.floor(y / 100.0) + Math.floor(y / 400.0) - 32045;
    }

    /** 公历日期的儒略日数（当日 12:00 UT） */
    public static double jdn(int year, int month, int day) {
        return jd(year, month, day);
    }

    /** 儒略日转公历（UT）年月日时分 */
    public static int[] fromJd(double jd) {
        double z = Math.floor(jd + 0.5);
        double f = jd + 0.5 - z;
        int a;
        if (z < 2299161) {
            a = (int) z;
        } else {
            double alpha = Math.floor((z - 1867216.25) / 36524.25);
            a = (int) (z + 1 + alpha - Math.floor(alpha / 4.0));
        }
        int b = a + 1524;
        int c = (int) Math.floor((b - 122.1) / 365.25);
        int d = (int) Math.floor(365.25 * c);
        int e = (int) Math.floor((b - d) / 30.6001);
        int day = b - d - (int) Math.floor(30.6001 * e);
        int month = e < 14 ? e - 1 : e - 13;
        int year = month > 2 ? c - 4716 : c - 4715;

        double dayFrac = f * 24.0;
        int hour = (int) Math.floor(dayFrac);
        int minute = (int) Math.floor((dayFrac - hour) * 60.0 + 0.5);
        if (minute >= 60) {
            minute -= 60;
            hour += 1;
        }
        return new int[]{year, month, day, hour, minute};
    }

    /** 儒略日转本地时区字符串 yyyy-MM-dd HH:mm */
    public static String jdToLocalString(double jd, double tzOffsetHours) {
        int[] t = fromJd(jd + tzOffsetHours / 24.0);
        return String.format("%04d-%02d-%02d %02d:%02d", t[0], t[1], t[2], t[3], t[4]);
    }

    // ------------------------------------------------------- 太阳位置（Meeus）

    /** 太阳几何平黄经 L0（度） */
    private static double meanLongitude(double t) {
        return 280.46646 + 36000.76983 * t + 0.0003032 * t * t;
    }

    /** 太阳平近点角 M（度） */
    private static double meanAnomaly(double t) {
        return 357.52911 + 35999.05029 * t - 0.0001537 * t * t;
    }

    /** 中心差 C（度） */
    private static double equationOfCenter(double t) {
        double m = meanAnomaly(t) * RAD;
        return (1.914602 - 0.004817 * t - 0.000014 * t * t) * Math.sin(m)
                + (0.019993 - 0.000101 * t) * Math.sin(2 * m)
                + 0.000289 * Math.sin(3 * m);
    }

    /** 黄赤交角（度） */
    private static double obliquity(double t) {
        return 23.439291 - 0.0130042 * t - 0.00000016 * t * t + 0.000000504 * t * t * t;
    }

    /**
     * 太阳视黄经（度）。
     *
     * <p>λ = Θ（真黄经）+ Δψ（黄经章动）− 光行差常数 − 周期光行差项，
     * 传入儒略日为世界时 UT，内部自动换算为力学时 TD。
     */
    public static double apparentLongitude(double jdUt) {
        double t = (jdUt + deltaT(jdUt) / 86400.0 - J2000) / 36525.0;
        double l0 = meanLongitude(t);
        double c = equationOfCenter(t);
        double theta = l0 + c;

        // 黄经章动 Δψ（Meeus 22 简略式，截断至 17" 主项）
        double omega = (125.04452 - 1934.136261 * t) * RAD;
        double lSun = l0 * RAD;
        double lMoon = (218.3165 + 481267.8813 * t) * RAD;
        double dPsi = (-17.20 * Math.sin(omega)
                - 1.32 * Math.sin(2 * lSun)
                - 0.23 * Math.sin(2 * lMoon)
                + 0.21 * Math.sin(2 * omega)) / 3600.0;

        // 光行差：常数项 + 周期项
        double omegaDeg = omega / RAD;
        double aberr = 0.00569 + 0.00478 * Math.sin(omegaDeg * RAD);

        return norm360(theta + dPsi - aberr);
    }

    /**
     * ΔT = TD − UT（秒）。采用 Espenak & Meeus 分段多项式，覆盖 1620—2150 年。
     */
    public static double deltaT(double jdUt) {
        int[] ymd = fromJd(jdUt);
        double year = ymd[0] + (ymd[1] - 0.5) / 12.0;
        double u, t;
        if (year >= 2150) {
            u = (year - 1820) / 100.0;
            return -20 + 32 * u * u;
        }
        if (year >= 2050) {
            return -20 + 32 * Math.pow((year - 1820) / 100.0, 2) - 0.5628 * (2150 - year);
        }
        if (year >= 2005) {
            t = year - 2000;
            return 62.92 + 0.32217 * t + 0.005589 * t * t;
        }
        if (year >= 1986) {
            t = year - 2000;
            return 63.86 + 0.3345 * t - 0.060374 * t * t + 0.0017275 * t * t * t
                    + 0.000651814 * Math.pow(t, 4) + 0.00002373599 * Math.pow(t, 5);
        }
        if (year >= 1961) {
            t = year - 1975;
            return 45.45 + 1.067 * t - t * t / 260.0 - t * t * t / 718.0;
        }
        if (year >= 1941) {
            t = year - 1950;
            return 29.07 + 0.407 * t - t * t / 233.0 + t * t * t / 2547.0;
        }
        if (year >= 1920) {
            t = year - 1920;
            return 21.20 + 0.84493 * t - 0.076100 * t * t + 0.0020936 * t * t * t;
        }
        if (year >= 1900) {
            t = year - 1900;
            return -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t * t * t - 0.000197 * Math.pow(t, 4);
        }
        if (year >= 1860) {
            t = year - 1860;
            return 7.62 + 0.5737 * t - 0.251754 * t * t + 0.01680668 * t * t * t
                    - 0.0004473624 * Math.pow(t, 4) + Math.pow(t, 5) / 233174.0;
        }
        if (year >= 1800) {
            t = year - 1800;
            return 13.72 - 0.332447 * t + 0.0068612 * t * t + 0.0041116 * t * t * t
                    - 0.00037436 * Math.pow(t, 4) + 0.0000121272 * Math.pow(t, 5)
                    - 0.0000001699 * Math.pow(t, 6) + 0.000000000875 * Math.pow(t, 7);
        }
        if (year >= 1700) {
            t = year - 1700;
            return 8.83 + 0.1603 * t - 0.0059285 * t * t + 0.00013336 * t * t * t
                    - Math.pow(t, 4) / 1174000.0;
        }
        if (year >= 1620) {
            t = year - 1600;
            return 120 - 0.9808 * t - 0.01532 * t * t + t * t * t / 7129.0;
        }
        // 更早年份粗略外推
        u = (year - 1820) / 100.0;
        return -20 + 32 * u * u;
    }

    /** 时差 Equation of Time（分钟）：真太阳时 - 平太阳时 */
    public static double equationOfTime(double jd) {
        double t = (jd - J2000) / 36525.0;
        double l0 = meanLongitude(t);
        double c = equationOfCenter(t);
        double theta = l0 + c;
        double eps = obliquity(t) * RAD;
        double alpha = Math.atan2(Math.cos(eps) * Math.sin(theta * RAD), Math.cos(theta * RAD)) / RAD;
        alpha = norm360(alpha);
        double e = l0 - 0.0057183 - alpha;
        e -= Math.floor(e / 360.0 + 0.5) * 360.0;   // 归一化到 ±180
        return e * 4.0;                              // 度 → 分钟
    }

    private static double norm360(double d) {
        double r = d % 360.0;
        return r < 0 ? r + 360.0 : r;
    }

    // ------------------------------------------------------------- 二十四节气

    /**
     * 求某年某节气的儒略日（UT）
     *
     * @param year 公历年
     * @param term 节气
     */
    public static double solarTermJd(int year, SolarTerm term) {
        // 初值：小寒约 1 月 5 日，之后每节约 15.22 日
        double guess = jd(year, 1, 5.0) + term.ordinal() * 15.22;
        double target = term.getDegree();
        double jd = guess;
        for (int i = 0; i < 8; i++) {
            double lambda = apparentLongitude(jd);
            double diff = target - lambda;
            diff -= Math.floor(diff / 360.0 + 0.5) * 360.0;   // ±180
            jd += diff * (365.25 / 360.0);
            if (Math.abs(diff) < 1e-8) {
                break;
            }
        }
        return jd;
    }

    /** 某年全部 24 节气（UT 儒略日），按年内顺序 */
    public static double[] solarTermsOfYear(int year) {
        double[] arr = new double[24];
        for (SolarTerm t : SolarTerm.values()) {
            arr[t.ordinal()] = solarTermJd(year, t);
        }
        return arr;
    }

    /** 节气时刻（本地时区字符串） */
    public static String solarTermLocal(int year, SolarTerm term, double tzOffsetHours) {
        return jdToLocalString(solarTermJd(year, term), tzOffsetHours);
    }

    // ------------------------------------------------------------- 年月日时柱

    /**
     * 日干支序号：已知 2024-02-10 为甲辰日（序号 40），
     * 反推常量 C = 49，故 序号 = (JDN + 49) mod 60。
     */
    public static int dayGanZhiIndex(int year, int month, int day) {
        double j = jdn(year, month, day);
        return JiaZi.normalize((int) (Math.round(j) + 49L));
    }

    /** 五虎遁：由年干求寅月干支序号 */
    public static int yinMonthIndex(TianGan yearGan) {
        return JiaZi.normalize(yearGan.index() * 12 + 2);
    }

    /** 由年柱序号与月支求月柱序号 */
    public static int monthGanZhiIndex(int yearGanZhiIndex, DiZhi monthZhi) {
        TianGan yearGan = JiaZi.ganOf(yearGanZhiIndex);
        int yin = yinMonthIndex(yearGan);
        int offset = (monthZhi.index() - DiZhi.YIN.index() + 12) % 12;
        return JiaZi.normalize(yin + offset);
    }

    /** 五鼠遁：由日干求子时干支序号 */
    public static int ziHourIndex(TianGan dayGan) {
        return (dayGan.index() % 5) * 12;
    }

    /** 由日柱序号与时辰支求时柱序号 */
    public static int hourGanZhiIndex(int dayGanZhiIndex, DiZhi hourZhi) {
        TianGan dayGan = JiaZi.ganOf(dayGanZhiIndex);
        return JiaZi.normalize(ziHourIndex(dayGan) + hourZhi.index());
    }

    // ----------------------------------------------------------- 节界与月支

    /** 一个「节」的落点 */
    public static class JiePoint {
        public final double jd;          // UT 儒略日
        public final SolarTerm term;
        public final int year;
        public final DiZhi monthZhi;

        JiePoint(double jd, SolarTerm term, int year, DiZhi monthZhi) {
            this.jd = jd;
            this.term = term;
            this.year = year;
            this.monthZhi = monthZhi;
        }
    }

    /** 取某年前后各一年的所有「节」，按时间升序 */
    public static List<JiePoint> jiePointsAround(int year) {
        List<JiePoint> list = new ArrayList<JiePoint>();
        for (int y = year - 1; y <= year + 1; y++) {
            for (SolarTerm t : SolarTerm.values()) {
                if (!t.isJie()) {
                    continue;
                }
                list.add(new JiePoint(solarTermJd(y, t), t, y, t.getMonthZhi()));
            }
        }
        return list;
    }

    /** 找出某时刻之前最近的一个「节」 */
    public static JiePoint lastJieBefore(double jdUt, int approxYear) {
        JiePoint best = null;
        for (JiePoint p : jiePointsAround(approxYear)) {
            if (p.jd <= jdUt && (best == null || p.jd > best.jd)) {
                best = p;
            }
        }
        return best;
    }

    /** 找出某「节」之前最近的一个「节」 */
    public static JiePoint jieBefore(JiePoint jie) {
        JiePoint best = null;
        for (JiePoint p : jiePointsAround(jie.year)) {
            if (p.jd < jie.jd - 1e-9 && (best == null || p.jd > best.jd)) {
                best = p;
            }
        }
        return best;
    }

    /** 找出某时刻之后最近的一个「节」 */
    public static JiePoint nextJieAfter(double jdUt, int approxYear) {
        JiePoint best = null;
        for (JiePoint p : jiePointsAround(approxYear)) {
            if (p.jd > jdUt && (best == null || p.jd < best.jd)) {
                best = p;
            }
        }
        return best;
    }

    /**
     * 由「节」推算命理年（以立春为界）：
     * 小寒属上一命理年之丑月，其余节属其所在公历年。
     */
    public static int mingLiYearOf(JiePoint jie) {
        return jie.term == SolarTerm.XIAO_HAN ? jie.year - 1 : jie.year;
    }

    /** 年柱序号：命理年 - 4（甲子年公元 4 年） */
    public static int yearGanZhiIndex(int mingLiYear) {
        return JiaZi.normalize(mingLiYear - 4);
    }

    // ------------------------------------------------------------- 真太阳时

    /**
     * 计算真太阳时小时数（0-24 浮点）
     *
     * @param hourMinute 本地钟表时间小时（含小数）
     * @param tzOffsetHours 出生地时区偏移（东八区为 8）
     * @param longitude 出生地经度（东经为正）
     * @return 真太阳时小时
     */
    public static double trueSolarHour(double hourMinute, double tzOffsetHours,
                                       double longitude, double jdUt) {
        // 平太阳时：以时区中央经线（东八区 120°E）为基准修正经度差，4 分钟/度
        double standardMeridian = tzOffsetHours * 15.0;
        double meanSolar = hourMinute + (longitude - standardMeridian) * 4.0 / 60.0;
        // 时差校正
        return meanSolar + equationOfTime(jdUt) / 60.0;
    }

    /** 真太阳时小时 → 时辰地支 */
    public static DiZhi hourToZhi(double solarHour) {
        double h = solarHour % 24.0;
        if (h < 0) {
            h += 24.0;
        }
        int idx = (int) Math.floor((h + 1) / 2.0) % 12;
        return DiZhi.of(idx);
    }

    /** 是否夜子时（23:00 之后），日柱需顺延一天 */
    public static boolean isLateZi(double solarHour) {
        double h = solarHour % 24.0;
        if (h < 0) {
            h += 24.0;
        }
        return h >= 23.0;
    }

    /** 公历日期加天数 */
    public static int[] addDays(int year, int month, int day, int delta) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(year, month - 1, day);
        c.add(Calendar.DAY_OF_MONTH, delta);
        return new int[]{c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)};
    }
}
