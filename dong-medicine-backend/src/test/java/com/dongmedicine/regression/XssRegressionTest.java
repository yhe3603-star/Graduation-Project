package com.dongmedicine.regression;

import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.common.util.XssUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("回归测试 - Xss")
class XssRegressionTest {
@Nested
    @DisplayName("Bug: XSS漏洞 - AiChatCard渲染恶意HTML")
    class XssVulnerabilityBug {

        @Test
        @DisplayName("script标签应被转义")
        void scriptTagShouldBeEscaped() {
            String input = "<script>alert('xss')</script>";
            String sanitized = XssUtils.sanitize(input);
            assertFalse(sanitized.contains("<script>"));
            assertTrue(sanitized.contains("&lt;script"));
        }

        @Test
        @DisplayName("javascript协议应被检测")
        void javascriptProtocolShouldBeDetected() {
            assertTrue(XssUtils.containsXss("javascript:alert(1)"));
        }

        @Test
        @DisplayName("事件处理器应被检测")
        void eventHandlerShouldBeDetected() {
            assertTrue(XssUtils.containsXss("onerror=alert(1)"));
        }

        @Test
        @DisplayName("iframe应被检测")
        void iframeShouldBeDetected() {
            assertTrue(XssUtils.containsXss("<iframe src=\"evil\">"));
        }

        @Test
        @DisplayName("URL中javascript协议应被清除")
        void urlJavascriptProtocolShouldBeCleared() {
            assertEquals("", XssUtils.sanitizeUrl("javascript:alert(1)"));
        }

        @Test
        @DisplayName("URL中data协议应被清除")
        void urlDataProtocolShouldBeCleared() {
            assertEquals("", XssUtils.sanitizeUrl("data:text/html,<script>alert(1)</script>"));
        }

        @Test
        @DisplayName("SQL注入应被检测")
        void sqlInjectionShouldBeDetected() {
            assertTrue(XssUtils.containsSqlInjection("1 OR 1=1"));
            assertTrue(XssUtils.containsSqlInjection("'; DROP TABLE users;--"));
        }

        @Test
        @DisplayName("LIKE通配符应被转义")
        void likeWildcardsShouldBeEscaped() {
            assertEquals("\\%", PageUtils.escapeLike("%"));
            assertEquals("\\_", PageUtils.escapeLike("_"));
            assertEquals("\\\\", PageUtils.escapeLike("\\"));
        }

        @Test
        @DisplayName("img标签onerror应被检测")
        void imgOnerrorShouldBeDetected() {
            assertTrue(XssUtils.containsXss("<img src=x onerror=alert(1)>"));
        }

        @Test
        @DisplayName("svg标签应被检测")
        void svgTagShouldBeDetected() {
            assertTrue(XssUtils.containsXss("<svg onload=alert(1)>"));
        }

        @Test
        @DisplayName("大小写混合的XSS应被检测")
        void mixedCaseXssShouldBeDetected() {
            assertTrue(XssUtils.containsXss("<ScRiPt>alert(1)</ScRiPt>"));
            assertTrue(XssUtils.containsXss("JAVASCRIPT:alert(1)"));
        }

        @Test
        @DisplayName("正常文本不应被误判为XSS")
        void normalTextShouldNotBeFlagged() {
            assertFalse(XssUtils.containsXss("侗族医药文化"));
            assertFalse(XssUtils.containsXss("钩藤的药用价值"));
        }

        @Test
        @DisplayName("正常URL不应被清除")
        void normalUrlShouldNotBeCleared() {
            assertNotEquals("", XssUtils.sanitizeUrl("https://example.com/image.png"));
            assertNotEquals("", XssUtils.sanitizeUrl("http://localhost:8080/api/plants"));
        }
    }

}
