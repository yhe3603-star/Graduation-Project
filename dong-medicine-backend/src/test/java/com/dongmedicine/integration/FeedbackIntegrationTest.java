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

@DisplayName("反馈模块集成测试")
class FeedbackIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("POST /api/feedback")
    class SubmitFeedback {

        @Test
        @DisplayName("匿名提交反馈应成功")
        void shouldSubmitAnonymously() throws Exception {
            mockMvc.perform(post("/api/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"suggestion\",\"title\":\"测试反馈\",\"content\":\"集成测试反馈内容\",\"contact\":\"\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("空内容应返回400")
        void shouldFailWithEmptyContent() throws Exception {
            mockMvc.perform(post("/api/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"suggestion\",\"title\":\"测试\",\"content\":\"\",\"contact\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少类型应返回400")
        void shouldFailWithoutType() throws Exception {
            mockMvc.perform(post("/api/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"测试\",\"content\":\"内容\",\"contact\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少标题应返回400")
        void shouldFailWithoutTitle() throws Exception {
            mockMvc.perform(post("/api/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"suggestion\",\"content\":\"内容\",\"contact\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/feedback/stats")
    class FeedbackStats {

        @Test
        @DisplayName("统计接口应无需登录")
        void shouldReturnStatsWithoutLogin() throws Exception {
            mockMvc.perform(get("/api/feedback/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").isNumber())
                    .andExpect(jsonPath("$.data.pending").isNumber())
                    .andExpect(jsonPath("$.data.resolved").isNumber());
        }
    }
}
