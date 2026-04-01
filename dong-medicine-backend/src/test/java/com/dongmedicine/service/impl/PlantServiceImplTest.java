package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.mapper.PlantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantServiceImplTest {

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private FileCleanupHelper fileCleanupHelper;

    @InjectMocks
    private PlantServiceImpl plantService;

    private Plant testPlant;

    @BeforeEach
    void setUp() {
        testPlant = new Plant();
        testPlant.setId(1);
        testPlant.setNameCn("钩藤");
        testPlant.setNameDong("Gouteng");
        testPlant.setCategory("清热药");
        testPlant.setUsageWay("内服");
        testPlant.setEfficacy("清热平肝，息风止痉");
        testPlant.setViewCount(100);
        testPlant.setFavoriteCount(10);
        testPlant.setPopularity(50);
    }

    @Test
    @DisplayName("根据分类和用法过滤 - 成功")
    void testAdvancedSearchSuccess() {
        List<Plant> plants = Arrays.asList(testPlant);
        when(plantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(plants);

        List<Plant> result = plantService.advancedSearch(null, "清热药", "内服");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("钩藤", result.get(0).getNameCn());
    }

    @Test
    @DisplayName("根据分类和用法过滤 - 空参数")
    void testAdvancedSearchEmptyParams() {
        List<Plant> plants = Arrays.asList(testPlant);
        when(plantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(plants);

        List<Plant> result = plantService.advancedSearch(null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("搜索植物 - 成功")
    void testSearchSuccess() {
        List<Plant> plants = Arrays.asList(testPlant);
        when(plantMapper.searchByFullText(anyString(), anyInt())).thenReturn(plants);

        List<Plant> result = plantService.search("钩藤");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("搜索植物 - 关键词为空")
    void testSearchEmptyKeyword() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            plantService.search("");
        });

        assertTrue(exception.getMessage().contains("关键词"));
    }

    @Test
    @DisplayName("搜索植物 - 限制数量超出范围")
    void testSearchInvalidLimit() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            plantService.search("钩藤", 200);
        });

        assertTrue(exception.getMessage().contains("限制数量"));
    }

    @Test
    @DisplayName("获取相似植物 - 成功")
    void testGetSimilarPlantsSuccess() {
        Plant similarPlant = new Plant();
        similarPlant.setId(2);
        similarPlant.setNameCn("相似植物");
        similarPlant.setCategory("清热药");

        when(plantMapper.selectById(1)).thenReturn(testPlant);
        when(plantMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(similarPlant));

        List<Plant> result = plantService.getSimilarPlants(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取相似植物 - 植物不存在")
    void testGetSimilarPlantsNotFound() {
        when(plantMapper.selectById(999)).thenReturn(null);

        List<Plant> result = plantService.getSimilarPlants(999);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取植物详情 - 成功")
    void testGetDetailWithStorySuccess() {
        when(plantMapper.selectById(1)).thenReturn(testPlant);

        Plant result = plantService.getDetailWithStory(1);

        assertNotNull(result);
        assertEquals("钩藤", result.getNameCn());
    }

    @Test
    @DisplayName("获取植物详情 - 不存在")
    void testGetDetailWithStoryNotFound() {
        when(plantMapper.selectById(999)).thenReturn(null);

        Plant result = plantService.getDetailWithStory(999);

        assertNull(result);
    }

    @Test
    @DisplayName("增加浏览次数 - 成功")
    void testIncrementViewCountSuccess() {
        doNothing().when(plantMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> {
            plantService.incrementViewCount(1);
        });

        verify(plantMapper, times(1)).incrementViewCount(1);
    }

    @Test
    @DisplayName("增加浏览次数 - 异常不抛出")
    void testIncrementViewCountException() {
        doThrow(new RuntimeException("DB Error")).when(plantMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> {
            plantService.incrementViewCount(1);
        });
    }

    @Test
    @DisplayName("删除植物及文件 - 成功")
    void testDeleteWithFilesSuccess() {
        testPlant.setImages("[\"image1.jpg\"]");
        testPlant.setVideos("[\"video1.mp4\"]");
        testPlant.setDocuments("[\"doc1.pdf\"]");

        when(plantMapper.selectById(1)).thenReturn(testPlant);
        doNothing().when(fileCleanupHelper).deleteFilesFromJson(anyString());
        when(plantMapper.deleteById(1)).thenReturn(1);

        assertDoesNotThrow(() -> {
            plantService.deleteWithFiles(1);
        });

        verify(fileCleanupHelper, times(1)).deleteFilesFromJson("[\"image1.jpg\"]");
        verify(fileCleanupHelper, times(1)).deleteFilesFromJson("[\"video1.mp4\"]");
        verify(fileCleanupHelper, times(1)).deleteFilesFromJson("[\"doc1.pdf\"]");
        verify(plantMapper, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("删除植物及文件 - 植物不存在")
    void testDeleteWithFilesNotFound() {
        when(plantMapper.selectById(999)).thenReturn(null);

        assertDoesNotThrow(() -> {
            plantService.deleteWithFiles(999);
        });

        verify(plantMapper, never()).deleteById(anyInt());
    }
}
