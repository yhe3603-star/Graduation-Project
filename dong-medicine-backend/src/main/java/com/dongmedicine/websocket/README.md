# WebSocket 层 -- AI聊天实时通信

> 本目录存放WebSocket处理器，实现AI聊天的流式推送。传统HTTP请求-响应模式无法实现"逐字输出"的效果，WebSocket通过长连接解决了这个问题。

---

## 一、文件说明

```
websocket/
└── ChatWebSocketHandler.java    # AI聊天WebSocket处理器（核心文件）
```

---

## 二、架构设计

```
┌─────────────────────────────────────────────────────────────────────┐
│                          前端（浏览器）                               │
│  new WebSocket("ws://host:8080/ws/chat?token=xxx")                 │
│  → 发送: {"type":"chat", "message":"钩藤有什么功效?", ...}           │
│  ← 接收: {"type":"start"}, {"type":"token","content":"钩"}, ...     │
│         {"type":"token","content":"藤"}, ...                        │
│         {"type":"done","content":"钩藤具有清热平肝..."}              │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ WebSocket连接
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   WebSocketConfig                                    │
│  注册 /ws/chat 端点 → AuthHandshakeInterceptor                     │
│  从URL参数中提取Token → 验证 → 存入session attributes              │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                ChatWebSocketHandler                                  │
│  extends TextWebSocketHandler                                       │
│                                                                     │
│  核心方法:                                                          │
│  afterConnectionEstablished()  → 保存session到ConcurrentHashMap    │
│  handleTextMessage()           → 解析JSON文本消息                   │
│  afterConnectionClosed()       → 清理session + 持久化聊天记录       │
│  handleTransportError()       → 错误处理 + 清理                    │
│                                                                     │
│  消息类型处理:                                                       │
│  type="chat"  → handleChat()    → AI流式回复                       │
│  type="stop"  → handleStop()    → 取消正在生成的AI回复              │
└───────────┬─────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    AiChatService                                     │
│  chatStream(message, history, callback)                             │
│  → Disposable subscription                                         │
│  → callback.onToken(token)   // 每个token立即推送给前端             │
│  → callback.onComplete(full) // 完成时推送完整回复                  │
│  → callback.onError(error)   // 错误时推送错误信息                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 三、ChatWebSocketHandler 详解

文件：`websocket/ChatWebSocketHandler.java`

### 3.1 核心数据结构

```java
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 活跃的WebSocket连接：sessionId → WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 活跃的AI请求订阅（用于取消正在生成的回复）：sessionId → Disposable
    private final Map<String, Disposable> activeSubscriptions = new ConcurrentHashMap<>();

    // 聊天会话ID映射：wsSessionId → chatSessionId
    private final Map<String, String> chatSessionIds = new ConcurrentHashMap<>();

    // 专用线程池（最多50个线程），避免阻塞WebSocket I/O线程
    private final ExecutorService chatExecutor = Executors.newFixedThreadPool(50, 
        r -> new Thread(r, "ws-chat-{N}"));
}
```

### 3.2 连接生命周期

```
afterConnectionEstablished(session)
  │
  ├── session.getId() → 存入 sessions Map
  └── 日志记录连接信息

handleTextMessage(session, message)
  │
  ├── 解析JSON → 获取 type 字段
  ├── type="chat"  → handleChat(session, json)
  │   ├── 取消该session之前的AI请求（如果有）
  │   ├── 生成或复用 chatSessionId
  │   ├── 保存用户消息到Redis（ChatHistoryService.saveMessageToRedis）
  │   ├── 发送 {"type":"start", "sessionId":"..."} 给前端
  │   ├── 提交到 chatExecutor 线程池异步执行
  │   │   └── aiChatService.chatStream(message, history, callback)
  │   │       ├── callback.onToken(token)
  │   │       │   └── 发送 {"type":"token", "content":"钩"} 给前端
  │   │       ├── callback.onComplete(fullReply)
  │   │       │   ├── 移除activeSubscriptions中的记录
  │   │       │   ├── 发送 {"type":"done", "content":"完整回复"} 给前端
  │   │       │   └── 保存AI回复到Redis
  │   │       └── callback.onError(error)
  │   │           ├── 移除activeSubscriptions中的记录
  │   │           └── 发送 {"type":"error", "content":"错误信息"} 给前端
  │   └── 将Disposable存入activeSubscriptions
  │
  └── type="stop"  → handleStop(session)
      └── 调用Disposable.dispose() 取消正在进行的AI请求

