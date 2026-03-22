package com.dongmedicine.common.constant;

public final class RoleConstants {

    private RoleConstants() {
    }

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static boolean isValid(String role) {
        return ROLE_USER.equals(role) || ROLE_ADMIN.equals(role);
    }

    public static String normalize(String role) {
        if (role == null || role.isEmpty()) {
            return ROLE_USER;
        }
        String normalized = role.toLowerCase().trim();
        return isValid(normalized) ? normalized : ROLE_USER;
    }
}
