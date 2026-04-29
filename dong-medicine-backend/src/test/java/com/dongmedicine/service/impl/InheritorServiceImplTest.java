package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.common.util.FileCleanupHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InheritorServiceImplTest {

    @Mock
    private InheritorMapper inheritorMapper;

    @Mock
    private FileCleanupHelper fileCleanupHelper;

    private InheritorServiceImpl inheritorService;

    @BeforeEach
    void setUp() throws Exception {
        inheritorService = new InheritorServiceImpl();
        setBaseMapper(inheritorService, inheritorMapper);
        setField(inheritorService, "inheritorMapper", inheritorMapper);
        setField(inheritorService, "fileCleanupHelper", fileCleanupHelper);
    }

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

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Test
    @DisplayName("搜索传承人 - 返回空列表")
    void searchEmpty() {
        when(inheritorMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Inheritor> result = inheritorService.search("不存在的名字");
        assertNotNull(result);
    }

    @Test
    @DisplayName("按级别查询 - 空列表")
    void getByLevelEmpty() {
        when(inheritorMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Inheritor> result = inheritorService.getByLevel("国家级");
        assertNotNull(result);
    }

    @Test
    @DisplayName("获取详情 - 存在的ID")
    void getDetailByIdExists() {
        Inheritor inheritor = new Inheritor();
        inheritor.setId(1);
        inheritor.setName("测试传承人");
        when(inheritorMapper.selectById(1)).thenReturn(inheritor);

        Inheritor result = inheritorService.getDetailById(1);
        assertNotNull(result);
        assertEquals("测试传承人", result.getName());
    }

    @Test
    @DisplayName("获取详情 - 不存在的ID")
    void getDetailByIdNotExists() {
        when(inheritorMapper.selectById(999)).thenReturn(null);

        Inheritor result = inheritorService.getDetailById(999);
        assertNull(result);
    }

    @Test
    @DisplayName("增加浏览次数")
    void incrementViewCount() {
        doNothing().when(inheritorMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> {
            inheritorService.incrementViewCount(1);
        });

        verify(inheritorMapper).incrementViewCount(1);
    }

    @Test
    @DisplayName("增加浏览次数 - 异常被静默处理")
    void incrementViewCountExceptionHandled() {
        doThrow(new RuntimeException("DB error")).when(inheritorMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> {
            inheritorService.incrementViewCount(1);
        });
    }

    @Test
    @DisplayName("删除传承人及关联文件")
    void deleteWithFiles() {
        Inheritor inheritor = new Inheritor();
        inheritor.setId(1);
        inheritor.setName("测试传承人");
        inheritor.setImages("[\"img1.jpg\"]");
        inheritor.setVideos("[\"vid1.mp4\"]");
        inheritor.setDocuments("[\"doc1.pdf\"]");
        when(inheritorMapper.selectById(1)).thenReturn(inheritor);
        when(inheritorMapper.deleteById(1)).thenReturn(1);
        doNothing().when(fileCleanupHelper).deleteFilesFromJson(anyString());

        assertDoesNotThrow(() -> {
            inheritorService.deleteWithFiles(1);
        });

        verify(fileCleanupHelper).deleteFilesFromJson("[\"img1.jpg\"]");
        verify(fileCleanupHelper).deleteFilesFromJson("[\"vid1.mp4\"]");
        verify(fileCleanupHelper).deleteFilesFromJson("[\"doc1.pdf\"]");
    }

    @Test
    @DisplayName("删除传承人 - 不存在时正常返回")
    void deleteWithFilesNotFound() {
        when(inheritorMapper.selectById(999)).thenReturn(null);

        assertDoesNotThrow(() -> {
            inheritorService.deleteWithFiles(999);
        });

        verify(inheritorMapper, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("获取统计数据")
    void getStats() {
        when(inheritorMapper.selectCount(any())).thenReturn(10L);
        when(inheritorMapper.countByLevel("自治区级")).thenReturn(3);
        when(inheritorMapper.countByLevel("市级")).thenReturn(5);
        when(inheritorMapper.countByLevel("县级")).thenReturn(2);
        when(inheritorMapper.sumViewCount()).thenReturn(100L);
        when(inheritorMapper.sumFavoriteCount()).thenReturn(50L);

        var stats = inheritorService.getStats();
        assertNotNull(stats);
        assertEquals(10L, stats.get("total"));
        assertEquals(3, stats.get("regionLevelCount"));
        assertEquals(100L, stats.get("totalViews"));
    }
}
