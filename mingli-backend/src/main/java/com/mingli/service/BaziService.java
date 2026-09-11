package com.mingli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingli.core.BaziPaiPan;
import com.mingli.core.JiaZi;
import com.mingli.core.model.AnalysisResult;
import com.mingli.core.model.BaziChart;
import com.mingli.dto.AnalyzeRequest;
import com.mingli.entity.BaziRecord;
import com.mingli.mapper.BaziRecordMapper;
import com.mingli.rules.MingLiRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 排盘与命理分析业务 */
@Service
public class BaziService {

    private static final Logger log = LoggerFactory.getLogger(BaziService.class);

    @Autowired
    private MingLiRuleEngine engine;

    @Autowired(required = false)
    private BaziRecordMapper recordMapper;

    @Autowired
    private DictService dictService;

    private final ObjectMapper om = new ObjectMapper();

    /** 排盘 + 推理，可选择落库 */
    public Map<String, Object> analyze(AnalyzeRequest req) {
        BaziPaiPan.BirthInput in = new BaziPaiPan.BirthInput();
        in.name = req.getName();
        in.gender = req.getGender();
        in.year = req.getYear();
        in.month = req.getMonth();
        in.day = req.getDay();
        in.hour = req.getHour() == null ? 12 : req.getHour();
        in.minute = req.getMinute() == null ? 0 : req.getMinute();
        in.longitude = req.getLongitude() == null ? 120.0 : req.getLongitude();
        in.tzOffsetHours = req.getTzOffset() == null ? 8.0 : req.getTzOffset();
        in.cityName = req.getCityName();
        in.useTrueSolarTime = !Boolean.FALSE.equals(req.getUseTrueSolarTime());
        in.calendarType = "SOLAR";

        BaziChart chart = BaziPaiPan.paiPan(in);
        AnalysisResult analysis = engine.analyze(chart);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("chart", chart);
        res.put("analysis", analysis);
        res.put("dict", dictService.overview());

        if (Boolean.TRUE.equals(req.getSave())) {
            try {
                Long id = save(chart, analysis, in);
                res.put("recordId", id);
            } catch (Exception e) {
                log.warn("保存排盘记录失败（数据库可能未就绪）：{}", e.getMessage());
                res.put("saveError", "记录未保存：" + e.getMessage());
            }
        }
        return res;
    }

    private Long save(BaziChart chart, AnalysisResult analysis, BaziPaiPan.BirthInput in) throws Exception {
        if (recordMapper == null) {
            throw new IllegalStateException("持久层未启用");
        }
        BaziRecord r = new BaziRecord();
        r.setName(chart.name);
        r.setGender(chart.gender);
        r.setBirthTime(chart.birthTime);
        r.setCalendarType(chart.calendarType);
        r.setTzOffset(new BigDecimal(in.tzOffsetHours));
        r.setLongitude(new BigDecimal(in.longitude));
        r.setCityName(chart.cityName);
        r.setTrueSolar(in.useTrueSolarTime ? 1 : 0);
        r.setYearGz(JiaZi.name(chart.year.index));
        r.setMonthGz(JiaZi.name(chart.month.index));
        r.setDayGz(JiaZi.name(chart.day.index));
        r.setHourGz(JiaZi.name(chart.hour.index));
        r.setDayMaster(chart.dayMaster.getCn());
        r.setChartJson(om.writeValueAsString(chart));
        r.setAnalysisJson(om.writeValueAsString(analysis));
        recordMapper.insert(r);
        return r.getId();
    }

    /** 分页查询历史记录 */
    public Map<String, Object> history(int page, int size, String keyword, String gender) {
        Map<String, Object> res = new HashMap<String, Object>();
        if (recordMapper == null) {
            res.put("list", new java.util.ArrayList<Map<String, Object>>());
            res.put("total", 0);
            res.put("warning", "数据库未连接，历史记录不可用");
            return res;
        }
        try {
            int offset = Math.max(0, (page - 1) * size);
            List<BaziRecord> list = recordMapper.selectPage(offset, size, keyword, gender);
            int total = recordMapper.count(keyword, gender);
            res.put("list", list);
            res.put("total", total);
        } catch (Exception e) {
            log.warn("查询历史记录失败：{}", e.getMessage());
            res.put("list", new java.util.ArrayList<Map<String, Object>>());
            res.put("total", 0);
            res.put("warning", "数据库未就绪：" + e.getMessage());
        }
        return res;
    }

    public BaziRecord detail(Long id) {
        return recordMapper == null ? null : recordMapper.selectById(id);
    }

    public int delete(Long id) {
        return recordMapper == null ? 0 : recordMapper.deleteById(id);
    }

    /** 首页概览统计 */
    public Map<String, Object> stat() {
        Map<String, Object> m = new HashMap<String, Object>();
        int total = 0;
        List<Map<String, Object>> byDm = new java.util.ArrayList<Map<String, Object>>();
        if (recordMapper != null) {
            try {
                total = recordMapper.count(null, null);
                byDm = recordMapper.statByDayMaster();
            } catch (Exception e) {
                log.warn("统计失败：{}", e.getMessage());
            }
        }
        m.put("total", total);
        m.put("byDayMaster", byDm);
        m.put("dictSource", dictService.isFromDb() ? "MySQL" : "内置默认");
        m.put("year", Calendar.getInstance().get(Calendar.YEAR));
        return m;
    }
}
