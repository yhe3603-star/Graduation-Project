package com.dongmedicine.service.impl;

import com.dongmedicine.dto.AnswerDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.mapper.QuizQuestionMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
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
@DisplayName("答题服务测试")
class QuizServiceImplTest {

    @Mock
    private QuizQuestionMapper questionMapper;

    @Mock
    private QuizRecordMapper recordMapper;

    @InjectMocks
    private QuizServiceImpl quizService;

    private QuizQuestion testQuestion;
    private AnswerDTO testAnswer;

    @BeforeEach
    void setUp() {
        testQuestion = new QuizQuestion();
        testQuestion.setId(1);
        testQuestion.setQuestion("测试问题");
        testQuestion.setAnswer("A");

        testAnswer = new AnswerDTO();
        testAnswer.setQuestionId(1);
        testAnswer.setAnswer("A");
    }

    @Test
    @DisplayName("获取随机题目 - 成功")
    void testGetRandomQuestions_Success() {
        when(questionMapper.selectRandomQuestions(anyInt()))
                .thenReturn(Arrays.asList(testQuestion));

        var result = quizService.getRandomQuestions();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(questionMapper).selectRandomQuestions(10);
    }

    @Test
    @DisplayName("计算分数 - 全部正确")
    void testCalculateScore_AllCorrect() {
        when(questionMapper.selectBatchIds(anyCollection()))
                .thenReturn(Arrays.asList(testQuestion));

        Integer score = quizService.calculateScore(Arrays.asList(testAnswer));

        assertEquals(10, score);
    }

    @Test
    @DisplayName("计算分数 - 全部错误")
    void testCalculateScore_AllWrong() {
        testAnswer.setAnswer("B");
        when(questionMapper.selectBatchIds(anyCollection()))
                .thenReturn(Arrays.asList(testQuestion));

        Integer score = quizService.calculateScore(Arrays.asList(testAnswer));

        assertEquals(0, score);
    }

    @Test
    @DisplayName("计算分数 - 空答案抛出异常")
    void testCalculateScore_EmptyAnswers_ThrowsException() {
        assertThrows(Exception.class, () -> quizService.calculateScore(null));
        assertThrows(Exception.class, () -> quizService.calculateScore(Collections.emptyList()));
    }

    @Test
    @DisplayName("提交答案 - 成功")
    void testSubmit_Success() {
        when(questionMapper.selectBatchIds(anyCollection()))
                .thenReturn(Arrays.asList(testQuestion));
        when(recordMapper.insert(any(QuizRecord.class))).thenReturn(1);

        Integer score = quizService.submit(1, Arrays.asList(testAnswer));

        assertNotNull(score);
        verify(recordMapper).insert(any(QuizRecord.class));
    }

    @Test
    @DisplayName("提交答案 - 空用户ID抛出异常")
    void testSubmit_NullUserId_ThrowsException() {
        assertThrows(Exception.class, () -> quizService.submit(null, Arrays.asList(testAnswer)));
    }

    @Test
    @DisplayName("获取用户记录 - 成功")
    void testGetUserRecords_Success() {
        QuizRecord record = new QuizRecord();
        record.setUserId(1);
        record.setScore(80);
        when(recordMapper.selectList(any()))
                .thenReturn(Arrays.asList(record));

        List<QuizRecord> result = quizService.getUserRecords(1);

        assertFalse(result.isEmpty());
        assertEquals(80, result.get(0).getScore());
    }

    @Test
    @DisplayName("获取用户记录 - 空用户ID返回空列表")
    void testGetUserRecords_NullUserId_ReturnsEmpty() {
        List<QuizRecord> result = quizService.getUserRecords(null);
        assertTrue(result.isEmpty());
    }
}
