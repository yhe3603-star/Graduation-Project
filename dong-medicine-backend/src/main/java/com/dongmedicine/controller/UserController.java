package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.User;
import com.dongmedicine.config.JwtUtil;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.dto.LoginDTO;
import com.dongmedicine.dto.RegisterDTO;
import com.dongmedicine.dto.ChangePasswordDTO;
import com.dongmedicine.service.UserService;
import com.dongmedicine.service.CaptchaService;
import com.dongmedicine.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;

    @PostMapping("/login")
    @RateLimit(value = 5, key = "user_login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        // 验证验证码
        captchaService.validateCaptchaOrThrow(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        String token = service.login(dto.getUsername(), dto.getPassword());
        User user = service.getUserByUsername(dto.getUsername());
        return R.ok(Map.of("token", token, "id", user.getId(), "username", user.getUsername(), "role", user.getRole()));
    }

    @PostMapping("/register")
    @RateLimit(value = 3, key = "user_register")
    public R<String> register(@Valid @RequestBody RegisterDTO dto) {
        // 验证验证码
        captchaService.validateCaptchaOrThrow(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        // 验证两次密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw BusinessException.badRequest("两次输入的密码不一致");
        }
        
        service.register(dto.getUsername(), dto.getPassword());
        return R.ok("注册成功");
    }

    @GetMapping("/me")
    public R<User> me() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return R.ok(service.getUserInfo(userId));
    }

    @PostMapping("/change-password")
    public R<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        
        // 验证验证码
        captchaService.validateCaptchaOrThrow(dto.getCaptchaKey(), dto.getCaptchaCode());
        
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
            throw BusinessException.unauthorized("Token无效或已过期");
        }
        
        return R.ok(Map.of(
            "id", userId,
            "username", username,
            "role", role != null ? role : RoleConstants.ROLE_USER
        ));
    }

    @PostMapping("/refresh-token")
    public R<Map<String, Object>> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw BusinessException.badRequest("Token无效");
        }
        String cleanToken = authHeader.substring(7).trim();
        if (tokenBlacklistService.isBlacklisted(cleanToken)) {
            throw BusinessException.badRequest("Token已失效");
        }
        if (!jwtUtil.canRefresh(cleanToken)) {
            throw BusinessException.badRequest("Token无法刷新");
        }
        String newToken = jwtUtil.refreshToken(cleanToken);
        if (newToken == null) {
            throw BusinessException.badRequest("Token无法刷新");
        }
        tokenBlacklistService.addToBlacklist(cleanToken);

        JwtUtil.TokenInfo newTokenInfo = jwtUtil.parseToken(newToken);

        Map<String, Object> body = new HashMap<>();
        body.put("token", newToken);
        body.put("id", newTokenInfo != null ? newTokenInfo.getUserId() : null);
        body.put("username", newTokenInfo != null ? newTokenInfo.getUsername() : null);
        body.put("role", newTokenInfo != null && newTokenInfo.getRole() != null ? newTokenInfo.getRole() : RoleConstants.ROLE_USER);
        return R.ok(body);
    }
}
