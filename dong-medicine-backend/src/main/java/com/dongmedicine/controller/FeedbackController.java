package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public R<String> submit(@RequestBody Feedback feedback) {
        try {
            feedbackService.submitFeedback(feedback, SecurityUtils.getCurrentUserId());
            return R.ok("提交成功");
        } catch (Exception e) {
            return R.error("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public R<List<Feedback>> myFeedbacks() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userId == null ? List.of() : feedbackService.listByUserId(userId));
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(Map.of(
            "total", feedbackService.count(),
            "processed", feedbackService.countByStatus("resolved"),
            "pending", feedbackService.countByStatus("pending")
        ));
    }
}
