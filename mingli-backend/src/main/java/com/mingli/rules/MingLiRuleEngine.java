package com.mingli.rules;

import com.mingli.core.DiZhi;
import com.mingli.core.JiaZi;
import com.mingli.core.ShiShen;
import com.mingli.core.TianGan;
import com.mingli.core.WuXing;
import com.mingli.core.ZangGan;
import com.mingli.core.model.AnalysisResult;
import com.mingli.core.model.BaziChart;
import com.mingli.core.model.DaYunItem;
import com.mingli.core.model.Pillar;
import com.mingli.core.model.ZangGanItem;
import com.mingli.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 盲派命理规则推理引擎。
 *
 * <p>依据《盲派命理规则（开发导向版）》实现：
 * 一、基础查找表（1.1—1.9）；二、财富与职业（2.1—2.3）；三、官职学历行业（3.1—3.3）；
 * 四、取象七原则；五、正局反局；六、婚姻子女桃花（6.1—6.2）；七、大运流年应期（7.1—7.2）。
 *
 * <p>输出为「规则线索」，凡原文标注存疑者均以 doubtful 标记，不给出单一确定结论。
 */
@Component
public class MingLiRuleEngine {

    @Autowired
    private DictService dict;

    // ================================================================== 入口

    public AnalysisResult analyze(BaziChart c) {
        AnalysisResult r = new AnalysisResult();
        overView(c, r);
        shenSha(c, r);
        caiFu(c, r);
        guanLuXueLi(c, r);
        hangYe(c, r);
        hunYin(c, r);
        ziNv(c, r);
        quXiang(c, r);
        zhengFanJu(c, r);
        suiYun(c, r);

        if (r.highlights.isEmpty()) {
            r.highlights.add("原局做功线索不突出，宜以大运流年为主参看。");
        }
        r.headline = r.highlights.get(0);
        return r;
    }

    // ============================================================ 一、命局总览

    private void overView(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("overview", "命局总览");
        TianGan dm = c.dayMaster;
        s.add("日主", dm.getCn() + "（" + c.dayMasterWuXing + "，" + (c.dayMasterYang ? "阳干" : "阴干") + "）");
        s.add("四柱", c.fullText());
        s.add("月令", c.month.getZhi() + "月（" + c.month.getGanZhi() + "），本气 " + ZangGan.benQi(c.month.zhi).getCn());
        s.add("强弱参考", c.strengthText + "（量化分 " + c.dayMasterScore + "，仅供线索）");
        s.add("空亡", c.kongWangXun + " → " + String.join("、", c.kongWang));
        s.add("真太阳时", c.trueSolarTimeText);
        if (c.boundaryWarning != null) {
            s.add("节气临界", c.boundaryWarning, "warn");
        }
        s.summary = "日主" + dm.getCn() + "生于" + c.month.getZhi() + "月，"
                + (c.strengthText.equals("偏强") || c.strengthText.equals("略强")
                    ? "日主得势，宜泄宜克，喜食伤财官。" : "日主偏弱，宜生宜扶，喜印比。")
                + "（此为量化参考，实际须依原局做功定）";
        r.highlights.add(s.summary);
    }

    // ============================================================ 二、神煞基础

