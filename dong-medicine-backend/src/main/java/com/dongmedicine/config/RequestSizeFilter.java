package com.dongmedicine.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestSizeFilter implements Filter {

    @Value("${app.request.max-body-size:10485760}")
    private long maxBodySize;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            String method = httpRequest.getMethod();
            
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                String contentType = httpRequest.getContentType();
                
                if (contentType != null && contentType.contains("application/json")) {
                    int contentLength = httpRequest.getContentLength();
                    
                    if (contentLength > maxBodySize) {
                        sendErrorResponse((HttpServletResponse) response, "请求体大小超出限制，最大允许 " + formatSize(maxBodySize));
                        return;
                    }
                    
                    if (contentLength == -1) {
                        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest, (int) maxBodySize + 1);
                        chain.doFilter(wrappedRequest, response);
                        return;
                    }
                }
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":413,\"msg\":\"" + message + "\",\"data\":null}");
    }
    
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return (size / (1024 * 1024)) + " MB";
    }
}
