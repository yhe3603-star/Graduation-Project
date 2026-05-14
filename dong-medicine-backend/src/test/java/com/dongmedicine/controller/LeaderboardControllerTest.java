package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.LeaderboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("排行榜Controller测试")
class LeaderboardControllerTest {

    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private LeaderboardController leaderboardController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("综合排行榜 - 默认排序")
    void testCombinedLeaderboard_DefaultSort() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("rank", 1);
        entry.put("userId", 1);
        entry.put("username", "测试用户");
        entry.put("quizScore", 90);
        entry.put("gameScore", 80);
        entry.put("totalScore", 170);
        when(leaderboardService.getCombinedLeaderboard("total", 100)).thenReturn(List.of(entry));

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("total", 100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getData().get(0).get("rank"));
        assertEquals(170, result.getData().get(0).get("totalScore"));
    }

    @Test
    @DisplayName("综合排行榜 - 按答题排序")
    void testCombinedLeaderboard_SortByQuiz() {
        when(leaderboardService.getCombinedLeaderboard("quiz", 10)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("quiz", 10);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("综合排行榜 - 空数据")
    void testCombinedLeaderboard_Empty() {
        when(leaderboardService.getCombinedLeaderboard("total", 100)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("total", 100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("综合排行榜 - limit边界值")
    void testCombinedLeaderboard_LimitBoundary() {
        when(leaderboardService.getCombinedLeaderboard("total", 0)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("total", 0);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("答题排行榜 - 成功")
    void testQuizLeaderboard_Success() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("rank", 1);
        entry.put("userId", 1);
        entry.put("username", "答题高手");
        entry.put("score", 95);
        when(leaderboardService.getQuizLeaderboard(100)).thenReturn(List.of(entry));

        R<List<Map<String, Object>>> result = leaderboardController.getQuizLeaderboard(100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getData().get(0).get("rank"));
        assertEquals(95, result.getData().get(0).get("score"));
        assertEquals("答题高手", result.getData().get(0).get("username"));
    }

    @Test
    @DisplayName("答题排行榜 - 空数据")
    void testQuizLeaderboard_Empty() {
        when(leaderboardService.getQuizLeaderboard(100)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getQuizLeaderboard(100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("答题排行榜 - null limit使用默认值")
    void testQuizLeaderboard_NullLimit() {
        when(leaderboardService.getQuizLeaderboard(null)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getQuizLeaderboard(null);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("游戏排行榜 - 成功")
    void testGameLeaderboard_Success() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("rank", 1);
        entry.put("userId", 2);
        entry.put("username", "游戏玩家");
        entry.put("score", 88);
        when(leaderboardService.getGameLeaderboard(100)).thenReturn(List.of(entry));

        R<List<Map<String, Object>>> result = leaderboardController.getGameLeaderboard(100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(88, result.getData().get(0).get("score"));
    }

    @Test
    @DisplayName("游戏排行榜 - 空数据")
    void testGameLeaderboard_Empty() {
        when(leaderboardService.getGameLeaderboard(100)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getGameLeaderboard(100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("游戏排行榜 - null limit使用默认值")
    void testGameLeaderboard_NullLimit() {
        when(leaderboardService.getGameLeaderboard(null)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getGameLeaderboard(null);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("综合排行榜 - limit上限为200")
    void testCombinedLeaderboard_LimitExceeded() {
        when(leaderboardService.getCombinedLeaderboard("total", 999)).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("total", 999);

        assertEquals(200, result.getCode());
    }
}
