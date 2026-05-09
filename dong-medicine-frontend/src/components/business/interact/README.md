# 互动组件（Interact Components）

## 什么是互动组件？

类比：**互动游戏区**——博物馆里不只是看展品，还有体验区让你动手操作、参与互动。

互动组件让用户不只是"看"，还能"做"——发表评论、回答问题、玩识别游戏、输入验证码。它们是平台中最有趣的部分，能大幅提升用户参与度。

```
展示组件：用户看 → "哦，知道了"
互动组件：用户做 → "哇，真有趣！" ← 参与感更强
```

---

## 文件结构

```
interact/
├── CaptchaInput.vue     # 验证码输入组件
├── PlantGame.vue        # 药用植物识别游戏
├── CommentSection.vue   # 评论区
├── InteractSidebar.vue  # 互动侧边栏（成绩统计 + 排行榜）
├── QuizSection.vue      # 侗医药知识答题
├── index.js             # 统一导出
└── README.md            # 本文档
```

> **注意**：`index.js` 当前仅导出 `CommentSection`、`PlantGame`、`QuizSection`、`InteractSidebar` 四个组件，`CaptchaInput` 未在 index.js 中统一导出，需在使用处单独引入。

---

## 组件列表

### CommentSection —— 评论区

**用途：** 让用户对药用植物、知识条目等内容发表评论和回复，支持主评论与嵌套回复

```
┌────────────────────────────────────┐
│  [头像] 分享你对侗乡医药的看法...    │
│                          0/500 [发布]│
│                                    │
│  排序方式：[最新] [最早]            │
│                                    │
│  ┌──────────────────────────────┐  │
│  │ 用户A  2025-04-20            │  │
│  │ 钩藤泡茶效果不错！            │  │
│  │                     [回复]   │  │
│  │  ┌────────────────────────┐  │  │
│  │  │ 用户B  回复 @用户A     │  │  │
│  │  │ 请问怎么泡？           │  │  │
│  │  │               [回复]   │  │  │
│  │  └────────────────────────┘  │  │
│  └──────────────────────────────┘  │
│                                    │
│  ← 上一页  1  2  下一页 →          │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| comments | Array | `[]` | 评论数据列表，每项含 id、username、content、createTime、replyToId 等字段 |
| isLoggedIn | Boolean | - | 用户是否已登录 |
| userName | String | - | 当前用户名，用于显示头像首字 |
| loading | Boolean | - | 评论加载状态 |
| total | Number | `0` | 评论总数（用于分页） |
| page | Number | `1` | 当前页码 |
| size | Number | `6` | 每页条数 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| post | `(content, replyData, onSuccess, onError)` | 发布评论/回复，replyData 为 `{replyToId, replyToUserId, replyToName}` 或 null |
| reply | - | 内部由 post 事件统一处理 |
| page-change | `(page)` | 页码变化 |
| size-change | `(size)` | 每页条数变化 |

#### 核心逻辑

- **主评论/回复分离**：通过 `replyToId` 字段区分主评论和回复，`mainComments` 过滤出 `!replyToId` 的评论，`getReplies(commentId)` 获取某条评论的回复
- **排序**：支持"最新"和"最早"两种排序，按 `createTime` 排序
- **分页**：对主评论进行前端分页（`paginatedMainComments`），回复不在分页范围内
- **时间格式化**：`formatTime` 实现"刚刚/X分钟前/X小时前/日期"的智能显示
- **回复机制**：点击回复设置 `replyTo` 对象，输入框 placeholder 变为"回复 @xxx"，发布时携带回复信息

```vue
<!-- 使用示例 -->
<CommentSection
  :comments="commentList"
  :is-logged-in="isLoggedIn"
  :user-name="currentUser"
  :loading="loading"
  :total="total"
  :page="currentPage"
  :size="pageSize"
  @post="handlePost"
  @page-change="handlePageChange"
  @size-change="handleSizeChange"
/>
```

---

### QuizSection —— 侗医药知识答题

**用途：** 展示侗医药知识测验题目，支持难度选择、限时答题、成绩评定

```
┌────────────────────────────────────┐
│  📝 侗医药知识趣味答题              │
│                                    │
│  选择难度：                         │
│  ┌────────┐ ┌────────┐ ┌────────┐ │
│  │ 📝 初级 │ │ 💡 中级 │ │ 🏆 高级 │ │
│  │  10题   │ │  20题   │ │  30题   │ │
│  │ 10分/题 │ │ 15分/题 │ │ 20分/题 │ │
│  └────────┘ └────────┘ └────────┘ │
│                                    │
│         [开始答题]                  │
└────────────────────────────────────┘

        ↓ 开始后 ↓

