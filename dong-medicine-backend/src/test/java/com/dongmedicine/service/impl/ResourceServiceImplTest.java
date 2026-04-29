package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.Resource;
import com.dongmedicine.mapper.ResourceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("资源服务测试")
class ResourceServiceImplTest {

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    private Resource testResource;

    private void setBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        setBaseMapper(resourceService, resourceMapper);

        testResource = new Resource();
        testResource.setId(1);
        testResource.setTitle("侗医基础教程");
        testResource.setCategory("教程");
        testResource.setDescription("测试描述");
        testResource.setViewCount(100);
        testResource.setDownloadCount(50);
    }

    @Test
    @DisplayName("按分类和关键词查询 - 成功")
    void testListByCategoryAndKeyword_Success() {
        when(resourceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testResource));

        List<Resource> result = resourceService.listByCategoryAndKeyword("教程", "侗医");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(resourceMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("按分类和关键词查询 - 空参数")
    void testListByCategoryAndKeyword_EmptyParams() {
        when(resourceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testResource));

        List<Resource> result = resourceService.listByCategoryAndKeyword(null, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("获取热门资源 - 成功")
    void testGetHotResources_Success() {
        when(resourceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testResource));

        List<Resource> result = resourceService.getHotResources();

        assertNotNull(result);
        verify(resourceMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("增加下载量 - 成功")
    void testIncrementDownload_Success() {
        doNothing().when(resourceMapper).incrementDownloadCount(anyInt());

        assertDoesNotThrow(() -> resourceService.incrementDownload(1));
        verify(resourceMapper).incrementDownloadCount(1);
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementViewCount_Success() {
        doNothing().when(resourceMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> resourceService.incrementViewCount(1));
        verify(resourceMapper).incrementViewCount(1);
    }

    @Test
    @DisplayName("增加浏览量 - 异常不抛出")
    void testIncrementViewCount_ExceptionNotThrown() {
        doThrow(new RuntimeException("DB Error")).when(resourceMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> resourceService.incrementViewCount(1));
    }

    @Test
    @DisplayName("清除缓存 - 成功")
    void testClearCache_Success() {
        assertDoesNotThrow(() -> resourceService.clearCache());
    }
}
