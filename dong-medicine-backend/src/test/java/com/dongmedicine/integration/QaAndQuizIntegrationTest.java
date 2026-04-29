package com.dongmedicine.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("问答与测验模块集成测试")
class QaAndQuizIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/qa/list")
    class QaList {

        @Test
        @DisplayName("应返回问答列表")
        void shouldReturnQaList() throws Exception {
            mockMvc.perform(get("/api/qa/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("搜索问答应返回结果")
        void shouldSearchQa() throws Exception {
            mockMvc.perform(get("/api/qa/search")
                            .param("keyword", "侗医药")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/quiz/questions")
    class QuizQuestions {

        @Test
        @DisplayName("获取随机题目应返回列表")
        void shouldReturnRandomQuestions() throws Exception {
            mockMvc.perform(get("/api/quiz/questions")
                            .param("count", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("count超过50应被限制")
        void shouldLimitCountTo50() throws Exception {
            mockMvc.perform(get("/api/quiz/questions")
                            .param("count", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(lessThanOrEqualTo(50)));
        }
    }

    @Nested
    @DisplayName("GET /api/quiz/list")
    class QuizList {

        @Test
        @DisplayName("题目列表分页应正常")
        void shouldReturnQuizListPaged() throws Exception {
            mockMvc.perform(get("/api/quiz/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }
}