┌────────────────────────────────────┐
│  第 3 / 10 题          ⏱ 02:30    │
│  ━━━━━━━━━━━●━━━━━━━━━            │
│                                    │
│  问题：钩藤的主要功效是什么？       │
│                                    │
│  ○ A. 清热解毒                     │
│  ● B. 息风止痉  ← 你选了这个       │
│  ○ C. 活血化瘀                     │
│  ○ D. 补气养阴                     │
│                                    │
│  [← 上一题]  [下一题 →]  [提前交卷] │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| isStarted | Boolean | - | 是否已开始答题 |
| finished | Boolean | - | 是否已结束 |
| loading | Boolean | - | 题目加载状态 |
| submitting | Boolean | - | 提交中状态 |
| questions | Array | `[]` | 题目列表，每项含 q（问题）和 options（选项数组） |
| current | Number | `0` | 当前题目索引 |
| answers | Array | `[]` | 用户答案数组 |
| score | Number | `0` | 最终得分 |
| correct | Number | `0` | 正确题数 |
| formattedTime | String | `"03:00"` | 格式化倒计时文本 |
| isLowTime | Boolean | - | 是否剩余时间不足（触发闪烁） |
| selectedDifficulty | String | `'easy'` | 当前选中难度 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| start | - | 开始答题 |
| prev | - | 上一题 |
| next | - | 下一题 |
| submit | - | 提交答卷/提前交卷 |
| retry | - | 重新选择难度 |
| share | - | 分享成绩 |
| selectDifficulty | `(level)` | 选择难度，level 为 `'easy'`/`'medium'`/`'hard'` |

#### 核心逻辑

- **难度体系**：初级（10题 x 10分）、中级（20题 x 15分）、高级（30题 x 20分）
- **成绩评定**：`getScoreLevel` 根据分数划分等级 —— 优秀(>=90)、良好(>=70)、及格(>=60)、不及格(<60)，对应不同 emoji 和评语
- **倒计时闪烁**：`isLowTime` 为 true 时，倒计时区域变红并添加闪烁动画
- **进度条**：根据 `(current + 1) / questions.length` 计算进度条宽度

```vue
<!-- 使用示例 -->
<QuizSection
  :is-started="isStarted"
  :finished="finished"
  :loading="loading"
  :submitting="submitting"
  :questions="questionList"
  :current="currentIndex"
  :answers="userAnswers"
  :score="finalScore"
  :correct="correctCount"
  :formatted-time="timeDisplay"
  :is-low-time="isLowTime"
  :selected-difficulty="difficulty"
  @start="startQuiz"
  @prev="prevQuestion"
  @next="nextQuestion"
  @submit="submitQuiz"
  @retry="retryQuiz"
  @share="shareResult"
  @select-difficulty="changeDifficulty"
/>
```

---

### PlantGame —— 药用植物识别游戏

**用途：** 通过看图识植物的游戏方式帮助用户学习识别侗族药用植物

