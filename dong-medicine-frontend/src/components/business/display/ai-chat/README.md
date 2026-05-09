# AI 对话子组件（ai-chat Components）

## 什么是 AI 对话子组件？

类比：**客服中心的各个工位**——前台接待区（欢迎面板）、对话窗口（消息列表）、输入台（输入区域）、档案柜（会话抽屉）、工号牌（会话徽章），每个工位各司其职，协同完成一次完整的智能对话服务。

本目录包含 `AiChatCard.vue` 的 5 个子组件，它们共同构成了侗医智能助手的对话界面。`AiChatCard.vue` 作为父容器组件位于上级 `display/` 目录，负责组合这些子组件并管理 WebSocket 连接和会话状态。

```
AiChatCard.vue（父组件，display/ 目录）
├── WelcomePanel.vue     ← 欢迎面板：首次进入时的引导界面
├── ChatMessageList.vue  ← 消息列表：对话消息的渲染与滚动
├── ChatInputArea.vue    ← 输入区域：用户输入与发送/停止控制
├── SessionDrawer.vue    ← 会话抽屉：历史会话列表管理
└── SessionBadge.vue     ← 会话徽章：当前会话状态标识
```

---

## 组件列表

### WelcomePanel -- 欢迎面板

**用途：** 用户首次进入 AI 聊天或开始新对话时，展示欢迎消息和快捷提问标签

```
┌────────────────────────────────────┐
│              🤖                    │
│                                    │
│     您好！我是侗族医药智能助手      │
│   您可以问我关于侗族医药的问题     │
│                                    │
│  [侗族医药有什么特点？]             │
│  [什么是侗族药浴疗法？]             │
│  [侗族常用草药有哪些？]             │
│       ↑ 点击快捷标签直接提问        │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<WelcomePanel
  :quick-questions="['侗族医药有什么特点？', '什么是侗族药浴疗法？']"
  @send-question="handleQuickQuestion"
/>
```

**核心逻辑：**

- 通过 `quickQuestions` prop 接收快捷问题数组，由父组件 `AiChatCard.vue` 定义默认值
- 点击快捷标签触发 `send-question` 事件，父组件调用 `sendQuickQuestion()` 方法直接发送问题
- 当消息列表为空且非流式输出时，父组件通过 `v-show="showWelcome"` 控制欢迎面板显示

**响应式设计：** 480px 以下快捷标签改为纵向排列（`flex-direction: column`），适配窄屏。

---

### ChatMessageList -- 消息列表

**用途：** 渲染用户与 AI 的对话消息，支持 Markdown 格式渲染和流式输出光标动画

```
┌────────────────────────────────────┐
│                 👤  用户消息        │
│              ┌──────────────┐      │
│              │ 钩藤有什么功效？│      │
│              └──────────────┘      │
│                                    │
│  🤖  AI 回复                       │
│  ┌──────────────────────┐         │
│  │ 钩藤具有**息风止痉**、 │         │
│  │ 清热平肝的功效...▌    │ ← 流式光标│
│  └──────────────────────┘         │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<ChatMessageList
  ref="messageListRef"
  :messages="messages"
  :streaming="streaming"
/>
```

**核心算法实现：**

1. **Markdown 渲染与 XSS 防护**

   ```javascript
   function renderMarkdown(text) {
     if (!text) return ''
     const html = marked.parse(text)           // Markdown → HTML
     return DOMPurify.sanitize(html, DOMPURIFY_CONFIG)  // HTML → 安全 HTML
   }
   ```

   采用 `marked` + `DOMPurify` 双重处理管线：`marked` 将 Markdown 文本解析为 HTML，`DOMPurify` 再对 HTML 进行安全过滤。`DOMPURIFY_CONFIG` 配置了严格的白名单策略：

   | 类别 | 配置 |
   |------|------|
   | 允许的标签 | `p, br, strong, em, b, i, u, s, code, pre, ul, ol, li, h1-h6, blockquote, a, table` 系列, `hr, span, sub, sup, del, mark` |
   | 允许的属性 | `href, target, rel, class` |
   | 禁止的标签 | `style, script, iframe, form, input, button, object, embed, link` |
   | 禁止的属性 | `onerror, onload, onclick, onmouseover, onfocus, onblur` |

