package com.dongmedicine.controller;

import com.dongmedicine.common.R;
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

@RestController
@RequestMapping("/api/plants")
@Validated
@RequiredArgsConstructor
public class PlantController {

    private final PlantService service;

    @GetMapping("/list")
    public R<List<Plant>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String usageWay,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return R.ok(service.search(keyword));
        }
        return R.ok(service.listByDoubleFilter(category, usageWay));
    }

    @GetMapping("/search")
    public R<List<Plant>> search(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "页大小不能小于1")
            @Max(value = 100, message = "页大小不能大于100") Integer pageSize) {
        List<Plant> results = service.search(keyword);
        if (results.size() > pageSize) {
            results = results.subList(0, pageSize);
        }
        return R.ok(results);
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
            @RequestParam @NotBlank(message = "难度不能为空") String difficulty,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "数量不能小于1")
            @Max(value = 100, message = "数量不能大于100") Integer limit) {
        return R.ok(service.getRandomByDifficulty(difficulty, limit));
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
