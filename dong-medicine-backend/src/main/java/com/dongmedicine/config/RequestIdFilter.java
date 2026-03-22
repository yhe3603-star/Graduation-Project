package com.dongmedicine.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final int REQUEST_ID_LENGTH = 16;
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = extractOrGenerateRequestId(request);
        
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private String extractOrGenerateRequestId(HttpServletRequest request) {
        String existingId = request.getHeader(REQUEST_ID_HEADER);
        
        if (existingId != null && !existingId.isBlank()) {
            if (existingId.length() <= 64 && REQUEST_ID_PATTERN.matcher(existingId).matches()) {
                return existingId;
            }
        }
        
        return generateRequestId();
    }

    private String generateRequestId() {
        byte[] bytes = new byte[REQUEST_ID_LENGTH / 2];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(REQUEST_ID_LENGTH);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || 
               path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs");
    }
}
