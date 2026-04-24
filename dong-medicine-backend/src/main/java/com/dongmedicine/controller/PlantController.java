package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plants")
@Validated
@RequiredArgsConstructor
public class PlantController {

    private final PlantService service;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String usageWay,
            @RequestParam(required = false) String keyword) {
        Page<Plant> pageResult = service.advancedSearchPaged(keyword, category, usageWay, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<Plant> pageResult = service.searchPaged(keyword, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/{id}")
    public R<Plant> detail(@PathVariable @NotNull Integer id) {
        Plant plant = service.getDetailWithStory(id);
        return plant == null ? R.error("植物不存在") : R.ok(plant);
    }

    @GetMapping("/{id}/similar")
    public R<List<Plant>> similar(@PathVariable @NotNull Integer id) {
        return R.ok(service.getSimilarPlants(id));
    }

    @GetMapping("/random")
    public R<List<Plant>> random(
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "数量不能小于1")
            @Max(value = 100, message = "数量不能大于100") Integer limit) {
        return R.ok(service.getRandomPlants(limit));
    }

    @PostMapping("/batch")
    public R<List<Plant>> batch(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return R.ok(List.of());
        if (ids.size() > 50) return R.error("单次查询不能超过50条");
        return R.ok(service.listByIds(ids));
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
