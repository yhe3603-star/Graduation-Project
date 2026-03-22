package com.dongmedicine.config;

import com.dongmedicine.common.constant.RoleConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    @Value("${app.security.jwt-refresh-threshold-minutes:30}")
    private long refreshThresholdMinutes;

    @Autowired
    private AppProperties appProperties;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(appProperties.getSecurity().getJwtSecret().getBytes());
    }

    private long getExpirationTime() {
        return appProperties.getSecurity().getJwtExpiration();
    }

    private long getRefreshThresholdMs() {
        return TimeUnit.MINUTES.toMillis(refreshThresholdMinutes);
    }

    public String generateToken(String username, Integer userId, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + getExpirationTime()))
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String refreshToken(String oldToken) {
        TokenInfo tokenInfo = parseToken(oldToken);
        if (tokenInfo == null || tokenInfo.getClaims() == null) {
            return null;
        }
        return generateToken(tokenInfo.getUsername(), tokenInfo.getUserId(), tokenInfo.getRole());
    }

    public boolean shouldRefresh(String token) {
        TokenInfo tokenInfo = parseToken(token);
        if (tokenInfo == null || tokenInfo.getExpiration() == null) {
            return false;
        }
        long remainingTime = tokenInfo.getExpiration().getTime() - System.currentTimeMillis();
        return remainingTime > 0 && remainingTime < getRefreshThresholdMs();
    }

    public TokenInfo parseToken(String token) {
        if (token == null) {
            return null;
        }
        String cleanToken = token.replace("Bearer ", "");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();
            
            Object userIdObj = claims.get("userId");
            Integer userId = userIdObj != null ? ((Number) userIdObj).intValue() : null;
            
            Object roleObj = claims.get("role");
            String role = roleObj != null ? roleObj.toString() : RoleConstants.ROLE_USER;
            
            return new TokenInfo(
                    claims.getSubject(),
                    userId,
                    role,
                    claims.getExpiration(),
                    claims
            );
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        TokenInfo tokenInfo = parseToken(token);
        return tokenInfo != null && tokenInfo.getClaims() != null;
    }

    public boolean isTokenExpired(String token) {
        TokenInfo tokenInfo = parseToken(token);
        if (tokenInfo == null || tokenInfo.getExpiration() == null) {
            return true;
        }
        return tokenInfo.getExpiration().before(new Date());
    }

    public static class TokenInfo {
        private final String username;
        private final Integer userId;
        private final String role;
        private final Date expiration;
        private final Claims claims;

        public TokenInfo(String username, Integer userId, String role, Date expiration, Claims claims) {
            this.username = username;
            this.userId = userId;
            this.role = role;
            this.expiration = expiration;
            this.claims = claims;
        }

        public String getUsername() {
            return username;
        }

        public Integer getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }

        public Date getExpiration() {
            return expiration;
        }

        public Claims getClaims() {
            return claims;
        }
    }
}
