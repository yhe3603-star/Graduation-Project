package com.dongmedicine.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizQuestionDTO {

    private Integer id;

    private String q;

    private List<String> options;
}