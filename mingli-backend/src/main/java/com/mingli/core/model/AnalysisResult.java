package com.mingli.core.model;

import java.util.ArrayList;
import java.util.List;

/** 命理分析结果 */
public class AnalysisResult {

    /** 一句话总评 */
    public String headline = "";
    /** 关键提示 */
    public List<String> highlights = new ArrayList<String>();
    /** 分析分块 */
    public List<Section> sections = new ArrayList<Section>();
    /** 命中的规则条文（含出处编号） */
    public List<RuleHit> ruleHits = new ArrayList<RuleHit>();
    /** 免责声明 */
    public String disclaimer =
            "本结果为传统命理文献（《盲派基本规则》结构化整理稿）的规则线索推演，仅供文化研究参考，"
          + "不构成科学结论或现实预测承诺，不应作为医疗、法律、投资、婚恋等重大决策依据。";

    public Section section(String key, String title) {
        for (Section s : sections) {
            if (s.key.equals(key)) {
                return s;
            }
        }
        Section s = new Section();
        s.key = key;
        s.title = title;
        sections.add(s);
        return s;
    }

    public void hit(String code, String title, String text) {
        hit(code, title, text, false);
    }

    public void hit(String code, String title, String text, boolean doubtful) {
        RuleHit r = new RuleHit();
        r.code = code;
        r.title = title;
        r.text = text;
        r.doubtful = doubtful;
        ruleHits.add(r);
    }

    public static class Section {
        public String key;
        public String title;
        public String summary = "";
        public List<Item> items = new ArrayList<Item>();

        public void add(String label, String value) {
            add(label, value, "info");
        }

        public void add(String label, String value, String level) {
            Item it = new Item();
            it.label = label;
            it.value = value;
            it.level = level;
            items.add(it);
        }
    }

    public static class Item {
        public String label;
        public String value;
        /** info / good / warn */
        public String level = "info";
    }

    public static class RuleHit {
        public String code;
        public String title;
        public String text;
        public boolean doubtful;
    }
}
