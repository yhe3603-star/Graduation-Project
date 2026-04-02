# 配置模块 (config)

本目录存放 Spring Boot 的配置类，用于配置框架行为。

## 目录

- [什么是配置类？](#什么是配置类)
- [目录结构](#目录结构)
- [核心配置类](#核心配置类)
- [配置文件说明](#配置文件说明)

---

## 什么是配置类？

### 配置类的概念

**配置类**是用来告诉 Spring Boot 如何工作的类。它就像电器的"设置菜单"——通过配置，你可以调整框架的各种行为。

### 为什么需要配置类？

```
┌─────────────────────────────────────────────────────────────────┐
│                     没有配置类                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Spring Boot 使用默认配置：                                      │
│  - 默认端口 8080                                                │
│  - 默认不启用安全验证                                            │
│  - 默认不启用缓存                                                │
│  - 默认不启用跨域                                                │
│                                                                 │
│  → 无法满足项目需求                                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      有配置类                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  通过配置类自定义：                                               │
│  - 自定义安全规则（哪些接口需要登录）                              │
│  - 自定义缓存策略                                                │
│  - 自定义跨域规则                                                │
│  - 自定义JWT验证                                                 │
│                                                                 │
│  → 满足项目需求                                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 配置类的基本结构

```java
@Configuration  // 标记这是一个配置类
public class ExampleConfig {
    
    @Bean  // 创建一个Bean，交给Spring管理
    public SomeService someService() {
        return new SomeServiceImpl();
    }
}
```

---

## 目录结构

```
config/
│
├── health/                            # 健康检查
│   └── HealthCheckConfig.java
│
├── logging/                           # 日志配置
│   └── LoggingConfig.java
│
├── SecurityConfig.java                # Spring Security 安全配置
├── JwtUtil.java                       # JWT 工具类
├── JwtAuthenticationFilter.java       # JWT 认证过滤器
├── CacheConfig.java                   # 缓存配置
├── RateLimitAspect.java               # 请求限流切面
├── CorsConfig.java                    # 跨域配置
├── WebConfig.java                     # Web配置
├── OpenApiConfig.java                 # Swagger API文档配置
├── DeepSeekConfig.java                # DeepSeek AI配置
└── TokenBlacklistConfig.java          # Token黑名单配置
```

---

## 核心配置类

### SecurityConfig - 安全配置

配置 Spring Security，定义哪些接口需要认证。

```java
/**
 * Spring Security 安全配置
 * 定义认证规则和权限控制
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtFilter;
    private final TokenBlacklistService blacklistService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（前后端分离不需要）
            .csrf(csrf -> csrf.disable())
            
            // 配置Session：无状态（使用JWT）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开接口：不需要登录
                .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                .requestMatchers("/api/captcha/**").permitAll()
                .requestMatchers("/api/plants/**").permitAll()
                .requestMatchers("/api/knowledge/**").permitAll()
                .requestMatchers("/api/inheritors/**").permitAll()
                .requestMatchers("/api/resources/**").permitAll()
                .requestMatchers("/api/chat/**").permitAll()
                
                // 管理员接口：需要ADMIN角色
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/upload/**").hasRole("ADMIN")
                
                // 其他接口：需要登录
                .anyRequest().authenticated()
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用BCrypt加密密码
        return new BCryptPasswordEncoder();
    }
}
```

### JwtAuthenticationFilter - JWT认证过滤器

拦截请求，验证JWT Token。

```java
/**
 * JWT认证过滤器
 * 拦截每个请求，验证Token有效性
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService blacklistService;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. 从请求头获取Token
        String token = extractToken(request);
        
        if (token != null) {
            // 2. 检查Token是否在黑名单中
            if (blacklistService.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"msg\":\"Token已失效\"}");
                return;
            }
            
            // 3. 验证Token有效性
            if (jwtUtil.validateToken(token)) {
                // 4. 从Token中获取用户名
                String username = jwtUtil.extractUsername(token);
                
                // 5. 加载用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 6. 检查用户状态
                if (userDetails instanceof CustomUserDetails) {
                    CustomUserDetails customUser = (CustomUserDetails) userDetails;
                    if (customUser.isBanned()) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"code\":403,\"msg\":\"账号已被封禁\"}");
                        return;
                    }
                }
                
                // 7. 设置认证信息
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        
        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

### JwtUtil - JWT工具类

生成和验证JWT Token。

```java
/**
 * JWT工具类
 * 用于生成、解析、验证JWT Token
 */
@Component
public class JwtUtil {
    
    @Value("${app.security.jwt-secret}")
    private String secret;
    
    @Value("${app.security.jwt-expiration}")
    private long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成Token
     */
    public String generateToken(Integer userId, String username, String role) {
        return Jwts.builder()
            .subject(username)
            .claim("userId", userId)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
    }
    
    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration();
        return expiration.before(new Date());
    }
}
```

### CacheConfig - 缓存配置

配置多级缓存（Caffeine + Redis）。

```java
/**
 * 缓存配置
 * 配置Caffeine本地缓存 + Redis分布式缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 1. Caffeine本地缓存配置
        CaffeineCacheManager caffeineManager = new CaffeineCacheManager();
        caffeineManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)           // 最大缓存数量
            .expireAfterWrite(60, TimeUnit.MINUTES)  // 过期时间
            .recordStats());             // 记录统计信息
        
        // 2. Redis缓存配置
        RedisCacheConfiguration redisConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // 3. 组合缓存管理器
        return new CompositeCacheManager(
            caffeineManager,
            RedisCacheManager.builder(factory)
                .cacheDefaults(redisConfig)
                .build()
        );
    }
}
```

### RateLimitAspect - 请求限流

限制接口访问频率，防止恶意请求。

```java
/**
 * 请求限流切面
 * 使用Redis计数器实现限流
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 本地令牌桶（Redis不可用时的降级方案）
    private final Map<String, LocalTokenBucket> localBuckets = new ConcurrentHashMap<>();
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = "rate_limit:" + rateLimit.key() + ":" + getClientId();
        int limit = rateLimit.value();
        
        // 尝试使用Redis限流
        if (!tryAcquireWithRedis(key, limit)) {
            // Redis不可用，使用本地限流
            if (!tryAcquireLocally(key, limit)) {
                throw new BusinessException("请求过于频繁，请稍后再试");
            }
        }
        
        return point.proceed();
    }
    
    private boolean tryAcquireWithRedis(String key, int limit) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }
            return count != null && count <= limit;
        } catch (Exception e) {
            return false;  // Redis不可用
        }
    }
    
    private boolean tryAcquireLocally(String key, int limit) {
        LocalTokenBucket bucket = localBuckets.computeIfAbsent(key, 
            k -> new LocalTokenBucket(limit));
        return bucket.tryAcquire();
    }
}
```

---

## 配置文件说明

### application.yml - 主配置

```yaml
server:
  port: 8080

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

app:
  security:
    jwt-secret: ${JWT_SECRET}
    jwt-expiration: ${JWT_EXPIRATION:86400000}
  cache:
    enabled: true
    max-size: 1000
    expire-minutes: 60
```

### application-dev.yml - 开发环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dong_medicine
    username: root
    password: 123456

  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    com.dongmedicine: DEBUG
```

### application-prod.yml - 生产环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

logging:
  level:
    com.dongmedicine: INFO
```

---

## 最佳实践

### 1. 配置分离

- 开发环境和生产环境使用不同的配置文件
- 敏感信息使用环境变量

### 2. 配置验证

```java
@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfig {
    
    @NotNull
    private String jwtSecret;
    
    @Min(3600000)  // 最少1小时
    private long jwtExpiration = 86400000;
}
```

### 3. 条件配置

```java
@Configuration
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
public class CacheConfig {
    // 只有启用缓存时才加载
}
```

---

**最后更新时间**：2026年4月3日
