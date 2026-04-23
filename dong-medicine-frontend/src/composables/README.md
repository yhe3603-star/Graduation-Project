# 组合式函数目录 (composables/)

> 类比：想象一本**可复用的菜谱**。做酸菜鱼需要"腌鱼片"、"炒酸菜"、"调汤底"三个步骤，你把这些步骤封装成一个"酸菜鱼菜谱"。以后每次做酸菜鱼，只需要翻开菜谱照着做就行，不用从头想步骤。**Composable 就是这种菜谱**，把复杂的逻辑封装起来，在多个组件中复用。

## 什么是 Composable？

Composable（组合式函数）是 Vue 3 中**封装和复用有状态逻辑**的标准方式。它是一个以 `use` 开头的函数，内部使用 Vue 的响应式 API（`ref`、`computed`、`watch` 等），返回响应式数据和方法。

简单来说：
- **工具函数（utils）**：无状态的纯函数，输入相同就输出相同，如 `formatTime()`
- **组合式函数（composables）**：有状态的逻辑封装，包含响应式数据，如 `useQuiz()`

```javascript
// 工具函数：无状态，纯计算
function formatTime(date) {
  return date.toLocaleString()
}

// 组合式函数：有状态，包含响应式数据和操作方法
function useQuiz() {
  const score = ref(0)           // 响应式状态
  const currentQuestion = ref(0) // 响应式状态
  const nextQuestion = () => {   // 操作方法
    currentQuestion.value++
  }
  return { score, currentQuestion, nextQuestion }
}
```

## 为什么用 Composable 而不是 Mixin？

Vue 2 时代用 Mixin 复用逻辑，但 Mixin 有很多问题：

| 问题 | Mixin | Composable |
|------|-------|------------|
| 命名冲突 | 多个 Mixin 可能有同名变量，不知道用谁的 | 显式解构，`const { score } = useQuiz()`，来源清晰 |
| 数据来源不明 | 模板里看到 `score`，不知道来自哪个 Mixin | 一看就知道 `score` 来自 `useQuiz()` |
| 类型推断差 | TypeScript 很难推断 Mixin 的类型 | 完美的 TypeScript 支持 |
| 参数传递 | 无法传参 | 可以传参 `useQuiz(request, isLoggedIn)` |

**一句话总结**：Composable 是 Mixin 的升级版，更清晰、更灵活、更安全。

---

## 全部 12 个 Composable

| 文件 | 导出函数 | 功能描述 |
|------|---------|---------|
| `useFavorite.js` | `useFavorite` | 收藏/取消收藏、浏览量更新 |
| `useQuiz.js` | `useQuiz` | 趣味答题完整流程（选题、计时、提交、分享） |
| `usePlantGame.js` | `usePlantGame` | 植物识别游戏（选难度、答题、连击加分） |
| `useAdminData.js` | `useAdminData`, `useAdminDialogs`, `useAdminActions` | 管理后台数据加载、对话框管理、CRUD 操作 |
| `useInteraction.js` | `useCountdown`, `useComments`, `usePagination`, `useFilter`, `useStats` | 交互功能（倒计时、评论、分页、筛选、统计） |
| `useVisualData.js` | `useVisualData` | 数据可视化图表数据计算 |
| `useMedia.js` | `useDocumentPreview`, `useMediaTabs`, `useDocumentList`, `useMediaDisplay`, `useFileInfo` | 媒体文件展示与预览 |
| `useFormDialog.js` | `useFormDialog` | 表单对话框（新增/编辑模式切换、更新日志） |
| `useUpdateLog.js` | `useUpdateLog`, `useUpdateLogDisplay` | 更新日志的增删改查与展示 |
| `usePersonalCenter.js` | `usePersonalCenter` | 个人中心（收藏、答题记录、密码修改、退出） |
| `useDebounce.js` | `useDebounceFn`, `useDebounce` | 防抖函数和防抖值 |
| `useErrorHandler.js` | `useErrorHandler` | 统一错误处理（API 错误、业务错误码） |

