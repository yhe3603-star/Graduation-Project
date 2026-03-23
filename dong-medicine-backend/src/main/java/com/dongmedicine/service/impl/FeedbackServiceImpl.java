package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.entity.User;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.mapper.FeedbackMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Feedback> listByStatus(String status) {
        return list(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, status)
                .orderByDesc(Feedback::getCreateTime));
    }

    @Override
    public List<Feedback> listByUserId(Integer userId) {
        return list(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, userId)
                .orderByDesc(Feedback::getCreateTime));
    }

    @Override
    public void submitFeedback(Feedback feedback, Integer userId) {
        feedback.setUserId(userId);
        feedback.setStatus("pending");
        feedback.setCreateTime(LocalDateTime.now());
        
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                feedback.setUserName(user.getUsername());
            }
        }
        save(feedback);
    }

    @Override
    public void replyFeedback(Integer id, String reply) {
        Feedback feedback = getById(id);
        if (feedback == null) {
            throw BusinessException.notFound("反馈不存在");
        }
        feedback.setReply(reply);
        feedback.setStatus("resolved");
        updateById(feedback);
    }

    @Override
    public long countByStatus(String status) {
        return count(new LambdaQueryWrapper<Feedback>().eq(Feedback::getStatus, status));
    }
}
