package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.entity.User;
import com.dongmedicine.config.JwtUtil;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.dto.LoginDTO;
import com.dongmedicine.dto.ChangePasswordDTO;
import com.dongmedicine.service.UserService;
import com.dongmedicine.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @RateLimit(value = 5, key = "user_login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        String token = service.login(dto.getUsername(), dto.getPassword());
        User user = service.getUserByUsername(dto.getUsername());
        return R.ok(Map.of("token", token, "id", user.getId(), "username", user.getUsername(), "role", user.getRole()));
    }

    @PostMapping("/register")
    @RateLimit(value = 3, key = "user_register")
    public R<String> register(@Valid @RequestBody LoginDTO dto) {
        service.register(dto.getUsername(), dto.getPassword());
        return R.ok("注册成功");
    }

    @GetMapping("/me")
    public R<User> me() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        return R.ok(service.getUserInfo(userId));
    }

    @PostMapping("/change-password")
    public R<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        service.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
        String token = request.getHeader("Authorization");
        if (token != null) {
            tokenBlacklistService.addToBlacklist(token);
        }
        return R.ok("密码修改成功，请重新登录");
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            tokenBlacklistService.addToBlacklist(token);
        }
        return R.ok("退出成功");
    }

    @GetMapping("/validate")
    public R<Map<String, Object>> validate() {
        Integer userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        String role = SecurityUtils.getCurrentUserRole();
        
        if (userId == null || username == null) {
            return R.error("Token无效或已过期");
        }
        
        return R.ok(Map.of(
            "id", userId,
            "username", username,
            "role", role != null ? role : RoleConstants.ROLE_USER
        ));
    }

    @PostMapping("/refresh-token")
    public R<Map<String, Object>> refreshToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return R.error("Token无效");
        }
        
        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseToken(token);
        if (tokenInfo != null && tokenInfo.getClaims() != null && jwtUtil.shouldRefresh(token)) {
            String newToken = jwtUtil.refreshToken(token);
            tokenBlacklistService.addToBlacklist(token);
            
            JwtUtil.TokenInfo newTokenInfo = jwtUtil.parseToken(newToken);
            
            return R.ok(Map.of(
                "token", newToken,
                "id", newTokenInfo != null ? newTokenInfo.getUserId() : null,
                "username", newTokenInfo != null ? newTokenInfo.getUsername() : null,
                "role", newTokenInfo != null && newTokenInfo.getRole() != null ? newTokenInfo.getRole() : RoleConstants.ROLE_USER
            ));
        }
        
        return R.error("Token无法刷新");
    }
}
