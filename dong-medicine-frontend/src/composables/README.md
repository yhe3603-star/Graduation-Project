# Composables 组合式函数目录

本目录包含项目的所有组合式函数（Composables），用于复用组件逻辑。

## 目录结构

```
composables/
├── index.js              # 导出入口
├── useAdminData.js       # 管理后台数据
├── useQuiz.js            # 答题逻辑
├── usePlantGame.js       # 植物游戏逻辑
├── useInteraction.js     # 交互功能
├── useFavorite.js        # 收藏功能
├── useMedia.js           # 媒体处理
├── usePersonalCenter.js  # 个人中心功能
├── useFormDialog.js      # 表单对话框
├── useUpdateLog.js       # 更新日志
├── useDebounce.js        # 防抖函数
└── useErrorHandler.js    # 错误处理
```

## 组合式函数说明

### useAdminData.js - 管理后台数据

**用途**: 管理后台数据管理，包含数据获取、对话框管理、CRUD操作

**导出**:
```javascript
export function useAdminData(apiPath, options) {
  // 状态
  const data = ref([])
  const loading = ref(false)
  const dialogVisible = ref(false)
  const currentItem = ref(null)
  
  // 方法
  const fetchData = async () => { ... }
  const handleAdd = () => { ... }
  const handleEdit = (item) => { ... }
  const handleDelete = async (id) => { ... }
  const handleSave = async (formData) => { ... }
  
  return {
    data, loading, dialogVisible, currentItem,
    fetchData, handleAdd, handleEdit, handleDelete, handleSave
  }
}
```

**使用示例**:
```javascript
import { useAdminData } from '@/composables'

const { data, loading, fetchData, handleDelete } = useAdminData('/admin/plants')
```

---

### useQuiz.js - 答题逻辑

**用途**: 趣味答题功能，题目加载、答案提交、计时、评分

**导出**:
```javascript
export function useQuiz() {
  const questions = ref([])
  const currentIndex = ref(0)
  const score = ref(0)
  const answers = ref([])
  const timeLeft = ref(0)
  const isFinished = ref(false)
  
  const loadQuestions = async (count) => { ... }
  const submitAnswer = (answer) => { ... }
  const nextQuestion = () => { ... }
  const resetQuiz = () => { ... }
  
  return {
    questions, currentIndex, score, answers, timeLeft, isFinished,
    loadQuestions, submitAnswer, nextQuestion, resetQuiz
  }
}
```

---

### usePlantGame.js - 植物游戏逻辑

**用途**: 植物识别游戏，难度设置、答题逻辑、分数计算

**导出**:
```javascript
export function usePlantGame() {
  const currentPlant = ref(null)
  const options = ref([])
  const score = ref(0)
  const difficulty = ref('medium')
  const streak = ref(0)
  
  const startGame = async () => { ... }
  const checkAnswer = (selectedId) => { ... }
  const nextRound = () => { ... }
  
  return {
    currentPlant, options, score, difficulty, streak,
    startGame, checkAnswer, nextRound
  }
}
```

---

### useInteraction.js - 交互功能

**用途**: 交互功能，包含倒计时、评论、分页、过滤、统计

**导出**:
```javascript
export function useInteraction() {
  const comments = ref([])
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  
  const fetchComments = async (targetType, targetId) => { ... }
  const addComment = async (content) => { ... }
  const replyComment = async (commentId, content) => { ... }
  
  return {
    comments, page, pageSize, total,
    fetchComments, addComment, replyComment
  }
}
```

---

### useFavorite.js - 收藏功能

**用途**: 收藏状态管理、收藏/取消收藏操作

**导出**:
```javascript
export function useFavorite() {
  const favorites = ref([])
  const loading = ref(false)
  
  const checkFavorited = async (targetType, targetId) => { ... }
  const toggleFavorite = async (targetType, targetId) => { ... }
  const fetchFavorites = async () => { ... }
  
  return {
    favorites, loading,
    checkFavorited, toggleFavorite, fetchFavorites
  }
}
```

---

### useMedia.js - 媒体处理

**用途**: 文档预览、媒体显示

