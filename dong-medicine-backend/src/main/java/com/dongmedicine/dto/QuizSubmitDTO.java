package com.dongmedicine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuizSubmitDTO {

    @Valid
    @NotEmpty(message = "答案列表不能为空")
    private List<AnswerDTO> answers;
}