package com.dongmedicine.config;

import com.dongmedicine.common.util.XssUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class XssFilter implements Filter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();
        
        if (contentType != null && contentType.contains("application/json")) {
            chain.doFilter(new XssJsonRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
        }
    }

    private static class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return sanitizeValue(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;

            String[] sanitizedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitizedValues[i] = sanitizeValue(values[i]);
            }
            return sanitizedValues;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> originalMap = super.getParameterMap();
            Map<String, String[]> sanitizedMap = new HashMap<>();

            for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
                String[] values = entry.getValue();
                String[] sanitizedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    sanitizedValues[i] = sanitizeValue(values[i]);
                }
                sanitizedMap.put(entry.getKey(), sanitizedValues);
            }
            return sanitizedMap;
        }

        @Override
        public String getHeader(String name) {
            return sanitizeValue(super.getHeader(name));
        }

        String sanitizeValue(String value) {
            if (value == null || value.isEmpty()) return value;

            if (XssUtils.containsXss(value)) {
                log.warn("检测到潜在的XSS攻击: {}", XssUtils.sanitizeForLog(value));
                return XssUtils.sanitize(value);
            }
            return value;
        }
    }

    private static class XssJsonRequestWrapper extends XssHttpServletRequestWrapper {

        private final byte[] cachedBody;

        public XssJsonRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            String body = readBody(request);
            String sanitizedBody = sanitizeJsonBody(body);
            this.cachedBody = sanitizedBody.getBytes(StandardCharsets.UTF_8);
        }

        private String readBody(HttpServletRequest request) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            return stringBuilder.toString();
        }

        private String sanitizeJsonBody(String json) {
            if (!StringUtils.hasText(json)) {
                return json;
            }
            
            try {
                JsonNode rootNode = OBJECT_MAPPER.readTree(json);
                JsonNode sanitizedNode = sanitizeJsonNode(rootNode);
                return OBJECT_MAPPER.writeValueAsString(sanitizedNode);
            } catch (Exception e) {
                log.debug("JSON解析失败，返回原始内容: {}", e.getMessage());
                return json;
            }
        }

        private JsonNode sanitizeJsonNode(JsonNode node) {
            if (node.isTextual()) {
                String text = node.asText();
                if (XssUtils.containsXss(text)) {
                    log.warn("检测到JSON中的XSS攻击: {}", XssUtils.sanitizeForLog(text));
                    return TextNode.valueOf(XssUtils.sanitize(text));
                }
                return node;
            } else if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                ObjectNode newNode = OBJECT_MAPPER.createObjectNode();
                objectNode.fields().forEachRemaining(entry -> {
                    newNode.set(entry.getKey(), sanitizeJsonNode(entry.getValue()));
                });
                return newNode;
            } else if (node.isArray()) {
                ArrayNode arrayNode = (ArrayNode) node;
                ArrayNode newArrayNode = OBJECT_MAPPER.createArrayNode();
                for (JsonNode element : arrayNode) {
                    newArrayNode.add(sanitizeJsonNode(element));
                }
                return newArrayNode;
            }
            return node;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException("ReadListener not supported");
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return cachedBody.length;
        }

        @Override
        public long getContentLengthLong() {
            return cachedBody.length;
        }
    }
}