---

## 核心 Composable 详解

### useFavorite -- 收藏功能

收藏是多个页面共享的功能（植物、知识、传承人、资源都可以收藏），所以抽成 composable：

```javascript
import { useFavorite } from '@/composables'

// 在组件中使用，传入收藏类型
const { isFavorited, toggleFavorite, loadFavorites } = useFavorite('plant')

// 检查某个植物是否已收藏
const favorited = isFavorited(plantId)  // 返回 true/false

// 切换收藏状态（已收藏则取消，未收藏则添加）
await toggleFavorite(plantId, isFavorited(plantId))

// 页面加载时获取收藏列表
onMounted(() => loadFavorites())
```

**内部逻辑**：
1. `loadFavorites`：请求 `/favorites/my` 获取当前用户的所有收藏
2. `isFavorited(id)`：在本地收藏列表中查找，判断是否已收藏
3. `toggleFavorite(id, isFav)`：
   - 已收藏 -> 请求 `DELETE /favorites/{type}/{id}`，从列表移除
   - 未收藏 -> 请求 `POST /favorites/{type}/{id}`，添加到列表
4. 未登录时点击收藏，提示"请先登录"

---

### useQuiz -- 趣味答题

答题功能涉及多个状态和步骤，是典型的复杂逻辑封装：

```javascript
import { useQuiz } from '@/composables'

const request = inject('request')
const isLoggedIn = computed(() => !!sessionStorage.getItem('token'))

const {
  // 状态
  isQuizStarted,        // 是否已开始答题
  selectedQuestions,    // 当前题目列表
  currentQuestion,      // 当前题目索引
  userAnswers,          // 用户答案数组
  quizFinished,         // 是否已交卷
  finalScore,           // 最终得分
  correctCount,         // 正确题数
  formattedTime,        // 倒计时显示（如 "04:30"）
  isLowTime,            // 是否剩余不足 10 秒
  selectedDifficulty,   // 当前难度

  // 方法
  setDifficulty,        // 设置难度（easy/medium/hard）
  startNewQuiz,         // 开始新答题
  nextQuestion,         // 下一题
  prevQuestion,         // 上一题
  submitQuiz,           // 提交答卷
  resetQuiz,            // 重置答题
  shareQuizResult       // 分享成绩
} = useQuiz(request, isLoggedIn)
```

**答题流程**：

```
选择难度（初级10题/中级20题/高级30题）
    |
    v
点击"开始答题" --> startNewQuiz()
    |
    v
倒计时开始（5分钟）
    |
    v
逐题作答 --> nextQuestion() / prevQuestion()
    |
    +-- 时间到 --> 自动交卷 autoSubmit()
    +-- 手动交卷 --> submitQuiz()
    |
    v
显示成绩 --> finalScore, correctCount
    |
    v
分享成绩 --> shareQuizResult()
```

**难度配置**：

```javascript
const difficultyConfig = {
  easy:   { label: '初级', count: 10, scorePerQuestion: 10, time: 5 },  // 10题，每题10分，5分钟
  medium: { label: '中级', count: 20, scorePerQuestion: 15, time: 5 },  // 20题，每题15分，5分钟
  hard:   { label: '高级', count: 30, scorePerQuestion: 20, time: 5 }   // 30题，每题20分，5分钟
}
```

---

### usePlantGame -- 植物识别游戏

植物识别游戏比答题更复杂，包含连击加分机制：

```javascript
import { usePlantGame } from '@/composables'

const {
  // 状态
  currentPlant,         // 当前植物（显示图片，猜名字）
  options,              // 选项列表
  answered,             // 是否已回答当前题
  selectedAnswer,       // 选中的答案
  gameScore,            // 游戏分数
  streak,               // 连击次数
  totalQuestions,       // 总答题数
  correctAnswers,       // 正确数
  gameFinished,         // 游戏是否结束
  formattedTime,        // 倒计时

  // 方法
  loadPlants,           // 加载植物数据
  selectDifficulty,     // 选择难度
  startGame,            // 开始游戏
  checkAnswer,          // 检查答案
  resetGame,            // 重置游戏
  submitGameScore       // 提交游戏分数
} = usePlantGame(request, isLoggedIn)
```

