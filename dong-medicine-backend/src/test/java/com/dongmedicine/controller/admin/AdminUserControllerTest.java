package com.dongmedicine.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台管理-用户Controller测试")
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    @Test
    @DisplayName("用户列表 - 成功")
    void testListUsers_Success() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPasswordHash("secret123");
        Page<User> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Arrays.asList(user));
        when(userService.page(any())).thenReturn(pageResult);

        R<Map<String, Object>> result = adminUserController.listUsers(1, 20);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        // Verify password is cleared
        assertNull(user.getPasswordHash());
        verify(userService).page(any());
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void testDeleteUser_Success() {
        doNothing().when(userService).deleteUser(1);

        R<String> result = adminUserController.deleteUser(1);

        assertEquals(200, result.getCode());
        assertEquals("删除用户成功", result.getData());
        verify(userService).deleteUser(1);
    }

    @Test
    @DisplayName("更新用户角色 - 成功")
    void testUpdateUserRole_Success() {
        doNothing().when(userService).updateUserRole(1, "admin");

        R<String> result = adminUserController.updateUserRole(1, "admin");

        assertEquals(200, result.getCode());
        assertEquals("角色更新成功", result.getData());
        verify(userService).updateUserRole(1, "admin");
    }

    @Test
    @DisplayName("封禁用户 - 成功")
    void testBanUser_Success() {
        doNothing().when(userService).banUser(1, "违规操作");

        R<String> result = adminUserController.banUser(1, "违规操作");

        assertEquals(200, result.getCode());
        assertEquals("用户已被封禁", result.getData());
        verify(userService).banUser(1, "违规操作");
    }

    @Test
    @DisplayName("封禁用户 - 无原因")
    void testBanUser_NoReason() {
        doNothing().when(userService).banUser(1, null);

        R<String> result = adminUserController.banUser(1, null);

        assertEquals(200, result.getCode());
        assertEquals("用户已被封禁", result.getData());
        verify(userService).banUser(1, null);
    }

    @Test
    @DisplayName("解封用户 - 成功")
    void testUnbanUser_Success() {
        doNothing().when(userService).unbanUser(1);

        R<String> result = adminUserController.unbanUser(1);

        assertEquals(200, result.getCode());
        assertEquals("用户已解封", result.getData());
        verify(userService).unbanUser(1);
    }
}
