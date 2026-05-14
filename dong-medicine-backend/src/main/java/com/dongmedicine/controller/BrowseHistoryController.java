package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.BrowseHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "浏览历史", description = "用户浏览历史记录")
@Slf4j
@RestController
@RequestMapping("/api/browse-history")
@RequiredArgsConstructor
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    @Operation(summary = "获取我的浏览历史")
    @GetMapping("/my")
    @SaCheckLogin
    public R<List<Map<String, Object>>> myHistory(
            @Parameter(name = "limit", description = "返回记录数上限", example = "50") @RequestParam(defaultValue = "50") int limit) {
        Integer userId = SecurityUtils.getCurrentUserId();
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return R.ok(browseHistoryService.getMyHistory(userId, safeLimit));
    }

    @Operation(summary = "记录浏览历史")
    @PostMapping("/record")
    @SaCheckLogin
    public R<String> record(
            @Parameter(name = "targetType", description = "目标类型") @RequestParam String targetType,
            @Parameter(name = "targetId", description = "目标ID") @RequestParam Integer targetId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        browseHistoryService.record(userId, targetType, targetId);
        return R.ok("ok");
    }
}
