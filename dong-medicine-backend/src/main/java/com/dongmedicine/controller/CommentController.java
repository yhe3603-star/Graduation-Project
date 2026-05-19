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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "用户评论", description = "用户评论发布、查询与管理")
@RestController
@RequestMapping("/api/comments")
@Validated
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @Operation(summary = "发布评论")
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
        return R.ok(comment.getStatus());
    }

    @Operation(summary = "获取评论列表")
    @GetMapping("/list/{targetType}/{targetId}")
    public R<Map<String, Object>> list(
            @Parameter(name = "targetType", description = "目标类型") @PathVariable @NotBlank(message = "目标类型不能为空") String targetType,
            @Parameter(name = "targetId", description = "目标ID") @PathVariable @NotNull(message = "目标ID不能为空") Integer targetId,
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        Page<CommentDTO> pageResult = service.listApprovedPaged(targetType, targetId, page, size, userId);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "获取全量评论（不分页）")
    @GetMapping("/list/full")
    public R<List<CommentDTO>> listFull() {
        List<CommentDTO> list = service.listAllApproved();
        return R.ok(list);
    }

    @Operation(summary = "获取全部评论")
    @GetMapping("/list/all")
    public R<Map<String, Object>> listAll(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "12") @RequestParam(defaultValue = "12") Integer size) {
        Page<CommentDTO> pageResult = service.pageAllApproved(page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "获取通用评论")
    @GetMapping("/list/general")
    public R<Map<String, Object>> listGeneral(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "1000") @RequestParam(defaultValue = "1000") Integer size) {
        Page<CommentDTO> pageResult = service.listApprovedPaged("general", 0, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "获取我的评论")
    @GetMapping("/my")
    @SaCheckLogin
    public R<Map<String, Object>> myComments(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        Page<CommentDTO> pageResult = service.listByUserIdPaged(SecurityUtils.getCurrentUserId(), page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @Operation(summary = "点赞评论")
    @PostMapping("/{id}/like")
    @SaCheckLogin
    public R<Void> like(@Parameter(name = "id", description = "评论ID") @PathVariable Integer id) {
        service.likeComment(SecurityUtils.getCurrentUserId(), id);
        return R.ok(null);
    }

    @Operation(summary = "取消点赞评论")
    @DeleteMapping("/{id}/like")
    @SaCheckLogin
    public R<Void> unlike(@Parameter(name = "id", description = "评论ID") @PathVariable Integer id) {
        service.unlikeComment(SecurityUtils.getCurrentUserId(), id);
        return R.ok(null);
    }
}
