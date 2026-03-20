package com.dongmedicine.config.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class SensitiveDataConverter extends MessageConverter {
    
    private static final List<SensitivePattern> SENSITIVE_PATTERNS = new ArrayList<>();
    
    static {
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(password\\s*[=:]\\s*)[\"']?[^\\s,\"'}]+[\"']?", Pattern.CASE_INSENSITIVE),
            "$1******"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(token\\s*[=:]\\s*)[\"']?[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*[\"']?", Pattern.CASE_INSENSITIVE),
            "$1********"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(Bearer\\s+)[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*", Pattern.CASE_INSENSITIVE),
            "$1********"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(Authorization\\s*[=:]\\s*Bearer\\s+)[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*", Pattern.CASE_INSENSITIVE),
            "$1********"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(api[_-]?key\\s*[=:]\\s*)[\"']?[A-Za-z0-9_-]{20,}[\"']?", Pattern.CASE_INSENSITIVE),
            "$1********"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(secret\\s*[=:]\\s*)[\"']?[A-Za-z0-9_-]{10,}[\"']?", Pattern.CASE_INSENSITIVE),
            "$1********"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(\\d{3})\\d{4}(\\d{4})", Pattern.CASE_INSENSITIVE),
            "$1****$2"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("([a-zA-Z0-9._%+-]+)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),
            "$1@***.***"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(\\d{4})\\d{10}(\\d{4})"),
            "$1**********$2"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(newPassword\\s*[=:]\\s*)[\"']?[^\\s,\"'}]+[\"']?", Pattern.CASE_INSENSITIVE),
            "$1******"
        ));
        
        SENSITIVE_PATTERNS.add(new SensitivePattern(
            Pattern.compile("(currentPassword\\s*[=:]\\s*)[\"']?[^\\s,\"'}]+[\"']?", Pattern.CASE_INSENSITIVE),
            "$1******"
        ));
    }
    
    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null || message.isEmpty()) {
            return message;
        }
        return maskSensitiveData(message);
    }
    
    private String maskSensitiveData(String message) {
        String maskedMessage = message;
        for (SensitivePattern pattern : SENSITIVE_PATTERNS) {
            maskedMessage = pattern.getPattern().matcher(maskedMessage).replaceAll(pattern.getReplacement());
        }
        return maskedMessage;
    }
    
    private static class SensitivePattern {
        private final Pattern pattern;
        private final String replacement;
        
        public SensitivePattern(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public String getReplacement() {
            return replacement;
        }
    }
}
