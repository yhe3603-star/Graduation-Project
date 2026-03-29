package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.QuizQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuizQuestionMapper extends BaseMapper<QuizQuestion> {

    @Select("SELECT * FROM quiz_questions WHERE id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM quiz_questions)) + 1) LIMIT #{limit}")
    List<QuizQuestion> selectRandomQuestions(@Param("limit") int limit);

    @Select("SELECT * FROM quiz_questions WHERE difficulty = #{difficulty} AND id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM quiz_questions WHERE difficulty = #{difficulty})) + 1) LIMIT #{limit}")
    List<QuizQuestion> selectRandomQuestionsByDifficulty(@Param("difficulty") String difficulty, @Param("limit") int limit);
}
