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

@Tag(name = "浏览历史", description = "用户浏览历史记录")
@Slf4j
@RestController
@RequestMapping("/api/browse-history")
@RequiredArgsConstructor
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    @GetMapping("/my")
    @SaCheckLogin
    public R<List<Map<String, Object>>> myHistory(
            @RequestParam(defaultValue = "50") int limit) {
        Integer userId = SecurityUtils.getCurrentUserId();
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return R.ok(browseHistoryService.getMyHistory(userId, safeLimit));
    }
}