afterConnectionClosed(session, status)
  │
  ├── 从sessions中移除
  ├── 从chatSessionIds中移除
  ├── 取消该session的AI请求（如果有）
  └── 将Redis中的聊天记录刷入MySQL（ChatHistoryService.flushSessionToMysql）

handleTransportError(session, exception)
  │
  └── 清理所有相关资源
```

### 3.3 WebSocket消息协议

**前端发送的消息格式**：

```json
// 开始聊天
{
  "type": "chat",
  "message": "钩藤有什么功效？",
  "sessionId": "optional-existing-session-id",
  "history": [
    {"role": "user", "content": "你好"},
    {"role": "assistant", "content": "你好！请问有什么可以帮您？"}
  ]
}

// 停止生成
{
  "type": "stop"
}
```

**后端推送的消息格式**：

```json
// 开始生成
{"type": "start", "sessionId": "uuid-xxx"}

// 逐字推送
{"type": "token", "content": "钩"}

// 生成完成
{"type": "done", "content": "钩藤具有清热平肝、息风止痉的功效...", "sessionId": "uuid-xxx"}

// 已停止
{"type": "stopped"}

// 错误
{"type": "error", "content": "AI服务暂时不可用"}
```

---

## 四、WebSocketConfig 配置

文件：`config/WebSocketConfig.java`

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Value("${app.websocket.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new AuthHandshakeInterceptor())
                .setAllowedOrigins(allowedOrigins.split(","));
    }
}
```

### 握手认证（AuthHandshakeInterceptor）

```java
static class AuthHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ...) {
        // 从URL参数中获取Token: ws://host/ws/chat?token=xxx
        String token = servletRequest.getParameter("token");
        if (token != null) {
            // 验证Token，获取userId
            Object loginId = StpUtil.getLoginIdByToken(token);
            attributes.put("userId", Integer.parseInt(loginId.toString()));
            // 已登录用户 → 聊天记录会被持久化
        } else {
            attributes.put("userId", null);
            // 匿名用户 → 聊天记录不持久化
        }
        return true;  // 始终允许连接，匿名用户也可以聊天
    }
}
```

---

## 五、聊天记录持久化

### 两级存储架构

```
┌─────────────────────────────────────────────────┐
│  聊天过程中                                      │
│  ChatHistoryService.saveMessageToRedis()        │
│  → 消息暂存Redis，key: chat:session:{sessionId} │
│  → List结构，按时间追加                          │
│  → 设置30分钟过期（自动续期）                     │
└─────────────────────────────────────────────────┘
                    │
                    │ WebSocket断开时
                    ▼
┌─────────────────────────────────────────────────┐
│  连接关闭时                                      │
│  ChatHistoryService.flushSessionToMysql()       │
│  → 从Redis读取整个会话的消息                     │
│  → 批量INSERT到MySQL chat_history表             │
│  → 删除Redis中的临时数据                         │
└─────────────────────────────────────────────────┘
```

### 代码流程

```java
// ChatWebSocketHandler中

// 1. 用户发送消息 → 存入Redis
chatHistoryService.saveMessageToRedis(userId, chatSessionId, "user", userMessage);

// 2. AI回复完成 → 存入Redis  
chatHistoryService.saveMessageToRedis(userId, chatSessionId, "assistant", fullReply);

// 3. WebSocket断开 → 批量刷入MySQL
chatHistoryService.flushSessionToMysql(userId, chatSessionId);
```

---

## 六、性能与并发设计

### 线程模型

