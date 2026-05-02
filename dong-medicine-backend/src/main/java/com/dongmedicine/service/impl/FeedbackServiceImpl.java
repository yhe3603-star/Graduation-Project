package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.entity.User;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.mapper.FeedbackMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.mq.producer.FeedbackProducer;
import com.dongmedicine.mq.producer.NotificationProducer;
import com.dongmedicine.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final UserMapper userMapper;

    @Autowired(required = false)
    private FeedbackProducer feedbackProducer;
    @Autowired(required = false)
    private NotificationProducer notificationProducer;

    @Override
    public List<Feedback> listByStatus(String status) {
        return list(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, status)
                .orderByDesc(Feedback::getCreatedAt));
    }

    @Override
    public List<Feedback> listByUserId(Integer userId) {
        return list(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, userId)
                .orderByDesc(Feedback::getCreatedAt));
    }

    @Override
    public Page<Feedback> listByUserIdPaged(Integer userId, Integer page, Integer size) {
        return page(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getUserId, userId)
                        .orderByDesc(Feedback::getCreatedAt));
    }

    @Override
    public void submitFeedback(Feedback feedback, Integer userId) {
        feedback.setUserId(userId);
        feedback.setStatus("pending");
        
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                feedback.setUserName(user.getUsername());
            }
        } else {
            feedback.setUserName("anonymous");
        }
        
        try {
            if (feedbackProducer != null) {
                feedbackProducer.sendFeedback(feedback);
                log.debug("反馈已通过 RabbitMQ 异步提交, userId={}", userId);
            } else {
                save(feedback);
            }
        } catch (Exception e) {
            log.warn("RabbitMQ 提交反馈失败, 降级为同步保存, error={}", e.getMessage());
            save(feedback);
        }
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
        
        if (feedback.getUserId() != null && notificationProducer != null) {
            try {
                notificationProducer.sendFeedbackReplyNotification(
                        feedback.getUserId(),
                        feedback.getTitle(),
                        reply
                );
                log.debug("反馈回复通知已发送, feedbackId={}, userId={}", id, feedback.getUserId());
            } catch (Exception e) {
                log.warn("发送反馈回复通知失败, feedbackId={}, error={}", id, e.getMessage());
            }
        }
    }

    @Override
    public long countByStatus(String status) {
        return count(new LambdaQueryWrapper<Feedback>().eq(Feedback::getStatus, status));
    }
}
