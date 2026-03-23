package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容长度不能超过1000字符")
    private String reply;
}
