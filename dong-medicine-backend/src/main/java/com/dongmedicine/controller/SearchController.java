package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.constant.TargetType;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PlantService;
import com.dongmedicine.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "搜索建议", description = "搜索自动补全与建议")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final PlantService plantService;
    private final KnowledgeService knowledgeService;
    private final InheritorService inheritorService;
    private final SearchHistoryService searchHistoryService;

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggest")
    public R<List<Map<String, Object>>> suggest(@Parameter(name = "keyword", description = "搜索关键词") @RequestParam(defaultValue = "") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return R.ok(List.of());
        }

        searchHistoryService.recordSearch(SecurityUtils.getCurrentUserIdOrNull(), keyword.trim());

        List<Map<String, Object>> results = new ArrayList<>();

        List<Plant> plants = plantService.searchPaged(keyword, 1, 5).getRecords();
        for (Plant p : plants) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", TargetType.PLANT.getValue());
            item.put("id", p.getId());
            item.put("name", p.getNameCn());
            item.put("category", p.getCategory());
            results.add(item);
        }

        List<Knowledge> knowledgeList = knowledgeService.advancedSearchPaged(keyword, null, null, null, "popularity", 1, 5).getRecords();
        for (Knowledge k : knowledgeList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", TargetType.KNOWLEDGE.getValue());
            item.put("id", k.getId());
            item.put("name", k.getTitle());
            item.put("category", k.getType());
            results.add(item);
        }

        List<Inheritor> inheritors = inheritorService.searchPaged(keyword, 1, 5).getRecords();
        for (Inheritor i : inheritors) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", TargetType.INHERITOR.getValue());
            item.put("id", i.getId());
            item.put("name", i.getName());
            item.put("category", i.getLevel());
            results.add(item);
        }

        String kw = keyword.toLowerCase();
        results.sort((a, b) -> {
            String nameA = ((String) a.get("name")).toLowerCase();
            String nameB = ((String) b.get("name")).toLowerCase();
            int scoreA = nameA.equals(kw) ? 3 : nameA.startsWith(kw) ? 2 : nameA.contains(kw) ? 1 : 0;
            int scoreB = nameB.equals(kw) ? 3 : nameB.startsWith(kw) ? 2 : nameB.contains(kw) ? 1 : 0;
            return Integer.compare(scoreB, scoreA);
        });

        return R.ok(results.size() > 15 ? results.subList(0, 15) : results);
    }

    @Operation(summary = "获取热门搜索词")
    @GetMapping("/hot")
    public R<List<Map<String, Object>>> hotKeywords(@Parameter(name = "limit", description = "返回数量", example = "20") @RequestParam(defaultValue = "20") Integer limit) {
        return R.ok(searchHistoryService.topKeywords(Math.min(limit, 50)));
    }
}
