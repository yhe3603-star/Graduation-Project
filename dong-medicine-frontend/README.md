# 侗乡医药数字展示平台前端

> 基于 Vue 3 + Vite 的侗族医药文化遗产数字化展示平台前端应用

## 目录

- [项目概述](#项目概述)
- [新手入门指南](#新手入门指南)
- [Vue 3 基础概念](#vue-3-基础概念)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [页面组件](#页面组件)
- [业务组件](#业务组件)
- [组合式函数](#组合式函数)
- [路由配置](#路由配置)
- [状态管理](#状态管理)
- [工具函数](#工具函数)
- [安全机制](#安全机制)
- [样式系统](#样式系统)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)
- [常见问题](#常见问题)

---

## 项目概述

本项目是侗乡医药数字展示平台的前端应用，提供用户界面展示、交互功能、数据可视化等功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 信息展示 | 植物、传承人、知识、资源展示 |
| 用户交互 | 评论、收藏、反馈 |
| 游戏化学习 | 趣味答题、植物识别游戏 |
| 数据可视化 | ECharts 图表统计 |
| 后台管理 | 内容管理、用户管理、数据统计 |

---

## 新手入门指南

### 我应该从哪里开始？

如果你是第一次接触前端开发，建议按照以下顺序学习：

```
第1步：学习 HTML/CSS 基础 → 了解网页结构和样式
第2步：学习 JavaScript 基础 → 了解网页交互逻辑
第3步：学习 Vue 3 基础 → 阅读"Vue 3 基础概念"
第4步：理解项目结构 → 阅读"项目结构"
第5步：运行项目 → 按照"快速开始"操作
第6步：阅读代码 → 从简单组件开始，逐步深入
```

### 学习路线图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端学习路线图                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  基础阶段:                                                       │
│  HTML → CSS → JavaScript → ES6+语法                             │
│                                                                 │
│  框架阶段:                                                       │
│  Vue 3基础 → Vue Router → Pinia → 组件开发                       │
│                                                                 │
│  工具阶段:                                                       │
│  Vite → Element Plus → ECharts → Axios                          │
│                                                                 │
│  实战阶段:                                                       │
│  本项目代码阅读 → 修改组件 → 开发新功能                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 推荐学习资源

| 学习内容 | 推荐资源 | 链接 |
|---------|---------|------|
| HTML/CSS | 菜鸟教程 | https://www.runoob.com/html/html-tutorial.html |
| JavaScript | MDN Web Docs | https://developer.mozilla.org/zh-CN/docs/Web/JavaScript |
| Vue 3 | Vue.js 官方中文教程 | https://cn.vuejs.org/tutorial/ |
| Element Plus | Element Plus 文档 | https://element-plus.org/zh-CN/ |

---

## Vue 3 基础概念

### 什么是 Vue？

Vue 是一个用于构建用户界面的 JavaScript 框架。它基于标准 HTML、CSS 和 JavaScript 构建，并提供了一套声明式的、组件化的编程模型。

**通俗理解**：Vue 就像是一个"智能模板"，你可以告诉它"这里显示用户名"，当用户名变化时，它会自动更新显示。

### Vue 应用的基本结构

一个 Vue 应用由两部分组成：

```
┌─────────────────────────────────────────────────────────────────┐
│                        Vue 应用结构                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 模板 (Template) - 页面结构                                   │
│     ┌─────────────────────────────────────────────────────┐    │
│     │  <template>                                         │    │
│     │    <div class="greeting">                           │    │
│     │      <h1>{{ message }}</h1>                         │    │
│     │      <button @click="count++">点击了{{ count }}次</button>│
│     │    </div>                                           │    │
│     │  </template>                                        │    │
│     └─────────────────────────────────────────────────────┘    │
│                                                                 │
│  2. 脚本 (Script) - 逻辑代码                                     │
│     ┌─────────────────────────────────────────────────────┐    │
│     │  <script setup>                                     │    │
│     │    import { ref } from 'vue'                        │    │
│     │                                                     │    │
│     │    const message = ref('你好，Vue!')                 │    │
│     │    const count = ref(0)                             │    │
│     │  </script>                                          │    │
│     └─────────────────────────────────────────────────────┘    │
│                                                                 │
│  3. 样式 (Style) - 页面样式（可选）                               │
│     ┌─────────────────────────────────────────────────────┐    │
│     │  <style scoped>                                     │    │
│     │    .greeting {                                      │    │
│     │      color: blue;                                   │    │
│     │    }                                                │    │
│     │  </style>                                           │    │
│     └─────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 响应式数据 (ref 和 reactive)

Vue 的核心特性是**响应式系统** - 当数据变化时，页面自动更新。

#### ref - 用于基本类型

```vue
<template>
  <div>
    <p>用户名: {{ username }}</p>
    <button @click="changeName">修改名字</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// ref 用于创建响应式数据
// 基本类型（字符串、数字、布尔值）使用 ref
const username = ref('张三')

// 修改 ref 的值需要使用 .value
const changeName = () => {
  username.value = '李四'  // 页面会自动更新显示"李四"
}
</script>
```

**为什么需要 .value？**
- ref 把数据包装成一个对象，`.value` 是这个对象中存储实际值的属性
- Vue 通过这个包装对象来追踪数据变化

#### reactive - 用于对象类型

```vue
<template>
  <div>
    <p>姓名: {{ user.name }}</p>
    <p>年龄: {{ user.age }}</p>
    <button @click="user.age++">长大一岁</button>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

// reactive 用于创建响应式对象
// 对象类型使用 reactive，不需要 .value
const user = reactive({
  name: '张三',
  age: 25
})

// 直接修改对象属性，页面会自动更新
// user.age++ 就可以，不需要 user.value.age++
</script>
```

### 计算属性 (computed)

计算属性是根据其他数据计算得出的值，会自动缓存结果。

```vue
<template>
  <div>
    <p>姓: <input v-model="firstName"></p>
    <p>名: <input v-model="lastName"></p>
    <p>全名: {{ fullName }}</p>
    <!-- fullName 会自动根据 firstName 和 lastName 计算 -->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const firstName = ref('张')
const lastName = ref('三')

// 计算属性 - 自动计算，有缓存
// 只有 firstName 或 lastName 变化时才会重新计算
const fullName = computed(() => {
  return firstName.value + lastName.value
})
</script>
```

### 事件处理 (@click, @submit 等)

使用 `@` 符号绑定事件：

```vue
<template>
  <div>
    <!-- 点击事件 -->
    <button @click="handleClick">点击我</button>
    
    <!-- 传递参数 -->
    <button @click="handleDelete(1)">删除ID为1的项目</button>
    
    <!-- 访问事件对象 -->
    <button @click="handleEvent($event)">获取事件对象</button>
    
    <!-- 表单提交 -->
    <form @submit.prevent="handleSubmit">
      <input v-model="inputValue">
      <button type="submit">提交</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const inputValue = ref('')

const handleClick = () => {
  alert('按钮被点击了！')
}

const handleDelete = (id) => {
  console.log('删除项目:', id)
}

const handleEvent = (event) => {
  console.log('事件对象:', event.target)
}

const handleSubmit = () => {
  console.log('提交的数据:', inputValue.value)
  // .prevent 修饰符阻止了表单默认提交行为
}
</script>
```

### 条件渲染 (v-if, v-show)

根据条件显示或隐藏元素：

```vue
<template>
  <div>
    <!-- v-if: 条件为false时，元素不渲染到DOM中 -->
    <div v-if="isLoggedIn">
      欢迎回来，{{ username }}！
    </div>
    <div v-else>
      请先登录
    </div>
    
    <!-- v-show: 条件为false时，元素仍在DOM中，只是display:none -->
    <div v-show="hasNotification">
      你有新消息！
    </div>
    
    <!-- v-else-if -->
    <div v-if="score >= 90">优秀</div>
    <div v-else-if="score >= 60">及格</div>
    <div v-else>不及格</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const isLoggedIn = ref(false)
const username = ref('张三')
const hasNotification = ref(true)
const score = ref(85)
</script>
```

**v-if 和 v-show 的区别：**
- `v-if` 是"真正的"条件渲染，条件为false时元素不存在于DOM中
- `v-show` 只是切换 CSS `display` 属性，元素始终存在于DOM中
- 频繁切换用 `v-show`，条件很少改变用 `v-if`

### 列表渲染 (v-for)

循环渲染列表：

```vue
<template>
  <div>
    <!-- 遍历数组 -->
    <ul>
      <li v-for="item in items" :key="item.id">
        {{ item.name }} - ¥{{ item.price }}
      </li>
    </ul>
    
    <!-- 遍历对象 -->
    <div v-for="(value, key) in userInfo" :key="key">
      {{ key }}: {{ value }}
    </div>
    
    <!-- 遍历数字 -->
    <span v-for="n in 5" :key="n">{{ n }}</span>
    <!-- 输出: 1 2 3 4 5 -->
  </div>
</template>

<script setup>
import { ref } from 'vue'

const items = ref([
  { id: 1, name: '苹果', price: 5 },
  { id: 2, name: '香蕉', price: 3 },
  { id: 3, name: '橙子', price: 4 }
])

const userInfo = {
  name: '张三',
  age: 25,
  city: '北京'
}
</script>
```

**为什么需要 :key？**
- `key` 帮助 Vue 识别列表中的每个元素，以便高效更新
- 通常使用数据的唯一标识（如 id）作为 key
- 不要使用索引 index 作为 key（除非列表不会重新排序）

### 双向绑定 (v-model)

`v-model` 实现表单元素的双向数据绑定：

```vue
<template>
  <div>
    <!-- 文本输入框 -->
    <input v-model="username" placeholder="请输入用户名">
    <p>你输入的是: {{ username }}</p>
    
    <!-- 多行文本 -->
    <textarea v-model="content"></textarea>
    
    <!-- 复选框 -->
    <input type="checkbox" v-model="agreed">
    <label>同意条款</label>
    
    <!-- 单选按钮 -->
    <input type="radio" v-model="gender" value="male"> 男
    <input type="radio" v-model="gender" value="female"> 女
    
    <!-- 下拉选择 -->
    <select v-model="selectedCity">
      <option value="">请选择城市</option>
      <option value="beijing">北京</option>
      <option value="shanghai">上海</option>
    </select>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const username = ref('')
const content = ref('')
const agreed = ref(false)
const gender = ref('male')
const selectedCity = ref('')
</script>
```

### 组件基础

组件是可复用的 Vue 实例。

#### 定义组件

```vue
<!-- UserCard.vue - 用户卡片组件 -->
<template>
  <div class="user-card">
    <img :src="avatar" :alt="name">
    <h3>{{ name }}</h3>
    <p>{{ bio }}</p>
    <button @click="handleClick">关注</button>
  </div>
</template>

<script setup>
// 定义组件接收的属性（父组件传递的数据）
const props = defineProps({
  name: {
    type: String,
    required: true
  },
  avatar: {
    type: String,
    default: '/default-avatar.png'
  },
  bio: {
    type: String,
    default: '这个人很懒，什么都没写...'
  }
})

// 定义组件发出的事件（通知父组件）
const emit = defineEmits(['follow'])

const handleClick = () => {
  emit('follow', props.name)  // 发出 follow 事件，传递用户名
}
</script>

<style scoped>
.user-card {
  border: 1px solid #ddd;
  padding: 16px;
  border-radius: 8px;
}
</style>
```

#### 使用组件

```vue
<template>
  <div>
    <!-- 使用 UserCard 组件 -->
    <UserCard 
      name="张三" 
      avatar="/avatars/zhangsan.jpg"
      bio="前端开发工程师"
      @follow="handleFollow"
    />
    
    <!-- 传递动态数据 -->
    <UserCard 
      :name="userName" 
      :avatar="userAvatar"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import UserCard from './components/UserCard.vue'

const userName = ref('李四')
const userAvatar = ref('/avatars/lisi.jpg')

const handleFollow = (name) => {
  alert(`你关注了 ${name}`)
}
</script>
```

### 组件通信

```
┌─────────────────────────────────────────────────────────────────┐
│                        组件通信方式                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  父组件 → 子组件: Props                                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  父组件:                                                   │  │
│  │    <ChildComponent :message="parentData" />              │  │
│  │                                                           │  │
│  │  子组件:                                                   │  │
│  │    const props = defineProps(['message'])                 │  │
│  │    // 使用: {{ props.message }}                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  子组件 → 父组件: Emit                                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  子组件:                                                   │  │
│  │    const emit = defineEmits(['update'])                   │  │
│  │    emit('update', newValue)                               │  │
│  │                                                           │  │
│  │  父组件:                                                   │  │
│  │    <ChildComponent @update="handleUpdate" />              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  跨组件: Pinia 状态管理                                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  // stores/user.js                                        │  │
│  │  const useUserStore = defineStore('user', () => {        │  │
│  │    const username = ref('')                               │  │
│  │    return { username }                                    │  │
│  │  })                                                       │  │
│  │                                                           │  │
│  │  // 任意组件中使用                                         │  │
│  │  const userStore = useUserStore()                         │  │
│  │  console.log(userStore.username)                          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 生命周期钩子

生命周期钩子是组件在不同阶段执行的函数：

```vue
<script setup>
import { ref, onMounted, onUpdated, onUnmounted } from 'vue'

const data = ref([])

// 组件挂载完成后执行（DOM已生成）
onMounted(() => {
  console.log('组件已挂载，可以访问DOM')
  // 通常在这里发起API请求
  fetchData()
})

// 组件更新后执行
onUpdated(() => {
  console.log('组件已更新')
})

// 组件卸载前执行
onUnmounted(() => {
  console.log('组件即将卸载')
  // 清理定时器、事件监听等
})

const fetchData = async () => {
  // 获取数据...
}
</script>
```

**常用生命周期钩子：**

| 钩子 | 执行时机 | 常见用途 |
|------|----------|----------|
| `onMounted` | 组件挂载完成 | 发起API请求、操作DOM |
| `onUpdated` | 组件更新完成 | 响应数据变化 |
| `onUnmounted` | 组件卸载 | 清理定时器、取消订阅 |
| `onBeforeMount` | 挂载之前 | 很少使用 |
| `onBeforeUpdate` | 更新之前 | 很少使用 |

### 组合式函数 (Composables)

组合式函数是 Vue 3 中复用逻辑的方式：

```javascript
// composables/useCounter.js
import { ref } from 'vue'

// 定义一个可复用的计数器逻辑
export function useCounter(initialValue = 0) {
  const count = ref(initialValue)
  
  const increment = () => {
    count.value++
  }
  
  const decrement = () => {
    count.value--
  }
  
  const reset = () => {
    count.value = initialValue
  }
  
  // 返回需要暴露的数据和方法
  return {
    count,
    increment,
    decrement,
    reset
  }
}
```

```vue
<!-- 使用组合式函数 -->
<template>
  <div>
    <p>计数: {{ count }}</p>
    <button @click="increment">+1</button>
    <button @click="decrement">-1</button>
    <button @click="reset">重置</button>
  </div>
</template>

<script setup>
import { useCounter } from '@/composables/useCounter'

// 使用组合式函数，获取计数器逻辑
const { count, increment, decrement, reset } = useCounter(10)
</script>
```

---

## 技术栈

| 技术 | 版本 | 用途 | 通俗解释 |
|------|------|------|----------|
| Vue | 3.4+ | 前端框架 | 构建网页应用的核心工具 |
| Vite | 5.0+ | 构建工具 | 打包和运行代码的工具 |
| Vue Router | 4.2+ | 路由管理 | 控制页面跳转 |
| Pinia | 2.3+ | 状态管理 | 组件间共享数据 |
| Element Plus | 2.4+ | UI 组件库 | 提供现成的按钮、表格等组件 |
| Axios | 1.6+ | HTTP 请求 | 与后端通信 |
| ECharts | 5.4+ | 图表库 | 绘制数据可视化图表 |
| Vitest | 1.0+ | 测试框架 | 测试代码 |

---

## 项目结构

```
dong-medicine-frontend/
│
├── public/                              # 静态资源目录
│   └── favicon.ico                      # 网站图标
│
├── src/
│   │
│   ├── views/                           # 页面组件 (14个)
│   │   ├── Home.vue                     # 首页
│   │   ├── Plants.vue                   # 药用植物页面
│   │   ├── Inheritors.vue               # 传承人页面
│   │   ├── Knowledge.vue                # 知识库页面
│   │   ├── Qa.vue                       # 问答社区页面
│   │   ├── Resources.vue                # 学习资源页面
│   │   ├── Interact.vue                 # 互动专区页面
│   │   ├── Visual.vue                   # 数据可视化页面
│   │   ├── PersonalCenter.vue           # 个人中心页面
│   │   ├── Admin.vue                    # 管理后台页面
│   │   ├── About.vue                    # 关于页面
│   │   ├── Feedback.vue                 # 意见反馈页面
│   │   ├── GlobalSearch.vue             # 全局搜索页面
│   │   └── NotFound.vue                 # 404页面
│   │
│   ├── components/                      # 组件目录
│   │   │
│   │   ├── base/                        # 基础组件
│   │   │   ├── ErrorBoundary.vue        # 错误边界组件
│   │   │   ├── VirtualList.vue          # 虚拟列表组件
│   │   │   └── index.js
│   │   │
│   │   ├── common/                      # 通用组件
│   │   │   ├── PageLoading.vue          # 页面加载动画
│   │   │   ├── SkeletonGridCard.vue     # 卡片骨架屏
│   │   │   ├── SkeletonGridImage.vue    # 图片骨架屏
│   │   │   ├── SkeletonListQa.vue       # 问答骨架屏
│   │   │   └── SkeletonListResource.vue # 资源骨架屏
│   │   │
│   │   └── business/                    # 业务组件
│   │       │
│   │       ├── layout/                  # 布局组件
│   │       │   ├── AppHeader.vue        # 应用头部
│   │       │   └── AppFooter.vue        # 应用底部
│   │       │
│   │       ├── display/                 # 展示组件
│   │       │   ├── AiChatCard.vue       # AI对话卡片
│   │       │   ├── CardGrid.vue         # 卡片网格
│   │       │   ├── ChartCard.vue        # 图表卡片
│   │       │   ├── PageSidebar.vue      # 页面侧边栏
│   │       │   ├── Pagination.vue       # 分页组件
│   │       │   ├── SearchFilter.vue     # 搜索过滤
│   │       │   ├── UpdateLogCard.vue    # 更新日志卡片
│   │       │   ├── UpdateLogDialog.vue  # 更新日志对话框
│   │       │   └── index.js
│   │       │
│   │       ├── interact/                # 交互组件
│   │       │   ├── CaptchaInput.vue     # 验证码输入
│   │       │   ├── CommentSection.vue   # 评论组件
│   │       │   ├── InteractSidebar.vue  # 互动侧边栏
│   │       │   ├── PlantGame.vue        # 植物识别游戏
│   │       │   ├── QuizSection.vue      # 趣味答题
│   │       │   └── index.js
│   │       │
│   │       ├── media/                   # 媒体组件
│   │       │   ├── DocumentList.vue     # 文档列表
│   │       │   ├── DocumentPreview.vue  # 文档预览
│   │       │   ├── ImageCarousel.vue    # 图片轮播
│   │       │   ├── MediaDisplay.vue     # 媒体展示
│   │       │   ├── VideoPlayer.vue      # 视频播放
│   │       │   └── index.js
│   │       │
│   │       ├── upload/                  # 上传组件
│   │       │   ├── DocumentUploader.vue # 文档上传
│   │       │   ├── FileUploader.vue     # 通用文件上传
│   │       │   ├── ImageUploader.vue    # 图片上传
│   │       │   ├── VideoUploader.vue    # 视频上传
│   │       │   └── index.js
│   │       │
│   │       ├── dialogs/                 # 详情对话框
│   │       │   ├── InheritorDetailDialog.vue
│   │       │   ├── KnowledgeDetailDialog.vue
│   │       │   ├── PlantDetailDialog.vue
│   │       │   ├── QuizDetailDialog.vue
│   │       │   └── ResourceDetailDialog.vue
│   │       │
│   │       └── admin/                   # 管理后台组件
│   │           ├── dialogs/             # 管理对话框
│   │           ├── forms/               # 管理表单
│   │           ├── AdminDashboard.vue   # 管理仪表盘
│   │           ├── AdminDataTable.vue   # 数据表格
│   │           └── AdminSidebar.vue     # 管理侧边栏
│   │
│   ├── composables/                     # 组合式函数 (11个)
│   │   ├── index.js                     # 导出入口
│   │   ├── useAdminData.js              # 管理后台数据
│   │   ├── useDebounce.js               # 防抖函数
│   │   ├── useErrorHandler.js           # 错误处理
│   │   ├── useFavorite.js               # 收藏功能
│   │   ├── useFormDialog.js             # 表单对话框
│   │   ├── useInteraction.js            # 交互功能
│   │   ├── useMedia.js                  # 媒体处理
│   │   ├── usePersonalCenter.js         # 个人中心
│   │   ├── usePlantGame.js              # 植物游戏
│   │   ├── useQuiz.js                   # 答题逻辑
│   │   ├── useUpdateLog.js              # 更新日志
│   │   └── useVisualData.js             # 可视化数据
│   │
│   ├── router/                          # 路由配置
│   │   └── index.js                     # 路由定义 + 守卫
│   │
│   ├── stores/                          # 状态管理
│   │   ├── index.js                     # Pinia 实例
│   │   └── user.js                      # 用户状态
│   │
│   ├── utils/                           # 工具函数 (7个)
│   │   ├── index.js                     # 通用工具
│   │   ├── adminUtils.js                # 管理后台工具
│   │   ├── cache.js                     # 缓存工具
│   │   ├── logger.js                    # 日志工具
│   │   ├── media.js                     # 媒体工具
│   │   ├── request.js                   # Axios 封装
│   │   └── xss.js                       # XSS 防护
│   │
│   ├── styles/                          # 样式文件 (9个)
│   │   ├── index.css                    # 样式入口
│   │   ├── variables.css                # CSS 变量
│   │   ├── base.css                     # 基础样式
│   │   ├── components.css               # 组件样式
│   │   ├── pages.css                    # 页面样式
│   │   ├── common.css                   # 通用样式
│   │   ├── home.css                     # 首页样式
│   │   ├── media-common.css             # 媒体样式
│   │   └── dialog-common.css            # 对话框样式
│   │
│   ├── config/                          # 配置文件
│   │   └── homeConfig.js                # 首页配置
│   │
│   ├── directives/                      # 自定义指令
│   │   └── index.js
│   │
│   ├── __tests__/                       # 测试文件
│   │
│   ├── App.vue                          # 根组件
│   └── main.js                          # 入口文件
│
├── index.html                           # HTML 模板
├── package.json                         # 项目配置
├── vite.config.js                       # Vite 配置
├── vitest.config.js                     # 测试配置
├── Dockerfile                           # Docker 构建文件
├── nginx.conf                           # Nginx 配置
└── default.conf                         # Nginx 默认配置
```

### 目录职责说明

| 目录 | 职责 | 通俗解释 |
|------|------|----------|
| `views/` | 页面组件 | 对应一个个网页，如首页、植物页等 |
| `components/` | 可复用组件 | 可在多个页面中复用的UI组件 |
| `composables/` | 组合式函数 | 可复用的逻辑代码 |
| `stores/` | 状态管理 | 存储全局共享的数据 |
| `router/` | 路由配置 | 控制URL和页面的对应关系 |
| `utils/` | 工具函数 | 通用的小函数 |
| `styles/` | 样式文件 | CSS样式 |
| `config/` | 配置文件 | 可配置的数据 |

---

## 页面组件

| 页面 | 路由 | 功能描述 |
|------|------|----------|
| Home.vue | `/` | 首页，展示平台核心功能入口、统计数据、传承人风采、快速导航 |
| Plants.vue | `/plants` | 药用资源图鉴，展示黔东南道地药材图文详解，支持分类筛选 |
| Inheritors.vue | `/inheritors` | 传承人风采展示，按级别筛选、展示传承谱系与技艺特色 |
| Knowledge.vue | `/knowledge` | 非遗医药知识库，支持分类检索、搜索过滤、收藏功能 |
| Qa.vue | `/qa` | 问答社区，侗医药知识问答、疑难解答、互动交流 |
| Interact.vue | `/interact` | 文化互动专区，包含趣味答题、植物识别游戏、评论交流 |
| Resources.vue | `/resources` | 学习资源库，支持视频/文档/图片资源的预览、下载、收藏 |
| Visual.vue | `/visual` | 数据可视化，展示药方频次、疗法分类、传承人分布等统计图表 |
| PersonalCenter.vue | `/personal` | 个人中心（需登录），管理收藏、答题记录、评论历史、账号设置 |
| Admin.vue | `/admin` | 管理后台（需管理员权限），数据管理、用户管理、评论审核、日志查看 |
| About.vue | `/about` | 关于页面，介绍选题背景、平台特色、功能模块、侗医文化 |
| Feedback.vue | `/feedback` | 意见反馈，用户提交功能建议、问题反馈 |
| GlobalSearch.vue | `/search` | 全局搜索，跨知识、植物、传承人、问答的统一搜索入口 |
| NotFound.vue | `/:pathMatch(.*)*` | 404页面 |

---

## 业务组件

### 布局组件 (layout/)

| 组件 | 用途 |
|------|------|
| AppHeader.vue | 应用头部，导航栏、登录状态、全局搜索入口 |
| AppFooter.vue | 应用底部，版权信息、友情链接 |

### 展示组件 (display/)

| 组件 | 用途 |
|------|------|
| AiChatCard.vue | AI 对话卡片，集成智能问答 |
| CardGrid.vue | 卡片网格组件，通用卡片列表展示 |
| ChartCard.vue | 图表卡片组件，封装 ECharts 图表 |
| PageSidebar.vue | 页面侧边栏，展示统计和热门内容 |
| Pagination.vue | 分页组件 |
| SearchFilter.vue | 搜索过滤组件 |
| UpdateLogCard.vue | 更新日志卡片 |
| UpdateLogDialog.vue | 更新日志对话框 |

### 交互组件 (interact/)

| 组件 | 用途 |
|------|------|
| CaptchaInput.vue | 验证码输入组件 |
| CommentSection.vue | 评论组件，支持发表评论、回复 |
| InteractSidebar.vue | 互动侧边栏，展示排行榜和统计 |
| PlantGame.vue | 植物识别游戏，根据图片识别药材名称 |
| QuizSection.vue | 趣味答题组件，支持计时、评分 |

### 媒体组件 (media/)

| 组件 | 用途 |
|------|------|
| DocumentList.vue | 文档列表组件 |
| DocumentPreview.vue | 文档预览组件 |
| ImageCarousel.vue | 图片轮播组件 |
| MediaDisplay.vue | 媒体展示组件 |
| VideoPlayer.vue | 视频播放组件 |

### 上传组件 (upload/)

| 组件 | 用途 |
|------|------|
| DocumentUploader.vue | 文档上传组件 |
| FileUploader.vue | 通用文件上传组件 |
| ImageUploader.vue | 图片上传组件 |
| VideoUploader.vue | 视频上传组件 |

### 详情对话框组件 (dialogs/)

| 组件 | 用途 |
|------|------|
| PlantDetailDialog.vue | 药材详情对话框 |
| KnowledgeDetailDialog.vue | 知识详情对话框 |
| InheritorDetailDialog.vue | 传承人详情对话框 |
| ResourceDetailDialog.vue | 资源详情对话框 |
| QuizDetailDialog.vue | 答题详情对话框 |

### 管理后台组件 (admin/)

| 组件 | 用途 |
|------|------|
| AdminDashboard.vue | 管理仪表盘，数据概览 |
| AdminDataTable.vue | 数据表格组件，通用 CRUD 表格 |
| AdminSidebar.vue | 管理侧边栏导航 |

---

## 组合式函数

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

---

## 路由配置

| 路由路径 | 名称 | 组件 | 权限要求 |
|---------|------|------|----------|
| `/` | Home | Home.vue | 无 |
| `/plants` | Plants | Plants.vue | 无 |
| `/inheritors` | Inheritors | Inheritors.vue | 无 |
| `/knowledge` | Knowledge | Knowledge.vue | 无 |
| `/qa` | Qa | Qa.vue | 无 |
| `/interact` | Interact | Interact.vue | 无 |
| `/resources` | Resources | Resources.vue | 无 |
| `/visual` | Visual | Visual.vue | 无 |
| `/personal` | Personal | PersonalCenter.vue | 需登录 |
| `/admin` | Admin | Admin.vue | 需登录 + 管理员权限 |
| `/about` | About | About.vue | 无 |
| `/feedback` | Feedback | Feedback.vue | 无 |
| `/search` | Search | GlobalSearch.vue | 无 |
| `/:pathMatch(.*)*` | NotFound | NotFound.vue | 无 |

### 路由守卫功能

```javascript
// router/index.js

// 本地Token过期检查
function isTokenLocallyExpired(userStore) {
  const token = userStore.token
  if (!token) return true
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Date.now() >= payload.exp * 1000
  } catch {
    return true
  }
}

// Token验证缓存（60秒）
const VALIDATION_CACHE_TTL = 60 * 1000

// 路由守卫逻辑
router.beforeEach(async (to, from, next) => {
  // 1. 初始化用户状态
  // 2. 检查是否需要认证
  // 3. 本地Token过期检查
  // 4. 服务端Token验证（带缓存）
  // 5. 管理员权限验证
})
```

---

## 状态管理

### 用户状态 (stores/user.js)

| 状态/方法 | 描述 |
|----------|------|
| `token` | 用户认证令牌 |
| `userId` | 用户 ID |
| `username` | 用户名 |
| `role` | 用户角色 |
| `userInfo` | 用户详细信息 |
| `isLoggedIn` | 登录状态（计算属性） |
| `isAdmin` | 是否管理员（计算属性） |
| `setAuth()` | 设置认证信息 |
| `clearAuth()` | 清除认证信息 |
| `login()` | 登录方法 |
| `logout()` | 登出方法 |
| `validateToken()` | 验证 Token 有效性 |
| `changePassword()` | 修改密码 |
| `fetchUserInfo()` | 获取用户信息 |

---

## 工具函数

### 通用工具 (utils/index.js)

| 函数 | 描述 |
|------|------|
| `formatTime()` | 时间格式化，支持相对时间 |
| `extractData()` | 响应数据提取，兼容多种数据结构 |
| `getRankClass()` | 获取排名样式类 |
| `formatFileSize()` | 文件大小格式化 |
| `truncate()` | 文本截断 |
| `debounce()` | 防抖函数 |
| `throttle()` | 节流函数 |
| `deepClone()` | 深拷贝 |
| `isEmpty/isNotEmpty()` | 空值判断 |
| `generateId()` | 生成唯一 ID |
| `sleep()` | 延迟函数 |
| `retry()` | 重试函数 |

### Axios 封装 (utils/request.js)

核心功能：
- 请求/响应拦截器
- Token 自动注入
- Token 刷新机制（Promise缓存，避免竞态）
- 请求取消（防重复提交）
- 自动重试机制
- XSS/SQL 注入防护
- 统一错误处理

---

## 安全机制

### XSS 防护 (utils/xss.js)

覆盖 30+ 危险模式：
- script标签
- javascript/vbscript协议
- 事件处理器 (onclick, onerror等)
- HTML实体编码
- eval/expression函数
- 危险标签 (iframe, object, embed等)

### 密码验证规则

- 长度 8-50 位
- 必须包含字母和数字
- 不能包含空格

---

## 样式系统

### CSS 变量 (styles/variables.css)

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
  
  /* 间距系统 */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 24px;
  
  /* 圆角系统 */
  --radius-xs: 4px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
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

# 预览生产构建
npm run preview
```

### 访问地址

- 开发地址: http://localhost:5173
- 生产构建: `dist/` 目录

---

## Docker 部署

### 构建镜像

```bash
docker build -t dong-medicine-frontend .
```

### 运行容器

```bash
docker run -d \
  --name dong-medicine-frontend \
  -p 80:80 \
  dong-medicine-frontend
```

### 使用 Docker Compose

```bash
# 在项目根目录执行
docker-compose up -d --build
```

---

## 常见问题

### 1. npm install 失败

**解决方案**：清除缓存重试
```bash
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### 2. 页面空白

**可能原因**：
- 后端服务未启动
- API 请求失败

**解决方案**：
- 确保后端服务运行在 http://localhost:8080
- 打开浏览器控制台查看错误信息

### 3. 登录后刷新页面退出

**可能原因**：Token 未正确存储

**解决方案**：
- 检查浏览器 localStorage 中是否有 token
- 检查 stores/user.js 中的 token 持久化逻辑

### 4. 样式不生效

**可能原因**：
- scoped 样式作用域问题
- CSS 优先级问题

**解决方案**：
- 使用 `:deep()` 穿透 scoped 样式
- 检查 CSS 选择器优先级

---

**最后更新时间**：2026年4月3日
