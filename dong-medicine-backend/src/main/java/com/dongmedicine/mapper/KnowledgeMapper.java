package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    @Update("UPDATE knowledge SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Integer id);

    @Update("UPDATE knowledge SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(@Param("id") Integer id, @Param("delta") int delta);

    @Update("UPDATE knowledge SET popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementPopularity(@Param("id") Integer id);
}