```
┌────────────────────────────────────┐
│  🌿 药用植物识别游戏                │
│                                    │
│  选择难度：                         │
│  ┌────────┐ ┌────────┐ ┌────────┐ │
│  │ 🌱 初级 │ │ 🌿 中级 │ │ 🌳 高级 │ │
│  │ 3个选项 │ │ 4个选项 │ │ 5个选项 │ │
│  │ 10分/题 │ │ 15分/题 │ │ 20分/题 │ │
│  └────────┘ └────────┘ └────────┘ │
│                                    │
│         [开始游戏]                  │
└────────────────────────────────────┘

        ↓ 游戏中 ↓

┌────────────────────────────────────┐
│  当前得分: 80    ⏱ 01:45   连击 3  │
│                                    │
│  ┌──────────────────────────┐      │
│  │                          │      │
│  │    [显示一张植物图片]      │      │
│  │                          │      │
│  └──────────────────────────┘      │
│                                    │
│  这是什么植物？                     │
│  [钩藤]  [透骨草]                   │
│  [九节茶] [半夏]                    │
│                                    │
│  [结束游戏]  [收藏植物]             │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| currentPlant | Object | - | 当前展示的植物对象，需含 nameCn 和 images 字段 |
| options | Array | `[]` | 选项列表（植物中文名数组） |
| score | Number | `0` | 当前得分 |
| streak | Number | `0` | 当前连击数 |
| answered | Boolean | - | 是否已作答 |
| selectedAnswer | String | - | 用户选择的答案 |
| finished | Boolean | - | 游戏是否结束 |
| gameStarted | Boolean | - | 游戏是否已开始 |
| submitting | Boolean | - | 提交中状态 |
| plantsLoaded | Boolean | - | 植物数据是否加载完成（控制开始按钮可用性） |
| correct | Number | `0` | 答对题数 |
| total | Number | `0` | 总题数 |
| isLoggedIn | Boolean | - | 是否已登录（控制收藏按钮显示） |
| selectedDifficulty | String | - | 当前难度 |
| formattedTime | String | `"03:00"` | 格式化倒计时 |
| isLowTime | Boolean | - | 是否时间不足 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| selectDifficulty | `(level)` | 选择难度 |
| start | - | 开始游戏 |
| answer | `(option)` | 选择答案 |
| endGame | - | 结束游戏 |
| favorite | - | 收藏当前植物 |
| restart | - | 重新选择难度 |

#### 核心逻辑

- **难度体系**：初级（3选项/10分）、中级（4选项/15分）、高级（5选项/20分）
- **图片展示**：使用 `getFirstImage` 工具函数从 `currentPlant.images` 中提取第一张图片
- **答案反馈**：`answered` 为 true 时，正确选项变绿（`.correct`），错误选项变红（`.wrong`）
- **连击系统**：`streak > 0` 时显示连击徽章
- **三阶段界面**：游戏介绍 → 游戏进行 → 游戏结果，通过 `gameStarted` 和 `finished` 控制显示

```vue
<!-- 使用示例 -->
<PlantGame
  :current-plant="currentPlant"
  :options="gameOptions"
  :score="gameScore"
  :streak="streakCount"
  :answered="hasAnswered"
  :selected-answer="userAnswer"
  :finished="gameFinished"
  :game-started="gameStarted"
  :submitting="submitting"
  :plants-loaded="plantsLoaded"
  :correct="correctCount"
  :total="totalQuestions"
  :is-logged-in="isLoggedIn"
  :selected-difficulty="difficulty"
  :formatted-time="timeDisplay"
  :is-low-time="isLowTime"
  @select-difficulty="changeDifficulty"
  @start="startGame"
  @answer="checkAnswer"
  @end-game="endGame"
  @favorite="favoritePlant"
  @restart="restartGame"
/>
```

---

### CaptchaInput —— 验证码输入

**用途：** 在注册登录等场景中验证用户身份，防止机器人刷屏。组件自动从后端获取图形验证码，用户输入后由后端校验

```
┌────────────────────────────────────┐
│  🔑 [请输入验证码____]  [A x K 2] │
│                          ↑ 点击刷新 │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | String | `''` | 双向绑定的验证码输入值 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | `(value)` | 验证码输入值更新 |
| update:captchaKey | `(key)` | 验证码 key 更新（后端返回，用于提交时校验） |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| refreshCaptcha() | 刷新验证码（清空输入并重新获取） |

#### 核心逻辑

- **验证码获取**：组件挂载时自动调用 `GET /captcha` 获取验证码图片和 key
- **输入过滤**：`handleInput` 只允许输入数字（`value.replace(/\D/g, '')`），最大长度4位
- **点击刷新**：点击验证码图片触发 `refreshCaptcha`，清空输入并重新获取
- **双值绑定**：同时向父组件传递用户输入值（`modelValue`）和验证码 key（`captchaKey`），提交时两者一起发送到后端校验

```vue
<!-- 使用示例 -->
<CaptchaInput
  v-model="captchaCode"
  @update:captcha-key="captchaKey = $event"
/>

<!-- 验证码校验由后端完成，前端只负责收集输入 -->
<script setup>
const submitLogin = async () => {
  await api.login({
    username: username.value,
    password: password.value,
    captchaCode: captchaCode.value,
    captchaKey: captchaKey.value
  })
}
</script>
```

---

### InteractSidebar —— 互动侧边栏

**用途：** 在互动页面侧边展示用户成绩统计和排行榜，支持综合/答题/游戏三种排行维度

