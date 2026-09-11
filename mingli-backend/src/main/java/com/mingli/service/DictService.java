package com.mingli.service;

import com.mingli.mapper.DictMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 命理规则字典服务。
 *
 * <p>启动时从 MySQL 加载规则字典并缓存；若字典表未初始化或数据库不可用，
 * 自动回退到内置默认数据（与 data.sql 保持一致），保证排盘与推理始终可用。
 */
@Service
public class DictService {

    private static final Logger log = LoggerFactory.getLogger(DictService.class);

    @Autowired(required = false)
    private DictMapper dictMapper;

    private volatile boolean fromDb = false;
    /** 上次尝试从数据库加载的时间戳，失败后一段时间内不重试 */
    private volatile long lastDbTryAt = 0L;
    private static final long RETRY_INTERVAL = 10_000L;

    // ---- 缓存结构 ----
    private final Map<String, String> luShen = new LinkedHashMap<String, String>();
    private final Map<String, String> yangRen = new LinkedHashMap<String, String>();
    private final Map<String, String> yiMa = new LinkedHashMap<String, String>();
    private final Map<String, String> kongWang = new LinkedHashMap<String, String>();
    private final Map<String, List<String>> gongWei = new LinkedHashMap<String, List<String>>();
    private final Map<String, Map<String, String>> shiShen = new LinkedHashMap<String, Map<String, String>>();
    private final Map<String, String> ganWuXiang = new LinkedHashMap<String, String>();
    private final Map<String, String> zhiWuXiang = new LinkedHashMap<String, String>();
    private final Map<String, String> muKu = new LinkedHashMap<String, String>();
    private final List<Map<String, Object>> quCai = new ArrayList<Map<String, Object>>();
    private final List<Map<String, Object>> quCaiMode = new ArrayList<Map<String, Object>>();
    private final List<Map<String, Object>> rules = new ArrayList<Map<String, Object>>();

    @PostConstruct
    public void init() {
        reload();
    }

    /**
     * 应用完全就绪后（spring.sql.init 已确保执行完毕）再加载一次字典。
     *
     * <p>避免 @PostConstruct 阶段与 DataSourceInitializer 的 sql 初始化产生竞态：
     * 若此时 data.sql 尚未写入全部字典表，会导致部分表被缓存为空且不再刷新。
     * ApplicationReadyEvent 在所有 bean 与 sql 脚本均完成后发布，可保证取到完整数据。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        reload();
        log.info("应用就绪后重新加载字典完成：fromDb={}, lushen={}, ganWuxiang={}, zhiWuxiang={}, rules={}",
                fromDb, luShen.size(), ganWuXiang.size(), zhiWuXiang.size(), rules.size());
    }

    /**
     * 重新加载字典：优先 MySQL，失败则回退内置默认。
     *
     * <p>启动时 sql init 脚本可能尚未执行完，故对外暴露 ensureDbLoaded()，
     * 首次访问接口时会再尝试一次从数据库加载。
     */
    public synchronized void reload() {
        lastDbTryAt = System.currentTimeMillis();
        if (dictMapper == null) {
            clearAll();
            loadDefaults();
            fromDb = false;
            return;
        }
        try {
            dictMapper.countTable("dict_lushen");
            clearAll();
            loadFromDb();
            fromDb = true;
            log.info("命理规则字典已从数据库加载（{} 条规则）", rules.size());
        } catch (Exception e) {
            clearAll();
            loadDefaults();
            fromDb = false;
            log.warn("从数据库加载字典失败，回退内置默认数据：{}", e.toString());
        }
    }

    /** 若当前使用内置默认字典，尝试改从数据库加载（带 10 秒冷却） */
    public synchronized void ensureDbLoaded() {
        if (fromDb || dictMapper == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastDbTryAt > RETRY_INTERVAL) {
            reload();
        }
    }

