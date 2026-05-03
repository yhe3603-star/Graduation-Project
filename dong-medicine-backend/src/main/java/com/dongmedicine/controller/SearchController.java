package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.entity.SearchHistory;
import com.dongmedicine.mapper.SearchHistoryMapper;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PlantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "搜索建议", description = "搜索自动补全与建议")
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final PlantService plantService;
    private final KnowledgeService knowledgeService;
    private final InheritorService inheritorService;
    private final SearchHistoryMapper searchHistoryMapper;

    @GetMapping("/suggest")
    public R<List<Map<String, Object>>> suggest(@RequestParam(defaultValue = "") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return R.ok(List.of());
        }

        try {
            SearchHistory sh = new SearchHistory();
            sh.setUserId(SecurityUtils.getCurrentUserIdOrNull());
            sh.setKeyword(keyword.trim());
            searchHistoryMapper.insert(sh);
        } catch (Exception e) {
            log.debug("记录搜索历史失败: {}", e.getMessage());
        }

        List<Map<String, Object>> results = new ArrayList<>();

        // Plant name suggestions (top 5)
        List<Plant> plants = plantService.searchPaged(keyword, 1, 5).getRecords();
        for (Plant p : plants) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "plant");
            item.put("id", p.getId());
            item.put("name", p.getNameCn());
            item.put("category", p.getCategory());
            results.add(item);
        }

        // Knowledge title suggestions (top 5)
        List<Knowledge> knowledgeList = knowledgeService.advancedSearchPaged(keyword, null, null, null, "popularity", 1, 5).getRecords();
        for (Knowledge k : knowledgeList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "knowledge");
            item.put("id", k.getId());
            item.put("name", k.getTitle());
            item.put("category", k.getType());
            results.add(item);
        }

        // Inheritor name suggestions (top 5)
        List<Inheritor> inheritors = inheritorService.searchPaged(keyword, 1, 5).getRecords();
        for (Inheritor i : inheritors) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "inheritor");
            item.put("id", i.getId());
            item.put("name", i.getName());
            item.put("category", i.getLevel());
            results.add(item);
        }

        // Sort by relevance (simple: exact match > starts with > contains)
        String kw = keyword.toLowerCase();
        results.sort((a, b) -> {
            String nameA = ((String) a.get("name")).toLowerCase();
            String nameB = ((String) b.get("name")).toLowerCase();
            int scoreA = nameA.equals(kw) ? 3 : nameA.startsWith(kw) ? 2 : nameA.contains(kw) ? 1 : 0;
            int scoreB = nameB.equals(kw) ? 3 : nameB.startsWith(kw) ? 2 : nameB.contains(kw) ? 1 : 0;
            return Integer.compare(scoreB, scoreA);
        });

        // Limit to 15 total
        return R.ok(results.size() > 15 ? results.subList(0, 15) : results);
    }

    @GetMapping("/hot")
    public R<List<Map<String, Object>>> hotKeywords(@RequestParam(defaultValue = "20") Integer limit) {
        return R.ok(searchHistoryMapper.topKeywords(Math.min(limit, 50)));
    }
}
