package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.util.PageUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.dto.QuizCreateDTO;
import com.dongmedicine.dto.QuizQuestionDTO;
import com.dongmedicine.dto.QuizSubmitDTO;
import com.dongmedicine.dto.QuizUpdateDTO;
import com.dongmedicine.entity.QuizQuestion;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.service.QuizService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "趣味答题", description = "侗乡医药知识趣味答题与题目管理")
@Slf4j
@RestController
@RequestMapping("/api/quiz")
@Validated
@RequiredArgsConstructor
public class QuizController {

    private final QuizService service;

    @GetMapping("/questions")
    public R<List<QuizQuestionDTO>> list(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "10") int scorePerQuestion) {
        int safeCount = Math.min(Math.max(count, 1), 50);
        return R.ok(service.getRandomQuestions(safeCount));
    }

    @PostMapping("/submit")
    public R<Map<String, Object>> submit(@RequestBody QuizSubmitDTO dto,
                                         @RequestParam(defaultValue = "10") int scorePerQuestion) {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        int score = (userId == null)
                ? service.calculateScore(dto.getAnswers(), scorePerQuestion)
                : service.submit(userId, dto.getAnswers(), scorePerQuestion);
        int totalQuestions = dto.getAnswers() != null ? dto.getAnswers().size() : 0;
        int correctAnswers = score / (scorePerQuestion > 0 ? scorePerQuestion : 10);
        return R.ok(Map.of("score", score, "correct", correctAnswers, "total", totalQuestions));
    }

    @GetMapping("/records")
    public R<Map<String, Object>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok(Map.of("records", List.of(), "total", 0));
        Page<QuizRecord> pageResult = service.pageUserRecords(userId, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/list")
    public R<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<QuizQuestion> pageResult = service.pageQuestions(page, size);
        return R.ok(PageUtils.toMap(pageResult));
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
