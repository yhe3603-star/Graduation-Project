package com.dongmedicine.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            "/api/user/login",
            "/api/user/register",
            "/api/user/validate",
            "/api/user/refresh-token",
            "/api/captcha",
            "/api/plants/**",
            "/api/knowledge/**",
            "/api/inheritors/**",
            "/api/qa/**",
            "/api/resources/list",
            "/api/resources/hot",
            "/api/resources/search",
            "/api/comments/list/**",
            "/api/leaderboard/**",
            "/api/stats/**",
            "/api/quiz/questions",
            "/api/quiz/submit",
            "/api/quiz/list",
            "/api/plant-game/submit",
            "/api/chat",
            "/api/feedback/stats",
            "/api/documents/**",
            "/public/**",
            "/images/**",
            "/videos/**",
            "/documents/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/actuator/health"
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
