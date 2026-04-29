package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.FeedbackMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.mq.producer.FeedbackProducer;
import com.dongmedicine.mq.producer.NotificationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeedbackServiceImplTest {

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FeedbackProducer feedbackProducer;

    @Mock
    private NotificationProducer notificationProducer;

    private FeedbackServiceImpl feedbackService;

    @BeforeEach
    void setUp() throws Exception {
        feedbackService = new FeedbackServiceImpl(userMapper, feedbackProducer, notificationProducer);
        setBaseMapper(feedbackService, feedbackMapper);
    }

    private void setBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Test
    @DisplayName("提交反馈 - 匿名用户")
    void submitFeedbackAnonymous() {
        doNothing().when(feedbackProducer).sendFeedback(any(Feedback.class));

        Feedback feedback = new Feedback();
        feedback.setType("suggestion");
        feedback.setTitle("测试反馈");
        feedback.setContent("反馈内容");

        assertDoesNotThrow(() -> {
            feedbackService.submitFeedback(feedback, null);
        });

        assertEquals("anonymous", feedback.getUserName());
        assertEquals("pending", feedback.getStatus());
    }

    @Test
    @DisplayName("提交反馈 - 登录用户")
    void submitFeedbackWithUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        when(userMapper.selectById(1)).thenReturn(user);
        doNothing().when(feedbackProducer).sendFeedback(any(Feedback.class));

        Feedback feedback = new Feedback();
        feedback.setType("bug");
        feedback.setTitle("Bug反馈");
        feedback.setContent("Bug内容");

        assertDoesNotThrow(() -> {
            feedbackService.submitFeedback(feedback, 1);
        });

        assertEquals("testuser", feedback.getUserName());
    }

    @Test
    @DisplayName("提交反馈 - RabbitMQ失败降级为同步保存")
    void submitFeedbackFallbackToSync() {
        doThrow(new RuntimeException("RabbitMQ unavailable")).when(feedbackProducer).sendFeedback(any(Feedback.class));
        when(feedbackMapper.insert(any(Feedback.class))).thenReturn(1);

        Feedback feedback = new Feedback();
        feedback.setType("suggestion");
        feedback.setTitle("降级测试");
        feedback.setContent("降级内容");

        assertDoesNotThrow(() -> {
            feedbackService.submitFeedback(feedback, null);
        });
    }

    @Test
    @DisplayName("按状态查询反馈 - pending")
    void listByStatusPending() {
        when(feedbackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Feedback> result = feedbackService.listByStatus("pending");
        assertNotNull(result);
    }

    @Test
    @DisplayName("按用户查询反馈")
    void listByUserId() {
        when(feedbackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Feedback> result = feedbackService.listByUserId(1);
        assertNotNull(result);
    }

    @Test
    @DisplayName("按状态计数")
    void countByStatus() {
        when(feedbackMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        long count = feedbackService.countByStatus("pending");
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("回复反馈 - 正常回复")
    void replyFeedback() {
        Feedback existing = new Feedback();
        existing.setId(1);
        existing.setUserId(2);
        existing.setStatus("pending");
        when(feedbackMapper.selectById(1)).thenReturn(existing);
        when(feedbackMapper.updateById(any(Feedback.class))).thenReturn(1);
        doNothing().when(notificationProducer).sendFeedbackReplyNotification(anyInt(), anyString(), anyString());

        assertDoesNotThrow(() -> {
            feedbackService.replyFeedback(1, "已修复");
        });
    }

    @Test
    @DisplayName("回复反馈 - 反馈不存在应抛异常")
    void replyFeedbackNotFound() {
        when(feedbackMapper.selectById(999)).thenReturn(null);

        assertThrows(Exception.class, () -> {
            feedbackService.replyFeedback(999, "回复内容");
        });
    }
}
