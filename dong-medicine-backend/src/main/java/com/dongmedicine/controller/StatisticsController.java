package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final OperationLogService logService;
    private final PlantService plantService;
    private final KnowledgeService knowledgeService;
    private final QaService qaService;
    private final InheritorService inheritorService;
    private final ResourceService resourceService;

    @GetMapping("/trend")
    public R<Map<String, Object>> getVisitTrend() {
        List<Map<String, Object>> dbData = logService.getTrendLast7Days();
        
        Map<String, Long> dateCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : dbData) {
            Object dateObj = row.get("date");
            Object countObj = row.get("count");
            String dateStr = dateObj != null ? dateObj.toString() : "";
            long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
            dateCountMap.put(dateStr, count);
        }
        
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("M/d");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            String displayStr = date.format(displayFormatter);
            dates.add(displayStr);
            counts.add(dateCountMap.getOrDefault(dateStr, 0L));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return R.ok(result);
    }

    @GetMapping("/plants")
    public R<Map<String, Object>> plantStats() {
        return R.ok(plantService.getStats());
    }

    @GetMapping("/knowledge")
    public R<Map<String, Object>> knowledgeStats() {
        return R.ok(knowledgeService.getStats());
    }

    @GetMapping("/qa")
    public R<Map<String, Object>> qaStats() {
        return R.ok(qaService.getStats());
    }

    @GetMapping("/inheritors")
    public R<Map<String, Object>> inheritorStats() {
        return R.ok(inheritorService.getStats());
    }

    @GetMapping("/resources")
    public R<Map<String, Object>> resourceStats() {
        return R.ok(resourceService.getStats());
    }
}
