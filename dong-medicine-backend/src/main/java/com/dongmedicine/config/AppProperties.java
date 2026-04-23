package com.dongmedicine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cors cors = new Cors();
    private Cache cache = new Cache();
    private Logging logging = new Logging();

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : allowedOrigins;
        }
    }

    public static class Cache {
        private boolean enabled = true;
        private Long defaultExpireTime = 3600000L;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Long getDefaultExpireTime() { return defaultExpireTime; }
        public void setDefaultExpireTime(Long defaultExpireTime) { this.defaultExpireTime = defaultExpireTime == null ? 3600000L : defaultExpireTime; }
    }

    public static class Logging {
        private boolean performanceMonitoring = true;
        private boolean requestLogging = true;
        private boolean databaseMonitoring = true;

        public boolean isPerformanceMonitoring() { return performanceMonitoring; }
        public void setPerformanceMonitoring(boolean performanceMonitoring) { this.performanceMonitoring = performanceMonitoring; }
        public boolean isRequestLogging() { return requestLogging; }
        public void setRequestLogging(boolean requestLogging) { this.requestLogging = requestLogging; }
        public boolean isDatabaseMonitoring() { return databaseMonitoring; }
        public void setDatabaseMonitoring(boolean databaseMonitoring) { this.databaseMonitoring = databaseMonitoring; }
    }

    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }
    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }
}
