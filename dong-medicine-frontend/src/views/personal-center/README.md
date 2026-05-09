# 个人中心子页面（personal-center Components）

## 什么是个人中心子页面？

类比：**个人档案室**——进门是个人名片墙（资料区），旁边有五个档案柜：学习统计、我的收藏、答题记录、浏览历史、账号设置。每个档案柜存放不同类型的个人数据，用户按需切换查看。

本目录包含 `PersonalCenter.vue` 页面的 6 个子组件，它们以 Tab 面板的形式组织在个人中心页面中。`PersonalCenter.vue` 作为父页面组件位于上级 `views/` 目录，负责组合这些子组件、管理数据加载和 Tab 切换。

```
PersonalCenter.vue（父页面，views/ 目录）
├── ProfileSection.vue      ← 个人资料卡片：头像、姓名、角色、统计摘要、快捷操作
├── StatsDashboard.vue      ← 学习统计面板：五项统计卡片 + 成绩趋势折线图
├── FavoritesPanel.vue      ← 我的收藏面板：按类型筛选的收藏列表
├── QuizHistoryPanel.vue    ← 答题记录面板：答题和游戏记录合并展示
├── BrowseHistoryPanel.vue  ← 浏览历史面板：浏览记录按类型筛选
└── SettingsPanel.vue       ← 账号设置面板：基本信息、修改密码、退出登录
```

---

## 组件列表

### ProfileSection -- 个人资料卡片

**用途：** 展示用户头像、姓名、角色和核心学习统计数据，提供快捷操作入口

```
┌────────────────────────────────────┐
│  ┌────┐                            │
│  │ 头 │  侗乡医药用户              │
│  │ 像 │  [普通用户]                 │
│  └────┘                            │
│  ─────────────────────────────     │
│  12次    85分    5次    8个    36次  │
│  答题   平均分  游戏   收藏   浏览  │
│                                    │
│  ┌──────────┐  ┌──────────┐       │
│  │ 📊 学习统计│  │ ⭐ 我的收藏│       │
│  ├──────────┤  ├──────────┤       │
│  │ ✏️ 答题记录│  │ ⚙️ 账号设置│       │
│  └──────────┘  └──────────┘       │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<ProfileSection
  :user-name="'侗乡医药用户'"
  :is-admin="false"
  :study-stats="studyStats"
  :actions="actions"
  @tab-change="activeTab = $event"
/>
```

**核心逻辑：**

- **头像生成：** 使用用户名首字作为头像文字（`userName?.charAt(0) || '用'`），靛蓝渐变背景
- **统计摘要：** `studyStats` 数组由 `useStudyStats` composable 计算得出，包含 5 项指标（总答题次数、平均得分、植物游戏次数、收藏总数、浏览总数），以 `flex` 均匀分布展示
- **快捷操作：** `actions` 数组定义 4 个入口（学习统计、我的收藏、答题记录、账号设置），点击触发 `tab-change` 事件切换到对应 Tab

**响应式设计：** 768px 以下头像区域改为纵向居中布局，快捷操作改为单列；480px 以下统计块进一步缩小。

---

### StatsDashboard -- 学习统计面板

**用途：** 以数据卡片和折线图展示用户的学习数据概览和成绩趋势

```
┌────────────────────────────────────────────┐
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ │
│  │ 12次 │ │ 85分 │ │ 5次  │ │ 8个  │ │ 36次 │ │
│  │ 答题 │ │均分  │ │ 游戏 │ │ 收藏 │ │ 浏览 │ │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘ │
│                                              │
│  📈 成绩趋势                                 │
│  ┌──────────────────────────────────────┐    │
│  │  100│     ●                          │    │
│  │   80│   ● ─ ─ ●                      │    │
│  │   60│ ●         ◆                    │    │
│  │   40│              ◆                 │    │
│  │    0└──────────────────────          │    │
│  │      04/01  04/05  04/10  04/15      │    │
│  │      ── 趣味答题  ── 植物识别         │    │
│  └──────────────────────────────────────┘    │
└────────────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<StatsDashboard
  :quiz-records="quizRecords"
  :game-records="gameRecords"
  :favorites="favorites"
  :browse-history="browseHistory"
  :loading="loading"
/>
```

