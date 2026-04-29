package com.dongmedicine.mq.producer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import com.dongmedicine.entity.OperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOperationLog(OperationLog operationLog) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_DIRECT,
                    RabbitMQConstants.ROUTING_KEY_OPERATION_LOG,
                    operationLog
            );
            
            log.debug("操作日志消息已发送, userId={}, operation={}", 
                    operationLog.getUserId(), operationLog.getType());
        } catch (Exception e) {
            log.error("发送操作日志消息失败, operationType={}, error={}", 
                    operationLog.getType(), e.getMessage(), e);
            throw e;
        }
    }
}