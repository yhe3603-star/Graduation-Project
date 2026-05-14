package com.dongmedicine.service.impl;

import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.service.CaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("验证码服务测试")
class CaptchaServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("生成验证码 - 返回有效结果")
    void testGenerateCaptcha() {
        CaptchaService.CaptchaResult result = captchaService.generateCaptcha();

        assertNotNull(result);
        assertNotNull(result.getCaptchaKey());
        assertFalse(result.getCaptchaKey().isEmpty());
        assertNotNull(result.getCaptchaImage());
        assertTrue(result.getCaptchaImage().startsWith("data:image/png;base64,"));
        verify(valueOps).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("验证验证码 - 正确验证码通过")
    void testValidateCaptcha_Correct() {
        when(valueOps.get("captcha:abc123")).thenReturn("ABCDE");
        assertTrue(captchaService.validateCaptcha("abc123", "ABCDE"));
        verify(redisTemplate).delete("captcha:abc123");
    }

    @Test
    @DisplayName("验证验证码 - 大小写不敏感")
    void testValidateCaptcha_CaseInsensitive() {
        when(valueOps.get("captcha:abc123")).thenReturn("ABCDE");
        assertTrue(captchaService.validateCaptcha("abc123", "abcde"));
    }

    @Test
    @DisplayName("验证验证码 - 错误验证码失败")
    void testValidateCaptcha_Incorrect() {
        when(valueOps.get("captcha:abc123")).thenReturn("ABCDE");
        assertFalse(captchaService.validateCaptcha("abc123", "WRONG"));
    }

    @Test
    @DisplayName("验证验证码 - 过期验证码失败")
    void testValidateCaptcha_Expired() {
        when(valueOps.get("captcha:abc123")).thenReturn(null);
        assertFalse(captchaService.validateCaptcha("abc123", "ABCDE"));
    }

    @Test
    @DisplayName("验证验证码 - null参数返回false")
    void testValidateCaptcha_NullParams() {
        assertFalse(captchaService.validateCaptcha(null, "ABCDE"));
        assertFalse(captchaService.validateCaptcha("abc123", null));
    }

    @Test
    @DisplayName("验证验证码OrThrow - 空key抛异常")
    void testValidateCaptchaOrThrow_BlankKey() {
        assertThrows(BusinessException.class, () ->
                captchaService.validateCaptchaOrThrow("", "ABCDE"));
    }

    @Test
    @DisplayName("验证验证码OrThrow - 错误验证码抛异常")
    void testValidateCaptchaOrThrow_Incorrect() {
        when(valueOps.get("captcha:abc123")).thenReturn("ABCDE");
        assertThrows(BusinessException.class, () ->
                captchaService.validateCaptchaOrThrow("abc123", "WRONG"));
    }
}