**核心算法实现：**

1. **统计指标计算（`computeStudyStats`）**

   ```javascript
   // 来自 useStudyStats composable
   function computeStudyStats(quizRecords, gameRecords, favorites, browseHistory) {
     const totalAttempts = quizRecords.length + gameRecords.length
     let totalScore = 0, scoreCount = 0
     // 遍历答题和游戏记录，累加分数
     quizRecords.forEach(r => { totalScore += Number(r.score); scoreCount++ })
     gameRecords.forEach(r => { totalScore += Number(r.score); scoreCount++ })
     const avgScore = scoreCount > 0 ? totalScore / scoreCount : 0
     // 返回五项统计
   }
   ```

   五项统计卡片使用不同的渐变色区分：红色（答题）、靛蓝（均分）、青绿（游戏）、金铜（收藏）、紫色（浏览）。

2. **成绩趋势图（ECharts 折线图）**

   ```javascript
   // 数据构建：按日期聚合，同一天多次答题取平均分
   function buildScoreTrendData(quizRecords, gameRecords) {
     // 1. 将答题和游戏记录统一为 { date, score, type } 格式
     // 2. 按日期分组，同类型同天的分数取平均值
     // 3. 返回 { dates, quizScores, gameScores } 供 ECharts 渲染
   }
   ```

   折线图特性：
   - 双系列：趣味答题（蓝色 `#3498db`）和植物识别（青绿 `#28B463`）
   - 平滑曲线（`smooth: true`）+ 面积渐变填充
   - 自动连接空值（`connectNulls: true`），某天无某类记录时不中断折线
   - 自定义 tooltip 格式化，显示日期和分数
   - ResizeObserver 监听容器尺寸变化，自动调整图表大小

3. **图表生命周期管理**

   ```javascript
   onMounted(updateStats)           // 初始化计算和渲染
   onUnmounted(disposeChart)        // 销毁 ECharts 实例和 ResizeObserver
   watch([...data], updateStats, { deep: true })  // 数据变化时重新计算
   ```

**技术选型依据：** ECharts 采用按需导入模式（`echarts/core`），仅注册 `LineChart`、`GridComponent`、`TooltipComponent`、`LegendComponent`、`GraphicComponent`、`CanvasRenderer`，相比全量导入减少约 60% 的包体积。

---

### FavoritesPanel -- 我的收藏面板

**用途：** 展示用户收藏的药用植物、知识、传承人、学习资源、问答，支持按类型筛选和分页

```
┌────────────────────────────────────┐
│  [全部] [药材] [知识] [传承人] [资源] [问答] │
│                                    │
│  ┌──────────────────────────────┐  │
│  │ 🖼 钩藤                      │  │
│  │ 息风止痉、清热平肝... [药材]  │  │
│  ├──────────────────────────────┤  │
│  │ 📄 侗族药浴疗法               │  │
│  │ 传统外治疗法...     [知识]    │  │
│  ├──────────────────────────────┤  │
│  │ 👤 杨氏传承人                 │  │
│  │ 擅长药浴疗法...     [传承人]  │  │
│  └──────────────────────────────┘  │
│                                    │
│  ← 1  2  3 →                      │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<FavoritesPanel
  :favorites="favorites"
  @go-detail="goToDetail"
/>
```

**核心逻辑：**

- **类型筛选：** 使用 `el-radio-group` + `el-radio-button` 实现 6 个筛选选项（全部/药材/知识/传承人/资源/问答），`filtered` 计算属性根据 `typeFilter` 过滤
- **类型映射：** 每种类型对应不同的图标、标签颜色和名称

  | 类型 | 图标 | 标签颜色 | 名称 |
  |------|------|----------|------|
  | plant | Picture | success | 药材 |
  | knowledge | Document | primary | 知识 |
  | inheritor | User | warning | 传承人 |
  | resource | Folder | info | 资源 |
  | qa | ChatDotRound | default | 问答 |