    private void clearAll() {
        luShen.clear(); yangRen.clear(); yiMa.clear(); kongWang.clear();
        gongWei.clear(); shiShen.clear(); ganWuXiang.clear(); zhiWuXiang.clear();
        muKu.clear(); quCai.clear(); quCaiMode.clear();
        // 规则条文在默认值中已写入，此处仅清理数据库来源标记
        rules.clear();
    }

    private void loadFromDb() {
        for (Map<String, Object> r : dictMapper.lushen()) {
            luShen.put(str(r.get("gan")), str(r.get("zhi")));
        }
        for (Map<String, Object> r : dictMapper.yangren()) {
            yangRen.put(str(r.get("gan")), str(r.get("zhi")));
        }
        for (Map<String, Object> r : dictMapper.yima()) {
            for (String g : str(r.get("group_zhi")).split("")) {
                if (!g.trim().isEmpty()) {
                    yiMa.put(g, str(r.get("ma_zhi")));
                }
            }
        }
        for (Map<String, Object> r : dictMapper.kongwang()) {
            kongWang.put(str(r.get("xun_name")), str(r.get("kong_zhi")));
        }
        for (Map<String, Object> r : dictMapper.gongwei()) {
            List<String> row = new ArrayList<String>();
            row.add(str(r.get("pos_year")));
            row.add(str(r.get("pos_month")));
            row.add(str(r.get("pos_day_gan")));
            row.add(str(r.get("pos_day_zhi")));
            row.add(str(r.get("pos_hour")));
            gongWei.put(str(r.get("dimension")), row);
        }
        for (Map<String, Object> r : dictMapper.shishen()) {
            Map<String, String> m = new LinkedHashMap<String, String>();
            m.put("positive", str(r.get("positive")));
            m.put("negative", str(r.get("negative")));
            m.put("careers", str(r.get("careers")));
            shiShen.put(str(r.get("name")), m);
        }
        for (Map<String, Object> r : dictMapper.ganWuxiang()) {
            ganWuXiang.put(str(r.get("gan")), str(r.get("wuxiang")));
        }
        for (Map<String, Object> r : dictMapper.zhiWuxiang()) {
            zhiWuXiang.put(str(r.get("zhi")), str(r.get("wuxiang")));
        }
        for (Map<String, Object> r : dictMapper.muku()) {
            muKu.put(str(r.get("ku_type")), str(r.get("wuxiang")));
        }
        quCai.addAll(dictMapper.qucai());
        quCaiMode.addAll(dictMapper.qucaiMode());
        rules.addAll(dictMapper.rules(null));
    }