    private void shenSha(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("shensha", "神煞与基础象（1.1—1.4）");
        TianGan dm = c.dayMaster;

        // 禄（1.1）
        DiZhi lu = Dicts.luShen(dm);
        List<String> luAt = zhiPositions(c, lu);
        if (!luAt.isEmpty()) {
            s.add("禄神", "日主" + dm.getCn() + "禄在" + lu.getCn() + "，现于" + String.join("、", luAt)
                    + "。见禄代表原身，主身体、权力、衣禄、财富", "good");
            r.hit("1.1", "禄神", "日主" + dm.getCn() + "见禄于" + lu.getCn() + "，禄当财用时喜印生禄，忌伤食泄禄、劫财分禄。");
        } else {
            s.add("禄神", "四支不见" + lu.getCn() + "，原局无禄，须看印比与伤食做功", "info");
        }

        // 羊刃（1.2）
        if (dm.isYang()) {
            DiZhi ren = Dicts.yangRen(dm);
            List<String> renAt = zhiPositions(c, ren);
            if (!renAt.isEmpty()) {
                s.add("羊刃", "日主" + dm.getCn() + "刃在" + ren.getCn() + "，现于" + String.join("、", renAt)
                        + "。刃主刚烈、实权，亦须有制化", "warn");
                r.hit("1.2", "羊刃", dm.getCn() + "刃在" + ren.getCn() + "，羊刃配七杀可取军警、执法、实权之象。");
            }
        } else {
            s.add("羊刃", dm.getCn() + "为阴干，按规则无羊刃（1.2）", "info");
            r.hit("1.2", "羊刃", "仅阳干有羊刃，阴干无。", true);
        }

        // 驿马（1.3）
        DiZhi ma1 = Dicts.yiMa(c.year.zhi);
        DiZhi ma2 = Dicts.yiMa(c.day.zhi);
        List<String> maAt = new ArrayList<String>();
        for (Pillar p : c.pillars()) {
            if (p.zhi == ma1 || p.zhi == ma2) {
                maAt.add(p.position);
            }
        }
        if (!maAt.isEmpty()) {
            s.add("驿马", "年支" + c.year.getZhi() + "/日支" + c.day.getZhi() + "之驿马为"
                    + ma1.getCn() + (ma2 != ma1 ? "、" + ma2.getCn() : "") + "，现于"
                    + String.join("、", maAt) + "。主走动、迁移、车船；逢合则停留不动", "info");
            r.hit("1.3", "驿马", "见驿马主走动迁移；逢合主停留不动。");
        }

        // 空亡（1.4）
        Map<String, String> kwMean = new LinkedHashMap<String, String>();
        kwMean.put("年柱", "祖业空");
        kwMean.put("月柱", "兄弟空");
        kwMean.put("日柱", "夫妻缘薄");
        kwMean.put("时柱", "子女迟损");
        List<String> kwHit = new ArrayList<String>();
        for (Pillar p : c.pillars()) {
            if (p.kongWang) {
                String m = kwMean.get(p.position);
                kwHit.add(p.position + "（" + p.getZhi() + "，" + (m == null ? "空" : m) + "）");
            }
        }
        s.add("空亡", c.kongWangXun + "空" + String.join("、", c.kongWang)
                + (kwHit.isEmpty() ? "，四柱无支落空" : "，命中：" + String.join("，", kwHit))
                + "。凶空减半、吉空福不全；用神旺相而空，可取玄学、艺术等务虚之象", kwHit.isEmpty() ? "info" : "warn");
        r.hit("1.4", "空亡", "年空→祖业，月空→兄弟，日空→夫妻缘薄，时空→子女迟损；流年不讲空亡。");

        // 桃花
        DiZhi th1 = Dicts.taoHua(c.year.zhi);
        DiZhi th2 = Dicts.taoHua(c.day.zhi);
        List<String> thAt = new ArrayList<String>();
        for (Pillar p : c.pillars()) {
            if (p.zhi == th1 || p.zhi == th2) {
                thAt.add(p.position);
            }
        }
        if (!thAt.isEmpty()) {
            s.add("桃花", "桃花在" + th1.getCn() + (th2 != th1 ? "、" + th2.getCn() : "") + "，现于"
                    + String.join("、", thAt) + "，主人缘、异性缘与才艺表现", "info");
        }

        s.summary = "禄刃马空为盲派最基础的取象入口，须与原局做功、主宾位置结合，不可单独断吉凶。";
    }

    // ============================================================ 三、财富职业

    private void caiFu(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("caifu", "财富与取财方式（2.1—2.2）");
        TianGan dm = c.dayMaster;

        int caiCount = countShiShen(c, ShiShen.ZHENG_CAI) + countShiShen(c, ShiShen.PIAN_CAI);
        int shiShang = countShiShen(c, ShiShen.SHI_SHEN) + countShiShen(c, ShiShen.SHANG_GUAN);
        int biJie = countShiShen(c, ShiShen.BI_JIAN) + countShiShen(c, ShiShen.JIE_CAI);
        int yin = countShiShen(c, ShiShen.ZHENG_YIN) + countShiShen(c, ShiShen.PIAN_YIN);
        int guanSha = countShiShen(c, ShiShen.ZHENG_GUAN) + countShiShen(c, ShiShen.QI_SHA);

        s.add("十神分布", "财 " + caiCount + " · 食伤 " + shiShang + " · 比劫 " + biJie
                + " · 印 " + yin + " · 官杀 " + guanSha + "（按四柱天干与地支藏干计）");

        // 2.1 财富替代
        String substitute;
        if (caiCount == 0 && shiShang == 0) {
            substitute = "原局无财、亦无伤食泄 → 以禄当财。喜印生禄，忌伤食泄禄、劫财分禄；主辛苦求财";
            r.hit("2.1", "以禄当财", "无财、无伤食泄，以禄当财；喜印生禄，忌伤食泄禄、劫财分禄。");
        } else if (caiCount == 0) {
            substitute = "原局无财、但有伤食 → 以伤食当财。伤官偏经营谋略，食神偏思想脑力；地支内食神做功可主企业经营";
            r.hit("2.1", "以伤食当财", "无财、有伤食，以伤食当财；地支内食神做功→企业经营。");
        } else {
            substitute = "原局有财 → 以财星看财，须察财星是否有根、是否被劫、是否与日主/主位发生关系";
        }
        s.add("财富替代（2.1）", substitute, caiCount == 0 ? "warn" : "info");

        // 2.2 取财方式
        List<String> modes = new ArrayList<String>();
        if (shiShang >= 2 || (shiShang >= 1 && caiCount >= 1)) {
            modes.add("经营取财（商业、企业、门店、生产、开发）");
            r.hit("2.2", "经营取财", "伤食做功/食神生财/财星做功，主经营取财。");
        }
        if (biJie >= 3) {
            modes.add("风险取财或体力取财（比劫多而效率高偏资本运作，效率低偏体力）");
            r.hit("2.2", "比劫做功", "比劫/劫财做功，效率高者偏风险取财，效率低者偏体力取财。");
        }
        if (hasShiShangAtMainPosition(c)) {
            modes.add("智力取财（咨询、法律、设计、艺术、写作、技术）");
            r.hit("2.2", "智力取财", "伤食居主位或与主位做功，主智力取财。");
        }
        if (modes.isEmpty()) {
            modes.add("工薪取财（印、官杀、食伤、财各小功而效率不高，主稳定工薪）");
            r.hit("2.2", "工薪取财", "各神小功而效率不高，主稳定工薪。");
        }
        s.add("取财方式（2.2）", String.join("；", modes));
        s.summary = substitute + "。取财方式：" + String.join("；", modes);
        r.highlights.add("取财方式：" + modes.get(0));
    }

