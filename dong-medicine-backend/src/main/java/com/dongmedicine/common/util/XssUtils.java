package com.dongmedicine.common.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class XssUtils {

    private static final Pattern[] DANGEROUS_PATTERNS = {
        Pattern.compile("<script", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script[^>]*/>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("&#x?[0-9a-f]+;?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<object", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<embed", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<link", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<meta", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<base", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<form", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<input", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<button", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<textarea", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<select", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<style", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("srcdoc\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("xlink:href\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("xmlns\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<svg", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<math", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<audio", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<video", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<source", Pattern.CASE_INSENSITIVE)
    };

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(select|insert|update|delete|drop|create|alter|truncate|exec|execute|xp_|sp_)\\b|" +
        "(\\b(union)\\b.*\\b(select|insert|update|delete)\\b)|" +
        "(--|#|/\\*|\\*/|;|\\|\\|)|" +
        "(\\b(or|and)\\b\\s+\\d+\\s*=\\s*\\d+)|" +
        "(\\b(or|and)\\b\\s+['\"]\\w+['\"]\\s*=\\s*['\"]\\w+['\"]))"
    );

    private XssUtils() {
    }

    public static String sanitize(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    public static String sanitizeHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        return input.replaceAll("(?i)<script[^>]*>.*?</script>", "")
                .replaceAll("(?i)<script[^>]*/>", "")
                .replaceAll("(?i)javascript:", "")
                .replaceAll("(?i)on\\s*\\s*=", "onxxx=")
                .replaceAll("(?i)eval\\s*\\(", "evalxxx(")
                .replaceAll("(?i)expression\\s*\\(", "expressionxxx(")
                .replaceAll("(?i)vbscript:", "")
                .replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "")
                .replaceAll("(?i)<object[^>]*>.*?</object>", "")
                .replaceAll("(?i)<embed[^>]*/>", "")
                .replaceAll("(?i)<link[^>]*/>", "")
                .replaceAll("(?i)<meta[^>]*/>", "");
    }

    public static boolean containsXss(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSqlInjection(String input) {
        return StringUtils.hasText(input) && SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public static String stripHtmlTags(String input) {
        return StringUtils.hasText(input) ? input.replaceAll("<[^>]*>", "") : input;
    }

    public static String escapeJavaScript(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String sanitizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String sanitized = url.trim();
        if (containsXss(sanitized) ||
            sanitized.toLowerCase().startsWith("javascript:") ||
            sanitized.toLowerCase().startsWith("data:") ||
            sanitized.toLowerCase().startsWith("vbscript:")) {
            return "";
        }
        if (!sanitized.startsWith("/") &&
            !sanitized.toLowerCase().startsWith("http://") &&
            !sanitized.toLowerCase().startsWith("https://")) {
            return "";
        }
        return sanitized;
    }

    public static String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        String sanitized = fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.+|\\.+$", "");

        if (sanitized.length() > 255) {
            int lastDot = sanitized.lastIndexOf('.');
            String ext = lastDot > 0 ? sanitized.substring(lastDot) : "";
            String base = lastDot > 0 ? sanitized.substring(0, lastDot) : sanitized;
            sanitized = base.substring(0, 250 - ext.length()) + ext;
        }
        return sanitized;
    }

    public static String sanitizeForLog(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        String sanitized = input.replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return sanitized.length() > 1000 ? sanitized.substring(0, 1000) + "..." : sanitized;
    }

    public static boolean isSafeInput(String input, int maxLength) {
        return !StringUtils.hasText(input) || (input.length() <= maxLength && !containsXss(input));
    }
}
