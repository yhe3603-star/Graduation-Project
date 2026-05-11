package com.dongmedicine.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@DisplayName("接口响应时间测试")
class ApiResponseTimeTest extends BasePerformanceTest {

    @BeforeEach
    void warmup() throws Exception {
        // JVM预热：对主要端点各发一次请求，避免冷启动影响测量
        mockMvc.perform(get("/api/stats/overview")).andReturn();
        mockMvc.perform(get("/api/plants/list").param("page", "1").param("size", "6")).andReturn();
        mockMvc.perform(get("/api/knowledge/list").param("page", "1").param("size", "6")).andReturn();
        mockMvc.perform(get("/api/search/suggest").param("keyword", "侗")).andReturn();
    }

    @Test
    @DisplayName("首页加载应小于等于3秒")
    void homePageShouldBeUnder3s() throws Exception {
        long t1 = measureMillis(get("/api/stats/overview"));
        long t2 = measureMillis(get("/api/plants/list").param("page", "1").param("size", "6"));
        long t3 = measureMillis(get("/api/knowledge/list").param("page", "1").param("size", "6"));
        long total = t1 + t2 + t3;
        System.out.printf("首页加载 - stats/overview: %dms, plants/list: %dms, knowledge/list: %dms, 总计: %dms%n",
                t1, t2, t3, total);
        assertTrue(total <= HOME_PAGE_THRESHOLD_MS,
                "首页加载总耗时 " + total + "ms 超过 " + HOME_PAGE_THRESHOLD_MS + "ms");
    }

    @Test
    @DisplayName("知识库列表加载应小于等于0.5秒")
    void knowledgeListShouldBeUnder500ms() throws Exception {
        long duration = measureMillis(get("/api/knowledge/list")
                .param("page", "1").param("size", "12"));
        System.out.println("知识库列表响应时间: " + duration + "ms");
        assertTrue(duration <= LIST_THRESHOLD_MS,
                "知识库列表响应时间 " + duration + "ms 超过 " + LIST_THRESHOLD_MS + "ms");
    }

    @Test
    @DisplayName("植物图鉴列表加载应小于等于0.5秒")
    void plantListShouldBeUnder500ms() throws Exception {
        long duration = measureMillis(get("/api/plants/list")
                .param("page", "1").param("size", "12"));
        System.out.println("植物图鉴列表响应时间: " + duration + "ms");
        assertTrue(duration <= LIST_THRESHOLD_MS,
                "植物图鉴列表响应时间 " + duration + "ms 超过 " + LIST_THRESHOLD_MS + "ms");
    }

    @Test
    @DisplayName("全文搜索响应应小于等于0.5秒")
    void searchShouldBeUnder500ms() throws Exception {
        long duration = measureMillis(get("/api/search/suggest")
                .param("keyword", "侗"));
        System.out.println("全文搜索响应时间: " + duration + "ms");
        assertTrue(duration <= SEARCH_THRESHOLD_MS,
                "全文搜索响应时间 " + duration + "ms 超过 " + SEARCH_THRESHOLD_MS + "ms");
    }

    @Test
    @DisplayName("植物搜索应小于等于0.5秒")
    void plantSearchShouldBeUnder500ms() throws Exception {
        long duration = measureMillis(get("/api/plants/search")
                .param("keyword", "钩藤").param("page", "1").param("size", "12"));
        System.out.println("植物搜索响应时间: " + duration + "ms");
        assertTrue(duration <= SEARCH_THRESHOLD_MS,
                "植物搜索响应时间 " + duration + "ms 超过 " + SEARCH_THRESHOLD_MS + "ms");
    }

    @Test
    @DisplayName("传承人列表应小于等于0.5秒")
    void inheritorListShouldBeUnder500ms() throws Exception {
        long duration = measureMillis(get("/api/inheritors/list")
                .param("page", "1").param("size", "12"));
        System.out.println("传承人列表响应时间: " + duration + "ms");
        assertTrue(duration <= LIST_THRESHOLD_MS,
                "传承人列表响应时间 " + duration + "ms 超过 " + LIST_THRESHOLD_MS + "ms");
    }
}
