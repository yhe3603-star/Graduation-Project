# 组合式函数目录 (composables)

本目录存放Vue 3的组合式函数（Composables），用于封装和复用有状态的逻辑。

## 📖 什么是组合式函数？

组合式函数是Vue 3引入的一种代码组织方式，它允许我们将组件中可复用的逻辑提取出来，形成独立的函数。这些函数可以：
- 封装响应式状态
- 提供可复用的逻辑
- 组合多个功能
- 简化组件代码

## 📁 文件列表

| 文件名 | 功能说明 | 主要导出 |
|--------|----------|----------|
| `useAdminData.js` | 管理后台数据处理 | `useAdminData`, `useAdminDialogs`, `useAdminActions` |
| `useDebounce.js` | 防抖处理 | `useDebounceFn` |
| `useErrorHandler.js` | 错误处理 | `useErrorHandler` |
| `useFavorite.js` | 收藏功能 | `useFavorite` |
| `useFormDialog.js` | 表单对话框管理 | `useFormDialog` |
| `useInteraction.js` | 交互功能 | `useCountdown`, `useComments`, `usePagination`, `useFilter`, `useStats` |
| `useMedia.js` | 媒体处理 | `useMedia` |
| `usePersonalCenter.js` | 个人中心功能 | `usePersonalCenter` |
| `usePlantGame.js` | 植物识别游戏 | `usePlantGame` |
| `useQuiz.js` | 趣味答题功能 | `useQuiz` |
| `useUpdateLog.js` | 更新日志功能 | `useUpdateLog` |
| `useVisualData.js` | 可视化数据 | `useVisualData` |

## 📦 详细说明

### 1. useAdminData.js

管理后台数据处理的核心组合式函数。

**导出函数:**
- `useAdminData(entityType, request)` - 处理管理后台的数据请求
- `useAdminDialogs()` - 管理对话框状态
- `useAdminActions(request, entityType)` - 处理增删改查操作

**使用示例:**
```javascript
import { useAdminData } from '@/composables/useAdminData'

const { dataList, loading, fetchData, deleteItem } = useAdminData('plants', request)
```

### 2. useDebounce.js

防抖处理函数，用于优化频繁触发的事件。

**导出函数:**
- `useDebounceFn(fn, delay)` - 创建防抖函数

**使用示例:**
```javascript
import { useDebounceFn } from '@/composables/useDebounce'

const debouncedSearch = useDebounceFn((keyword) => {
  // 搜索逻辑
}, 300)
```

### 3. useInteraction.js

交互功能的组合式函数集合。

**导出函数:**
- `useCountdown(seconds)` - 倒计时功能
- `useComments(request, targetType, targetId)` - 评论功能
- `usePagination(defaultPageSize)` - 分页功能
- `useFilter(defaultFilters)` - 筛选功能
- `useStats(request)` - 统计数据

**使用示例:**
```javascript
import { useCountdown, usePagination } from '@/composables/useInteraction'

// 倒计时
const { formattedTime, start, stop, isExpired } = useCountdown(60)

// 分页
const { currentPage, pageSize, total, changePage } = usePagination(10)
```

### 4. usePlantGame.js

植物识别游戏的核心逻辑。

**功能:**
- 游戏状态管理
- 难度选择
- 答题逻辑
- 分数计算
- 连胜奖励

**使用示例:**
```javascript
import { usePlantGame } from '@/composables/usePlantGame'

const {
  currentPlant, options, score, streak,
  selectDifficulty, checkAnswer, resetGame
} = usePlantGame(request, isLoggedIn)
```

### 5. useQuiz.js

趣味答题功能的核心逻辑。

**功能:**
- 答题状态管理
- 题目加载
- 答案提交
- 分数计算
- 计时器

**使用示例:**
```javascript
import { useQuiz } from '@/composables/useQuiz'

const {
  selectedQuestions, currentQuestion, userAnswers,
  finalScore, startNewQuiz, submitQuiz, resetQuiz
} = useQuiz(request, isLoggedIn)
```

### 6. useVisualData.js

数据可视化页面的数据处理。

**功能:**
- 获取各类统计数据
- 处理图表数据
- 数据格式转换

**使用示例:**
```javascript
import { useVisualData } from '@/composables/useVisualData'

const {
  chartData, loading,
  provinceData, categoryData, trendData
} = useVisualData(request)
```

### 7. useFavorite.js

收藏功能管理。

**功能:**
- 添加/取消收藏
- 检查收藏状态
- 获取收藏列表

**使用示例:**
```javascript
import { useFavorite } from '@/composables/useFavorite'

const {
  isFavorited, favoriteList,
  toggleFavorite, fetchFavorites
} = useFavorite(request, isLoggedIn)
```

### 8. useMedia.js

媒体资源处理。

**功能:**
- 媒体类型判断
- 媒体预览
- 媒体下载

**使用示例:**
```javascript
import { useMedia } from '@/composables/useMedia'

const {
  getMediaType, previewMedia, downloadMedia
} = useMedia()
```

## 🎯 使用规范

### 命名规范
- 组合式函数以 `use` 开头，如 `useCountdown`
- 文件名与函数名一致

### 函数结构
```javascript
import { ref, computed, onMounted } from 'vue'

export function useMyFeature(param) {
  // 响应式状态
  const state = ref(null)
  
  // 计算属性
  const computedValue = computed(() => {
    return state.value * 2
  })
  
  // 方法
  const doSomething = () => {
    state.value = 'new value'
  }
  
  // 生命周期
  onMounted(() => {
    // 初始化逻辑
  })
  
  // 返回需要暴露的内容
  return {
    state,
    computedValue,
    doSomething
  }
}
```

### 最佳实践
1. **单一职责**: 每个组合式函数只负责一个功能
2. **参数验证**: 对传入参数进行验证
3. **返回值**: 返回响应式引用和方法
4. **命名清晰**: 函数和变量名要有意义

## 📚 扩展阅读

- [Vue 3 组合式函数](https://cn.vuejs.org/guide/reusability/composables.html)
- [Vue 3 响应式 API](https://cn.vuejs.org/api/reactivity-core.html)
