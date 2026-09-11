package com.mingli.mapper;

import com.mingli.entity.BaziRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 排盘记录持久层 */
public interface BaziRecordMapper {

    int insert(BaziRecord record);

    BaziRecord selectById(Long id);

    List<BaziRecord> selectPage(@Param("offset") int offset,
                                @Param("size") int size,
                                @Param("keyword") String keyword,
                                @Param("gender") String gender);

    int count(@Param("keyword") String keyword, @Param("gender") String gender);

    int deleteById(Long id);

    List<Map<String, Object>> statByDayMaster();
}
