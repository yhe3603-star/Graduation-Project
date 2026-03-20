package com.dongmedicine.service;

import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.AnswerDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import java.util.List;

public interface QuizService {
    List<QuizQuestionDTO> getRandomQuestions();
    Integer submit(Integer userId, List<AnswerDTO> answers);
    Integer calculateScore(List<AnswerDTO> answers);
    List<QuizRecord> getUserRecords(Integer userId);

    void deleteQuestion(Integer questionId);
    
    List<QuizQuestion> getAllQuestions();
    void addQuestionDirect(QuizQuestion question);
    void updateQuestionDirect(QuizQuestion question);
}