```
WebSocket I/O线程（Netty/Tomcat NIO）
  │
  ├── 接收消息 → 解析JSON → 路由到handleChat/handleStop
  │
  └── handleChat → chatExecutor.submit(task)
                    │
                    ▼
               ws-chat-1 线程（最多50个）
               │
               └── aiChatService.chatStream()
                   └── WebClient 流式调用 DeepSeek API
                       └── 每个token → session.sendMessage() → 推送给WebSocket I/O线程
```

**关键设计点**：
- AI调用在专用线程池 `chatExecutor`（50线程）中执行，不阻塞WebSocket I/O线程
- 发送消息时使用 `session.sendMessage()`，这是线程安全的
- 使用 `ConcurrentHashMap` 管理所有共享状态
- 每个session同时只有一个活跃的AI请求（新请求会取消旧请求）

### 取消机制

用户点击"停止生成"时：
```
前端发送 {"type":"stop"}
  → handleStop(session)
    → activeSubscriptions.get(wsSessionId)
    → Disposable.dispose()  // 取消WebFlux的响应式流订阅
    → activeSubscriptions.remove(wsSessionId)
    → 发送 {"type":"stopped"}
```

### 资源清理

连接关闭（正常/异常）时，`afterConnectionClosed` 和 `handleTransportError` 都会：
1. 移除 session 映射
2. 移除 chatSessionId 映射
3. 取消活跃的 AI 请求
4. 将聊天记录从 Redis 刷入 MySQL

---

## 七、ChatHistoryService 交互

文件：`service/ChatHistoryService.java`

```java
// 保存消息到Redis（临时存储，聊天过程中使用）
void saveMessageToRedis(Integer userId, String sessionId, String role, String content);

// 从Redis获取会话的完整聊天记录
List<ChatHistory> getSessionHistory(Integer userId, String sessionId);

// 将Redis中的消息批量写入MySQL（WebSocket断开时调用）
void flushSessionToMysql(Integer userId, String sessionId);

// 获取用户的所有会话列表
List<Map<String, Object>> getUserSessions(Integer userId);

// 删除整个会话
void deleteSession(Integer userId, String sessionId);
```

---

## 八、ChatHistoryController

文件：`controller/ChatHistoryController.java`

提供前端查询和管理聊天记录的REST API：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/chat-history/sessions` | 获取当前用户的所有会话列表（需登录） |
| GET | `/api/chat-history/sessions/{sessionId}` | 获取指定会话的所有消息（需登录） |
| DELETE | `/api/chat-history/sessions/{sessionId}` | 删除指定会话（需登录） |

---

## 九、ChatController（HTTP统计）

文件：`controller/ChatController.java`

提供了简单的聊天统计接口：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/chat/stats` | 获取AI聊天统计（总请求数、成功数、失败数） |

使用 `AtomicLong` 计数器，在 `ChatWebSocketHandler` 的 `onComplete` / `onError` 中更新。

---

## 十、前端连接示例

```javascript
// 连接WebSocket
const token = localStorage.getItem('token');
const ws = new WebSocket(`ws://localhost:8080/ws/chat?token=${token}`);

// 发送消息
ws.send(JSON.stringify({
  type: 'chat',
  message: '钩藤有什么功效？',
  sessionId: currentSessionId,  // 可选
  history: []  // 可选，对话历史
}));

// 接收消息
ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  switch (data.type) {
    case 'start':
      console.log('AI开始回复, sessionId:', data.sessionId);
      break;
    case 'token':
      appendText(data.content);  // 逐字追加到界面
      break;
    case 'done':
      console.log('AI回复完成:', data.content);
      break;
    case 'stopped':
      console.log('生成已停止');
      break;
    case 'error':
      console.error('AI服务错误:', data.content);
      break;
  }
};

// 停止生成
ws.send(JSON.stringify({ type: 'stop' }));
```

---

## 十一、配置参考

```yaml
# application.yml
app:
  websocket:
    allowed-origins: "http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173"

deepseek:
  api-key: ${DEEPSEEK_API_KEY}
  base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
  model: ${DEEPSEEK_MODEL:deepseek-chat}
  connect-timeout: 10   # 连接超时（秒）
  read-timeout: 60      # 读取超时（秒）
```
