package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.dto.CommentAddDTO;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.entity.Comment;
import com.dongmedicine.service.CommentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("评论Controller测试")
class CommentControllerTest {

    @Mock
    private CommentService service;

    @InjectMocks
    private CommentController commentController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private CommentDTO testCommentDTO;
    private Page<CommentDTO> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testCommentDTO = new CommentDTO();
        testCommentDTO.setId(1);
        testCommentDTO.setUserId(1);
        testCommentDTO.setUsername("testUser");
        testCommentDTO.setTargetType("plant");
        testCommentDTO.setTargetId(10);
        testCommentDTO.setContent("这是一条测试评论");
        testCommentDTO.setLikes(0);
        testCommentDTO.setReplyCount(0);
        testCommentDTO.setCreateTime(LocalDateTime.now());

        testPage = new Page<>(1, 20, 1);
        testPage.setRecords(Arrays.asList(testCommentDTO));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    // ==================== 列表查询 tests ====================

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.listApprovedPaged(eq("plant"), eq(10), eq(1), eq(20))).thenReturn(testPage);

        R<Map<String, Object>> result = commentController.list("plant", 10, 1, 20);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).listApprovedPaged("plant", 10, 1, 20);
    }

    @Test
    @DisplayName("列表查询 - 自定义分页参数")
    void testList_CustomPagination() {
        Page<CommentDTO> customPage = new Page<>(2, 5, 10);
        customPage.setRecords(Arrays.asList(testCommentDTO));
        when(service.listApprovedPaged(eq("knowledge"), eq(5), eq(2), eq(5))).thenReturn(customPage);

        R<Map<String, Object>> result = commentController.list("knowledge", 5, 2, 5);

        assertEquals(200, result.getCode());
        verify(service).listApprovedPaged("knowledge", 5, 2, 5);
    }

    // ==================== 全部评论列表 tests ====================

    @Test
    @DisplayName("全部评论列表 - 成功")
    void testListAll_Success() {
        when(service.pageAllApproved(eq(1), eq(12))).thenReturn(testPage);

        R<Map<String, Object>> result = commentController.listAll(1, 12);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).pageAllApproved(1, 12);
    }

    @Test
    @DisplayName("全部评论列表 - 自定义分页")
    void testListAll_CustomPagination() {
        Page<CommentDTO> customPage = new Page<>(3, 20, 50);
        customPage.setRecords(Arrays.asList(testCommentDTO));
        when(service.pageAllApproved(eq(3), eq(20))).thenReturn(customPage);

        R<Map<String, Object>> result = commentController.listAll(3, 20);

        assertEquals(200, result.getCode());
        verify(service).pageAllApproved(3, 20);
    }

    // ==================== 发表评论 tests ====================

    @Test
    @DisplayName("发表评论 - 已登录成功")
    void testAdd_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        securityUtilsMock.when(SecurityUtils::getCurrentUsernameOrNull).thenReturn("testUser");
        doNothing().when(service).addComment(any(Comment.class));

        CommentAddDTO dto = new CommentAddDTO();
        dto.setTargetType("plant");
        dto.setTargetId(10);
        dto.setContent("这是一条新评论");

        R<String> result = commentController.add(dto);

        assertEquals(200, result.getCode());
        assertEquals("评论发表成功", result.getData());
        verify(service).addComment(any(Comment.class));
    }

    @Test
    @DisplayName("发表评论 - 带回复信息")
    void testAdd_WithReplyInfo() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        securityUtilsMock.when(SecurityUtils::getCurrentUsernameOrNull).thenReturn("testUser");
        doNothing().when(service).addComment(any(Comment.class));

        CommentAddDTO dto = new CommentAddDTO();
        dto.setTargetType("plant");
        dto.setTargetId(10);
        dto.setContent("这是回复评论");
        dto.setReplyToId(5);
        dto.setReplyToUserId(2);
        dto.setReplyToUsername("otherUser");

        R<String> result = commentController.add(dto);

        assertEquals(200, result.getCode());
        verify(service).addComment(argThat(comment ->
                comment.getReplyToId().equals(5) &&
                comment.getReplyToUserId().equals(2) &&
                "otherUser".equals(comment.getReplyToUsername())
        ));
    }

    @Test
    @DisplayName("发表评论 - 未登录抛异常")
    void testAdd_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(com.dongmedicine.common.exception.ErrorCode.LOGIN_REQUIRED, "请先登录"));

        CommentAddDTO dto = new CommentAddDTO();
        dto.setTargetType("plant");
        dto.setTargetId(10);
        dto.setContent("这是一条新评论");

        assertThrows(BusinessException.class, () -> commentController.add(dto));
        verify(service, never()).addComment(any(Comment.class));
    }

    // ==================== 我的评论 tests ====================

    @Test
    @DisplayName("我的评论 - 已登录成功")
    void testMyComments_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        when(service.listByUserIdPaged(eq(1), eq(1), eq(20))).thenReturn(testPage);

        R<Map<String, Object>> result = commentController.myComments(1, 20);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).listByUserIdPaged(1, 1, 20);
    }

    @Test
    @DisplayName("我的评论 - 自定义分页")
    void testMyComments_CustomPagination() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1);
        Page<CommentDTO> customPage = new Page<>(2, 5, 8);
        customPage.setRecords(Arrays.asList(testCommentDTO));
        when(service.listByUserIdPaged(eq(1), eq(2), eq(5))).thenReturn(customPage);

        R<Map<String, Object>> result = commentController.myComments(2, 5);

        assertEquals(200, result.getCode());
        verify(service).listByUserIdPaged(1, 2, 5);
    }

    @Test
    @DisplayName("我的评论 - 未登录抛异常")
    void testMyComments_NotLoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                .thenThrow(new BusinessException(com.dongmedicine.common.exception.ErrorCode.LOGIN_REQUIRED, "请先登录"));

        assertThrows(BusinessException.class, () -> commentController.myComments(1, 20));
        verify(service, never()).listByUserIdPaged(anyInt(), anyInt(), anyInt());
    }
}
