package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.entity.Comment;
import java.util.List;

public interface CommentService extends IService<Comment> {
    void addComment(Comment comment);
    List<CommentDTO> listApproved(String targetType, Integer targetId);
    List<CommentDTO> listByUserId(Integer userId);
    void approveComment(Integer commentId);
    void rejectComment(Integer commentId);
    List<CommentDTO> listAllDTO();
    List<CommentDTO> listAllDTO(String status, Integer page, Integer size);
    List<CommentDTO> listAllApproved();
}
