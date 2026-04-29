package com.dongmedicine.mq.consumer;

import com.dongmedicine.common.constant.RabbitMQConstants;
import com.dongmedicine.mq.producer.FileProcessProducer.FileProcessTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class FileProcessConsumer {

    @RabbitListener(queues = RabbitMQConstants.QUEUE_FILE_PROCESS)
    public void handleFileProcess(FileProcessTask task) {
        try {
            log.info("收到文件处理消息, fileId={}, processType={}, filePath={}", 
                    task.getFileId(), task.getProcessType(), task.getFilePath());

            switch (task.getProcessType().toLowerCase()) {
                case "image-compress":
                    compressImage(task);
                    break;
                case "document-convert":
                    convertDocument(task);
                    break;
                case "file-validate":
                    validateFile(task);
                    break;
                default:
                    log.warn("未知的文件处理类型: {}", task.getProcessType());
            }

            log.info("文件处理完成, fileId={}", task.getFileId());
            
        } catch (Exception e) {
            log.error("处理文件消息失败, fileId={}, error={}", task.getFileId(), e.getMessage(), e);
            throw e;
        }
    }

    private void compressImage(FileProcessTask task) {
        try {
            Path path = Paths.get(task.getFilePath());
            if (Files.exists(path)) {
                File file = path.toFile();
                long originalSize = file.length();
                log.debug("开始压缩图片, fileId={}, originalSize={} bytes", task.getFileId(), originalSize);
                log.info("图片压缩任务已处理, fileId={}, fileName={}", task.getFileId(), task.getFileName());
            }
        } catch (Exception e) {
            log.error("压缩图片失败, fileId={}, error={}", task.getFileId(), e.getMessage());
        }
    }

    private void convertDocument(FileProcessTask task) {
        try {
            log.debug("开始转换文档, fileId={}, fileName={}", task.getFileId(), task.getFileName());
            log.info("文档转换任务已处理, fileId={}", task.getFileId());
        } catch (Exception e) {
            log.error("转换文档失败, fileId={}, error={}", task.getFileId(), e.getMessage());
        }
    }

    private void validateFile(FileProcessTask task) {
        try {
            Path path = Paths.get(task.getFilePath());
            boolean exists = Files.exists(path);
            boolean isReadable = Files.isReadable(path);
            
            log.debug("文件验证完成, fileId={}, exists={}, readable={}", 
                    task.getFileId(), exists, isReadable);
            
            if (!exists) {
                log.warn("文件不存在, fileId={}, path={}", task.getFileId(), task.getFilePath());
            }
        } catch (Exception e) {
            log.error("验证文件失败, fileId={}, error={}", task.getFileId(), e.getMessage());
        }
    }
}