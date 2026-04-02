# 组合式函数目录 (composables)

本目录存放 Vue 3 的组合式函数（Composables），用于封装和复用逻辑。

## 目录

- [什么是组合式函数？](#什么是组合式函数)
- [目录结构](#目录结构)
- [组合式函数列表](#组合式函数列表)
- [开发规范](#开发规范)
- [常用组合式函数详解](#常用组合式函数详解)

---

## 什么是组合式函数？

### 组合式函数的概念

**组合式函数（Composable）** 是 Vue 3 中复用逻辑的方式。你可以把它想象成"功能模块"——把一组相关的功能打包成一个函数，需要的时候就可以调用。

### 为什么需要组合式函数？

```
┌─────────────────────────────────────────────────────────────────┐
│                    没有组合式函数                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  组件A：                                                        │
│    - 定义 loading 状态                                          │
│    - 定义 fetchData 方法                                        │
│    - 定义错误处理逻辑                                           │
│                                                                 │
│  组件B：                                                        │
│    - 定义 loading 状态（重复）                                   │
│    - 定义 fetchData 方法（重复）                                 │
│    - 定义错误处理逻辑（重复）                                    │
│                                                                 │
│  → 代码重复、难以维护                                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     有组合式函数                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  useDataLoader.js：                                             │
│    - 封装 loading 状态                                          │
│    - 封装 fetchData 方法                                        │
│    - 封装错误处理逻辑                                           │
│                                                                 │
│  组件A：const { loading, data, fetch } = useDataLoader()        │
│  组件B：const { loading, data, fetch } = useDataLoader()        │
│                                                                 │
│  → 代码复用、易于维护                                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 组合式函数的基本结构

```javascript
// composables/useCounter.js
import { ref } from 'vue'

// 组合式函数以 "use" 开头
export function useCounter(initialValue = 0) {
  // 1. 响应式状态
  const count = ref(initialValue)
  
  // 2. 计算属性
  const doubleCount = computed(() => count.value * 2)
  
  // 3. 方法
  const increment = () => {
    count.value++
  }
  
  const decrement = () => {
    count.value--
  }
  
  const reset = () => {
    count.value = initialValue
  }
  
  // 4. 返回需要暴露的内容
  return {
    count,
    doubleCount,
    increment,
    decrement,
    reset
  }
}
```

### 使用组合式函数

```vue
<template>
  <div>
    <p>计数: {{ count }}</p>
    <p>双倍: {{ doubleCount }}</p>
    <button @click="increment">+1</button>
    <button @click="decrement">-1</button>
    <button @click="reset">重置</button>
  </div>
</template>

<script setup>
import { useCounter } from '@/composables/useCounter'

// 使用组合式函数
const { count, doubleCount, increment, decrement, reset } = useCounter(10)
</script>
```

---

## 目录结构

```
composables/
│
├── index.js                           # 导出入口
├── useAdminData.js                    # 管理后台数据
├── useDebounce.js                     # 防抖函数
├── useErrorHandler.js                 # 错误处理
├── useFavorite.js                     # 收藏功能
├── useFormDialog.js                   # 表单对话框
├── useInteraction.js                  # 交互功能
├── useMedia.js                        # 媒体处理
├── usePersonalCenter.js               # 个人中心
├── usePlantGame.js                    # 植物游戏
├── useQuiz.js                         # 答题逻辑
├── useUpdateLog.js                    # 更新日志
└── useVisualData.js                   # 可视化数据
```

---

## 组合式函数列表

| 函数 | 功能描述 |
|------|----------|
| useAdminData | 管理后台数据管理，包含数据获取、对话框管理、CRUD 操作 |
| useQuiz | 趣味答题功能，题目加载、答案提交、计时、评分 |
| usePlantGame | 植物识别游戏，难度设置、答题逻辑、分数计算 |
| useInteraction | 交互功能，包含倒计时、评论、分页、过滤、统计 |
| useFavorite | 收藏功能，收藏状态管理、收藏/取消收藏操作 |
| useMedia | 媒体处理，文档预览、媒体显示 |
| usePersonalCenter | 个人中心功能，用户信息、收藏管理、密码修改 |
| useFormDialog | 表单对话框通用逻辑 |
| useUpdateLog | 更新日志解析与管理 |
| useDebounce | 防抖函数 |
| useErrorHandler | 错误处理 |
| useVisualData | 数据可视化数据获取 |

---

## 开发规范

### 1. 命名规范

```javascript
// 组合式函数以 "use" 开头
export function useFeatureName() {
  // ...
}

// 返回的对象使用解构
const { data, loading, error, fetch } = useFeatureName()
```

### 2. 结构规范

```javascript
export function useFeatureName(options = {}) {
  // 1. 接收配置参数
  const { initialValue = null, autoFetch = true } = options
  
  // 2. 定义响应式状态
  const data = ref(initialValue)
  const loading = ref(false)
  const error = ref(null)
  
  // 3. 定义计算属性
  const hasData = computed(() => data.value !== null)
  
  // 4. 定义方法
  const fetch = async () => {
    loading.value = true
    error.value = null
    try {
      data.value = await api.fetchData()
    } catch (e) {
      error.value = e
    } finally {
      loading.value = false
    }
  }
  
  // 5. 生命周期钩子
  onMounted(() => {
    if (autoFetch) {
      fetch()
    }
  })
  
  // 6. 返回需要暴露的内容
  return {
    // 状态
    data,
    loading,
    error,
    // 计算属性
    hasData,
    // 方法
    fetch
  }
}
```

### 3. 返回值规范

```javascript
// 推荐：返回对象，便于解构和重命名
return {
  data,
  loading,
  error,
  fetch
}

// 使用时可以重命名
const { data: users, fetch: fetchUsers } = useFeatureName()
```

---

## 常用组合式函数详解

### useQuiz - 答题逻辑

封装趣味答题的完整逻辑。

```javascript
// composables/useQuiz.js
import { ref, computed } from 'vue'
import { getQuizQuestions, submitQuiz } from '@/api/quiz'

export function useQuiz() {
  // 难度配置
  const difficultyConfig = {
    easy: { label: '初级', count: 10, scorePerQuestion: 10, time: 5 },
    medium: { label: '中级', count: 20, scorePerQuestion: 15, time: 5 },
    hard: { label: '高级', count: 30, scorePerQuestion: 20, time: 5 }
  }
  
  // 状态
  const questions = ref([])
  const currentIndex = ref(0)
  const answers = ref({})
  const score = ref(0)
  const showResult = ref(false)
  const loading = ref(false)
  
  // 计算属性
  const currentQuestion = computed(() => questions.value[currentIndex.value])
  const progress = computed(() => {
    if (questions.value.length === 0) return 0
    return Math.round((currentIndex.value / questions.value.length) * 100)
  })
  
  // 方法
  const startQuiz = async (difficulty) => {
    loading.value = true
    try {
      const config = difficultyConfig[difficulty]
      const res = await getQuizQuestions(config.count, config.scorePerQuestion)
      questions.value = res.data
      currentIndex.value = 0
      answers.value = {}
      score.value = 0
      showResult.value = false
    } finally {
      loading.value = false
    }
  }
  
  const answerQuestion = (answer) => {
    answers.value[currentQuestion.value.id] = answer
  }
  
  const nextQuestion = () => {
    if (currentIndex.value < questions.value.length - 1) {
      currentIndex.value++
    }
  }
  
  const submitAnswers = async () => {
    const res = await submitQuiz({
      answers: Object.entries(answers.value).map(([id, answer]) => ({
        questionId: id,
        answer
      }))
    })
    score.value = res.data.totalScore
    showResult.value = true
  }
  
  return {
    // 状态
    questions,
    currentIndex,
    score,
    showResult,
    loading,
    // 计算属性
    currentQuestion,
    progress,
    // 方法
    startQuiz,
    answerQuestion,
    nextQuestion,
    submitAnswers
  }
}
```

**使用示例：**

```vue
<template>
  <div class="quiz-page">
    <div v-if="loading">加载中...</div>
    
    <div v-else-if="!showResult">
      <p>进度: {{ progress }}%</p>
      <p>问题 {{ currentIndex + 1 }}/{{ questions.length }}</p>
      <h3>{{ currentQuestion.question }}</h3>
      <button 
        v-for="option in currentQuestion.options" 
        :key="option"
        @click="answerQuestion(option)"
      >
        {{ option }}
      </button>
      <button @click="nextQuestion">下一题</button>
      <button @click="submitAnswers">提交</button>
    </div>
    
    <div v-else>
      <h2>得分: {{ score }}</h2>
    </div>
  </div>
</template>

<script setup>
import { useQuiz } from '@/composables/useQuiz'

const {
  questions,
  currentIndex,
  score,
  showResult,
  loading,
  currentQuestion,
  progress,
  startQuiz,
  answerQuestion,
  nextQuestion,
  submitAnswers
} = useQuiz()

onMounted(() => {
  startQuiz('easy')
})
</script>
```

### useFavorite - 收藏功能

封装收藏相关的逻辑。

```javascript
// composables/useFavorite.js
import { ref, computed } from 'vue'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'

export function useFavorite(targetType) {
  // 状态
  const favoriteMap = ref({})  // 记录每个目标是否已收藏
  const loading = ref(false)
  
  // 检查是否已收藏
  const isFavorited = (targetId) => {
    return favoriteMap.value[targetId] === true
  }
  
  // 切换收藏状态
  const toggleFavorite = async (targetId) => {
    loading.value = true
    try {
      if (isFavorited(targetId)) {
        await removeFavorite(targetType, targetId)
        favoriteMap.value[targetId] = false
      } else {
        await addFavorite(targetType, targetId)
        favoriteMap.value[targetId] = true
      }
    } finally {
      loading.value = false
    }
  }
  
  // 初始化收藏状态
  const initFavoriteStatus = async (targetId) => {
    const res = await checkFavorite(targetType, targetId)
    favoriteMap.value[targetId] = res.data
  }
  
  return {
    isFavorited,
    toggleFavorite,
    initFavoriteStatus,
    loading
  }
}
```

**使用示例：**

```vue
<template>
  <button 
    @click="toggleFavorite(plant.id)"
    :class="{ active: isFavorited(plant.id) }"
  >
    {{ isFavorited(plant.id) ? '已收藏' : '收藏' }}
  </button>
</template>

<script setup>
import { useFavorite } from '@/composables/useFavorite'

const { isFavorited, toggleFavorite } = useFavorite('plant')

onMounted(() => {
  initFavoriteStatus(plant.id)
})
</script>
```

### useDebounce - 防抖函数

封装防抖逻辑，用于搜索输入等场景。

```javascript
// composables/useDebounce.js
import { ref, watch } from 'vue'

export function useDebounce(value, delay = 300) {
  const debouncedValue = ref(value.value)
  let timeout = null
  
  watch(value, (newValue) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      debouncedValue.value = newValue
    }, delay)
  })
  
  return debouncedValue
}
```

**使用示例：**

```vue
<template>
  <input v-model="searchKeyword" placeholder="搜索...">
  <p>搜索词: {{ debouncedKeyword }}</p>
</template>

<script setup>
import { ref } from 'vue'
import { useDebounce } from '@/composables/useDebounce'

const searchKeyword = ref('')
const debouncedKeyword = useDebounce(searchKeyword, 500)

// 监听防抖后的值，发起搜索请求
watch(debouncedKeyword, (keyword) => {
  if (keyword) {
    search(keyword)
  }
})
</script>
```

---

## 最佳实践

### 1. 单一职责

每个组合式函数只负责一个功能。

```javascript
// 好的做法：职责单一
export function useCounter() { /* 计数器逻辑 */ }
export function useTimer() { /* 计时器逻辑 */ }

// 不好的做法：职责混乱
export function useCounterAndTimer() { /* 混合逻辑 */ }
```

### 2. 可配置

通过参数提供灵活性。

```javascript
export function useFetch(url, options = {}) {
  const { 
    immediate = true,      // 是否立即执行
    method = 'GET',        // 请求方法
    headers = {}           // 请求头
  } = options
  
  // ...
}
```

### 3. 返回清晰

返回的内容要有清晰的命名。

```javascript
return {
  // 状态
  data,
  loading,
  error,
  // 方法
  fetch,
  reset
}
```

---

**相关文档**

- [Vue 3 组合式函数](https://cn.vuejs.org/guide/reusability/composables.html)
- [Vue 3 组合式 API](https://cn.vuejs.org/api/composition-api-setup.html)

---

**最后更新时间**：2026年4月3日
