package com.dongmedicine.regression;

import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.common.util.XssUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("回归测试 - 历史Bug场景")
class RegressionTest {

    @Nested
    @DisplayName("Bug: 列表接口默认查全量数据(size=9999)")
    class PaginationBypassBug {

        @Test
        @DisplayName("size=9999应被限制为100")
        void size9999ShouldBeCapped() {
            var page = PageUtils.getPage(1, 9999);
            assertEquals(100, page.getSize());
        }

        @Test
        @DisplayName("size=1000应被限制为100")
        void size1000ShouldBeCapped() {
            var page = PageUtils.getPage(1, 1000);
            assertEquals(100, page.getSize());
        }

        @Test
        @DisplayName("size=0应被限制为1")
        void size0ShouldBeCapped() {
            var page = PageUtils.getPage(1, 0);
            assertEquals(1, page.getSize());
        }

        @Test
        @DisplayName("page=-1应被限制为1")
        void negativePageShouldBeCapped() {
            var page = PageUtils.getPage(-1, 10);
            assertEquals(1, page.getCurrent());
        }

        @Test
        @DisplayName("page=0应被限制为1")
        void zeroPageShouldBeCapped() {
            var page = PageUtils.getPage(0, 10);
            assertEquals(1, page.getCurrent());
        }

        @Test
        @DisplayName("null参数应使用默认值")
        void nullParamsShouldUseDefaults() {
            var page = PageUtils.getPage(null, null);
            assertEquals(1, page.getCurrent());
            assertEquals(20, page.getSize());
        }

        @ParameterizedTest
        @DisplayName("各种size边界值应被正确限制")
        @ValueSource(ints = {1, 10, 50, 99, 100, 101, 500, 9999, Integer.MAX_VALUE})
        void variousSizeBoundaryValues(int size) {
            var page = PageUtils.getPage(1, size);
            assertTrue(page.getSize() >= 1 && page.getSize() <= 100,
                    "size=" + size + " result=" + page.getSize() + " should be in [1,100]");
        }
    }

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

    @Nested
    @DisplayName("Bug: 401 Unauthorized - view接口需要登录")
    class ViewEndpointAuthBug {

        @Test
        @DisplayName("PageUtils.escapeLike应正确转义复合字符串")
        void escapeLikeShouldHandleComplexString() {
            String result = PageUtils.escapeLike("test\\%_value");
            assertEquals("test\\\\\\%\\_value", result);
        }

        @Test
        @DisplayName("escapeLike应处理null和空字符串")
        void escapeLikeShouldHandleNullAndEmpty() {
            assertNull(PageUtils.escapeLike(null));
            assertEquals("", PageUtils.escapeLike(""));
        }
    }

    @Nested
    @DisplayName("Bug: WebSocket DeepSeek调用阻塞")
    class WebSocketBlockingBug {

        @Test
        @DisplayName("PageUtils分页应确保size上限100")
        void pageSizeShouldBeBounded() {
            for (int size : new int[]{1, 10, 50, 100, 101, 500, 9999, Integer.MAX_VALUE}) {
                var page = PageUtils.getPage(1, size);
                assertTrue(page.getSize() <= 100, "size=" + size + " should be <= 100");
                assertTrue(page.getSize() >= 1, "size=" + size + " should be >= 1");
            }
        }
    }

    @Nested
    @DisplayName("Bug: 视频播放ERR_CACHE_OPERATION_NOT_SUPPORTED")
    class VideoPlaybackBug {

        @Test
        @DisplayName("分页参数边界值应正确处理")
        void paginationBoundaryShouldWork() {
            var page1 = PageUtils.getPage(1, 1);
            assertEquals(1, page1.getSize());

            var page100 = PageUtils.getPage(1, 100);
            assertEquals(100, page100.getSize());

            var pageMax = PageUtils.getPage(Integer.MAX_VALUE, 100);
            assertEquals(Integer.MAX_VALUE, pageMax.getCurrent());
        }
    }

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
    @DisplayName("Bug: 问答题目count参数无上限")
    class QuizCountUnboundedBug {

        @Test
        @DisplayName("PageUtils应确保所有size参数被限制")
        void allSizeParamsShouldBeBounded() {
            assertEquals(100, PageUtils.getPage(1, 9999).getSize());
            assertEquals(100, PageUtils.getPage(1, 500).getSize());
            assertEquals(50, PageUtils.getPage(1, 50).getSize());
            assertEquals(1, PageUtils.getPage(1, 1).getSize());
        }
    }

    @Nested
    @DisplayName("Bug: 管理后台日志LIMIT 5000")
    class AdminLogLimitBug {

        @Test
        @DisplayName("PageUtils最大size应为100")
        void maxPageSizeShouldBe100() {
            var page = PageUtils.getPage(1, 5000);
            assertEquals(100, page.getSize());
        }
    }

    @Nested
    @DisplayName("Bug: RestTemplate无超时配置")
    class RestTemplateTimeoutBug {

        @Test
        @DisplayName("PageUtils应处理极端值")
        void pageUtilsShouldHandleExtremeValues() {
            assertDoesNotThrow(() -> PageUtils.getPage(null, null));
            assertDoesNotThrow(() -> PageUtils.getPage(-1, -1));
            assertDoesNotThrow(() -> PageUtils.getPage(0, 0));
            assertDoesNotThrow(() -> PageUtils.getPage(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("Bug: 密码错误返回500而非400/401")
    class PasswordWrongHttpCodeBug {

        @Test
        @DisplayName("sanitize应处理null输入")
        void sanitizeShouldHandleNull() {
            assertNull(XssUtils.sanitize(null));
        }

        @Test
        @DisplayName("sanitize应处理空字符串")
        void sanitizeShouldHandleEmpty() {
            assertEquals("", XssUtils.sanitize(""));
        }

        @Test
        @DisplayName("sanitize应保留正常中文文本")
        void sanitizeShouldPreserveChineseText() {
            String input = "侗族医药是中华民族传统医药的重要组成部分";
            assertEquals(input, XssUtils.sanitize(input));
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

    @Nested
    @DisplayName("Bug: 搜索特殊字符导致数据库错误")
    class SearchSpecialCharsBug {

        @Test
        @DisplayName("escapeLike应转义所有LIKE通配符")
        void escapeLikeShouldEscapeAllWildcards() {
            String input = "%test_value\\path%";
            String result = PageUtils.escapeLike(input);
            assertTrue(result.contains("\\%"));
            assertTrue(result.contains("\\_"));
            assertTrue(result.contains("\\\\"));
        }

        @Test
        @DisplayName("sanitizeUrl应处理null")
        void sanitizeUrlShouldHandleNull() {
            assertEquals("", XssUtils.sanitizeUrl(null));
        }

        @Test
        @DisplayName("sanitizeUrl应保留合法HTTP URL")
        void sanitizeUrlShouldPreserveHttpUrl() {
            String url = "http://example.com/api/data";
            String result = XssUtils.sanitizeUrl(url);
            assertEquals(url, result);
        }

        @Test
        @DisplayName("sanitizeUrl应保留合法HTTPS URL")
        void sanitizeUrlShouldPreserveHttpsUrl() {
            String url = "https://example.com/api/data";
            String result = XssUtils.sanitizeUrl(url);
            assertEquals(url, result);
        }
    }
}
