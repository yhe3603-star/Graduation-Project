# MQ 层 -- RabbitMQ 消息队列

> 本目录存放 RabbitMQ 的消息生产者（Producer）和消费者（Consumer），实现异步解耦。
> 类比：快递中转站 -- 生产者把"包裹"（消息）放到中转站，消费者从站里取走处理，不用等对方。

---

## 一、目录结构

```
mq/
├── producer/                       # 消息生产者（发送消息）
│   ├── FeedbackProducer.java       #   反馈消息生产者
│   ├── FileProcessProducer.java    #   文件处理消息生产者
│   ├── NotificationProducer.java   #   通知消息生产者
│   ├── OperationLogProducer.java   #   操作日志消息生产者
│   └── StatisticsProducer.java     #   统计消息生产者
└── consumer/                       # 消息消费者（接收并处理消息）
    ├── FeedbackConsumer.java       #   反馈消息消费者 → 写入MySQL
    ├── FileProcessConsumer.java    #   文件处理消费者
    ├── NotificationConsumer.java   #   通知消息消费者
    ├── OperationLogConsumer.java   #   操作日志消费者 → 写入MySQL
    └── StatisticsConsumer.java     #   统计消费者 → 写入Redis
```

---

## 二、架构总览

```
┌──────────────────────────────────────────────────────────────────────┐
│                           业务代码                                    │
│  OperationLogAspect → FeedbackService → PlantService → ...          │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Producer（生产者）                                │
│  OperationLogProducer  FeedbackProducer  StatisticsProducer  ...    │
│  sendOperationLog()    sendFeedback()    sendStatisticsTask()       │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
    rabbitTemplate.convertAndSend(exchange, routingKey, message)
         │           │            │            │
         ▼           ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       RabbitMQ Broker                                │
│  Exchange: dong.medicine.direct / dong.medicine.topic               │
│  Queue: operation.log.queue / feedback.queue / statistics.queue ... │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Consumer（消费者）                                │
│  OperationLogConsumer  FeedbackConsumer  StatisticsConsumer  ...    │
│  @RabbitListener 异步监听队列消息                                    │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │
         ▼           ▼            ▼
    MySQL               Redis
  (操作日志表)     (统计计数器/缓存)
```

---

## 三、消息队列配置

配置类：`com.dongmedicine.config.RabbitMQConfig`

### 3.1 交换机（Exchange）

| 交换机名称 | 类型 | 用途 |
|-----------|------|------|
| `dong.medicine.direct` | Direct | 精确路由（操作日志、反馈、文件处理、通知） |
| `dong.medicine.topic` | Topic | 通配符路由（统计消息支持 `statistics.*`） |
| `dong.medicine.dlx` | Direct | 死信交换机（消息处理失败时转入DLQ） |

### 3.2 队列（Queue）

| 队列名称 | 绑定交换机 | Routing Key | 特性 |
|---------|-----------|-------------|------|
| `operation.log.queue` | direct | `operation.log` | Lazy模式 |
| `feedback.queue` | direct | `feedback` | Lazy模式, max-length=1000 |
| `file.process.queue` | direct | `file.process` | Lazy模式, max-length=500 |
| `statistics.queue` | topic | `statistics.*` | Lazy模式, DLX |
| `notification.queue` | direct | `notification` | Lazy模式, DLX |

### 3.3 死信队列（DLQ）

每个业务队列都有对应的死信队列，用于存放处理失败的消息：

| DLQ名称 | 用途 |
|---------|------|
| `dlq.operation.log` | 操作日志失败消息 |
| `dlq.feedback` | 反馈失败消息 |
| `dlq.file.process` | 文件处理失败消息 |
| `dlq.statistics` | 统计失败消息 |
| `dlq.notification` | 通知失败消息 |

### 3.4 RabbitTemplate 配置

```java
@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                    Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter);  // JSON序列化
    rabbitTemplate.setConfirmCallback((data, ack, cause) -> {
        // 消息到达Exchange的回调
    });
    rabbitTemplate.setReturnsCallback(returnedMessage -> {
        // 消息无法路由到Queue的回调
    });
    rabbitTemplate.setRetryTemplate(retryTemplate());  // 重试模板
    return rabbitTemplate;
}
```

重试策略：指数退避，初始间隔1秒，倍数2.0，最大间隔10秒，最多3次。

---

## 四、生产者详解

### 4.1 OperationLogProducer -- 操作日志生产者

文件：`mq/producer/OperationLogProducer.java`

```java
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class OperationLogProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendOperationLog(OperationLog operationLog) {
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE_DIRECT,           // "dong.medicine.direct"
            RabbitMQConstants.ROUTING_KEY_OPERATION_LOG,  // "operation.log"
            operationLog
        );
    }
}
```

