package com.dongmedicine.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 写操作HTTP方法列表，这些方法需要登录认证
     */
    private static final List<String> WRITE_METHODS = List.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 获取当前请求对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String method = request.getMethod().toUpperCase();
                // 所有写操作（POST/PUT/DELETE/PATCH）必须登录
                if (WRITE_METHODS.contains(method)) {
                    StpUtil.checkLogin();
                }
            }
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            // ========== 认证相关（无需登录） ==========
            "/api/auth/**",
            "/api/user/login",
            "/api/user/register",
            "/api/user/validate",
            "/api/user/refresh-token",
            "/api/captcha/**",

            // ========== 药用植物-只读接口 ==========
            "/api/plants/list",
            "/api/plants/search",
            "/api/plants/random",
            "/api/plants/{id}",
            "/api/plants/{id}/similar",
            "/api/plants/{id}/view",
            "/api/plants/batch",

            // ========== 知识库-只读接口 ==========
            "/api/knowledge/list",
            "/api/knowledge/search",
            "/api/knowledge/{id}",
            "/api/knowledge/{id}/view",

            // ========== 传承人-只读接口 ==========
            "/api/inheritors/list",
            "/api/inheritors/search",
            "/api/inheritors/{id}",
            "/api/inheritors/{id}/view",

            // ========== 问答-只读接口 ==========
            "/api/qa/list",
            "/api/qa/search",
            "/api/qa/{id}/view",

            // ========== 测验-只读接口 ==========
            "/api/quiz/questions",
            "/api/quiz/list",
            "/api/quiz/records",

            // ========== 学习资源-只读接口 ==========
            "/api/resources/list",
            "/api/resources/search",
            "/api/resources/hot",
            "/api/resources/categories",
            "/api/resources/types",
            "/api/resources/{id}",
            "/api/resources/download/{id}",
            "/api/resources/{id}/view",

            // ========== 评论-只读接口 ==========
            "/api/comments/list/**",

            // ========== 排行榜-只读接口 ==========
            "/api/leaderboard/**",

            // ========== 反馈-只读接口 ==========
            "/api/feedback/stats",
            "/api/feedback",

            // ========== 文档与元数据-只读接口 ==========
            "/api/documents/**",
            "/api/metadata/**",
            "/api/stats/**",

            // ========== 静态资源 ==========
            "/public/**",
            "/images/**",
            "/videos/**",

            // ========== 开发工具与监控 ==========
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/doc.html",
            "/webjars/**",
            "/favicon.ico",
            "/error"
        );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "userid", "Userid", "UserId")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
