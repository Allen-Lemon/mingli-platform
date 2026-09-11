package com.mingli.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 命理规则字典持久层 */
public interface DictMapper {

    List<Map<String, Object>> lushen();
    List<Map<String, Object>> yangren();
    List<Map<String, Object>> yima();
    List<Map<String, Object>> kongwang();
    List<Map<String, Object>> gongwei();
    List<Map<String, Object>> shishen();
    List<Map<String, Object>> ganWuxiang();
    List<Map<String, Object>> zhiWuxiang();
    List<Map<String, Object>> muku();
    List<Map<String, Object>> qucai();
    List<Map<String, Object>> qucaiMode();
    List<Map<String, Object>> rules(@Param("category") String category);
    List<Map<String, Object>> categories();

    /** 用于启动时探测字典是否已初始化 */
    int countTable(@Param("table") String table);
}
