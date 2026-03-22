package com.dongmedicine.common;

import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.config.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Integer getCurrentUserId() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    public static CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        return auth.getPrincipal() instanceof CustomUserDetails ? (CustomUserDetails) auth.getPrincipal() : null;
    }

    public static String getCurrentUsername() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    public static String getCurrentUserRole() {
        CustomUserDetails user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse(RoleConstants.ROLE_USER);
    }

    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return RoleConstants.ROLE_ADMIN.equals(role) || RoleConstants.ROLE_ADMIN.equalsIgnoreCase(role);
    }
}
