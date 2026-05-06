package com.dongmedicine.controller.admin;

import com.dongmedicine.common.R;
import com.dongmedicine.mapper.SearchHistoryMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台管理-统计Controller测试")
class AdminStatsControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private InheritorService inheritorService;
    @Mock
    private PlantService plantService;
    @Mock
    private QaService qaService;
    @Mock
    private ResourceService resourceService;
    @Mock
    private QuizService quizService;
    @Mock
    private CommentService commentService;
    @Mock
    private FeedbackService feedbackService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SearchHistoryMapper searchHistoryMapper;

    @InjectMocks
    private AdminStatsController adminStatsController;

    @Test
    @DisplayName("获取统计数据 - 成功")
    void testStats_Success() {
        when(userService.count()).thenReturn(100L);
        when(knowledgeService.count()).thenReturn(50L);
        when(inheritorService.count()).thenReturn(20L);
        when(plantService.count()).thenReturn(30L);
        when(qaService.count()).thenReturn(40L);
        when(resourceService.count()).thenReturn(15L);
        when(quizService.countQuestions()).thenReturn(10L);
        when(commentService.count()).thenReturn(200L);
        when(feedbackService.count()).thenReturn(10L);

        R<Map<String, Long>> result = adminStatsController.stats();

        assertEquals(200, result.getCode());
        Map<String, Long> data = result.getData();
        assertNotNull(data);
        assertEquals(100L, data.get("users"));
        assertEquals(50L, data.get("knowledge"));
        assertEquals(20L, data.get("inheritors"));
        assertEquals(30L, data.get("plants"));
        assertEquals(40L, data.get("qa"));
        assertEquals(15L, data.get("resources"));
        assertEquals(10L, data.get("quiz"));
        assertEquals(200L, data.get("comments"));
        assertEquals(10L, data.get("feedback"));
    }

    @Test
    @DisplayName("获取用户增长趋势 - 成功")
    void testGetUserGrowth_Success() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("date", "2024-01-01");
        row1.put("count", 5L);
        rows.add(row1);
        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("date", "2024-01-02");
        row2.put("count", 3L);
        rows.add(row2);
        when(userMapper.countByDateLast7Days()).thenReturn(rows);

        R<Map<String, Object>> result = adminStatsController.getUserGrowth();

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        List<String> dates = (List<String>) data.get("dates");
        List<Long> counts = (List<Long>) data.get("counts");
        assertNotNull(dates);
        assertNotNull(counts);
        assertEquals(2, dates.size());
        assertEquals("2024-01-01", dates.get(0));
        assertEquals(5L, counts.get(0));
        assertEquals("2024-01-02", dates.get(1));
        assertEquals(3L, counts.get(1));
    }

    @Test
    @DisplayName("获取用户增长趋势 - 空数据")
    void testGetUserGrowth_EmptyData() {
        when(userMapper.countByDateLast7Days()).thenReturn(Collections.emptyList());

        R<Map<String, Object>> result = adminStatsController.getUserGrowth();

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        List<String> dates = (List<String>) data.get("dates");
        List<Long> counts = (List<Long>) data.get("counts");
        assertTrue(dates.isEmpty());
        assertTrue(counts.isEmpty());
    }

    @Test
    @DisplayName("获取内容浏览量排行 - 成功")
    void testGetContentViews_Success() {
        List<Map<String, Object>> plants = new ArrayList<>();
        Map<String, Object> p1 = new LinkedHashMap<>();
        p1.put("name", "钩藤");
        p1.put("value", 500L);
        plants.add(p1);

        List<Map<String, Object>> knowledge = new ArrayList<>();
        Map<String, Object> k1 = new LinkedHashMap<>();
        k1.put("name", "侗医草药");
        k1.put("value", 300L);
        knowledge.add(k1);

        List<Map<String, Object>> inheritors = new ArrayList<>();
        Map<String, Object> i1 = new LinkedHashMap<>();
        i1.put("name", "张三");
        i1.put("value", 200L);
        inheritors.add(i1);

        when(plantService.topByViewCount(10)).thenReturn(plants);
        when(knowledgeService.topByViewCount(10)).thenReturn(knowledge);
        when(inheritorService.topByViewCount(10)).thenReturn(inheritors);

        R<List<Map<String, Object>>> result = adminStatsController.getContentViews();

        assertEquals(200, result.getCode());
        List<Map<String, Object>> data = result.getData();
        assertNotNull(data);
        assertEquals(3, data.size());
        // Should be sorted by value descending
        assertEquals("钩藤", data.get(0).get("name"));
        assertEquals(500L, data.get(0).get("value"));
        assertEquals("侗医草药", data.get(1).get("name"));
        assertEquals("张三", data.get(2).get("name"));
    }

    @Test
    @DisplayName("获取搜索热词 - 成功")
    void testGetSearchKeywords_Success() {
        List<Map<String, Object>> keywords = new ArrayList<>();
        Map<String, Object> kw1 = new LinkedHashMap<>();
        kw1.put("name", "钩藤");
        kw1.put("value", 50L);
        keywords.add(kw1);
        when(searchHistoryMapper.topKeywords(10)).thenReturn(keywords);

        R<List<Map<String, Object>>> result = adminStatsController.getSearchKeywords();

        assertEquals(200, result.getCode());
        List<Map<String, Object>> data = result.getData();
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("钩藤", data.get(0).get("name"));
        assertEquals(50L, data.get(0).get("value"));
        verify(searchHistoryMapper).topKeywords(10);
    }
}