    // ======================================================== 四、官职学历行业

    private void guanLuXueLi(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("guanlu", "官职、学历与行业（3.1—3.3）");
        TianGan dm = c.dayMaster;

        int guan = countShiShen(c, ShiShen.ZHENG_GUAN);
        int sha = countShiShen(c, ShiShen.QI_SHA);
        int yin = countShiShen(c, ShiShen.ZHENG_YIN) + countShiShen(c, ShiShen.PIAN_YIN);
        int shi = countShiShen(c, ShiShen.SHI_SHEN);
        int cai = countShiShen(c, ShiShen.ZHENG_CAI) + countShiShen(c, ShiShen.PIAN_CAI);
        int biJie = countShiShen(c, ShiShen.BI_JIAN) + countShiShen(c, ShiShen.JIE_CAI);

        boolean guanShaRooted = isShiShenRooted(c, ShiShen.ZHENG_GUAN) || isShiShenRooted(c, ShiShen.QI_SHA);
        boolean yinRooted = isShiShenRooted(c, ShiShen.ZHENG_YIN) || isShiShenRooted(c, ShiShen.PIAN_YIN);

        StringBuilder g = new StringBuilder();
        if (guan + sha == 0) {
            g.append("原局不见官杀，官职不以官杀论，须看禄、印与财官相统的结构；");
        } else if (guanShaRooted && yinRooted) {
            g.append("官杀有根、印亦有根，官杀主职务、印主权柄，二者得地，具备官职结构的基础（3.1）；");
            r.hit("3.1", "官命结构", "官杀与印皆有根，官杀配印，常有官职；做功最好有主位字参与。");
        } else {
            g.append("官杀或印有一方无根，官职权柄不足，须待岁运落实；");
        }
        if ((guan + sha) > 0 && !guanShaRooted) {
            g.append("官杀虚透（天干见而地支无根）偏名气，逢运落实才可能应职；");
            r.hit("3.1", "官杀虚透", "官杀虚透偏名气，逢运落实才可能应职。");
        }
        g.append("具体单位性质须看官星位置与带帽（年上官星被制多为国企之官）。");
        s.add("官禄（3.1）", g.toString(), guanShaRooted && yinRooted ? "good" : "info");

        // 3.2 学历
        StringBuilder e = new StringBuilder();
        List<String> poXue = new ArrayList<String>();
        if (yin > 0) e.append("印星主学习与名节，见印利学；");
        if (shi > 0) e.append("食神主思考，利学业；");
        if (cai >= 2) poXue.add("财多则心乱欲望重，为破学因素");
        if (countShiShen(c, ShiShen.SHANG_GUAN) > 0) poXue.add("伤官聪明但不喜规训，配印或官杀做功可成学");
        if (biJie >= 2) poXue.add("比劫好动，通常不利静学");
        s.add("学历（3.2）", e.toString() + (poXue.isEmpty() ? "未见明显破学因素" : "破学因素：" + String.join("；", poXue)),
                poXue.isEmpty() ? "good" : "warn");
        r.hit("3.2", "学历判定", "学历主要看官杀/印/食神；财、伤官、比劫常为破学因素。");

        // 文理
        String wenLi = wenLi(c);
        s.add("文理倾向（3.2）", wenLi);

        s.summary = g.toString();
    }

