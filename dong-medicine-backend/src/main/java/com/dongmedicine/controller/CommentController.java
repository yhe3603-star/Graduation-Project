package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.CommentAddDTO;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.entity.Comment;
import com.dongmedicine.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "用户评论", description = "用户评论发布、查询与管理")
@RestController
@RequestMapping("/api/comments")
@Validated
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @PostMapping
    @SaCheckLogin
    public R<String> add(@Valid @RequestBody CommentAddDTO dto) {
        Comment comment = new Comment();
        comment.setUserId(SecurityUtils.getCurrentUserId());
        comment.setUsername(SecurityUtils.getCurrentUsernameOrNull());
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
    }

    @GetMapping("/list/{targetType}/{targetId}")
    public R<Map<String, Object>> list(
            @PathVariable @NotBlank(message = "目标类型不能为空") String targetType,
            @PathVariable @NotNull(message = "目标ID不能为空") Integer targetId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<CommentDTO> pageResult = service.listApprovedPaged(targetType, targetId, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/list/all")
    public R<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<CommentDTO> pageResult = service.pageAllApproved(page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/my")
    @SaCheckLogin
    public R<Map<String, Object>> myComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<CommentDTO> pageResult = service.listByUserIdPaged(SecurityUtils.getCurrentUserId(), page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }
}
