package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.service.QuizService;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.QuizSubmitDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService service;

    @GetMapping("/questions")
    public R<List<QuizQuestionDTO>> list() {
        return R.ok(service.getRandomQuestions());
    }

    @PostMapping("/submit")
    public R<Map<String, Object>> submit(@RequestBody QuizSubmitDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        int score = (userId == null) ? service.calculateScore(dto.getAnswers()) : service.submit(userId, dto.getAnswers());
        int correctAnswers = score / 10;
        int totalQuestions = dto.getAnswers() != null ? dto.getAnswers().size() : 0;
        return R.ok(Map.of("score", score, "correct", correctAnswers, "total", totalQuestions));
    }

    @GetMapping("/records")
    public R<List<QuizRecord>> records() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userId == null ? List.of() : service.getUserRecords(userId));
    }

    @GetMapping("/list")
    public R<List<QuizQuestion>> listAll() {
        return R.ok(service.getAllQuestions());
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody QuizQuestion question) {
        normalizeOptions(question);
        service.addQuestionDirect(question);
        return R.ok("添加成功");
    }

    @PutMapping("/update")
    public R<String> update(@RequestBody QuizQuestion question) {
        normalizeOptions(question);
        service.updateQuestionDirect(question);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        service.deleteQuestion(id);
        return R.ok("删除成功");
    }

    private void normalizeOptions(QuizQuestion question) {
        if (question.getOptions() == null && question.getOptionList() != null) {
            question.setOptionList(question.getOptionList());
        }
    }
}
