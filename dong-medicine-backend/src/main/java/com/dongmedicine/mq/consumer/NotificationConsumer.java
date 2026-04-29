package com.dongmedicine.mq.consumer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import com.dongmedicine.mq.producer.NotificationProducer.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationConsumer {

    private final StringRedisTemplate redisTemplate;

    private static final String NOTIFICATION_KEY_PREFIX = "notification:user:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION)
    public void handleNotification(NotificationMessage notification) {
        try {
            log.info("收到通知消息, type={}, userId={}, title={}", 
                    notification.getType(), notification.getUserId(), notification.getTitle());

            if (notification.getUserId() == null) {
                log.warn("通知消息缺少用户ID, type={}, title={}", 
                        notification.getType(), notification.getTitle());
                return;
            }

            saveToRedis(notification);
            
            processNotification(notification);
            
            log.debug("通知处理完成, userId={}, type={}", notification.getUserId(), notification.getType());
            
        } catch (Exception e) {
            log.error("处理通知消息失败, type={}, error={}", notification.getType(), e.getMessage(), e);
            throw e;
        }
    }

    private void saveToRedis(NotificationMessage notification) {
        try {
            String key = NOTIFICATION_KEY_PREFIX + notification.getUserId();
            String value = formatNotification(notification);
            
            redisTemplate.opsForList().leftPush(key, value);
            redisTemplate.opsForList().trim(key, 0, 99);
            
            String unreadKey = NOTIFICATION_KEY_PREFIX + notification.getUserId() + ":unread";
            redisTemplate.opsForValue().increment(unreadKey);
            
            log.debug("通知已保存到Redis, userId={}, key={}", notification.getUserId(), key);
        } catch (Exception e) {
            log.error("保存通知到Redis失败, userId={}, error={}", notification.getUserId(), e.getMessage());
        }
    }

    private void processNotification(NotificationMessage notification) {
        switch (notification.getType()) {
            case "feedback_reply":
                log.info("处理反馈回复通知, userId={}, title={}", 
                        notification.getUserId(), notification.getTitle());
                break;
            case "qa_answer":
                log.info("处理问答回答通知, userId={}, title={}", 
                        notification.getUserId(), notification.getTitle());
                break;
            case "system":
                log.info("处理系统通知, userId={}, title={}", 
                        notification.getUserId(), notification.getTitle());
                break;
            default:
                log.warn("未知的通知类型: {}", notification.getType());
        }
    }

    private String formatNotification(NotificationMessage notification) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"").append(notification.getType()).append("\",");
        sb.append("\"title\":\"").append(notification.getTitle()).append("\",");
        sb.append("\"content\":\"").append(notification.getContent()).append("\",");
        sb.append("\"relatedType\":\"").append(notification.getRelatedType() != null ? notification.getRelatedType() : "").append("\",");
        sb.append("\"isRead\":").append(notification.getIsRead() != null ? notification.getIsRead() : false).append(",");
        sb.append("\"createTime\":\"").append(notification.getCreateTime() != null ? 
                notification.getCreateTime().format(FORMATTER) : "").append("\"");
        sb.append("}");
        return sb.toString();
    }
}