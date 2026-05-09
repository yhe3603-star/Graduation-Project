# 展示组件（Display Components）

## 什么是展示组件？

类比：**展示柜**——商场里的展示柜把商品漂亮地陈列出来，让顾客一目了然。

展示组件负责把数据"好看地"呈现给用户，是用户看到最多的组件类型。它们不处理复杂的交互逻辑，主要工作是：

- **接收数据**（通过 props）
- **展示数据**（通过模板）
- **通知父组件**（通过 emit，比如"用户点击了这张卡片"）

```
数据 → [展示组件] → 界面
       ↑ 接收      ↓ 显示
     props        模板渲染
```

---

## 目录结构

```
display/
├── ai-chat/              AI 对话子组件目录
│   ├── ChatInputArea.vue   输入区域
│   ├── ChatMessageList.vue 消息列表
│   ├── SessionBadge.vue    会话徽章
│   ├── SessionDrawer.vue   会话抽屉
│   ├── WelcomePanel.vue    欢迎面板
│   └── README.md           子组件详细文档
├── AiChatCard.vue        AI 对话卡片（容器组件，组合 ai-chat/ 子组件）
├── CardGrid.vue          卡片网格
├── ChartCard.vue         图表卡片
├── KnowledgeGraph.vue    知识图谱
├── PageSidebar.vue       页面侧边栏
├── Pagination.vue        分页器
├── SearchFilter.vue      搜索过滤器
├── StatCard.vue          统计数字卡片
├── UpdateLogCard.vue     更新日志卡片
├── UpdateLogDialog.vue   更新日志对话框
├── index.js              统一导出
└── README.md             本文件
```

---

## 组件列表

### CardGrid -- 卡片网格

**用途：** 以网格形式展示多张卡片（药用植物、知识条目等），支持图片展示、徽章标记和多字段自适应。

```
┌─────────┐ ┌─────────┐ ┌─────────┐
│ [图片]   │ │ [图片]   │ │ [图片]   │
│  钩藤    │ │  透骨草  │ │  九节茶  │
│  gons    │ │  touc    │ │  juc     │
│  息风止痉 │ │  祛风除湿│ │  清热解毒│
│ [草本]   │ │ [藤本]   │ │ [灌木]   │
└─────────┘ └─────────┘ └─────────┘
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `items` | `Array` | `[]` | 数据列表 |
| `showImage` | `Boolean` | `true` | 是否显示图片区 |
| `imageField` | `String` | `"images"` | 图片字段名 |
| `imageErrorText` | `String` | `"图片"` | 图片加载失败提示文字 |
| `titleField` | `String` | `"title"` | 用作标题的字段名 |
| `descLength` | `Number` | `50` | 描述截断长度 |

**Events：** `click` -- 卡片点击，参数为 item 对象

**Slots：** `footer` -- 卡片底部自定义内容（作用域插槽，提供 `item`）

**核心逻辑：**

1. **图片字段智能解析**（`getImage` 函数）：支持 JSON 字符串/数组/对象路径/URL 等多种格式，自动补全 `/` 前缀
   ```javascript
   // 支持的数据格式：
   // 1. JSON 字符串：'[{"path":"/uploads/1.jpg"}]'
   // 2. 数组对象：[{path: '/uploads/1.jpg'}]
   // 3. 字符串数组：['/uploads/1.jpg']
   // 4. 直接 URL：'https://example.com/img.jpg'
   ```

2. **徽章分类映射**（`BADGE_CLASS_MAP`）：将难度/等级/徽章值映射为 gold/green/blue 样式类
   ```javascript
   const BADGE_CLASS_MAP = {
     gold:  ["easy", "beginner", "初级", "省级", "自治区级"],
     green: ["medium", "advanced", "中级"],
     blue:  ["hard", "professional", "高级", "市级"]
   }
   ```

3. **标题字段回退链**：`item[titleField]` -> `item.nameCn` -> `item.name` -> `item.title` -> "未命名"

```vue
<!-- 使用示例 -->
<CardGrid
  :items="plantList"
  image-field="images"
  title-field="nameCn"
  :desc-length="60"
  @click="handleCardClick"
