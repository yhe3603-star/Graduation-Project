package com.dongmedicine.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("XSS工具类测试")
class XssUtilsTest {

    @Test
    @DisplayName("sanitize - null返回null")
    void testSanitize_Null() {
        assertNull(XssUtils.sanitize(null));
    }

    @Test
    @DisplayName("sanitize - 空字符串返回空")
    void testSanitize_Empty() {
        assertEquals("", XssUtils.sanitize(""));
    }

    @Test
    @DisplayName("sanitize - 转义HTML特殊字符")
    void testSanitize_HtmlChars() {
        String result = XssUtils.sanitize("<script>alert('xss')</script>");
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
        assertFalse(result.contains("<script>"));
    }

    @Test
    @DisplayName("sanitize - 转义引号")
    void testSanitize_Quotes() {
        String result = XssUtils.sanitize("test\"value'here");
        assertTrue(result.contains("&quot;"));
        assertTrue(result.contains("&#x27;"));
    }

    @Test
    @DisplayName("containsXss - 检测script标签")
    void testContainsXss_Script() {
        assertTrue(XssUtils.containsXss("<script>alert(1)</script>"));
    }

    @Test
    @DisplayName("containsXss - 检测javascript协议")
    void testContainsXss_JavascriptProtocol() {
        assertTrue(XssUtils.containsXss("javascript:alert(1)"));
    }

    @Test
    @DisplayName("containsXss - 检测事件处理器")
    void testContainsXss_EventHandler() {
        assertTrue(XssUtils.containsXss("onclick=alert(1)"));
    }

    @Test
    @DisplayName("containsXss - 检测iframe")
    void testContainsXss_Iframe() {
        assertTrue(XssUtils.containsXss("<iframe src=\"evil\">"));
    }

    @Test
    @DisplayName("containsXss - 正常文本返回false")
    void testContainsXss_NormalText() {
        assertFalse(XssUtils.containsXss("侗族医药知识"));
    }

    @Test
    @DisplayName("containsXss - null返回false")
    void testContainsXss_Null() {
        assertFalse(XssUtils.containsXss(null));
    }

    @Test
    @DisplayName("containsSqlInjection - 检测SQL注入")
    void testContainsSqlInjection_Select() {
        assertTrue(XssUtils.containsSqlInjection("1 OR 1=1"));
    }

    @Test
    @DisplayName("containsSqlInjection - 正常文本返回false")
    void testContainsSqlInjection_NormalText() {
        assertFalse(XssUtils.containsSqlInjection("侗医药知识"));
    }

    @Test
    @DisplayName("stripHtmlTags - 移除HTML标签")
    void testStripHtmlTags() {
        assertEquals("hello world", XssUtils.stripHtmlTags("<b>hello</b> <i>world</i>"));
    }

    @Test
    @DisplayName("sanitizeUrl - 正常HTTP URL")
    void testSanitizeUrl_NormalHttp() {
        assertEquals("http://example.com", XssUtils.sanitizeUrl("http://example.com"));
    }

    @Test
    @DisplayName("sanitizeUrl - 正常HTTPS URL")
    void testSanitizeUrl_NormalHttps() {
        assertEquals("https://example.com", XssUtils.sanitizeUrl("https://example.com"));
    }

    @Test
    @DisplayName("sanitizeUrl - 相对路径")
    void testSanitizeUrl_RelativePath() {
        assertEquals("/api/plants", XssUtils.sanitizeUrl("/api/plants"));
    }

    @Test
    @DisplayName("sanitizeUrl - javascript协议返回空")
    void testSanitizeUrl_JavascriptProtocol() {
        assertEquals("", XssUtils.sanitizeUrl("javascript:alert(1)"));
    }

    @Test
    @DisplayName("sanitizeUrl - data协议返回空")
    void testSanitizeUrl_DataProtocol() {
        assertEquals("", XssUtils.sanitizeUrl("data:text/html,<script>alert(1)</script>"));
    }

    @Test
    @DisplayName("sanitizeFileName - 移除非法字符")
    void testSanitizeFileName_IllegalChars() {
        String result = XssUtils.sanitizeFileName("test<>file.pdf");
        assertTrue(result.contains("_"));
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
    }

    @Test
    @DisplayName("sanitizeFileName - null返回空")
    void testSanitizeFileName_Null() {
        assertEquals("", XssUtils.sanitizeFileName(null));
    }

    @Test
    @DisplayName("escapeJavaScript - 转义特殊字符")
    void testEscapeJavaScript() {
        String result = XssUtils.escapeJavaScript("test\"value\nline");
        assertTrue(result.contains("\\\""));
        assertTrue(result.contains("\\n"));
    }

    @Test
    @DisplayName("isSafeInput - 正常输入安全")
    void testIsSafeInput_Safe() {
        assertTrue(XssUtils.isSafeInput("侗医药", 100));
    }

    @Test
    @DisplayName("isSafeInput - 超长输入不安全")
    void testIsSafeInput_TooLong() {
        assertFalse(XssUtils.isSafeInput("a".repeat(200), 100));
    }

    @Test
    @DisplayName("isSafeInput - XSS输入不安全")
    void testIsSafeInput_Xss() {
        assertFalse(XssUtils.isSafeInput("<script>alert(1)</script>", 100));
    }

    @Test
    @DisplayName("sanitizeHtml - 移除script标签")
    void testSanitizeHtml_Script() {
        String result = XssUtils.sanitizeHtml("<script>alert(1)</script>hello");
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("hello"));
    }

    @Test
    @DisplayName("sanitizeForLog - 截断超长输入")
    void testSanitizeForLog_TooLong() {
        String longInput = "a".repeat(2000);
        String result = XssUtils.sanitizeForLog(longInput);
        assertTrue(result.length() <= 1003);
        assertTrue(result.endsWith("..."));
    }
}
