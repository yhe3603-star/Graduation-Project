package com.dongmedicine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongmedicine.common.R;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.dto.CaptchaDTO;
import com.dongmedicine.service.CaptchaService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "验证码", description = "用户验证码获取")
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 生成验证码
     * 返回验证码key和Base64编码的图片
     * 
     * @return CaptchaDTO 包含captchaKey和captchaImage
     */
    @Operation(summary = "获取验证码")
    @GetMapping
    @RateLimit(value = 30, key = "captcha")
    public R<CaptchaDTO> getCaptcha() {
        CaptchaService.CaptchaResult result = captchaService.generateCaptcha();
        CaptchaDTO dto = new CaptchaDTO(result.getCaptchaKey(), result.getCaptchaImage());
        return R.ok(dto);
    }
}
