# Consumer -- 消息消费者

> 本目录存放5个RabbitMQ消息消费者，负责从队列接收消息并执行实际的业务处理（写数据库、写Redis、处理文件等）。
> 类比：快递收件人 -- 快递公司（RabbitMQ）把包裹（消息）送到你家门口，你拆开包裹并执行里面的任务（写数据库/更新缓存/处理文件）。

---

## 一、目录结构

```
mq/consumer/
├── StatisticsConsumer.java      # 统计消费者 → 写入Redis计数器
├── OperationLogConsumer.java    # 操作日志消费者 → 写入MySQL
├── NotificationConsumer.java    # 通知消费者 → 写入Redis + 日志处理
├── FileProcessConsumer.java     # 文件处理消费者 → 文件系统操作
└── FeedbackConsumer.java        # 反馈消费者 → 写入MySQL
```

---

## 二、架构定位

Consumer位于"RabbitMQ Broker"与"数据存储"之间，是消息的**消费端**：

```
┌──────────────────────────────────────────────────────────────────────┐
│                       RabbitMQ Broker                                 │
│  Queue: operation.log.queue / feedback.queue / statistics.queue ... │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     Consumer 层（本目录）                               │
│                                                                      │
│  OperationLogConsumer  FeedbackConsumer  StatisticsConsumer          │
│  @RabbitListener       @RabbitListener    @RabbitListener            │
│  → MySQL insert        → MySQL insert     → Redis INCR/DECR         │
│                                                                      │
│  FileProcessConsumer   NotificationConsumer                          │
│  @RabbitListener       @RabbitListener                               │
│  → 文件系统操作         → Redis List + 日志处理                       │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
┌────────────────┐ ┌────────────────┐ ┌──────────────────────────────┐
│   MySQL         │ │    Redis        │ │       文件系统               │
│ operation_log   │ │ stats:view:*   │ │  图片压缩/文档转换/文件验证  │
│ feedback        │ │ stats:like:*   │ │                              │
│                 │ │ notification:* │ │                              │
└────────────────┘ └────────────────┘ └──────────────────────────────┘
```

---

## 三、公共特征

所有Consumer共享以下编程规范：

```java
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class XxxConsumer {
    // 依赖注入

    @RabbitListener(queues = RabbitMQConstants.QUEUE_XXX)
    public void handleXxx(XxxMessage message) {
        try {
            log.debug("收到xxx消息, ...");
            // 业务处理
            log.debug("xxx处理完成, ...");
        } catch (Exception e) {
            log.error("处理xxx消息失败, ..., error={}", e.getMessage(), e);
            throw e;   // 重新抛出，触发RabbitMQ重试机制
        }
    }
}
```

| 特征 | 说明 |
|------|------|
| `@ConditionalOnProperty` | 通过 `app.rabbitmq.enabled` 配置控制是否启用，默认true |
| `@RequiredArgsConstructor` | 构造器注入依赖 |
| `@RabbitListener` | 声明式队列监听，Spring自动注册为消息消费者 |
| `@Slf4j` | 日志记录 |
| try-catch | 处理成功记debug/info日志，失败记error日志并**重新抛出异常** |
| 异常重抛 | 触发RabbitMQ重试机制（最多3次，指数退避），最终进入DLQ |

---

## 四、各文件详解

### 4.1 OperationLogConsumer -- 操作日志消费者

> 类比：药铺的"抄录员" -- 从"待记录箱"（队列）取出操作记录，一笔一笔抄录到正式账本（MySQL）。

**核心职责**：从 `operation.log.queue` 接收操作日志消息，直接插入MySQL `operation_log` 表。

**消息来源**：`OperationLogProducer.sendOperationLog()` 发送的 `OperationLog` Entity对象。

