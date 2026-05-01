package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "收藏管理", description = "用户收藏增删查")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService service;

    @PostMapping("/{targetType}/{targetId}")
    @SaCheckLogin
    public R<String> add(@PathVariable String targetType, @PathVariable Integer targetId) {
        service.addFavorite(SecurityUtils.getCurrentUserId(), targetType, targetId);
        return R.ok("收藏成功");
    }

    @DeleteMapping("/{targetType}/{targetId}")
    @SaCheckLogin
    public R<String> remove(@PathVariable String targetType, @PathVariable Integer targetId) {
        service.removeFavorite(SecurityUtils.getCurrentUserId(), targetType, targetId);
        return R.ok("取消收藏成功");
    }

    @GetMapping("/my")
    @SaCheckLogin
    public R<List<Map<String, Object>>> myFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        int safeSize = Math.min(Math.max(size != null ? size : 20, 1), 100);
        List<Map<String, Object>> all = service.getMyFavorites(SecurityUtils.getCurrentUserId());
        int start = (Math.max(page, 1) - 1) * safeSize;
        int end = Math.min(start + safeSize, all.size());
        return R.ok(start < all.size() ? all.subList(start, end) : List.of());
    }
}