>
  <template #footer="{ item }">
    <el-tag size="small">{{ item.category }}</el-tag>
  </template>
</CardGrid>
```

---

### ChartCard -- 图表卡片

**用途：** 在卡片中展示数据图表（柱状图、饼图、折线图、雷达图等）

```
┌──────────────────────────┐
│  药用植物分类统计    [操作] │
│  ┌──┐                    │
│  │  │ ┌──┐               │
│  │  │ │  │ ┌──┐          │
│  │  │ │  │ │  │          │
│  └──┘ └──┘ └──┘          │
│  草本  灌木  藤本          │
└──────────────────────────┘
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | - | 卡片标题 |
| `option` | `Object` | (必填) | ECharts 配置对象 |
| `height` | `Number` | `300` | 图表高度(px) |
| `loading` | `Boolean` | `false` | 加载状态 |

**Slots：** `actions` -- 图表右上角操作区

**核心逻辑：**

- 按需引入 ECharts 模块（`echarts/core`）：BarChart、LineChart、PieChart、RadarChart
- `watch(option, deep: true)` 深度监听配置变化，自动调用 `chart.setOption(newOpt, true)` 更新
- `onMounted` 初始化图表，`onUnmounted` 自动销毁实例
- 监听 `window.resize` 事件自动调整图表尺寸
- `defineExpose({ resize, chart })` 暴露 resize 方法和 chart 实例

```vue
<!-- 使用示例 -->
<ChartCard
  title="药用植物分类统计"
  :option="chartOption"
  :height="350"
  :loading="chartLoading"
>
  <template #actions>
    <el-button-group>
      <el-button size="small" @click="switchChart('bar')">柱状图</el-button>
      <el-button size="small" @click="switchChart('pie')">饼图</el-button>
    </el-button-group>
  </template>
</ChartCard>
```

---

### KnowledgeGraph -- 知识图谱

**用途：** 以力导向图的形式展示侗医药知识节点与关联关系

```
        ┌─────────┐
        │侗医药知识│ ← 中心节点（靛蓝，大）
        └────┬────┘
       ┌─────┼─────┐
       ↓     ↓     ↓
   ┌─────┐┌─────┐┌─────┐
   │ 钩藤 ││透骨草││九节茶│ ← 药材节点（绿色，中）
   └──┬──┘└──┬──┘└──┬──┘
      ↓       ↓       ↓
   ┌─────┐┌─────┐┌─────┐
   │息风  ││祛风  ││清热  │ ← 功效分类节点（蓝色，小）
   │止痉  ││除湿  ││解毒  │
   └─────┘└─────┘└─────┘
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `plantName` | `String` | `''` | 中心植物名称 |
| `relatedPlants` | `Array` | `[]` | 关联植物列表 |
| `knowledgeTitle` | `String` | `'侗医药知识'` | 中心知识节点标题 |
| `height` | `String/Number` | `'500px'` | 图谱高度 |
| `width` | `String/Number` | `'100%'` | 图谱宽度 |

**核心算法：**

1. **`buildGraphData()`** -- 构建图数据
   - 三类节点：知识（靛蓝，symbolSize: 50）、药材（绿色，symbolSize: 36）、功效分类（蓝色，symbolSize: 28）
   - 功效分类去重：使用 `efficacySet`（Map）确保同名功效只创建一个节点
   - 从 `relatedPlants` 中提取 `efficacy/category/diseaseCategory/herbCategory/therapyCategory` 字段作为功效分类

2. **力导向布局配置**：
   ```javascript
   force: {
     repulsion: 300,        // 节点间斥力
     gravity: 0.08,         // 向心力
     edgeLength: [100, 200], // 边长范围
     layoutAnimation: true,  // 布局动画
     friction: 0.6          // 摩擦系数
   }
   ```

3. **交互特性**：支持拖拽（`draggable: true`）、漫游（`roam: true`）、点击高亮相邻节点（`emphasis.focus: 'adjacency'`）

4. **响应式**：使用 `ResizeObserver` 监听容器尺寸变化自动 resize

```vue
<!-- 使用示例 -->
<KnowledgeGraph
  knowledge-title="侗族药浴疗法"
  :related-plants="bathHerbs"
  height="400px"
