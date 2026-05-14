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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

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

    @Operation(summary = "获取知识库列表")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "12") @RequestParam(defaultValue = "12") Integer size,
            @Parameter(name = "sortBy", description = "排序方式") @RequestParam(required = false) String sortBy,
            @Parameter(name = "keyword", description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(name = "therapy", description = "疗法类型") @RequestParam(required = false) String therapy,
            @Parameter(name = "disease", description = "疾病类型") @RequestParam(required = false) String disease,
            @Parameter(name = "herb", description = "草药名称") @RequestParam(required = false) String herb) {
        Page<Knowledge> pageResult = service.advancedSearchPaged(keyword, therapy, disease, herb, sortBy, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "搜索知识库")
    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @Parameter(name = "keyword", description = "搜索关键词") @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @Parameter(name = "therapy", description = "疗法类型") @RequestParam(required = false) String therapy,
            @Parameter(name = "disease", description = "疾病类型") @RequestParam(required = false) String disease,
            @Parameter(name = "herb", description = "草药名称") @RequestParam(required = false) String herb,
            @Parameter(name = "sortBy", description = "排序方式", example = "popularity") @RequestParam(defaultValue = "popularity") String sortBy,
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "12") @RequestParam(defaultValue = "12") Integer size) {
        Page<Knowledge> pageResult = service.advancedSearchPaged(keyword, therapy, disease, herb, sortBy, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "获取知识详情")
    @GetMapping("/{id}")
    public R<Knowledge> detail(@Parameter(name = "id", description = "知识ID") @PathVariable @NotNull(message = "ID不能为空") Integer id) {
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

    @Operation(summary = "增加知识浏览量")
    @PostMapping("/{id}/view")
    @RateLimit(value = 10, key = "knowledge_view")
    public R<String> incrementView(@Parameter(name = "id", description = "知识ID") @PathVariable @NotNull(message = "ID不能为空") Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }

    @Operation(summary = "收藏知识")
    @PostMapping("/favorite/{id}")
    @SaCheckLogin
    public R<String> favorite(@Parameter(name = "id", description = "知识ID") @PathVariable @NotNull(message = "ID不能为空") Integer id) {
        service.addFavorite(SecurityUtils.getCurrentUserId(), id);
        return R.ok("收藏成功");
    }

    @Operation(summary = "提交知识反馈")
    @PostMapping("/feedback")
    @SaCheckLogin
    public R<String> feedback(
            @Parameter(name = "knowledgeId", description = "知识ID") @RequestParam @NotNull(message = "知识ID不能为空") Integer knowledgeId,
            @Parameter(name = "content", description = "反馈内容") @RequestParam @NotBlank(message = "反馈内容不能为空") String content) {
        if (content.length() > 500) {
            throw BusinessException.badRequest("反馈内容长度不能超过500字符");
        }
        service.submitFeedback(knowledgeId, content);
        return R.ok("反馈已提交，感谢您的支持！");
    }
}
