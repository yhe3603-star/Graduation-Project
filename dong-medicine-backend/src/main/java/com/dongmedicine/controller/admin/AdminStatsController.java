package com.dongmedicine.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.dongmedicine.common.R;
import com.dongmedicine.mapper.SearchHistoryMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
    private final UserMapper userMapper;
    private final SearchHistoryMapper searchHistoryMapper;

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

    @GetMapping("/stats/user-growth")
    public R<Map<String, Object>> getUserGrowth() {
        List<Map<String, Object>> rows = userMapper.countByDateLast7Days();
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            dates.add(String.valueOf(row.get("date")));
            counts.add(row.get("count") != null ? ((Number) row.get("count")).longValue() : 0L);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return R.ok(result);
    }

    @GetMapping("/stats/content-views")
    public R<List<Map<String, Object>>> getContentViews() {
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(plantService.topByViewCount(10));
        all.addAll(knowledgeService.topByViewCount(10));
        all.addAll(inheritorService.topByViewCount(10));
        all.sort((a, b) -> Long.compare(
                ((Number) b.get("value")).longValue(),
                ((Number) a.get("value")).longValue()));
        return R.ok(all.subList(0, Math.min(10, all.size())));
    }

    @GetMapping("/stats/search-keywords")
    public R<List<Map<String, Object>>> getSearchKeywords() {
        return R.ok(searchHistoryMapper.topKeywords(10));
    }
}
