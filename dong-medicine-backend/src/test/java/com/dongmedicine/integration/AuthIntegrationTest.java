package com.dongmedicine.integration;

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

@DisplayName("认证模块集成测试")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String loginJson(String username, String password) {
        return String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"captchaKey\":\"%s\",\"captchaCode\":\"%s\"}",
                username, password, TEST_CAPTCHA_KEY, TEST_CAPTCHA_CODE);
    }

    private String registerJson(String username, String password, String confirmPassword) {
        return String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"confirmPassword\":\"%s\",\"captchaKey\":\"%s\",\"captchaCode\":\"%s\"}",
                username, password, confirmPassword, TEST_CAPTCHA_KEY, TEST_CAPTCHA_CODE);
    }

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(body, "$.data.token");
        return "Bearer " + token;
    }

    @Nested
    @DisplayName("POST /api/user/login")
    class Login {

        @Test
        @DisplayName("正确凭据应登录成功")
        void shouldLoginSuccessfully() throws Exception {
            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(TEST_USERNAME, TEST_PASSWORD)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                    .andExpect(jsonPath("$.data.role").value("user"));
        }

        @Test
        @DisplayName("管理员登录应成功")
        void shouldLoginAsAdmin() throws Exception {
            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(ADMIN_USERNAME, TEST_PASSWORD)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.role").value("admin"));
        }

        @Test
        @DisplayName("错误密码应登录失败")
        void shouldFailWithWrongPassword() throws Exception {
            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(TEST_USERNAME, "wrongpassword")))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("不存在的用户应返回404")
        void shouldFailWithNonExistentUser() throws Exception {
            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("nonexistent", TEST_PASSWORD)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("空用户名应返回400")
        void shouldFailWithEmptyUsername() throws Exception {
            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("", TEST_PASSWORD)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少验证码字段应返回400")
        void shouldFailWithoutCaptcha() throws Exception {
            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"Test1234\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/user/register")
    class Register {

        @Test
        @DisplayName("注册已存在的用户名应返回409")
        void shouldFailWithExistingUsername() throws Exception {
            mockMvc.perform(post("/api/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson(TEST_USERNAME, "NewPass1234", "NewPass1234")))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("注册用户名太短应返回400")
        void shouldFailWithShortUsername() throws Exception {
            mockMvc.perform(post("/api/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson("ab", "Test1234", "Test1234")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("两次密码不一致应返回400")
        void shouldFailWithMismatchedPassword() throws Exception {
            mockMvc.perform(post("/api/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson("newuser123", "Test1234", "Different567")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("注册新用户应成功")
        void shouldRegisterSuccessfully() throws Exception {
            mockMvc.perform(post("/api/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson("newuser456", "Test1234", "Test1234")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/captcha")
    class Captcha {

        @Test
        @DisplayName("验证码接口应返回响应")
        void shouldReturnCaptchaResponse() throws Exception {
            mockMvc.perform(get("/api/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.captchaKey").isNotEmpty())
                    .andExpect(jsonPath("$.data.captchaImage").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/user/logout")
    class Logout {

        @Test
        @DisplayName("登录后退出应成功")
        void shouldLogoutAfterLogin() throws Exception {
            String token = loginAndGetToken();

            mockMvc.perform(post("/api/user/logout")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/user/validate")
    class ValidateToken {

        @Test
        @DisplayName("有效Token应返回用户信息")
        void shouldValidateValidToken() throws Exception {
            String token = loginAndGetToken();

            mockMvc.perform(get("/api/user/validate")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
        }

        @Test
        @DisplayName("无效Token应返回401")
        void shouldRejectInvalidToken() throws Exception {
            mockMvc.perform(get("/api/user/validate")
                            .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
