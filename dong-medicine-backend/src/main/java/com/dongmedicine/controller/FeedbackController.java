package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.FeedbackDTO;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        feedbackService.submitFeedback(feedback, SecurityUtils.getCurrentUserId());
        return R.ok("提交成功");
    }

    @GetMapping("/my")
    public R<List<Feedback>> myFeedbacks() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userId == null ? List.of() : feedbackService.listByUserId(userId));
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
