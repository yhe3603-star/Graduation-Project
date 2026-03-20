package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;

@Data
@TableName("quiz_questions")
public class QuizQuestion {
    @TableId
    private Integer id;
    private String question;
    @JsonIgnore
    private String options;
    private String answer;
    private String category;
    private String difficulty;
    private String correctAnswer;
    private String explanation;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> getOptionList() {
        try {
            return objectMapper.readValue(options, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    public void setOptionList(List<String> optionList) {
        try {
            this.options = objectMapper.writeValueAsString(optionList);
        } catch (Exception e) {
            this.options = "[]";
        }
    }
}
