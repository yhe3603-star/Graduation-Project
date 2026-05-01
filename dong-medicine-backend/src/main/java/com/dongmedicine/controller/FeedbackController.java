package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.FeedbackDTO;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户反馈", description = "用户意见反馈与回复")
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public R<String> submit(@Valid @RequestBody FeedbackDTO dto) {
        Feedback feedback = new Feedback();
        feedback.setType(dto.getType());
        feedback.setTitle(dto.getTitle());
        feedback.setContent(dto.getContent());
        feedback.setContact(dto.getContact());
        feedbackService.submitFeedback(feedback, SecurityUtils.getCurrentUserIdOrNull());
        return R.ok("提交成功");
    }

    @GetMapping("/my")
    @SaCheckLogin
    public R<List<Feedback>> myFeedbacks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        int safeSize = Math.min(Math.max(size != null ? size : 20, 1), 100);
        List<Feedback> all = feedbackService.listByUserId(SecurityUtils.getCurrentUserId());
        int start = (Math.max(page, 1) - 1) * safeSize;
        int end = Math.min(start + safeSize, all.size());
        return R.ok(start < all.size() ? all.subList(start, end) : List.of());
    }

    @GetMapping("/stats")
    public R<Object> stats() {
        long total = feedbackService.count();
        long pending = feedbackService.countByStatus("pending");
        long resolved = feedbackService.countByStatus("resolved");
        return R.ok(java.util.Map.of(
            "total", total,
            "pending", pending,
            "resolved", resolved
        ));
    }
}
