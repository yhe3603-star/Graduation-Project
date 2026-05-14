package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.service.LeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "排行榜", description = "答题与游戏排行榜")
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "获取综合排行榜")
    @GetMapping("/combined")
    @RateLimit(5)
    public R<List<Map<String, Object>>> getCombinedLeaderboard(
            @Parameter(name = "sortBy", description = "排序方式", example = "total") @RequestParam(defaultValue = "total") String sortBy,
            @Parameter(name = "limit", description = "返回数量", example = "100") @RequestParam(defaultValue = "100") Integer limit) {
        return R.ok(leaderboardService.getCombinedLeaderboard(sortBy, limit));
    }

    @Operation(summary = "获取答题排行榜")
    @GetMapping("/quiz")
    @RateLimit(5)
    public R<List<Map<String, Object>>> getQuizLeaderboard(@Parameter(name = "limit", description = "返回数量", example = "100") @RequestParam(defaultValue = "100") Integer limit) {
        return R.ok(leaderboardService.getQuizLeaderboard(limit));
    }

    @Operation(summary = "获取游戏排行榜")
    @GetMapping("/game")
    @RateLimit(5)
    public R<List<Map<String, Object>>> getGameLeaderboard(@Parameter(name = "limit", description = "返回数量", example = "100") @RequestParam(defaultValue = "100") Integer limit) {
        return R.ok(leaderboardService.getGameLeaderboard(limit));
    }
}
