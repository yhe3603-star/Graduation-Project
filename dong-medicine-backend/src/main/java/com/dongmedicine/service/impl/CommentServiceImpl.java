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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Comment comment) {
        if (comment.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (!StringUtils.hasText(comment.getUsername())) {
            throw new RuntimeException("用户名不能为空");
        }
        if (!StringUtils.hasText(comment.getTargetType())) {
            throw new RuntimeException("目标类型不能为空");
        }
        if (comment.getTargetId() == null) {
            throw new RuntimeException("目标ID不能为空");
        }
        if (!StringUtils.hasText(comment.getContent())) {
            throw new RuntimeException("评论内容不能为空");
        }

        comment.setStatus("approved");
        comment.setLikes(0);
        comment.setReplyCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
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
            throw new RuntimeException("评论不存在");
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
    public List<CommentDTO> listAllDTO(String status, Integer page, Integer size) {
        int safePage = page == null ? 1 : Math.max(page, 1);
        int safeSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreatedAt);
        if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status)) {
            wrapper.eq(Comment::getStatus, status);
        }
        Page<Comment> pageResult = page(new Page<>(safePage, safeSize), wrapper);
        return pageResult.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> listAllApproved() {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, "approved")
                .orderByDesc(Comment::getCreatedAt)).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
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