**源码解析**：

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_OPERATION_LOG)  // "operation.log.queue"
public void handleOperationLog(OperationLog operationLog) {
    try {
        log.debug("收到操作日志消息, userId={}, operation={}",
                operationLog.getUserId(), operationLog.getType());

        operationLogMapper.insert(operationLog);   // 直接插入MySQL

        log.debug("操作日志保存成功, id={}", operationLog.getId());
    } catch (Exception e) {
        log.error("处理操作日志消息失败, operationType={}, error={}",
                operationLog.getType(), e.getMessage(), e);
        throw e;   // 触发重试 → 最终进入DLQ
    }
}
```

**处理流程**：

```
OperationLog消息 → handleOperationLog() → operationLogMapper.insert() → MySQL
                                                          │
                                                    插入失败 → 抛异常 → RabbitMQ重试(3次) → DLQ
```

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `operationLogMapper` | OperationLogMapper | MyBatis-Plus Mapper，执行insert操作 |

**设计特点**：最简单的消费者，接收消息后直接调用Mapper插入数据库，无额外业务逻辑。如果数据库连接异常导致插入失败，异常会触发RabbitMQ的重试机制（指数退避，最多3次），最终消息进入死信队列 `operation.log.dlq`。

---

### 4.2 FeedbackConsumer -- 反馈消息消费者

> 类比：药铺的"意见簿归档员" -- 从意见箱取出意见，正式归档到意见簿（MySQL）。

**核心职责**：从 `feedback.queue` 接收反馈消息，插入MySQL `feedback` 表。

**消息来源**：`FeedbackProducer.sendFeedback()` 发送的 `Feedback` Entity对象。

**源码解析**：

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_FEEDBACK)  // "feedback.queue"
public void handleFeedback(Feedback feedback) {
    try {
        log.debug("收到反馈消息, id={}, userId={}, type={}",
                feedback.getId(), feedback.getUserId(), feedback.getType());

        feedbackMapper.insert(feedback);   // 直接插入MySQL

        // 日志中截断长内容，防止日志膨胀
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
```

**安全设计 -- 日志内容截断**：

```java
// 反馈内容超过50字符时截断，防止大量反馈内容撑爆日志文件
feedback.getContent() != null && feedback.getContent().length() > 50
    ? feedback.getContent().substring(0, 50) + "..."
    : feedback.getContent()
```

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `feedbackMapper` | FeedbackMapper | MyBatis-Plus Mapper，执行insert操作 |

---

### 4.3 StatisticsConsumer -- 统计消息消费者

> 类比：药铺的"算盘先生" -- 每次有人看药、买药，算盘先生拨一下珠子（Redis INCR），7天后自动清零。

**核心职责**：从 `statistics.queue` 接收统计消息，根据统计类型更新Redis计数器。

**消息来源**：`StatisticsProducer.sendStatisticsTask()` 发送的 `StatisticsTask` 内部类对象。