**导出**:
```javascript
export function useMedia() {
  const previewUrl = ref('')
  const previewVisible = ref(false)
  const mediaType = ref('')
  
  const previewDocument = (url) => { ... }
  const previewVideo = (url) => { ... }
  const previewImage = (url) => { ... }
  const closePreview = () => { ... }
  
  return {
    previewUrl, previewVisible, mediaType,
    previewDocument, previewVideo, previewImage, closePreview
  }
}
```

---

### usePersonalCenter.js - 个人中心功能

**用途**: 用户信息、收藏管理、密码修改

**导出**:
```javascript
export function usePersonalCenter() {
  const userInfo = ref(null)
  const favorites = ref([])
  const records = ref([])
  
  const fetchUserInfo = async () => { ... }
  const updateUserInfo = async (data) => { ... }
  const changePassword = async (data) => { ... }
  const fetchFavorites = async () => { ... }
  const fetchRecords = async () => { ... }
  
  return {
    userInfo, favorites, records,
    fetchUserInfo, updateUserInfo, changePassword, fetchFavorites, fetchRecords
  }
}
```

---

### useFormDialog.js - 表单对话框

**用途**: 表单对话框通用逻辑

**导出**:
```javascript
export function useFormDialog(initialData = {}) {
  const visible = ref(false)
  const formData = ref({ ...initialData })
  const loading = ref(false)
  
  const open = (data = null) => { ... }
  const close = () => { ... }
  const reset = () => { ... }
  const submit = async (apiPath) => { ... }
  
  return {
    visible, formData, loading,
    open, close, reset, submit
  }
}
```

---

### useUpdateLog.js - 更新日志

**用途**: 更新日志解析与管理

**导出**:
```javascript
export function useUpdateLog() {
  const logs = ref([])
  const loading = ref(false)
  
  const fetchLogs = async () => { ... }
  const parseLog = (content) => { ... }
  
  return {
    logs, loading,
    fetchLogs, parseLog
  }
}
```

---

### useDebounce.js - 防抖函数

**用途**: 防抖处理

**导出**:
```javascript
export function useDebounce(fn, delay = 300) {
  const timer = ref(null)
  
  const debouncedFn = (...args) => {
    if (timer.value) clearTimeout(timer.value)
    timer.value = setTimeout(() => fn(...args), delay)
  }
  
  const cancel = () => {
    if (timer.value) clearTimeout(timer.value)
  }
  
  return { debouncedFn, cancel }
}
```

---

### useErrorHandler.js - 错误处理

**用途**: 统一错误处理

**导出**:
```javascript
export function useErrorHandler() {
  const error = ref(null)
  const errorMessage = ref('')
  
  const handleError = (err) => { ... }
  const clearError = () => { ... }
  const showError = (message) => { ... }
  
  return {
    error, errorMessage,
    handleError, clearError, showError
  }
}
```

---

## 组合使用示例

### 多个Composable组合使用

```vue
<template>
  <div class="quiz-page">
    <div v-if="error">{{ errorMessage }}</div>
    <div v-else-if="loading">加载中...</div>
    <div v-else>
      <QuizSection
        :questions="questions"
        :current-index="currentIndex"
        :score="score"
        @submit="submitAnswer"
        @next="nextQuestion"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useQuiz } from '@/composables/useQuiz'
import { useErrorHandler } from '@/composables/useErrorHandler'
import { useDebounce } from '@/composables/useDebounce'

const {
  questions,
  currentIndex,
  score,
  loadQuestions,
  submitAnswer,
  nextQuestion
} = useQuiz()

const { error, errorMessage, handleError } = useErrorHandler()

const { debouncedFn: debouncedSubmit } = useDebounce(submitAnswer, 300)

onMounted(async () => {
  try {
    await loadQuestions(10)
  } catch (err) {
    handleError(err)
  }
})
</script>
```

### 在Composable中调用其他Composable