- **分页：** 前端分页（`paginated` 计算属性），每页 6 条，使用 `Pagination` 组件
- **点击跳转：** 点击收藏项触发 `go-detail` 事件，父组件根据 `item.type` 路由到对应详情页

---

### QuizHistoryPanel -- 答题记录面板

**用途：** 合并展示趣味答题和植物识别游戏的记录，按分数等级着色

```
┌────────────────────────────────────┐
│  ┌────┐                            │
│  │ 85 │  趣味答题                   │
│  │ 分 │  2025-04-20  正确 8/10     │
│  └────┘                            │
│  ┌────┐                            │
│  │ 60 │  植物识别 · 中等            │
│  │ 分 │  2025-04-18  正确 6/10     │
│  └────┘                            │
│  ┌────┐                            │
│  │ 45 │  植物识别 · 困难            │
│  │ 分 │  2025-04-15  正确 3/8      │
│  └────┘                            │
│                                    │
│  ← 1  2 →                         │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<QuizHistoryPanel
  :quiz-records="quizRecords"
  :game-records="gameRecords"
/>
```

**核心逻辑：**

- **记录合并：** 将答题记录（`type: 'quiz'`）和游戏记录（`type: 'game'`）合并为统一列表，按时间倒序排列

  ```javascript
  const allRecords = computed(() => {
    const quiz = props.quizRecords.map(r => ({ ...r, type: 'quiz' }))
    const game = props.gameRecords.map(r => ({ ...r, type: 'game' }))
    return [...quiz, ...game].sort((a, b) =>
      new Date(b.createdAt || b.createTime) - new Date(a.createdAt || a.createTime)
    )
  })
  ```

- **分数等级着色：** 圆形分数徽标根据分数段使用不同渐变色

  | 分数范围 | 样式类 | 渐变色 |
  |----------|--------|--------|
  | >= 80 | `score-high` | 青绿渐变（`--dong-jade`） |
  | >= 60 | `score-medium` | 金铜渐变（`--dong-gold`） |
  | < 60 | `score-low` | 蓝紫渐变 |

- **难度名称映射：** `{ easy: '简单', medium: '中等', hard: '困难' }`
- **分页：** 前端分页，每页 6 条

---

### BrowseHistoryPanel -- 浏览历史面板

**用途：** 展示用户的浏览记录，支持按内容类型筛选和分页，点击可跳转到对应详情

```
┌────────────────────────────────────┐
│  [全部] [药材] [知识] [传承人] [资源] [问答] │
│                                    │
│  ┌──────────────────────────────┐  │
│  │ 🌿 钩藤                      │  │
│  │ 息风止痉... 🕐 2小时前 [药材] →│  │
│  ├──────────────────────────────┤  │
│  │ 📄 侗族药浴疗法               │  │
│  │ 传统外治... 🕐 1天前  [知识] →│  │
│  └──────────────────────────────┘  │
│                                    │
│  ← 1  2 →                         │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<!-- 无需 props，组件内部通过 useBrowseHistory composable 自行加载数据 -->
<BrowseHistoryPanel />
```

**核心逻辑：**

- **数据自加载：** 与其他面板不同，`BrowseHistoryPanel` 内部直接使用 `useBrowseHistory` composable 加载数据（`onMounted(loadBrowseHistory)`），而非通过 props 接收。这是因为浏览历史数据仅在本面板使用，无需由父组件统一管理。
- **类型筛选：** 与 `FavoritesPanel` 相同的 6 选项筛选器，使用 `usePersonalCenter` 导出的 `typeTagMap` 和 `typeNameMap`
- **跳转逻辑：** `goToHistoryItem()` 方法来自 `useBrowseHistory` composable，根据 `targetType` 路由到对应页面并携带 `id` 参数
- **时间格式化：** 使用 `new Date(time).toLocaleString('zh-CN')` 格式化浏览时间

---

### SettingsPanel -- 账号设置面板

