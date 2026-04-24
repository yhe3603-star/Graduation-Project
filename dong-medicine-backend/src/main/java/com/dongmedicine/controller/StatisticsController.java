package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.entity.*;
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
    @Cacheable(value = "hotData", key = "'stats:plants'")
    public R<Map<String, Object>> plantStats() {
        List<Plant> all = plantService.list();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("categoryCount", all.stream().map(Plant::getCategory).filter(Objects::nonNull).filter(s -> !s.isBlank()).distinct().count());
        stats.put("totalViews", all.stream().mapToLong(p -> p.getViewCount() != null ? p.getViewCount() : 0).sum());
        stats.put("totalFavorites", all.stream().mapToLong(p -> p.getFavoriteCount() != null ? p.getFavoriteCount() : 0).sum());
        return R.ok(stats);
    }

    @GetMapping("/knowledge")
    @Cacheable(value = "hotData", key = "'stats:knowledge'")
    public R<Map<String, Object>> knowledgeStats() {
        List<Knowledge> all = knowledgeService.list();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("therapyCategoryCount", all.stream().map(Knowledge::getTherapyCategory).filter(Objects::nonNull).filter(s -> !s.isBlank()).distinct().count());
        stats.put("diseaseCategoryCount", all.stream().map(Knowledge::getDiseaseCategory).filter(Objects::nonNull).filter(s -> !s.isBlank()).distinct().count());
        stats.put("typeCount", all.stream().map(Knowledge::getType).filter(Objects::nonNull).filter(s -> !s.isBlank()).distinct().count());
        stats.put("totalViews", all.stream().mapToLong(k -> k.getViewCount() != null ? k.getViewCount() : 0).sum());
        stats.put("totalFavorites", all.stream().mapToLong(k -> k.getFavoriteCount() != null ? k.getFavoriteCount() : 0).sum());
        return R.ok(stats);
    }

    @GetMapping("/qa")
    @Cacheable(value = "hotData", key = "'stats:qa'")
    public R<Map<String, Object>> qaStats() {
        List<Qa> all = qaService.list();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("categoryCount", all.stream().map(Qa::getCategory).filter(Objects::nonNull).filter(s -> !s.isBlank()).distinct().count());
        stats.put("totalViews", all.stream().mapToLong(q -> q.getViewCount() != null ? q.getViewCount() : 0).sum());
        stats.put("totalFavorites", all.stream().mapToLong(q -> q.getFavoriteCount() != null ? q.getFavoriteCount() : 0).sum());
        return R.ok(stats);
    }

    @GetMapping("/inheritors")
    @Cacheable(value = "hotData", key = "'stats:inheritors'")
    public R<Map<String, Object>> inheritorStats() {
        List<Inheritor> all = inheritorService.list();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("regionLevelCount", all.stream().filter(i -> "自治区级".equals(i.getLevel())).count());
        stats.put("cityLevelCount", all.stream().filter(i -> "市级".equals(i.getLevel())).count());
        stats.put("countyLevelCount", all.stream().filter(i -> "县级".equals(i.getLevel())).count());
        stats.put("totalViews", all.stream().mapToLong(i -> i.getViewCount() != null ? i.getViewCount() : 0).sum());
        stats.put("totalFavorites", all.stream().mapToLong(i -> i.getFavoriteCount() != null ? i.getFavoriteCount() : 0).sum());
        return R.ok(stats);
    }

    @GetMapping("/resources")
    @Cacheable(value = "hotData", key = "'stats:resources'")
    public R<Map<String, Object>> resourceStats() {
        List<Resource> all = resourceService.list();
        long videoCount = 0, documentCount = 0, imageCount = 0, totalSize = 0;
        for (Resource r : all) {
            String files = r.getFiles();
            if (files == null || files.isBlank()) continue;
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<java.util.Map<String, Object>> fileList = mapper.readValue(files, java.util.List.class);
                for (java.util.Map<String, Object> f : fileList) {
                    String type = f.get("type") != null ? f.get("type").toString() : "";
                    long size = f.get("size") != null ? Long.parseLong(f.get("size").toString()) : 0;
                    totalSize += size;
                    if (type.startsWith("video/")) videoCount++;
                    else if (type.startsWith("image/")) imageCount++;
                    else documentCount++;
                }
            } catch (Exception ignored) {}
        }
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("videoCount", videoCount);
        stats.put("documentCount", documentCount);
        stats.put("imageCount", imageCount);
        stats.put("totalFavorites", all.stream().mapToLong(r -> r.getFavoriteCount() != null ? r.getFavoriteCount() : 0).sum());
        stats.put("totalViews", all.stream().mapToLong(r -> r.getViewCount() != null ? r.getViewCount() : 0).sum());
        stats.put("totalDownloads", all.stream().mapToLong(r -> r.getDownloadCount() != null ? r.getDownloadCount() : 0).sum());
        stats.put("totalSize", totalSize);
        return R.ok(stats);
    }
}