```javascript
import { ref } from 'vue'
import { useErrorHandler } from './useErrorHandler'
import { useDebounce } from './useDebounce'

export function useSearch(apiPath) {
  const { error, handleError } = useErrorHandler()
  const results = ref([])
  const loading = ref(false)
  
  const { debouncedFn: debouncedSearch } = useDebounce(async (keyword) => {
    loading.value = true
    try {
      const response = await request.get(apiPath, { params: { keyword } })
      results.value = response.data
    } catch (err) {
      handleError(err)
    } finally {
      loading.value = false
    }
  }, 500)
  
  return {
    results,
    loading,
    error,
    search: debouncedSearch
  }
}
```

---

## 开发规范

1. **命名规范**: 函数以 `use` 开头，使用大驼峰命名法
2. **返回值**: 返回响应式状态和方法
3. **组合使用**: 可以在组合式函数中调用其他组合式函数
4. **文档注释**: 添加JSDoc注释说明用途和参数

### JSDoc注释规范

```javascript
/**
 * 答题逻辑组合式函数
 * @param {Object} options - 配置选项
 * @param {number} options.questionCount - 题目数量，默认10
 * @param {number} options.timeLimit - 时间限制（秒），默认0表示不限时
 * @returns {Object} 答题相关的状态和方法
 * @example
 * const { questions, score, loadQuestions } = useQuiz({ questionCount: 20 })
 */
export function useQuiz(options = {}) {
  // ...
}
```

---

## 已知限制

| Composable | 限制 | 影响 |
|------------|------|------|
| useQuiz | 不支持题目分类筛选 | 无法按类型答题 |
| usePlantGame | 图片预加载未实现 | 可能影响游戏流畅度 |
| useMedia | 不支持音频预览 | 音频文件无法预览 |
| useFavorite | 未实现本地缓存 | 每次都需请求API |
| useDebounce | 不支持立即执行模式 | 某些场景需要立即执行 |
| useAdminData | 不支持批量操作 | 批量删除需多次请求 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **useQuiz**
   - 添加题目分类筛选
   - 实现答题历史记录
   - 添加错题本功能

2. **useFavorite**
   - 实现本地缓存
   - 添加收藏夹分类

3. **useMedia**
   - 添加音频预览支持
   - 实现媒体文件缓存

### 中期改进 (1-2月)

1. **TypeScript支持**
   - 添加类型定义文件
   - 提供更好的IDE支持

2. **测试覆盖**
   - 编写单元测试
   - 添加集成测试

3. **性能优化**
   - 实现请求缓存
   - 添加请求取消功能

### 长期规划 (3-6月)

1. **插件化架构**
   - 支持自定义Composable
   - 提供插件注册机制

2. **状态持久化**
   - 支持多种存储后端
   - 实现状态同步

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 响应式系统 |
| Vue Router | 4.2+ | 路由（部分函数） |
| Pinia | 2.3+ | 状态管理（部分函数） |
| Axios | 1.6+ | HTTP请求 |

---

## 常见问题

### 1. 如何在Composable中使用路由？

```javascript
import { useRouter } from 'vue-router'

export function useMyComposable() {
  const router = useRouter()
  
  const navigate = (path) => {
    router.push(path)
  }
  
  return { navigate }
}
```

### 2. 如何在Composable中使用Pinia Store？

```javascript
import { useUserStore } from '@/stores/user'

export function useMyComposable() {
  const userStore = useUserStore()
  
  const doSomething = () => {
    if (userStore.isLoggedIn) {
      // ...
    }
  }
  
  return { doSomething }
}
```

### 3. 如何处理Composable中的错误？

```javascript
export function useMyComposable() {
  const error = ref(null)
  
  const fetchData = async () => {
    try {
      // API调用
    } catch (err) {
      error.value = err
      console.error('请求失败:', err)
    }
  }
  
  return { error, fetchData }
}
```

### 4. 如何实现Composable的清理逻辑？

```javascript
import { onUnmounted } from 'vue'

export function useMyComposable() {
  const timer = ref(null)
  
  const startTimer = () => {
    timer.value = setInterval(() => {}, 1000)
  }
  
  // 组件卸载时清理
  onUnmounted(() => {
    if (timer.value) {
      clearInterval(timer.value)
    }
  })
  
  return { startTimer }
}
```

---

**最后更新时间**: 2026年3月30日
