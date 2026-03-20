package com.dongmedicine.config.health;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CacheHealthIndicator implements HealthIndicator {

    private final CacheManager cacheManager;

    @Override
    public Health health() {
        Map<String, Object> cacheDetails = new HashMap<>();
        boolean allHealthy = true;

        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                Cache<Object, Object> nativeCache = (Cache<Object, Object>) caffeineCache.getNativeCache();
                CacheStats stats = nativeCache.stats();
                
                Map<String, Object> details = new HashMap<>();
                details.put("size", nativeCache.estimatedSize());
                details.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
                details.put("hitCount", stats.hitCount());
                details.put("missCount", stats.missCount());
                details.put("evictionCount", stats.evictionCount());
                
                cacheDetails.put(cacheName, details);
                
                if (stats.hitRate() < 0.1 && stats.requestCount() > 100) {
                    allHealthy = false;
                }
            }
        }

        if (allHealthy) {
            return Health.up()
                    .withDetails(cacheDetails)
                    .build();
        } else {
            return Health.status("WARNING")
                    .withDetail("message", "Some caches have low hit rates")
                    .withDetails(cacheDetails)
                    .build();
        }
    }
}
