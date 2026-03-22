package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.FeedbackMapper;
import com.dongmedicine.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("反馈服务测试")
class FeedbackServiceImplTest {

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    private Feedback testFeedback;
    private User testUser;

    @BeforeEach
    void setUp() {
        testFeedback = new Feedback();
        testFeedback.setId(1);
        testFeedback.setUserId(1);
        testFeedback.setUserName("testuser");
        testFeedback.setType("建议");
        testFeedback.setTitle("测试反馈标题");
        testFeedback.setContent("测试反馈内容，这是一个测试反馈的内容。");
        testFeedback.setStatus("pending");

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setRole("user");
    }

    @Test
    @DisplayName("按状态查询 - 成功")
    void testListByStatus_Success() {
        when(feedbackMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testFeedback));

        List<Feedback> result = feedbackService.listByStatus("pending");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("pending", result.get(0).getStatus());
    }

    @Test
    @DisplayName("按状态查询 - 无结果返回空列表")
    void testListByStatus_EmptyResult() {
        when(feedbackMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Feedback> result = feedbackService.listByStatus("resolved");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("按用户ID查询 - 成功")
    void testListByUserId_Success() {
        when(feedbackMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testFeedback));

        List<Feedback> result = feedbackService.listByUserId(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    @DisplayName("提交反馈 - 登录用户成功")
    void testSubmitFeedback_LoggedInUser_Success() {
        when(userMapper.selectById(1)).thenReturn(testUser);
        doNothing().when(feedbackMapper).insert(any(Feedback.class));

        Feedback newFeedback = new Feedback();
        newFeedback.setType("问题");
        newFeedback.setTitle("新问题");
        newFeedback.setContent("新问题描述");

        assertDoesNotThrow(() -> feedbackService.submitFeedback(newFeedback, 1));

        assertEquals("pending", newFeedback.getStatus());
        assertEquals(1, newFeedback.getUserId());
        assertEquals("testuser", newFeedback.getUserName());
        verify(feedbackMapper).insert(any(Feedback.class));
    }

    @Test
    @DisplayName("提交反馈 - 游客用户成功")
    void testSubmitFeedback_GuestUser_Success() {
        doNothing().when(feedbackMapper).insert(any(Feedback.class));

        Feedback newFeedback = new Feedback();
        newFeedback.setType("建议");
        newFeedback.setTitle("游客建议");
        newFeedback.setContent("游客建议内容");

        assertDoesNotThrow(() -> feedbackService.submitFeedback(newFeedback, null));

        assertEquals("pending", newFeedback.getStatus());
        assertNull(newFeedback.getUserId());
        verify(feedbackMapper).insert(any(Feedback.class));
    }

    @Test
    @DisplayName("回复反馈 - 成功")
    void testReplyFeedback_Success() {
        when(feedbackMapper.selectById(1)).thenReturn(testFeedback);
        doNothing().when(feedbackMapper).updateById(any(Feedback.class));

        assertDoesNotThrow(() -> feedbackService.replyFeedback(1, "这是管理员回复"));

        verify(feedbackMapper).updateById(any(Feedback.class));
    }

    @Test
    @DisplayName("回复反馈 - 反馈不存在抛出异常")
    void testReplyFeedback_NotFound_ThrowsException() {
        when(feedbackMapper.selectById(999)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> feedbackService.replyFeedback(999, "回复内容"));

        assertEquals("反馈不存在", exception.getMessage());
    }

    @Test
    @DisplayName("统计状态数量 - 成功")
    void testCountByStatus_Success() {
        when(feedbackMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        long count = feedbackService.countByStatus("pending");

        assertEquals(5L, count);
        verify(feedbackMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("统计状态数量 - 无结果返回0")
    void testCountByStatus_NoResult() {
        when(feedbackMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        long count = feedbackService.countByStatus("nonexistent");

        assertEquals(0L, count);
    }
}
