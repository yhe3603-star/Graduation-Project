package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.constant.TargetType;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.service.BrowseHistoryService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PopularityAsyncService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "非遗知识库", description = "侗乡医药知识查询、检索、浏览统计")
@RestController
@RequestMapping("/api/knowledge")
@Validated
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService service;
    private final BrowseHistoryService browseHistoryService;
    private final PopularityAsyncService popularityAsyncService;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String therapy,
            @RequestParam(required = false) String disease,
            @RequestParam(required = false) String herb) {
        Page<Knowledge> pageResult = service.advancedSearchPaged(keyword, therapy, disease, herb, sortBy, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(required = false) String therapy,
            @RequestParam(required = false) String disease,
            @RequestParam(required = false) String herb,
            @RequestParam(defaultValue = "popularity") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<Knowledge> pageResult = service.advancedSearchPaged(keyword, therapy, disease, herb, sortBy, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/{id}")
    public R<Knowledge> detail(@PathVariable @NotNull(message = "ID不能为空") Integer id) {
        try { popularityAsyncService.incrementKnowledgeViewAndPopularity(id); } catch (Exception e) { log.debug("更新浏览量失败", e); }
        Knowledge knowledge = service.getDetailWithRelated(id);
        if (knowledge == null) {
            throw BusinessException.notFound("知识条目不存在");
        }
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId != null) {
            try {
                browseHistoryService.record(userId, TargetType.KNOWLEDGE.getValue(), id);
            } catch (Exception e) {
                log.debug("记录浏览历史失败", e);
            }
        }
        return R.ok(knowledge);
    }

    @PostMapping("/{id}/view")
    @RateLimit(value = 10, key = "knowledge_view")
    public R<String> incrementView(@PathVariable @NotNull(message = "ID不能为空") Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }

    @PostMapping("/favorite/{id}")
    @SaCheckLogin
    public R<String> favorite(@PathVariable @NotNull(message = "ID不能为空") Integer id) {
        service.addFavorite(SecurityUtils.getCurrentUserId(), id);
        return R.ok("收藏成功");
    }

    @PostMapping("/feedback")
    @SaCheckLogin
    public R<String> feedback(
            @RequestParam @NotNull(message = "知识ID不能为空") Integer knowledgeId,
            @RequestParam @NotBlank(message = "反馈内容不能为空") String content) {
        if (content.length() > 500) {
            throw BusinessException.badRequest("反馈内容长度不能超过500字符");
        }
        service.submitFeedback(knowledgeId, content);
        return R.ok("反馈已提交，感谢您的支持！");
    }
}
