package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String message;
    
    @Size(max = 20, message = "历史消息数量不能超过20条")
    private List<Message> history;
    
    @Data
    public static class Message {
        private String role;
        @Size(max = 2000, message = "历史消息长度不能超过2000个字符")
        private String content;
    }
}
