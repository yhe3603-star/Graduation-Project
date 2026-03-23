package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
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
import java.util.Map;

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
        if (userId == null) throw BusinessException.unauthorized("请先登录");
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
    }

    @GetMapping("/list/{targetType}/{targetId}")
    public R<List<CommentDTO>> list(
            @PathVariable @NotBlank(message = "目标类型不能为空") String targetType,
            @PathVariable @NotNull(message = "目标ID不能为空") Integer targetId) {
        return R.ok(service.listApproved(targetType, targetId));
    }

    @GetMapping("/list/all")
    public R<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<CommentDTO> pageResult = service.pageAllApproved(page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public R<List<CommentDTO>> myComments() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw BusinessException.unauthorized("请先登录");
        return R.ok(service.listByUserId(userId));
    }
}
