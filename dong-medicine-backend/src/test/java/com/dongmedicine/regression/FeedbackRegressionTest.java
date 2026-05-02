package com.dongmedicine.regression;

import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.common.util.XssUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("回归测试 - Feedback")
class FeedbackRegressionTest {
@Nested
    @DisplayName("Bug: 反馈提交logFetchError未定义")
    class FeedbackSubmitBug {

        @Test
        @DisplayName("XssUtils.sanitizeForLog应处理null")
        void sanitizeForLogShouldHandleNull() {
            String result = XssUtils.sanitizeForLog(null);
            assertNull(result);
        }

        @Test
        @DisplayName("XssUtils.sanitizeForLog应截断超长输入")
        void sanitizeForLogShouldTruncate() {
            String longInput = "a".repeat(5000);
            String result = XssUtils.sanitizeForLog(longInput);
            assertTrue(result.length() <= 1003);
        }

        @Test
        @DisplayName("XssUtils.sanitizeForLog应处理空字符串")
        void sanitizeForLogShouldHandleEmpty() {
            String result = XssUtils.sanitizeForLog("");
            assertEquals("", result);
        }

        @Test
        @DisplayName("XssUtils.sanitizeForLog应清除控制字符")
        void sanitizeForLogShouldRemoveControlChars() {
            String input = "test\n\r\tvalue";
            String result = XssUtils.sanitizeForLog(input);
            assertFalse(result.contains("\n"));
            assertFalse(result.contains("\r"));
        }
    }

@Nested
    @DisplayName("Bug: 反馈提交缺少验证码字段导致500")
    class FeedbackMissingFieldBug {

        @Test
        @DisplayName("containsSqlInjection应处理null")
        void containsSqlInjectionShouldHandleNull() {
            assertFalse(XssUtils.containsSqlInjection(null));
        }

        @Test
        @DisplayName("containsSqlInjection应处理空字符串")
        void containsSqlInjectionShouldHandleEmpty() {
            assertFalse(XssUtils.containsSqlInjection(""));
        }

        @Test
        @DisplayName("containsXss应处理null")
        void containsXssShouldHandleNull() {
            assertFalse(XssUtils.containsXss(null));
        }

        @Test
        @DisplayName("containsXss应处理空字符串")
        void containsXssShouldHandleEmpty() {
            assertFalse(XssUtils.containsXss(""));
        }
    }

}
