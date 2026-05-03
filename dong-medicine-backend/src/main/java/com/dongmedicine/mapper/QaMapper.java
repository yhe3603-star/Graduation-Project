package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Qa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface QaMapper extends BaseMapper<Qa> {

    @Update("UPDATE qa SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    @Update("UPDATE qa SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    @Update("UPDATE qa SET view_count = IFNULL(view_count, 0) + 3, popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementViewCount3AndPopularity(Integer id);

    // ===== 统计查询方法 =====

    @Select("SELECT COUNT(DISTINCT category) FROM qa WHERE category IS NOT NULL AND category != ''")
    int countDistinctCategory();

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM qa")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM qa")
    long sumFavoriteCount();

    // ===== 去重查询方法（用于筛选器） =====

    @Select("SELECT DISTINCT category FROM qa WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategory();

    @Select("SELECT category AS name, IFNULL(SUM(popularity), 0) AS value FROM qa WHERE category IS NOT NULL AND category != '' GROUP BY category ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> topCategoryByPopularity(@Param("limit") int limit);
}
