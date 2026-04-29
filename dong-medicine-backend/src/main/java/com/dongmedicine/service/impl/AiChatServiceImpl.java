package com.dongmedicine.service.impl;

import com.dongmedicine.config.DeepSeekConfig;
import com.dongmedicine.dto.ChatRequest;
import com.dongmedicine.dto.ChatResponse;
import com.dongmedicine.service.AiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import io.netty.channel.ChannelOption;
import reactor.core.scheduler.Schedulers;

import reactor.core.Disposable;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final DeepSeekConfig deepSeekConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private WebClient webClient;

    private static final String SYSTEM_PROMPT = "你是侗族医药智能助手，专门回答关于侗族医药的问题。" +
        "侗族医药是中国传统医药的重要组成部分，具有悠久的历史和独特的理论体系。" +
        "请用专业、友好的语气回答用户的问题，如果问题超出侗族医药范围，请礼貌说明。" +
        "回答时可以适当介绍侗族医药的特点、常用药物、传统疗法等知识。";

    @PostConstruct
    private void initWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .responseTimeout(Duration.ofSeconds(120));
        webClient = WebClient.builder()
                .baseUrl(deepSeekConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekConfig.getApiKey())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);

        try {
            if (deepSeekConfig.getApiKey() == null || deepSeekConfig.getApiKey().isEmpty()) {
                log.warn("[{}] AI服务未配置", requestId);
                return ChatResponse.error("AI服务未配置，请联系管理员");
            }

            String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
            log.debug("[{}] 调用AI服务开始", requestId);

            List<Map<String, String>> messages = buildMessages(request.getMessage(), request.getHistory());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", deepSeekConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekConfig.getApiKey());

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            org.springframework.http.ResponseEntity<String> response =
                    restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, entity, String.class);

            long elapsed = System.currentTimeMillis() - startTime;

            if (response.getStatusCode() == org.springframework.http.HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("error")) {
                    String errorMsg = root.path("error").path("message").asText();
                    log.error("[{}] AI服务返回错误, 耗时={}ms", requestId, elapsed);
                    return ChatResponse.error("AI服务暂时不可用");
                }
                String reply = root.path("choices").get(0).path("message").path("content").asText();
                log.debug("[{}] AI服务调用成功, 耗时={}ms, 回复长度={}", requestId, elapsed, reply.length());
                return ChatResponse.success(reply);
            } else {
                log.error("[{}] AI服务响应异常, 状态={}, 耗时={}ms", requestId, response.getStatusCode(), elapsed);
                return ChatResponse.error("AI服务响应异常");
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[{}] AI服务调用异常, 耗时={}ms, 错误类型={}", requestId, elapsed, e.getClass().getSimpleName());
            return ChatResponse.error("AI服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public Disposable chatStream(String message, JsonNode history, StreamCallback callback) {
        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);

        try {
            if (deepSeekConfig.getApiKey() == null || deepSeekConfig.getApiKey().isEmpty()) {
                log.warn("[{}] AI服务未配置", requestId);
                callback.onError("AI服务未配置，请联系管理员");
                return () -> {};
            }

            log.info("[{}] 调用AI流式服务开始, message={}", requestId, message.substring(0, Math.min(50, message.length())));

            List<Map<String, String>> messages = buildMessagesFromJson(message, history);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", deepSeekConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            requestBody.put("stream", true);

            StringBuilder fullReply = new StringBuilder();

            return webClient
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .filter(line -> !line.isBlank() && !line.equals("[DONE]"))
                    .map(line -> {
                        String data = line.trim();
                        if (data.startsWith("data:")) {
                            return data.substring(5).trim();
                        }
                        return data;
                    })
                    .publishOn(Schedulers.boundedElastic())
                    .subscribe(
                            data -> {
                                try {
                                    JsonNode chunk = objectMapper.readTree(data);

                                    if (chunk.has("error")) {
                                        String errorMsg = chunk.path("error").path("message").asText("未知错误");
                                        log.error("[{}] API返回错误: {}", requestId, errorMsg);
                                        callback.onError("AI服务错误: " + errorMsg);
                                        return;
                                    }

                                    JsonNode choices = chunk.path("choices");
                                    if (choices == null || choices.isEmpty() || !choices.isArray()) {
                                        return;
                                    }

                                    JsonNode choice0 = choices.get(0);
                                    if (choice0 == null) {
                                        return;
                                    }

                                    JsonNode delta = choice0.path("delta");
                                    if (delta == null) {
                                        JsonNode msgNode = choice0.path("message");
                                        if (msgNode != null && msgNode.has("content")) {
                                            String content = msgNode.path("content").asText("");
                                            if (!content.isEmpty()) {
                                                fullReply.append(content);
                                                callback.onToken(content);
                                            }
                                        }
                                        return;
                                    }

                                    String content = delta.path("content").asText("");
                                    if (!content.isEmpty()) {
                                        fullReply.append(content);
                                        callback.onToken(content);
                                    }
                                } catch (Exception e) {
                                    log.error("[{}] 解析SSE数据块失败: {}", requestId, e.getMessage());
                                }
                            },
                            error -> {
                                long elapsed = System.currentTimeMillis() - startTime;
                                log.error("[{}] AI流式服务异常, 耗时={}ms", requestId, elapsed, error);
                                callback.onError("AI服务暂时不可用，请稍后重试");
                            },
                            () -> {
                                long elapsed = System.currentTimeMillis() - startTime;
                                log.info("[{}] AI流式服务完成, 耗时={}ms, 回复长度={}",
                                        requestId, elapsed, fullReply.length());
                                callback.onComplete(fullReply.toString());
                            }
                    );

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[{}] AI流式服务调用异常, 耗时={}ms", requestId, elapsed, e);
            callback.onError("AI服务暂时不可用，请稍后重试");
            return () -> {};
        }
    }

    private List<Map<String, String>> buildMessages(String message, List<ChatRequest.Message> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        if (history != null) {
            for (ChatRequest.Message msg : history) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        messages.add(Map.of("role", "user", "content", message));
        return messages;
    }

    private List<Map<String, String>> buildMessagesFromJson(String message, JsonNode history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        if (history != null && history.isArray()) {
            for (JsonNode msg : history) {
                String role = msg.path("role").asText("");
                String content = msg.path("content").asText("");
                if (!role.isEmpty() && !content.isEmpty()) {
                    messages.add(Map.of("role", role, "content", content));
                }
            }
        }

        messages.add(Map.of("role", "user", "content", message));
        return messages;
    }
}