**连击加分机制**：

```javascript
const checkAnswer = (answer) => {
  if (answered.value) return  // 已经回答过了，不能重复
  answered.value = true
  selectedAnswer.value = answer
  totalQuestions.value++

  const isCorrect = answer === currentPlant.value.nameCn
  if (isCorrect) {
    correctAnswers.value++
    streak.value++  // 连击 +1
    const baseScore = SCORE_CONFIG[difficulty.value]  // 基础分：10/15/20
    const bonus = Math.min(streak.value - 1, 5) * 2   // 连击奖励：最高 +10
    gameScore.value += baseScore + bonus
    // 连击 1 次：0 奖励  |  连击 3 次：+4  |  连击 6 次：+10（上限）
  } else {
    streak.value = 0  // 答错，连击归零
  }
}
```

**难度影响选项数量**：

```javascript
const OPTION_COUNT = {
  easy: 3,     // 3 选 1
  medium: 4,   // 4 选 1
  hard: 5      // 5 选 1
}
```

---

### useAdminData -- 管理后台数据管理

管理后台是最复杂的页面，数据管理拆成了三个 composable：

#### useAdminData -- 数据加载

```javascript
import { useAdminData } from '@/composables'

const {
  // 各模块数据
  users, knowledgeList, inheritorsList, plantsList,
  qaList, resourcesList, feedbackList, quizList,
  commentsList, logList, adminStats,

  // 分页信息
  pagination,

  // 排序后的数据（待处理的排在前面）
  sortedComments,   // 评论：待审核的排前面
  sortedFeedback,   // 反馈：待处理的排前面

  // 方法
  fetchData,          // 加载所有数据
  handleAdminPage,    // 翻页
  handleAdminSize     // 改变每页条数
} = useAdminData(request)
```

#### useAdminDialogs -- 对话框管理

```javascript
const {
  dialogVisible,     // 表单对话框是否可见
  detailVisible,     // 详情对话框是否可见
  currentDetail,     // 当前查看的详情数据
  formData,          // 表单数据

  openDialog,        // 打开新增对话框
  viewDetail,        // 查看详情
  editItem           // 打开编辑对话框（预填数据）
} = useAdminDialogs()
```

#### useAdminActions -- CRUD 操作

```javascript
const {
  saveItem,          // 保存（新增或更新）
  deleteItem,        // 删除（带确认弹窗）
  approveComment,    // 审核通过评论
  rejectComment,     // 拒绝评论
  batchDeleteLogs,   // 批量删除日志
  clearAllLogs,      // 清空所有日志
  replyFeedback      // 回复反馈
} = useAdminActions(request, fetchData)
```

---

### useInteraction -- 交互功能集合

这个文件导出多个独立的 composable，按需使用：

#### useCountdown -- 倒计时

```javascript
import { useCountdown } from '@/composables'

const { formattedTime, isRunning, isExpired, isLowTime, start, stop, reset } = useCountdown(5)
// 参数 5 表示 5 分钟

start()   // 开始倒计时
// formattedTime.value 变化：05:00 -> 04:59 -> ... -> 00:00
// isLowTime.value 在剩余 10 秒时变为 true（可以变红提醒）
// isExpired.value 在倒计时结束时变为 true

reset(3)  // 重置为 3 分钟
stop()    // 暂停倒计时
```

#### useComments -- 评论功能

```javascript
const { comments, loadComments, handleCommentPost, currentPage, totalItems } = useComments(request, isLoggedIn)

// 加载评论列表
loadComments()

// 发表评论
handleCommentPost('这个钩藤的介绍很详细！', null, () => {
  console.log('评论成功')
}, () => {
  console.log('评论失败')
})
```

