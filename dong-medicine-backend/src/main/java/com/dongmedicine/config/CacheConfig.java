package com.dongmedicine.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${app.cache.max-size:1000}")
    private int maxCacheSize;

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
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        CacheManager caffeineManager = createCaffeineCacheManager();

        try {
            connectionFactory.getConnection().ping();
            log.info("Redis available — Caffeine(L1) + Redis(L2) two-level cache active");
            CacheManager redisManager = createRedisCacheManager(connectionFactory);
            return new TwoLevelCacheManager(caffeineManager, redisManager);
        } catch (Exception e) {
            log.warn("Redis unavailable — Caffeine-only cache active (no persistence across restarts)");
            return caffeineManager;
        }
    }

    private CaffeineCacheManager createCaffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxCacheSize)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }

    private CacheManager createRedisCacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(createObjectMapper(), Object.class);
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("plants", defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put("knowledges", defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put("inheritors", defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put("resources", defaultConfig.entryTtl(Duration.ofHours(4)));
        configs.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        configs.put("quizQuestions", defaultConfig.entryTtl(Duration.ofHours(12)));
        configs.put("searchResults", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configs.put("hotData", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }

    /**
     * Two-level cache: Caffeine (L1, fast in-memory) → Redis (L2, distributed/persistent).
     * Reads check Caffeine first, then Redis, then DB.
     * Writes go to both levels to keep them in sync.
     */
    private static class TwoLevelCacheManager implements CacheManager {

        private final CacheManager l1;
        private final CacheManager l2;

        TwoLevelCacheManager(CacheManager l1, CacheManager l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        @Override
        public org.springframework.cache.Cache getCache(String name) {
            org.springframework.cache.Cache c1 = l1.getCache(name);
            org.springframework.cache.Cache c2 = l2.getCache(name);
            if (c1 == null && c2 == null) return null;
            return new TwoLevelCache(
                c1 != null ? c1 : new NoOpCache(name),
                c2 != null ? c2 : new NoOpCache(name)
            );
        }

        @Override
        public java.util.Collection<String> getCacheNames() {
            java.util.Set<String> names = new java.util.LinkedHashSet<>();
            names.addAll(l1.getCacheNames());
            names.addAll(l2.getCacheNames());
            return names;
        }
    }

    private static class TwoLevelCache implements org.springframework.cache.Cache {
        private final org.springframework.cache.Cache l1;
        private final org.springframework.cache.Cache l2;

        TwoLevelCache(org.springframework.cache.Cache l1, org.springframework.cache.Cache l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        @Override
        public String getName() { return l1.getName(); }

        @Override
        public Object getNativeCache() { return l1.getNativeCache(); }

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper v = l1.get(key);
            if (v != null) return v;
            v = l2.get(key);
            if (v != null) l1.put(key, v.get());
            return v;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            T v = l1.get(key, type);
            if (v != null) return v;
            v = l2.get(key, type);
            if (v != null) l1.put(key, v);
            return v;
        }

        @Override
        public void put(Object key, Object value) {
            l1.put(key, value);
            l2.put(key, value);
        }

        @Override
        public void evict(Object key) {
            l1.evict(key);
            l2.evict(key);
        }

        @Override
        public void clear() {
            l1.clear();
            l2.clear();
        }
    }

    private static class NoOpCache implements org.springframework.cache.Cache {
        private final String name;

        NoOpCache(String name) { this.name = name; }

        @Override
        public String getName() { return name; }

        @Override
        public Object getNativeCache() { return null; }

        @Override
        public ValueWrapper get(Object key) { return null; }

        @Override
        public <T> T get(Object key, Class<T> type) { return null; }

        @Override
        public void put(Object key, Object value) {}

        @Override
        public void evict(Object key) {}

        @Override
        public void clear() {}
    }
}
