package com.dongmedicine.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorCode 回归测试")
class ErrorCodeTest {

    @Nested
    @DisplayName("getByCode")
    class GetByCode {
        @Test
        @DisplayName("已知错误码应返回对应枚举")
        void knownCodes() {
            assertEquals(ErrorCode.USER_NOT_FOUND, ErrorCode.getByCode(1001));
            assertEquals(ErrorCode.USER_ALREADY_EXISTS, ErrorCode.getByCode(1002));
            assertEquals(ErrorCode.PASSWORD_WRONG, ErrorCode.getByCode(1003));
            assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.getByCode(2001));
            assertEquals(ErrorCode.PARAM_ERROR, ErrorCode.getByCode(3001));
            assertEquals(ErrorCode.FILE_UPLOAD_ERROR, ErrorCode.getByCode(4001));
            assertEquals(ErrorCode.SYSTEM_ERROR, ErrorCode.getByCode(9001));
        }

        @Test
        @DisplayName("未知错误码应返回UNKNOWN_ERROR")
        void unknownCode() {
            assertEquals(ErrorCode.UNKNOWN_ERROR, ErrorCode.getByCode(99999));
            assertEquals(ErrorCode.UNKNOWN_ERROR, ErrorCode.getByCode(-1));
        }

        @Test
        @DisplayName("0应返回SUCCESS")
        void zeroCode() {
            assertEquals(ErrorCode.SUCCESS, ErrorCode.getByCode(0));
        }
    }

    @Nested
    @DisplayName("getCode/getMessage")
    class GetCodeMessage {
        @Test
        @DisplayName("应返回正确的错误码")
        void code() {
            assertEquals(1001, ErrorCode.USER_NOT_FOUND.getCode());
            assertEquals(2001, ErrorCode.RESOURCE_NOT_FOUND.getCode());
            assertEquals(3001, ErrorCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("应返回非空错误消息")
        void message() {
            assertNotNull(ErrorCode.USER_NOT_FOUND.getMessage());
            assertNotNull(ErrorCode.SYSTEM_ERROR.getMessage());
            assertFalse(ErrorCode.USER_NOT_FOUND.getMessage().isEmpty());
        }
    }

    @Nested
    @DisplayName("错误码分类")
    class ErrorCodeCategories {
        @Test
        @DisplayName("用户相关错误码应在1xxx范围")
        void userErrors() {
            assertTrue(ErrorCode.USER_NOT_FOUND.getCode() >= 1000 && ErrorCode.USER_NOT_FOUND.getCode() < 2000);
            assertTrue(ErrorCode.PASSWORD_WRONG.getCode() >= 1000 && ErrorCode.PASSWORD_WRONG.getCode() < 2000);
            assertTrue(ErrorCode.TOKEN_EXPIRED.getCode() >= 1000 && ErrorCode.TOKEN_EXPIRED.getCode() < 2000);
        }

        @Test
        @DisplayName("资源相关错误码应在2xxx范围")
        void resourceErrors() {
            assertTrue(ErrorCode.RESOURCE_NOT_FOUND.getCode() >= 2000 && ErrorCode.RESOURCE_NOT_FOUND.getCode() < 3000);
            assertTrue(ErrorCode.PLANT_NOT_FOUND.getCode() >= 2000 && ErrorCode.PLANT_NOT_FOUND.getCode() < 3000);
        }

        @Test
        @DisplayName("参数相关错误码应在3xxx范围")
        void paramErrors() {
            assertTrue(ErrorCode.PARAM_ERROR.getCode() >= 3000 && ErrorCode.PARAM_ERROR.getCode() < 4000);
        }

        @Test
        @DisplayName("文件相关错误码应在4xxx范围")
        void fileErrors() {
            assertTrue(ErrorCode.FILE_UPLOAD_ERROR.getCode() >= 4000 && ErrorCode.FILE_UPLOAD_ERROR.getCode() < 5000);
        }

        @Test
        @DisplayName("系统错误码应在9xxx范围")
        void systemErrors() {
            assertTrue(ErrorCode.SYSTEM_ERROR.getCode() >= 9000 && ErrorCode.SYSTEM_ERROR.getCode() < 10000);
        }
    }

    @Nested
    @DisplayName("错误码唯一性")
    class Uniqueness {
        @Test
        @DisplayName("所有错误码应唯一")
        void allCodesUnique() {
            java.util.Set<Integer> codes = new java.util.HashSet<>();
            for (ErrorCode errorCode : ErrorCode.values()) {
                assertTrue(codes.add(errorCode.getCode()),
                        "Duplicate code: " + errorCode.getCode() + " for " + errorCode);
            }
        }
    }
}
