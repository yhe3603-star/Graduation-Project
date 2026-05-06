package com.dongmedicine.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RTest {

    @Test
    @DisplayName("R.ok - 成功响应")
    void ok_success() {
        R<String> result = R.ok("test data");
        
        assertTrue(result.isSuccess());
        assertEquals("test data", result.getData());
        assertEquals("success", result.getMsg());
    }

    @Test
    @DisplayName("R.ok - 成功响应带列表数据")
    void ok_withList() {
        List<String> list = Arrays.asList("item1", "item2", "item3");
        R<List<String>> result = R.ok(list);
        
        assertTrue(result.isSuccess());
        assertEquals(3, result.getData().size());
    }

    @Test
    @DisplayName("R.ok - 成功响应无数据")
    void ok_noData() {
        R<Void> result = R.ok();
        
        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("R.ok - 成功响应带自定义消息和数据")
    void ok_withMsgAndData() {
        R<String> result = R.ok("操作成功", "test data");
        
        assertTrue(result.isSuccess());
        assertEquals("操作成功", result.getMsg());
        assertEquals("test data", result.getData());
    }

    @Test
    @DisplayName("R.error - 错误响应")
    void error_withMessage() {
        R<Void> result = R.error("操作失败");
        
        assertFalse(result.isSuccess());
        assertEquals("操作失败", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("R.error - 错误响应带错误码")
    void error_withCodeAndMessage() {
        R<Void> result = R.error(400, "参数错误");
        
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMsg());
    }

    @Test
    @DisplayName("R.unauthorized - 未授权响应")
    void unauthorized_test() {
        R<Void> result = R.unauthorized("未登录");
        
        assertFalse(result.isSuccess());
        assertEquals(401, result.getCode());
        assertEquals("未登录", result.getMsg());
    }

    @Test
    @DisplayName("R.forbidden - 禁止访问响应")
    void forbidden_test() {
        R<Void> result = R.forbidden("无权限");
        
        assertFalse(result.isSuccess());
        assertEquals(403, result.getCode());
        assertEquals("无权限", result.getMsg());
    }

    @Test
    @DisplayName("R.notFound - 未找到响应")
    void notFound_test() {
        R<Void> result = R.notFound("资源不存在");
        
        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMsg());
    }

    @Test
    @DisplayName("R.badRequest - 错误请求响应")
    void badRequest_test() {
        R<Void> result = R.badRequest("参数格式错误");
        
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("参数格式错误", result.getMsg());
    }

    @Test
    @DisplayName("isSuccess - 验证成功状态")
    void isSuccess_test() {
        R<String> successResult = R.ok("data");
        R<Void> errorResult = R.error("error");
        
        assertTrue(successResult.isSuccess());
        assertFalse(errorResult.isSuccess());
    }

    @Test
    @DisplayName("R.error - 错误响应带数据")
    void error_withData() {
        R<String> result = R.error("错误信息");
        R<String> resultWithData = R.ok("错误信息", "error data");
        
        assertFalse(result.isSuccess());
        assertEquals("错误信息", result.getMsg());
        assertNull(result.getData());
        assertEquals("error data", resultWithData.getData());
    }
}
