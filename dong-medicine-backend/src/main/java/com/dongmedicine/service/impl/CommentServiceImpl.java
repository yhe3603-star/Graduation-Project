package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.entity.Comment;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.CommentMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.CommentService;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Comment comment) {
        if (comment.getUserId() == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        if (!StringUtils.hasText(comment.getUsername())) {
            throw BusinessException.badRequest("用户名不能为空");
        }
        if (!StringUtils.hasText(comment.getTargetType())) {
            throw BusinessException.badRequest("目标类型不能为空");
        }
        if (comment.getTargetId() == null) {
            throw BusinessException.badRequest("目标ID不能为空");
        }
        if (!StringUtils.hasText(comment.getContent())) {
            throw BusinessException.badRequest("评论内容不能为空");
        }

        comment.setStatus("approved");
        comment.setLikes(0);
        comment.setReplyCount(0);
        save(comment);
    }

    @Override
    public List<CommentDTO> listApproved(String targetType, Integer targetId) {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getStatus, "approved")
                .orderByDesc(Comment::getCreatedAt)).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> listByUserId(Integer userId) {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreatedAt)).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void approveComment(Integer commentId) {
        updateCommentStatus(commentId, "approved");
    }

    @Override
    public void rejectComment(Integer commentId) {
        updateCommentStatus(commentId, "rejected");
    }

    private void updateCommentStatus(Integer commentId, String status) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw BusinessException.notFound("评论不存在");
        }
        comment.setStatus(status);
        updateById(comment);
    }

    @Override
    public List<CommentDTO> listAllDTO() {
        return list(new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreatedAt)).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<CommentDTO> pageAllDTO(String status, Integer page, Integer size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreatedAt);
        if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status)) {
            wrapper.eq(Comment::getStatus, status);
        }
        Page<Comment> entityPage = page(PageUtils.getPage(page, size), wrapper);
        Page<CommentDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    @Override
    public List<CommentDTO> listAllApproved() {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, "approved")
                .orderByDesc(Comment::getCreatedAt)).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<CommentDTO> pageAllApproved(Integer page, Integer size) {
        Page<Comment> entityPage = page(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getStatus, "approved")
                        .orderByDesc(Comment::getCreatedAt));
        Page<CommentDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUserId());
        dto.setUsername(comment.getUsername());
        dto.setTargetType(comment.getTargetType());
        dto.setTargetId(comment.getTargetId());
        dto.setContent(comment.getContent());
        dto.setReplyToId(comment.getReplyToId());
        dto.setReplyToUserId(comment.getReplyToUserId());
        dto.setReplyToUsername(comment.getReplyToUsername());
        dto.setLikes(comment.getLikes());
        dto.setReplyCount(comment.getReplyCount());
        dto.setStatus(comment.getStatus());
        dto.setCreateTime(comment.getCreatedAt());
        dto.setUpdateTime(comment.getUpdatedAt());
        return dto;
    }
}
