package com.dongmedicine.service.impl;

import com.dongmedicine.config.DeepSeekConfig;
import com.dongmedicine.dto.ChatRequest;
import com.dongmedicine.dto.ChatResponse;
import com.dongmedicine.service.AiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private DeepSeekConfig deepSeekConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = "你是侗族医药智能助手，专门回答关于侗族医药的问题。" +
        "侗族医药是中国传统医药的重要组成部分，具有悠久的历史和独特的理论体系。" +
        "请用专业、友好的语气回答用户的问题，如果问题超出侗族医药范围，请礼貌说明。" +
        "回答时可以适当介绍侗族医药的特点、常用药物、传统疗法等知识。";

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            // 检查API密钥是否配置
            if (deepSeekConfig.getApiKey() == null || deepSeekConfig.getApiKey().isEmpty()) {
                log.warn("DeepSeek API密钥未配置");
                return ChatResponse.error("AI服务未配置: 请在环境变量中设置DEEPSEEK_API_KEY");
            }
            
            String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
            log.info("调用DeepSeek API: {}", url);
            log.info("API Key: {}...", deepSeekConfig.getApiKey().substring(0, Math.min(10, deepSeekConfig.getApiKey().length())));
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            
            if (request.getHistory() != null) {
                for (ChatRequest.Message msg : request.getHistory()) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }
            
            messages.add(Map.of("role", "user", "content", request.getMessage()));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", deepSeekConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekConfig.getApiKey());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("发送请求体: {}", objectMapper.writeValueAsString(requestBody));
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            log.info("API响应状态: {}", response.getStatusCode());
            log.info("API响应体: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("error")) {
                    String errorMsg = root.path("error").path("message").asText();
                    log.error("DeepSeek API错误: {}", errorMsg);
                    return ChatResponse.error("AI服务错误: " + errorMsg);
                }
                String reply = root.path("choices").get(0).path("message").path("content").asText();
                return ChatResponse.success(reply);
            } else {
                return ChatResponse.error("AI服务响应异常: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("AI聊天服务异常", e);
            return ChatResponse.error("AI服务暂时不可用: " + e.getMessage());
        }
    }
}
