package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.mapper.InheritorMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("传承人服务测试")
class InheritorServiceImplTest {

    @Mock
    private InheritorMapper inheritorMapper;

    @InjectMocks
    private InheritorServiceImpl inheritorService;

    private Inheritor testInheritor;

    @BeforeEach
    void setUp() {
        testInheritor = new Inheritor();
        testInheritor.setId(1);
        testInheritor.setName("张三");
        testInheritor.setLevel("国家级");
        testInheritor.setSpecialties("侗医火攻疗法");
        testInheritor.setExperienceYears(30);
        testInheritor.setViewCount(100);
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(inheritorMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testInheritor));

        List<Inheritor> result = inheritorService.search("张三");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("张三", result.get(0).getName());
    }

    @Test
    @DisplayName("搜索 - 空关键词抛出异常")
    void testSearch_EmptyKeyword_ThrowsException() {
        assertThrows(BusinessException.class, () -> inheritorService.search(""));
        assertThrows(BusinessException.class, () -> inheritorService.search(null));
    }

    @Test
    @DisplayName("搜索 - 无结果返回空列表")
    void testSearch_NoResults() {
        when(inheritorMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Inheritor> result = inheritorService.search("不存在的人");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("按级别列表查询 - 成功")
    void testListByLevel_Success() {
        when(inheritorMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testInheritor));

        List<Inheritor> result = inheritorService.listByLevel("国家级", "experience");

        assertNotNull(result);
        verify(inheritorMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("按级别列表查询 - 空级别返回全部")
    void testListByLevel_EmptyLevel() {
        when(inheritorMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testInheritor));

        List<Inheritor> result = inheritorService.listByLevel(null, "experience");

        assertNotNull(result);
    }

    @Test
    @DisplayName("获取详情 - 成功")
    void testGetDetailWithExtras_Success() {
        when(inheritorMapper.selectById(1)).thenReturn(testInheritor);

        Inheritor result = inheritorService.getDetailWithExtras(1);

        assertNotNull(result);
        assertEquals("张三", result.getName());
    }

    @Test
    @DisplayName("获取详情 - 不存在返回null")
    void testGetDetailWithExtras_NotFound() {
        when(inheritorMapper.selectById(999)).thenReturn(null);

        Inheritor result = inheritorService.getDetailWithExtras(999);

        assertNull(result);
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementViewCount_Success() {
        doNothing().when(inheritorMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> inheritorService.incrementViewCount(1));
        verify(inheritorMapper).incrementViewCount(1);
    }

    @Test
    @DisplayName("增加浏览量 - 异常不抛出")
    void testIncrementViewCount_ExceptionNotThrown() {
        doThrow(new RuntimeException("DB Error")).when(inheritorMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> inheritorService.incrementViewCount(1));
    }
}