**用途：** 提供基本信息查看、密码修改（含验证码）和退出登录功能

```
┌────────────────────────────────────┐
│  👤 基本信息                        │
│  用户名：[dong_user]  (不可修改)    │
│  角色：  [普通用户]    (不可修改)    │
├────────────────────────────────────┤
│  🔒 修改密码                        │
│  当前密码：[••••••••]              │
│  新密码：  [••••••••]              │
│  确认密码：[••••••••]              │
│  验证码：  [A3K2] 🔄               │
│  [确认修改]  [重置]                 │
├────────────────────────────────────┤
│  🚪 退出登录                        │
│  点击下方按钮将退出当前账号...      │
│  [退出登录]                         │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<SettingsPanel
  ref="settingsRef"
  :user-name="userName"
  :is-admin="isAdmin"
  :password-form="passwordForm"
  :password-rules="passwordRules"
  :password-loading="passwordLoading"
  :logout-loading="logoutLoading"
  @change-password="handleChangePassword"
  @reset-password="resetPasswordForm"
  @logout="handleLogout"
  @update:captcha-key="passwordForm.captchaKey = $event"
/>
```

**核心逻辑：**

1. **三段式卡片布局**

   | 卡片 | 图标 | 功能 |
   |------|------|------|
   | 基本信息 | User | 只读展示用户名和角色 |
   | 修改密码 | Lock | 表单验证 + 验证码 + API 提交 |
   | 退出登录 | SwitchButton | 确认对话框 + 退出 |

2. **密码修改流程**

   ```javascript
   // 父组件 PersonalCenter.vue 中的处理逻辑（来自 usePersonalCenter composable）
   handleChangePassword:
     1. 表单验证（passwordFormRef.validate()）
     2. 发送 POST /user/change-password 请求
     3. 成功：清空认证状态，跳转首页
     4. 失败：显示错误消息，刷新验证码
   ```

   密码验证规则来自 `createPasswordValidator()` 工厂函数，确认密码校验支持动态引用表单密码字段。

3. **验证码集成**

   使用 `CaptchaInput` 组件（来自 `interact/` 目录），支持 `captchaKey` / `captchaCode` 双向绑定。`captchaKey` 通过 `update:captcha-key` 事件同步到父组件的 `passwordForm`。

4. **表单引用暴露**

   ```javascript
   defineExpose({ formRef, captchaRef })
   ```

   通过 `defineExpose` 暴露 `formRef` 和 `captchaRef`，父组件在 `onMounted` 中通过 `settingsRef.value.formRef` 获取表单引用，用于触发验证和重置。

5. **计算属性双向绑定**

   ```javascript
   // 使用 computed 的 get/set 实现 props 的双向绑定
   const currentPassword = computed({
     get: () => props.passwordForm.currentPassword,
     set: (v) => { props.passwordForm.currentPassword = v }
   })
   ```

   由于 `passwordForm` 由父组件管理（`usePersonalCenter` composable），子组件通过 computed get/set 间接修改父组件状态。代码中标注了 `eslint-disable vue/no-mutating-props` 以抑制 lint 警告。

---

## 文件间的依赖关系与交互流程

### 组件层级关系

```
PersonalCenter.vue（父页面）
│
├── usePersonalCenter() composable
│   ├── isLoggedIn, userName, isAdmin  → ProfileSection
│   ├── favorites                      → FavoritesPanel, StatsDashboard
│   ├── quizRecords, gameRecords       → QuizHistoryPanel, StatsDashboard
│   ├── passwordForm, passwordRules    → SettingsPanel
│   └── goToDetail, handleChangePassword, handleLogout → 各面板事件处理
│
├── useStudyStats() composable
│   ├── studyStats                     → ProfileSection
│   ├── initScoreChart                 → StatsDashboard
│   └── computeStudyStats              → StatsDashboard
│
├── useBrowseHistory() composable
│   ├── browseHistory                  → StatsDashboard（统计指标）
│   └── loadBrowseHistory              → BrowseHistoryPanel（自加载）
│
└── 子组件
    ├── ProfileSection      ← 左侧固定，始终显示
    ├── StatsDashboard      ← Tab: stats
    ├── FavoritesPanel      ← Tab: favorites
    ├── QuizHistoryPanel    ← Tab: quiz
    ├── BrowseHistoryPanel  ← Tab: history（自加载数据）
    └── SettingsPanel       ← Tab: settings
```

