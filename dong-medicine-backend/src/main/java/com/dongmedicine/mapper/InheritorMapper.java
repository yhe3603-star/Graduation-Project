package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Inheritor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface InheritorMapper extends BaseMapper<Inheritor> {

    @Update("UPDATE inheritors SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    @Update("UPDATE inheritors SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    @Update("UPDATE inheritors SET popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementPopularity(Integer id);

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
}
