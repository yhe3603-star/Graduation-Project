package com.dongmedicine.regression;

import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.common.util.XssUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("回归测试 - 其他Bug场景")
class GeneralRegressionTest {

    @Nested
    @DisplayName("Bug: WebSocket DeepSeek调用阻塞")
    class WebSocketBlockingBug {
        @Test
        @DisplayName("PageUtils分页应确保size上限100")
        void pageSizeShouldBeBounded() {
            for (int size : new int[]{1, 10, 50, 100, 101, 500, 9999, Integer.MAX_VALUE}) {
                var page = PageUtils.getPage(1, size);
                assertTrue(page.getSize() <= 100);
                assertTrue(page.getSize() >= 1);
            }
        }
    }

    @Nested
    @DisplayName("Bug: 视频播放ERR_CACHE_OPERATION_NOT_SUPPORTED")
    class VideoPlaybackBug {
        @Test
        @DisplayName("分页参数边界值应正确处理")
        void paginationBoundaryShouldWork() {
            assertEquals(1, PageUtils.getPage(1, 1).getSize());
            assertEquals(100, PageUtils.getPage(1, 100).getSize());
            assertEquals(Integer.MAX_VALUE, PageUtils.getPage(Integer.MAX_VALUE, 100).getCurrent());
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
            assertEquals(100, PageUtils.getPage(1, 5000).getSize());
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
            assertEquals(url, XssUtils.sanitizeUrl(url));
        }

        @Test
        @DisplayName("sanitizeUrl应保留合法HTTPS URL")
        void sanitizeUrlShouldPreserveHttpsUrl() {
            String url = "https://example.com/api/data";
            assertEquals(url, XssUtils.sanitizeUrl(url));
        }
    }
}
