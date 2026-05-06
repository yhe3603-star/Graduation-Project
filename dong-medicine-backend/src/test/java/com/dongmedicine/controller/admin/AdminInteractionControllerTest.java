package com.dongmedicine.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.dto.CommentDTO;
import com.dongmedicine.dto.FeedbackReplyDTO;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.service.CommentService;
import com.dongmedicine.service.FeedbackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台管理-互动Controller测试")
class AdminInteractionControllerTest {

    @Mock
    private FeedbackService feedbackService;
    @Mock
    private CommentService commentService;

    @InjectMocks
    private AdminInteractionController adminInteractionController;

    // ========== 反馈 ==========

    @Nested
    @DisplayName("反馈管理")
    class FeedbackTests {

        @Test
        @DisplayName("反馈列表 - 全部状态")
        void testListFeedback_AllStatus() {
            Feedback feedback = new Feedback();
            feedback.setId(1);
            feedback.setContent("测试反馈");
            Page<Feedback> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(feedback));
            when(feedbackService.page(any(), any())).thenReturn(pageResult);

            R<Map<String, Object>> result = adminInteractionController.listFeedback("all", 1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(feedbackService).page(any(), any());
        }

        @Test
        @DisplayName("反馈列表 - 按状态筛选")
        void testListFeedback_FilterByStatus() {
            Feedback feedback = new Feedback();
            feedback.setId(1);
            feedback.setStatus("pending");
            Page<Feedback> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(feedback));
            when(feedbackService.page(any(), any())).thenReturn(pageResult);

            R<Map<String, Object>> result = adminInteractionController.listFeedback("pending", 1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(feedbackService).page(any(), any());
        }

        @Test
        @DisplayName("回复反馈 - 成功")
        void testReplyFeedback_Success() {
            FeedbackReplyDTO dto = new FeedbackReplyDTO();
            dto.setReply("已处理您的反馈");
            doNothing().when(feedbackService).replyFeedback(1, "已处理您的反馈");

            R<String> result = adminInteractionController.replyFeedback(1, dto);

            assertEquals(200, result.getCode());
            assertEquals("回复成功", result.getData());
            verify(feedbackService).replyFeedback(1, "已处理您的反馈");
        }

        @Test
        @DisplayName("删除反馈 - 成功")
        void testDeleteFeedback_Success() {
            when(feedbackService.removeById(1)).thenReturn(true);

            R<String> result = adminInteractionController.deleteFeedback(1);

            assertEquals(200, result.getCode());
            assertEquals("删除反馈成功", result.getData());
            verify(feedbackService).removeById(1);
        }
    }

    // ========== 评论 ==========

    @Nested
    @DisplayName("评论管理")
    class CommentTests {

        @Test
        @DisplayName("评论列表 - 全部状态")
        void testListComments_AllStatus() {
            CommentDTO dto = new CommentDTO();
            dto.setId(1);
            dto.setContent("测试评论");
            Page<CommentDTO> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(dto));
            when(commentService.pageAllDTO("all", 1, 20)).thenReturn(pageResult);

            R<Map<String, Object>> result = adminInteractionController.listComments("all", 1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(commentService).pageAllDTO("all", 1, 20);
        }

        @Test
        @DisplayName("评论列表 - 按状态筛选")
        void testListComments_FilterByStatus() {
            CommentDTO dto = new CommentDTO();
            dto.setId(1);
            dto.setStatus("pending");
            Page<CommentDTO> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(dto));
            when(commentService.pageAllDTO("pending", 1, 20)).thenReturn(pageResult);

            R<Map<String, Object>> result = adminInteractionController.listComments("pending", 1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(commentService).pageAllDTO("pending", 1, 20);
        }

        @Test
        @DisplayName("审核通过评论 - 成功")
        void testApproveComment_Success() {
            doNothing().when(commentService).approveComment(1);

            R<String> result = adminInteractionController.approveComment(1);

            assertEquals(200, result.getCode());
            assertEquals("审核通过", result.getData());
            verify(commentService).approveComment(1);
        }

        @Test
        @DisplayName("拒绝评论 - 成功")
        void testRejectComment_Success() {
            doNothing().when(commentService).rejectComment(1);

            R<String> result = adminInteractionController.rejectComment(1);

            assertEquals(200, result.getCode());
            assertEquals("已拒绝", result.getData());
            verify(commentService).rejectComment(1);
        }

        @Test
        @DisplayName("删除评论 - 成功")
        void testDeleteComment_Success() {
            when(commentService.removeById(1)).thenReturn(true);

            R<String> result = adminInteractionController.deleteComment(1);

            assertEquals(200, result.getCode());
            assertEquals("删除评论成功", result.getData());
            verify(commentService).removeById(1);
        }
    }
}
