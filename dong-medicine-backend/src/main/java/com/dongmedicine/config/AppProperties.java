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
        private Long jwtExpiration = 86400000L;
        private List<String> corsAllowedOrigins = new ArrayList<>();

        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
        public Long getJwtExpiration() { return jwtExpiration; }
        public void setJwtExpiration(Long jwtExpiration) { this.jwtExpiration = jwtExpiration == null ? 86400000L : jwtExpiration; }
        public List<String> getCorsAllowedOrigins() { return corsAllowedOrigins; }
        public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
            this.corsAllowedOrigins = corsAllowedOrigins == null ? new ArrayList<>() : corsAllowedOrigins;
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
