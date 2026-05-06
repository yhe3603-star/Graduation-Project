package com.dongmedicine.common.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TargetType {
    PLANT("plant"),
    KNOWLEDGE("knowledge"),
    INHERITOR("inheritor"),
    RESOURCE("resource"),
    QA("qa");

    private final String value;

    TargetType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TargetType fromValue(String value) {
        for (TargetType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的目标类型: " + value);
    }

    /**
     * Compile-time string constants for use in switch-case labels.
     */
    public static final class Constants {
        public static final String PLANT = "plant";
        public static final String KNOWLEDGE = "knowledge";
        public static final String INHERITOR = "inheritor";
        public static final String RESOURCE = "resource";
        public static final String QA = "qa";

        private Constants() {}
    }
}
