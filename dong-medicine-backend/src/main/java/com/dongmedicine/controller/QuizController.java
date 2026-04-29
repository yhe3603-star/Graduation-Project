package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.dto.QuizCreateDTO;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.QuizSubmitDTO;
import com.dongmedicine.dto.QuizUpdateDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
            @RequestParam(defaultValue = "10") @jakarta.validation.constraints.Max(50) int count,
            @RequestParam(defaultValue = "10") int scorePerQuestion) {
        return R.ok(service.getRandomQuestions(Math.min(count, 50)));
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
    public R<List<QuizRecord>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return R.ok(List.of());
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<QuizRecord> all = service.getUserRecords(userId);
        int start = (Math.max(page, 1) - 1) * safeSize;
        int end = Math.min(start + safeSize, all.size());
        return R.ok(start < all.size() ? all.subList(start, end) : List.of());
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
    public R<String> add(@RequestBody @Valid QuizCreateDTO dto) {
        QuizQuestion question = new QuizQuestion();
        BeanUtils.copyProperties(dto, question);
        // DTO中的options是List<String>，需要通过setOptionList转换为JSON字符串
        if (dto.getOptions() != null) {
            question.setOptionList(dto.getOptions());
        }
        service.addQuestionDirect(question);
        return R.ok("添加成功");
    }

    @PutMapping("/update")
    @SaCheckRole("admin")
    public R<String> update(@RequestBody @Valid QuizUpdateDTO dto) {
        QuizQuestion question = new QuizQuestion();
        BeanUtils.copyProperties(dto, question, "options");
        question.setId(dto.getId());
        // DTO中的options是List<String>，需要通过setOptionList转换为JSON字符串
        if (dto.getOptions() != null) {
            question.setOptionList(dto.getOptions());
        }
        service.updateQuestionDirect(question);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public R<String> delete(@PathVariable Integer id) {
        service.deleteQuestion(id);
        return R.ok("删除成功");
    }
}
