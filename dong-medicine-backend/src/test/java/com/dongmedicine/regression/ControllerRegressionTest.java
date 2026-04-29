package com.dongmedicine.regression;

import com.dongmedicine.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller层API回归测试")
class ControllerRegressionTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"%s\",\"captchaKey\":\"%s\",\"captchaCode\":\"%s\"}",
                                TEST_USERNAME, TEST_PASSWORD, TEST_CAPTCHA_KEY, TEST_CAPTCHA_CODE)))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(body, "$.data.token");
        return "Bearer " + token;
    }

    @Nested
    @DisplayName("Bug: 浏览量递增POST不需要登录")
    class ViewEndpointAuthRegression {

        @Test
        @DisplayName("植物浏览量递增不应返回401")
        void plantViewNoAuth() throws Exception {
            mockMvc.perform(post("/api/plants/1/view"))
                    .andExpect(status().is(not(401)));
        }

        @Test
        @DisplayName("知识浏览量递增不应返回401")
        void knowledgeViewNoAuth() throws Exception {
            mockMvc.perform(post("/api/knowledge/1/view"))
                    .andExpect(status().is(not(401)));
        }

        @Test
        @DisplayName("传承人浏览量递增不应返回401")
        void inheritorViewNoAuth() throws Exception {
            mockMvc.perform(post("/api/inheritors/1/view"))
                    .andExpect(status().is(not(401)));
        }
    }

    @Nested
    @DisplayName("Bug: 反馈提交不需要登录")
    class FeedbackAuthRegression {

        @Test
        @DisplayName("匿名提交反馈应成功")
        void anonymousFeedback() throws Exception {
            mockMvc.perform(post("/api/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"suggestion\",\"title\":\"回归测试\",\"content\":\"验证匿名反馈\",\"contact\":\"\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("反馈统计不需要登录")
        void feedbackStatsNoAuth() throws Exception {
            mockMvc.perform(get("/api/feedback/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Bug: 列表接口size参数无上限")
    class PageSizeRegression {

        @Test
        @DisplayName("植物列表size=9999应被限制")
        void plantListSizeCapped() throws Exception {
            mockMvc.perform(get("/api/plants/list?page=1&size=9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records", hasSize(lessThanOrEqualTo(100))));
        }

        @Test
        @DisplayName("知识列表size=0应被限制")
        void knowledgeListSizeCapped() throws Exception {
            mockMvc.perform(get("/api/knowledge/list?page=1&size=0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(greaterThanOrEqualTo(1)));
        }

        @Test
        @DisplayName("传承人列表page=-1应被限制")
        void inheritorListPageCapped() throws Exception {
            mockMvc.perform(get("/api/inheritors/list?page=-1&size=10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").value(greaterThanOrEqualTo(1)));
        }
    }

    @Nested
    @DisplayName("Bug: XSS搜索导致服务器错误")
    class XssSearchRegression {

        @Test
        @DisplayName("script标签搜索不应500")
        void scriptTagSearch() throws Exception {
            mockMvc.perform(get("/api/plants/search?keyword=%3Cscript%3E&page=1&size=10"))
                    .andExpect(status().is(not(500)));
        }

        @Test
        @DisplayName("SQL注入搜索不应500")
        void sqlInjectionSearch() throws Exception {
            mockMvc.perform(get("/api/plants/search?keyword=1+OR+1%3D1&page=1&size=10"))
                    .andExpect(status().is(not(500)));
        }
    }

    @Nested
    @DisplayName("Bug: 验证码接口不需要登录")
    class CaptchaAuthRegression {

        @Test
        @DisplayName("验证码接口应返回200")
        void captchaNoAuth() throws Exception {
            mockMvc.perform(get("/api/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.captchaKey").isNotEmpty())
                    .andExpect(jsonPath("$.data.captchaImage").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Bug: 评论列表不需要登录")
    class CommentAuthRegression {

        @Test
        @DisplayName("评论列表应不需要登录")
        void commentListNoAuth() throws Exception {
            mockMvc.perform(get("/api/comments/list/plant/1"))
                    .andExpect(status().is(not(401)));
        }
    }

    @Nested
    @DisplayName("Bug: 排行榜不需要登录")
    class LeaderboardAuthRegression {

        @Test
        @DisplayName("综合排行榜应不需要登录")
        void leaderboardNoAuth() throws Exception {
            mockMvc.perform(get("/api/leaderboard/combined"))
                    .andExpect(status().is(not(401)));
        }
    }

    @Nested
    @DisplayName("Bug: 测验题目不需要登录")
    class QuizAuthRegression {

        @Test
        @DisplayName("测验题目应不需要登录")
        void quizQuestionsNoAuth() throws Exception {
            mockMvc.perform(get("/api/quiz/questions?count=5"))
                    .andExpect(status().is(not(401)));
        }
    }

    @Nested
    @DisplayName("Bug: 资源相关接口不需要登录")
    class ResourceAuthRegression {

        @Test
        @DisplayName("资源列表应不需要登录")
        void resourceListNoAuth() throws Exception {
            mockMvc.perform(get("/api/resources/list"))
                    .andExpect(status().is(not(401)));
        }

        @Test
        @DisplayName("资源分类应不需要登录")
        void resourceCategoriesNoAuth() throws Exception {
            mockMvc.perform(get("/api/resources/categories"))
                    .andExpect(status().is(not(401)));
        }

        @Test
        @DisplayName("资源类型应不需要登录")
        void resourceTypesNoAuth() throws Exception {
            mockMvc.perform(get("/api/resources/types"))
                    .andExpect(status().is(not(401)));
        }

        @Test
        @DisplayName("热门资源应不需要登录")
        void hotResourcesNoAuth() throws Exception {
            mockMvc.perform(get("/api/resources/hot"))
                    .andExpect(status().is(not(401)));
        }
    }

    @Nested
    @DisplayName("Bug: 需要登录的接口应返回401")
    class AuthRequiredRegression {

        @Test
        @DisplayName("获取用户信息需要登录")
        void userInfoRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/user/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("我的反馈需要登录")
        void myFeedbackRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/feedback/my"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("我的评论需要登录")
        void myCommentsRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/comments/my"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("我的收藏需要登录")
        void myFavoritesRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/favorites/my"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Bug: 登录后Token应有效")
    class TokenValidationRegression {

        @Test
        @DisplayName("登录后Token应能验证")
        void tokenShouldBeValidAfterLogin() throws Exception {
            String token = loginAndGetToken();
            mockMvc.perform(get("/api/user/validate")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("登录后应能获取用户信息")
        void shouldGetUserInfoAfterLogin() throws Exception {
            String token = loginAndGetToken();
            mockMvc.perform(get("/api/user/me")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
        }
    }

    @Nested
    @DisplayName("Bug: 统一响应格式")
    class ResponseFormatRegression {

        @Test
        @DisplayName("植物列表应返回统一格式")
        void plantListFormat() throws Exception {
            mockMvc.perform(get("/api/plants/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("知识列表应返回统一格式")
        void knowledgeListFormat() throws Exception {
            mockMvc.perform(get("/api/knowledge/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("分页数据应包含page/size/total/records")
        void paginationFormat() throws Exception {
            mockMvc.perform(get("/api/plants/list?page=1&size=10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").exists())
                    .andExpect(jsonPath("$.data.size").exists())
                    .andExpect(jsonPath("$.data.total").exists())
                    .andExpect(jsonPath("$.data.records").exists());
        }
    }

    @Nested
    @DisplayName("Bug: 元数据接口不需要登录")
    class MetadataAuthRegression {

        @Test
        @DisplayName("筛选选项不需要登录")
        void filtersNoAuth() throws Exception {
            mockMvc.perform(get("/api/metadata/filters"))
                    .andExpect(status().is(not(401)));
        }
    }

    @Nested
    @DisplayName("Bug: 统计接口不需要登录")
    class StatsAuthRegression {

        @Test
        @DisplayName("图表统计不需要登录")
        void chartStatsNoAuth() throws Exception {
            mockMvc.perform(get("/api/stats/chart"))
                    .andExpect(status().is(not(401)));
        }
    }
}
