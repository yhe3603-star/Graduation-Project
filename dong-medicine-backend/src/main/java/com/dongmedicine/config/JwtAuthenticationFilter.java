package com.dongmedicine.config;

import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.TokenBlacklistService;
import com.dongmedicine.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                LOGGER.debug("Token is blacklisted");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            JwtUtil.TokenInfo tokenInfo = jwtUtil.parseToken(token);
            
            if (tokenInfo != null && tokenInfo.getClaims() != null && !isTokenExpired(tokenInfo)) {
                String username = tokenInfo.getUsername();
                Integer userId = tokenInfo.getUserId();
                String role = tokenInfo.getRole();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (userId == null) {
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                    User user = userService.getUserInfo(userId);
                    
                    if (user == null) {
                        LOGGER.debug("User not found: {}", userId);
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    String currentRole = user.getRole();
                    if (!RoleConstants.normalize(currentRole).equals(RoleConstants.normalize(role))) {
                        LOGGER.debug("User role changed, token invalid: {}", userId);
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }

                    CustomUserDetails userDetails = new CustomUserDetails(username, userId, role);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("JWT认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenExpired(JwtUtil.TokenInfo tokenInfo) {
        return tokenInfo.getExpiration() != null && tokenInfo.getExpiration().before(new java.util.Date());
    }
}
