package com.dongmedicine.mq.producer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendStatisticsTask(StatisticsTask task) {
        try {
            String routingKey = RabbitMQConstants.ROUTING_KEY_STATISTICS + "." + task.getStatisticsType();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_TOPIC,
                    routingKey,
                    task
            );
            
            log.debug("统计消息已发送, type={}, targetId={}", task.getStatisticsType(), task.getTargetId());
        } catch (Exception e) {
            log.error("发送统计消息失败, type={}, error={}", task.getStatisticsType(), e.getMessage(), e);
            throw e;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsTask {
        private String statisticsType;
        private Integer targetId;
        private String targetType;
        private String actionType;
        private Integer userId;
        private Long timestamp;
        private String extraData;
    }
}