# Producer -- 消息生产者

> 本目录存放5个RabbitMQ消息生产者，负责将业务事件封装为消息并发送到队列，实现异步解耦。
> 类比：快递寄件窗口 -- 你把包裹（消息）递给窗口工作人员（Producer），不用等收件人签收就能离开，后续配送由快递公司（RabbitMQ）负责。

---

## 一、目录结构

```
mq/producer/
├── FileProcessProducer.java     # 文件处理消息生产者（含内部类FileProcessTask）
├── FeedbackProducer.java        # 反馈消息生产者
├── NotificationProducer.java    # 通知消息生产者（含内部类NotificationMessage + 3个便捷方法）
├── StatisticsProducer.java      # 统计消息生产者（含内部类StatisticsTask）
└── OperationLogProducer.java    # 操作日志消息生产者
```

---

## 二、架构定位

Producer位于"业务代码"与"RabbitMQ Broker"之间，是消息的**发送端**：

```
┌──────────────────────────────────────────────────────────────────────┐
│                          业务代码层                                   │
│  OperationLogAspect │ FeedbackServiceImpl │ PlantServiceImpl │ ...   │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      Producer 层（本目录）                             │
│                                                                      │
│  OperationLogProducer  FeedbackProducer  StatisticsProducer          │
│  sendOperationLog()    sendFeedback()    sendStatisticsTask()        │
│                                                                      │
│  FileProcessProducer   NotificationProducer                          │
│  sendFileProcessTask() sendNotification()                            │
│                       sendFeedbackReplyNotification()                │
│                       sendQaAnswerNotification()                     │
│                       sendSystemNotification()                       │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
    rabbitTemplate.convertAndSend(exchange, routingKey, message)
         │
         ▼
┌──────────────────────────────────────────────────────────────────────┐
│                       RabbitMQ Broker                                 │
│  Exchange: dong.medicine.direct / dong.medicine.topic                │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 三、公共特征

所有Producer共享以下编程规范：

```java
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class XxxProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendXxx(XxxMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.debug("xxx消息已发送, ...");
        } catch (Exception e) {
            log.error("发送xxx消息失败, ..., error={}", e.getMessage(), e);
            throw e;   // 异常向上抛出，由调用方决定降级策略
        }
    }
}
```

| 特征 | 说明 |
|------|------|
| `@ConditionalOnProperty` | 通过 `app.rabbitmq.enabled` 配置控制是否启用，默认true |
| `@RequiredArgsConstructor` | 构造器注入 `RabbitTemplate` |
| `@Slf4j` | 日志记录 |
| try-catch | 发送成功记debug日志，失败记error日志并**重新抛出异常** |
| 消息序列化 | 由 `Jackson2JsonMessageConverter` 自动将Java对象序列化为JSON |

---

## 四、各文件详解

### 4.1 OperationLogProducer -- 操作日志生产者

> 类比：药铺的"值班记录本" -- 每次操作都记一笔，但不用当场写完，先丢进"待记录箱"（队列），后面有人统一抄录。

**核心职责**：将操作日志对象异步发送到 `operation.log.queue`，避免日志写入阻塞HTTP响应。

**消息路由**：

| 项目 | 值 |
|------|-----|
| Exchange | `dong.medicine.direct`（Direct精确路由） |
| Routing Key | `operation.log` |
| Queue | `operation.log.queue` |

**源码解析**：

```java
public void sendOperationLog(OperationLog operationLog) {
    try {
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE_DIRECT,           // "dong.medicine.direct"
            RabbitMQConstants.ROUTING_KEY_OPERATION_LOG,  // "operation.log"
            operationLog                                 // 直接发送Entity对象
        );
        log.debug("操作日志消息已发送, userId={}, operation={}",
                operationLog.getUserId(), operationLog.getType());
    } catch (Exception e) {
        log.error("发送操作日志消息失败, operationType={}, error={}",
                operationLog.getType(), e.getMessage(), e);
        throw e;
    }
}
```

**调用方**：`OperationLogAspect` 在Controller方法执行后，通过 `RabbitMQOperationLogService.saveLogAsync()` 调用此方法。

**降级逻辑**：当RabbitMQ不可用时，`OperationLogAspect` 捕获异常，降级为直接调用 `OperationLogService.save()` 同步写入数据库。

**消息格式**（OperationLog Entity直接序列化）：

```json
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
  "params": "{\"arg0\":1}"
}
```

---

### 4.2 FeedbackProducer -- 反馈消息生产者

> 类比：意见箱 -- 顾客把意见投入箱中，不用等掌柜当面回复。

**核心职责**：将用户反馈对象异步发送到 `feedback.queue`，由消费者写入数据库。

**消息路由**：

| 项目 | 值 |
|------|-----|
| Exchange | `dong.medicine.direct` |
| Routing Key | `feedback` |
| Queue | `feedback.queue` |

**源码解析**：

```java
public void sendFeedback(Feedback feedback) {
    try {
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE_DIRECT,        // "dong.medicine.direct"
            RabbitMQConstants.ROUTING_KEY_FEEDBACK,    // "feedback"
            feedback                                   // 直接发送Entity对象
        );
        log.debug("反馈消息已发送, id={}, userId={}", feedback.getId(), feedback.getUserId());
    } catch (Exception e) {
        log.error("发送反馈消息失败, feedbackId={}, error={}", feedback.getId(), e.getMessage(), e);
        throw e;
    }
}
```

**调用方**：`FeedbackServiceImpl.submitFeedback()` 提交反馈时调用。

**设计特点**：直接发送 `Feedback` Entity对象，消费者收到后直接 `insert` 到数据库，无需额外转换。

---

### 4.3 StatisticsProducer -- 统计消息生产者

> 类比：药铺的"计数器" -- 每次有人看药、买药，不用当场记账，按一下计数器（发消息），后面统一算账。

**核心职责**：将浏览/点赞/分享/下载等统计事件发送到 `statistics.queue`，由消费者异步更新Redis计数器。

**消息路由**：

| 项目 | 值 |
|------|-----|
| Exchange | `dong.medicine.topic`（Topic通配符路由） |
| Routing Key | `statistics.{statisticsType}`（动态拼接，如 `statistics.view`） |
| Queue | `statistics.queue`（绑定 `statistics.*`） |

**源码解析**：

```java
public void sendStatisticsTask(StatisticsTask task) {
    try {
        // 动态路由键：statistics.view / statistics.like / statistics.share / statistics.download
        String routingKey = RabbitMQConstants.ROUTING_KEY_STATISTICS + "." + task.getStatisticsType();

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE_TOPIC,  // "dong.medicine.topic"
            routingKey,                        // "statistics.view" 等
            task
        );
        log.debug("统计消息已发送, type={}, targetId={}", task.getStatisticsType(), task.getTargetId());
    } catch (Exception e) {
        log.error("发送统计消息失败, type={}, error={}", task.getStatisticsType(), e.getMessage(), e);
        throw e;
    }
}
```

**内部类 StatisticsTask**：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public static class StatisticsTask {
    private String statisticsType;  // 统计类型: "view" / "like" / "share" / "download"
    private Integer targetId;       // 目标ID（植物ID/知识ID等）
    private String targetType;      // 目标类型: "plant" / "knowledge" / "inheritor" 等
    private String actionType;      // 操作类型: "add" / "remove"（用于点赞的增减）
    private Integer userId;         // 操作用户ID
    private Long timestamp;         // 时间戳
    private String extraData;       // 扩展数据（JSON格式）
}
```

