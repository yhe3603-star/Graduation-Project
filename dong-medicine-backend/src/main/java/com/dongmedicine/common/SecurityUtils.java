package com.dongmedicine.common;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Integer getCurrentUserId() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录");
        }
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof Integer) {
                return (Integer) loginId;
            }
            return Integer.parseInt(loginId.toString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "登录凭证无效");
        }
    }

    public static Integer getCurrentUserIdOrNull() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof Integer) {
                return (Integer) loginId;
            }
            return Integer.parseInt(loginId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentUsername() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录");
        }
        Object username = StpUtil.getSession().get("username");
        if (username == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "登录凭证无效");
        }
        return username.toString();
    }

    public static String getCurrentUsernameOrNull() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object username = StpUtil.getSession().get("username");
        return username != null ? username.toString() : null;
    }

    public static String getCurrentUserRole() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED, "请先登录");
        }
        Object role = StpUtil.getSession().get("role");
        return role != null ? role.toString() : RoleConstants.ROLE_USER;
    }

    public static String getCurrentUserRoleOrNull() {
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
        if (!StpUtil.isLogin()) {
            return false;
        }
        Object role = StpUtil.getSession().get("role");
        String roleStr = role != null ? role.toString() : null;
        return RoleConstants.ROLE_ADMIN.equals(roleStr) || RoleConstants.ROLE_ADMIN.equalsIgnoreCase(roleStr);
    }
}
