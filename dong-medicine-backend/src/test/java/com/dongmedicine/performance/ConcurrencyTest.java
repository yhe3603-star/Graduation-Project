package com.dongmedicine.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@DisplayName("并发访问测试")
class ConcurrencyTest extends BasePerformanceTest {

    @Test
    @DisplayName("50并发读取植物列表应无报错")
    void concurrentPlantListShouldHaveNoErrors() throws Exception {
        ConcurrencyResult result = runConcurrent(CONCURRENT_USERS, () -> {
            mockMvc.perform(get("/api/plants/list")
                    .param("page", "1").param("size", "12")).andReturn();
            return null;
        });
        System.out.println("50并发植物列表 - " + result);
        assertEquals(0, result.errorCount(), "50并发下植物列表出现错误");
    }

    @Test
    @DisplayName("50并发读取知识库列表应无报错")
    void concurrentKnowledgeListShouldHaveNoErrors() throws Exception {
        ConcurrencyResult result = runConcurrent(CONCURRENT_USERS, () -> {
            mockMvc.perform(get("/api/knowledge/list")
                    .param("page", "1").param("size", "12")).andReturn();
            return null;
        });
        System.out.println("50并发知识库列表 - " + result);
        assertEquals(0, result.errorCount(), "50并发下知识库列表出现错误");
    }

    @Test
    @DisplayName("50并发搜索应无报错")
    void concurrentSearchShouldHaveNoErrors() throws Exception {
        ConcurrencyResult result = runConcurrent(CONCURRENT_USERS, () -> {
            mockMvc.perform(get("/api/search/suggest")
                    .param("keyword", "侗")).andReturn();
            return null;
        });
        System.out.println("50并发搜索 - " + result);
        assertEquals(0, result.errorCount(), "50并发下搜索出现错误");
    }

    @Test
    @DisplayName("50并发混合操作应无报错")
    void concurrentMixedOperationsShouldHaveNoErrors() throws Exception {
        ConcurrencyResult result = runConcurrent(CONCURRENT_USERS, () -> {
            mockMvc.perform(get("/api/plants/list")
                    .param("page", "1").param("size", "6")).andReturn();
            mockMvc.perform(get("/api/knowledge/list")
                    .param("page", "1").param("size", "6")).andReturn();
            return null;
        });
        System.out.println("50并发混合操作 - " + result);
        assertEquals(0, result.errorCount(), "50并发下混合操作出现错误");
    }
}
