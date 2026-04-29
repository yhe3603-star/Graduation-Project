package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.mapper.QaMapper;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("问答服务测试")
class QaServiceImplTest {

    @Mock
    private QaMapper qaMapper;

    @InjectMocks
    private QaServiceImpl qaService;

    private Qa testQa;

    @BeforeEach
    void setUp() throws Exception {
        setBaseMapper(qaService, qaMapper);

        testQa = new Qa();
        testQa.setId(1);
        testQa.setQuestion("侗医药有什么特点？");
        testQa.setAnswer("侗医药具有独特的理论体系");
        testQa.setCategory("基础");
        testQa.setPopularity(100);
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

    @Test
    @DisplayName("按分类列表查询 - 成功")
    void testListByCategory_Success() {
        when(qaMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testQa));

        List<Qa> result = qaService.listByCategory("基础");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("按分类列表查询 - 空分类")
    void testListByCategory_EmptyCategory() {
        when(qaMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testQa));

        List<Qa> result = qaService.listByCategory(null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(qaMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testQa));

        List<Qa> result = qaService.search("侗医药");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("搜索 - 空关键词抛异常")
    void testSearch_EmptyKeyword() {
        assertThrows(BusinessException.class, () -> qaService.search(""));
    }

    @Test
    @DisplayName("搜索 - null关键词抛异常")
    void testSearch_NullKeyword() {
        assertThrows(BusinessException.class, () -> qaService.search(null));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementViewCount_Success() {
        doNothing().when(qaMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> qaService.incrementViewCount(1));
        verify(qaMapper).incrementViewCount(1);
    }

    @Test
    @DisplayName("增加浏览量 - 异常不抛出")
    void testIncrementViewCount_Exception() {
        doThrow(new RuntimeException("DB Error")).when(qaMapper).incrementViewCount(anyInt());

        assertDoesNotThrow(() -> qaService.incrementViewCount(1));
    }
}
