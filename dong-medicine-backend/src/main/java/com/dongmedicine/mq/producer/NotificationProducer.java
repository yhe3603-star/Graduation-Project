package com.dongmedicine.mq.producer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationMessage notification) {
        try {
            if (notification.getCreateTime() == null) {
                notification.setCreateTime(LocalDateTime.now());
            }
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_DIRECT,
                    RabbitMQConstants.ROUTING_KEY_NOTIFICATION,
                    notification
            );
            
            log.debug("通知消息已发送, type={}, userId={}, title={}", 
                    notification.getType(), notification.getUserId(), notification.getTitle());
        } catch (Exception e) {
            log.error("发送通知消息失败, type={}, error={}", notification.getType(), e.getMessage(), e);
            throw e;
        }
    }

    public void sendFeedbackReplyNotification(Integer userId, String feedbackTitle, String reply) {
        NotificationMessage notification = new NotificationMessage();
        notification.setUserId(userId);
        notification.setType("feedback_reply");
        notification.setTitle("您的反馈已有新回复");
        notification.setContent("反馈标题: " + feedbackTitle + "\n回复内容: " + reply);
        notification.setRelatedType("feedback");
        sendNotification(notification);
    }

    public void sendQaAnswerNotification(Integer userId, String questionTitle, String answerContent) {
        NotificationMessage notification = new NotificationMessage();
        notification.setUserId(userId);
        notification.setType("qa_answer");
        notification.setTitle("您的问题有了新回答");
        notification.setContent("问题: " + questionTitle + "\n回答: " + answerContent);
        notification.setRelatedType("qa");
        sendNotification(notification);
    }

    public void sendSystemNotification(Integer userId, String title, String content) {
        NotificationMessage notification = new NotificationMessage();
        notification.setUserId(userId);
        notification.setType("system");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType("system");
        sendNotification(notification);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationMessage {
        private Integer userId;
        private String type;
        private String title;
        private String content;
        private String relatedType;
        private Integer relatedId;
        private Boolean isRead;
        private LocalDateTime createTime;
    }
}