package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PlantService;
import com.dongmedicine.service.QaService;
import com.dongmedicine.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

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
        filters.put("plants", plantService.getFilterOptions());
        filters.put("knowledge", knowledgeService.getFilterOptions());
        filters.put("qa", qaService.getFilterOptions());
        filters.put("inheritors", inheritorService.getFilterOptions());
        filters.put("resources", resourceService.getFilterOptions());
        return R.ok(filters);
    }
}
