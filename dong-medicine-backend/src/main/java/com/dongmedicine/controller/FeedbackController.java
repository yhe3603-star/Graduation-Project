package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.FeedbackDTO;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户反馈", description = "用户意见反馈与回复")
@Slf4j
@RestController
@RequestMapping("/api/feedback")
@Validated
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @SaCheckLogin
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
    public R<Map<String, Object>> myFeedbacks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Feedback> pageResult = feedbackService.listByUserIdPaged(SecurityUtils.getCurrentUserId(), page, size);
        return R.ok(PageUtils.toMap(pageResult));
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
