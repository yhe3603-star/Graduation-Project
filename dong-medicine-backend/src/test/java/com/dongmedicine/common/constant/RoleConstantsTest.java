package com.dongmedicine.common.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleConstantsTest {

    @Test
    @DisplayName("角色常量值测试")
    void testRoleConstants() {
        assertEquals("user", RoleConstants.ROLE_USER);
        assertEquals("admin", RoleConstants.ROLE_ADMIN);
    }

    @Test
    @DisplayName("验证有效角色 - user")
    void testIsValidUser() {
        assertTrue(RoleConstants.isValid("user"));
    }

    @Test
    @DisplayName("验证有效角色 - admin")
    void testIsValidAdmin() {
        assertTrue(RoleConstants.isValid("admin"));
    }

    @Test
    @DisplayName("验证无效角色")
    void testIsValidInvalid() {
        assertFalse(RoleConstants.isValid("superadmin"));
        assertFalse(RoleConstants.isValid("guest"));
        assertFalse(RoleConstants.isValid(""));
        assertFalse(RoleConstants.isValid(null));
    }

    @Test
    @DisplayName("规范化角色 - 有效角色")
    void testNormalizeValid() {
        assertEquals("user", RoleConstants.normalize("user"));
        assertEquals("admin", RoleConstants.normalize("admin"));
        assertEquals("user", RoleConstants.normalize("USER"));
        assertEquals("admin", RoleConstants.normalize("ADMIN"));
        assertEquals("user", RoleConstants.normalize(" User "));
        assertEquals("admin", RoleConstants.normalize(" Admin "));
    }

    @Test
    @DisplayName("规范化角色 - 无效角色返回默认值")
    void testNormalizeInvalid() {
        assertEquals("user", RoleConstants.normalize("superadmin"));
        assertEquals("user", RoleConstants.normalize("guest"));
        assertEquals("user", RoleConstants.normalize(""));
        assertEquals("user", RoleConstants.normalize(null));
    }
}
