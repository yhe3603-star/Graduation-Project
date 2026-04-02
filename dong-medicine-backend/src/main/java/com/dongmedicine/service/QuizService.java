package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.AnswerDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import java.util.List;

public interface QuizService {
    List<QuizQuestionDTO> getRandomQuestions(int count);
    Integer submit(Integer userId, List<AnswerDTO> answers, int scorePerQuestion);
    Integer calculateScore(List<AnswerDTO> answers, int scorePerQuestion);
    List<QuizRecord> getUserRecords(Integer userId);

    void deleteQuestion(Integer questionId);
    
    List<QuizQuestion> getAllQuestions();

    Page<QuizQuestion> pageQuestions(int page, int size);

    long countQuestions();
    void addQuestionDirect(QuizQuestion question);
    void updateQuestionDirect(QuizQuestion question);
}
