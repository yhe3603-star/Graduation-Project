package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.service.PlantGameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "识药小游戏", description = "侗乡药用植物识别小游戏")
@RestController
@RequestMapping("/api/plant-game")
@Validated
@RequiredArgsConstructor
public class PlantGameController {

    private final PlantGameService service;

    @PostMapping("/submit")
    @RateLimit(value = 10, key = "plant_game_submit")
    public R<Integer> submit(@Valid @RequestBody PlantGameSubmitDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        Integer score = (userId == null) ? service.calculateScore(dto) : service.submit(userId, dto);
        return R.ok(score);
    }

    @GetMapping("/records")
    public R<Map<String, Object>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Integer userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return R.ok(PageUtils.toMap(new Page<>(page, size)));
        Page<PlantGameRecord> pageResult = service.getUserRecordsPaged(userId, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }
}
