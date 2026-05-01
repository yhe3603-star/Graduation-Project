package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.service.BrowseHistoryService;
import com.dongmedicine.service.InheritorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "传承人风采", description = "侗乡医药非遗传承人信息查询与展示")
@RestController
@RequestMapping("/api/inheritors")
@RequiredArgsConstructor
public class InheritorController {

    private final InheritorService service;
    private final BrowseHistoryService browseHistoryService;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "name") String sortBy) {
        Page<Inheritor> pageResult = service.pageByLevel(level, sortBy, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<Inheritor> pageResult = service.searchPaged(keyword, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/{id}")
    public R<Inheritor> detail(@PathVariable Integer id) {
        Inheritor in = service.getDetailWithExtras(id);
        if (in == null) throw BusinessException.notFound("传承人不存在");
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId != null) {
            try {
                browseHistoryService.record(userId, "inheritor", id);
            } catch (Exception e) {
                // Silently ignore
            }
        }
        return R.ok(in);
    }

    @PostMapping("/{id}/view")
    @RateLimit(value = 10, key = "inheritor_view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
