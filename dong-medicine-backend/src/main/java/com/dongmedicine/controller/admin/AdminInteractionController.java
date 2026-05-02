package com.dongmedicine.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.dto.FeedbackReplyDTO;
import com.dongmedicine.entity.Comment;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.service.CommentService;
import com.dongmedicine.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "后台管理-互动", description = "管理员反馈与评论管理")
@RestController
@RequestMapping("/api/admin")
@Validated
@SaCheckRole("admin")
@RequiredArgsConstructor
public class AdminInteractionController {

    private final FeedbackService feedbackService;
    private final CommentService commentService;

    // ========== 反馈 ==========

    @GetMapping("/feedback")
    public R<Map<String, Object>> listFeedback(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .orderByDesc(Feedback::getCreatedAt);
        if (!"all".equalsIgnoreCase(status)) {
            wrapper.eq(Feedback::getStatus, status);
        }
        Page<Feedback> pageResult = feedbackService.page(PageUtils.getPage(page, size), wrapper);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PutMapping("/feedback/{id}/reply")
    public R<String> replyFeedback(@PathVariable @NotNull Integer id, @RequestBody @Valid FeedbackReplyDTO dto) {
        feedbackService.replyFeedback(id, dto.getReply().trim());
        return R.ok("回复成功");
    }

    @DeleteMapping("/feedback/{id}")
    public R<String> deleteFeedback(@PathVariable @NotNull Integer id) {
        feedbackService.removeById(id);
        return R.ok("删除反馈成功");
    }

    // ========== 评论 ==========

    @GetMapping("/comments")
    public R<Map<String, Object>> listComments(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<CommentDTO> pageResult = commentService.pageAllDTO(status, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PutMapping("/comments/{id}/approve")
    public R<String> approveComment(@PathVariable @NotNull Integer id) {
        commentService.approveComment(id);
        return R.ok("审核通过");
    }

    @PutMapping("/comments/{id}/reject")
    public R<String> rejectComment(@PathVariable @NotNull Integer id) {
        commentService.rejectComment(id);
        return R.ok("已拒绝");
    }

    @DeleteMapping("/comments/{id}")
    public R<String> deleteComment(@PathVariable @NotNull Integer id) {
        commentService.removeById(id);
        return R.ok("删除评论成功");
    }
}
