package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.service.PopularityAsyncService;
import com.dongmedicine.service.QaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "非遗问答", description = "侗乡医药知识问答与用户反馈")
@Slf4j
@RestController
@RequestMapping("/api/qa")
@Validated
@RequiredArgsConstructor
public class QaController {

    private final QaService service;
    private final PopularityAsyncService popularityAsyncService;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<Qa> pageResult = service.advancedSearchPaged(keyword, category, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<Qa> pageResult = service.searchPaged(keyword, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/{id}")
    public R<Qa> detail(@PathVariable Integer id) {
        Qa qa = service.getDetail(id);
        if (qa == null) throw BusinessException.notFound("问答不存在");
        try { popularityAsyncService.incrementQaViewAndPopularity(id); } catch (Exception e) { log.debug("更新浏览量失败", e); }
        return R.ok(qa);
    }

    @PostMapping("/{id}/view")
    @RateLimit(value = 10, key = "qa_view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