#### usePagination -- 前端分页

```javascript
const { currentPage, pageSize, paginatedList, resetPage } = usePagination(12)

// 对列表进行分页切割
const pageItems = paginatedList(allItems)
// currentPage=1, pageSize=12 时，返回 allItems[0..11]
// currentPage=2, pageSize=12 时，返回 allItems[12..23]
```

#### useFilter -- 前端筛选

```javascript
const { keyword, filters, filteredList, setFilter, clearFilters } = useFilter(
  items,                    // 要筛选的列表
  ['nameCn', 'category']   // 搜索字段
)

keyword.value = '钩藤'     // 按关键词搜索
setFilter('category', '藤本')  // 按分类筛选
clearFilters()              // 清除所有筛选
```

#### useStats -- 统计数据

```javascript
const stats = useStats(items, [
  { value: 'count', label: '总数' },
  { value: 'views', label: '总浏览' },
  { value: 'favorites', label: '总收藏' }
])
// 返回 computed：[{ value: 50, label: '总数' }, { value: 1200, label: '总浏览' }, ...]
```

---

### useVisualData -- 图表数据计算

数据可视化页面需要从多个 API 获取数据，再计算成 ECharts 需要的格式：

```javascript
import { useVisualData } from '@/composables'

const { loading, stats, chartData, regionList, fetchData } = useVisualData(request)

// 加载数据
await fetchData()

// stats：各模块总数
stats.value  // { plants: 50, knowledge: 30, inheritors: 15, qa: 40, resources: 20 }

// chartData：ECharts 图表数据
chartData.value.therapyCategories  // 疗法分类饼图数据
chartData.value.inheritorLevels    // 传承人级别柱状图数据
chartData.value.plantCategories    // 植物分类柱状图数据
chartData.value.plantDistribution  // 植物分布地图数据
chartData.value.knowledgePopularity // 知识热度排行
chartData.value.formulaFreq        // 药方使用频率
chartData.value.userTrend          // 用户趋势折线图
```

**数据计算过程**：

```
请求 5 个 API（并行）
    |
    v
提取分页数据
    |
    v
计算各类图表数据：
  - 疗法分类：按 therapyCategory 分组计数
  - 传承人级别：按 level（国家/省/市/县）分组计数
  - 植物分类：按 category 分组计数，取前 8
  - 植物分布：解析 distribution 字段，按省份分组
  - 知识热度：按 popularity 排序，取前 10
  - 药方频率：筛选 type='药方' 的知识，按浏览量排序
  - 用户趋势：请求 /stats/trend 接口
```

---

## 如何创建一个新的 Composable

假设你要创建一个"药浴预约"的 composable：

### 第 1 步：创建文件

在 `composables/` 目录下新建 `useHerbalBath.js`，文件名以 `use` 开头：

```javascript
// composables/useHerbalBath.js
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

// composable 就是一个函数，以 use 开头
export function useHerbalBath(request) {
  // 1. 定义响应式状态
  const bathList = ref([])          // 药浴列表
  const loading = ref(false)        // 加载状态
  const selectedType = ref('all')   // 选中的药浴类型

  // 2. 定义计算属性
  const filteredList = computed(() => {
    if (selectedType.value === 'all') return bathList.value
    return bathList.value.filter(b => b.type === selectedType.value)
  })

  // 3. 定义方法
  const fetchBathList = async () => {
    loading.value = true
    try {
      const res = await request.get('/herbal-bath/list')
      bathList.value = res?.data?.records || []
    } catch (e) {
      ElMessage.error('加载药浴数据失败')
    } finally {
      loading.value = false
    }
  }

  const makeReservation = async (bathId) => {
    try {
      await request.post(`/herbal-bath/${bathId}/reserve`)
      ElMessage.success('预约成功')
      return true
    } catch (e) {
      ElMessage.error('预约失败')
      return false
    }
  }

  // 4. 返回所有需要暴露的状态和方法
  return {
    bathList,
    loading,
    selectedType,
    filteredList,
    fetchBathList,
    makeReservation
  }
}
```