2. **自动滚动到底部**

   ```javascript
   watch(() => props.messages.length, scrollToBottom)
   watch(() => props.messages, scrollToBottom, { deep: true })
   ```

   通过双重 `watch` 监听消息数量变化和消息内容变化（deep watch），确保流式输出时每次 token 追加都能自动滚动到底部。`scrollToBottom` 使用 `nextTick` 确保 DOM 更新完成后再执行滚动。

3. **流式输出光标动画**

   当消息处于流式输出状态（`msg.streaming === true`）时，在消息文本末尾追加闪烁光标 `▌`，通过 CSS `@keyframes blink` 实现 0.8 秒周期的闪烁效果。

**设计思路：** 用户消息和 AI 消息采用不同的视觉样式区分——用户消息为靛蓝背景白字（右对齐），AI 消息为白底深色字带阴影（左对齐），头像分别使用 `👤` 和 `🤖` emoji。

---

### ChatInputArea -- 输入区域

**用途：** 提供消息输入框和发送/停止按钮，支持流式输出中断

```
┌────────────────────────────────────┐
│  [请输入您的问题...]    [发送]     │  ← 正常状态
│  [请输入您的问题...]    [停止]     │  ← 流式输出中
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<ChatInputArea
  v-model="inputMessage"
  :disabled="!wsConnected"
  :streaming="streaming"
  @send="handleSend"
  @stop="handleStop"
/>
```

**核心逻辑：**

- **双向绑定：** 通过 `v-model`（`modelValue` + `update:modelValue`）与父组件的 `inputMessage` 状态同步
- **发送条件：** 输入框非空（`modelValue.trim()`）且非禁用状态时发送按钮才可用
- **流式中断：** 流式输出期间，发送按钮切换为红色"停止"按钮，点击触发 `stop` 事件，父组件调用 `stopGeneration()` 向 WebSocket 发送 `{ type: 'stop' }` 消息
- **键盘快捷键：** 按 Enter 键触发发送（`@keyup.enter`）
- **禁用逻辑：** WebSocket 未连接或正在流式输出时，输入框禁用

---

### SessionDrawer -- 会话抽屉

**用途：** 以侧边抽屉形式展示用户的历史会话列表，支持切换、删除和新建会话

```
┌──────────────────────────┐
│  历史会话                 │
│  ┌────────────────────┐  │
│  │  [+ 新对话]         │  │
│  └────────────────────┘  │
│                          │
│  ┌────────────────────┐  │
│  │ 钩藤的功效是什么？   │  │ ← 当前选中
│  │ 2小时前        🗑️  │  │
│  ├────────────────────┤  │
│  │ 侗族药浴疗法        │  │
│  │ 1天前          🗑️  │  │
│  ├────────────────────┤  │
│  │ 常用草药推荐        │  │
│  │ 3天前          🗑️  │  │
│  └────────────────────┘  │
└──────────────────────────┘
```

```vue
<!-- 使用示例 -->
<SessionDrawer
  :visible="drawerVisible"
  :sessions="sessions"
  :sessions-loading="sessionsLoading"
  :current-session-id="currentSessionId"
  :format-session-time="formatSessionTime"
  @open="loadSessions"
  @close="drawerVisible = false"
  @select="onSelectSession"
  @delete="onDeleteSession"
  @new-chat="startNewChat"
/>
```

**核心逻辑：**

- **时间格式化：** `formatSessionTime` 函数由父组件通过 prop 注入（来自 `useChatSessions` composable），实现相对时间显示（刚刚/X分钟前/X小时前/X天前），超过 7 天显示日期
- **会话选中状态：** 当前会话 ID 匹配时添加 `.active` 类，使用靛蓝渐变背景高亮
- **删除确认：** 删除按钮默认隐藏（`opacity: 0`），hover 时显示，点击触发 `delete` 事件，父组件弹出确认对话框后调用 API 删除
- **空状态处理：** 无历史会话时显示 `el-empty` 组件

**设计思路：** 抽屉从左侧滑出（`direction="ltr"`），宽度 300px，使用 `el-drawer` 组件实现。会话标题取 `session.preview` 字段（最后一条消息的摘要），超长文本使用 `text-overflow: ellipsis` 截断。

---

### SessionBadge -- 会话徽章

**用途：** 在聊天头部显示当前会话状态标识，提供"继续对话"提示和"新对话"快捷入口

```
┌──────────────────────────────────────────┐
│  🤖 侗医智能助手    [💬 继续对话 +新对话] │
│                    ↑ SessionBadge        │
└──────────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<SessionBadge @new-chat="startNewChat" />
```

