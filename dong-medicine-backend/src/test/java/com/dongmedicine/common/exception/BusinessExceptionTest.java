package com.dongmedicine.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("业务异常类测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("notFound - 创建资源未找到异常")
    void testNotFound() {
        BusinessException ex = BusinessException.notFound("用户不存在");
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("用户不存在", ex.getMessage());
        assertEquals(2001, ex.getCode());
    }

    @Test
    @DisplayName("unauthorized - 创建未授权异常")
    void testUnauthorized() {
        BusinessException ex = BusinessException.unauthorized("请先登录");
        assertEquals(ErrorCode.TOKEN_INVALID, ex.getErrorCode());
        assertEquals("请先登录", ex.getMessage());
    }

    @Test
    @DisplayName("badRequest - 创建参数错误异常")
    void testBadRequest() {
        BusinessException ex = BusinessException.badRequest("参数不能为空");
        assertEquals(ErrorCode.PARAM_ERROR, ex.getErrorCode());
        assertEquals("参数不能为空", ex.getMessage());
        assertEquals(3001, ex.getCode());
    }

    @Test
    @DisplayName("conflict - 创建资源冲突异常")
    void testConflict() {
        BusinessException ex = BusinessException.conflict("已经收藏过了");
        assertEquals(ErrorCode.DUPLICATE_OPERATION, ex.getErrorCode());
        assertEquals("已经收藏过了", ex.getMessage());
    }

    @Test
    @DisplayName("forbidden - 创建权限不足异常")
    void testForbidden() {
        BusinessException ex = BusinessException.forbidden("无权访问");
        assertEquals(ErrorCode.PERMISSION_DENIED, ex.getErrorCode());
        assertEquals("无权访问", ex.getMessage());
    }

    @Test
    @DisplayName("userNotFound - 创建用户未找到异常")
    void testUserNotFound() {
        BusinessException ex = BusinessException.userNotFound();
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("userAlreadyExists - 创建用户已存在异常")
    void testUserAlreadyExists() {
        BusinessException ex = BusinessException.userAlreadyExists();
        assertEquals(ErrorCode.USER_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("passwordWrong - 创建密码错误异常")
    void testPasswordWrong() {
        BusinessException ex = BusinessException.passwordWrong();
        assertEquals(ErrorCode.PASSWORD_WRONG, ex.getErrorCode());
    }

    @Test
    @DisplayName("使用错误码构造")
    void testConstructor_WithErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.SYSTEM_ERROR);
        assertEquals(ErrorCode.SYSTEM_ERROR, ex.getErrorCode());
        assertEquals(ErrorCode.SYSTEM_ERROR.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("使用错误码和自定义消息构造")
    void testConstructor_WithErrorCodeAndMessage() {
        BusinessException ex = new BusinessException(ErrorCode.PARAM_ERROR, "自定义消息");
        assertEquals(ErrorCode.PARAM_ERROR, ex.getErrorCode());
        assertEquals("自定义消息", ex.getMessage());
    }

    @Test
    @DisplayName("使用错误码、消息和原因构造")
    void testConstructor_WithCause() {
        Throwable cause = new RuntimeException("原始异常");
        BusinessException ex = new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误", cause);
        assertEquals(cause, ex.getCause());
        assertEquals("系统错误", ex.getMessage());
    }

    @Test
    @DisplayName("异常是RuntimeException的子类")
    void testIsRuntimeException() {
        BusinessException ex = BusinessException.badRequest("test");
        assertTrue(ex instanceof RuntimeException);
    }
}
