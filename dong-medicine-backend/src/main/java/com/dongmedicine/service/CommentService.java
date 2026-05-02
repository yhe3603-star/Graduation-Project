package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.entity.Comment;
import java.util.List;

public interface CommentService extends IService<Comment> {
    void addComment(Comment comment);
    List<CommentDTO> listApproved(String targetType, Integer targetId);
    Page<CommentDTO> listApprovedPaged(String targetType, Integer targetId, Integer page, Integer size);
    List<CommentDTO> listByUserId(Integer userId);
    Page<CommentDTO> listByUserIdPaged(Integer userId, Integer page, Integer size);
    void approveComment(Integer commentId);
    void rejectComment(Integer commentId);
    List<CommentDTO> listAllDTO();
    Page<CommentDTO> pageAllDTO(String status, Integer page, Integer size);
    List<CommentDTO> listAllApproved();
    Page<CommentDTO> pageAllApproved(Integer page, Integer size);
}
