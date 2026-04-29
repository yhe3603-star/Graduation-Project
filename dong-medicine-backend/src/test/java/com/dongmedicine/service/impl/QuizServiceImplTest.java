package com.dongmedicine.service.impl;

import com.dongmedicine.dto.AnswerDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.mapper.QuizQuestionMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizQuestionMapper questionMapper;

    @Mock
    private QuizRecordMapper recordMapper;

    @InjectMocks
    private QuizServiceImpl quizService;

    @Test
    @DisplayName("获取随机题目 - 默认数量")
    void getRandomQuestionsDefault() {
        when(questionMapper.selectRandomQuestions(anyInt())).thenReturn(Collections.emptyList());

        var result = quizService.getRandomQuestions(10);
        assertNotNull(result);
    }

    @Test
    @DisplayName("计算分数 - 空答案应抛异常")
    void calculateScoreEmptyAnswers() {
        assertThrows(Exception.class, () -> {
            quizService.calculateScore(null, 10);
        });
    }

    @Test
    @DisplayName("计算分数 - 全部正确")
    void calculateScoreAllCorrect() {
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId(1);
        answer.setAnswer("A");

        QuizQuestion question = new QuizQuestion();
        question.setId(1);
        question.setCorrectAnswer("A");

        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(question));

        int score = quizService.calculateScore(List.of(answer), 10);
        assertEquals(10, score);
    }

    @Test
    @DisplayName("计算分数 - 全部错误")
    void calculateScoreAllWrong() {
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId(1);
        answer.setAnswer("B");

        QuizQuestion question = new QuizQuestion();
        question.setId(1);
        question.setCorrectAnswer("A");

        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(question));

        int score = quizService.calculateScore(List.of(answer), 10);
        assertEquals(0, score);
    }

    @Test
    @DisplayName("提交答案 - 空答案应抛异常")
    void submitEmptyAnswers() {
        assertThrows(Exception.class, () -> {
            quizService.submit(1, null, 10);
        });
    }

    @Test
    @DisplayName("获取用户记录 - null用户应返回空列表")
    void getUserRecordsNullUser() {
        List<QuizRecord> result = quizService.getUserRecords(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("删除题目 - null ID应抛异常")
    void deleteQuestionNullId() {
        assertThrows(Exception.class, () -> {
            quizService.deleteQuestion(null);
        });
    }

    @Test
    @DisplayName("添加题目 - null应抛异常")
    void addQuestionNull() {
        assertThrows(Exception.class, () -> {
            quizService.addQuestionDirect(null);
        });
    }

    @Test
    @DisplayName("计算分数 - 默认每题分数")
    void calculateScoreDefaultPerQuestion() {
        AnswerDTO answer = new AnswerDTO();
        answer.setQuestionId(1);
        answer.setAnswer("A");

        QuizQuestion question = new QuizQuestion();
        question.setId(1);
        question.setCorrectAnswer("A");

        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(question));

        int score = quizService.calculateScore(List.of(answer), 0);
        assertEquals(10, score);
    }
}
