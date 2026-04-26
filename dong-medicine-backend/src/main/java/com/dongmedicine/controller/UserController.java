package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.User;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.dto.LoginDTO;
import com.dongmedicine.dto.RegisterDTO;
import com.dongmedicine.dto.ChangePasswordDTO;
import com.dongmedicine.service.UserService;
import com.dongmedicine.service.CaptchaService;
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
    private final CaptchaService captchaService;

    @PostMapping("/login")
    @RateLimit(value = 5, key = "user_login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        captchaService.validateCaptchaOrThrow(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        String token = service.login(dto.getUsername(), dto.getPassword());
        User user = service.getUserByUsername(dto.getUsername());
        return R.ok(Map.of("token", token, "id", user.getId(), "username", user.getUsername(), "role", user.getRole()));
    }

    @PostMapping("/register")
    @RateLimit(value = 3, key = "user_register")
    public R<String> register(@Valid @RequestBody RegisterDTO dto) {
        captchaService.validateCaptchaOrThrow(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw BusinessException.badRequest("两次输入的密码不一致");
        }
        
        service.register(dto.getUsername(), dto.getPassword());
        return R.ok("注册成功");
    }

    @GetMapping("/me")
    @SaCheckLogin
    public R<User> me() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return R.ok(service.getUserInfo(userId));
    }

    @PostMapping("/change-password")
    @SaCheckLogin
    public R<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        
        captchaService.validateCaptchaOrThrow(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        service.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
        StpUtil.logout();
        return R.ok("密码修改成功，请重新登录");
    }

    @PostMapping("/logout")
    public R<String> logout() {
        StpUtil.logout();
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
    public R<Map<String, Object>> refreshToken() {
        if (!StpUtil.isLogin()) {
            throw BusinessException.badRequest("Token无法刷新");
        }
        Integer userId = SecurityUtils.getCurrentUserId();
        String username = (String) StpUtil.getSession().get("username");
        String role = (String) StpUtil.getSession().get("role");

        StpUtil.renewTimeout(StpUtil.getTokenTimeout());

        return R.ok(Map.of(
            "token", StpUtil.getTokenValue(),
            "id", userId,
            "username", username != null ? username : "",
            "role", role != null ? role : RoleConstants.ROLE_USER
        ));
    }
}