**源码解析**：

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_STATISTICS)  // "statistics.queue"
public void handleStatistics(StatisticsTask task) {
    try {
        switch (task.getStatisticsType().toLowerCase()) {
            case "view":     processView(task);      break;  // 浏览量+1
            case "like":     processLike(task);      break;  // 点赞+1/-1
            case "share":    processShare(task);     break;  // 分享+1
            case "download": processDownload(task);  break;  // 下载+1
            default:         log.warn("未知的统计类型: {}", task.getStatisticsType());
        }
        updateDailyStatistics(task);   // 每种操作都更新日统计
    } catch (Exception e) {
        log.error("处理统计消息失败, type={}, error={}", task.getStatisticsType(), e.getMessage(), e);
        throw e;
    }
}
```

**4种统计处理方法**：

| 方法 | Redis操作 | Key格式 | 过期时间 | 说明 |
|------|----------|---------|---------|------|
| `processView()` | `INCR` | `stats:view:{targetType}:{targetId}` | 7天 | 浏览量+1 |
| `processLike()` | `INCR`/`DECR` | `stats:like:{targetType}:{targetId}` | 7天 | add=+1, remove=-1 |
| `processShare()` | `INCR` | `stats:share:{targetType}:{targetId}` | 7天 | 分享+1 |
| `processDownload()` | `INCR` | `stats:download:{targetType}:{targetId}` | 7天 | 下载+1 |
| `updateDailyStatistics()` | `INCR` | `stats:daily:{date}:{targetType}` | 30天 | 日汇总统计 |

**点赞统计的特殊逻辑**：

```java
private void processLike(StatisticsTask task) {
    String key = PREFIX_LIKE + task.getTargetType() + ":" + task.getTargetId();
    if ("add".equalsIgnoreCase(task.getActionType())) {
        redisTemplate.opsForValue().increment(key);    // 点赞 → +1
    } else if ("remove".equalsIgnoreCase(task.getActionType())) {
        redisTemplate.opsForValue().decrement(key);    // 取消点赞 → -1
    }
    redisTemplate.expire(key, 7, TimeUnit.DAYS);
}
```

**日统计更新**：

```java
private void updateDailyStatistics(StatisticsTask task) {
    String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);  // "20250509"
    String dailyKey = PREFIX_DAILY + today + ":" + task.getTargetType();
    // 例如: "stats:daily:20250509:plant"
    redisTemplate.opsForValue().increment(dailyKey);
    redisTemplate.expire(dailyKey, 30, TimeUnit.DAYS);  // 保留30天
}
```

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `redisTemplate` | StringRedisTemplate | Redis操作，INCR/DECR计数 |

**设计思路**：

1. **Redis计数器而非MySQL**：浏览/点赞/分享/下载是高频操作，直接写MySQL会造成大量UPDATE语句，使用Redis INCR性能极高
2. **7天过期**：统计Key设置7天TTL，过期后自动清理，避免Redis内存无限增长
3. **日统计30天保留**：日统计数据保留30天，用于绘制趋势图
4. **点赞双向操作**：通过actionType区分"点赞"和"取消点赞"，使用INCR/DECR实现

---

### 4.4 FileProcessConsumer -- 文件处理消费者

> 类比：药铺的"加工坊工人" -- 从"待加工箱"取出任务单，执行切片、研磨、检验等工序。

**核心职责**：从 `file.process.queue` 接收文件处理任务，根据处理类型执行不同操作。

**消息来源**：`FileProcessProducer.sendFileProcessTask()` 发送的 `FileProcessTask` 内部类对象。

**源码解析**：

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_FILE_PROCESS)  // "file.process.queue"
public void handleFileProcess(FileProcessTask task) {
    try {
        switch (task.getProcessType().toLowerCase()) {
            case "image-compress":   compressImage(task);    break;  // 图片压缩
            case "document-convert": convertDocument(task);  break;  // 文档转换
            case "file-validate":    validateFile(task);     break;  // 文件验证
            default:                 log.warn("未知的文件处理类型: {}", task.getProcessType());
        }
    } catch (Exception e) {
        log.error("处理文件消息失败, fileId={}, error={}", task.getFileId(), e.getMessage(), e);
        throw e;
    }
}
```

**3种文件处理方法**：

#### compressImage -- 图片压缩

```java
private void compressImage(FileProcessTask task) {
    Path path = Paths.get(task.getFilePath());
    if (Files.exists(path)) {
        File file = path.toFile();
        long originalSize = file.length();
        log.debug("开始压缩图片, fileId={}, originalSize={} bytes", task.getFileId(), originalSize);
        log.info("图片压缩任务已处理, fileId={}, fileName={}", task.getFileId(), task.getFileName());
    }
}
```

> **当前状态**：图片压缩逻辑目前仅记录日志，尚未接入实际的图片压缩库（如Thumbnailator）。框架已搭建完成，后续可扩展。

#### convertDocument -- 文档转换

```java
private void convertDocument(FileProcessTask task) {
    log.debug("开始转换文档, fileId={}, fileName={}", task.getFileId(), task.getFileName());
    log.info("文档转换任务已处理, fileId={}", task.getFileId());
}
```

> **当前状态**：文档转换逻辑目前仅记录日志，可对接KKFileView服务实现文档在线预览转换。

#### validateFile -- 文件验证

```java
private void validateFile(FileProcessTask task) {
    Path path = Paths.get(task.getFilePath());
    boolean exists = Files.exists(path);
    boolean isReadable = Files.isReadable(path);

    log.debug("文件验证完成, fileId={}, exists={}, readable={}",
            task.getFileId(), exists, isReadable);

    if (!exists) {
        log.warn("文件不存在, fileId={}, path={}", task.getFileId(), task.getFilePath());
    }
}
```

