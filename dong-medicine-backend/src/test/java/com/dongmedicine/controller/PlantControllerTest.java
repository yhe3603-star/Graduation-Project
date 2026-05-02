package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.service.PlantService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("植物Controller测试")
class PlantControllerTest {

    @Mock
    private PlantService service;

    @InjectMocks
    private PlantController plantController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private Plant testPlant;
    private Page<Plant> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testPlant = new Plant();
        testPlant.setId(1);
        testPlant.setNameCn("钩藤");
        testPlant.setCategory("清热药");

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testPlant));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.advancedSearchPaged(any(), any(), any(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = plantController.list(1, 12, null, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).advancedSearchPaged(isNull(), isNull(), isNull(), eq(1), eq(12));
    }

    @Test
    @DisplayName("列表查询 - 带关键词和分类")
    void testList_WithFilters() {
        when(service.advancedSearchPaged(eq("钩藤"), eq("清热药"), eq("内服"), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = plantController.list(1, 12, "清热药", "内服", "钩藤");

        assertEquals(200, result.getCode());
        verify(service).advancedSearchPaged("钩藤", "清热药", "内服", 1, 12);
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.searchPaged(eq("钩藤"), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = plantController.search("钩藤", 1, 12);

        assertEquals(200, result.getCode());
        verify(service).searchPaged("钩藤", 1, 12);
    }

    @Test
    @DisplayName("详情 - 成功")
    void testDetail_Success() {
        when(service.getDetailWithStory(1)).thenReturn(testPlant);

        R<Plant> result = plantController.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("钩藤", result.getData().getNameCn());
    }

    @Test
    @DisplayName("详情 - 不存在抛异常")
    void testDetail_NotFound() {
        when(service.getDetailWithStory(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> plantController.detail(999));
    }

    @Test
    @DisplayName("相似植物 - 成功")
    void testSimilar_Success() {
        when(service.getSimilarPlants(1)).thenReturn(Arrays.asList(testPlant));

        R<List<Plant>> result = plantController.similar(1);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("随机植物 - 成功")
    void testRandom_Success() {
        when(service.getRandomPlants(20)).thenReturn(Arrays.asList(testPlant));

        R<List<Plant>> result = plantController.random(20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("批量查询 - 成功")
    void testBatch_Success() {
        when(service.listByIds(anyList())).thenReturn(Arrays.asList(testPlant));

        R<List<Plant>> result = plantController.batch(Arrays.asList(1));

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("批量查询 - 空列表")
    void testBatch_EmptyList() {
        R<List<Plant>> result = plantController.batch(List.of());

        assertEquals(200, result.getCode());
        verify(service, never()).listByIds(anyList());
    }

    @Test
    @DisplayName("批量查询 - 超过50条")
    void testBatch_OverLimit() {
        List<Integer> ids = java.util.stream.IntStream.range(0, 51).boxed().toList();

        R<List<Plant>> result = plantController.batch(ids);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        doNothing().when(service).incrementViewCount(1);

        R<String> result = plantController.incrementView(1);

        assertEquals(200, result.getCode());
        verify(service).incrementViewCount(1);
    }
}
