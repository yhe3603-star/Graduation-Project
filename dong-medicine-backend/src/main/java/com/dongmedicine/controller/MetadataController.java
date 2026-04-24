package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PlantService;
import com.dongmedicine.service.QaService;
import com.dongmedicine.service.ResourceService;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.entity.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final PlantService plantService;
    private final KnowledgeService knowledgeService;
    private final QaService qaService;
    private final InheritorService inheritorService;
    private final ResourceService resourceService;

    @GetMapping("/filters")
    @Cacheable(value = "hotData", key = "'allFilters'")
    public R<Map<String, Object>> getAllFilters() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("plants", getPlantFilters());
        filters.put("knowledge", getKnowledgeFilters());
        filters.put("qa", getQaFilters());
        filters.put("inheritors", getInheritorFilters());
        filters.put("resources", getResourceFilters());
        return R.ok(filters);
    }

    private Map<String, List<String>> getPlantFilters() {
        List<Plant> all = plantService.list();
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("category", extractDistinct(all, Plant::getCategory));
        map.put("usageWay", extractDistinct(all, Plant::getUsageWay));
        return map;
    }

    private Map<String, List<String>> getKnowledgeFilters() {
        List<Knowledge> all = knowledgeService.list();
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("therapyCategory", extractDistinct(all, Knowledge::getTherapyCategory));
        map.put("diseaseCategory", extractDistinct(all, Knowledge::getDiseaseCategory));
        map.put("herbCategory", extractDistinct(all, Knowledge::getHerbCategory));
        return map;
    }

    private Map<String, List<String>> getQaFilters() {
        List<Qa> all = qaService.list();
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("category", extractDistinct(all, Qa::getCategory));
        return map;
    }

    private Map<String, List<String>> getInheritorFilters() {
        List<Inheritor> all = inheritorService.list();
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("level", extractDistinct(all, Inheritor::getLevel));
        return map;
    }

    private Map<String, List<String>> getResourceFilters() {
        List<Resource> all = resourceService.list();
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("category", extractDistinct(all, Resource::getCategory));
        return map;
    }

    private <T> List<String> extractDistinct(List<T> list, java.util.function.Function<T, String> getter) {
        return list.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
