package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Plant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface PlantMapper extends BaseMapper<Plant> {

    @Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    @Update("UPDATE plants SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    @Update("UPDATE plants SET popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementPopularity(Integer id);

    @Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 3, popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementViewCount3AndPopularity(Integer id);

    @Select("SELECT * FROM plants WHERE id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM plants)) + 1) LIMIT #{limit}")
    List<Plant> selectRandomPlants(@Param("limit") int limit);

    @Select("SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy, story) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByFullText(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("SELECT * FROM plants WHERE " +
            "name_cn LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "name_dong LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "efficacy LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "story LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByLike(@Param("keyword") String keyword, @Param("limit") int limit);

    // ===== 统计查询方法 =====

    @Select("SELECT COUNT(DISTINCT category) FROM plants WHERE category IS NOT NULL AND category != ''")
    int countDistinctCategory();

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM plants")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM plants")
    long sumFavoriteCount();

    // ===== 去重查询方法（用于筛选器） =====

    @Select("SELECT DISTINCT category FROM plants WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategory();

    @Select("SELECT DISTINCT usage_way FROM plants WHERE usage_way IS NOT NULL AND usage_way != '' ORDER BY usage_way")
    List<String> selectDistinctUsageWay();

    @Select("SELECT category AS name, COUNT(*) AS value FROM plants WHERE category IS NOT NULL AND category != '' GROUP BY category ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> countByCategory(@Param("limit") int limit);

    @Select("SELECT distribution AS name, COUNT(*) AS value FROM plants WHERE distribution IS NOT NULL AND distribution != '' GROUP BY distribution ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> countByDistribution(@Param("limit") int limit);

    @Select("SELECT name_cn AS name, view_count AS value FROM plants WHERE view_count > 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> topByViewCount(@Param("limit") int limit);
}