### 第 2 步：在组件中使用

```vue
<script setup>
import { inject, onMounted } from 'vue'
import { useHerbalBath } from '@/composables'

const request = inject('request')
const { filteredList, loading, selectedType, fetchBathList, makeReservation } = useHerbalBath(request)

onMounted(fetchBathList)
</script>

<template>
  <div v-loading="loading">
    <el-select v-model="selectedType">
      <el-option label="全部" value="all" />
      <el-option label="足浴" value="foot" />
      <el-option label="全身浴" value="full" />
    </el-select>

    <div v-for="bath in filteredList" :key="bath.id">
      <h3>{{ bath.name }}</h3>
      <el-button @click="makeReservation(bath.id)">预约</el-button>
    </div>
  </div>
</template>
```

### 第 3 步：在 index.js 中导出（可选）

如果希望从 `@/composables` 统一导入，在 `composables/index.js` 中添加：

```javascript
export * from './useHerbalBath'
```

---

## Composable 编写规范

### 命名规范
- 文件名：`useXxx.js`（驼峰命名）
- 导出函数：`useXxx`（与文件名一致）

### 参数规范
- 外部依赖通过参数传入（如 `request`、`isLoggedIn`），而不是在 composable 内部导入
- 这样方便测试，也方便在不同上下文中复用

### 返回值规范
- 返回一个对象，包含所有需要暴露的状态和方法
- 状态用 `ref` 或 `computed`，方法用普通函数
- 不要返回 `reactive` 对象，容易丢失响应性

### 清理规范
- 如果 composable 内部有定时器、事件监听等，必须在 `onUnmounted` 中清理

```javascript
import { onUnmounted } from 'vue'

export function useCountdown(minutes) {
  let timer = null

  const start = () => {
    timer = setInterval(() => { /* ... */ }, 1000)
  }

  // 组件销毁时自动清理定时器
  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  return { start }
}
```

---

## 常见错误

### 错误 1：在 composable 外部调用

```javascript
// 错误：composable 内部用了 onMounted、onUnmounted 等，
// 必须在 setup 函数中调用，否则生命周期钩子不生效
const { score } = useQuiz(request, isLoggedIn)  // 在组件外部调用！

// 正确：在 <script setup> 中调用
<script setup>
const { score } = useQuiz(request, isLoggedIn)
</script>
```

### 错误 2：忘记返回需要的状态

```javascript
// 错误：忘记返回 loading，组件无法访问
export function useHerbalBath(request) {
  const loading = ref(false)
  const fetchList = async () => { loading.value = true; /* ... */ }
  return { fetchList }  // loading 没有返回！
}

// 正确：返回所有组件需要的状态和方法
export function useHerbalBath(request) {
  const loading = ref(false)
  const fetchList = async () => { loading.value = true; /* ... */ }
  return { loading, fetchList }
}
```

### 错误 3：在 composable 中硬编码依赖

```javascript
// 错误：直接导入 request，无法替换，不方便测试
import request from '@/utils/request'
export function useHerbalBath() {
  const res = await request.get('/bath/list')
}

// 正确：通过参数传入依赖
export function useHerbalBath(request) {
  const res = await request.get('/bath/list')
}
// 使用时：const { ... } = useHerbalBath(inject('request'))
```

### 错误 4：不清理副作用

```javascript
// 错误：定时器没有被清理，组件销毁后还在执行
export function usePolling() {
  setInterval(() => { /* 轮询数据 */ }, 5000)
}

// 正确：组件销毁时清理定时器
export function usePolling() {
  let timer = setInterval(() => { /* 轮询数据 */ }, 5000)
  onUnmounted(() => clearInterval(timer))
}
```
