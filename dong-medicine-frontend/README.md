# 侗乡医药数字展示平台前端

> 基于 Vue 3 + Vite 的侗族医药文化遗产数字化展示平台前端应用

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构详解](#项目结构详解)
- [页面组件详解](#页面组件详解)
- [业务组件详解](#业务组件详解)
- [路由配置详解](#路由配置详解)
- [状态管理详解](#状态管理详解)
- [API请求封装](#api请求封装)
- [样式系统详解](#样式系统详解)
- [组合式函数详解](#组合式函数详解)
- [快速开始](#快速开始)

---

## 项目概述

本项目是侗乡医药数字展示平台的前端应用，提供用户界面展示、交互功能、数据可视化等功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 信息展示 | 植物、传承人、知识、资源展示 |
| 用户交互 | 评论、收藏、反馈 |
| 游戏化学习 | 趣味答题、植物识别游戏 |
| 后台管理 | 内容管理、用户管理、数据统计 |

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 前端框架，使用 Composition API |
| Vite | 5.0+ | 构建工具，开发服务器 |
| Vue Router | 4.2+ | 路由管理，支持路由守卫 |
| Pinia | 2.3+ | 状态管理，替代 Vuex |
| Element Plus | 2.4+ | UI 组件库 |
| Axios | 1.6+ | HTTP 请求客户端 |
| ECharts | 5.4+ | 数据可视化图表 |
| Vitest | 1.0+ | 单元测试框架 |

---

## 项目结构详解

```
dong-medicine-frontend/
│
├── public/                              # 静态资源目录
│   └── favicon.ico                      # 网站图标
│
├── src/
│   │
│   ├── views/                           # 页面组件 (13个)
│   │   │
│   │   ├── Home.vue                     # 首页
│   │   │   # 展示平台统计数据
│   │   │   # 传承人风采轮播
│   │   │   # 快速导航入口
│   │   │   # 热门内容推荐
│   │   │   #
│   │   ├── Plants.vue                   # 药用植物页面
│   │   │   # 植物卡片网格展示
│   │   │   # 分类筛选（根茎类、叶类等）
│   │   │   # 用法方式筛选
│   │   │   # 搜索功能
│   │   │   # 收藏功能
│   │   │   #
│   │   ├── Inheritors.vue               # 传承人页面
│   │   │   # 传承人卡片展示
│   │   │   # 等级筛选（国家/省/市/县级）
│   │   │   # 详情查看
│   │   │   #
│   │   ├── Knowledge.vue                # 知识库页面
│   │   │   # 知识条目列表
│   │   │   # 疗法分类筛选
│   │   │   # 疾病分类筛选
│   │   │   # 在线预览
│   │   │   #
│   │   ├── Qa.vue                       # 问答社区页面
│   │   │   # 问答列表
│   │   │   # AI智能回答
│   │   │   # 提问功能
│   │   │   #
│   │   ├── Resources.vue                # 学习资源页面
│   │   │   # 资源分类展示
│   │   │   # 视频/文档预览
│   │   │   # 下载功能
│   │   │   #
│   │   ├── Interact.vue                 # 互动专区页面
│   │   │   # 趣味答题模块
│   │   │   # 植物识别游戏
│   │   │   # 评论交流
│   │   │   #
│   │   ├── Visual.vue                   # 数据可视化页面
│   │   │   # ECharts图表
│   │   │   # 统计数据展示
│   │   │   #
│   │   ├── PersonalCenter.vue           # 个人中心页面
│   │   │   # 用户信息管理
│   │   │   # 收藏列表
│   │   │   # 答题记录
│   │   │   # 密码修改
│   │   │   #
│   │   ├── Admin.vue                    # 管理后台页面
│   │   │   # 仪表盘
│   │   │   # 用户管理
│   │   │   # 内容管理
│   │   │   # 评论审核
│   │   │   # 操作日志
│   │   │   #
│   │   ├── About.vue                    # 关于页面
│   │   ├── Feedback.vue                 # 意见反馈页面
│   │   └── GlobalSearch.vue             # 全局搜索页面
│   │
│   ├── components/                      # 组件目录
│   │   │
│   │   ├── base/                        # 基础组件
│   │   │   └── ErrorBoundary.vue        # 错误边界组件
│   │   │       # 捕获子组件错误
│   │   │       # 显示错误提示
│   │   │       # 提供重试功能
│   │   │       #
│   │   └── business/                    # 业务组件
│   │       │
│   │       ├── layout/                  # 布局组件
│   │       │   │
│   │       │   ├── AppHeader.vue        # 顶部导航栏
│   │       │   │   # Logo展示
│   │       │   │   # 导航菜单
│   │       │   │   # 用户状态显示
│   │       │   │   # 登录/注册入口
│   │       │   │   # 全局搜索入口
│   │       │   │   #
│   │       │   └── AppFooter.vue        # 底部版权
│   │       │       # 版权信息
│   │       │       # 友情链接
│   │       │       #
│   │       ├── display/                 # 展示组件
│   │       │   │
│   │       │   ├── CardGrid.vue         # 卡片网格组件
│   │       │   │   # Props: items, columns, loading
│   │       │   │   # 响应式网格布局
│   │       │   │   # 加载骨架屏
│   │       │   │   #
│   │       │   ├── ChartCard.vue        # 图表卡片组件
│   │       │   │   # Props: title, chartOption
│   │       │   │   # ECharts封装
│   │       │   │   # 自动resize
│   │       │   │   #
│   │       │   ├── PageSidebar.vue      # 页面侧边栏
│   │       │   │   # 统计数据展示
│   │       │   │   # 热门内容列表
│   │       │   │   #
│   │       │   ├── Pagination.vue       # 分页组件
│   │       │   │   # Props: total, page, pageSize
│   │       │   │   # Emits: change
│   │       │   │   #
│   │       │   ├── SearchFilter.vue     # 搜索过滤组件
│   │       │   │   # 搜索输入框
│   │       │   │   # 分类下拉选择
│   │       │   │   # 重置功能
│   │       │   │   #
│   │       │   ├── UpdateLogCard.vue    # 更新日志卡片
│   │       │   └── UpdateLogDialog.vue  # 更新日志对话框
│   │       │
│   │       ├── interact/                # 交互组件
│   │       │   │
│   │       │   ├── CommentSection.vue   # 评论区域组件
│   │       │   │   # Props: targetType, targetId
│   │       │   │   # 评论列表展示
│   │       │   │   # 发表评论
│   │       │   │   # 回复功能
│   │       │   │   #
│   │       │   ├── InteractSidebar.vue  # 互动侧边栏
│   │       │   │   # 排行榜
│   │       │   │   # 游戏入口
│   │       │   │   #
│   │       │   ├── PlantGame.vue        # 植物识别游戏
│   │       │   │   # 难度选择
│   │       │   │   # 图片展示
│   │       │   │   # 选项选择
│   │       │   │   # 计分系统
│   │       │   │   # 连击奖励
│   │       │   │   #
│   │       │   └── QuizSection.vue      # 趣味答题组件
│   │       │       # 题目展示
│   │       │       # 计时功能
│   │       │       # 答案提交
│   │       │       # 成绩展示
│   │       │       #
│   │       ├── media/                   # 媒体组件
│   │       │   │
│   │       │   ├── ImageCarousel.vue    # 图片轮播组件
│   │       │   │   # Props: images
│   │       │   │   # 自动播放
│   │       │   │   # 缩略图导航
│   │       │   │   #
│   │       │   ├── VideoPlayer.vue      # 视频播放器
│   │       │   │   # Props: src, poster
│   │       │   │   # 播放控制
│   │       │   │   # 进度条
│   │       │   │   #
│   │       │   ├── DocumentPreview.vue  # 文档预览组件
│   │       │   │   # Props: file
│   │       │   │   # PDF预览
│   │       │   │   # Word预览
│   │       │   │   #
│   │       │   ├── DocumentList.vue     # 文档列表组件
│   │       │   └── MediaDisplay.vue     # 媒体展示组件
│   │       │
│   │       ├── upload/                  # 上传组件
│   │       │   │
│   │       │   ├── ImageUploader.vue    # 图片上传组件
│   │       │   │   # Props: limit, accept
│   │       │   │   # 图片预览
│   │       │   │   # 大小限制
│   │       │   │   # 批量上传
│   │       │   │   #
│   │       │   ├── VideoUploader.vue    # 视频上传组件
│   │       │   ├── DocumentUploader.vue # 文档上传组件
│   │       │   └── FileUploader.vue     # 通用文件上传
│   │       │
│   │       ├── dialogs/                 # 详情对话框组件
│   │       │   │
│   │       │   ├── PlantDetailDialog.vue     # 植物详情对话框
│   │       │   │   # Props: visible, plant
│   │       │   │   # 图片轮播
│   │       │   │   # 基本信息
│   │       │   │   # 功效说明
│   │       │   │   # 收藏按钮
│   │       │   │   #
│   │       │   ├── KnowledgeDetailDialog.vue # 知识详情对话框
│   │       │   ├── InheritorDetailDialog.vue # 传承人详情对话框
│   │       │   ├── QuizDetailDialog.vue      # 答题详情对话框
│   │       │   └── ResourceDetailDialog.vue  # 资源详情对话框
│   │       │
│   │       └── admin/                   # 管理后台组件
│   │           │
│   │           ├── AdminDashboard.vue   # 管理仪表盘
│   │           │   # 统计卡片
│   │           │   # 图表展示
│   │           │   # 快捷操作
│   │           │   #
│   │           ├── AdminSidebar.vue     # 管理侧边栏
│   │           │   # 菜单导航
│   │           │   # 权限控制
│   │           │   #
│   │           ├── AdminDataTable.vue   # 数据表格组件
│   │           │   # Props: columns, data
│   │           │   # 排序、筛选
│   │           │   # 分页
│   │           │   # 操作按钮
│   │           │   #
│   │           ├── dialogs/             # 管理详情对话框
│   │           │   ├── UserDetailDialog.vue
│   │           │   ├── CommentDetailDialog.vue
│   │           │   ├── FeedbackDetailDialog.vue
│   │           │   └── LogDetailDialog.vue
│   │           │
│   │           └── forms/               # 管理表单对话框
│   │               ├── KnowledgeFormDialog.vue
│   │               ├── InheritorFormDialog.vue
│   │               ├── PlantFormDialog.vue
│   │               ├── QaFormDialog.vue
│   │               ├── ResourceFormDialog.vue
│   │               └── QuizQuestionFormDialog.vue
│   │
│   ├── composables/                     # 组合式函数
│   │   │
│   │   ├── useAdminData.js              # 管理后台数据
│   │   │   # 数据获取
│   │   │   # 对话框管理
│   │   │   # CRUD操作
│   │   │   #
│   │   ├── useQuiz.js                   # 答题逻辑
│   │   │   # 题目加载
│   │   │   # 计时器
│   │   │   # 答案提交
│   │   │   # 成绩计算
│   │   │   #
│   │   ├── usePlantGame.js              # 植物游戏逻辑
│   │   │   # 难度设置
│   │   │   # 游戏数据
│   │   │   # 计分系统
│   │   │   # 连击奖励
│   │   │   #
│   │   ├── useInteraction.js            # 交互功能
│   │   │   # 倒计时
│   │   │   # 评论处理
│   │   │   # 分页逻辑
│   │   │   #
│   │   ├── usePersonalCenter.js         # 个人中心功能
│   │   ├── useFavorite.js               # 收藏功能
│   │   ├── useMedia.js                  # 媒体处理
│   │   ├── useUpdateLog.js              # 更新日志
│   │   └── useFormDialog.js             # 表单对话框
│   │
│   ├── router/                          # 路由配置
│   │   └── index.js                     # 路由定义 + 守卫
│   │       # 路由表定义
│   │       # 路由守卫
│   │       # 权限验证
│   │       #
│   │
│   ├── stores/                          # 状态管理
│   │   └── user.js                      # 用户状态
│   │       # State: token, userId, username, role
│   │       # Getters: isLoggedIn, isAdmin
│   │       # Actions: login, logout, validateToken
│   │       #
│   │
│   ├── utils/                           # 工具函数
│   │   │
│   │   ├── index.js                     # 工具函数导出
│   │   │   # formatTime() - 时间格式化
│   │   │   # extractData() - 响应数据提取
│   │   │   # formatFileSize() - 文件大小格式化
│   │   │   # truncate() - 文本截断
│   │   │   # debounce() - 防抖
│   │   │   # throttle() - 节流
│   │   │   # deepClone() - 深拷贝
│   │   │   # isEmpty() - 空值判断
│   │   │   # generateId() - 生成唯一ID
│   │   │   #
│   │   ├── request.js                   # Axios请求封装
│   │   │   # Axios实例配置
│   │   │   # 请求拦截器
│   │   │   # 响应拦截器
│   │   │   # 错误处理
│   │   │   # 重试机制
│   │   │   #
│   │   ├── media.js                     # 媒体工具
│   │   │   # getMediaType() - 获取媒体类型
│   │   │   # isImage() - 判断图片
│   │   │   # isVideo() - 判断视频
│   │   │   #
│   │   ├── xss.js                       # XSS防护
│   │   │   # sanitize() - 清理危险字符
│   │   │   #
│   │   ├── logger.js                    # 日志工具
│   │   │   # log(), warn(), error()
│   │   │   #
│   │   └── adminUtils.js                # 管理后台工具
│   │
│   ├── styles/                          # 样式文件
│   │   │
│   │   ├── index.css                    # 样式入口
│   │   │   # 统一导入所有样式
│   │   │   #
│   │   ├── variables.css                # CSS变量定义
│   │   │   # 品牌色
│   │   │   # 功能色
│   │   │   # 间距系统
│   │   │   # 圆角系统
│   │   │   # 阴影系统
│   │   │   #
│   │   ├── base.css                     # 基础样式
│   │   │   # 重置样式
│   │   │   # 排版样式
│   │   │   # 布局样式
│   │   │   #
│   │   ├── components.css               # 组件样式
│   │   │   # 卡片样式
│   │   │   # 按钮样式
│   │   │   # 表单样式
│   │   │   #
│   │   ├── pages.css                    # 页面样式
│   │   ├── common.css                   # 通用样式
│   │   ├── dialog-common.css            # 对话框样式
│   │   └── media-common.css             # 媒体样式
│   │
│   ├── config/                          # 配置文件
│   │   └── index.js                     # 应用配置
│   │       # API基础路径
│   │       # 分页配置
│   │       # 上传限制
│   │       #
│   │
│   ├── App.vue                          # 根组件
│   │   # 布局结构
│   │   # 路由视图
│   │   # 全局组件
│   │   #
│   │
│   └── main.js                          # 入口文件
│       # 创建Vue应用
│       # 注册插件
│       # 挂载应用
│
├── index.html                           # HTML模板
├── package.json                         # 项目配置
├── vite.config.js                       # Vite配置
└── vitest.config.js                     # 测试配置
```

---

## 页面组件详解

### Home.vue - 首页

```vue
<template>
  <div class="home-page">
    <!-- 统计数据卡片 -->
    <div class="stats-section">
      <StatsCard :count="plantCount" label="药用植物" />
      <StatsCard :count="inheritorCount" label="传承人" />
      <StatsCard :count="knowledgeCount" label="知识条目" />
    </div>
    
    <!-- 传承人风采轮播 -->
    <InheritorCarousel :inheritors="featuredInheritors" />
    
    <!-- 快速导航 -->
    <QuickNav :items="navItems" />
    
    <!-- 热门内容 -->
    <HotContent :plants="hotPlants" :knowledge="hotKnowledge" />
  </div>
</template>

<script setup>
// 主要逻辑
const plantCount = ref(0)
const inheritorCount = ref(0)
const knowledgeCount = ref(0)

// 获取统计数据
onMounted(async () => {
  const stats = await fetchStats()
  plantCount.value = stats.plantCount
  // ...
})
</script>
```

### Plants.vue - 药用植物页面

```vue
<template>
  <div class="plants-page">
    <!-- 搜索过滤 -->
    <SearchFilter 
      v-model:keyword="keyword"
      v-model:category="category"
      v-model:usageWay="usageWay"
      :categories="categories"
      @search="handleSearch"
    />
    
    <!-- 植物卡片网格 -->
    <CardGrid :items="plants" :loading="loading">
      <template #default="{ item }">
        <PlantCard :plant="item" @click="showDetail(item)" />
      </template>
    </CardGrid>
    
    <!-- 分页 -->
    <Pagination :total="total" v-model:page="page" />
    
    <!-- 详情对话框 -->
    <PlantDetailDialog 
      v-model:visible="detailVisible" 
      :plant="currentPlant" 
    />
  </div>
</template>

<script setup>
// 主要变量
const plants = ref([])
const keyword = ref('')
const category = ref('')
const usageWay = ref('')

// 获取植物列表
const fetchPlants = async () => {
  loading.value = true
  const res = await request.get('/plants/list', {
    params: { category: category.value, usageWay: usageWay.value }
  })
  plants.value = res.data
}
</script>
```

---

## 业务组件详解

### PlantGame.vue - 植物识别游戏

```vue
<template>
  <div class="plant-game">
    <!-- 难度选择 -->
    <div class="difficulty-selector">
      <el-radio-group v-model="difficulty">
        <el-radio label="easy">简单</el-radio>
        <el-radio label="medium">中等</el-radio>
        <el-radio label="hard">困难</el-radio>
      </el-radio-group>
    </div>
    
    <!-- 游戏区域 -->
    <div class="game-area">
      <!-- 植物图片 -->
      <img :src="currentPlant?.images?.[0]" class="plant-image" />
      
      <!-- 选项 -->
      <div class="options">
        <el-button 
          v-for="option in options" 
          :key="option.id"
          @click="selectAnswer(option)"
        >
          {{ option.nameCn }}
        </el-button>
      </div>
    </div>
    
    <!-- 分数显示 -->
    <div class="score-board">
      <span>得分: {{ score }}</span>
      <span>连击: {{ combo }}</span>
    </div>
  </div>
</template>

<script setup>
// 使用组合式函数
const { 
  difficulty, 
  currentPlant, 
  options, 
  score, 
  combo,
  selectAnswer 
} = usePlantGame()
</script>
```

### CommentSection.vue - 评论区域

```vue
<template>
  <div class="comment-section">
    <!-- 发表评论 -->
    <div class="comment-input">
      <el-input 
        v-model="newComment" 
        type="textarea"
        placeholder="发表评论..."
      />
      <el-button @click="submitComment">发表</el-button>
    </div>
    
    <!-- 评论列表 -->
    <div class="comment-list">
      <CommentItem 
        v-for="comment in comments" 
        :key="comment.id"
        :comment="comment"
        @reply="handleReply"
      />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  targetType: { type: String, required: true },
  targetId: { type: Number, required: true }
})

const comments = ref([])
const newComment = ref('')

// 获取评论
const fetchComments = async () => {
  const res = await request.get(`/comments/list/${props.targetType}/${props.targetId}`)
  comments.value = res.data
}

// 发表评论
const submitComment = async () => {
  await request.post('/comments', {
    targetType: props.targetType,
    targetId: props.targetId,
    content: newComment.value
  })
  newComment.value = ''
  fetchComments()
}
</script>
```

---

## 路由配置详解

### router/index.js

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由表
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/plants',
    name: 'Plants',
    component: () => import('@/views/Plants.vue'),
    meta: { title: '药用植物' }
  },
  {
    path: '/personal',
    name: 'Personal',
    component: () => import('@/views/PersonalCenter.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/Admin.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  }
  // ...其他路由
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Home', query: { login: 'required' } })
    return
  }
  
  // 需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

export default router
```

---

## 状态管理详解

### stores/user.js

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const userInfo = ref(null)
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')
  
  // Actions
  async function login(loginData) {
    const res = await request.post('/user/login', loginData)
    setAuth(res.data)
    return res
  }
  
  function setAuth(data) {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    role.value = data.role
    
    // 持久化到 localStorage
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
  }
  
  function clearAuth() {
    token.value = ''
    userId.value = ''
    username.value = ''
    role.value = ''
    userInfo.value = null
    
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }
  
  async function validateToken() {
    if (!token.value) return false
    try {
      const res = await request.get('/user/validate')
      return res.data.valid
    } catch {
      clearAuth()
      return false
    }
  }
  
  async function logout() {
    await request.post('/user/logout')
    clearAuth()
  }
  
  return {
    token, userId, username, role, userInfo,
    isLoggedIn, isAdmin,
    login, logout, setAuth, clearAuth, validateToken
  }
})
```

---

## API请求封装

### utils/request.js

```javascript
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { sanitize } from '@/utils/xss'

// 创建Axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    
    // 添加认证头
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
      config.headers['X-User-Id'] = userStore.userId
    }
    
    // XSS清理
    if (config.data) {
      config.data = sanitizeData(config.data)
    }
    
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  async (error) => {
    const { response, config } = error
    
    // 401 未授权
    if (response?.status === 401) {
      const userStore = useUserStore()
      userStore.clearAuth()
      window.location.href = '/'
      return Promise.reject(error)
    }
    
    // 重试机制
    if (!config._retry && config._retryCount < 3) {
      config._retry = true
      config._retryCount = (config._retryCount || 0) + 1
      await new Promise(r => setTimeout(r, 1000 * config._retryCount))
      return request(config)
    }
    
    return Promise.reject(error)
  }
)

export default request
```

---

## 样式系统详解

### styles/variables.css - CSS变量

```css
:root {
  /* 品牌色 - 侗族文化特色 */
  --dong-indigo: #1A5276;        /* 靛蓝 - 主色调 */
  --dong-jade: #28B463;          /* 青绿 - 辅助色 */
  --dong-gold: #c9a227;          /* 金铜 - 强调色 */
  
  /* 功能色 */
  --color-primary: #1A5276;
  --color-success: #28B463;
  --color-warning: #f5a623;
  --color-danger: #e74c3c;
  --color-info: #3498db;
  
  /* 文字颜色 */
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #C0C4CC;
  
  /* 背景颜色 */
  --bg-page: #f5f7fa;
  --bg-card: #ffffff;
  --bg-hover: #f5f7fa;
  
  /* 间距系统 */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 24px;
  --space-2xl: 32px;
  --space-3xl: 48px;
  --space-4xl: 64px;
  
  /* 圆角系统 */
  --radius-xs: 4px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  
  /* 阴影系统 */
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 8px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 8px 16px rgba(0, 0, 0, 0.1);
}
```

### 样式使用示例

```css
/* 使用CSS变量 */
.card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: var(--space-lg);
}

.button-primary {
  background: var(--color-primary);
  color: white;
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-sm);
}
```

---

## 组合式函数详解

### composables/useQuiz.js - 答题逻辑

```javascript
import { ref, computed } from 'vue'
import request from '@/utils/request'

export function useQuiz() {
  // State
  const questions = ref([])
  const currentIndex = ref(0)
  const answers = ref({})
  const timeLeft = ref(0)
  const isFinished = ref(false)
  const score = ref(0)
  
  // Getters
  const currentQuestion = computed(() => questions.value[currentIndex.value])
  const progress = computed(() => (currentIndex.value / questions.value.length) * 100)
  
  // Actions
  async function loadQuestions(count = 10) {
    const res = await request.get('/quiz/questions', { params: { count } })
    questions.value = res.data
    startTimer()
  }
  
  function startTimer() {
    timeLeft.value = 600 // 10分钟
    const timer = setInterval(() => {
      timeLeft.value--
      if (timeLeft.value <= 0) {
        clearInterval(timer)
        submit()
      }
    }, 1000)
  }
  
  function selectAnswer(questionId, answer) {
    answers.value[questionId] = answer
  }
  
  async function submit() {
    const res = await request.post('/quiz/submit', {
      answers: answers.value
    })
    score.value = res.data.score
    isFinished.value = true
  }
  
  return {
    questions, currentIndex, answers, timeLeft, isFinished, score,
    currentQuestion, progress,
    loadQuestions, selectAnswer, submit
  }
}
```

### composables/usePlantGame.js - 植物游戏逻辑

```javascript
import { ref, computed } from 'vue'
import request from '@/utils/request'

export function usePlantGame() {
  // State
  const difficulty = ref('medium')
  const currentPlant = ref(null)
  const options = ref([])
  const score = ref(0)
  const combo = ref(0)
  const correctCount = ref(0)
  
  // 难度配置
  const difficultyConfig = {
    easy: { optionCount: 3, timeLimit: 15, baseScore: 10 },
    medium: { optionCount: 4, timeLimit: 10, baseScore: 20 },
    hard: { optionCount: 5, timeLimit: 5, baseScore: 30 }
  }
  
  // 加载游戏数据
  async function loadGame() {
    const res = await request.get('/plants/random', {
      params: { count: difficultyConfig[difficulty.value].optionCount }
    })
    currentPlant.value = res.data[0]
    options.value = res.data
  }
  
  // 选择答案
  function selectAnswer(option) {
    if (option.id === currentPlant.value.id) {
      // 正确
      combo.value++
      correctCount.value++
      score.value += calculateScore()
    } else {
      // 错误
      combo.value = 0
    }
    loadGame()
  }
  
  // 计算分数（含连击奖励）
  function calculateScore() {
    const base = difficultyConfig[difficulty.value].baseScore
    const comboBonus = Math.min(combo.value * 5, 50)
    return base + comboBonus
  }
  
  return {
    difficulty, currentPlant, options, score, combo, correctCount,
    loadGame, selectAnswer
  }
}
```

---

## 快速开始

### 环境要求

- Node.js 18+
- npm 9+

### 安装步骤

```bash
# 1. 进入项目目录
cd dong-medicine-frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

### 常用命令

```bash
# 开发模式
npm run dev

# 代码检查
npm run lint

# 运行测试
npm run test:run

# 构建生产版本
npm run build
```

### 访问地址

- 开发地址: http://localhost:5173
- 生产构建: `dist/` 目录

---

## 目录说明

| 目录 | 说明 |
|------|------|
| `views/` | 页面组件，对应路由 |
| `components/` | 可复用组件 |
| `composables/` | 组合式函数，复用逻辑 |
| `stores/` | Pinia状态管理 |
| `router/` | 路由配置 |
| `utils/` | 工具函数 |
| `styles/` | 全局样式 |
| `config/` | 应用配置 |
