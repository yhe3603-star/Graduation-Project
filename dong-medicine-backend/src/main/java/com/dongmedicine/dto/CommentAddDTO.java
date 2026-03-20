package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentAddDTO {

    @NotBlank(message = "目标类型不能为空")
    @Size(max = 50, message = "目标类型长度不能超过50个字符")
    private String targetType;

    @NotNull(message = "目标ID不能为空")
    private Integer targetId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String content;

    private Integer replyToId;
    
    private Integer replyToUserId;
    
    private String replyToUsername;
}
