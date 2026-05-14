package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "数据统计", description = "平台数据概览与统计")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final PlantService plantService;
    private final KnowledgeService knowledgeService;
    private final QaService qaService;
    private final InheritorService inheritorService;
    private final ResourceService resourceService;

    @Operation(summary = "获取数据概览")
    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview() {
        return R.ok(statisticsService.getOverview());
    }

    @Operation(summary = "获取图表数据")
    @GetMapping("/chart")
    public R<Map<String, Object>> getChartData() {
        return R.ok(statisticsService.getChartData());
    }

    @Operation(summary = "获取访问趋势")
    @GetMapping("/trend")
    public R<Map<String, Object>> getVisitTrend() {
        return R.ok(statisticsService.getVisitTrend());
    }

    @Operation(summary = "获取植物统计")
    @GetMapping("/plants")
    public R<Map<String, Object>> plantStats() {
        return R.ok(plantService.getStats());
    }

    @Operation(summary = "获取知识统计")
    @GetMapping("/knowledge")
    public R<Map<String, Object>> knowledgeStats() {
        return R.ok(knowledgeService.getStats());
    }

    @Operation(summary = "获取问答统计")
    @GetMapping("/qa")
    public R<Map<String, Object>> qaStats() {
        return R.ok(qaService.getStats());
    }

    @Operation(summary = "获取传承人统计")
    @GetMapping("/inheritors")
    public R<Map<String, Object>> inheritorStats() {
        return R.ok(inheritorService.getStats());
    }

    @Operation(summary = "获取资源统计")
    @GetMapping("/resources")
    public R<Map<String, Object>> resourceStats() {
        return R.ok(resourceService.getStats());
    }
}
