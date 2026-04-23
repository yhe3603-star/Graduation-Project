package com.dongmedicine.common;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.constant.RoleConstants;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Integer getCurrentUserId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return StpUtil.getLoginIdAsInt();
    }

    public static String getCurrentUsername() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object username = StpUtil.getSession().get("username");
        return username != null ? username.toString() : null;
    }

    public static String getCurrentUserRole() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object role = StpUtil.getSession().get("role");
        return role != null ? role.toString() : RoleConstants.ROLE_USER;
    }

    public static boolean isAuthenticated() {
        return StpUtil.isLogin();
    }

    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return RoleConstants.ROLE_ADMIN.equals(role) || RoleConstants.ROLE_ADMIN.equalsIgnoreCase(role);
    }
}
