package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.CommentAddDTO;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.entity.Comment;
import com.dongmedicine.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Validated
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public R<String> add(@Valid @RequestBody CommentAddDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.error("请先登录");
        try {
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setUsername(SecurityUtils.getCurrentUsername());
            comment.setTargetType(dto.getTargetType());
            comment.setTargetId(dto.getTargetId());
            comment.setContent(dto.getContent());
            comment.setReplyToId(dto.getReplyToId());
            comment.setReplyToUserId(dto.getReplyToUserId());
            comment.setReplyToUsername(dto.getReplyToUsername());
            comment.setLikes(0);
            comment.setReplyCount(0);
            service.addComment(comment);
            return R.ok("评论发表成功");
        } catch (Exception e) {
            return R.error("评论提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/{targetType}/{targetId}")
    public R<List<CommentDTO>> list(
            @PathVariable @NotBlank(message = "目标类型不能为空") String targetType,
            @PathVariable @NotNull(message = "目标ID不能为空") Integer targetId) {
        try {
            return R.ok(service.listApproved(targetType, targetId));
        } catch (Exception e) {
            return R.error("获取评论列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/all")
    public R<List<CommentDTO>> listAll() {
        try {
            return R.ok(service.listAllApproved());
        } catch (Exception e) {
            return R.error("获取评论列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public R<List<CommentDTO>> myComments() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.error("请先登录");
        try {
            return R.ok(service.listByUserId(userId));
        } catch (Exception e) {
            return R.error("获取我的评论失败: " + e.getMessage());
        }
    }
}