**调用方**：`OperationLogAspect` 在Controller方法执行后，通过 `RabbitMQOperationLogService.saveLogAsync()` → `OperationLogProducer.sendOperationLog()` 发送操作日志到队列，不阻塞HTTP响应。

**降级逻辑**：当RabbitMQ不可用时，`OperationLogAspect` 捕获异常，降级为直接调用 `OperationLogService.save()` 同步写入。

### 4.2 FeedbackProducer -- 反馈消息生产者

文件：`mq/producer/FeedbackProducer.java`

```java
@Component
@ConditionalOnProperty(app.rabbitmq.enabled)
public class FeedbackProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendFeedback(Feedback feedback) {
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE_DIRECT,        // "dong.medicine.direct"
            RabbitMQConstants.ROUTING_KEY_FEEDBACK,    // "feedback"
            feedback
        );
    }
}
```

**调用方**：`FeedbackServiceImpl.submitFeedback()` 发送反馈对象到队列，由消费者异步写入数据库。

### 4.3 StatisticsProducer -- 统计消息生产者

文件：`mq/producer/StatisticsProducer.java`

```java
@Component
@ConditionalOnProperty(app.rabbitmq.enabled)
public class StatisticsProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendStatisticsTask(StatisticsTask task) {
        String routingKey = RabbitMQConstants.ROUTING_KEY_STATISTICS + "." + task.getStatisticsType();
        // 例如: "statistics.view" 或 "statistics.like"
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE_TOPIC,  // "dong.medicine.topic"
            routingKey,                        // "statistics.*"
            task
        );
    }
}

// 内部类：统计任务
@Data
public static class StatisticsTask {
    private String statisticsType;  // "view" / "like" / "share" / "download"
    private Integer targetId;
    private String targetType;      // "plant" / "knowledge" / "inheritor" 等
    private String actionType;      // "add" / "remove"（用于点赞）
    private Integer userId;
    private Long timestamp;
    private String extraData;
}
```

### 4.4 FileProcessProducer -- 文件处理生产者

文件：`mq/producer/FileProcessProducer.java`

发送文件异步处理任务（如缩略图生成、格式转换等）。

### 4.5 NotificationProducer -- 通知消息生产者

文件：`mq/producer/NotificationProducer.java`

发送用户通知消息（如评论回复提醒、反馈回复提醒等）。

---

## 五、消费者详解

### 5.1 OperationLogConsumer -- 操作日志消费者

文件：`mq/consumer/OperationLogConsumer.java`

```java
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(app.rabbitmq.enabled)
public class OperationLogConsumer {
    private final OperationLogMapper operationLogMapper;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_OPERATION_LOG)  // "operation.log.queue"
    public void handleOperationLog(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);  // 直接写入MySQL
    }
}
```

**处理流程**：接收操作日志消息 → 调用Mapper插入数据库。如果插入失败（如数据库连接异常），异常会触发RabbitMQ的重试机制，最终进入DLQ。

### 5.2 FeedbackConsumer -- 反馈消息消费者

文件：`mq/consumer/FeedbackConsumer.java`

```java
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(app.rabbitmq.enabled)
public class FeedbackConsumer {
    private final FeedbackMapper feedbackMapper;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_FEEDBACK)  // "feedback.queue"
    public void handleFeedback(Feedback feedback) {
        feedbackMapper.insert(feedback);
    }
}
```

### 5.3 StatisticsConsumer -- 统计消息消费者

文件：`mq/consumer/StatisticsConsumer.java`

```java
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(app.rabbitmq.enabled)
public class StatisticsConsumer {
    private final StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_STATISTICS)  // "statistics.queue"
    public void handleStatistics(StatisticsTask task) {
        switch (task.getStatisticsType().toLowerCase()) {
            case "view"     → processView(task);      // stats:view:{type}:{id} INCR, 7天过期
            case "like"     → processLike(task);      // add→INCR, remove→DECR
            case "share"    → processShare(task);     // stats:share:{type}:{id} INCR
            case "download" → processDownload(task);  // stats:download:{type}:{id} INCR
        }
        updateDailyStatistics(task);  // stats:daily:{date}:{type} INCR, 30天过期
    }
}
```

**统计Key命名规则**：

| 统计类型 | Redis Key前缀 | 过期时间 |
|---------|--------------|---------|
| 浏览 | `stats:view:{targetType}:{targetId}` | 7天 |
| 点赞 | `stats:like:{targetType}:{targetId}` | 7天 |
| 分享 | `stats:share:{targetType}:{targetId}` | 7天 |
| 下载 | `stats:download:{targetType}:{targetId}` | 7天 |
| 日统计 | `stats:daily:{date}:{targetType}` | 30天 |

