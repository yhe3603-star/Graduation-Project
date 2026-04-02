# 配置类目录 (config)

本目录存放Spring Boot配置类，用于配置应用的各种功能。

## 📖 什么是配置类？

配置类是使用`@Configuration`注解标记的类，用于定义Spring Bean和配置应用行为。常见的配置包括：
- 安全配置（Spring Security）
- 跨域配置（CORS）
- 数据源配置
- 缓存配置
- 自定义Bean配置

## 📁 文件列表

| 文件名 | 功能说明 |
|--------|----------|
| `SecurityConfig.java` | Spring Security安全配置 |
| `JwtUtil.java` | JWT工具类 |
| `CorsConfig.java` | 跨域配置 |
| `RedisConfig.java` | Redis缓存配置 |
| `MyBatisPlusConfig.java` | MyBatis Plus配置 |
| `WebConfig.java` | Web MVC配置 |

## 📦 详细说明

### 1. SecurityConfig.java - 安全配置

**功能:** 配置Spring Security安全框架

**主要配置:**
- 认证规则：哪些URL需要认证
- 授权规则：不同角色的权限
- 密码编码器：BCrypt加密
- JWT过滤器：Token验证

**配置示例:**
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2. JwtUtil.java - JWT工具类

**功能:** 生成和解析JWT Token

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `generateToken(User user)` | 生成Token |
| `parseToken(String token)` | 解析Token |
| `validateToken(String token)` | 验证Token |
| `getUserId(String token)` | 获取用户ID |
| `getUsername(String token)` | 获取用户名 |

**使用示例:**
```java
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("userId", user.getId())
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
}
```

### 3. CorsConfig.java - 跨域配置

**功能:** 配置跨域资源共享（CORS）

**配置示例:**
```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

### 4. MyBatisPlusConfig.java - MyBatis Plus配置

**功能:** 配置MyBatis Plus分页插件等

**配置示例:**
```java
@Configuration
public class MyBatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
```

## 🎯 配置类规范

### 基本结构
```java
@Configuration
@RequiredArgsConstructor
public class ExampleConfig {
    
    private final SomeDependency dependency;
    
    @Bean
    public SomeBean someBean() {
        return new SomeBean();
    }
}
```

### 常用注解

| 注解 | 说明 |
|------|------|
| `@Configuration` | 标记为配置类 |
| `@Bean` | 定义Spring Bean |
| `@Value` | 注入配置属性 |
| `@ConfigurationProperties` | 绑定配置属性 |
| `@EnableWebSecurity` | 启用Web安全 |
| `@EnableCaching` | 启用缓存 |

### 最佳实践
1. **单一职责**: 每个配置类只负责一个功能
2. **命名规范**: 以`Config`结尾
3. **使用构造注入**: 使用`@RequiredArgsConstructor`
4. **配置外部化**: 使用`@Value`或`@ConfigurationProperties`

## 📚 扩展阅读

- [Spring Boot 配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Security 配置](https://docs.spring.io/spring-security/reference/servlet/configuration.html)
- [JWT 入门](https://jwt.io/introduction)
