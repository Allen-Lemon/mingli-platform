package com.mingli.core.model;

import com.mingli.core.DiZhi;
import com.mingli.core.JiaZi;
import com.mingli.core.TianGan;

import java.util.ArrayList;
import java.util.List;

/** 八字排盘结果 */
public class BaziChart {

    // ---------------- 输入 ----------------
    public String name;
    /** 性别：M / F */
    public String gender;
    public String birthTime;      // 本地钟表时间 yyyy-MM-dd HH:mm
    public String calendarType;   // SOLAR / LUNAR
    public double tzOffsetHours = 8.0;
    public double longitude = 120.0;
    public String cityName;
    public boolean useTrueSolarTime = true;

    // ---------------- 时间校正 ----------------
    public String trueSolarTimeText;   // 真太阳时
    public double equationOfTimeMin;   // 时差（分钟）
    public String solarTermAtBirth;    // 出生所处节气
    public String prevJieText;         // 上一个节
    public String nextJieText;         // 下一个节

    // ---------------- 四柱 ----------------
    public Pillar year;
    public Pillar month;
    public Pillar day;
    public Pillar hour;

    public TianGan dayMaster;
    public String dayMasterWuXing;
    public boolean dayMasterYang;

    /** 空亡地支 */
    public List<String> kongWang = new ArrayList<String>();
    public String kongWangXun;   // 所属旬

    /** 命理年（干支年名，如 甲辰） */
    public String mingLiYearName;

    /**
     * 节气临界提示：出生时刻距某个「节」不足 30 分钟时非空。
     * 因节气时刻算法存在约 ±15 分钟误差，此情形下年柱 / 月柱存在两可。
     */
    public String boundaryWarning;
    /** 临界情形下的另一套年柱 */
    public String altYearGz;
    /** 临界情形下的另一套月柱 */
    public String altMonthGz;

    // ---------------- 大运 ----------------
    /** 顺排 / 逆排 */
    public String daYunDirection;
    public double qiYunAge;         // 起运岁数
    public String qiYunText;        // 起运描述
    public List<DaYunItem> daYun = new ArrayList<DaYunItem>();

    // ---------------- 量化参考（仅供线索，不作定论） ----------------
    public int dayMasterScore;      // 日主强弱评分
    public String strengthText;     // 偏强 / 中和 / 偏弱

    public List<Pillar> pillars() {
        List<Pillar> list = new ArrayList<Pillar>();
        list.add(year);
        list.add(month);
        list.add(day);
        list.add(hour);
        return list;
    }

    /** 除日支外三支 */
    public List<DiZhi> otherZhiList() {
        List<DiZhi> list = new ArrayList<DiZhi>();
        list.add(year.zhi);
        list.add(month.zhi);
        list.add(hour.zhi);
        return list;
    }

    public String fullText() {
        StringBuilder sb = new StringBuilder();
        sb.append(JiaZi.name(year.index)).append(" ")
          .append(JiaZi.name(month.index)).append(" ")
          .append(JiaZi.name(day.index)).append(" ")
          .append(JiaZi.name(hour.index));
        return sb.toString();
    }
}
