package com.dongmedicine.dto;

import lombok.Data;

@Data
public class ChatResponse {
    private String reply;
    private boolean success;
    private String error;
    
    public static ChatResponse success(String reply) {
        ChatResponse response = new ChatResponse();
        response.setReply(reply);
        response.setSuccess(true);
        return response;
    }
    
    public static ChatResponse error(String error) {
        ChatResponse response = new ChatResponse();
        response.setError(error);
        response.setSuccess(false);
        return response;
    }
}
