package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.entity.QuizRecord;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final QuizRecordMapper quizRecordMapper;
    private final PlantGameRecordMapper plantGameRecordMapper;
    private final UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getCombinedLeaderboard(String sortBy, Integer limit) {
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
        leaderboard.sort((a, b) -> {
            int vb = scoreAsInt(b.get(sortKey));
            int va = scoreAsInt(a.get(sortKey));
            return Integer.compare(vb, va);
        });

        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).put("rank", i + 1);
        }

        if (leaderboard.size() > safeLimit) {
            leaderboard = new ArrayList<>(leaderboard.subList(0, safeLimit));
        }

        return leaderboard;
    }

    @Override
    public List<Map<String, Object>> getQuizLeaderboard(Integer limit) {
        int safeLimit = Math.min(Math.max(limit == null ? 100 : limit, 1), 200);
        Map<Integer, Integer> highestScores = getHighestQuizScores(safeLimit);
        Map<Integer, String> userNames = getUserNames(highestScores.keySet());
        return buildRankList(highestScores, userNames, safeLimit);
    }

    @Override
    public List<Map<String, Object>> getGameLeaderboard(Integer limit) {
        int safeLimit = Math.min(Math.max(limit == null ? 100 : limit, 1), 200);
        Map<Integer, Integer> highestScores = getHighestGameScores(safeLimit);
        Map<Integer, String> userNames = getUserNames(highestScores.keySet());
        return buildRankList(highestScores, userNames, safeLimit);
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
                .select("user_id", "MAX(score) AS max_score")
                .groupBy("user_id")
                .orderByDesc("max_score")
                .last("LIMIT " + Math.max(limit, 1));
        List<Map<String, Object>> rows = quizRecordMapper.selectMaps(wrapper);
        return toScoreMap(rows, "user_id", "max_score");
    }

    private Map<Integer, Integer> getHighestGameScores(int limit) {
        QueryWrapper<PlantGameRecord> wrapper = new QueryWrapper<PlantGameRecord>()
                .select("user_id", "MAX(score) AS max_score")
                .groupBy("user_id")
                .orderByDesc("max_score")
                .last("LIMIT " + Math.max(limit, 1));
        List<Map<String, Object>> rows = plantGameRecordMapper.selectMaps(wrapper);
        return toScoreMap(rows, "user_id", "max_score");
    }

    private Map<Integer, Integer> toScoreMap(List<Map<String, Object>> rows, String userIdKey, String scoreKey) {
        Map<Integer, Integer> highest = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object userIdValue = getRowValueIgnoreCase(row, userIdKey);
            Object scoreValue = getRowValueIgnoreCase(row, scoreKey);
            if (userIdValue == null || scoreValue == null) {
                continue;
            }
            if (!(userIdValue instanceof Number) || !(scoreValue instanceof Number)) {
                continue;
            }
            int userId = ((Number) userIdValue).intValue();
            int score = ((Number) scoreValue).intValue();
            highest.put(userId, score);
        }
        return highest;
    }

    /** JDBC/MySQL 返回的列名大小写因驱动而异 */
    private static Object getRowValueIgnoreCase(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    private static int scoreAsInt(Object v) {
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return 0;
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
