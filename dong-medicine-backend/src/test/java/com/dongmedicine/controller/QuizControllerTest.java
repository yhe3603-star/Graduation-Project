package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.service.QuizService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("问答Controller测试")
class QuizControllerTest {

    @Mock
    private QuizService quizService;

    @InjectMocks
    private QuizController quizController;

    private QuizQuestion testQuestion;

    @BeforeEach
    void setUp() {
        testQuestion = new QuizQuestion();
        testQuestion.setId(1);
        testQuestion.setQuestion("侗医药有什么特点？");
    }

    @Test
    @DisplayName("获取随机题目 - 成功")
    void testListQuestions_Success() {
        when(quizService.getRandomQuestions(10)).thenReturn(List.of());

        R<List<QuizQuestionDTO>> result = quizController.list(10, 10);

        assertEquals(200, result.getCode());
        verify(quizService).getRandomQuestions(10);
    }

    @Test
    @DisplayName("获取随机题目 - count超过50被限制")
    void testListQuestions_CountOverLimit() {
        when(quizService.getRandomQuestions(50)).thenReturn(List.of());

        R<List<QuizQuestionDTO>> result = quizController.list(999, 10);

        assertEquals(200, result.getCode());
        verify(quizService).getRandomQuestions(50);
    }

    @Test
    @DisplayName("题目列表分页 - 成功")
    void testListAll_Success() {
        Page<QuizQuestion> page = new Page<>(1, 20, 0);
        page.setRecords(List.of());
        when(quizService.pageQuestions(anyInt(), anyInt())).thenReturn(page);

        R<Map<String, Object>> result = quizController.listAll(1, 20);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("答题记录 - 未登录返回空列表")
    void testRecords_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(null);

            R<List<QuizRecord>> result = quizController.records(1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    @DisplayName("答题记录 - 已登录返回数据")
    void testRecords_LoggedIn() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(1);

            QuizRecord record = new QuizRecord();
            record.setId(1);
            record.setUserId(1);
            record.setScore(80);
            when(quizService.getUserRecords(1)).thenReturn(Arrays.asList(record));

            R<List<QuizRecord>> result = quizController.records(1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            assertEquals(1, result.getData().size());
        }
    }
}
