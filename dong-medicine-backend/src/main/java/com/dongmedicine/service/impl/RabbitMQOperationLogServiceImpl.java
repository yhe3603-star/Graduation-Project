package com.dongmedicine.service.impl;

import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mq.producer.OperationLogProducer;
import com.dongmedicine.service.RabbitMQOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQOperationLogServiceImpl implements RabbitMQOperationLogService {

    private final OperationLogProducer operationLogProducer;

    @Override
    public void saveLogAsync(OperationLog operationLog) {
        try {
            operationLogProducer.sendOperationLog(operationLog);
            log.debug("操作日志已通过 RabbitMQ 异步保存, userId={}, operation={}", 
                    operationLog.getUserId(), operationLog.getType());
        } catch (Exception e) {
            log.error("通过 RabbitMQ 保存操作日志失败, 将降级为同步保存, error={}", e.getMessage());
            throw e;
        }
    }
}