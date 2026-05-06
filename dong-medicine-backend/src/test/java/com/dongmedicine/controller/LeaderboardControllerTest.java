package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import com.dongmedicine.mapper.UserMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("排行榜Controller测试")
class LeaderboardControllerTest {

    @Mock
    private QuizRecordMapper quizRecordMapper;

    @Mock
    private PlantGameRecordMapper plantGameRecordMapper;

    @Mock
    private UserMapper userMapper;

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
        Map<String, Object> quizRow = new LinkedHashMap<>();
        quizRow.put("user_id", 1);
        quizRow.put("max_score", 90);
        when(quizRecordMapper.selectMaps(any())).thenReturn(List.of(quizRow));

        Map<String, Object> gameRow = new LinkedHashMap<>();
        gameRow.put("user_id", 1);
        gameRow.put("max_score", 80);
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(List.of(gameRow));

        User user = new User();
        user.setId(1);
        user.setUsername("测试用户");
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));

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
        Map<String, Object> quizRow = new LinkedHashMap<>();
        quizRow.put("user_id", 1);
        quizRow.put("max_score", 100);
        when(quizRecordMapper.selectMaps(any())).thenReturn(List.of(quizRow));
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        User user = new User();
        user.setId(1);
        user.setUsername("用户A");
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("quiz", 10);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("综合排行榜 - 空数据")
    void testCombinedLeaderboard_Empty() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("total", 100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("综合排行榜 - limit边界值")
    void testCombinedLeaderboard_LimitBoundary() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("total", 0);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("答题排行榜 - 成功")
    void testQuizLeaderboard_Success() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("user_id", 1);
        row.put("max_score", 95);
        when(quizRecordMapper.selectMaps(any())).thenReturn(List.of(row));

        User user = new User();
        user.setId(1);
        user.setUsername("答题高手");
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));

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
        when(quizRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getQuizLeaderboard(100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("答题排行榜 - null limit使用默认值")
    void testQuizLeaderboard_NullLimit() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getQuizLeaderboard(null);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("游戏排行榜 - 成功")
    void testGameLeaderboard_Success() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("user_id", 2);
        row.put("max_score", 88);
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(List.of(row));

        User user = new User();
        user.setId(2);
        user.setUsername("游戏玩家");
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));

        R<List<Map<String, Object>>> result = leaderboardController.getGameLeaderboard(100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(88, result.getData().get(0).get("score"));
    }

    @Test
    @DisplayName("游戏排行榜 - 空数据")
    void testGameLeaderboard_Empty() {
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getGameLeaderboard(100);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("游戏排行榜 - null limit使用默认值")
    void testGameLeaderboard_NullLimit() {
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getGameLeaderboard(null);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("综合排行榜 - limit上限为200")
    void testCombinedLeaderboard_LimitExceeded() {
        when(quizRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());
        when(plantGameRecordMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        R<List<Map<String, Object>>> result = leaderboardController.getCombinedLeaderboard("total", 999);

        assertEquals(200, result.getCode());
    }
}
