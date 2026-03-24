package com.dongmedicine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AppProperties appProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AppProperties appProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.appProperties = appProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**", "/videos/**", "/documents/**", "/public/**").permitAll()
                        .requestMatchers("/images/**", "/videos/**", "/documents/**", "/public/**").permitAll()
                        .requestMatchers("/api/user/login", "/api/user/register", "/api/user/validate", "/api/user/refresh-token").permitAll()
                        .requestMatchers("/api/user/change-password", "/api/user/logout", "/api/user/me").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/plants/**", "/api/knowledge/**", "/api/inheritors/**",
                                "/api/qa/**", "/api/resources/list", "/api/resources/hot",
                                "/api/comments/**", "/api/leaderboard/**", "/api/documents/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/plants/random").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/plants/*/view", "/api/knowledge/*/view", "/api/inheritors/*/view",
                                "/api/qa/*/view", "/api/resources/*/view", "/api/resources/*/download").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/comments", "/api/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/favorites/**", "/api/resources/download/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/favorites/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/upload/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/upload/**").hasRole("ADMIN")
                        .requestMatchers("/api/quiz/add", "/api/quiz/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/quiz/**").hasRole("ADMIN")
                        .requestMatchers("/api/quiz/questions", "/api/quiz/submit", "/api/quiz/list", "/api/plant-game/submit", "/api/chat").permitAll()
                        .requestMatchers("/api/chat/stats").hasRole("ADMIN")
                        .requestMatchers("/api/feedback/stats").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/feedback").permitAll()
                        .requestMatchers("/api/feedback/my").authenticated()
                        .requestMatchers("/api/quiz/records", "/api/plant-game/records").authenticated()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowedOrigins = appProperties.getSecurity().getCorsAllowedOrigins();
        
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            allowedOrigins = List.of("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173");
        }
        
        boolean wildcardOrigin = allowedOrigins.stream().anyMatch(origin ->
                "*".equals(origin) || "http://*".equals(origin) || "https://*".equals(origin));
        
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(!wildcardOrigin);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
