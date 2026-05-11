package com.dongmedicine.config.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SensitiveDataConverter 测试")
class SensitiveDataConverterTest {

    private final SensitiveDataConverter converter = new SensitiveDataConverter();

    private ILoggingEvent mockEvent(String message) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(message);
        return event;
    }

    @Test
    @DisplayName("纯文本消息应正常返回")
    void plainTextMessage() {
        String result = converter.convert(mockEvent("用户登录成功"));
        assertEquals("用户登录成功", result);
    }

    @Test
    @DisplayName("含手机号的消息应被遮盖")
    void messageWithPhone() {
        String result = converter.convert(mockEvent("用户手机：13812345678"));
        assertNotNull(result);
        assertFalse(result.contains("13812345678"), "手机号应被遮盖");
    }

    @Test
    @DisplayName("含JSON密码字段的消息应被遮盖")
    void messageWithJsonPassword() {
        String json = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        String result = converter.convert(mockEvent(json));
        assertNotNull(result);
        assertFalse(result.contains("secret123"), "password值应被maskJson遮盖");
    }

    @Test
    @DisplayName("含邮箱的消息应被遮盖")
    void messageWithEmail() {
        String result = converter.convert(mockEvent("联系邮箱：test@example.com"));
        assertNotNull(result);
        assertFalse(result.contains("test@example.com"), "邮箱应被遮盖");
    }

    @Test
    @DisplayName("含JWT的消息应被遮盖")
    void messageWithJwt() {
        String result = converter.convert(mockEvent("token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.sig"));
        assertNotNull(result);
        assertFalse(result.contains("eyJhbGciOiJIUzI1NiJ9"), "JWT应被遮盖");
    }

    @Test
    @DisplayName("null消息应返回null")
    void nullMessage() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(null);
        assertNull(converter.convert(event));
    }

    @Test
    @DisplayName("含JSON token字段的消息应被遮盖")
    void messageWithJsonToken() {
        String json = "{\"token\":\"abc123\",\"name\":\"test\"}";
        String result = converter.convert(mockEvent(json));
        assertNotNull(result);
        assertFalse(result.contains("abc123"), "token值应被maskJson遮盖");
    }
}