```
┌──────────────────────┐
│  🏆 我的成绩          │
│  ┌────────┬────────┐ │
│  │   12   │   85   │ │
│  │ 答题次数│ 最高分  │ │
│  ├────────┼────────┤ │
│  │    8   │  120   │ │
│  │ 游戏次数│ 游戏总分│ │
│  └────────┴────────┘ │
│                      │
│  🏅 成绩排行榜        │
│  [综合] [答题] [游戏] │
│                      │
│  1  用户A    180分    │
│  2  用户B    150分    │
│  3  用户C    120分    │
│  4  用户D    100分    │
│                      │
│  综合 = 答题最高分    │
│       + 游戏最高分    │
└──────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| quizCount | Number | `0` | 答题次数 |
| bestScore | Number | `0` | 答题最高分 |
| gameCount | Number | `0` | 游戏次数 |
| totalGameScore | Number | `0` | 游戏总分 |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| refreshLeaderboard() | 清空缓存并重新加载排行榜数据 |

#### 核心逻辑

- **排行榜数据加载**：组件挂载时自动加载综合排行榜，切换排序维度时按需加载
- **三种排行维度**：
  - 综合：`GET /leaderboard/combined?sortBy=total`（答题最高分 + 游戏最高分）
  - 答题：`GET /leaderboard/quiz`
  - 游戏：`GET /leaderboard/game`
- **缓存机制**：已加载的排行数据缓存在 `rankData` 中，切换维度时不重复请求
- **排名样式**：前三名分别显示金/银/铜色徽章（`.gold`/`.silver`/`.bronze`）

```vue
<!-- 使用示例 -->
<InteractSidebar
  :quiz-count="userStats.quizCount"
  :best-score="userStats.bestScore"
  :game-count="userStats.gameCount"
  :total-game-score="userStats.totalGameScore"
/>

<!-- 刷新排行榜 -->
<script setup>
const sidebarRef = ref(null)
const refreshRank = () => {
  sidebarRef.value?.refreshLeaderboard()
}
</script>
```

---

## 依赖关系

```
CommentSection.vue
  ├── @element-plus/icons-vue (ChatDotRound)
  ├── element-plus (ElMessage)
  └── @/components/business/display/Pagination.vue

QuizSection.vue
  └── @element-plus/icons-vue (Clock, Star, VideoPlay, ArrowLeft, ArrowRight, Check, RefreshRight, Share)

PlantGame.vue
  ├── @element-plus/icons-vue (Clock, Star, TrendCharts, Check, RefreshRight, VideoPlay)
  └── @/utils (getFirstImage)

CaptchaInput.vue
  ├── @element-plus/icons-vue (Key, Loading)
  └── @/utils/request

InteractSidebar.vue
  ├── @element-plus/icons-vue (Trophy, Medal)
  ├── @/utils/request
  └── @/utils/logger (logFetchError)
```

---

## 常见错误

### 错误1：评论提交后没有更新列表

```vue
<script setup>
// ❌ 提交评论后没有刷新评论列表，用户看不到自己发的评论
const submitComment = async (content) => {
  await api.postComment(content)
  // 缺少刷新列表的代码！
}

// ✅ 提交后重新获取评论列表
const submitComment = async (content) => {
  await api.postComment(content)
  // 重新加载评论列表
  await loadComments()
}
</script>
```

### 错误2：游戏没有处理退出时的清理

```vue
<script setup>
import { onMounted, onUnmounted } from 'vue'

// ❌ 游戏的定时器在组件销毁时没有清除，导致内存泄漏
let timer = null
onMounted(() => {
  timer = setInterval(() => { /* 倒计时 */ }, 1000)
})
// 缺少 onUnmounted 清理！

// ✅ 组件销毁时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>
```

### 错误3：验证码验证放在前端

```vue
<script setup>
// ❌ 验证码的正确答案放在前端，用户打开开发者工具就能看到
const correctCaptcha = '3K2A'  // 暴露了！

// ✅ 验证码只能由后端验证，前端只负责收集用户输入
const verifyCaptcha = async (userInput, captchaKey) => {
  const res = await api.verifyCaptcha({ code: userInput, captchaKey })
  // 由后端判断对错
  return res.data.valid
}
</script>
```

### 错误4：PlantGame/QuizSection 的业务逻辑写在子组件中

```vue
<!-- ❌ 把计时器、得分计算等逻辑放在 PlantGame/QuizSection 组件内部 -->
<!-- 这两个组件是纯展示组件，所有业务逻辑由父页面控制 -->

<!-- ✅ 父页面管理所有状态，子组件只负责 UI 展示和事件上报 -->
<PlantGame
  :current-plant="currentPlant"
  :score="gameScore"
  :formatted-time="timeDisplay"
  @answer="handleAnswer"
  @start="startGame"
/>
```

---

## 代码审查与改进建议

- [安全] CommentSection.vue 显示500字符限制但 el-input 未设置 maxlength 属性，用户可输入超长文本
- [组件设计] CommentSection.vue 通过 emit 传递成功/失败回调函数（`emit('post', content, replyData, onSuccess, onError)`）是 Vue 反模式，应改为 Promise 或单独的 success/error 事件
- [错误处理] CaptchaInput.vue 验证码加载失败无用户提示，只在 console.error 输出
- [导出] CaptchaInput.vue 未在 index.js 中统一导出，使用时需单独引入，与其他组件不一致
- [性能] InteractSidebar.vue 的 loadLeaderboard 在 catch 中仅 console.error，未向用户展示加载失败状态