/>
```

---

### Pagination -- 分页器

**用途：** 当数据太多时，分页显示，每页只显示一部分

```
共 200 条  每页 [6▼]  ← 上一页  [1]  2  3  4  5  ...  34  下一页 →  前往 [  ] 页
                         ↑ 当前页
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `page` | `Number` | `1` | 当前页码（支持 v-model:page） |
| `size` | `Number` | `6` | 每页条数（支持 v-model:size） |
| `total` | `Number` | `0` | 总条数 |
| `pageSizes` | `Array` | `[6,9,12,24,48]` | 可选每页条数 |
| `layout` | `String` | `"total, sizes, prev, pager, next, jumper"` | 分页布局 |
| `background` | `Boolean` | `true` | 是否使用背景色 |

**Events：**
- `update:page` -- 页码更新
- `update:size` -- 每页条数更新
- `change` -- 分页变化，参数 `{ page, size }`

**功能：** `total > 0` 时才渲染；768px 以下隐藏总数/条数/跳转控件，分页按钮缩小

```vue
<!-- 使用示例 -->
<Pagination
  v-model:page="currentPage"
  v-model:size="pageSize"
  :total="total"
  @change="handlePageChange"
/>
```

---

### SearchFilter -- 搜索过滤器

**用途：** 提供搜索框和筛选条件，帮用户快速找到想要的内容

```
┌────────────────────────────────────────┐
│ 🔍 [搜索药用植物名称...]               │
│                                        │
│ 分类：[全部] [草本] [灌木] [藤本]       │
│ 功效：[全部] [清热] [祛风] [止血]       │
└────────────────────────────────────────┘
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `modelValue` | `String` | - | 搜索关键词（支持 v-model） |
| `placeholder` | `String` | `"搜索..."` | 搜索框占位文字 |
| `filters` | `Array` | `[]` | 筛选配置数组 |

**Events：**
- `update:modelValue` -- v-model 双向绑定更新
- `search` -- 搜索触发（400ms 防抖后）
- `filter` -- 筛选条件变更，参数为当前所有筛选条件对象

**核心逻辑：**

1. **400ms 防抖搜索**：输入后 400ms 无新输入才触发搜索，避免频繁请求
2. **标签式筛选**：点击标签切换选中状态（dark/plain），再次点击取消选中
3. **筛选状态管理**：`activeFilters` 对象存储每组筛选的当前选中值

```vue
<!-- 使用示例 -->
<SearchFilter
  v-model="searchKeyword"
  placeholder="搜索药用植物名称..."
  :filters="[
    { key: 'category', label: '分类', options: [
      { value: 'herb', label: '草本' },
      { value: 'shrub', label: '灌木' }
    ]},
    { key: 'efficacy', label: '功效', options: [
      { value: 'heat', label: '清热' },
      { value: 'wind', label: '祛风' }
    ]}
  ]"
  @search="handleSearch"
  @filter="handleFilter"
/>
```

---

### StatCard -- 统计数字卡片

**用途：** 在仪表盘等场景展示单个统计指标

```
┌─────────────────────┐
│  ┌────┐  128        │
│  │ 🌿 │  药用植物数  │
│  └────┘             │
└─────────────────────┘
  彩色图标  数值/标签
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `icon` | `Object` | (必填) | Element Plus 图标组件 |
| `label` | `String` | (必填) | 统计项标签 |
| `value` | `Number/String` | (必填) | 统计数值 |
| `color` | `String` | (必填) | 图标背景色 |

```vue
<!-- 使用示例 -->
<StatCard
  :icon="DataLine"
  label="药用植物数"
  :value="128"
  color="#28B463"
/>
```

---

### AiChatCard -- AI 对话卡片

**用途：** 展示 AI 侗医药助手的对话界面，用户可以提问并获得流式回复

```
┌──────────────────────────────────────┐
│  🤖 侗医智能助手  [继续对话 +新对话]  │
│                        [历史会话] [AI在线] │
│                                      │
│  ┌────────────────────────────────┐  │
│  │  🤖 您好！我是侗族医药智能助手  │  │  ← WelcomePanel
│  │     [侗族医药有什么特点？]       │  │
│  │     [什么是侗族药浴疗法？]       │  │
│  │     [侗族常用草药有哪些？]       │  │
│  └────────────────────────────────┘  │
│                                      │
│  [请输入您的问题...]        [发送]   │  ← ChatInputArea
└──────────────────────────────────────┘
```

