package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.service.KnowledgeService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@Validated
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService service;

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
        Knowledge knowledge = service.getDetailWithRelated(id);
        if (knowledge == null) {
            throw BusinessException.notFound("知识条目不存在");
        }
        return R.ok(knowledge);
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable @NotNull(message = "ID不能为空") Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }

    @PostMapping("/favorite/{id}")
    @SaCheckLogin
    public R<String> favorite(@PathVariable @NotNull(message = "ID不能为空") Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        service.addFavorite(userId, id);
        return R.ok("收藏成功");
    }

    @PostMapping("/feedback")
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
