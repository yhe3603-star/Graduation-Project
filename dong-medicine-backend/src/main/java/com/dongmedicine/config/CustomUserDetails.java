package com.dongmedicine.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

/**
 * 自定义 UserDetails，包含 userId 和角色信息
 */
public class CustomUserDetails extends User {

    private final Integer userId;

    public CustomUserDetails(String username, Integer userId, String role) {
        super(username, "", Collections.singletonList(
                new SimpleGrantedAuthority(role != null && role.startsWith("ROLE_")
                        ? role : "ROLE_" + (role != null ? role.toUpperCase() : "USER"))));
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}
