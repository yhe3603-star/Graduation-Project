package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.service.OperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
@DisplayName("操作日志Controller测试")
class OperationLogControllerTest {

    @Mock
    private OperationLogService logService;

    @InjectMocks
    private OperationLogController operationLogController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private OperationLog testLog;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

        testLog = new OperationLog();
        testLog.setId(1);
        testLog.setUserId(1);
        testLog.setUsername("admin");
        testLog.setModule("植物管理");
        testLog.setOperation("新增植物");
        testLog.setType("CREATE");
        testLog.setMethod("POST");
        testLog.setSuccess(true);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("查询日志列表 - 无筛选条件")
    void testList_NoFilters() {
        List<OperationLog> logs = Arrays.asList(testLog);
        when(logService.list(any(QueryWrapper.class))).thenReturn(logs);

        R<List<OperationLog>> result = operationLogController.list(null, null, null, 100);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(logService).list(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("查询日志列表 - 带模块筛选")
    void testList_WithModule() {
        List<OperationLog> logs = Arrays.asList(testLog);
        when(logService.list(any(QueryWrapper.class))).thenReturn(logs);

        R<List<OperationLog>> result = operationLogController.list("植物管理", null, null, 100);

        assertEquals(200, result.getCode());
        verify(logService).list(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("查询日志列表 - 带类型筛选")
    void testList_WithType() {
        List<OperationLog> logs = Arrays.asList(testLog);
        when(logService.list(any(QueryWrapper.class))).thenReturn(logs);

        R<List<OperationLog>> result = operationLogController.list(null, "CREATE", null, 100);

        assertEquals(200, result.getCode());
        verify(logService).list(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("查询日志列表 - 带用户名筛选")
    void testList_WithUsername() {
        List<OperationLog> logs = Arrays.asList(testLog);
        when(logService.list(any(QueryWrapper.class))).thenReturn(logs);

        R<List<OperationLog>> result = operationLogController.list(null, null, "admin", 100);

        assertEquals(200, result.getCode());
        verify(logService).list(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("查询日志列表 - 带所有筛选条件")
    void testList_WithAllFilters() {
        List<OperationLog> logs = Arrays.asList(testLog);
        when(logService.list(any(QueryWrapper.class))).thenReturn(logs);

        R<List<OperationLog>> result = operationLogController.list("植物管理", "CREATE", "admin", 50);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(logService).list(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("查询日志列表 - limit超上限自动截断为100")
    void testList_LimitCapped() {
        List<OperationLog> logs = Arrays.asList(testLog);
        when(logService.list(any(QueryWrapper.class))).thenReturn(logs);

        R<List<OperationLog>> result = operationLogController.list(null, null, null, 500);

        assertEquals(200, result.getCode());
        verify(logService).list(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询日志 - 成功")
    void testGetById_Success() {
        when(logService.getById(1)).thenReturn(testLog);

        R<OperationLog> result = operationLogController.getById(1);

        assertEquals(200, result.getCode());
        assertEquals("admin", result.getData().getUsername());
        verify(logService).getById(1);
    }

    @Test
    @DisplayName("根据ID查询日志 - 不存在返回null")
    void testGetById_NotFound() {
        when(logService.getById(999)).thenReturn(null);

        R<OperationLog> result = operationLogController.getById(999);

        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("删除日志 - 成功")
    void testDelete_Success() {
        when(logService.removeById(1)).thenReturn(true);

        R<String> result = operationLogController.delete(1);

        assertEquals(200, result.getCode());
        assertEquals("删除成功", result.getData());
        verify(logService).removeById(1);
    }

    @Test
    @DisplayName("批量删除日志 - 成功")
    void testBatchDelete_Success() {
        when(logService.removeById(anyInt())).thenReturn(true);

        R<String> result = operationLogController.batchDelete(new Integer[]{1, 2, 3});

        assertEquals(200, result.getCode());
        assertEquals("批量删除成功", result.getData());
        verify(logService, times(3)).removeById(anyInt());
    }

    @Test
    @DisplayName("清空所有日志 - 成功")
    void testClearAll_Success() {
        doNothing().when(logService).clearAll();

        R<String> result = operationLogController.clearAll();

        assertEquals(200, result.getCode());
        assertEquals("清空成功", result.getData());
        verify(logService).clearAll();
    }

    @Test
    @DisplayName("查询日志统计 - 成功")
    void testStats_Success() {
        List<Map<String, Object>> grouped = new ArrayList<>();
        Map<String, Object> createItem = new HashMap<>();
        createItem.put("type", "CREATE");
        createItem.put("total", 10L);
        grouped.add(createItem);

        Map<String, Object> deleteItem = new HashMap<>();
        deleteItem.put("type", "DELETE");
        deleteItem.put("total", 5L);
        grouped.add(deleteItem);

        when(logService.listMaps(any(QueryWrapper.class))).thenReturn(grouped);
        when(logService.count()).thenReturn(15L);

        R<Map<String, Object>> result = operationLogController.stats();

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertEquals(15L, data.get("total"));
        assertEquals(10L, data.get("CREATE"));
        assertEquals(5L, data.get("DELETE"));
        assertEquals(0L, data.get("UPDATE"));
        assertEquals(0L, data.get("QUERY"));
        verify(logService).listMaps(any(QueryWrapper.class));
        verify(logService).count();
    }

    @Test
    @DisplayName("查询日志统计 - 空数据")
    void testStats_Empty() {
        when(logService.listMaps(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        when(logService.count()).thenReturn(0L);

        R<Map<String, Object>> result = operationLogController.stats();

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertEquals(0L, data.get("total"));
        assertEquals(0L, data.get("CREATE"));
        assertEquals(0L, data.get("UPDATE"));
        assertEquals(0L, data.get("DELETE"));
        assertEquals(0L, data.get("QUERY"));
    }
}
