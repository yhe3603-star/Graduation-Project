package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.service.PlantGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plant-game")
@Validated
@RequiredArgsConstructor
public class PlantGameController {

    private final PlantGameService service;

    @PostMapping("/submit")
    public R<Integer> submit(@Valid @RequestBody PlantGameSubmitDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Integer score = (userId == null) ? service.calculateScore(dto) : service.submit(userId, dto);
        return R.ok(score);
    }

    @GetMapping("/records")
    public R<List<PlantGameRecord>> records() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userId == null ? List.of() : service.getUserRecords(userId));
    }
}
