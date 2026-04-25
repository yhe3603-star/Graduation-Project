package com.dongmedicine.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${app.cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${app.cache.max-size:1000}")
    private int maxCacheSize;
    
    @Value("${app.cache.expire-minutes:60}")
    private int defaultExpireMinutes;

    private ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        om.registerModule(new JavaTimeModule());
        return om;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(createObjectMapper(), Object.class);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            connectionFactory.getConnection().ping();
            log.info("Redis connection successful, using Redis cache manager");
            return createRedisCacheManager(connectionFactory);
        } catch (Exception e) {
            log.warn("Redis connection failed, falling back to in-memory cache: {}", e.getMessage());
            return createFallbackCacheManager();
        }
    }

    private CacheManager createRedisCacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(createObjectMapper(), Object.class);
        
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put("plants", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("knowledges", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("inheritors", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("resources", defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("quizQuestions", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("searchResults", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("hotData", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        log.info("Redis cache manager initialized with custom TTL configurations");
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    private CacheManager createFallbackCacheManager() {
        log.info("Using Caffeine cache manager as fallback with max size: {}", maxCacheSize);
        
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxCacheSize)
                .expireAfterWrite(defaultExpireMinutes, TimeUnit.MINUTES)
                .expireAfterAccess(defaultExpireMinutes, TimeUnit.MINUTES)
                .recordStats());
        
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "plants", "knowledges", "inheritors", "resources",
            "users", "quizQuestions", "searchResults", "hotData"
        ));
        
        return cacheManager;
    }
}
