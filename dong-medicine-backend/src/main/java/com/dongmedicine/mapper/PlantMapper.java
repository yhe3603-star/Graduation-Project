package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Plant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PlantMapper extends BaseMapper<Plant> {

    @Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    @Update("UPDATE plants SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    @Select("SELECT * FROM plants WHERE difficulty = #{difficulty} AND id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM plants WHERE difficulty = #{difficulty})) + 1) LIMIT #{limit}")
    List<Plant> selectRandomByDifficulty(@Param("difficulty") String difficulty, @Param("limit") int limit);

    @Select("SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy, story) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByFullText(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("SELECT * FROM plants WHERE " +
            "name_cn LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "name_dong LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "efficacy LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "story LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByLike(@Param("keyword") String keyword, @Param("limit") int limit);
}
