package com.dongmedicine.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("植物模块集成测试")
class PlantIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/plants/list")
    class ListPlants {

        @Test
        @DisplayName("应返回植物列表和分页信息")
        void shouldReturnPlantList() throws Exception {
            mockMvc.perform(get("/api/plants/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").isNumber())
                    .andExpect(jsonPath("$.data.page").value(1));
        }

        @Test
        @DisplayName("size=9999应被限制为100")
        void shouldLimitSizeTo100() throws Exception {
            mockMvc.perform(get("/api/plants/list")
                            .param("page", "1")
                            .param("size", "9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(100));
        }

        @Test
        @DisplayName("size=0应被限制为1")
        void shouldLimitSizeTo1() throws Exception {
            mockMvc.perform(get("/api/plants/list")
                            .param("page", "1")
                            .param("size", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(1));
        }

        @Test
        @DisplayName("page=-1应被限制为1")
        void shouldLimitPageTo1() throws Exception {
            mockMvc.perform(get("/api/plants/list")
                            .param("page", "-1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").value(1));
        }

        @Test
        @DisplayName("按分类过滤应返回对应植物")
        void shouldFilterByCategory() throws Exception {
            mockMvc.perform(get("/api/plants/list")
                            .param("page", "1")
                            .param("size", "10")
                            .param("category", "清热药"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("默认分页参数应正常工作")
        void shouldWorkWithDefaultParams() throws Exception {
            mockMvc.perform(get("/api/plants/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/plants/search")
    class SearchPlants {

        @Test
        @DisplayName("搜索钩藤应返回结果")
        void shouldSearchByKeyword() throws Exception {
            mockMvc.perform(get("/api/plants/search")
                            .param("keyword", "钩藤")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/plants/{id}")
    class GetPlantDetail {

        @Test
        @DisplayName("获取存在的植物详情")
        void shouldReturnPlantDetail() throws Exception {
            mockMvc.perform(get("/api/plants/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data.nameCn").isNotEmpty());
        }

        @Test
        @DisplayName("获取不存在的植物应返回404")
        void shouldReturnErrorForNonExistent() throws Exception {
            mockMvc.perform(get("/api/plants/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/plants/{id}/view")
    class IncrementViewCount {

        @Test
        @DisplayName("浏览量递增应无需登录")
        void shouldIncrementWithoutLogin() throws Exception {
            mockMvc.perform(post("/api/plants/1/view"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/plants/random")
    class RandomPlants {

        @Test
        @DisplayName("获取随机植物应返回列表")
        void shouldReturnRandomPlants() throws Exception {
            mockMvc.perform(get("/api/plants/random")
                            .param("limit", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/plants/{id}/similar")
    class SimilarPlants {

        @Test
        @DisplayName("获取相似植物应返回列表")
        void shouldReturnSimilarPlants() throws Exception {
            mockMvc.perform(get("/api/plants/1/similar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