    private void loadDefaults() {
        luShen.clear(); yangRen.clear(); yiMa.clear(); kongWang.clear();
        String[] lus = {"甲寅", "乙卯", "丙巳", "戊巳", "丁午", "己午", "庚申", "辛酉", "壬亥", "癸子"};
        for (String s : lus) {
            luShen.put(s.substring(0, 1), s.substring(1));
        }
        String[] rens = {"甲卯", "丙午", "戊未", "庚酉", "壬子"};
        for (String s : rens) {
            yangRen.put(s.substring(0, 1), s.substring(1));
        }
        String[] maGroups = {"申子辰", "寅午戌", "巳酉丑", "亥卯未"};
        String[] maTargets = {"寅午戌", "申子辰", "亥卯未", "巳酉丑"};
        for (int i = 0; i < maGroups.length; i++) {
            for (char c : maGroups[i].toCharArray()) {
                yiMa.put(String.valueOf(c), maTargets[i]);
            }
        }
        kongWang.put("甲子旬", "戌、亥");
        kongWang.put("甲戌旬", "申、酉");
        kongWang.put("甲申旬", "午、未");
        kongWang.put("甲午旬", "辰、巳");
        kongWang.put("甲辰旬", "寅、卯");
        kongWang.put("甲寅旬", "子、丑");

        String[][] ss = {
                {"正印", "保守/稳重/仁慈/宽容/重名节/奉献/有修养", "依赖/惰性/无主见/呆滞/不进取", "公务员/教师/文化人/宗教/慈善/护士"},
                {"偏印", "思考力/敏感/机智/谋略/创意/不随大流", "自私/冷淡/挑剔/福薄/不通人情", "技术/医生/艺人/五术/咨询/律师/记者/编辑/情报/设计"},
                {"正官", "守法/正道/规矩/文雅/忠孝/自制/责任感/正义", "刻板/严肃/压力", "行政/管理/正规职务"},
                {"七杀", "胆量/果断/威权/魄力/进取", "暴烈/偏激/病灾/违法倾向", "军警/执法/实权/武职（须有制化）"},
                {"正财", "稳定/专一/勤恳/守本分", "保守/计较", "稳定收入/工薪/经营"},
                {"偏财", "经营/投机/外财/慷慨/交际/多情", "不专/欲望重/风险", "经营/贸易/资本/中介"},
                {"食神", "温和/专注/服务/思想/福寿", "懒散/贪图享受", "服务/教育/餐饮/文艺/技术"},
                {"伤官", "创意/表现/反叛/手艺/聪明", "不驯/口舌/傲慢/官非", "艺术/写作/技术/法律/表演/经营"},
                {"比肩", "独立/自主/合作/竞争", "固执/争斗", "合伙/自由业/体力/运动"},
                {"劫财", "胆大/操作/投机/争夺", "冒险/破财/争斗/非法", "资本运作/投机/操作类（须有制）"}
        };
        for (String[] row : ss) {
            Map<String, String> m = new LinkedHashMap<String, String>();
            m.put("positive", row[1]);
            m.put("negative", row[2]);
            m.put("careers", row[3]);
            shiShen.put(row[0], m);
        }

        String[] gans = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        String[] ganWx = {"树木/首领/头/肝", "花草/丝线/颈/脉", "太阳/权力/名气/眼/脑", "灯火/文化/医玄/心/眼",
                "大地/建筑/政府/胃", "田园/房屋/财帛/脾", "金铁/机器/军警/肺/骨", "珠宝/法律/精加工/肺/耳",
                "江海/运输/贸易/血液", "雨露/智谋/玄学/肾/脑"};
        for (int i = 0; i < gans.length; i++) {
            ganWuXiang.put(gans[i], ganWx[i]);
        }
        String[] zhis = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        String[] zhiWx = {"流动/贸易/技能", "湿土/隐蔽/银行/牢狱/玄学（阴中之阴）", "木/机构/头/肝", "草木/织物/建材/肝/肢",
                "湿土/库/机器/车辆/药", "变化/文化/影像", "火/信息/文章/名气", "田园/建筑/医药",
                "铁器/军警/司法/金融", "金石/法律/技术/传媒（阴中之阴）", "燥土/军火/学校/市场/建筑", "水/科技/数字/思想"};
        for (int i = 0; i < zhis.length; i++) {
            zhiWuXiang.put(zhis[i], zhiWx[i]);
        }

        muKu.put("羊刃库", "军队/警察/军团/营地");
        muKu.put("伤官库", "寺庙/学校");
        muKu.put("食神库", "学校/工厂");
        muKu.put("财库", "银行");
        muKu.put("官杀库", "权力中心/组织部");

        quCai.clear();
        quCai.add(row("cond", "无财、无伤食泄", "method", "以禄当财",
                "xiji", "喜印生禄；忌伤食泄禄、劫财分禄；辛苦求财"));
        quCai.add(row("cond", "无财、有伤食", "method", "以伤食当财",
                "xiji", "伤官偏经营/谋略，食神偏思想/脑力；地支内食神做功→企业经营"));
        quCai.add(row("cond", "财官相统/官杀制不尽", "method", "官杀当财",
                "xiji", "须财官相连且与日主/主位有关系；以原局为主"));

        quCaiMode.clear();
        quCaiMode.add(row("feature", "伤食做功/食神生财/财星做功/内食神格", "method", "经营取财",
                "industries", "商业/企业/门店/生产/开发"));
        quCaiMode.add(row("feature", "比劫/劫财做功且效率高", "method", "风险取财",
                "industries", "股票/证券/期货/资本运作"));
        quCaiMode.add(row("feature", "伤食居主位或与主位做功/木火成势", "method", "智力取财",
                "industries", "咨询/法律/设计/艺术/写作/技术"));
        quCaiMode.add(row("feature", "比劫/劫财/禄做功而效率低", "method", "体力取财",
                "industries", "体力劳动"));
        quCaiMode.add(row("feature", "效率高且配伤食/丙丁名气/虚透官杀", "method", "可取运动/歌影/表演",
                "industries", "文体/演艺"));
        quCaiMode.add(row("feature", "印/官杀/食伤/财各小功而效率不高", "method", "工薪取财",
                "industries", "稳定工薪"));

        if (rules.isEmpty()) {
            rules.add(row("rule_code", "10", "category", "边界", "title", "存疑与使用边界",
                    "content", "本平台规则为传统命理文献结构化整理，不构成科学结论或现实预测承诺。", "doubtful", 1));
        }
    }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    // ------------------------------------------------------------ 访问接口

