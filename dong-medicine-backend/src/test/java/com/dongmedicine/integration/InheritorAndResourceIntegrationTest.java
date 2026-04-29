package com.dongmedicine.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("传承人与资源模块集成测试")
class InheritorAndResourceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/inheritors/list")
    class InheritorList {

        @Test
        @DisplayName("应返回传承人列表")
        void shouldReturnInheritorList() throws Exception {
            mockMvc.perform(get("/api/inheritors/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("按级别过滤")
        void shouldFilterByLevel() throws Exception {
            mockMvc.perform(get("/api/inheritors/list")
                            .param("page", "1")
                            .param("size", "10")
                            .param("level", "国家级"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/inheritors/{id}")
    class InheritorDetail {

        @Test
        @DisplayName("获取传承人详情")
        void shouldReturnInheritorDetail() throws Exception {
            mockMvc.perform(get("/api/inheritors/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/resources/list")
    class ResourceList {

        @Test
        @DisplayName("应返回资源列表")
        void shouldReturnResourceList() throws Exception {
            mockMvc.perform(get("/api/resources/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/resources/hot")
    class HotResources {

        @Test
        @DisplayName("获取热门资源")
        void shouldReturnHotResources() throws Exception {
            mockMvc.perform(get("/api/resources/hot"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/resources/categories")
    class ResourceCategories {

        @Test
        @DisplayName("获取资源分类")
        void shouldReturnCategories() throws Exception {
            mockMvc.perform(get("/api/resources/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
