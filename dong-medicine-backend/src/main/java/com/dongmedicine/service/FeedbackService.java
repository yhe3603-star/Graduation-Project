package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Feedback;

import java.util.List;

public interface FeedbackService extends IService<Feedback> {
    List<Feedback> listByStatus(String status);
    List<Feedback> listByUserId(Integer userId);
    Page<Feedback> listByUserIdPaged(Integer userId, Integer page, Integer size);
    void submitFeedback(Feedback feedback, Integer userId);
    void replyFeedback(Integer id, String reply);
    long countByStatus(String status);
}
