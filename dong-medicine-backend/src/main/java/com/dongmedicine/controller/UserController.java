package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.User;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.dto.LoginDTO;
import com.dongmedicine.dto.RegisterDTO;
import com.dongmedicine.dto.ChangePasswordDTO;
import com.dongmedicine.service.UserService;
import com.dongmedicine.service.CaptchaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "用户管理", description = "用户注册、登录、个人信息管理")
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
        
        return R.ok(service.login(dto.getUsername(), dto.getPassword()));
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
        return R.ok(service.getUserInfo(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/change-password")
    @SaCheckLogin
    public R<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        captchaService.validateCaptchaOrThrow(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        service.changePassword(SecurityUtils.getCurrentUserId(), dto.getCurrentPassword(), dto.getNewPassword());
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
        try {
            Integer userId = SecurityUtils.getCurrentUserId();
            return R.ok(Map.of("valid", true, "id", userId));
        } catch (BusinessException e) {
            return R.ok(Map.of("valid", false));
        }
    }

    @PostMapping("/refresh-token")
    public R<Map<String, Object>> refreshToken() {
        if (!StpUtil.isLogin()) {
            throw BusinessException.badRequest("Token无法刷新");
        }

        StpUtil.renewTimeout(StpUtil.getTokenTimeout());

        return R.ok(Map.of(
            "token", StpUtil.getTokenValue(),
            "id", SecurityUtils.getCurrentUserId()
        ));
    }
}
