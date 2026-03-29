# 页面组件目录说明

## 文件夹结构

本目录包含项目的所有页面组件，每个页面对应一个路由。

```
views/
├── Home.vue             # 首页
├── Plants.vue           # 药用植物页面
├── Inheritors.vue       # 传承人页面
├── Knowledge.vue        # 知识库页面
├── Qa.vue               # 问答社区页面
├── Resources.vue        # 学习资源页面
├── Interact.vue         # 互动专区页面
├── Visual.vue           # 数据可视化页面
├── PersonalCenter.vue   # 个人中心页面（需登录）
├── Admin.vue            # 管理后台页面（需管理员权限）
├── About.vue            # 关于页面
├── Feedback.vue         # 意见反馈页面
├── GlobalSearch.vue     # 全局搜索页面
├── NotFound.vue         # 404页面
└── README.md            # 说明文档
```

---

## 详细说明

### 1. Home.vue - 首页

**路由**: `/`

**功能描述**:
- 展示平台核心功能入口和统计数据
- 显示传承人风采轮播
- 提供快速导航卡片
- 集成AI智能问答入口

**核心代码解析**:

```vue
<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { quickEntries, coreModules, extendModules, getLevelClass, createHeroStats } from '@/config/homeConfig'

const request = inject('request')
const stats = ref({ plants: 21, formulas: 12, inheritors: 10, therapies: 6 })
const featuredInheritors = ref([])

// 计算属性：生成首页统计数据
const heroStats = computed(() => createHeroStats(stats.value))

// 页面加载时获取数据
onMounted(async () => {
  try {
    // 并行请求植物和传承人数据
    const [pRes, iRes] = await Promise.all([
      request.get('/plants/list', { params: { page: 1, size: 1 }, skipAuthRefresh: true }),
      request.get('/inheritors/list', { params: { page: 1, size: 5 }, skipAuthRefresh: true })
    ])
    stats.value.plants = pRes.data?.total || 0
    stats.value.inheritors = iRes.data?.total || 0
    featuredInheritors.value = iRes.data?.records?.slice(0, 4) || []
  } catch (e) {
    console.error('首页数据加载失败:', e)
  }
})
</script>
```

**依赖组件**:
- `AiChatCard.vue` - AI对话卡片
- `CardGrid.vue` - 卡片网格
- `UpdateLogCard.vue` - 更新日志

**数据流**:
```
onMounted → 并行请求(/plants/list, /inheritors/list) → 更新stats和featuredInheritors → 页面渲染
```

---

### 2. Plants.vue - 药用植物页面

**路由**: `/plants`

**功能描述**:
- 药用植物列表展示（卡片网格布局）
- 分类筛选（根茎类、全草类、藤本类等）
- 用法方式筛选（内服、外用、药浴）
- 关键词搜索（支持名称、功效、侗语名）
- 植物详情查看（对话框）
- 收藏功能

**核心代码解析**:

