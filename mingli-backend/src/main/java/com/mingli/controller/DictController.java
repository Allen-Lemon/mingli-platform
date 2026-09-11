package com.mingli.controller;

import com.mingli.common.R;
import com.mingli.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 规则字典接口 */
@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    /** 全部字典（规则库页面用） */
    @GetMapping("/all")
    public R all() {
        return R.ok().put("data", dictService.overview());
    }

    /** 按分类查看规则条文 */
    @GetMapping("/rules")
    public R rules(@RequestParam(required = false) String category) {
        java.util.List<java.util.Map<String, Object>> all = dictService.getRules();
        if (category == null || category.isEmpty()) {
            return R.ok().put("data", all);
        }
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<java.util.Map<String, Object>>();
        for (java.util.Map<String, Object> r : all) {
            if (category.equals(String.valueOf(r.get("category")))) {
                out.add(r);
            }
        }
        return R.ok().put("data", out);
    }

    /** 十神类象 */
    @GetMapping("/shishen")
    public R shishen() {
        return R.ok().put("data", dictService.getShiShen());
    }

    /** 干支物象 */
    @GetMapping("/wuxiang")
    public R wuxiang() {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<String, Object>();
        m.put("gan", dictService.getGanWuXiang());
        m.put("zhi", dictService.getZhiWuXiang());
        m.put("muku", dictService.getMuKu());
        return R.ok().put("data", m);
    }
}