**依赖 Composables：**
- `useChatWebSocket()` -- WebSocket 连接管理、消息收发、流式输出控制
- `useChatSessions()` -- 历史会话列表加载/切换/删除
- `useUserStore()` -- 用户登录状态

**子组件（来自 `ai-chat/` 子目录）：**

| 子组件 | 功能 | 来源 |
|--------|------|------|
| `WelcomePanel` | 欢迎面板，首次进入时展示快捷提问标签 | `./ai-chat/WelcomePanel.vue` |
| `ChatMessageList` | 消息列表，Markdown 渲染 + 流式光标动画 | `./ai-chat/ChatMessageList.vue` |
| `ChatInputArea` | 输入区域，发送/停止按钮切换 | `./ai-chat/ChatInputArea.vue` |
| `SessionDrawer` | 会话抽屉，历史会话列表管理 | `./ai-chat/SessionDrawer.vue` |
| `SessionBadge` | 会话徽章，当前会话状态标识 | `./ai-chat/SessionBadge.vue` |

**核心逻辑：**

1. **显示切换**：`showWelcome = messages.length === 0 && !streaming`，无消息时显示欢迎面板，有消息时显示对话列表
2. **登录联动**：`watch(userStore.isLoggedIn)` 监听登录状态变化，登录时连接 WebSocket 并加载会话，登出时断开连接并清空数据
3. **生命周期管理**：`onMounted`/`onActivated` 连接 WebSocket，`onUnmounted`/`onDeactivated` 断开连接
4. **快捷问题**：硬编码三个默认问题（"侗族医药有什么特点？"等）

```vue
<!-- 使用示例 -->
<AiChatCard />
```

> **ai-chat/ 子目录详细文档**见 [ai-chat/README.md](./ai-chat/README.md)，包含每个子组件的 Props、Events、核心算法和交互流程说明。

---

### UpdateLogCard -- 更新日志卡片

**用途：** 以时间线形式展示内容条目的版本更新记录

```
┌──────────────────────────────────┐
│  🕐 更新日志          [+添加日志] │
│                                  │
│  ● 2026-05-01  管理员            │
│  │ 新增了3种侗族药浴配方          │
│  │                       [✏️][🗑️]│
│  ● 2026-04-15  管理员            │
│  │ 更新了钩藤的功效描述           │
│  │                       [✏️][🗑️]│
│  ● 2026-03-20  管理员            │
│    初始录入药用植物数据           │
│                                  │
│  [查看全部 (8) ▼]                │
└──────────────────────────────────┘
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `logs` | `Array` | `[]` | 日志列表，每项 `{ id, content, time, operator }` |
| `title` | `String` | `'更新日志'` | 卡片标题 |
| `limit` | `Number` | `5` | 默认显示条数 |
| `editable` | `Boolean` | `false` | 是否显示编辑/删除按钮 |
| `emptyText` | `String` | `'暂无更新记录'` | 空状态提示 |

**Events：**
- `add` -- 点击添加日志按钮
- `edit` -- 点击编辑按钮，参数为日志对象
- `delete` -- 确认删除后触发，参数为日志对象

**核心逻辑：**
- `displayLogs` computed：`showAll` 为 true 或日志数 <= limit 时显示全部，否则截取前 limit 条
- 5 色循环节点颜色：`['#1A5276', '#28B463', '#F39C12', '#E74C3C', '#9B59B6']`
- 删除操作使用 `ElMessageBox.confirm` 二次确认

```vue
<!-- 使用示例 -->
<UpdateLogCard
  :logs="updateLogs"
  :editable="true"
  :limit="5"
  @add="handleAddLog"
  @edit="handleEditLog"
  @delete="handleDeleteLog"
