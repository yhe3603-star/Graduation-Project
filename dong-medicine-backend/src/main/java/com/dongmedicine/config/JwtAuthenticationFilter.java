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
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private static final List<String> SKIP_TOKEN_VALIDATION_PATHS = Arrays.asList(
            "/api/user/refresh-token",
            "/api/user/login",
            "/api/user/register"
    );

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

        String requestURI = request.getRequestURI();
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        
        boolean isRefreshTokenRequest = "/api/user/refresh-token".equals(requestURI);

        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                LOGGER.debug("Token is blacklisted");
                if (!isRefreshTokenRequest) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"Token已失效，请重新登录\"}");
                    return;
                }
            }

            JwtUtil.TokenInfo tokenInfo = jwtUtil.parseToken(token);
            
            if (tokenInfo == null || tokenInfo.getClaims() == null) {
                LOGGER.debug("Token parse failed");
                if (!isRefreshTokenRequest) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"Token无效，请重新登录\"}");
                    return;
                }
                filterChain.doFilter(request, response);
                return;
            }
            
            if (isTokenExpired(tokenInfo)) {
                LOGGER.debug("Token is expired");
                if (!isRefreshTokenRequest) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"Token已过期，请重新登录\"}");
                    return;
                }
                filterChain.doFilter(request, response);
                return;
            }
            
            String username = tokenInfo.getUsername();
            Integer userId = tokenInfo.getUserId();
            String role = tokenInfo.getRole();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (userId == null) {
                    SecurityContextHolder.clearContext();
                    if (!isRefreshTokenRequest) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"msg\":\"Token无效\"}");
                        return;
                    }
                    filterChain.doFilter(request, response);
                    return;
                }
                User user = userService.getUserInfo(userId);
                
                if (user == null) {
                    LOGGER.debug("User not found: {}", userId);
                    SecurityContextHolder.clearContext();
                    if (!isRefreshTokenRequest) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"msg\":\"用户不存在\"}");
                        return;
                    }
                    filterChain.doFilter(request, response);
                    return;
                }
                
                if (user.isBanned()) {
                    LOGGER.debug("User is banned: {}", userId);
                    SecurityContextHolder.clearContext();
                    if (!isRefreshTokenRequest) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":403,\"msg\":\"账号已被封禁\"}");
                        return;
                    }
                    filterChain.doFilter(request, response);
                    return;
                }
                
                String currentRole = user.getRole();
                if (!RoleConstants.normalize(currentRole).equals(RoleConstants.normalize(role))) {
                    LOGGER.debug("User role changed, token invalid: {}", userId);
                    SecurityContextHolder.clearContext();
                    if (!isRefreshTokenRequest) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"msg\":\"用户角色已变更，请重新登录\"}");
                        return;
                    }
                    filterChain.doFilter(request, response);
                    return;
                }

                CustomUserDetails userDetails = new CustomUserDetails(username, userId, role);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            LOGGER.debug("JWT认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            if (!isRefreshTokenRequest) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    response.getWriter().write("{\"code\":401,\"msg\":\"认证失败，请重新登录\"}");
                } catch (IOException ignored) {
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenExpired(JwtUtil.TokenInfo tokenInfo) {
        return tokenInfo.getExpiration() != null && tokenInfo.getExpiration().before(new java.util.Date());
    }
}
