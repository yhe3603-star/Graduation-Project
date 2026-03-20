package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Feedback;

import java.util.List;

public interface FeedbackService extends IService<Feedback> {
    List<Feedback> listByStatus(String status);
    List<Feedback> listByUserId(Integer userId);
    void submitFeedback(Feedback feedback, Integer userId);
    void replyFeedback(Integer id, String reply);
    long countByStatus(String status);
}
