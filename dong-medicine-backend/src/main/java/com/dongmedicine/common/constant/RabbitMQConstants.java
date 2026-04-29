package com.dongmedicine.common.constant;

public class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    public static final String EXCHANGE_DIRECT = "dong.medicine.direct";
    public static final String EXCHANGE_TOPIC = "dong.medicine.topic";

    public static final String QUEUE_OPERATION_LOG = "operation.log.queue";

    public static final String ROUTING_KEY_OPERATION_LOG = "operation.log";

    public static final String QUEUE_FEEDBACK = "feedback.queue";
    public static final String ROUTING_KEY_FEEDBACK = "feedback";

    public static final String QUEUE_FILE_PROCESS = "file.process.queue";
    public static final String ROUTING_KEY_FILE_PROCESS = "file.process";

    public static final String QUEUE_STATISTICS = "statistics.queue";
    public static final String ROUTING_KEY_STATISTICS = "statistics";

    public static final String QUEUE_NOTIFICATION = "notification.queue";
    public static final String ROUTING_KEY_NOTIFICATION = "notification";

    public static final String DLX_EXCHANGE = "dong.medicine.dlx";
    public static final String DLQ_OPERATION_LOG = "dlq.operation.log";
    public static final String DLQ_FEEDBACK = "dlq.feedback";
    public static final String DLQ_FILE_PROCESS = "dlq.file.process";
    public static final String DLQ_STATISTICS = "dlq.statistics";
    public static final String DLQ_NOTIFICATION = "dlq.notification";
}