    private String wenLi(BaziChart c) {
        WuXing mx = dominantWuXing(c);
        if (mx == WuXing.METAL || mx == WuXing.WATER) {
            return "金水偏理（庚申戌亥→数学，申子辰→化学，申酉加丑→法律）";
        }
        if (mx == WuXing.WOOD || mx == WuXing.FIRE) {
            return "木火偏文（寅卯配火→中文，食神→中文/外语）";
        }
        if (mx == WuXing.EARTH) {
            return "土主中和，宜看印与食伤定文理";
        }
        return "须依原局组合定";
    }

    // ============================================================ 五、行业取象

    private void hangYe(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("hangye", "行业取象（3.3 / 取象七原则）");
        List<String> list = new ArrayList<String>();

        // 化象
        if (has(c, WuXing.WOOD) && has(c, WuXing.FIRE)) {
            boolean yinMuShengHuo = c.month.gan.getWuXing() == WuXing.FIRE
                    && (c.month.zhi.getWuXing() == WuXing.WOOD || hasZangGan(c, TianGan.YI));
            list.add(yinMuShengHuo ? "阴木生火 → 纺织、服装" : "阳木生火 → 家具、装潢");
        }
        if (hasZhi(c, DiZhi.ZI) && hasZhi(c, DiZhi.CHEN)) {
            list.add("子辰 → 化工、制药、提纯");
        }
        if (has(c, WuXing.EARTH) && has(c, WuXing.WOOD)) {
            list.add("土木 → 建筑、房地产");
        }
        if (hasZhi(c, DiZhi.SHEN) && hasZhi(c, DiZhi.YOU)) {
            list.add("申酉（加丑） → 法律、司法、检法");
        }
        if (hasZhi(c, DiZhi.HAI) || hasZhi(c, DiZhi.CHOU) || hasZhi(c, DiZhi.CHEN)) {
            list.add("亥/丑/辰在特定结构 → 科技、医药、玄学、金融（须全局佐证）");
        }

        // 十神类象 → 行业
        List<String> careers = new ArrayList<String>();
        for (Pillar p : c.pillars()) {
            if (p.ganShiShen != null) {
                Map<String, String> m = dict.shiShenOf(p.ganShiShen.getCn());
                String ca = m.get("careers");
                if (ca != null && !ca.isEmpty()) {
                    careers.add(p.ganShiShen.getCn() + "：" + ca);
                }
            }
        }
        s.add("十神职业类象（1.6）", String.join("；", dedup(careers)));

        // 宫位 → 单位性质
        s.add("宫位指示（1.5/3.3）", "年柱 → 远方、海外、大型机构；月令 → 单位、国家、大机构；"
                + "日支 → 居所与工作所；时柱 → 门户、门店、车、外部交际。主位关系决定本人经营 / 替人管理 / 工薪。");

        if (list.isEmpty()) {
            list.add("五行组合较均衡，行业须依共象、合象、墓象综合定，不可只用单一五行");
        }
        s.add("化象取象（四）", String.join("；", list));
        s.summary = String.join("；", list);
        r.highlights.add("行业线索：" + list.get(0));
    }

    // ================================================================ 六、婚姻

