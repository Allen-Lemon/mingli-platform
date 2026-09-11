package com.mingli.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 常用城市经度（东经为正），用于真太阳时校正 */
public final class Cities {

    private Cities() {}

    private static final String[][] DATA = {
            {"北京", "116.4074"}, {"上海", "121.4737"}, {"广州", "113.2644"}, {"深圳", "114.0579"},
            {"天津", "117.2010"}, {"重庆", "106.5516"}, {"成都", "104.0665"}, {"杭州", "120.1551"},
            {"南京", "118.7969"}, {"武汉", "114.3055"}, {"西安", "108.9402"}, {"苏州", "120.5853"},
            {"郑州", "113.6254"}, {"长沙", "112.9388"}, {"青岛", "120.3826"}, {"沈阳", "123.4291"},
            {"哈尔滨", "126.5350"}, {"长春", "125.3235"}, {"济南", "117.0009"}, {"太原", "112.5489"},
            {"石家庄", "114.5149"}, {"合肥", "117.2830"}, {"福州", "119.3063"}, {"厦门", "118.0894"},
            {"南昌", "115.8922"}, {"昆明", "102.8329"}, {"贵阳", "106.6302"}, {"南宁", "108.3665"},
            {"海口", "110.1999"}, {"兰州", "103.8236"}, {"西宁", "101.7782"}, {"银川", "106.2309"},
            {"乌鲁木齐", "87.6168"}, {"拉萨", "91.1409"}, {"呼和浩特", "111.7519"}, {"香港", "114.1694"},
            {"澳门", "113.5439"}, {"台北", "121.5654"}
    };

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (String[] row : DATA) {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("name", row[0]);
            m.put("longitude", Double.parseDouble(row[1]));
            out.add(m);
        }
        return out;
    }
}