```vue
<script setup>
import { computed, inject, onMounted, ref, watch } from "vue"
import { useRoute } from "vue-router"
import { useDebounceFn } from "@/composables/useDebounce"
import { useUserStore } from "@/stores/user"

const route = useRoute()
const request = inject("request")
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const totalItems = ref(0)

// 筛选参数
const keyword = ref("")
const catFilter = ref("")
const useFilter = ref("")

// 筛选配置
const filterConfig = [
  { key: "category", label: "类型", options: [
    { label: "全部", value: "" },
    { label: "根茎类", value: "根茎类" },
    { label: "全草类", value: "全草类" },
    { label: "藤本类", value: "藤本类" },
    { label: "叶类", value: "叶类" },
    { label: "花类", value: "花类" },
    { label: "皮类", value: "皮类" },
    { label: "菌类", value: "菌类" }
  ]},
  { key: "usage", label: "用法", type: "success", options: [
    { label: "全部", value: "" },
    { label: "内服", value: "内服" },
    { label: "外用", value: "外用" },
    { label: "药浴", value: "药浴" }
  ]}
]

// 防抖搜索
const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1
  loadPlantsData()
}, 300)

// 加载数据
const loadPlantsData = async () => {
  pageLoading.value = true
  try {
    // 并行请求：分页数据 + 统计数据
    const [pageRes, allRes] = await Promise.all([
      request.get("/plants/list", {
        params: {
          page: currentPage.value,
          size: pageSize.value,
          keyword: keyword.value,
          category: catFilter.value,
          usageWay: useFilter.value
        }
      }),
      request.get("/plants/list", { params: { page: 1, size: 9999 } })
    ])
    
    allPlants.value = pageRes.data?.records || []
    totalItems.value = pageRes.data?.total || 0
    allPlantsForStats.value = allRes.data?.records || []
    
    // 加载用户收藏
    if (isLoggedIn.value) {
      const favRes = await request.get("/favorites/my")
      favorites.value = favRes.data || []
    }
  } finally {
    pageLoading.value = false
  }
}

onMounted(loadPlantsData)
</script>
```

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/plants/list` | GET | 获取植物列表 |
| `/plants/{id}/view` | POST | 增加浏览次数 |
| `/favorites/my` | GET | 获取我的收藏 |
| `/favorites/plant/{id}` | POST/DELETE | 添加/取消收藏 |

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `PlantDetailDialog.vue` - 植物详情对话框
- `Pagination.vue` - 分页
- `PageSidebar.vue` - 侧边栏
- `UpdateLogCard.vue` - 更新日志

---

### 3. Inheritors.vue - 传承人页面

**路由**: `/inheritors`

**功能描述**:
- 传承人列表展示
- 按级别筛选（国家级/省级/市级/县级）
- 按排序方式查看（按姓名、从业年限、热度）
- 关键词搜索
- 传承人详情查看
- 收藏功能

**核心代码解析**:

```vue
<script setup>
// 级别筛选配置
const levelOptions = [
  { label: "全部级别", value: "" },
  { label: "国家级", value: "国家级" },
  { label: "省级", value: "省级" },
  { label: "市级", value: "市级" },
  { label: "县级", value: "县级" }
]

// 排序配置
const sortByOptions = [
  { label: "按姓名", value: "name" },
  { label: "按从业年限", value: "experience" },
  { label: "按热度", value: "popularity" }
]

// 加载传承人数据
const loadInheritorsData = async () => {
  const res = await request.get("/inheritors/list", {
    params: {
      page: currentPage.value,
      size: pageSize.value,
      level: levelFilter.value,
      sortBy: sortBy.value
    }
  })
  allInheritors.value = res.data?.records || []
  totalItems.value = res.data?.total || 0
}
</script>
```

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/inheritors/list` | GET | 获取传承人列表 |
| `/inheritors/{id}` | GET | 获取传承人详情 |
| `/inheritors/{id}/view` | POST | 增加浏览次数 |

---

### 4. Knowledge.vue - 知识库页面

**路由**: `/knowledge`

**功能描述**:
- 知识条目列表展示
- 分类筛选（疗法分类、疾病分类、药材分类）
- 关键词搜索
- 知识详情查看
- 收藏功能

**核心代码解析**:

