package com.dongmedicine.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SensitiveDataUtils 回归测试")
class SensitiveDataUtilsTest {

    @Nested
    @DisplayName("isSensitiveField")
    class IsSensitiveField {
        @Test
        @DisplayName("password相关字段应识别为敏感")
        void passwordFields() {
            assertTrue(SensitiveDataUtils.isSensitiveField("password"));
            assertTrue(SensitiveDataUtils.isSensitiveField("passwd"));
            assertTrue(SensitiveDataUtils.isSensitiveField("pwd"));
        }

        @Test
        @DisplayName("token相关字段应识别为敏感")
        void tokenFields() {
            assertTrue(SensitiveDataUtils.isSensitiveField("token"));
            assertTrue(SensitiveDataUtils.isSensitiveField("accesstoken"));
            assertTrue(SensitiveDataUtils.isSensitiveField("refreshtoken"));
        }

        @Test
        @DisplayName("个人信息字段应识别为敏感")
        void personalFields() {
            assertTrue(SensitiveDataUtils.isSensitiveField("phone"));
            assertTrue(SensitiveDataUtils.isSensitiveField("mobile"));
            assertTrue(SensitiveDataUtils.isSensitiveField("email"));
            assertTrue(SensitiveDataUtils.isSensitiveField("idcard"));
            assertTrue(SensitiveDataUtils.isSensitiveField("userPhone"));
            assertTrue(SensitiveDataUtils.isSensitiveField("userEmail"));
        }

        @Test
        @DisplayName("非敏感字段不应被误判")
        void nonSensitiveFields() {
            assertFalse(SensitiveDataUtils.isSensitiveField("username"));
            assertFalse(SensitiveDataUtils.isSensitiveField("title"));
            assertFalse(SensitiveDataUtils.isSensitiveField("content"));
        }

        @Test
        @DisplayName("null应返回false")
        void nullShouldReturnFalse() {
            assertFalse(SensitiveDataUtils.isSensitiveField(null));
        }

        @Test
        @DisplayName("大小写不敏感")
        void caseInsensitive() {
            assertTrue(SensitiveDataUtils.isSensitiveField("Password"));
            assertTrue(SensitiveDataUtils.isSensitiveField("TOKEN"));
            assertTrue(SensitiveDataUtils.isSensitiveField("Email"));
        }

        @Test
        @DisplayName("部分匹配也应识别")
        void partialMatch() {
            assertTrue(SensitiveDataUtils.isSensitiveField("userpassword"));
            assertTrue(SensitiveDataUtils.isSensitiveField("apitoken"));
        }
    }

    @Nested
    @DisplayName("maskValue")
    class MaskValue {
        @Test
        @DisplayName("短值应全部遮盖")
        void shortValue() {
            assertEquals("**", SensitiveDataUtils.maskValue("ab"));
            assertEquals("*", SensitiveDataUtils.maskValue("a"));
        }

        @Test
        @DisplayName("中等长度应保留首尾")
        void mediumValue() {
            String result = SensitiveDataUtils.maskValue("abcdef");
            assertEquals('a', result.charAt(0));
            assertEquals('f', result.charAt(result.length() - 1));
            assertTrue(result.contains("*"));
        }

        @Test
        @DisplayName("长值应保留前2后2")
        void longValue() {
            String result = SensitiveDataUtils.maskValue("abcdefghij");
            assertTrue(result.startsWith("ab"));
            assertTrue(result.endsWith("ij"));
            assertTrue(result.contains("*"));
        }

        @Test
        @DisplayName("null/空应原样返回")
        void nullEmpty() {
            assertNull(SensitiveDataUtils.maskValue(null));
            assertEquals("", SensitiveDataUtils.maskValue(""));
        }
    }

    @Nested
    @DisplayName("mask")
    class Mask {
        @Test
        @DisplayName("敏感字段应遮盖")
        void sensitiveField() {
            String result = SensitiveDataUtils.mask("password", "mySecret123");
            assertNotEquals("mySecret123", result);
            assertTrue(result.contains("*"));
        }

        @Test
        @DisplayName("非敏感字段应自动检测")
        void nonSensitiveField() {
            String result = SensitiveDataUtils.mask("username", "testuser");
            assertEquals("testuser", result);
        }

        @Test
        @DisplayName("null值应原样返回")
        void nullValue() {
            assertNull(SensitiveDataUtils.mask("password", null));
        }
    }

    @Nested
    @DisplayName("autoMask")
    class AutoMask {
        @Test
        @DisplayName("应遮盖手机号")
        void phone() {
            String result = SensitiveDataUtils.autoMask("联系手机：13812345678");
            assertTrue(result.contains("****"));
            assertFalse(result.contains("13812345678"));
        }

        @Test
        @DisplayName("应遮盖邮箱")
        void email() {
            String result = SensitiveDataUtils.autoMask("邮箱：test@example.com");
            assertTrue(result.contains("***"));
        }

        @Test
        @DisplayName("应遮盖JWT")
        void jwt() {
            String result = SensitiveDataUtils.autoMask("token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.signature");
            assertFalse(result.contains("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.signature"));
        }

        @Test
        @DisplayName("null/空应原样返回")
        void nullEmpty() {
            assertNull(SensitiveDataUtils.autoMask(null));
            assertEquals("", SensitiveDataUtils.autoMask(""));
        }

        @Test
        @DisplayName("正常文本不应被修改")
        void normalText() {
            String text = "侗族医药文化";
            assertEquals(text, SensitiveDataUtils.autoMask(text));
        }
    }

    @Nested
    @DisplayName("maskJson")
    class MaskJson {
        @Test
        @DisplayName("应遮盖JSON中的敏感字段")
        void jsonSensitiveFields() {
            String json = "{\"username\":\"admin\",\"password\":\"secret123\"}";
            String result = SensitiveDataUtils.maskJson(json);
            assertTrue(result.contains("*"));
            assertFalse(result.contains("secret123"));
        }

        @Test
        @DisplayName("应遮盖token字段")
        void jsonToken() {
            String json = "{\"token\":\"abc123\"}";
            String result = SensitiveDataUtils.maskJson(json);
            assertFalse(result.contains("abc123"));
        }

        @Test
        @DisplayName("null/空应原样返回")
        void nullEmpty() {
            assertNull(SensitiveDataUtils.maskJson(null));
            assertEquals("", SensitiveDataUtils.maskJson(""));
        }
    }
}
