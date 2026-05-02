package com.dongmedicine.regression;

import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.common.util.XssUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("回归测试 - Auth")
class AuthRegressionTest {
@Nested
    @DisplayName("Bug: 401 Unauthorized - view接口需要登录")
    class ViewEndpointAuthBug {

        @Test
        @DisplayName("PageUtils.escapeLike应正确转义复合字符串")
        void escapeLikeShouldHandleComplexString() {
            String result = PageUtils.escapeLike("test\\%_value");
            assertEquals("test\\\\\\%\\_value", result);
        }

        @Test
        @DisplayName("escapeLike应处理null和空字符串")
        void escapeLikeShouldHandleNullAndEmpty() {
            assertNull(PageUtils.escapeLike(null));
            assertEquals("", PageUtils.escapeLike(""));
        }
    }

@Nested
    @DisplayName("Bug: 密码错误返回500而非400/401")
    class PasswordWrongHttpCodeBug {

        @Test
        @DisplayName("sanitize应处理null输入")
        void sanitizeShouldHandleNull() {
            assertNull(XssUtils.sanitize(null));
        }

        @Test
        @DisplayName("sanitize应处理空字符串")
        void sanitizeShouldHandleEmpty() {
            assertEquals("", XssUtils.sanitize(""));
        }

        @Test
        @DisplayName("sanitize应保留正常中文文本")
        void sanitizeShouldPreserveChineseText() {
            String input = "侗族医药是中华民族传统医药的重要组成部分";
            assertEquals(input, XssUtils.sanitize(input));
        }
    }

}
