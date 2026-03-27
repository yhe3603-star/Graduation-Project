package com.dongmedicine.common.util;

import java.util.Set;
import java.util.regex.Pattern;

public final class SensitiveDataUtils {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "passwd", "pwd",
            "secret", "token", "accessToken", "refreshToken",
            "apiKey", "apiSecret",
            "idCard", "idCardNumber", "idcard",
            "phone", "mobile", "telephone",
            "email", "mail",
            "bankCard", "bankAccount",
            "address", "addr",
            "realName", "truename",
            "jwt", "authorization"
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9_.+-]+)@([a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d{9})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("([1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx])");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{16,19})");
    
    private static final Pattern SQL_SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[_-]?key)\\s*=\\s*['\"][^'\"]+['\"]|" +
            "(?i)(password|passwd|pwd|secret|token|api[_-]?key)\\s*=\\s*[^\\s,;)]+|" +
            "(?i)values\\s*\\([^)]*['\"][^'\"]+['\"][^)]*\\)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern JWT_PATTERN = Pattern.compile(
            "eyJ[a-zA-Z0-9_-]*\\.eyJ[a-zA-Z0-9_-]*\\.[a-zA-Z0-9_-]*",
            Pattern.CASE_INSENSITIVE
    );

    private SensitiveDataUtils() {}

    public static String mask(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        if (fieldName != null && isSensitiveField(fieldName)) {
            return maskValue(value);
        }
        
        return autoMask(value);
    }

    public static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lowerName = fieldName.toLowerCase();
        return SENSITIVE_FIELDS.stream().anyMatch(lowerName::contains);
    }

    public static String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        int len = value.length();
        if (len <= 2) {
            return "*".repeat(len);
        } else if (len <= 6) {
            return value.charAt(0) + "*".repeat(len - 2) + value.charAt(len - 1);
        } else {
            return value.substring(0, 2) + "*".repeat(len - 4) + value.substring(len - 2);
        }
    }

    public static String autoMask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text;
        result = EMAIL_PATTERN.matcher(result).replaceAll(m -> 
                m.group(1).substring(0, Math.min(2, m.group(1).length())) + "***@" + m.group(2));
        result = PHONE_PATTERN.matcher(result).replaceAll(m -> 
                m.group(1).substring(0, 3) + "****" + m.group(1).substring(7));
        result = ID_CARD_PATTERN.matcher(result).replaceAll(m -> 
                m.group(1).substring(0, 6) + "********" + m.group(1).substring(14));
        result = BANK_CARD_PATTERN.matcher(result).replaceAll(m -> 
                m.group(1).substring(0, 4) + "****" + m.group(1).substring(m.group(1).length() - 4));
        result = SQL_SENSITIVE_PATTERN.matcher(result).replaceAll(m -> {
            String matched = m.group();
            return matched.replaceAll("['\"][^'\"]+['\"]", "'***'").replaceAll("=\\s*[^\\s,;)]+", "= ***");
        });
        result = JWT_PATTERN.matcher(result).replaceAll(m -> {
            String token = m.group();
            if (token.length() > 20) {
                return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
            }
            return "***";
        });
        
        return result;
    }

    public static String maskJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        String result = json;
        for (String field : SENSITIVE_FIELDS) {
            result = maskJsonField(result, field);
        }
        return result;
    }

    private static String maskJsonField(String json, String fieldName) {
        Pattern stringPattern = Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"", 
                Pattern.CASE_INSENSITIVE);
        Pattern numberPattern = Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*(\\d+)", 
                Pattern.CASE_INSENSITIVE);
        
        String result = stringPattern.matcher(json).replaceAll(m -> 
                "\"" + fieldName + "\":\"" + maskValue(m.group(1)) + "\"");
        result = numberPattern.matcher(result).replaceAll(m -> 
                "\"" + fieldName + "\":\"****\"");
        
        return result;
    }
}