**技术选型 -- 为什么用Topic Exchange？**

统计消息使用Topic Exchange而非Direct Exchange，因为：
1. 路由键动态拼接 `statistics.{type}`，支持按统计类型分发
2. 未来可扩展为 `statistics.view.plant`、`statistics.like.knowledge` 等更细粒度的路由
3. 消费者可通过绑定 `statistics.*` 接收所有统计消息，也可绑定 `statistics.view` 只接收浏览统计

---

### 4.4 FileProcessProducer -- 文件处理生产者

> 类比：药铺的"加工坊" -- 新到的药材需要切片、晒干、研磨，先把任务单投进加工坊的"待加工箱"。

**核心职责**：将文件异步处理任务（图片压缩、文档转换、文件验证）发送到 `file.process.queue`。

**消息路由**：

| 项目 | 值 |
|------|-----|
| Exchange | `dong.medicine.direct` |
| Routing Key | `file.process` |
| Queue | `file.process.queue` |

**内部类 FileProcessTask**：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public static class FileProcessTask {
    private String fileId;            // 文件唯一标识
    private String filePath;          // 文件存储路径
    private String fileName;          // 文件名
    private String fileType;          // 文件MIME类型
    private String processType;       // 处理类型: "image-compress" / "document-convert" / "file-validate"
    private String originalFileName;  // 原始文件名
    private Long fileSize;            // 文件大小（字节）
    private Integer userId;           // 上传用户ID
    private String businessType;      // 业务类型（关联模块）
}
```

**processType 取值**：

| 值 | 说明 | 消费者处理 |
|----|------|-----------|
| `image-compress` | 图片压缩 | 检查文件存在性，执行压缩逻辑 |
| `document-convert` | 文档格式转换 | 转换文档格式（如PDF预览） |
| `file-validate` | 文件完整性验证 | 验证文件存在性和可读性 |

---

### 4.5 NotificationProducer -- 通知消息生产者

> 类比：药铺的"传话小厮" -- 掌柜让小厮去通知某位顾客"您的药方配好了"，小厮不用当场跑腿，先记在通知簿上。

**核心职责**：将用户通知消息发送到 `notification.queue`，支持3种通知类型的便捷发送方法。

**消息路由**：

| 项目 | 值 |
|------|-----|
| Exchange | `dong.medicine.direct` |
| Routing Key | `notification` |
| Queue | `notification.queue` |

**内部类 NotificationMessage**：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public static class NotificationMessage {
    private Integer userId;        // 目标用户ID
    private String type;           // 通知类型: "feedback_reply" / "qa_answer" / "system"
    private String title;          // 通知标题
    private String content;        // 通知内容
    private String relatedType;    // 关联类型: "feedback" / "qa" / "system"
    private Integer relatedId;     // 关联ID
    private Boolean isRead;        // 是否已读
    private LocalDateTime createTime;  // 创建时间
}
```

