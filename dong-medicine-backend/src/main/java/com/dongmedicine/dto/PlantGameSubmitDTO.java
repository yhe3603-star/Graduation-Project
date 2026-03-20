package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlantGameSubmitDTO {
    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotNull(message = "得分不能为空")
    private Integer score;

    @NotNull(message = "正确答题数不能为空")
    private Integer correctCount;

    @NotNull(message = "总答题数不能为空")
    private Integer totalCount;
}