/>
```

---

### UpdateLogDialog -- 更新日志对话框

**用途：** 新增/编辑更新日志条目的弹窗表单

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `visible` | `Boolean` | `false` | 对话框显示状态（v-model:visible） |
| `editingLog` | `Object` | `null` | 编辑时传入的日志对象，null 为新增模式 |
| `defaultOperator` | `String` | `'管理员'` | 默认操作人 |

**Events：**
- `update:visible` -- 对话框显示状态更新
- `save` -- 保存日志，参数 `{ id, time, operator, content }`

**核心逻辑：**
- 新增/编辑双模式：`editingLog` 非 null 时填充编辑数据，否则重置表单
- `watch(visible)` 监听对话框打开，自动填充或重置表单
- 保存前校验 `content.trim()` 非空
- `id` 生成：编辑时使用原 id，新增时使用 `Date.now()`

```vue
<!-- 使用示例 -->
<UpdateLogDialog
  v-model:visible="logDialogVisible"
  :editing-log="currentLog"
  default-operator="管理员"
  @save="handleSaveLog"
/>
```

---

### PageSidebar -- 页面侧边栏

**用途：** 在页面侧边展示数据统计和热门推荐

```
┌──────────┐
│ 📊 数据统计│
│ ┌──┐ ┌──┐│
│ │128│ │ 56││
│ │植物│ │知识││
│ └──┘ └──┘│
│ ┌──┐ ┌──┐│
│ │ 12│ │ 8 ││
│ │传承│ │问答││
│ └──┘ └──┘│
├──────────┤
│ 📈 热门推荐│
│ ① 钩藤    │
│ ② 透骨草  │
│ ③ 九节茶  │
│ 4  半夏    │
└──────────┘
```

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | `"数据统计"` | 统计区标题 |
| `stats` | `Array` | `[]` | 统计数据，每项 `{ value, label }` |
| `hotTitle` | `String` | - | 热门推荐区标题 |
| `hotItems` | `Array` | `[]` | 热门推荐列表 |

**Events：** `hotClick` -- 点击热门项

**Slots：** `default` -- 侧边栏底部额外内容

**核心逻辑：**
- 统计区：2x2 Grid 布局展示数值/标签
- 热门推荐：前三名使用金/银/铜色排名标记（`getRankClass` 函数）
- 热门项名称回退：`item.name || item.title || item.nameCn || item.question || '未命名'`

```vue
<!-- 使用示例 -->
<PageSidebar
  title="数据统计"
  :stats="[
    { value: 128, label: '药用植物' },
    { value: 56, label: '知识条目' },
    { value: 12, label: '传承人' },
    { value: 8, label: '问答' }
  ]"
  hot-title="热门植物"
  :hot-items="hotPlants"
  @hot-click="handleHotClick"
