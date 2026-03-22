package com.dongmedicine.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
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
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        httpResponse.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                        httpResponse.setContentType("application/json;charset=UTF-8");
                        httpResponse.getWriter().write("{\"code\":413,\"msg\":\"请求体大小超出限制\",\"data\":null}");
                        return;
                    }
                }
            }
        }
        
        chain.doFilter(request, response);
    }
}
