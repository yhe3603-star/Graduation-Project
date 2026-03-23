package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerDTO {

    @NotNull(message = "题目ID不能为空")
    private Integer questionId;

    @NotBlank(message = "答案不能为空")
    private String answer;
}
