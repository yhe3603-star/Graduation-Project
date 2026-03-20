package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.User;
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

    @PostMapping("/login")
    @RateLimit(value = 5, key = "user_login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        try {
            String token = service.login(dto.getUsername(), dto.getPassword());
            User user = service.getUserByUsername(dto.getUsername());
            return R.ok(Map.of("token", token, "id", user.getId(), "username", user.getUsername(), "role", user.getRole()));
        } catch (Exception e) {
            return R.error("用户名或密码错误");
        }
    }

    @PostMapping("/register")
    @RateLimit(value = 3, key = "user_register")
    public R<String> register(@Valid @RequestBody LoginDTO dto) {
        try {
            service.register(dto.getUsername(), dto.getPassword());
            return R.ok("注册成功");
        } catch (Exception e) {
            return R.error(e.getMessage().contains("已存在") ? "用户名已存在" : "注册失败，请检查输入");
        }
    }

    @GetMapping("/me")
    public R<User> me() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.error("未登录");
        return R.ok(service.getUserInfo(userId));
    }

    @PostMapping("/change-password")
    public R<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.error("未登录");
        try {
            service.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
            String token = request.getHeader("Authorization");
            if (token != null) {
                tokenBlacklistService.addToBlacklist(token);
            }
            return R.ok("密码修改成功，请重新登录");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
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
            "role", role != null ? role : "user"
        ));
    }
}
