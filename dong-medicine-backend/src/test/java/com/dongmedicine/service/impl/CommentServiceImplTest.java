package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.entity.Comment;
import com.dongmedicine.mapper.CommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("评论服务测试")
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment testComment;

    @BeforeEach
    void setUp() throws Exception {
        setBaseMapper(commentService, commentMapper);

        testComment = new Comment();
        testComment.setId(1);
        testComment.setUserId(1);
        testComment.setUsername("测试用户");
        testComment.setTargetType("plant");
        testComment.setTargetId(1);
        testComment.setContent("这是一条评论");
        testComment.setStatus("approved");
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
        throw new RuntimeException("baseMapper field not found in class hierarchy");
    }

    @Test
    @DisplayName("添加评论 - 用户ID为空抛异常")
    void testAddComment_UserIdNull() {
        testComment.setUserId(null);
        assertThrows(BusinessException.class, () -> commentService.addComment(testComment));
    }

    @Test
    @DisplayName("添加评论 - 用户名为空抛异常")
    void testAddComment_UsernameEmpty() {
        testComment.setUsername("");
        assertThrows(BusinessException.class, () -> commentService.addComment(testComment));
    }

    @Test
    @DisplayName("添加评论 - 目标类型为空抛异常")
    void testAddComment_TargetTypeEmpty() {
        testComment.setTargetType("");
        assertThrows(BusinessException.class, () -> commentService.addComment(testComment));
    }

    @Test
    @DisplayName("添加评论 - 目标ID为空抛异常")
    void testAddComment_TargetIdNull() {
        testComment.setTargetId(null);
        assertThrows(BusinessException.class, () -> commentService.addComment(testComment));
    }

    @Test
    @DisplayName("添加评论 - 内容为空抛异常")
    void testAddComment_ContentEmpty() {
        testComment.setContent("");
        assertThrows(BusinessException.class, () -> commentService.addComment(testComment));
    }

    @Test
    @DisplayName("添加评论 - 成功")
    void testAddComment_Success() {
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);

        assertDoesNotThrow(() -> commentService.addComment(testComment));
        verify(commentMapper).insert(any(Comment.class));
    }

    @Test
    @DisplayName("审核评论 - 评论不存在抛异常")
    void testApproveComment_NotFound() {
        when(commentMapper.selectById(999)).thenReturn(null);
        assertThrows(BusinessException.class, () -> commentService.approveComment(999));
    }

    @Test
    @DisplayName("审核评论 - 成功")
    void testApproveComment_Success() {
        when(commentMapper.selectById(1)).thenReturn(testComment);
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

        assertDoesNotThrow(() -> commentService.approveComment(1));
    }

    @Test
    @DisplayName("拒绝评论 - 成功")
    void testRejectComment_Success() {
        when(commentMapper.selectById(1)).thenReturn(testComment);
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

        assertDoesNotThrow(() -> commentService.rejectComment(1));
    }

    @Test
    @DisplayName("获取已审核评论列表 - 成功")
    void testListApproved_Success() {
        when(commentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testComment));

        List<CommentDTO> result = commentService.listApproved("plant", 1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取用户评论列表 - 成功")
    void testListByUserId_Success() {
        when(commentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testComment));

        List<CommentDTO> result = commentService.listByUserId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
