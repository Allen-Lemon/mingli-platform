package com.mingli.controller;

import com.mingli.common.R;
import com.mingli.dto.AnalyzeRequest;
import com.mingli.service.BaziService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/** 排盘与命理分析接口 */
@RestController
@RequestMapping("/api/bazi")
public class BaziController {

    @Autowired
    private BaziService baziService;

    /** 排盘 + 盲派规则推理 */
    @PostMapping("/analyze")
    public R analyze(@Valid @RequestBody AnalyzeRequest req) {
        try {
            return R.ok().put("data", baziService.analyze(req));
        } catch (Exception e) {
            return R.error("排盘失败：" + e.getMessage());
        }
    }

    /** 历史记录列表 */
    @GetMapping("/history")
    public R history(@RequestParam(defaultValue = "1") int page,
                     @RequestParam(defaultValue = "10") int size,
                     @RequestParam(required = false) String keyword,
                     @RequestParam(required = false) String gender) {
        return R.ok(baziService.history(page, size, keyword, gender));
    }

    /** 历史记录详情 */
    @GetMapping("/history/{id}")
    public R detail(@PathVariable Long id) {
        Object r = baziService.detail(id);
        return r == null ? R.error("记录不存在") : R.ok(r);
    }

    /** 删除历史记录 */
    @DeleteMapping("/history/{id}")
    public R delete(@PathVariable Long id) {
        return R.ok(baziService.delete(id));
    }

    /** 概览统计 */
    @GetMapping("/stat")
    public R stat() {
        return R.ok(baziService.stat());
    }

    /** 城市经度参考表（前端真太阳时校正用） */
    @GetMapping("/cities")
    public R cities() {
        return R.ok(com.mingli.common.Cities.list());
    }
}
