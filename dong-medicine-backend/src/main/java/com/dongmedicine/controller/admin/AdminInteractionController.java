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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

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

    @Operation(summary = "获取反馈列表")
    @GetMapping("/feedback")
    public R<Map<String, Object>> listFeedback(
            @Parameter(name = "status", description = "状态筛选", example = "all") @RequestParam(defaultValue = "all") String status,
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .orderByDesc(Feedback::getCreatedAt);
        if (!"all".equalsIgnoreCase(status)) {
            wrapper.eq(Feedback::getStatus, status);
        }
        Page<Feedback> pageResult = feedbackService.page(PageUtils.getPage(page, size), wrapper);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "回复反馈")
    @PutMapping("/feedback/{id}/reply")
    public R<String> replyFeedback(@PathVariable @NotNull Integer id, @RequestBody @Valid FeedbackReplyDTO dto) {
        feedbackService.replyFeedback(id, dto.getReply().trim());
        return R.ok("回复成功");
    }

    @Operation(summary = "删除反馈")
    @DeleteMapping("/feedback/{id}")
    public R<String> deleteFeedback(@PathVariable @NotNull Integer id) {
        feedbackService.removeById(id);
        return R.ok("删除反馈成功");
    }

    // ========== 评论 ==========

    @Operation(summary = "获取评论列表")
    @GetMapping("/comments")
    public R<Map<String, Object>> listComments(
            @Parameter(name = "status", description = "状态筛选", example = "all") @RequestParam(defaultValue = "all") String status,
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        Page<CommentDTO> pageResult = commentService.pageAllDTO(status, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "审核通过评论")
    @PutMapping("/comments/{id}/approve")
    public R<String> approveComment(@PathVariable @NotNull Integer id) {
        commentService.approveComment(id);
        return R.ok("审核通过");
    }

    @Operation(summary = "拒绝评论")
    @PutMapping("/comments/{id}/reject")
    public R<String> rejectComment(@PathVariable @NotNull Integer id) {
        commentService.rejectComment(id);
        return R.ok("已拒绝");
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{id}")
    public R<String> deleteComment(@PathVariable @NotNull Integer id) {
        commentService.removeById(id);
        return R.ok("删除评论成功");
    }
}
