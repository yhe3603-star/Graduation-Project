package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PlantService;
import com.dongmedicine.service.QaService;
import com.dongmedicine.service.ResourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Tag(name = "元数据", description = "平台元数据分类信息")
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class MetadataController {

    private final PlantService plantService;
    private final KnowledgeService knowledgeService;
    private final QaService qaService;
    private final InheritorService inheritorService;
    private final ResourceService resourceService;
    private final PlantMapper plantMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final InheritorMapper inheritorMapper;

    @Lazy
    private final MetadataController self;

    @GetMapping("/filters")
    public R<Map<String, Object>> getAllFilters() {
        return R.ok(self.getAllFiltersData());
    }

    @Cacheable(value = "hotData", key = "'allFilters'")
    public Map<String, Object> getAllFiltersData() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("plants", plantService.getFilterOptions());
        filters.put("knowledge", knowledgeService.getFilterOptions());
        filters.put("qa", qaService.getFilterOptions());
        filters.put("inheritors", inheritorService.getFilterOptions());
        filters.put("resources", resourceService.getFilterOptions());
        return filters;
    }

    @GetMapping("/featured")
    public R<Map<String, Object>> getFeatured() {
        return R.ok(self.getFeaturedData());
    }

    @Cacheable(value = "hotData", key = "'featured'")
    public Map<String, Object> getFeaturedData() {
        List<Plant> topPlants = plantMapper.selectRandomPlants(10);
        Plant topPlant = topPlants.stream()
                .max(Comparator.comparingInt(p -> p.getPopularity() != null ? p.getPopularity() : 0))
                .orElse(topPlants.isEmpty() ? null : topPlants.get(0));

        List<Knowledge> topKnowledge = knowledgeService.list().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getPopularity() != null ? b.getPopularity() : 0,
                        a.getPopularity() != null ? a.getPopularity() : 0))
                .limit(10).toList();
        Knowledge topK = topKnowledge.isEmpty() ? null : topKnowledge.get(0);

        List<Inheritor> topInheritors = inheritorMapper.selectList(null).stream()
                .sorted((a, b) -> Integer.compare(
                        b.getPopularity() != null ? b.getPopularity() : 0,
                        a.getPopularity() != null ? a.getPopularity() : 0))
                .limit(10).toList();
        Inheritor topI = topInheritors.isEmpty() ? null : topInheritors.get(0);

        Map<String, Object> featured = new LinkedHashMap<>();
        if (topPlant != null) {
            Map<String, Object> plantItem = new LinkedHashMap<>();
            plantItem.put("id", topPlant.getId());
            plantItem.put("title", topPlant.getNameCn());
            plantItem.put("description", topPlant.getEfficacy());
            plantItem.put("type", "plant");
            plantItem.put("typeLabel", "植物");
            plantItem.put("tagType", "success");
            plantItem.put("viewCount", topPlant.getViewCount());
            plantItem.put("favoriteCount", topPlant.getFavoriteCount());
            plantItem.put("createdAt", topPlant.getCreatedAt());
            plantItem.put("updatedAt", topPlant.getUpdatedAt());
            featured.put("plant", plantItem);
        }
        if (topK != null) {
            Map<String, Object> knowledgeItem = new LinkedHashMap<>();
            knowledgeItem.put("id", topK.getId());
            knowledgeItem.put("title", topK.getTitle());
            knowledgeItem.put("description", topK.getContent());
            knowledgeItem.put("type", "knowledge");
            knowledgeItem.put("typeLabel", "知识");
            knowledgeItem.put("tagType", "primary");
            knowledgeItem.put("viewCount", topK.getViewCount());
            knowledgeItem.put("favoriteCount", topK.getFavoriteCount());
            knowledgeItem.put("createdAt", topK.getCreatedAt());
            knowledgeItem.put("updatedAt", topK.getUpdatedAt());
            featured.put("knowledge", knowledgeItem);
        }
        if (topI != null) {
            Map<String, Object> inheritorItem = new LinkedHashMap<>();
            inheritorItem.put("id", topI.getId());
            inheritorItem.put("title", topI.getName());
            inheritorItem.put("description", topI.getBio());
            inheritorItem.put("type", "inheritor");
            inheritorItem.put("typeLabel", "传承人");
            inheritorItem.put("tagType", "warning");
            inheritorItem.put("viewCount", topI.getViewCount());
            inheritorItem.put("favoriteCount", topI.getFavoriteCount());
            inheritorItem.put("createdAt", topI.getCreatedAt());
            inheritorItem.put("updatedAt", topI.getUpdatedAt());
            featured.put("inheritor", inheritorItem);
        }

        String[] typeOrder = {"plant", "knowledge", "inheritor"};
        for (String type : typeOrder) {
            if (featured.containsKey(type)) {
                featured.put("top", featured.get(type));
                break;
            }
        }

        return featured;
    }
}
