package com.dongmedicine.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("知识库模块集成测试")
class KnowledgeIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/knowledge/list")
    class ListKnowledge {

        @Test
        @DisplayName("应返回知识列表")
        void shouldReturnKnowledgeList() throws Exception {
            mockMvc.perform(get("/api/knowledge/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("按疗法分类过滤")
        void shouldFilterByTherapy() throws Exception {
            mockMvc.perform(get("/api/knowledge/list")
                            .param("page", "1")
                            .param("size", "10")
                            .param("therapy", "药浴"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/knowledge/{id}")
    class GetKnowledgeDetail {

        @Test
        @DisplayName("获取知识详情")
        void shouldReturnKnowledgeDetail() throws Exception {
            mockMvc.perform(get("/api/knowledge/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data.title").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/knowledge/{id}/view")
    class IncrementViewCount {

        @Test
        @DisplayName("浏览量递增应无需登录")
        void shouldIncrementWithoutLogin() throws Exception {
            mockMvc.perform(post("/api/knowledge/1/view"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