```vue
<script setup>
// 筛选配置
const filterConfig = [
  { key: "therapy", label: "疗法", options: [
    { label: "全部", value: "" },
    { label: "药浴疗法", value: "药浴疗法" },
    { label: "艾灸疗法", value: "艾灸疗法" },
    { label: "推拿疗法", value: "推拿疗法" }
  ]},
  { key: "disease", label: "疾病", type: "success", options: [
    { label: "全部", value: "" },
    { label: "风湿骨痛", value: "风湿骨痛" },
    { label: "妇科疾病", value: "妇科疾病" },
    { label: "儿科疾病", value: "儿科疾病" }
  ]},
  { key: "herb", label: "药材", type: "warning", options: [
    { label: "全部", value: "" },
    { label: "根茎类", value: "根茎类" },
    { label: "全草类", value: "全草类" },
    { label: "叶类", value: "叶类" },
    { label: "花类", value: "花类" },
    { label: "果实种子类", value: "果实种子类" }
  ]}
]

// 加载知识数据
const loadKnowledgeData = async () => {
  const res = await request.get("/knowledge/list", {
    params: {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value,
      therapy: therapyFilter.value,
      disease: diseaseFilter.value,
      herb: herbFilter.value
    }
  })
  allKnowledge.value = res.data?.records || []
  totalItems.value = res.data?.total || 0
}
</script>
```

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/knowledge/list` | GET | 获取知识列表 |
| `/knowledge/{id}` | GET | 获取知识详情 |
| `/knowledge/{id}/view` | POST | 增加浏览次数 |
| `/favorites/knowledge/{id}` | POST/DELETE | 添加/取消收藏 |

---

### 5. Interact.vue - 互动专区页面

**路由**: `/interact`

**功能描述**:
- 趣味答题模块（支持难度选择、计时、评分）
- 植物识别游戏（根据图片识别药材名称）
- 评论交流（支持回复、点赞）
- 排行榜展示

**核心代码解析**:

```vue
<script setup>
import { useQuiz } from "@/composables/useQuiz"
import { usePlantGame } from "@/composables/usePlantGame"
import { useComments } from "@/composables/useInteraction"

const request = inject("request")
const isLoggedIn = inject("isLoggedIn")

// 答题功能
const {
  isQuizStarted, selectedQuestions, userAnswers, currentQuestion,
  quizFinished, finalScore, correctCount, selectedDifficulty,
  startNewQuiz, resetQuiz, nextQuestion, prevQuestion, submitQuiz
} = useQuiz(request, isLoggedIn)

// 植物游戏功能
const {
  difficulty, currentPlant, options, answered, selectedAnswer,
  gameScore, streak, gameFinished, correctAnswers, totalQuestions,
  setDifficulty, checkAnswer, resetGame, submitGameScore
} = usePlantGame(request, isLoggedIn)

// 评论功能
const {
  comments, loadComments, handleCommentPost
} = useComments(request, isLoggedIn)

// 提交答题结果
const handleQuizSubmit = async () => {
  await submitQuiz()
  sidebarRef.value?.refreshLeaderboard()
}

// 提交游戏结果
const handleGameSubmit = async () => {
  await submitGameScore()
  sidebarRef.value?.refreshLeaderboard()
}

onMounted(async () => {
  await loadPlants()
  if (isLoggedIn.value) {
    await loadQuizRecords()
    await loadGameRecords()
  }
  await loadComments()
})
</script>
```

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/quiz/questions` | GET | 获取随机题目 |
| `/quiz/submit` | POST | 提交答案 |
| `/quiz/records` | GET | 获取答题记录 |
| `/plant-game/submit` | POST | 提交游戏结果 |
| `/plant-game/records` | GET | 获取游戏记录 |
| `/comments/list/{type}/{id}` | GET | 获取评论列表 |
| `/comments` | POST | 发表评论 |
| `/leaderboard/combined` | GET | 获取综合排行榜 |

**依赖组件**:
- `QuizSection.vue` - 答题组件
- `PlantGame.vue` - 植物游戏
- `CommentSection.vue` - 评论组件
- `InteractSidebar.vue` - 互动侧边栏

---

### 6. Resources.vue - 学习资源页面

**路由**: `/resources`

**功能描述**:
- 学习资源列表展示
- 分类筛选（视频/文档/图片）
- 文件类型筛选
- 资源预览（视频播放、文档预览）
- 资源下载
- 收藏功能

**核心代码解析**:

```vue
<script setup>
// 文件类型配置
const fileTypes = [
  { type: "video", name: "视频", extensions: ["mp4", "avi", "mov", "wmv", "flv", "mkv"] },
  { type: "document", name: "文档", extensions: ["docx", "doc", "pdf", "pptx", "ppt", "xlsx", "xls", "txt"] },
  { type: "image", name: "图片", extensions: ["jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"] }
]

// 加载资源数据
const loadResourcesData = async () => {
  const res = await request.get("/resources/list", {
    params: {
      page: currentPage.value,
      size: pageSize.value,
      category: categoryFilter.value,
      keyword: keyword.value,
      fileType: fileTypeFilter.value
    }
  })
  allResources.value = res.data?.records || []
  totalItems.value = res.data?.total || 0
}

// 下载资源
const downloadResource = (resource) => {
  window.open(`/api/resources/download/${resource.id}`)
}
</script>
```

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/resources/list` | GET | 获取资源列表 |
| `/resources/hot` | GET | 获取热门资源 |
| `/resources/{id}` | GET | 获取资源详情 |
| `/resources/download/{id}` | GET | 下载资源文件 |

---

### 7. PersonalCenter.vue - 个人中心页面

**路由**: `/personal` (需登录)

**功能描述**:
- 用户信息展示与修改
- 我的收藏管理
- 答题记录查看
- 游戏记录查看
- 评论历史查看
- 密码修改

**核心代码解析**:

```vue
<script setup>
import { useUserStore } from "@/stores/user"
import { usePersonalCenter } from "@/composables/usePersonalCenter"

const userStore = useUserStore()
const {
  userInfo, favorites, quizRecords, gameRecords, comments,
  activeTab, loading,
  fetchUserInfo, fetchFavorites, fetchQuizRecords, fetchGameRecords, fetchComments,
  changePassword
} = usePersonalCenter()

onMounted(async () => {
  await fetchUserInfo()
  await fetchFavorites()
  await fetchQuizRecords()
  await fetchGameRecords()
  await fetchComments()
})
</script>
```

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/user/me` | GET | 获取用户信息 |
| `/favorites/my` | GET | 获取我的收藏 |
| `/quiz/records` | GET | 获取答题记录 |
| `/plant-game/records` | GET | 获取游戏记录 |
| `/comments/my` | GET | 获取我的评论 |
| `/user/change-password` | POST | 修改密码 |

---

### 8. Admin.vue - 管理后台页面

**路由**: `/admin` (需管理员权限)

**功能描述**:
- 数据统计仪表盘
- 用户管理（列表、封禁、角色分配）
- 内容管理（植物、知识、传承人、资源、问答）
- 评论审核
- 反馈处理
- 操作日志查看

**核心代码解析**:

```vue
<script setup>
import { useAdminData } from "@/composables/useAdminData"

const {
  activeModule, loading, tableData, pagination, dialogVisible, currentData,
  loadUsers, loadPlants, loadKnowledge, loadInheritors, loadResources, loadQa,
  loadComments, loadFeedback, loadLogs,
  handleAdd, handleEdit, handleDelete, handleSave
} = useAdminData()

// 统计数据
const stats = ref({
  users: 0, knowledge: 0, inheritors: 0, plants: 0,
  qa: 0, resources: 0, quiz: 0, comments: 0, feedback: 0
})

// 加载统计数据
const loadStats = async () => {
  const res = await request.get("/admin/stats")
  stats.value = res.data || {}
}

onMounted(loadStats)
</script>
```

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/admin/stats` | GET | 获取统计数据 |
| `/admin/users` | GET | 获取用户列表 |
| `/admin/users/{id}/ban` | PUT | 封禁用户 |
| `/admin/users/{id}/unban` | PUT | 解封用户 |
| `/admin/plants` | GET/POST/PUT/DELETE | 植物管理 |
| `/admin/knowledge` | GET/POST/PUT/DELETE | 知识管理 |
| `/admin/inheritors` | GET/POST/PUT/DELETE | 传承人管理 |
| `/admin/comments/{id}/approve` | PUT | 审核通过评论 |
| `/admin/comments/{id}/reject` | PUT | 拒绝评论 |
| `/admin/feedback/{id}/reply` | PUT | 回复反馈 |

**依赖组件**:
- `AdminDashboard.vue` - 仪表盘
- `AdminDataTable.vue` - 数据表格
- `AdminSidebar.vue` - 侧边栏
- `admin/dialogs/*` - 详情对话框
- `admin/forms/*` - 表单对话框

---

### 9. About.vue - 关于页面

**路由**: `/about`

**功能描述**:
- 选题背景介绍
- 平台特色说明
- 功能模块介绍
- 侗医文化背景

---

### 10. Feedback.vue - 意见反馈页面

**路由**: `/feedback`

**功能描述**:
- 反馈表单提交
- 反馈类型选择（功能建议、问题反馈、其他）
- 反馈记录查看

**API调用**:
| 接口 | 方法 | 说明 |
|------|------|------|
| `/feedback` | POST | 提交反馈 |
| `/feedback/my` | GET | 获取我的反馈 |

---

### 11. GlobalSearch.vue - 全局搜索页面

**路由**: `/search`

**功能描述**:
- 跨模块统一搜索（植物、知识、传承人、问答、资源）
- 搜索结果分类展示
- 搜索历史记录

**核心代码解析**:

```vue
<script setup>
const searchKeyword = ref("")
const searchResults = ref({
  plants: [], knowledge: [], inheritors: [], qa: [], resources: []
})

// 全局搜索
const globalSearch = async () => {
  if (!searchKeyword.value.trim()) return
  
  const [plants, knowledge, inheritors, qa, resources] = await Promise.all([
    request.get('/plants/search', { params: { keyword: searchKeyword.value, size: 5 } }),
    request.get('/knowledge/search', { params: { keyword: searchKeyword.value, size: 5 } }),
    request.get('/inheritors/search', { params: { keyword: searchKeyword.value, size: 5 } }),
    request.get('/qa/search', { params: { keyword: searchKeyword.value, size: 5 } }),
    request.get('/resources/search', { params: { keyword: searchKeyword.value, size: 5 } })
  ])
  
  searchResults.value = {
    plants: plants.data?.records || [],
    knowledge: knowledge.data?.records || [],
    inheritors: inheritors.data?.records || [],
    qa: qa.data?.records || [],
    resources: resources.data?.records || []
  }
}
</script>
```

---

### 12. NotFound.vue - 404页面

**路由**: `/:pathMatch(.*)*`

**功能描述**:
- 404错误提示
- 返回首页链接

---

## 页面权限配置

| 页面 | 路由 | 权限要求 |
|------|------|----------|
| Home.vue | `/` | 无 |
| Plants.vue | `/plants` | 无 |
| Inheritors.vue | `/inheritors` | 无 |
| Knowledge.vue | `/knowledge` | 无 |
| Qa.vue | `/qa` | 无 |
| Resources.vue | `/resources` | 无 |
| Interact.vue | `/interact` | 无 |
| Visual.vue | `/visual` | 无 |
| PersonalCenter.vue | `/personal` | 需登录 |
| Admin.vue | `/admin` | 需登录 + 管理员权限 |
| About.vue | `/about` | 无 |
| Feedback.vue | `/feedback` | 无 |
| GlobalSearch.vue | `/search` | 无 |
| NotFound.vue | `/:pathMatch(.*)*` | 无 |

---

## 开发规范

### 1. 页面结构规范

```vue
<template>
  <div class="page-name">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>页面标题</h1>
    </div>
    
    <!-- 页面主体 -->
    <div class="page-content">
      <!-- 内容 -->
    </div>
  </div>
</template>

<script setup>
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

// 2. 注入和状态
const route = useRoute()
const request = inject('request')

// 3. 响应式数据
const loading = ref(false)
const dataList = ref([])

// 4. 方法
const fetchData = async () => { /* ... */ }

// 5. 生命周期
onMounted(fetchData)
</script>

<style scoped>
.page-name {
  /* 样式 */
}
</style>
```

### 2. 命名规范

- 页面文件使用大驼峰命名法
- CSS类名使用小写连字符命名法
- 变量使用小驼峰命名法

### 3. 性能优化

- 使用 `v-if` 和 `v-show` 合理控制渲染
- 使用 `computed` 缓存计算结果
- 使用 `useDebounceFn` 处理频繁操作
- 使用 `v-loading` 显示加载状态

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 框架基础 |
| Vue Router | 4.2+ | 路由管理 |
| Pinia | 2.3+ | 状态管理 |
| Element Plus | 2.4+ | UI组件 |
| ECharts | 5.4+ | 图表展示 |

---

**最后更新时间**：2026年3月30日