    private void hunYin(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("hunyin", "婚姻与桃花（6.1）");
        boolean male = "M".equalsIgnoreCase(c.gender);
        DiZhi gong = c.day.zhi;                       // 夫妻宫
        ShiShen star1 = male ? ShiShen.ZHENG_CAI : ShiShen.ZHENG_GUAN;   // 正星
        ShiShen star2 = male ? ShiShen.PIAN_CAI : ShiShen.QI_SHA;        // 偏星

        String gongWx = dict.zhiWuXiangOf(gong.getCn());
        s.add("夫妻宫", "日支" + gong.getCn() + "（" + c.day.getGanZhi() + "），藏干 "
                + ZangGan.describe(gong) + (gongWx == null ? "" : "；宫支物象：" + gongWx));

        // 宫被刑冲破穿
        List<String> broken = new ArrayList<String>();
        for (DiZhi other : c.otherZhiList()) {
            if (other == gong) continue;
            if (gong.chong() == other) broken.add(other.getCn() + "冲宫");
            else if (gong.chuan() != null && gong.chuan() == other) broken.add(other.getCn() + "穿宫");
            else if (contains(gong.xing(), other)) broken.add(other.getCn() + "刑宫");
            else if (gong.liuHe() == other) broken.add(other.getCn() + "合宫");
        }

        int starCount = countShiShen(c, star1) + countShiShen(c, star2);
        String starPos = starPosition(c, star1, star2);

        s.add("配偶星", (male ? "男命以财为妻" : "女命以官杀为夫") + "，本命" + star1.getCn() + "/" + star2.getCn()
                + "共 " + starCount + " 现，" + (starCount == 0 ? "须转看"
                    + (male ? "伤食" : "印") + "（无星替代法）" : "位置：" + starPos));

        if (broken.isEmpty()) {
            s.add("宫位状态", "夫妻宫安静，未被刑冲破穿、未被他字合走，属好婚姻的基础条件", "good");
            r.hit("6.1", "好婚姻", "夫妻宫安静，不被刑冲破穿、不被配偶星以外之字合走、少杂透多现。");
        } else {
            s.add("宫位状态", "夫妻宫受" + String.join("、", broken)
                    + "。破坏轻重决定争吵 / 分居 / 离异的层次，须看制得住与否", "warn");
            r.hit("6.1", "差婚姻线索", "宫为用却被刑冲破穿/他合/杂透多现；破坏轻重→争吵/分居/离异。");
        }

        // 晚婚/早婚
        boolean early = "年月".contains(firstChar(starPos));
        s.add("婚期早晚（6.1）", starCount == 0 ? "配偶星不显，婚期须待岁运引出"
                : (early ? "配偶星在年月，偏早婚" : "配偶星在日时，偏晚婚"));

        // 正星得正位
        boolean zhengWei = (male && ZangGan.benQi(gong) != null
                && ShiShen.of(c.dayMaster, ZangGan.benQi(gong)) == ShiShen.ZHENG_CAI)
                || (!male && ZangGan.benQi(gong) != null
                && ShiShen.of(c.dayMaster, ZangGan.benQi(gong)) == ShiShen.ZHENG_GUAN);
        if (zhengWei) {
            s.add("正星得正位", (male ? "正财" : "正官") + "坐夫妻宫，正星得正位通常不喜被制被合", "info");
            r.hit("6.1", "正星得正位", "正星得正位（男正财/女正官坐宫）通常不喜被制被合；偏星相对可制。");
        }

        // 桃花
        DiZhi th1 = Dicts.taoHua(c.year.zhi);
        DiZhi th2 = Dicts.taoHua(c.day.zhi);
        List<String> thAt = zhiPositions(c, th1);
        for (String p : zhiPositions(c, th2)) {
            if (!thAt.contains(p)) thAt.add(p);
        }
        if (!thAt.isEmpty()) {
            s.add("桃花（6.1）", "桃花在" + th1.getCn() + (th2 != th1 ? "、" + th2.getCn() : "") + "，现于"
                    + String.join("、", thAt) + "。禄合财/官杀/伤食、禄逢三合亦为桃花（禄合印不为桃花）", "info");
        }

        s.summary = "夫妻宫以" + gong.getCn() + "为主、配偶星为辅；"
                + (broken.isEmpty() ? "宫静为吉，宜细看是否被杂透多现干扰。" : "宫受" + broken.get(0) + "，宜看制得住与否。");
    }

    // ================================================================ 七、子女

    private void ziNv(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("zinv", "子女（6.2）");
        boolean male = "M".equalsIgnoreCase(c.gender);
        ShiShen son = male ? ShiShen.QI_SHA : ShiShen.SHANG_GUAN;
        ShiShen daughter = male ? ShiShen.ZHENG_GUAN : ShiShen.SHI_SHEN;
        s.add("子女星", "本命" + (male ? "男命：七杀为儿、正官为女" : "女命：伤官为儿、食神为女")
                + "；儿星" + son.getCn() + "现 " + countShiShen(c, son) + "，女星"
                + daughter.getCn() + "现 " + countShiShen(c, daughter));
        s.add("子女宫", "时柱" + c.hour.getGanZhi() + "，看子女顺序：日支 → 时支 → 时干 → 月支 → 月干 → 年支 → 年干；"
                + "墓库逢冲可能主子女数多");
        if (c.hour.kongWang) {
            s.add("时空提示", "时柱落空亡，按 1.4「时空→子女迟损」，须结合岁运变通，不可只按一条断", "warn");
            r.hit("6.2", "子女与空亡", "看子女须看财/宫位/空亡及岁运变通，不能只按一条。");
        }
        s.summary = "子女以时柱为宫，星法依性别取官杀/食伤，须结合空亡与岁运。";
    }

    // ============================================================ 八、取象七原则