---

## 六、常量定义

文件：`com.dongmedicine.common.constant.RabbitMQConstants`

```java
public class RabbitMQConstants {
    // 交换机
    public static final String EXCHANGE_DIRECT = "dong.medicine.direct";
    public static final String EXCHANGE_TOPIC  = "dong.medicine.topic";

    // 操作日志
    public static final String QUEUE_OPERATION_LOG       = "operation.log.queue";
    public static final String ROUTING_KEY_OPERATION_LOG = "operation.log";

    // 反馈
    public static final String QUEUE_FEEDBACK       = "feedback.queue";
    public static final String ROUTING_KEY_FEEDBACK = "feedback";

    // 文件处理
    public static final String QUEUE_FILE_PROCESS       = "file.process.queue";
    public static final String ROUTING_KEY_FILE_PROCESS = "file.process";

    // 统计
    public static final String QUEUE_STATISTICS       = "statistics.queue";
    public static final String ROUTING_KEY_STATISTICS = "statistics";

    // 通知
    public static final String QUEUE_NOTIFICATION       = "notification.queue";
    public static final String ROUTING_KEY_NOTIFICATION = "notification";

    // 死信
    public static final String DLX_EXCHANGE = "dong.medicine.dlx";
    public static final String DLQ_OPERATION_LOG = "dlq.operation.log";
    public static final String DLQ_FEEDBACK      = "dlq.feedback";
    public static final String DLQ_FILE_PROCESS  = "dlq.file.process";
    public static final String DLQ_STATISTICS    = "dlq.statistics";
    public static final String DLQ_NOTIFICATION  = "dlq.notification";
}
```

---

## 七、消息格式

所有消息使用 `Jackson2JsonMessageConverter` 进行JSON序列化：

```java
// RabbitMQConfig中配置
@Bean
public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

发送的消息会被自动序列化为JSON，消费者收到后自动反序列化为Java对象。例如：

```json
// 操作日志消息
{
  "userId": 1,
  "username": "admin",
  "module": "PLANT",
  "operation": "detail",
  "type": "QUERY",
  "method": "GET /api/plants/1",
  "ip": "127.0.0.1",
  "duration": 42,
  "success": true,
  "params": "{\"arg0\":1}",
  "createdAt": null
}

// 统计消息
{
  "statisticsType": "view",
  "targetId": 1,
  "targetType": "plant",
  "actionType": null,
  "userId": 5,
  "timestamp": 1714608000000,
  "extraData": null
}
```

---

## 八、生产者/消费者对照表

| 业务场景 | 生产者 | 消费者 | 目标存储 |
|---------|--------|--------|---------|
| 操作日志记录 | OperationLogProducer | OperationLogConsumer | MySQL operation_log表 |
| 用户反馈提交 | FeedbackProducer | FeedbackConsumer | MySQL feedback表 |
| 文件异步处理 | FileProcessProducer | FileProcessConsumer | 文件系统 |
| 浏览/点赞/分享统计 | StatisticsProducer | StatisticsConsumer | Redis counter |
| 系统通知 | NotificationProducer | NotificationConsumer | 待定 |

---

## 九、RabbitMQ连接配置

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: /
    connection-timeout: 10000
    listener:
      simple:
        prefetch: 10          # 每个消费者预取10条消息
        retry:
          enabled: true       # 消费失败自动重试

app:
  rabbitmq:
    enabled: ${APP_RABBITMQ_ENABLED:true}  # 可通过环境变量关闭RabbitMQ
```

### 关闭RabbitMQ

设置环境变量 `APP_RABBITMQ_ENABLED=false` 可完全禁用RabbitMQ功能。此时所有Producer和Consumer的 `@ConditionalOnProperty` 条件不满足，不会被Spring创建。操作日志会通过 `OperationLogAspect` 降级为同步写入数据库。

---

## 十、设计要点

1. **异步解耦**：HTTP请求处理（Controller → Service）不等待日志写入和统计更新完成，提升响应速度
2. **Lazy队列**：所有队列配置 `x-queue-mode: lazy`，消息优先存储在磁盘，降低内存占用
3. **死信队列**：statistics和notification配置了DLX，处理失败的消息不会丢失
4. **发送确认**：RabbitTemplate配置了ConfirmCallback，追踪消息是否到达Exchange
5. **指数退避重试**：消息发送或消费失败时自动重试（最多3次）
6. **条件启用**：通过 `@ConditionalOnProperty` 支持通过配置关闭RabbitMQ功能
7. **降级机制**：`OperationLogAspect` 中当RabbitMQ不可用时自动降级为同步写入
