package com.dongmedicine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerDTO {

    @NotNull(message = "题目ID不能为空")
    private Integer questionId;

    private String answer;
}