**3个便捷方法**：

| 方法 | 通知类型 | 触发场景 |
|------|---------|---------|
| `sendFeedbackReplyNotification(userId, feedbackTitle, reply)` | `feedback_reply` | 管理员回复用户反馈时 |
| `sendQaAnswerNotification(userId, questionTitle, answerContent)` | `qa_answer` | 用户的问答有了新回答时 |
| `sendSystemNotification(userId, title, content)` | `system` | 系统级通知（如账号封禁、公告等） |

**自动填充时间戳**：

```java
public void sendNotification(NotificationMessage notification) {
    // 如果调用方未设置createTime，自动填充当前时间
    if (notification.getCreateTime() == null) {
        notification.setCreateTime(LocalDateTime.now());
    }
    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE_DIRECT,
        RabbitMQConstants.ROUTING_KEY_NOTIFICATION,
        notification
    );
}
```

**设计思路**：提供便捷方法封装通知构造逻辑，调用方只需传入业务参数（如反馈标题、回复内容），无需关心通知对象的字段映射。`sendNotification()` 是底层方法，3个便捷方法内部都调用它。

---

## 五、消息路由对照表

| Producer | Exchange | Routing Key | Queue | Exchange类型 |
|----------|----------|-------------|-------|-------------|
| OperationLogProducer | `dong.medicine.direct` | `operation.log` | `operation.log.queue` | Direct |
| FeedbackProducer | `dong.medicine.direct` | `feedback` | `feedback.queue` | Direct |
| StatisticsProducer | `dong.medicine.topic` | `statistics.{type}` | `statistics.queue` | Topic |
| FileProcessProducer | `dong.medicine.direct` | `file.process` | `file.process.queue` | Direct |
| NotificationProducer | `dong.medicine.direct` | `notification` | `notification.queue` | Direct |

---

## 六、消息格式分类

### 直接发送Entity对象

| Producer | 消息对象 | 说明 |
|----------|---------|------|
| OperationLogProducer | `OperationLog` | 直接使用Entity，消费者直接insert |
| FeedbackProducer | `Feedback` | 直接使用Entity，消费者直接insert |

### 使用内部类封装

| Producer | 内部类 | 说明 |
|----------|--------|------|
| StatisticsProducer | `StatisticsTask` | 自定义统计任务对象，含统计类型/目标/操作等 |
| FileProcessProducer | `FileProcessTask` | 自定义文件处理任务，含文件路径/类型/处理方式等 |
| NotificationProducer | `NotificationMessage` | 自定义通知消息，含用户/类型/标题/内容等 |

**设计选择**：OperationLog和Feedback直接使用Entity是因为消费者需要完整实体数据直接入库；Statistics/FileProcess/Notification使用内部类是因为消息格式与数据库表结构不同，需要额外的字段（如processType、actionType等）。

---

## 七、条件启用与降级

### 条件启用

所有Producer通过 `@ConditionalOnProperty` 条件注入：

```java
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
```

- `app.rabbitmq.enabled=true`（默认）：Producer正常注入，消息发送到RabbitMQ
- `app.rabbitmq.enabled=false`：Producer不会被Spring创建，相关功能降级

### 降级策略

| Producer | 降级方案 |
|----------|---------|
| OperationLogProducer | `OperationLogAspect` 捕获异常，降级为 `OperationLogService.save()` 同步写入 |
| FeedbackProducer | `FeedbackServiceImpl` 捕获异常，降级为直接调用Mapper插入 |
| StatisticsProducer | 统计数据丢失（可接受，非关键业务） |
| FileProcessProducer | 文件上传成功但异步处理不执行 |
| NotificationProducer | 通知不发送（可接受，非关键业务） |

---

## 八、文件间依赖关系

```
OperationLogProducer ──依赖──► RabbitMQConstants（Exchange/Queue/RoutingKey常量）
                      ──依赖──► OperationLog Entity
                      ──依赖──► RabbitTemplate

FeedbackProducer ──依赖──► RabbitMQConstants
                ──依赖──► Feedback Entity
                ──依赖──► RabbitTemplate

StatisticsProducer ──依赖──► RabbitMQConstants
                  ──依赖──► RabbitTemplate
                  ──内部类──► StatisticsTask（自包含）

FileProcessProducer ──依赖──► RabbitMQConstants
                   ──依赖──► RabbitTemplate
                   ──内部类──► FileProcessTask（自包含）

NotificationProducer ──依赖──► RabbitMQConstants
                    ──依赖──► RabbitTemplate
                    ──内部类──► NotificationMessage（自包含）
```

5个Producer之间**无直接依赖**，各自独立发送不同类型的消息。它们共同依赖 `RabbitMQConstants`（路由常量）和 `RabbitTemplate`（发送工具）。
