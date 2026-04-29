package com.dongmedicine.mq.consumer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import com.dongmedicine.mq.producer.StatisticsProducer.StatisticsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class StatisticsConsumer {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX_VIEW = "stats:view:";
    private static final String PREFIX_LIKE = "stats:like:";
    private static final String PREFIX_SHARE = "stats:share:";
    private static final String PREFIX_DOWNLOAD = "stats:download:";
    private static final String PREFIX_DAILY = "stats:daily:";

    @RabbitListener(queues = RabbitMQConstants.QUEUE_STATISTICS)
    public void handleStatistics(StatisticsTask task) {
        try {
            log.debug("收到统计消息, type={}, targetId={}, action={}", 
                    task.getStatisticsType(), task.getTargetId(), task.getActionType());

            switch (task.getStatisticsType().toLowerCase()) {
                case "view":
                    processView(task);
                    break;
                case "like":
                    processLike(task);
                    break;
                case "share":
                    processShare(task);
                    break;
                case "download":
                    processDownload(task);
                    break;
                default:
                    log.warn("未知的统计类型: {}", task.getStatisticsType());
            }

            updateDailyStatistics(task);
            
        } catch (Exception e) {
            log.error("处理统计消息失败, type={}, error={}", task.getStatisticsType(), e.getMessage(), e);
            throw e;
        }
    }

    private void processView(StatisticsTask task) {
        String key = PREFIX_VIEW + task.getTargetType() + ":" + task.getTargetId();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        log.debug("浏览量统计更新, key={}", key);
    }

    private void processLike(StatisticsTask task) {
        String key = PREFIX_LIKE + task.getTargetType() + ":" + task.getTargetId();
        if ("add".equalsIgnoreCase(task.getActionType())) {
            redisTemplate.opsForValue().increment(key);
        } else if ("remove".equalsIgnoreCase(task.getActionType())) {
            redisTemplate.opsForValue().decrement(key);
        }
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        log.debug("点赞统计更新, key={}, action={}", key, task.getActionType());
    }

    private void processShare(StatisticsTask task) {
        String key = PREFIX_SHARE + task.getTargetType() + ":" + task.getTargetId();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        log.debug("分享统计更新, key={}", key);
    }

    private void processDownload(StatisticsTask task) {
        String key = PREFIX_DOWNLOAD + task.getTargetType() + ":" + task.getTargetId();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        log.debug("下载统计更新, key={}", key);
    }

    private void updateDailyStatistics(StatisticsTask task) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String dailyKey = PREFIX_DAILY + today + ":" + task.getTargetType();
        redisTemplate.opsForValue().increment(dailyKey);
        redisTemplate.expire(dailyKey, 30, TimeUnit.DAYS);
        log.debug("日统计更新, key={}", dailyKey);
    }
}