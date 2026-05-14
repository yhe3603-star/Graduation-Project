package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mapper.OperationLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationLogServiceImplTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

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
        setBaseMapper(operationLogService, operationLogMapper);
    }

    @Test
    @DisplayName("保存操作日志 - 直接委托给ServiceImpl")
    void testSaveDelegatesToSuper() {
        OperationLog log = new OperationLog();
        log.setUserId(1);
        log.setType("LOGIN");
        log.setOperation("用户登录");

        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        boolean result = operationLogService.save(log);

        assertTrue(result);
        verify(operationLogMapper, times(1)).insert(log);
        verify(operationLogMapper, never()).selectCount(any());
    }

    @Test
    @DisplayName("定时清理旧日志 - 日志数量未超过上限时不清理")
    void testCleanOldLogs_NoCleanup() {
        when(operationLogMapper.selectCount(any())).thenReturn(100L);

        operationLogService.cleanOldLogs();

        verify(operationLogMapper, times(1)).selectCount(any());
        verify(operationLogMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("定时清理旧日志 - 日志数量超过上限时清理")
    void testCleanOldLogs_WithCleanup() {
        OperationLog oldLog = new OperationLog();
        oldLog.setId(1);

        when(operationLogMapper.selectCount(any())).thenReturn(600L);
        when(operationLogMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(oldLog));
        when(operationLogMapper.deleteByIds(any())).thenReturn(1);

        operationLogService.cleanOldLogs();

        verify(operationLogMapper, times(1)).selectCount(any());
        verify(operationLogMapper, times(1)).selectList(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("清空所有操作日志 - 成功")
    void testClearAll() {
        when(operationLogMapper.delete(any(QueryWrapper.class))).thenReturn(5);

        operationLogService.clearAll();

        verify(operationLogMapper, times(1)).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("获取最近7天操作日志趋势 - 成功")
    void testGetTrendLast7Days() {
        List<Map<String, Object>> trend = new ArrayList<>();
        trend.add(Map.of("date", "2024-01-01", "count", 10));
        trend.add(Map.of("date", "2024-01-02", "count", 20));

        when(operationLogMapper.selectTrendLast7Days()).thenReturn(trend);

        List<Map<String, Object>> result = operationLogService.getTrendLast7Days();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).get("count"));
        verify(operationLogMapper, times(1)).selectTrendLast7Days();
    }
}
