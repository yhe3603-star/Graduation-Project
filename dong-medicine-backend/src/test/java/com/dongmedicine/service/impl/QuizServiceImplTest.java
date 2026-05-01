package com.dongmedicine.service.impl;

import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.AnswerDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.mapper.QuizQuestionMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService 单元测试")
class QuizServiceImplTest {

    @Mock
    private QuizQuestionMapper questionMapper;

    @Mock
    private QuizRecordMapper recordMapper;

    @InjectMocks
    private QuizServiceImpl quizService;

    private QuizQuestion createQuestion(Integer id, String question, String correctAnswer, String... options) {
        QuizQuestion q = new QuizQuestion();
        q.setId(id);
        q.setQuestion(question);
        q.setCorrectAnswer(correctAnswer);
        q.setOptionList(List.of(options));
        return q;
    }

    @Nested
    @DisplayName("getRandomQuestions - 随机组卷测试")
    class GetRandomQuestions {

        @Test
        @DisplayName("应返回指定数量的题目")
        void shouldReturnSpecifiedCount() {
            List<QuizQuestion> questions = List.of(
                    createQuestion(1, "Q1", "A", "A", "B", "C", "D"),
                    createQuestion(2, "Q2", "B", "A", "B", "C", "D"),
                    createQuestion(3, "Q3", "C", "A", "B", "C", "D")
            );
            when(questionMapper.selectRandomQuestions(3)).thenReturn(questions);

            List<QuizQuestionDTO> result = quizService.getRandomQuestions(3);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getQ()).isEqualTo("Q1");
            assertThat(result.get(0).getOptions()).hasSize(4);
        }

        @Test
        @DisplayName("count为0时应使用默认10题")
        void shouldUseDefaultWhenCountIsZero() {
            when(questionMapper.selectRandomQuestions(10)).thenReturn(Collections.emptyList());

            quizService.getRandomQuestions(0);

            verify(questionMapper).selectRandomQuestions(10);
        }

        @Test
        @DisplayName("count为负数时应使用默认10题")
        void shouldUseDefaultWhenCountIsNegative() {
            when(questionMapper.selectRandomQuestions(10)).thenReturn(Collections.emptyList());

            quizService.getRandomQuestions(-5);

            verify(questionMapper).selectRandomQuestions(10);
        }

        @Test
        @DisplayName("DTO转换应隐藏正确答案")
        void shouldHideCorrectAnswerInDTO() {
            QuizQuestion q = createQuestion(1, "Test Q?", "B", "A" , "B", "C", "D");
            when(questionMapper.selectRandomQuestions(1)).thenReturn(List.of(q));

            List<QuizQuestionDTO> result = quizService.getRandomQuestions(1);

            assertThat(result.get(0).getQ()).isEqualTo("Test Q?");
            assertThat(result.get(0).getId()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("calculateScore - 计分逻辑测试")
    class CalculateScore {

        @Test
        @DisplayName("全部答对应返回满分")
        void shouldReturnFullScoreForAllCorrect() {
            QuizQuestion q1 = createQuestion(1, "Q1", "A", "A", "B", "C", "D");
            QuizQuestion q2 = createQuestion(2, "Q2", "B", "A", "B", "C", "D");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q1, q2));

            List<AnswerDTO> answers = List.of(
                    createAnswer(1, "A"),
                    createAnswer(2, "B")
            );

            int score = quizService.calculateScore(answers, 10);
            assertThat(score).isEqualTo(20);
        }

        @Test
        @DisplayName("大小写不敏感匹配")
        void shouldBeCaseInsensitive() {
            QuizQuestion q = createQuestion(1, "Q1", "Correct", "Correct", "Wrong", "", "");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q));