    private void quXiang(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("quxiang", "取象（共象·合象·化象·墓象·制象·带象·借象）");

        // 带象：一柱干统支
        List<String> dai = new ArrayList<String>();
        for (Pillar p : c.pillars()) {
            if (p.ganShiShen == null) continue;
            TianGan ben = ZangGan.benQi(p.zhi);
            ShiShen zs = ShiShen.of(c.dayMaster, ben);
            if (zs == null) continue;
            String gs = p.ganShiShen.getCn();
            if (gs.contains("官") && zs == ShiShen.PIAN_CAI) {
                dai.add(p.position + p.getGanZhi() + "：官戴财帽，以财为主 → 国企经营、管理财物");
            } else if (gs.contains("财") && zs.isGuanSha()) {
                dai.add(p.position + p.getGanZhi() + "：财戴官帽，以官为主 → 行政财权");
            } else if (gs.contains("印") && zs.isGuanSha()) {
                dai.add(p.position + p.getGanZhi() + "：印带官帽 → 权力");
            } else if (gs.contains("印") && zs.isCai()) {
                dai.add(p.position + p.getGanZhi() + "：印带财帽 → 工资、单位收入");
            } else {
                dai.add(p.position + p.getGanZhi() + "：天干" + gs + "统支中" + zs.getCn()
                        + "，干定支性质（" + dict.zhiWuXiangOf(p.getZhi()) + "）");
            }
        }
        s.add("带象", String.join("；", dai));

        // 墓象
        List<String> mu = new ArrayList<String>();
        for (Pillar p : c.pillars()) {
            if (p.zhi.isMuKu()) {
                TianGan ben = ZangGan.benQi(p.zhi);
                ShiShen zs = ShiShen.of(c.dayMaster, ben);
                if (zs != null) {
                    String kuType = shiShenToKu(zs);
                    mu.add(p.position + p.getZhi() + "为墓库，所墓之神为" + zs.getCn()
                            + (kuType == null ? "" : "（" + kuType + " → " + dict.getMuKu().get(kuType) + "）"));
                }
            }
        }
        s.add("墓象（1.9）", mu.isEmpty() ? "四支不见辰戌丑未，无墓象" : String.join("；", mu));

        // 制象
        List<String> zhi = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            Pillar a = c.pillars().get(i);
            for (int j = i + 1; j < 4; j++) {
                Pillar b = c.pillars().get(j);
                if (a.zhi.chong() == b.zhi) {
                    zhi.add(a.getZhi() + "（" + a.position + "）冲" + b.getZhi() + "（" + b.position + "）→ 冲制");
                } else if (a.zhi.liuHe() == b.zhi) {
                    zhi.add(a.getZhi() + "（" + a.position + "）合" + b.getZhi() + "（" + b.position + "）→ 合制");
                } else if (a.zhi.chuan() != null && a.zhi.chuan() == b.zhi) {
                    zhi.add(a.getZhi() + "（" + a.position + "）穿" + b.getZhi() + "（" + b.position + "）→ 穿制，主伤");
                }
            }
        }
        s.add("制象", zhi.isEmpty() ? "原局支间无明显合冲穿制" : String.join("；", zhi)
                + "。须分清制得住、制不尽、反被制");

        // 共象
        s.add("共象", "同一字的干支象、十神象、宫位象（必要时神煞象）有 ≥2 类指向同一事物方可定象，"
                + "单一象不足为凭（原则四）");