**核心逻辑：**

- 仅在存在当前会话（`currentSessionId`）且不在欢迎面板时显示（由父组件 `v-if` 控制）
- 点击"新对话"按钮触发 `new-chat` 事件，父组件调用 `resetToWelcome()` 重置聊天状态
- 使用青绿-靛蓝渐变背景和圆角边框，视觉上与品牌色系协调

---

## 文件间的依赖关系与交互流程

### 组件层级关系

```
AiChatCard.vue（父组件）
│
├── useChatWebSocket() composable
│   ├── messages          → ChatMessageList
│   ├── inputMessage      → ChatInputArea (v-model)
│   ├── streaming         → ChatMessageList, ChatInputArea
│   ├── wsConnected       → ChatInputArea (disabled), Header Tag
│   └── currentSessionId  → SessionDrawer, SessionBadge
│
├── useChatSessions() composable
│   ├── sessions          → SessionDrawer
│   ├── sessionsLoading   → SessionDrawer
│   └── drawerVisible     → SessionDrawer
│
├── useUserStore()
│   └── isLoggedIn        → SessionDrawer (显示/隐藏历史按钮)
│
└── 子组件
    ├── WelcomePanel      ← showWelcome = messages.length === 0 && !streaming
    ├── ChatMessageList   ← v-show="!showWelcome"
    ├── ChatInputArea     ← 始终显示
    ├── SessionDrawer     ← isLoggedIn 时显示历史按钮
    └── SessionBadge      ← currentSessionId && !showWelcome
```

### 完整对话交互流程

```
1. 用户进入首页
   → AiChatCard 挂载 → 检查登录状态
   → 已登录：connectWebSocket() + loadSessions()
   → 未登录：仅显示欢迎面板，输入框可用但发送时提示登录

2. 用户点击快捷标签
   → WelcomePanel emit('send-question', question)
   → AiChatCard.onQuickQuestion() → sendQuickQuestion() → sendMessage()
   → WebSocket 发送 { type: 'chat', message, history }
   → 欢迎面板隐藏，消息列表显示

3. AI 流式回复
   → WebSocket 收到 { type: 'start' } → 添加 assistant 消息，streaming = true
   → WebSocket 收到 { type: 'token' } → 追加 content，自动滚动
   → WebSocket 收到 { type: 'done' } → streaming = false，光标消失

4. 用户中断生成
   → ChatInputArea emit('stop')
   → AiChatCard.stopGeneration() → WebSocket 发送 { type: 'stop' }

5. 用户切换历史会话
   → 点击历史按钮 → SessionDrawer 显示
   → 点击会话项 → loadSession() → 加载消息列表 → 自动滚动

6. 用户开始新对话
   → SessionBadge 或 SessionDrawer 的"新对话"按钮
   → resetToWelcome() → 清空消息和 sessionId → 显示欢迎面板
```

### WebSocket 连接生命周期

```
onMounted / onActivated
  → connectWebSocket()
  → ws.onopen: wsConnected = true, 重试计数归零
  → ws.onclose: wsConnected = false, 指数退避重连（最多 5 次，最长 30 秒）
  → ws.onerror: wsConnected = false

onUnmounted / onDeactivated
  → closeWebSocket() → ws.close()
```

---

## 代码审查与改进建议

- [安全] `ChatMessageList.vue` 使用 `v-html` 渲染 AI 返回内容，虽然已使用 `DOMPurify.sanitize()` 过滤，但 `marked` 配置未禁用 HTML 标签解析，建议设置 `{ breaks: true, gfm: true, html: false }` 从源头减少攻击面
- [安全] `ChatMessageList.vue` 聊天消息无长度限制，超长消息可能导致 DOM 渲染卡顿或 WebSocket 传输问题
- [性能] `ChatMessageList.vue` 的 `deep watch` 监听整个 `messages` 数组，流式输出时每个 token 都触发深度比较，建议改为监听最后一条消息的 `content` 长度
- [体验] `WelcomePanel.vue` 的快捷问题硬编码在 `AiChatCard.vue` 中，建议改为从后端配置或根据用户浏览历史动态生成
- [体验] `SessionDrawer.vue` 缺少会话搜索功能，当历史会话较多时用户难以快速定位
- [健壮性] `ChatInputArea.vue` 未对输入内容做前端长度校验，超长文本可能导致后端处理异常
