package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackDTO {

    @NotBlank(message = "反馈类型不能为空")
    @Size(max = 20, message = "反馈类型长度不能超过20字符")
    private String type;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容长度不能超过2000字符")
    private String content;

    @Size(max = 100, message = "联系方式长度不能超过100字符")
    private String contact;
}