        s.summary = "取象须多象会合，见" + (zhi.isEmpty() ? "原局支间关系平和，宜以岁运引发" : zhi.get(0)) + "。";
        r.hit("4", "取象七原则", "共象、合象、化象、墓象、制象、带象、借象；须多象会合方可定象。");
    }

    private String shiShenToKu(ShiShen s) {
        if (s == null) return null;
        if (s.isCai()) return "财库";
        if (s.isGuanSha()) return "官杀库";
        if (s == ShiShen.SHI_SHEN) return "食神库";
        if (s == ShiShen.SHANG_GUAN) return "伤官库";
        return null;
    }

    // ============================================================ 九、正局反局

    private void zhengFanJu(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("jugeju", "正局与反局（五）");
        TianGan dm = c.dayMaster;

        // 步骤1：日主意向
        TianGan he = dm.he();
        boolean dayHe = false;
        String heAt = "";
        for (Pillar p : c.pillars()) {
            if (p == c.day) continue;
            if (p.gan == he) {
                dayHe = true;
                heAt = p.position;
            }
        }
        if (dayHe) {
            s.add("日主意向（步骤1）", "日主" + dm.getCn() + "与" + heAt + he.getCn() + "五合，日主有合，意向在"
                    + he.getCn() + "及其坐支，须一并看该柱坐支是否与日支同向");
            r.hit("5", "日主意向", "日主合到某柱时，一并看该柱坐支是否同向；意同为正局，意反为反局。");
        } else {
            s.add("日主意向（步骤1）", "日主无五合，转看日支意向（步骤2）：日支" + c.day.getZhi()
                    + "所追求之物若与原局之势相反则为反局，不能只看表面冲克");
        }

        // 步骤5：冲合反局
        List<String> cross = new ArrayList<String>();
        DiZhi[] all = {c.year.zhi, c.month.zhi, c.day.zhi, c.hour.zhi};
        String[] pos = {"年", "月", "日", "时"};
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (all[i].liuHe() == all[j]) cross.add(pos[i] + pos[j] + "合");
                else if (all[i].chong() == all[j]) cross.add(pos[i] + pos[j] + "冲");
            }
        }
        boolean chongHeFan = cross.size() >= 2
                && joinContains(cross, "年月") && joinContains(cross, "日时");
        if (chongHeFan) {
            s.add("冲合反局（步骤5）", "年月与日时构成两党，地支一边合一边冲、交叉对立 → 冲合反局；"
                    + "内外分层、主宾一致对外者不为反局", "warn");
            r.hit("5", "冲合反局", "年月与日时构成两党且地支一边合一边冲、交叉对立 → 冲合反局。");
        } else {
            s.add("冲合反局（步骤5）", "未见年月/日时交叉对冲的合冲结构，不构成典型冲合反局", "good");
        }

        // 步骤4：时支不可坏
        boolean shiZhiTi = (c.hour.zhi == Dicts.luShen(dm));
        if (shiZhiTi) {
            s.add("时支（步骤4）", "时支" + c.hour.getZhi() + "为日主之禄（体），不可坏；时支为用时方可坏", "warn");
            r.hit("5", "时支为体", "时支为劫/体，不可坏；时支是用则可坏。");
        }

        String verdict = chongHeFan ? "原局存在反局线索，主相关事项（财/官/婚/身）易有反复"
                : "原局意向未现明显反局，宜按正局看做功方向";
        s.add("判定", verdict + "。原局是车，大运是路，反局须岁运引动方应");
        s.summary = verdict;
    }

    private boolean joinContains(List<String> list, String sub) {
        for (String s : list) {
            if (s.startsWith(sub)) return true;
        }
        return false;
    }

    // ============================================================ 十、岁运应期

    private void suiYun(BaziChart c, AnalysisResult r) {
        AnalysisResult.Section s = r.section("suiyun", "大运、流年与应期（7.1—7.2）");
        s.add("起运", c.qiYunText + "（三天折一岁，一日折四月，一时辰折十日）");

        int now = Calendar.getInstance().get(Calendar.YEAR);
        DaYunItem cur = null;
        for (DaYunItem d : c.daYun) {
            if (now >= d.startYear && now <= d.endYear) {
                cur = d;
                break;
            }
        }
        if (cur != null) {
            s.add("当前大运", cur.getGanZhi() + "（" + cur.startYear + "—" + cur.endYear + "，"
                    + String.format("%.1f", cur.startAge) + "岁起，天干十神" + cur.ganShiShen + "，"
                    + cur.daXian + "）", "good");
            String rel = relationWithChart(c, cur.index);
            s.add("与原局作用", rel);
        } else {
            s.add("当前大运", "未落在已排定的大运区间内（起运前或超出排定步数）");
        }

        int liuNianIndex = JiaZi.normalize(now - 4);
        s.add("流年", now + " 年 " + JiaZi.name(liuNianIndex) + "（流年不讲空亡）");
        if (cur != null) {
            s.add("岁运作用", relationWithChart(c, liuNianIndex));
        }

        s.add("应期口诀（7.2）", "合主到，冲主动，墓主收，穿主伤。原局有合以冲为应；原局有冲以合为应；"
                + "旺者逢冲多冲起，弱者被旺冲多冲去");
        s.add("大限（7.1）", "年柱 1—18 岁、月柱 18—35 岁、日柱 35—55 岁、时柱 55 岁以后；"
                + "大限与大运合看（交脱诀）：大限凶而大运吉可暂缓，大限吉而大运凶可延迟");

        s.summary = cur == null ? "起运后参看大运与流年的合冲刑穿墓作用。"
                : "现处" + cur.getGanZhi() + "大运，流年" + JiaZi.name(liuNianIndex) + "，"
                + "以合冲刑穿墓作用到哪个字，哪个字的事项易发动。";
        r.hit("7.2", "应期规则", "合主到，冲主动，墓主收，穿主伤；原局有合以冲为应，原局有冲以合为应。");
    }

    private String relationWithChart(BaziChart c, int gzIndex) {
        DiZhi z = JiaZi.zhiOf(gzIndex);
        List<String> out = new ArrayList<String>();
        String[] pos = {"年支", "月支", "日支", "时支"};
        DiZhi[] all = {c.year.zhi, c.month.zhi, c.day.zhi, c.hour.zhi};
        for (int i = 0; i < all.length; i++) {
            if (all[i].chong() == z) out.add("冲" + pos[i] + all[i].getCn());
            else if (all[i].liuHe() == z) out.add("合" + pos[i] + all[i].getCn());
            else if (all[i].chuan() != null && all[i].chuan() == z) out.add("穿" + pos[i] + all[i].getCn());
        }
        TianGan g = JiaZi.ganOf(gzIndex);
        for (Pillar p : c.pillars()) {
            if (p.gan == g) {
                out.add("并动天干" + g.getCn() + "（" + p.position + "）");
            }
        }
        return out.isEmpty() ? "与原局无直接合冲刑穿，作用较弱" : String.join("、", out);
    }

    // ================================================================== 工具

    private List<String> zhiPositions(BaziChart c, DiZhi z) {
        List<String> out = new ArrayList<String>();
        if (z == null) return out;
        for (Pillar p : c.pillars()) {
            if (p.zhi == z) out.add(p.position);
        }
        return out;
    }

    /** 统计某十神出现次数（四柱天干 + 地支藏干） */
    private int countShiShen(BaziChart c, ShiShen target) {
        int n = 0;
        for (Pillar p : c.pillars()) {
            if (p.ganShiShen == target) n++;
            for (ZangGanItem it : p.zangGan) {
                if (it.shiShen == target) n++;
            }
        }
        return n;
    }

    /** 某十神在地支是否有根 */
    private boolean isShiShenRooted(BaziChart c, ShiShen target) {
        for (Pillar p : c.pillars()) {
            for (ZangGanItem it : p.zangGan) {
                if (it.shiShen == target) return true;
            }
        }
        return false;
    }

    private boolean hasShiShangAtMainPosition(BaziChart c) {
        ShiShen a = ShiShen.of(c.dayMaster, c.day.zhi == null ? c.dayMaster : ZangGan.benQi(c.day.zhi));
        ShiShen b = ShiShen.of(c.dayMaster, ZangGan.benQi(c.hour.zhi));
        ShiShen hg = c.hour.ganShiShen;
        return (a != null && a.isShiShang()) || (b != null && b.isShiShang()) || (hg != null && hg.isShiShang());
    }

    private boolean has(BaziChart c, WuXing wx) {
        for (Pillar p : c.pillars()) {
            if (p.gan.getWuXing() == wx || p.zhi.getWuXing() == wx) return true;
            for (ZangGanItem it : p.zangGan) {
                if (it.gan.getWuXing() == wx) return true;
            }
        }
        return false;
    }

    private boolean hasZhi(BaziChart c, DiZhi z) {
        for (Pillar p : c.pillars()) {
            if (p.zhi == z) return true;
        }
        return false;
    }

    private boolean hasZangGan(BaziChart c, TianGan g) {
        for (Pillar p : c.pillars()) {
            for (ZangGanItem it : p.zangGan) {
                if (it.gan == g) return true;
            }
        }
        return false;
    }

    private WuXing dominantWuXing(BaziChart c) {
        Map<WuXing, Integer> count = new LinkedHashMap<WuXing, Integer>();
        for (Pillar p : c.pillars()) {
            inc(count, p.gan.getWuXing(), 2);
            inc(count, p.zhi.getWuXing(), 1);
            for (ZangGanItem it : p.zangGan) {
                inc(count, it.gan.getWuXing(), 1);
            }
        }
        WuXing best = null;
        int max = -1;
        for (Map.Entry<WuXing, Integer> e : count.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    private void inc(Map<WuXing, Integer> m, WuXing k, int v) {
        Integer cur = m.get(k);
        m.put(k, (cur == null ? 0 : cur) + v);
    }

    private String starPosition(BaziChart c, ShiShen s1, ShiShen s2) {
        List<String> out = new ArrayList<String>();
        for (Pillar p : c.pillars()) {
            if (p.ganShiShen == s1 || p.ganShiShen == s2) {
                out.add(p.position);
            } else {
                for (ZangGanItem it : p.zangGan) {
                    if (it.shiShen == s1 || it.shiShen == s2) {
                        out.add(p.position);
                        break;
                    }
                }
            }
        }
        return out.isEmpty() ? "不显" : String.join("、", out);
    }

    private static boolean contains(DiZhi[] arr, DiZhi z) {
        if (arr == null) return false;
        for (DiZhi d : arr) {
            if (d == z) return true;
        }
        return false;
    }

    private static String firstChar(String s) {
        return (s == null || s.isEmpty()) ? "" : s.substring(0, 1);
    }

    private static List<String> dedup(List<String> list) {
        List<String> out = new ArrayList<String>();
        for (String s : list) {
            if (!out.contains(s)) out.add(s);
        }
        return out;
    }

    /** 静默的常量查表，避免与 BaziPaiPan 重复定义 */
    static final class Dicts {
        static DiZhi luShen(TianGan g) {
            return com.mingli.core.BaziPaiPan.luShen(g);
        }
        static DiZhi yangRen(TianGan g) {
            return com.mingli.core.BaziPaiPan.yangRen(g);
        }
        static DiZhi yiMa(DiZhi z) {
            return com.mingli.core.BaziPaiPan.yiMa(z);
        }
        static DiZhi taoHua(DiZhi z) {
            return com.mingli.core.BaziPaiPan.taoHua(z);
        }
    }
}