**文件验证逻辑**：检查文件是否存在（`Files.exists`）和是否可读（`Files.isReadable`），不存在则记录WARN日志。

**依赖注入**：无外部依赖，直接使用 `java.nio.file` API操作文件系统。

**设计思路**：文件处理消费者采用"框架先行"策略，先定义好消息格式和处理分支，具体压缩/转换逻辑后续按需接入。这保证了文件上传流程的完整性，即使当前压缩/转换功能未完全实现，也不会影响主流程。

---

### 4.5 NotificationConsumer -- 通知消息消费者

> 类比：药铺的"传令兵" -- 收到通知任务后，把通知送到指定顾客的"信箱"（Redis List），并标记为未读。

**核心职责**：从 `notification.queue` 接收通知消息，保存到Redis并按类型处理。

**消息来源**：`NotificationProducer.sendNotification()` 发送的 `NotificationMessage` 内部类对象。

**源码解析**：

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION)  // "notification.queue"
public void handleNotification(NotificationMessage notification) {
    try {
        // 1. 校验必填字段
        if (notification.getUserId() == null) {
            log.warn("通知消息缺少用户ID, type={}, title={}",
                    notification.getType(), notification.getTitle());
            return;   // 缺少userId的消息直接丢弃，不抛异常
        }

        // 2. 保存到Redis
        saveToRedis(notification);

        // 3. 按类型处理
        processNotification(notification);

    } catch (Exception e) {
        log.error("处理通知消息失败, type={}, error={}", notification.getType(), e.getMessage(), e);
        throw e;
    }
}
```

**saveToRedis -- 保存通知到Redis**：

```java
private void saveToRedis(NotificationMessage notification) {
    try {
        // 1. 推入用户通知列表（List结构，最新在前）
        String key = NOTIFICATION_KEY_PREFIX + notification.getUserId();
        // 例如: "notification:user:5"
        String value = formatNotification(notification);  // 格式化为JSON字符串

        redisTemplate.opsForList().leftPush(key, value);   // 左推（最新在前）
        redisTemplate.opsForList().trim(key, 0, 99);       // 保留最近100条

        // 2. 递增未读计数
        String unreadKey = NOTIFICATION_KEY_PREFIX + notification.getUserId() + ":unread";
        // 例如: "notification:user:5:unread"
        redisTemplate.opsForValue().increment(unreadKey);

    } catch (Exception e) {
        log.error("保存通知到Redis失败, userId={}, error={}", notification.getUserId(), e.getMessage());
        // 不抛异常，Redis写入失败不影响消息确认
    }
}
```

**Redis存储结构**：

| Key | 类型 | 说明 | 过期策略 |
|-----|------|------|---------|
| `notification:user:{userId}` | List | 用户通知列表，最多100条 | 无过期（手动管理） |
| `notification:user:{userId}:unread` | String(数值) | 未读通知数量 | 无过期（手动管理） |

**formatNotification -- 格式化通知为JSON**：

```java
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
```

> **注意**：此处使用手动拼接JSON而非Jackson序列化，是为了确保Redis中存储的格式稳定可控，不依赖序列化配置。

**processNotification -- 按类型处理**：

```java
private void processNotification(NotificationMessage notification) {
    switch (notification.getType()) {
        case "feedback_reply":   // 反馈回复通知
            log.info("处理反馈回复通知, userId={}, title={}", ...);
            break;
        case "qa_answer":        // 问答回答通知
            log.info("处理问答回答通知, userId={}, title={}", ...);
            break;
        case "system":           // 系统通知
            log.info("处理系统通知, userId={}, title={}", ...);
            break;
        default:
            log.warn("未知的通知类型: {}", notification.getType());
    }
}
```

> **当前状态**：`processNotification()` 目前仅记录日志，后续可扩展为WebSocket推送、邮件通知等。

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `redisTemplate` | StringRedisTemplate | Redis操作，List推送 + 计数器递增 |

**设计特点**：

1. **userId校验**：缺少userId的通知直接丢弃（return），不抛异常，避免无效消息触发重试
2. **Redis写入容错**：`saveToRedis()` 内部catch异常不向上抛出，Redis写入失败不影响消息确认
3. **List容量限制**：`trim(key, 0, 99)` 保留最近100条通知，防止Redis内存膨胀
4. **未读计数**：独立的 `:unread` Key存储未读数，前端可通过 `GET /api/notifications/unread-count` 快速获取

---

## 五、消费者与存储对照表

| Consumer | 监听队列 | 目标存储 | 写入方式 |
|----------|---------|---------|---------|
| OperationLogConsumer | `operation.log.queue` | MySQL `operation_log` 表 | `operationLogMapper.insert()` |
| FeedbackConsumer | `feedback.queue` | MySQL `feedback` 表 | `feedbackMapper.insert()` |
| StatisticsConsumer | `statistics.queue` | Redis计数器 | `redisTemplate.opsForValue().increment()` |
| NotificationConsumer | `notification.queue` | Redis List + 计数器 | `redisTemplate.opsForList().leftPush()` + `increment()` |
| FileProcessConsumer | `file.process.queue` | 文件系统 | `java.nio.file.Files` API |

---

## 六、消费者与Producer对应关系

| 业务场景 | Producer | Consumer | 消息对象 |
|---------|----------|----------|---------|
| 操作日志记录 | OperationLogProducer | OperationLogConsumer | `OperationLog` Entity |
| 用户反馈提交 | FeedbackProducer | FeedbackConsumer | `Feedback` Entity |
| 浏览/点赞/分享统计 | StatisticsProducer | StatisticsConsumer | `StatisticsTask` 内部类 |
| 文件异步处理 | FileProcessProducer | FileProcessConsumer | `FileProcessTask` 内部类 |
| 系统通知 | NotificationProducer | NotificationConsumer | `NotificationMessage` 内部类 |

---

## 七、异常处理与重试机制

### 消息处理失败流程

```
Consumer处理消息 → 抛出异常
       │
       ▼
