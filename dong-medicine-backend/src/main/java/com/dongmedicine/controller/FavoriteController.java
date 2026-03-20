package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService service;

    @PostMapping("/{targetType}/{targetId}")
    @PreAuthorize("isAuthenticated()")
    public R<String> add(@PathVariable String targetType, @PathVariable Integer targetId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.error("请先登录");
        try {
            service.addFavorite(userId, targetType, targetId);
            return R.ok("收藏成功");
        } catch (Exception e) {
            return R.error("收藏失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{targetType}/{targetId}")
    @PreAuthorize("isAuthenticated()")
    public R<String> remove(@PathVariable String targetType, @PathVariable Integer targetId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.error("请先登录");
        try {
            service.removeFavorite(userId, targetType, targetId);
            return R.ok("取消收藏成功");
        } catch (Exception e) {
            return R.error("取消收藏失败: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public R<List<Map<String, Object>>> myFavorites() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.error("请先登录");
        return R.ok(service.getMyFavorites(userId));
    }
}
