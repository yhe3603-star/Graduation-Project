package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.CaptchaDTO;
import com.dongmedicine.service.CaptchaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaptchaController测试")
class CaptchaControllerTest {

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private CaptchaController captchaController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("获取验证码 - 成功")
    void testGetCaptcha_Success() {
        CaptchaService.CaptchaResult captchaResult = new CaptchaService.CaptchaResult("key123", "data:image/png;base64,abc123");
        when(captchaService.generateCaptcha()).thenReturn(captchaResult);

        R<CaptchaDTO> result = captchaController.getCaptcha();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("key123", result.getData().getCaptchaKey());
        assertEquals("data:image/png;base64,abc123", result.getData().getCaptchaImage());
    }
}
