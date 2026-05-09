# WebSocket测试 (`websocket`)

## 目录定位

本目录包含项目 WebSocket 聊天处理器的单元测试，验证 `com.dongmedicine.websocket` 包下的 `ChatWebSocketHandler` 类。WebSocket 是平台 AI 智能问答功能的核心通信通道，负责处理用户与 AI 的实时对话交互。

## 文件清单

### ChatWebSocketHandlerTest.java - WebSocket聊天处理器测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testAfterConnectionEstablished` | 连接建立时记录会话，验证 session.getId() 被调用 |
| `testAfterConnectionClosed` | 连接关闭时移除会话，验证 session.getId() 被调用 |
| `testHandleTransportError` | 传输错误时移除会话，验证 session.getId() 被调用 |
| `testHandleTextMessage_UnknownType` | 未知消息类型发送错误响应，验证 session.sendMessage 被调用 |
| `testHandleTextMessage_InvalidJson` | JSON 解析失败发送错误响应，验证 session.sendMessage 被调用 |
| `testHandleStop_NoActiveRequest` | 无活跃请求时停止生成正常响应，验证 session.sendMessage 被调用 |
| `testAfterConnectionClosed_WithActiveSubscription` | 连接关闭时有活跃订阅应取消，验证 session.getId() 被调用两次 |

## 测试设计思路

1. **Mock 对象构建**：Mock `WebSocketSession`、`AiChatService`、`ObjectMapper`，隔离外部依赖
2. **Session 生命周期**：覆盖连接建立 -> 消息处理 -> 连接关闭的完整生命周期
3. **消息类型处理**：验证已知类型（stop）和未知类型的消息处理
4. **异常容错**：JSON 解析失败时发送错误消息而非抛出异常
5. **资源清理**：连接关闭时确保活跃订阅被取消，防止资源泄漏

## 核心验证点

| 验证维度 | 说明 |
|---------|------|
| 连接管理 | 建立/关闭/错误时正确维护会话集合 |
| 消息解析 | JSON 解析成功/失败两种路径 |
| 消息类型路由 | 根据 type 字段路由到不同处理逻辑 |
| 错误处理 | 解析失败/未知类型时向客户端发送错误消息 |
| 停止生成 | 无活跃请求时正常响应停止命令 |
| 资源释放 | 连接关闭时取消活跃的 AI 请求订阅 |

## 依赖关系

- `ChatWebSocketHandlerTest` 依赖 `AiChatService`（Mock，AI 对话服务）和 `ObjectMapper`（Mock，JSON 序列化）
- `WebSocketSession` 使用 Mockito Mock，模拟 WebSocket 连接
- `ChatWebSocketHandler` 是 `org.springframework.web.socket.handler.TextWebSocketHandler` 的子类
- 本测试与 `controller/ChatControllerTest` 和 `controller/ChatHistoryControllerTest` 互补：WebSocket 测试覆盖实时通信层，Controller 测试覆盖 HTTP API 层