RabbitMQ重试（指数退避，最多3次）
  第1次: 等待1秒
  第2次: 等待2秒
  第3次: 等待4秒
       │
       ▼ 重试耗尽
消息进入死信队列（DLQ）
  operation.log.queue → operation.log.dlq
  feedback.queue      → feedback.dlq
  statistics.queue    → statistics.dlq
  file.process.queue  → file.process.dlq
  notification.queue  → notification.dlq
```

### 特殊异常处理

| Consumer | 特殊处理 | 说明 |
|----------|---------|------|
| NotificationConsumer | userId为null时 `return` | 不抛异常，消息直接确认（丢弃），避免无效重试 |
| NotificationConsumer | `saveToRedis()` 内部catch | Redis写入失败不抛异常，不影响消息确认 |
| StatisticsConsumer | 未知统计类型仅 `log.warn` | 不抛异常，消息正常确认 |

---

## 八、文件间依赖关系

```
OperationLogConsumer ──依赖──► RabbitMQConstants（队列名常量）
                     ──依赖──► OperationLogMapper（MyBatis-Plus Mapper）
                     ──依赖──► OperationLog Entity（消息反序列化目标）

FeedbackConsumer ──依赖──► RabbitMQConstants
                ──依赖──► FeedbackMapper（MyBatis-Plus Mapper）
                ──依赖──► Feedback Entity（消息反序列化目标）

StatisticsConsumer ──依赖──► RabbitMQConstants
                  ──依赖──► StringRedisTemplate（Redis操作）
                  ──依赖──► StatisticsProducer.StatisticsTask（消息反序列化目标）

FileProcessConsumer ──依赖──► RabbitMQConstants
                   ──依赖──► FileProcessProducer.FileProcessTask（消息反序列化目标）
                   ──依赖──► java.nio.file.Files（文件系统API）

NotificationConsumer ──依赖──► RabbitMQConstants
                    ──依赖──► StringRedisTemplate（Redis操作）
                    ──依赖──► NotificationProducer.NotificationMessage（消息反序列化目标）
```

5个Consumer之间**无直接依赖**，各自独立消费不同队列的消息。StatisticsConsumer、FileProcessConsumer、NotificationConsumer分别依赖对应Producer的内部类作为消息反序列化目标类型，形成Producer-Consumer配对关系。
