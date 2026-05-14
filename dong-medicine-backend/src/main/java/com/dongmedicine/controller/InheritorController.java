package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.constant.TargetType;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.service.BrowseHistoryService;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.PopularityAsyncService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "传承人风采", description = "侗乡医药非遗传承人信息查询与展示")
@Slf4j
@RestController
@RequestMapping("/api/inheritors")
@Validated
@RequiredArgsConstructor
public class InheritorController {

    private final InheritorService service;
    private final BrowseHistoryService browseHistoryService;
    private final PopularityAsyncService popularityAsyncService;

    @Operation(summary = "获取传承人列表")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "12") @RequestParam(defaultValue = "12") Integer size,
            @Parameter(name = "level", description = "传承人级别") @RequestParam(required = false) String level,
            @Parameter(name = "keyword", description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(name = "sortBy", description = "排序方式", example = "name") @RequestParam(defaultValue = "name") String sortBy) {
        Page<Inheritor> pageResult = service.advancedSearchPaged(keyword, level, sortBy, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "搜索传承人")
    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @Parameter(name = "keyword", description = "搜索关键词") @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "12") @RequestParam(defaultValue = "12") Integer size) {
        Page<Inheritor> pageResult = service.searchPaged(keyword, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "获取传承人详情")
    @GetMapping("/{id}")
    public R<Inheritor> detail(@Parameter(name = "id", description = "传承人ID") @PathVariable Integer id) {
        try { popularityAsyncService.incrementInheritorViewAndPopularity(id); } catch (Exception e) { log.debug("更新浏览量失败", e); }
        Inheritor in = service.getDetailWithExtras(id);
        if (in == null) throw BusinessException.notFound("传承人不存在");
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId != null) {
            try {
                browseHistoryService.record(userId, TargetType.INHERITOR.getValue(), id);
            } catch (Exception e) {
                log.debug("记录浏览历史失败", e);
            }
        }
        return R.ok(in);
    }

    @Operation(summary = "增加传承人浏览量")
    @PostMapping("/{id}/view")
    @RateLimit(value = 10, key = "inheritor_view")
    public R<String> incrementView(@Parameter(name = "id", description = "传承人ID") @PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
