# 大文件组件拆分设计文档

**日期**: 2026-05-03
**范围**: P0 巨石组件拆分 (PersonalCenter.vue + AiChatCard.vue)
**策略**: 方案一 — 顺序推进，先 PersonalCenter 后 AiChatCard

## 目标

| 文件 | 当前行数 | 目标行数 | 拆分数 |
|------|---------|---------|--------|
| PersonalCenter.vue | 1131 | <150 | 6 子组件 + 1 composable |
| AiChatCard.vue | 916 | <250 | 5 子组件 + 2 composables |

## Phase 1: PersonalCenter.vue

### 现状结构 (1131 行)

- Template 183 行: not-logged-in + profile + quick-actions + 5 tabs
- Script 340 行: 混用 usePersonalCenter composable + 内联 browseHistory + 内联 studyStats + ECharts chart
- Style 540 行: 7 个功能区的样式 + 3 层响应式断点

### 拆分架构

```
PersonalCenter.vue  (容器 ~120行)
  │  <template> — 子组件拼装，不写业务 DOM
  │  <script> — 只做 tab 切换 + 数据宿主 + 分发 props
  │  <style> — 仅 .personal-container grid + .not-logged-in (~40行)
  │
  ├── ProfileSection.vue         props: { userName, isAdmin, studyStats, actions }
  │     emits: ['tab-change']
  │
  ├── StatsDashboard.vue         props: { quizRecords, gameRecords, favorites, browseHistory }
  │     内部: ECharts 实例 + useStudyStats composable
  │
  ├── FavoritesPanel.vue         props: { favorites[] }
  │     内部: 类型筛选 + 分页 + emits('go-detail')
  │
  ├── QuizHistoryPanel.vue       props: { quizRecords[], gameRecords[] }
  │     内部: 合并排序 + 分页
  │
  ├── BrowseHistoryPanel.vue     (无 props，自管理 API 调用)
  │     内部: 加载 + 分页 + 跳转
  │
  └── SettingsPanel.vue          props: { userName, isAdmin }
        内部: 密码表单 + 验证码 + 退出登录
```

### 数据宿主 (PersonalCenter.vue 保留)

```js
// 已有 composable — 不动
const { favorites, quizRecords, gameRecords, passwordForm, passwordRules,
        handleChangePassword, resetPasswordForm, handleLogout, ... } = usePersonalCenter(...)

// 新建 composable — 从组件 script 提取
const { studyStats, hasScoreData, initScoreChart, disposeChart } = useStudyStats()

// Browse History — 保留在父组件作为数据宿主，传给 BrowseHistoryPanel
const { browseHistory, loadBrowseHistory, goToHistoryItem } = useBrowseHistory()
```

### 新增 Composables

**useStudyStats.js** (~100行): 从 PersonalCenter.vue script 行 327-587 提取
- `studyStatsRaw`, `studyStats` (computed)
- `computeStudyStats()`, `buildScoreTrendData()`
- `initScoreChart(ref)`, `disposeChart()`
- `hasScoreData`

### 样式迁移

- 子组件自带 `<style scoped>`，仅含自己需要的样式
- 共享 SCSS 变量通过 Vite 已配置的自动注入获得
- 响应式断点分散到各子组件，各自负责

---

## Phase 2: AiChatCard.vue

### 现状结构 (916 行)

- Template 82 行: drawer + session badge + welcome + messages + input
- Script 414 行: WebSocket + 消息处理 + 会话 CRUD + markdown 渲染
- Style 347 行: chat layout + drawer + messages + 响应式

### 拆分架构

```
AiChatCard.vue  (容器 ~200行)
  │  <template> — el-card + 子组件拼装
  │  <script> — WebSocket 生命周期 + 登录状态监听
  │  <style> — 仅 .ai-chat-wrapper + .ai-chat-card (~30行)
  │
  ├── SessionDrawer.vue          props: { visible, sessions, currentSessionId }
  │     emits: ['close', 'select', 'delete', 'new-chat']
  │
  ├── WelcomePanel.vue           props: { quickQuestions[] }
  │     emits: ['send-question']
  │
  ├── ChatMessageList.vue        props: { messages[], streaming }
  │     内部: markdown 渲染 + 自动滚动到底部
  │
  ├── ChatInputArea.vue          props: { disabled, wsConnected, streaming }
  │     emits: ['send', 'stop']
  │     v-model: inputMessage
  │
  └── SessionBadge.vue           props: { sessionId }
        emits: ['new-chat']
```

### 新增 Composables

**useChatWebSocket.js** (~200行): 从行 204-308 提取
- `ws`, `wsConnected`, `streaming`
- `connectWebSocket()`, `handleWsMessage()`
- `sendMessage()`, `stopGeneration()`
- `messages`, `currentSessionId`, `currentAssistantIndex`
- `resetToWelcome()`

**useChatSessions.js** (~120行): 从行 370-510 提取
- `sessions`, `sessionsLoading`, `drawerVisible`
- `loadSessions()`, `loadSession()`, `deleteSession()`
- `startNewChat()`, `formatSessionTime()`, `updateLocalSession()`

### 风险控制

1. **WebSocket 生命周期**: `onMounted/onActivated/onDeactivated/onUnmounted` 中的 WS 管理逻辑在提取后保持完全一致的时序
2. **流式渲染**: `messages[currentAssistantIndex]` 响应性在提取到 composable 后保持不变（composable 返回的 ref 在模板中同样响应）
3. **forceWelcome**: 保留在父组件中，不被提取

---

## 验证标准

每个 Phase 完成后的验证命令：

```bash
# 前端构建
cd dong-medicine-frontend && npm run build

# 单元测试
cd dong-medicine-frontend && npm run test:run

# E2E 测试 (需要后端)
cd dong-medicine-frontend && npm run test:e2e

# Lint
cd dong-medicine-frontend && npm run lint
```

验收条件：
- Build 无错误
- 所有已有测试通过（不新增失败的测试）
- Lint 无新增警告
- 新组件文件每个 < 200 行
- 原组件文件 < 250 行
