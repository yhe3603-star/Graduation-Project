package com.dongmedicine.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("密码验证器测试")
class PasswordValidatorTest {

    @Test
    @DisplayName("验证 - null密码")
    void testValidate_Null() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate(null);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("不能为空"));
    }

    @Test
    @DisplayName("验证 - 空密码")
    void testValidate_Empty() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("验证 - 太短")
    void testValidate_TooShort() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Ab1");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("8"));
    }

    @Test
    @DisplayName("验证 - 太长")
    void testValidate_TooLong() {
        String longPwd = "A1" + "b".repeat(49);
        PasswordValidator.ValidationResult result = PasswordValidator.validate(longPwd);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("50"));
    }

    @Test
    @DisplayName("验证 - 只有字母")
    void testValidate_OnlyLetters() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("abcdefgh");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("字母和数字"));
    }

    @Test
    @DisplayName("验证 - 只有数字")
    void testValidate_OnlyDigits() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("12345678");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("验证 - 包含空格")
    void testValidate_ContainsWhitespace() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Abc 1234");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("空格"));
    }

    @Test
    @DisplayName("验证 - 有效密码")
    void testValidate_Valid() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Abc12345");
        assertTrue(result.isValid());
        assertNotNull(result.getStrengthLabel());
        assertTrue(result.getStrength() >= 1);
    }

    @Test
    @DisplayName("验证 - 强密码")
    void testValidate_Strong() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Abc12345!@#");
        assertTrue(result.isValid());
        assertTrue(result.getStrength() >= 3);
    }

    @Test
    @DisplayName("isValid - 有效密码返回true")
    void testIsValid_Valid() {
        assertTrue(PasswordValidator.isValid("Abc12345"));
    }

    @Test
    @DisplayName("isValid - 无效密码返回false")
    void testIsValid_Invalid() {
        assertFalse(PasswordValidator.isValid("abc"));
    }

    @Test
    @DisplayName("强度标签 - 弱密码")
    void testStrengthLabel_Weak() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("Ab123456");
        assertTrue(result.isValid());
        assertNotNull(result.getStrengthLabel());
    }
}
