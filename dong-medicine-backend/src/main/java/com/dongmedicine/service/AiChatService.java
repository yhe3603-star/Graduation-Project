package com.dongmedicine.service;

import com.dongmedicine.dto.ChatRequest;
import com.dongmedicine.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;

public interface AiChatService {

    ChatResponse chat(ChatRequest request);

    void chatStream(String message, JsonNode history, StreamCallback callback);

    interface StreamCallback {
        void onToken(String token);
        void onComplete(String fullReply);
        void onError(String error);
    }
}
