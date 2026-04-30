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

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class FileProcessProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendFileProcessTask(FileProcessTask task) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_DIRECT,
                    RabbitMQConstants.ROUTING_KEY_FILE_PROCESS,
                    task
            );
            
            log.debug("文件处理消息已发送, fileId={}, type={}", task.getFileId(), task.getProcessType());
        } catch (Exception e) {
            log.error("发送文件处理消息失败, fileId={}, error={}", task.getFileId(), e.getMessage(), e);
            throw e;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileProcessTask {
        private String fileId;
        private String filePath;
        private String fileName;
        private String fileType;
        private String processType;
        private String originalFileName;
        private Long fileSize;
        private Integer userId;
        private String businessType;
    }
}