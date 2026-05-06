package com.dongmedicine.service.impl;

import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mq.producer.OperationLogProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQOperationLogServiceImplTest {

    @Mock
    private OperationLogProducer operationLogProducer;

    @InjectMocks
    private RabbitMQOperationLogServiceImpl rabbitMQOperationLogService;

    private OperationLog testLog;

    @BeforeEach
    void setUp() {
        testLog = new OperationLog();
        testLog.setUserId(1);
        testLog.setType("LOGIN");
        testLog.setOperation("用户登录");
        testLog.setModule("auth");
    }

    @Test
    @DisplayName("异步保存操作日志 - 成功发送到RabbitMQ")
    void testSaveLogAsyncSuccess() {
        doNothing().when(operationLogProducer).sendOperationLog(testLog);

        assertDoesNotThrow(() -> rabbitMQOperationLogService.saveLogAsync(testLog));
        verify(operationLogProducer, times(1)).sendOperationLog(testLog);
    }

    @Test
    @DisplayName("异步保存操作日志 - 发送失败抛出异常")
    void testSaveLogAsyncFailure() {
        doThrow(new RuntimeException("MQ连接失败")).when(operationLogProducer).sendOperationLog(testLog);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> rabbitMQOperationLogService.saveLogAsync(testLog));
        assertEquals("MQ连接失败", exception.getMessage());
        verify(operationLogProducer, times(1)).sendOperationLog(testLog);
    }
}
