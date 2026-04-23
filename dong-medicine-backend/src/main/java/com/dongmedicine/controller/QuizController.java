package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.service.QuizService;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.QuizSubmitDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService service;

    @GetMapping("/questions")
    public R<List<QuizQuestionDTO>> list(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "10") int scorePerQuestion) {
        return R.ok(service.getRandomQuestions(count));
    }

    @PostMapping("/submit")
    public R<Map<String, Object>> submit(@RequestBody QuizSubmitDTO dto,
                                         @RequestParam(defaultValue = "10") int scorePerQuestion) {
        Integer userId = SecurityUtils.getCurrentUserId();
        int score = (userId == null) 
                ? service.calculateScore(dto.getAnswers(), scorePerQuestion) 
                : service.submit(userId, dto.getAnswers(), scorePerQuestion);
        int totalQuestions = dto.getAnswers() != null ? dto.getAnswers().size() : 0;
        int correctAnswers = score / (scorePerQuestion > 0 ? scorePerQuestion : 10);
        return R.ok(Map.of("score", score, "correct", correctAnswers, "total", totalQuestions));
    }

    @GetMapping("/records")
    public R<List<QuizRecord>> records() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userId == null ? List.of() : service.getUserRecords(userId));
    }

    @GetMapping("/list")
    public R<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<QuizQuestion> pageResult = service.pageQuestions(
                page == null ? 1 : page,
                size == null ? 20 : size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return R.ok(data);
    }

    @PostMapping("/add")
    @SaCheckRole("admin")
    public R<String> add(@RequestBody QuizQuestion question) {
        normalizeOptions(question);
        service.addQuestionDirect(question);
        return R.ok("添加成功");
    }

    @PutMapping("/update")
    @SaCheckRole("admin")
    public R<String> update(@RequestBody QuizQuestion question) {
        normalizeOptions(question);
        service.updateQuestionDirect(question);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
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
