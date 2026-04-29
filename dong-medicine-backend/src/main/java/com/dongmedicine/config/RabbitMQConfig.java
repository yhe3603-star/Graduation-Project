package com.dongmedicine.config;

import com.dongmedicine.common.constant.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.operation-log:operation.log.queue}")
    private String operationLogQueue;

    @Value("${rabbitmq.queue.feedback:feedback.queue}")
    private String feedbackQueue;

    @Value("${rabbitmq.queue.file-process:file.process.queue}")
    private String fileProcessQueue;

    @Value("${rabbitmq.queue.statistics:statistics.queue}")
    private String statisticsQueue;

    @Value("${rabbitmq.queue.notification:notification.queue}")
    private String notificationQueue;

    @Value("${rabbitmq.exchange.direct:dong.medicine.direct}")
    private String directExchange;

    @Value("${rabbitmq.exchange.topic:dong.medicine.topic}")
    private String topicExchange;

    @Value("${rabbitmq.exchange.dlx:dong.medicine.dlx}")
    private String dlxExchange;

    @Value("${rabbitmq.routing-key.operation-log:operation.log}")
    private String operationLogRoutingKey;

    @Value("${rabbitmq.routing-key.feedback:feedback}")
    private String feedbackRoutingKey;

    @Value("${rabbitmq.routing-key.file-process:file.process}")
    private String fileProcessRoutingKey;

    @Value("${rabbitmq.routing-key.statistics:statistics}")
    private String statisticsRoutingKey;

    @Value("${rabbitmq.routing-key.notification:notification}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${rabbitmq.retry.initial-interval:1000}")
    private long initialInterval;

    @Value("${rabbitmq.retry.multiplier:2.0}")
    private double multiplier;

    @Bean
    public Queue operationLogQueue() {
        return QueueBuilder.durable(operationLogQueue)
                .withArgument("x-queue-mode", "lazy")
                .build();
    }

    @Bean
    public Queue feedbackQueue() {
        return QueueBuilder.durable(feedbackQueue)
                .withArgument("x-queue-mode", "lazy")
                .withArgument("x-max-length", 1000)
                .build();
    }

    @Bean
    public Queue fileProcessQueue() {
        return QueueBuilder.durable(fileProcessQueue)
                .withArgument("x-queue-mode", "lazy")
                .withArgument("x-max-length", 500)
                .build();
    }

    @Bean
    public Queue statisticsQueue() {
        return QueueBuilder.durable(statisticsQueue)
                .withArgument("x-queue-mode", "lazy")
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", "dlq.statistics")
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue)
                .withArgument("x-queue-mode", "lazy")
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", "dlq.notification")
                .build();
    }

    @Bean
    public DirectExchange directExchange() {
        return ExchangeBuilder.directExchange(directExchange)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(topicExchange)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(dlxExchange)
                .durable(true)
                .build();
    }

    @Bean
    public Queue dlqOperationLog() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_OPERATION_LOG).build();
    }

    @Bean
    public Queue dlqFeedback() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_FEEDBACK).build();
    }

    @Bean
    public Queue dlqFileProcess() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_FILE_PROCESS).build();
    }

    @Bean
    public Queue dlqStatistics() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_STATISTICS).build();
    }

    @Bean
    public Queue dlqNotification() {
        return QueueBuilder.durable(RabbitMQConstants.DLQ_NOTIFICATION).build();
    }

    @Bean
    public Binding dlqOperationLogBinding() {
        return BindingBuilder.bind(dlqOperationLog()).to(dlxExchange()).with("dlq.operation.log");
    }

    @Bean
    public Binding dlqFeedbackBinding() {
        return BindingBuilder.bind(dlqFeedback()).to(dlxExchange()).with("dlq.feedback");
    }

    @Bean
    public Binding dlqFileProcessBinding() {
        return BindingBuilder.bind(dlqFileProcess()).to(dlxExchange()).with("dlq.file.process");
    }

    @Bean
    public Binding dlqStatisticsBinding() {
        return BindingBuilder.bind(dlqStatistics()).to(dlxExchange()).with("dlq.statistics");
    }

    @Bean
    public Binding dlqNotificationBinding() {
        return BindingBuilder.bind(dlqNotification()).to(dlxExchange()).with("dlq.notification");
    }

    @Bean
    public Binding operationLogBinding(Queue operationLogQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(operationLogQueue)
                .to(directExchange)
                .with(operationLogRoutingKey);
    }

    @Bean
    public Binding feedbackBinding(Queue feedbackQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(feedbackQueue)
                .to(directExchange)
                .with(feedbackRoutingKey);
    }

    @Bean
    public Binding fileProcessBinding(Queue fileProcessQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(fileProcessQueue)
                .to(directExchange)
                .with(fileProcessRoutingKey);
    }

    @Bean
    public Binding statisticsBinding(Queue statisticsQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(statisticsQueue)
                .to(topicExchange)
                .with(statisticsRoutingKey + ".*");
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(directExchange)
                .with(notificationRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                        Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送到交换器成功, correlationId={}", 
                        correlationData != null ? correlationData.getId() : "null");
            } else {
                log.error("消息发送到交换器失败, correlationId={}, cause={}", 
                        correlationData != null ? correlationData.getId() : "null", cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.error("消息路由失败, exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returnedMessage.getExchange(),
                    returnedMessage.getRoutingKey(),
                    returnedMessage.getReplyCode(),
                    returnedMessage.getReplyText());
        });
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setRetryTemplate(retryTemplate());
        
        return rabbitTemplate;
    }

    @Bean
    public org.springframework.retry.support.RetryTemplate retryTemplate() {
        org.springframework.retry.support.RetryTemplate retryTemplate = new org.springframework.retry.support.RetryTemplate();
        org.springframework.retry.backoff.ExponentialBackOffPolicy backOffPolicy = new org.springframework.retry.backoff.ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        org.springframework.retry.policy.SimpleRetryPolicy retryPolicy = new org.springframework.retry.policy.SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxRetryAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}