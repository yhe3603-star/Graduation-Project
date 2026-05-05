package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Inheritor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface InheritorMapper extends BaseMapper<Inheritor> {

    @Update("UPDATE inheritors SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    @Update("UPDATE inheritors SET favorite_count = IFNULL(favorite_count, 0) + #{delta}, popularity = IFNULL(popularity, 0) + #{delta} * 10 WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    @Update("UPDATE inheritors SET view_count = IFNULL(view_count, 0) + 1, popularity = IFNULL(popularity, 0) + 3 WHERE id = #{id}")
    void incrementViewCount3AndPopularity(Integer id);

    // ===== 统计查询方法 =====

    @Select("SELECT COUNT(*) FROM inheritors WHERE level = #{level}")
    int countByLevel(@Param("level") String level);

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM inheritors")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM inheritors")
    long sumFavoriteCount();

    // ===== 去重查询方法（用于筛选器） =====

    @Select("SELECT DISTINCT level FROM inheritors WHERE level IS NOT NULL AND level != '' ORDER BY level")
    List<String> selectDistinctLevel();

    @Select("SELECT name, view_count AS value FROM inheritors WHERE view_count > 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> topByViewCount(@Param("limit") int limit);
}
