package com.dongmedicine.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IpUtils 回归测试")
class IpUtilsTest {

    @Nested
    @DisplayName("getClientIp")
    class GetClientIp {

        @Test
        @DisplayName("null请求应返回unknown")
        void nullRequest() {
            assertEquals("unknown", IpUtils.getClientIp(null));
        }

        @Test
        @DisplayName("X-Forwarded-For头应优先使用")
        void xForwardedFor() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1");
            request.setRemoteAddr("127.0.0.1");
            assertEquals("192.168.1.1", IpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("X-Real-IP头应次优先使用")
        void xRealIp() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Real-IP", "10.0.0.5");
            request.setRemoteAddr("127.0.0.1");
            assertEquals("10.0.0.5", IpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("无代理头应使用remoteAddr")
        void remoteAddr() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("10.0.0.5");
            assertEquals("10.0.0.5", IpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("X-Forwarded-For应取第一个IP")
        void xForwardedForFirst() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "1.1.1.1, 2.2.2.2, 3.3.3.3");
            assertEquals("1.1.1.1", IpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("unknown值应跳过")
        void unknownValue() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "unknown");
            request.setRemoteAddr("10.0.0.1");
            assertEquals("10.0.0.1", IpUtils.getClientIp(request));
        }
    }
}
