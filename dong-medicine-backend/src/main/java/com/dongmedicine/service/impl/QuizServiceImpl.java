package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.AnswerDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.mapper.QuizQuestionMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import com.dongmedicine.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    @Autowired
    private QuizQuestionMapper questionMapper;
    @Autowired
    private QuizRecordMapper recordMapper;

    @Override
    public List<QuizQuestionDTO> getRandomQuestions() {
        List<QuizQuestion> allQuestions = questionMapper.selectList(null);
        Collections.shuffle(allQuestions);
        return allQuestions.stream().limit(10).map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Integer submit(Integer userId, List<AnswerDTO> answers) {
        if (userId == null || answers == null || answers.isEmpty()) {
            throw new RuntimeException("用户ID和答案不能为空");
        }

        int score = calculateScore(answers);

        QuizRecord record = new QuizRecord();
        record.setUserId(userId);
        record.setScore(score);
        record.setTotalQuestions(answers.size());
        record.setCorrectAnswers(score / 10);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        return score;
    }

    @Override
    public Integer calculateScore(List<AnswerDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("答案不能为空");
        }

        int score = 0;
        for (AnswerDTO dto : answers) {
            if (dto.getQuestionId() == null || !StringUtils.hasText(dto.getAnswer())) {
                throw new RuntimeException("题目ID和答案不能为空");
            }
            QuizQuestion q = questionMapper.selectById(dto.getQuestionId());
            if (q != null && q.getAnswer().equals(dto.getAnswer())) {
                score += 10;
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
            throw new RuntimeException("题目ID不能为空");
        }
        if (questionMapper.deleteById(questionId) == 0) {
            throw new RuntimeException("删除题目失败");
        }
    }

    @Override
    public List<QuizQuestion> getAllQuestions() {
        return questionMapper.selectList(null);
    }

    @Override
    @CacheEvict(value = "quizQuestions", allEntries = true)
    public void addQuestionDirect(QuizQuestion question) {
        if (question == null || !StringUtils.hasText(question.getQuestion())) {
            throw new RuntimeException("题目内容不能为空");
        }
        questionMapper.insert(question);
    }

    @Override
    @CacheEvict(value = "quizQuestions", allEntries = true)
    public void updateQuestionDirect(QuizQuestion question) {
        if (question == null || question.getId() == null) {
            throw new RuntimeException("题目ID不能为空");
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
