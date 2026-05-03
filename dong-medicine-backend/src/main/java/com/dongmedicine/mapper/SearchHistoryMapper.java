package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    @Select("SELECT keyword AS name, COUNT(*) AS value FROM search_history " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY keyword ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> topKeywords(@Param("limit") int limit);
}