            List<AnswerDTO> answers = List.of(createAnswer(1, "correct"));
            int score = quizService.calculateScore(answers, 10);
            assertThat(score).isEqualTo(10);
        }

        @Test
        @DisplayName("前后空格不影响匹配")
        void shouldTrimWhitespace() {
            QuizQuestion q = createQuestion(1, "Q1", "Answer", "Answer", "B", "C", "D");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q));

            List<AnswerDTO> answers = List.of(createAnswer(1, "  Answer  "));
            int score = quizService.calculateScore(answers, 10);
            assertThat(score).isEqualTo(10);
        }

        @Test
        @DisplayName("全部答错应返回0分")
        void shouldReturnZeroForAllWrong() {
            QuizQuestion q1 = createQuestion(1, "Q1", "A", "A", "B", "C", "D");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q1));

            List<AnswerDTO> answers = List.of(createAnswer(1, "B"));
            int score = quizService.calculateScore(answers, 10);
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("自定义每题分值")
        void shouldUseCustomScorePerQuestion() {
            QuizQuestion q = createQuestion(1, "Q1", "A", "A", "B", "C", "D");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q));

            List<AnswerDTO> answers = List.of(createAnswer(1, "A"));
            int score = quizService.calculateScore(answers, 5);
            assertThat(score).isEqualTo(5);
        }

        @Test
        @DisplayName("empty答案列表应抛出异常")
        void shouldThrowForEmptyAnswers() {
            assertThatThrownBy(() -> quizService.calculateScore(Collections.emptyList(), 10))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("null答案列表应抛出异常")
        void shouldThrowForNullAnswers() {
            assertThatThrownBy(() -> quizService.calculateScore(null, 10))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("题目ID不存在应跳过")
        void shouldSkipNonExistentQuestionId() {
            QuizQuestion q1 = createQuestion(1, "Q1", "A", "A", "B", "C", "D");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q1));

            List<AnswerDTO> answers = List.of(
                    createAnswer(1, "A"),
                    createAnswer(999, "B")
            );
            int score = quizService.calculateScore(answers, 10);
            assertThat(score).isEqualTo(10);
        }

        @Test
        @DisplayName("空答案文本应跳过该题")
        void shouldSkipEmptyAnswerText() {
            QuizQuestion q = createQuestion(1, "Q1", "A", "A", "B", "C", "D");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q));

            List<AnswerDTO> answers = List.of(createAnswer(1, ""));
            int score = quizService.calculateScore(answers, 10);
            assertThat(score).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("submit - 提交流程测试")
    class Submit {

        @Test
        @DisplayName("null userId应抛出异常")
        void shouldThrowForNullUserId() {
            List<AnswerDTO> answers = List.of(createAnswer(1, "A"));
            assertThatThrownBy(() -> quizService.submit(null, answers, 10))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("null answers应抛出异常")
        void shouldThrowForNullAnswers() {
            assertThatThrownBy(() -> quizService.submit(1, null, 10))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("成功提交应保存记录并返回分数")
        void shouldSaveRecordAndReturnScore() {
            QuizQuestion q1 = createQuestion(1, "Q1", "A", "A", "B", "C", "D");
            QuizQuestion q2 = createQuestion(2, "Q2", "B", "A", "B", "C", "D");
            when(questionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(q1, q2));

            List<AnswerDTO> answers = List.of(
                    createAnswer(1, "A"),
                    createAnswer(2, "B")
            );

            int score = quizService.submit(100, answers, 10);

            assertThat(score).isEqualTo(20);

            ArgumentCaptor<QuizRecord> captor = ArgumentCaptor.forClass(QuizRecord.class);
            verify(recordMapper).insert(captor.capture());
            QuizRecord saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(100);
            assertThat(saved.getScore()).isEqualTo(20);
            assertThat(saved.getTotalQuestions()).isEqualTo(2);
            assertThat(saved.getCorrectAnswers()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getUserRecords - 查询记录测试")
    class GetUserRecords {

        @Test
        @DisplayName("null userId应返回空列表")
        void shouldReturnEmptyForNullUserId() {
            List<QuizRecord> records = quizService.getUserRecords(null);
            assertThat(records).isEmpty();
            verifyNoInteractions(recordMapper);
        }

        @Test
        @DisplayName("正常查询应按创建时间倒序")
        void shouldOrderByCreatedAtDesc() {
            when(recordMapper.selectList(any())).thenReturn(Collections.emptyList());

            quizService.getUserRecords(1);

            verify(recordMapper).selectList(any());
        }
    }

    @Nested
    @DisplayName("deleteQuestion - 删除题目测试")
    class DeleteQuestion {

        @Test
        @DisplayName("null ID应抛出异常")
        void shouldThrowForNullId() {
            assertThatThrownBy(() -> quizService.deleteQuestion(null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("删除不存在ID应抛出异常")
        void shouldThrowForNonExistentId() {
            when(questionMapper.deleteById(999)).thenReturn(0);

            assertThatThrownBy(() -> quizService.deleteQuestion(999))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("正常删除应成功")
        void shouldDeleteSuccessfully() {
            when(questionMapper.deleteById(1)).thenReturn(1);

            quizService.deleteQuestion(1);

            verify(questionMapper).deleteById(1);
        }
    }

    private AnswerDTO createAnswer(Integer questionId, String answer) {
        AnswerDTO dto = new AnswerDTO();
        dto.setQuestionId(questionId);
        dto.setAnswer(answer);
        return dto;
    }
}