### 页面布局结构

```
┌──────────────────────────────────────────────────┐
│  个人中心                                         │
│  我的信息 · 收藏管理 · 学习记录                    │
├──────────────┬───────────────────────────────────┤
│              │  [学习统计] [我的收藏] [答题记录]   │
│  Profile     │  [浏览历史] [修改密码]              │
│  Section     │───────────────────────────────────│
│  (320px固定) │                                   │
│              │  当前 Tab 面板内容                  │
│              │                                   │
│              │                                   │
└──────────────┴───────────────────────────────────┘
```

1024px 以下改为单列布局，ProfileSection 在上，Tab 内容在下。

### 数据加载流程

```
1. 页面挂载
   → usePersonalCenter.onMounted → fetchUserData()
   → 并行请求 4 个接口：/favorites/my, /quiz/records, /plant-game/records, /comments/my
   → useBrowseHistory.onMounted → loadBrowseHistory() → /browse-history/my
   → useStudyStats.computeStudyStats() 计算统计指标
   → useStudyStats.initScoreChart() 渲染折线图

2. 切换到"学习统计"Tab
   → 重新加载答题和游戏记录（数据可能已更新）
   → 重新加载浏览历史
   → 重新计算统计指标和渲染图表

3. 修改密码
   → SettingsPanel 表单验证
   → POST /user/change-password
   → 成功：清空认证 → 跳转首页
   → 失败：刷新验证码

4. 退出登录
   → ElMessageBox.confirm 确认
   → userStore.logout()
   → 跳转首页
```

### 共享依赖

| 依赖 | 使用者 | 用途 |
|------|--------|------|
| `usePersonalCenter` | PersonalCenter.vue | 核心状态管理（用户信息、收藏、记录、密码表单） |
| `useStudyStats` | PersonalCenter.vue, StatsDashboard | 统计计算和图表渲染 |
| `useBrowseHistory` | PersonalCenter.vue, BrowseHistoryPanel | 浏览历史数据加载和跳转 |
| `useUserStore` | usePersonalCenter | 用户登录状态和认证操作 |
| `Pagination` | FavoritesPanel, QuizHistoryPanel, BrowseHistoryPanel | 分页控件 |
| `CaptchaInput` | SettingsPanel | 验证码输入 |
| `typeTagMap` / `typeNameMap` | FavoritesPanel, BrowseHistoryPanel | 类型标签映射（来自 usePersonalCenter） |

---

## 代码审查与改进建议

- [设计] `SettingsPanel.vue` 使用 computed get/set 直接修改 props 对象属性（`props.passwordForm.currentPassword = v`），属于 Vue 反模式，建议改为 emit 事件或使用 `defineModel`
- [性能] `StatsDashboard.vue` 的 `watch` 使用 `deep: true` 监听四个数组，任何数据变化都触发重新计算和图表重绘，建议增加防抖或比较关键字段变化
- [体验] `FavoritesPanel.vue` 和 `BrowseHistoryPanel.vue` 的类型筛选逻辑高度相似（`typeFilter` + `filtered` + `paginated`），可提取为通用 composable
- [体验] `QuizHistoryPanel.vue` 缺少按时间范围筛选功能，记录较多时用户难以查找特定时期的答题记录
- [安全] `SettingsPanel.vue` 的密码修改成功后直接调用 `userStore.clearAuth()` 清空认证，但未清除 localStorage 中的 token，建议统一使用 `userStore.logout()`
- [健壮性] `BrowseHistoryPanel.vue` 是唯一自加载数据的面板，与其他面板的数据流模式不一致，建议统一由父组件管理数据加载
