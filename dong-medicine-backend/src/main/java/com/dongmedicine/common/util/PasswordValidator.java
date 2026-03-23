package com.dongmedicine.common.util;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private PasswordValidator() {
    }

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 50;
    
    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    public static ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.failure("密码不能为空");
        }

        if (password.length() < MIN_LENGTH) {
            return ValidationResult.failure("密码长度不能少于" + MIN_LENGTH + "位");
        }

        if (password.length() > MAX_LENGTH) {
            return ValidationResult.failure("密码长度不能超过" + MAX_LENGTH + "位");
        }

        if (WHITESPACE_PATTERN.matcher(password).find()) {
            return ValidationResult.failure("密码不能包含空格");
        }

        boolean hasLetter = LETTER_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();

        if (!hasLetter || !hasDigit) {
            return ValidationResult.failure("密码必须包含字母和数字");
        }

        int strength = calculateStrength(password);
        String strengthLabel = getStrengthLabel(strength);

        return ValidationResult.success(strength, strengthLabel);
    }

    public static boolean isValid(String password) {
        return validate(password).isValid();
    }

    private static int calculateStrength(String password) {
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;

        if (LETTER_PATTERN.matcher(password).find()) score++;
        if (Pattern.compile("[a-z]").matcher(password).find() && 
            Pattern.compile("[A-Z]").matcher(password).find()) score++;

        if (DIGIT_PATTERN.matcher(password).find()) score++;
        if (Pattern.compile("[0-9]{2,}").matcher(password).find()) score++;

        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score++;
        if (Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{2,}").matcher(password).find()) score++;

        return Math.min(score, 5);
    }

    private static String getStrengthLabel(int strength) {
        if (strength <= 1) return "弱";
        if (strength <= 2) return "一般";
        if (strength <= 3) return "中等";
        if (strength <= 4) return "强";
        return "非常强";
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final int strength;
        private final String strengthLabel;

        private ValidationResult(boolean valid, String message, int strength, String strengthLabel) {
            this.valid = valid;
            this.message = message;
            this.strength = strength;
            this.strengthLabel = strengthLabel;
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message, 0, null);
        }

        public static ValidationResult success(int strength, String strengthLabel) {
            return new ValidationResult(true, "密码符合要求", strength, strengthLabel);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public int getStrength() {
            return strength;
        }

        public String getStrengthLabel() {
            return strengthLabel;
        }
    }
}
