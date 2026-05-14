package com.dongmedicine.service.impl;

import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import com.dongmedicine.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("排行榜服务测试")
class LeaderboardServiceImplTest {

    @Mock
    private QuizRecordMapper quizRecordMapper;

    @Mock
    private PlantGameRecordMapper plantGameRecordMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    @Test
    @DisplayName("综合排行榜 - 无数据返回空列表")
    void testCombinedLeaderboard_Empty() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(List.of());
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(List.of());

        List<Map<String, Object>> result = leaderboardService.getCombinedLeaderboard("total", 10);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("综合排行榜 - 有数据时正确排序")
    void testCombinedLeaderboard_WithData() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(List.of(
                Map.of("user_id", 1, "max_score", 90),
                Map.of("user_id", 2, "max_score", 80)
        ));
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(List.of(
                Map.of("user_id", 1, "max_score", 70),
                Map.of("user_id", 3, "max_score", 95)
        ));
        User user1 = new User(); user1.setId(1); user1.setUsername("alice");
        User user2 = new User(); user2.setId(2); user2.setUsername("bob");
        User user3 = new User(); user3.setId(3); user3.setUsername("charlie");
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user1, user2, user3));

        List<Map<String, Object>> result = leaderboardService.getCombinedLeaderboard("total", 10);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).get("userId"));
        assertEquals(160, result.get(0).get("totalScore"));
    }

    @Test
    @DisplayName("综合排行榜 - limit参数正确截断")
    void testCombinedLeaderboard_LimitTruncation() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(List.of(
                Map.of("user_id", 1, "max_score", 90)
        ));
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(List.of());
        User user1 = new User(); user1.setId(1); user1.setUsername("alice");
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user1));

        List<Map<String, Object>> result = leaderboardService.getCombinedLeaderboard("total", 1);
        assertTrue(result.size() <= 1);
    }

    @Test
    @DisplayName("Quiz排行榜 - 无数据返回空列表")
    void testQuizLeaderboard_Empty() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(List.of());
        List<Map<String, Object>> result = leaderboardService.getQuizLeaderboard(10);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Game排行榜 - 无数据返回空列表")
    void testGameLeaderboard_Empty() {
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(List.of());
        List<Map<String, Object>> result = leaderboardService.getGameLeaderboard(10);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
