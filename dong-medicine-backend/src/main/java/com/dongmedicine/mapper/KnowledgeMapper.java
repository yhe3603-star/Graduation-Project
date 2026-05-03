package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    @Update("UPDATE knowledge SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Integer id);

    @Update("UPDATE knowledge SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(@Param("id") Integer id, @Param("delta") int delta);

    @Update("UPDATE knowledge SET popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementPopularity(@Param("id") Integer id);

    @Update("UPDATE knowledge SET view_count = IFNULL(view_count, 0) + 3, popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementViewCount3AndPopularity(@Param("id") Integer id);

    // ===== 统计查询方法 =====

    @Select("SELECT COUNT(DISTINCT therapy_category) FROM knowledge WHERE therapy_category IS NOT NULL AND therapy_category != ''")
    int countDistinctTherapyCategory();

    @Select("SELECT COUNT(DISTINCT disease_category) FROM knowledge WHERE disease_category IS NOT NULL AND disease_category != ''")
    int countDistinctDiseaseCategory();

    @Select("SELECT COUNT(DISTINCT type) FROM knowledge WHERE type IS NOT NULL AND type != ''")
    int countDistinctType();

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM knowledge")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM knowledge")
    long sumFavoriteCount();

    // ===== 去重查询方法（用于筛选器） =====

    @Select("SELECT DISTINCT therapy_category FROM knowledge WHERE therapy_category IS NOT NULL AND therapy_category != '' ORDER BY therapy_category")
    List<String> selectDistinctTherapyCategory();

    @Select("SELECT DISTINCT disease_category FROM knowledge WHERE disease_category IS NOT NULL AND disease_category != '' ORDER BY disease_category")
    List<String> selectDistinctDiseaseCategory();

    @Select("SELECT DISTINCT herb_category FROM knowledge WHERE herb_category IS NOT NULL AND herb_category != '' ORDER BY herb_category")
    List<String> selectDistinctHerbCategory();

    @Select("SELECT therapy_category AS name, COUNT(*) AS value FROM knowledge WHERE therapy_category IS NOT NULL AND therapy_category != '' GROUP BY therapy_category ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> countByTherapyCategory(@Param("limit") int limit);

    @Select("SELECT title AS name, popularity AS value FROM knowledge WHERE popularity > 0 ORDER BY popularity DESC LIMIT #{limit}")
    List<Map<String, Object>> topByPopularity(@Param("limit") int limit);

    @Select("SELECT title AS name, view_count AS value FROM knowledge WHERE (type = '药方' OR title LIKE '%方%') AND view_count > 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> topFormulaByViewCount(@Param("limit") int limit);

    @Select("SELECT title AS name, view_count AS value FROM knowledge WHERE view_count > 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> topByViewCount(@Param("limit") int limit);
}
