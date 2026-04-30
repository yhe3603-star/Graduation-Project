package com.dongmedicine.mq.producer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import com.dongmedicine.entity.Feedback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class FeedbackProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendFeedback(Feedback feedback) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_DIRECT,
                    RabbitMQConstants.ROUTING_KEY_FEEDBACK,
                    feedback
            );
            
            log.debug("反馈消息已发送, id={}, userId={}", feedback.getId(), feedback.getUserId());
        } catch (Exception e) {
            log.error("发送反馈消息失败, feedbackId={}, error={}", feedback.getId(), e.getMessage(), e);
            throw e;
        }
    }
}