package com.dongmedicine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Security security = new Security();
    private Cache cache = new Cache();
    private Logging logging = new Logging();

    public static class Security {
        private String jwtSecret;
        private long jwtExpiration = 86400000L; // 24小时
        private List<String> corsAllowedOrigins = new ArrayList<>();

        // getters and setters
        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
        public long getJwtExpiration() { return jwtExpiration; }
        public void setJwtExpiration(long jwtExpiration) { this.jwtExpiration = jwtExpiration; }
        public List<String> getCorsAllowedOrigins() { return corsAllowedOrigins; }
        public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
            this.corsAllowedOrigins = corsAllowedOrigins == null ? new ArrayList<>() : corsAllowedOrigins;
        }
    }

    public static class Cache {
        private boolean enabled = true;
        private long defaultExpireTime = 3600000L; // 1小时

        // getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public long getDefaultExpireTime() { return defaultExpireTime; }
        public void setDefaultExpireTime(long defaultExpireTime) { this.defaultExpireTime = defaultExpireTime; }
    }

    public static class Logging {
        private boolean performanceMonitoring = true;
        private boolean requestLogging = true;
        private boolean databaseMonitoring = true;

        // getters and setters
        public boolean isPerformanceMonitoring() { return performanceMonitoring; }
        public void setPerformanceMonitoring(boolean performanceMonitoring) { this.performanceMonitoring = performanceMonitoring; }
        public boolean isRequestLogging() { return requestLogging; }
        public void setRequestLogging(boolean requestLogging) { this.requestLogging = requestLogging; }
        public boolean isDatabaseMonitoring() { return databaseMonitoring; }
        public void setDatabaseMonitoring(boolean databaseMonitoring) { this.databaseMonitoring = databaseMonitoring; }
    }

    // 主要的getters and setters
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }
    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }
}
