package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    private String message;
    private List<Message> history;
    
    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
