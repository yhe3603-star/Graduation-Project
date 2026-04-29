package com.dongmedicine.mq.consumer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogConsumer {

    private final OperationLogMapper operationLogMapper;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_OPERATION_LOG)
    public void handleOperationLog(OperationLog operationLog) {
        try {
            log.debug("收到操作日志消息, userId={}, operation={}", 
                    operationLog.getUserId(), operationLog.getType());

            operationLogMapper.insert(operationLog);
            
            log.debug("操作日志保存成功, id={}", operationLog.getId());
        } catch (Exception e) {
            log.error("处理操作日志消息失败, operationType={}, error={}", 
                    operationLog.getType(), e.getMessage(), e);
            throw e;
        }
    }
}