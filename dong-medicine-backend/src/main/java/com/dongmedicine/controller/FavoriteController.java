package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService service;

    @PostMapping("/{targetType}/{targetId}")
    @SaCheckLogin
    public R<String> add(@PathVariable String targetType, @PathVariable Integer targetId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        service.addFavorite(userId, targetType, targetId);
        return R.ok("收藏成功");
    }

    @DeleteMapping("/{targetType}/{targetId}")
    @SaCheckLogin
    public R<String> remove(@PathVariable String targetType, @PathVariable Integer targetId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        service.removeFavorite(userId, targetType, targetId);
        return R.ok("取消收藏成功");
    }

    @GetMapping("/my")
    @SaCheckLogin
    public R<List<Map<String, Object>>> myFavorites() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return R.ok(service.getMyFavorites(userId));
    }
}
