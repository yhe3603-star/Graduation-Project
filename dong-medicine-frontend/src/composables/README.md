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

## 开发规范

1. **命名规范**: 函数以 `use` 开头，使用大驼峰命名法
2. **返回值**: 返回响应式状态和方法
3. **组合使用**: 可以在组合式函数中调用其他组合式函数
4. **文档注释**: 添加JSDoc注释说明用途和参数

---

**最后更新时间**: 2026年3月27日
