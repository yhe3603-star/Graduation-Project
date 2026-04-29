package com.dongmedicine.mq.consumer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class FeedbackConsumer {

    private final FeedbackMapper feedbackMapper;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_FEEDBACK)
    public void handleFeedback(Feedback feedback) {
        try {
            log.debug("收到反馈消息, id={}, userId={}, type={}", 
                    feedback.getId(), feedback.getUserId(), feedback.getType());

            feedbackMapper.insert(feedback);
            
            log.info("反馈已保存到数据库, id={}, content={}", 
                    feedback.getId(), 
                    feedback.getContent() != null && feedback.getContent().length() > 50 
                        ? feedback.getContent().substring(0, 50) + "..." 
                        : feedback.getContent());
            
        } catch (Exception e) {
            log.error("处理反馈消息失败, feedbackId={}, error={}", feedback.getId(), e.getMessage(), e);
            throw e;
        }
    }
}