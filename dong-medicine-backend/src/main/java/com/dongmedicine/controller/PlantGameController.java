package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.service.PlantGameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "识药小游戏", description = "侗乡药用植物识别小游戏")
@RestController
@RequestMapping("/api/plant-game")
@Validated
@RequiredArgsConstructor
public class PlantGameController {

    private final PlantGameService service;

    @PostMapping("/submit")
    public R<Integer> submit(@Valid @RequestBody PlantGameSubmitDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        Integer score = (userId == null) ? service.calculateScore(dto) : service.submit(userId, dto);
        return R.ok(score);
    }

    @GetMapping("/records")
    public R<List<PlantGameRecord>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok(List.of());
        int safeSize = Math.min(Math.max(size != null ? size : 20, 1), 100);
        List<PlantGameRecord> all = service.getUserRecords(userId);
        int start = (Math.max(page, 1) - 1) * safeSize;
        int end = Math.min(start + safeSize, all.size());
        return R.ok(start < all.size() ? all.subList(start, end) : List.of());
    }
}
