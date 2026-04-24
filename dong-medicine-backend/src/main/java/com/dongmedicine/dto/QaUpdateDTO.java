package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 问答更新DTO - 包含id字段，仅允许管理员修改的字段
 */
@Data
public class QaUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Integer id;

    @Size(max = 50, message = "分类长度不能超过50字符")
    private String category;

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字符")
    private String question;

    @NotBlank(message = "答案不能为空")
    @Size(max = 5000, message = "答案长度不能超过5000字符")
    private String answer;
}
