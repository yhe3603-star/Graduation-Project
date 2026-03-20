package com.dongmedicine.service;

import com.dongmedicine.dto.ChatRequest;
import com.dongmedicine.dto.ChatResponse;

public interface AiChatService {
    ChatResponse chat(ChatRequest request);
}
