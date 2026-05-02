package com.dongmedicine.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dongmedicine.common.R;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "后台管理-统计", description = "管理员数据统计")
@RestController
@RequestMapping("/api/admin")
@Validated
@SaCheckRole("admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final UserService userService;
    private final KnowledgeService knowledgeService;
    private final InheritorService inheritorService;
    private final PlantService plantService;
    private final QaService qaService;
    private final ResourceService resourceService;
    private final QuizService quizService;
    private final CommentService commentService;
    private final FeedbackService feedbackService;

    @GetMapping("/stats")
    public R<Map<String, Long>> stats() {
        Map<String, Long> data = new HashMap<>();
        data.put("users", userService.count());
        data.put("knowledge", knowledgeService.count());
        data.put("inheritors", inheritorService.count());
        data.put("plants", plantService.count());
        data.put("qa", qaService.count());
        data.put("resources", resourceService.count());
        data.put("quiz", quizService.countQuestions());
        data.put("comments", commentService.count());
        data.put("feedback", feedbackService.count());
        return R.ok(data);
    }

    @GetMapping("/stats/plants-distribution")
    public R<List<Map<String, Object>>> getPlantDistribution() {
        return R.ok(plantService.listMaps(new QueryWrapper<Plant>()
                .select("distribution as name", "count(*) as value")
                .groupBy("distribution")
                .orderByDesc("distribution")));
    }

    @GetMapping("/stats/knowledge-popularity")
    public R<List<Map<String, Object>>> getKnowledgePopularity() {
        return R.ok(knowledgeService.listMaps(new QueryWrapper<Knowledge>()
                .select("title as name", "popularity as value")
                .orderByDesc("popularity")
                .last("LIMIT 10")));
    }
}
