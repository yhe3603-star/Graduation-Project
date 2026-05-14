package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "收藏管理", description = "用户收藏增删查")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService service;

    @Operation(summary = "添加收藏")
    @PostMapping("/{targetType}/{targetId}")
    @SaCheckLogin
    public R<String> add(@Parameter(name = "targetType", description = "目标类型") @PathVariable String targetType, @Parameter(name = "targetId", description = "目标ID") @PathVariable Integer targetId) {
        service.addFavorite(SecurityUtils.getCurrentUserId(), targetType, targetId);
        return R.ok("收藏成功");
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{targetType}/{targetId}")
    @SaCheckLogin
    public R<String> remove(@Parameter(name = "targetType", description = "目标类型") @PathVariable String targetType, @Parameter(name = "targetId", description = "目标ID") @PathVariable Integer targetId) {
        service.removeFavorite(SecurityUtils.getCurrentUserId(), targetType, targetId);
        return R.ok("取消收藏成功");
    }

    @Operation(summary = "获取我的收藏列表")
    @GetMapping("/my")
    @SaCheckLogin
    public R<Map<String, Object>> myFavorites(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        Page<Map<String, Object>> pageResult = service.getMyFavoritesPaged(SecurityUtils.getCurrentUserId(), page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }
}
