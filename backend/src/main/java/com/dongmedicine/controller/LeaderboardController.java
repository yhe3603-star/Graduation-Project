package com.dongmedicine.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dongmedicine.common.R;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.entity.User;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import com.dongmedicine.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final QuizRecordMapper quizRecordMapper;
    private final PlantGameRecordMapper plantGameRecordMapper;
    private final UserMapper userMapper;

    @GetMapping("/combined")
    @RateLimit(5)
    public R<List<Map<String, Object>>> getCombinedLeaderboard(
            @RequestParam(defaultValue = "total") String sortBy,
            @RequestParam(defaultValue = "100") Integer limit) {
        int safeLimit = Math.min(Math.max(limit == null ? 100 : limit, 1), 200);
        Map<Integer, Integer> quizHighest = getHighestQuizScores(safeLimit * 3);
        Map<Integer, Integer> gameHighest = getHighestGameScores(safeLimit * 3);
        Map<Integer, String> userNames = getUserNames(mergeUserIds(quizHighest, gameHighest));

        Set<Integer> allUserIds = mergeUserIds(quizHighest, gameHighest);

        List<Map<String, Object>> leaderboard = new ArrayList<>();
        for (Integer userId : allUserIds) {
            int quizScore = quizHighest.getOrDefault(userId, 0);
            int gameScore = gameHighest.getOrDefault(userId, 0);
            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", userId);
            entry.put("username", userNames.getOrDefault(userId, "未知用户"));
            entry.put("quizScore", quizScore);
            entry.put("gameScore", gameScore);
            entry.put("totalScore", quizScore + gameScore);
            leaderboard.add(entry);
        }

        String sortKey = sortBy.equals("quiz") ? "quizScore" : (sortBy.equals("game") ? "gameScore" : "totalScore");
        leaderboard.sort((a, b) -> (Integer) b.get(sortKey) - (Integer) a.get(sortKey));

        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).put("rank", i + 1);
        }

        if (leaderboard.size() > safeLimit) {
            leaderboard = new ArrayList<>(leaderboard.subList(0, safeLimit));
        }

        return R.ok(leaderboard);
    }

    @GetMapping("/quiz")
    @RateLimit(5)
    public R<List<Map<String, Object>>> getQuizLeaderboard(@RequestParam(defaultValue = "100") Integer limit) {
        int safeLimit = Math.min(Math.max(limit == null ? 100 : limit, 1), 200);
        Map<Integer, Integer> highestScores = getHighestQuizScores(safeLimit);
        Map<Integer, String> userNames = getUserNames(highestScores.keySet());
        return R.ok(buildRankList(highestScores, userNames, safeLimit));
    }

    @GetMapping("/game")
    @RateLimit(5)
    public R<List<Map<String, Object>>> getGameLeaderboard(@RequestParam(defaultValue = "100") Integer limit) {
        int safeLimit = Math.min(Math.max(limit == null ? 100 : limit, 1), 200);
        Map<Integer, Integer> highestScores = getHighestGameScores(safeLimit);
        Map<Integer, String> userNames = getUserNames(highestScores.keySet());
        return R.ok(buildRankList(highestScores, userNames, safeLimit));
    }

    private Set<Integer> mergeUserIds(Map<Integer, Integer> left, Map<Integer, Integer> right) {
        Set<Integer> allUserIds = new HashSet<>();
        allUserIds.addAll(left.keySet());
        allUserIds.addAll(right.keySet());
        return allUserIds;
    }

    private Map<Integer, String> getUserNames(Set<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        return userMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
    }

    private Map<Integer, Integer> getHighestQuizScores(int limit) {
        QueryWrapper<QuizRecord> wrapper = new QueryWrapper<QuizRecord>()
                .select("user_id AS userId", "MAX(score) AS score")
                .groupBy("user_id")
                .orderByDesc("score")
                .last("LIMIT " + Math.max(limit, 1));
        List<Map<String, Object>> rows = quizRecordMapper.selectMaps(wrapper);
        return toScoreMap(rows);
    }

    private Map<Integer, Integer> getHighestGameScores(int limit) {
        QueryWrapper<PlantGameRecord> wrapper = new QueryWrapper<PlantGameRecord>()
                .select("user_id AS userId", "MAX(score) AS score")
                .groupBy("user_id")
                .orderByDesc("score")
                .last("LIMIT " + Math.max(limit, 1));
        List<Map<String, Object>> rows = plantGameRecordMapper.selectMaps(wrapper);
        return toScoreMap(rows);
    }

    private Map<Integer, Integer> toScoreMap(List<Map<String, Object>> rows) {
        Map<Integer, Integer> highest = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object userIdValue = row.get("userId");
            Object scoreValue = row.get("score");
            if (!(userIdValue instanceof Number) || !(scoreValue instanceof Number)) {
                continue;
            }
            int userId = ((Number) userIdValue).intValue();
            int score = ((Number) scoreValue).intValue();
            highest.put(userId, score);
        }
        return highest;
    }

    private List<Map<String, Object>> buildRankList(Map<Integer, Integer> scores, Map<Integer, String> userNames, int limit) {
        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : sorted) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("userId", entry.getKey());
            item.put("username", userNames.getOrDefault(entry.getKey(), "未知用户"));
            item.put("score", entry.getValue());
            result.add(item);
            if (rank > limit) break;
        }
        return result;
    }
}
