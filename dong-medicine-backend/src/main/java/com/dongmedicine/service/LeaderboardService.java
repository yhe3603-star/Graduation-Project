package com.dongmedicine.service;

import java.util.List;
import java.util.Map;

/**
 * 排行榜服务接口
 * 提供答题与游戏排行榜查询功能
 */
public interface LeaderboardService {

    /**
     * 获取综合排行榜
     *
     * @param sortBy 排序方式: total/quiz/game
     * @param limit  返回条数限制
     * @return 排行榜列表
     */
    List<Map<String, Object>> getCombinedLeaderboard(String sortBy, Integer limit);

    /**
     * 获取答题排行榜
     *
     * @param limit 返回条数限制
     * @return 排行榜列表
     */
    List<Map<String, Object>> getQuizLeaderboard(Integer limit);

    /**
     * 获取游戏排行榜
     *
     * @param limit 返回条数限制
     * @return 排行榜列表
     */
    List<Map<String, Object>> getGameLeaderboard(Integer limit);
}
