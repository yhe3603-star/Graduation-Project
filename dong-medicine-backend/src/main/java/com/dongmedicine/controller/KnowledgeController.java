package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService service;

    @GetMapping("/list")
    public R<List<Knowledge>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String sortBy) {
        return R.ok(service.advancedSearch(null, null, null, sortBy));
    }

    @GetMapping("/search")
    public R<List<Knowledge>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String therapy,
            @RequestParam(required = false) String disease,
            @RequestParam(defaultValue = "popularity") String sortBy) {
        return R.ok(service.advancedSearch(keyword, therapy, disease, sortBy));
    }

    @GetMapping("/{id}")
    public R<Knowledge> detail(@PathVariable Integer id) {
        return R.ok(service.getDetailWithRelated(id));
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }

    @PostMapping("/favorite/{id}")
    public R<String> favorite(@PathVariable Integer id, @RequestParam Integer userId) {
        service.addFavorite(userId, id);
        return R.ok("收藏成功");
    }

    @PostMapping("/feedback")
    public R<String> feedback(@RequestParam Integer knowledgeId, @RequestParam String content) {
        service.submitFeedback(knowledgeId, content);
        return R.ok("反馈已提交，感谢您的支持！");
    }
}
