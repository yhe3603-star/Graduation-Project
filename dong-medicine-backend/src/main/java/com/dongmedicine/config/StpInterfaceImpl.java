package com.dongmedicine.config;

import cn.dev33.satoken.stp.StpInterface;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    private static final long ROLE_CACHE_TTL = 5 * 60 * 1000;
    private final ConcurrentHashMap<String, CacheEntry> roleCache = new ConcurrentHashMap<>();

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String key = loginId.toString();
        CacheEntry cached = roleCache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.roles;
        }

        List<String> roleList = new ArrayList<>();
        try {
            Integer userId = Integer.parseInt(key);
            User user = userService.getUserInfo(userId);
            if (user != null && user.getRole() != null) {
                roleList.add(user.getRole());
            }
        } catch (Exception e) {
            // ignore invalid user IDs
        }

        roleCache.put(key, new CacheEntry(roleList, System.currentTimeMillis() + ROLE_CACHE_TTL));
        return roleList;
    }

    private static class CacheEntry {
        final List<String> roles;
        final long expireAt;

        CacheEntry(List<String> roles, long expireAt) {
            this.roles = roles;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }
}
