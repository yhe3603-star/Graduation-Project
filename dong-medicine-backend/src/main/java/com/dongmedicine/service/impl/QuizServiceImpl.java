package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.AnswerDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.mapper.QuizQuestionMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import com.dongmedicine.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private static final int DEFAULT_QUESTION_COUNT = 10;
    private static final int SCORE_PER_QUESTION = 10;

    @Value("${app.quiz.question-count:10}")
    private int questionCount;

    @Autowired
    private QuizQuestionMapper questionMapper;
    @Autowired
    private QuizRecordMapper recordMapper;

    @Override
    public List<QuizQuestionDTO> getRandomQuestions() {
        int count = questionCount > 0 ? questionCount : DEFAULT_QUESTION_COUNT;
        List<QuizQuestion> questions = questionMapper.selectRandomQuestions(count);
        return questions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Integer submit(Integer userId, List<AnswerDTO> answers) {
        if (userId == null || answers == null || answers.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID和答案不能为空");
        }

        int score = calculateScore(answers);

        QuizRecord record = new QuizRecord();
        record.setUserId(userId);
        record.setScore(score);
        record.setTotalQuestions(answers.size());
        record.setCorrectAnswers(score / SCORE_PER_QUESTION);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        return score;
    }

    @Override
    public Integer calculateScore(List<AnswerDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "答案不能为空");
        }

        List<Integer> questionIds = answers.stream()
                .map(AnswerDTO::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (questionIds.isEmpty()) {
            return 0;
        }

        List<QuizQuestion> questions = questionMapper.selectBatchIds(questionIds);
        Map<Integer, QuizQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));

        int score = 0;
        for (AnswerDTO dto : answers) {
            if (dto.getQuestionId() == null || !StringUtils.hasText(dto.getAnswer())) {
                continue;
            }
            QuizQuestion q = questionMap.get(dto.getQuestionId());
            if (q != null && q.getAnswer().equals(dto.getAnswer())) {
                score += SCORE_PER_QUESTION;
            }
        }
        return score;
    }

    @Override
    public List<QuizRecord> getUserRecords(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return recordMapper.selectList(new LambdaQueryWrapper<QuizRecord>()
                .eq(QuizRecord::getUserId, userId)
                .orderByDesc(QuizRecord::getCreateTime));
    }

    @Override
    @CacheEvict(value = "quizQuestions", allEntries = true)
    public void deleteQuestion(Integer questionId) {
        if (questionId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "题目ID不能为空");
        }
        if (questionMapper.deleteById(questionId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "删除题目失败");
        }
    }

    @Override
    public List<QuizQuestion> getAllQuestions() {
        return questionMapper.selectList(null);
    }

    @Override
    public long countQuestions() {
        return questionMapper.selectCount(null);
    }

    @Override
    public Page<QuizQuestion> pageQuestions(int page, int size) {
        return questionMapper.selectPage(
                PageUtils.getPage(page, size),
                new LambdaQueryWrapper<QuizQuestion>().orderByDesc(QuizQuestion::getId));
    }

    @Override
    @CacheEvict(value = "quizQuestions", allEntries = true)
    public void addQuestionDirect(QuizQuestion question) {
        if (question == null || !StringUtils.hasText(question.getQuestion())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "题目内容不能为空");
        }
        questionMapper.insert(question);
    }

    @Override
    @CacheEvict(value = "quizQuestions", allEntries = true)
    public void updateQuestionDirect(QuizQuestion question) {
        if (question == null || question.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "题目ID不能为空");
        }
        questionMapper.updateById(question);
    }

    private QuizQuestionDTO convertToDTO(QuizQuestion question) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(question.getId());
        dto.setQ(question.getQuestion());
        dto.setOptions(question.getOptionList());
        return dto;
    }
}