/>
```

---

## ai-chat/ 子目录

AiChatCard.vue 的 5 个子组件，各自负责 AI 对话界面的一个功能模块。

```
AiChatCard.vue（父组件，display/ 目录）
├── WelcomePanel.vue     ← 欢迎面板：首次进入时的引导界面
├── ChatMessageList.vue  ← 消息列表：对话消息的渲染与滚动
├── ChatInputArea.vue    ← 输入区域：用户输入与发送/停止控制
├── SessionDrawer.vue    ← 会话抽屉：历史会话列表管理
└── SessionBadge.vue     ← 会话徽章：当前会话状态标识
```

### WelcomePanel.vue

**用途：** 用户首次进入 AI 聊天或开始新对话时，展示欢迎消息和快捷提问标签

**Props：** `quickQuestions` (Array, default: []) -- 快捷问题数组

**Events：** `send-question` -- 点击快捷标签触发

### ChatMessageList.vue

**用途：** 渲染用户与 AI 的对话消息，支持 Markdown 格式渲染和流式输出光标动画

**Props：**
- `messages` (Array, default: []) -- 消息列表，每项 `{ role, content, streaming? }`
- `streaming` (Boolean, default: false) -- 是否正在流式输出

**Expose：** `scrollToBottom()` -- 滚动到底部

**核心算法：**
- Markdown 渲染管线：`marked.parse(text)` -> `DOMPurify.sanitize(html, DOMPURIFY_CONFIG)`
- DOMPURIFY_CONFIG 严格白名单：允许 p/br/strong/em/code/pre/ul/ol/li/h1-h6/a/table 等标签，禁止 script/iframe/form 等标签和 onerror/onclick 等事件属性
- 双重 watch 自动滚动：监听 `messages.length` 和 `messages`(deep) 变化
- 流式光标：`msg.streaming === true` 时追加闪烁光标 `▌`（0.8s 周期 blink 动画）

### ChatInputArea.vue

**用途：** 提供消息输入框和发送/停止按钮

**Props：**
- `modelValue` (String, default: '') -- 输入内容（v-model）
- `disabled` (Boolean, default: false) -- 是否禁用
- `streaming` (Boolean, default: false) -- 是否正在流式输出

**Events：** `update:modelValue`、`send`、`stop`

**核心逻辑：** 流式输出期间发送按钮切换为红色"停止"按钮；Enter 键触发发送

### SessionDrawer.vue

**用途：** 以侧边抽屉形式展示用户的历史会话列表

**Props：**
- `visible` (Boolean) -- 抽屉显示状态
- `sessions` (Array) -- 会话列表
- `sessionsLoading` (Boolean) -- 加载状态
- `currentSessionId` (String) -- 当前会话 ID
- `formatSessionTime` (Function, required) -- 时间格式化函数

**Events：** `open`、`close`、`select`、`delete`、`new-chat`

**设计：** 从左侧滑出（`direction="ltr"`），宽度 300px，删除按钮 hover 时显示

### SessionBadge.vue

**用途：** 在聊天头部显示当前会话状态标识

**Events：** `new-chat` -- 点击"新对话"按钮

**设计：** 青绿-靛蓝渐变背景圆角徽章，含"继续对话"文字和"新对话"按钮

> **完整的 ai-chat 子组件文档**（包含交互流程图、WebSocket 生命周期、依赖关系图等）见 [ai-chat/README.md](./ai-chat/README.md)。

---

## 常见错误

### 错误1：在展示组件里发请求

```vue
<script setup>
// ❌ 展示组件不应该自己发请求，数据应该由父组件传入
import { ref } from 'vue'
import { getPlants } from '@/api/plant'
const plants = ref([])
getPlants().then(res => { plants.value = res.data })

// ✅ 展示组件只负责展示，数据通过 props 传入
const props = defineProps({
  items: { type: Array, default: () => [] }
})
</script>
```

### 错误2：分页器忘记处理边界

```vue
<script setup>
// ❌ 没有处理第一页点"上一页"和最后一页点"下一页"的情况
const prevPage = () => { currentPage.value-- }  // 第一页时变成0或负数！

// ✅ Pagination 组件已封装 el-pagination，自动处理边界
// 只需监听 change 事件即可
const handlePageChange = ({ page, size }) => {
  fetchData({ page, size })
}
</script>
```

### 错误3：ChartCard 传入完整的 echarts 配置但忘记按需引入图表类型

```vue
<script setup>
// ❌ 使用了雷达图但 ChartCard 只引入了 BarChart/LineChart/PieChart/RadarChart
// 如果需要其他图表类型，需要在 ChartCard.vue 中补充引入

// ✅ ChartCard 已按需引入 BarChart、LineChart、PieChart、RadarChart
// 如需其他类型（如 ScatterChart），需在 ChartCard.vue 的 echarts.use 中添加
</script>
```

---

## 代码审查与改进建议

- [安全] `ChatMessageList.vue`（ai-chat/子目录）使用 `v-html` 渲染 AI 返回内容，虽然已使用 `DOMPurify.sanitize()` 过滤，但 `marked` 配置未禁用 HTML 标签解析，建议设置 `{ breaks: true, gfm: true, html: false }` 从源头减少攻击面
- [安全] `ChatMessageList.vue` 聊天消息无长度限制，超长消息可能导致 DOM 渲染卡顿或 WebSocket 传输问题
- [性能] `CardGrid.vue` 的 `getImage` 函数每次渲染都执行 `JSON.parse`，应使用 computed 缓存解析结果
- [性能] `KnowledgeGraph.vue` 的 `buildGraphData` 在每次渲染时重建全部节点和边，数据量大时应考虑增量更新
- [体验] `AiChatCard.vue` 的快捷问题硬编码在组件中，建议改为从后端配置或根据用户浏览历史动态生成
