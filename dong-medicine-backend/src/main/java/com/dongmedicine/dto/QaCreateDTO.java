package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 问答创建DTO - 仅包含管理员可提交的字段，防止字段篡改
 */
@Data
public class QaCreateDTO {

    @Size(max = 50, message = "分类长度不能超过50字符")
    private String category;

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字符")
    private String question;

    @NotBlank(message = "答案不能为空")
    @Size(max = 5000, message = "答案长度不能超过5000字符")
    private String answer;
}