    public boolean isFromDb() { ensureDbLoaded(); return fromDb; }
    public Map<String, String> getLuShen() { ensureDbLoaded(); return Collections.unmodifiableMap(luShen); }
    public Map<String, String> getYangRen() { return Collections.unmodifiableMap(yangRen); }
    public Map<String, String> getYiMa() { return Collections.unmodifiableMap(yiMa); }
    public Map<String, String> getKongWang() { return Collections.unmodifiableMap(kongWang); }
    public Map<String, List<String>> getGongWei() { return Collections.unmodifiableMap(gongWei); }
    public Map<String, Map<String, String>> getShiShen() { return Collections.unmodifiableMap(shiShen); }
    public Map<String, String> getGanWuXiang() { return Collections.unmodifiableMap(ganWuXiang); }
    public Map<String, String> getZhiWuXiang() { return Collections.unmodifiableMap(zhiWuXiang); }
    public Map<String, String> getMuKu() { return Collections.unmodifiableMap(muKu); }
    public List<Map<String, Object>> getQuCai() { return Collections.unmodifiableList(quCai); }
    public List<Map<String, Object>> getQuCaiMode() { return Collections.unmodifiableList(quCaiMode); }
    public List<Map<String, Object>> getRules() { return Collections.unmodifiableList(rules); }

    /** 十神类象，缺失时返回空描述 */
    public Map<String, String> shiShenOf(String name) {
        Map<String, String> m = shiShen.get(name);
        if (m != null) {
            return m;
        }
        Map<String, String> empty = new LinkedHashMap<String, String>();
        empty.put("positive", "");
        empty.put("negative", "");
        empty.put("careers", "");
        return empty;
    }

    public String ganWuXiangOf(String gan) { ensureDbLoaded(); return ganWuXiang.get(gan); }
    public String zhiWuXiangOf(String zhi) { ensureDbLoaded(); return zhiWuXiang.get(zhi); }

    /** 字典总览，供前端「规则库」页展示 */
    public Map<String, Object> overview() {
        ensureDbLoaded();
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("lushen", luShen);
        m.put("yangren", yangRen);
        m.put("yima", yiMa);
        m.put("kongwang", kongWang);
        m.put("gongwei", gongWei);
        m.put("shishen", shiShen);
        m.put("ganWuxiang", ganWuXiang);
        m.put("zhiWuxiang", zhiWuXiang);
        m.put("muku", muKu);
        m.put("qucai", quCai);
        m.put("qucaiMode", quCaiMode);
        m.put("rules", rules);
        m.put("source", fromDb ? "MySQL" : "内置默认");
        return m;
    }
}
