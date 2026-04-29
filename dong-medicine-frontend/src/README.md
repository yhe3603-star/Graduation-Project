# 前端源代码目录 (src/)

> **类比：你的工作台，所有源代码都在这里**

`src/` 是整个前端项目的"心脏"——你写的每一行代码、每一个组件、每一张图片，几乎都在这个目录里。`src` 是 source（源代码）的缩写，只有 `src/` 里的文件才会被 Vite 处理和打包。

---

## 一、目录结构详解

```
src/
├── assets/              # 静态资源（图片、SVG等，会被Vite优化）
│   └── vue.svg          #   Vue官方Logo
│
├── components/          # Vue组件（本项目的核心，详见下方分层说明）
│   ├── base/            #   基础组件：无业务逻辑，最底层
│   │   ├── ErrorBoundary.vue   # 错误边界：捕获子组件错误，防止整个页面崩溃
│   │   ├── VirtualList.vue     # 虚拟列表：大数据量列表性能优化
│   │   └── index.js            # 统一导出
│   │
│   ├── common/          #   通用组件：跨页面复用的UI组件
│   │   ├── PageLoading.vue     # 全局页面加载动画
│   │   ├── SkeletonGridCard.vue    # 卡片网格骨架屏
│   │   ├── SkeletonGridImage.vue   # 图片网格骨架屏
│   │   ├── SkeletonListQa.vue      # 问答列表骨架屏
│   │   └── SkeletonListResource.vue # 资源列表骨架屏
│   │
│   └── business/        #   业务组件：包含具体业务逻辑
│       ├── admin/       #     管理后台专用组件
│       │   ├── AdminDashboard.vue  # 管理仪表盘
│       │   ├── AdminDataTable.vue  # 通用数据表格
│       │   ├── AdminSidebar.vue    # 管理侧边栏
│       │   ├── dialogs/  #   管理端详情弹窗（9个）
│       │   └── forms/    #   管理端表单弹窗（6个）
│       │
│       ├── dialogs/     #     前台详情弹窗（6个）
│       │   ├── PlantDetailDialog.vue     # 植物详情
│       │   ├── KnowledgeDetailDialog.vue # 知识详情
│       │   ├── InheritorDetailDialog.vue # 传承人详情
│       │   ├── QuizDetailDialog.vue      # 答题详情
│       │   └── ResourceDetailDialog.vue  # 资源详情
│       │
│       ├── display/     #     展示类组件
│       │   ├── AiChatCard.vue     # AI聊天卡片
│       │   ├── CardGrid.vue       # 卡片网格布局
│       │   ├── ChartCard.vue      # 图表卡片
│       │   ├── PageSidebar.vue    # 页面侧边栏
│       │   ├── Pagination.vue     # 分页器
│       │   ├── SearchFilter.vue   # 搜索筛选
│       │   ├── StatCard.vue       # 统计卡片
│       │   ├── UpdateLogCard.vue  # 更新日志卡片
│       │   └── UpdateLogDialog.vue # 更新日志弹窗
│       │
│       ├── interact/    #     交互类组件
│       │   ├── CaptchaInput.vue   # 验证码输入
│       │   ├── CommentSection.vue # 评论区
│       │   ├── InteractSidebar.vue # 互动侧边栏
│       │   ├── PlantGame.vue      # 植物识别游戏
│       │   └── QuizSection.vue    # 答题区
│       │
│       ├── layout/      #     布局组件
│       │   ├── AppHeader.vue      # 全局顶部导航栏
│       │   └── AppFooter.vue      # 全局底部栏
│       │
│       ├── media/       #     媒体组件
│       │   ├── DocumentList.vue    # 文档列表
│       │   ├── DocumentPreview.vue # 文档预览
│       │   ├── ImageCarousel.vue   # 图片轮播
│       │   ├── MediaDisplay.vue    # 媒体展示（统一入口）
│       │   └── VideoPlayer.vue     # 视频播放器
│       │
│       └── upload/      #     上传组件
│           ├── DocumentUploader.vue # 文档上传
│           ├── FileUploader.vue     # 通用文件上传
│           ├── ImageUploader.vue    # 图片上传
│           └── VideoUploader.vue    # 视频上传
│
├── composables/         # 组合式函数（可复用的业务逻辑"菜谱"）
│   ├── index.js         #   统一导出所有composable
│   ├── useAdminData.js  #   管理端数据管理
│   ├── useDebounce.js   #   防抖工具
│   ├── useErrorHandler.js # 统一错误处理
│   ├── useFavorite.js   #   收藏功能
│   ├── useFormDialog.js #   表单弹窗逻辑
│   ├── useInteraction.js #  交互工具（倒计时/评论/分页/筛选/统计）
│   ├── useMedia.js      #   媒体展示
│   ├── usePersonalCenter.js # 个人中心
│   ├── usePlantGame.js  #   植物识别游戏
│   ├── useQuiz.js       #   答题功能
│   ├── useUpdateLog.js  #   更新日志
│   └── useVisualData.js #   数据可视化
│
├── config/              # 页面配置
│   └── homeConfig.js    #   首页导航和模块配置
│
├── directives/          # 自定义指令
│   └── index.js         #   注册所有自定义指令
│
├── router/              # 路由配置
│   └── index.js         #   路由定义 + 导航守卫
│
├── stores/              # Pinia状态管理
│   ├── index.js         #   Pinia实例创建
│   └── user.js          #   用户状态（登录/权限/Token管理）
│
├── styles/              # 全局样式与设计系统
│   ├── variables.css    #   CSS变量（颜色/字体/间距/阴影/动画）
│   ├── base.css         #   基础样式重置
│   ├── common.css       #   通用样式类
│   ├── components.css   #   组件样式
│   ├── dialog-common.css #  弹窗通用样式
│   ├── media-common.css #  媒体组件样式
│   ├── home.css         #   首页样式
│   ├── pages.css        #   页面通用样式
│   ├── Visual.css       #   数据可视化页面样式
│   └── index.css        #   样式统一入口
│
├── utils/               # 工具函数
│   ├── index.js         #   统一导出
│   ├── request.js       #   HTTP客户端（Axios增强版）
│   ├── cache.js         #   双层缓存（内存+SessionStorage）
│   ├── xss.js           #   XSS/SQL注入防护
│   ├── logger.js        #   日志工具
│   ├── media.js         #   媒体工具函数
│   ├── chartConfig.js   #   ECharts图表配置
│   └── adminUtils.js    #   管理端工具函数
│
├── views/               # 页面组件（每个对应一个路由）
│   ├── Home.vue         #   首页
│   ├── Plants.vue       #   药用植物
│   ├── Knowledge.vue    #   知识库
│   ├── Inheritors.vue   #   传承人
│   ├── Qa.vue           #   问答社区
│   ├── Interact.vue     #   互动专区
│   ├── Resources.vue    #   学习资源
│   ├── Visual.vue       #   数据可视化
│   ├── PersonalCenter.vue # 个人中心（需登录）
│   ├── Admin.vue        #   管理后台（需登录+管理员）
│   ├── About.vue        #   关于平台
│   ├── Feedback.vue     #   意见反馈
│   ├── GlobalSearch.vue #   全局搜索
│   └── NotFound.vue     #   404页面
│
├── App.vue              # 根组件（全局布局 + 登录/注册弹窗）
└── main.js              # 应用入口（初始化Vue + 注册插件）
```

---

## 二、Vue 3 Composition API 基础（新手必学）

Vue 3 引入了 Composition API，这是本项目使用的写法。下面逐个解释核心概念。

### 2.1 什么是 `<script setup>`？

**类比：精简版的工作台**

Vue 组件有三种写法，`<script setup>` 是最简洁的：

```vue
<!-- 写法一：选项式 API（Vue 2风格，啰嗦） -->
<script>
export default {
  data() { return { count: 0 } },
  methods: { increment() { this.count++ } }
}
</script>

<!-- 写法二：setup函数（Vue 3早期，稍啰嗦） -->
<script>
import { ref } from 'vue'
export default {
  setup() {
    const count = ref(0)
    function increment() { count.value++ }
    return { count, increment }  // 必须手动return，忘一个就报错
  }
}
</script>

<!-- 写法三：<script setup>（本项目使用，最简洁！） -->
<script setup>
import { ref } from 'vue'
const count = ref(0)           // 定义响应式数据
function increment() { count.value++ }  // 定义方法
// 不需要return！模板里直接用 count 和 increment
</script>
```

**好处**：
- 更少的代码，不用写 `export default` 和 `return`
- 更好的 TypeScript 支持
- 更好的运行时性能

### 2.2 什么是 `ref()` 和 `reactive()`？

**类比：魔法盒子**

普通 JavaScript 变量改了值，页面不会自动更新。`ref()` 和 `reactive()` 是 Vue 提供的"魔法盒子"——放在盒子里的值一旦改变，页面会自动更新。

```javascript
import { ref, reactive } from 'vue'

// ========== ref：适合基本类型（字符串、数字、布尔值） ==========
const plantName = ref('钩藤')   // 创建一个"魔法盒子"，初始值是'钩藤'
console.log(plantName.value)     // 读取值：'钩藤'（注意要加 .value）
plantName.value = '透骨草'       // 修改值，页面自动更新！

// ========== reactive：适合对象/数组 ==========
const plant = reactive({
  name: '钩藤',
  dongName: '介秀',
  category: '祛风湿药'
})
console.log(plant.name)          // 读取值：'钩藤'（不需要.value）
plant.name = '九节茶'            // 修改值，页面自动更新！
```

**常见错误**：

```javascript
// 错误1：忘记 .value（ref必须通过.value访问，在JS中）
const count = ref(0)
count = 1              // 错误！这会覆盖整个ref对象
count.value = 1        // 正确！

// 错误2：解构reactive会丢失响应性
const plant = reactive({ name: '钩藤' })
const { name } = plant        // 错误！name变成了普通变量，不再是响应式的
// 解决：用toRefs
const { name } = toRefs(plant) // 正确！name保持响应式
```

**什么时候用 ref，什么时候用 reactive？**
- 简单值（数字、字符串、布尔值）用 `ref()`
- 对象/数组用 `reactive()` 或 `ref()` 都可以，本项目主要用 `ref()`
- 不确定就用 `ref()`，它更通用

### 2.3 什么是 `computed()`？

**类比：自动计算的Excel公式**

`computed()` 是"计算属性"——它像一个 Excel 公式，依赖的数据变了，结果自动重新计算。

```javascript
import { ref, computed } from 'vue'

const plants = ref([
  { name: '钩藤', category: '祛风湿药' },
  { name: '九节茶', category: '清热药' },
  { name: '透骨草', category: '祛风湿药' }
])

// 计算属性：自动筛选出"祛风湿药"
const windHerbs = computed(() => {
  return plants.value.filter(p => p.category === '祛风湿药')
})
// windHerbs 的值是 [{ name: '钩藤', ... }, { name: '透骨草', ... }]

// 如果 plants 变了，windHerbs 会自动重新计算
plants.value.push({ name: '威灵仙', category: '祛风湿药' })
// windHerbs 自动更新为3个植物
```

**computed vs 普通函数的区别**：

```javascript
// 普通函数：每次调用都重新计算
function getWindHerbs() {
  return plants.value.filter(p => p.category === '祛风湿药')
}

// 计算属性：有缓存！依赖不变就不重新计算
const windHerbs = computed(() => {
  return plants.value.filter(p => p.category === '祛风湿药')
})
// plants没变 → 直接返回缓存结果（快！）
// plants变了 → 重新计算并缓存
```

### 2.4 什么是 `onMounted()`？

**类比：搬家后第一件事**

`onMounted()` 是 Vue 的**生命周期钩子**——当组件"挂载"到页面上后（就像搬进新家后），自动执行你指定的代码。

```javascript
import { ref, onMounted } from 'vue'

const plants = ref([])  // 初始是空数组

// 组件挂载后，自动去后端获取植物数据
onMounted(async () => {
  const res = await request.get('/plants/list', { params: { page: 1, size: 10 } })
  plants.value = res.data.records
})
```

**Vue 3 常用生命周期**：

| 钩子 | 什么时候执行 | 类比 | 常见用途 |
|------|------------|------|---------|
| `onMounted` | 组件挂载到页面后 | 搬进新家 | 请求数据、初始化 |
| `onUnmounted` | 组件被销毁前 | 搬出旧家 | 清理定时器、取消请求 |
| `onUpdated` | 数据变化页面更新后 | 重新装修 | 操作更新后的DOM |

---

## 三、组件分层体系

本项目的组件按照**从通用到具体**的顺序分为四层，上层依赖下层，下层不知道上层存在：

```
+================================================================+
|                        views/  页面组件                         |
|   Home.vue  Plants.vue  Knowledge.vue  Admin.vue  ...          |
|   职责：组装业务组件，定义页面布局，处理路由参数                   |
+================================================================+
                              |
                              v
+================================================================+
|                    business/  业务组件                          |
|  +----------+  +----------+  +----------+  +----------+        |
|  | display/ |  | interact/|  | media/   |  | upload/  |  ...   |
|  | 展示组件 |  | 交互组件 |  | 媒体组件 |  | 上传组件 |        |
|  +----------+  +----------+  +----------+  +----------+        |
|   职责：包含具体业务逻辑（如搜索筛选、答题、视频播放）            |
+================================================================+
                              |
                              v
+================================================================+
|                     common/  通用组件                           |
|   PageLoading.vue  SkeletonGridCard.vue  ...                   |
|   职责：跨页面复用的UI组件，不含具体业务逻辑                      |
+================================================================+
                              |
                              v
+================================================================+
|                      base/  基础组件                            |
|   ErrorBoundary.vue  VirtualList.vue                           |
|   职责：最底层的通用能力，与业务完全无关                          |
+================================================================+
```

**依赖规则**：
- `views/` 可以引用 `business/`、`common/`、`base/`
- `business/` 可以引用 `common/`、`base/`，不能引用 `views/`
- `common/` 可以引用 `base/`，不能引用 `business/`、`views/`
- `base/` 不能引用任何其他层，完全独立

**为什么要分层？** 就像盖楼——地基（base）要稳固，不能依赖楼层；楼层（business）可以用地基的材料，但不能依赖屋顶（views）。这样改一层不影响其他层。

---

## 四、Composables 详解（可复用的菜谱）

**类比：可复用的菜谱**

如果你在多个页面都需要"获取植物列表 + 分页 + 搜索"的逻辑，难道每个页面都写一遍？当然不！Composable 就是把可复用的逻辑提取出来，像菜谱一样——谁需要就"照着做"。

```javascript
// composables/useQuiz.js —— 答题功能的"菜谱"
import { ref } from 'vue'
import request from '@/utils/request'

export function useQuiz() {
  const questions = ref([])       // 题目列表
  const currentScore = ref(0)     // 当前得分
  const loading = ref(false)      // 加载状态

  async function fetchQuestions(category) {
    loading.value = true
    try {
      const res = await request.get('/quiz/questions', { params: { category } })
      questions.value = res.data
    } finally {
      loading.value = false
    }
  }

  function checkAnswer(questionId, answer) {
    // 检查答案逻辑...
  }

  // 返回"食材"——组件需要用到的数据和方法
  return {
    questions,
    currentScore,
    loading,
    fetchQuestions,
    checkAnswer
  }
}
```

在组件中使用：

```vue
<script setup>
import { useQuiz } from '@/composables'

// "照着菜谱做"——解构出需要的数据和方法
const { questions, currentScore, loading, fetchQuestions, checkAnswer } = useQuiz()

onMounted(() => {
  fetchQuestions('plants')  // 获取植物类题目
})
</script>
```

**本项目的所有 Composable**：

| Composable | 职责 | 在哪些页面使用 |
|-----------|------|-------------|
| `useAdminData` | 管理端数据CRUD | Admin.vue |
| `useDebounce` | 防抖（输入时延迟触发） | 多个搜索组件 |
| `useErrorHandler` | 统一错误处理 | 多个组件 |
| `useFavorite` | 收藏/取消收藏 | Plants, Knowledge, Resources |
| `useFormDialog` | 表单弹窗逻辑 | Admin管理端 |
| `useInteraction` | 倒计时/评论/分页/筛选/统计 | Interact, Qa |
| `useMedia` | 媒体展示 | Resources |
| `usePersonalCenter` | 个人中心功能 | PersonalCenter |
| `usePlantGame` | 植物识别游戏 | Interact |
| `useQuiz` | 答题功能 | Interact |
| `useUpdateLog` | 更新日志管理 | Home |
| `useVisualData` | 数据可视化 | Visual |

---

## 五、技术栈版本与用途

| 技术 | 版本 | 用途 | 在本项目中的角色 |
|------|------|------|---------------|
| Vue.js | 3.4 | 渐进式JavaScript框架 | 核心：所有组件基于Vue 3 Composition API |
| Vue Router | 4.2 | 路由管理 | 页面导航 + 权限守卫 |
| Pinia | 2.3 | 状态管理 | 用户登录状态、全局数据共享 |
| Element Plus | 2.4 | UI组件库 | 按钮/表格/表单/弹窗/分页等UI组件 |
| Axios | 1.6 | HTTP客户端 | 与后端API通信 |
| ECharts | 5.4 | 数据可视化 | 统计图表展示 |
| Vite | 5.0 | 构建工具 | 开发服务器 + 生产构建 |
| lodash-es | 4.17 | 工具函数 | debounce/throttle等 |
| vuedraggable | 4.1 | 拖拽排序 | 管理端排序功能 |
| Vitest | 1.0 | 单元测试 | 组件和函数的自动化测试 |
| ESLint | 9.22 | 代码规范 | 统一代码风格 |

---

## 六、应用启动流程 (main.js 逐行解读)

当你运行 `npm run dev`，浏览器打开页面后，`main.js` 是第一个执行的文件。让我们逐行理解它做了什么：

```javascript
// ===== 第一步：导入所有需要的东西 =====

import { createApp } from "vue"              // 导入Vue的"创建应用"函数
import ElementPlus from "element-plus"        // 导入Element Plus组件库
import zhCn from "element-plus/dist/locale/zh-cn.mjs"  // 导入中文语言包
import "element-plus/dist/index.css"          // 导入Element Plus的样式
import * as Icons from "@element-plus/icons-vue"  // 导入所有Element Plus图标
import App from "./App.vue"                   // 导入根组件
import router from "./router"                 // 导入路由配置
import request from "./utils/request"         // 导入HTTP客户端
import { pinia } from "./stores"              // 导入Pinia状态管理
import "./styles/index.css"                   // 导入全局样式

// ===== 第二步：创建Vue应用实例 =====
const app = createApp(App)
// 相当于：创建了一个空的Vue应用，根组件是App.vue

// ===== 第三步：注册插件（给Vue应用安装"功能模块"） =====

app.use(ElementPlus, { locale: zhCn })
// 安装Element Plus，设置语言为中文
// 之后所有el-xxx组件都可以在模板中直接使用

app.use(router)
// 安装Vue Router，之后可以用<router-view>和<router-link>

app.use(pinia)
// 安装Pinia，之后可以用defineStore创建状态仓库

// ===== 第四步：全局注入和配置 =====

app.provide("request", request)
// 全局注入request服务，子组件可以用inject("request")获取

app.config.globalProperties.$axios = request
// 把request挂载到全局属性，组件中可以用this.$axios访问

Object.entries(Icons).forEach(([name, component]) => {
  app.component(name, component)
})
// 注册所有Element Plus图标组件，之后可以直接用<Edit />、<Delete />等

// ===== 第五步：挂载应用到DOM =====
app.mount("#app")
// 把Vue应用挂载到index.html中id为"app"的div元素上
// 到这一步，页面才真正渲染出来！

// ===== 第六步：移除加载动画 =====
router.isReady().then(() => {
  const loading = document.getElementById('app-loading')
  if (loading) {
    loading.classList.add('fade-out')     // 添加淡出动画
    setTimeout(() => loading.remove(), 300) // 300ms后移除加载动画元素
  }
})
// 路由准备就绪后，移除index.html中的加载动画
```

**用流程图表示**：

```
npm run dev
    |
    v
Vite启动开发服务器，打开index.html
    |
    v
index.html 中 <script type="module" src="/src/main.js"> 加载main.js
    |
    v
main.js 执行：
    1. createApp(App)        → 创建Vue应用
    2. app.use(ElementPlus)  → 注册UI组件库
    3. app.use(router)       → 注册路由
    4. app.use(pinia)        → 注册状态管理
    5. app.provide(request)  → 全局注入HTTP客户端
    6. app.mount("#app")     → 挂载到页面 → App.vue渲染
    7. router.isReady()      → 路由就绪 → 移除加载动画
    |
    v
App.vue 渲染：
    - AppHeader（顶部导航栏）
    - <router-view>（根据URL显示对应页面）
    - AppFooter（底部栏）
    - 登录/注册弹窗（按需显示）
```

---

## 七、常见新手误区

### 误区1：在 `<script setup>` 里用 `this`

```vue
<!-- 错误！<script setup> 里没有 this -->
<script setup>
import { ref } from 'vue'
const count = ref(0)
this.count++    <!-- 报错！this是undefined -->
</script>

<!-- 正确：直接使用变量名 -->
<script setup>
import { ref } from 'vue'
const count = ref(0)
count.value++   <!-- 正确！ -->
</script>
```

### 误区2：修改 props 的值

```vue
<!-- 错误！props是父组件传过来的，子组件不能直接修改 -->
<script setup>
const props = defineProps(['title'])
props.title = '新标题'  <!-- 报错！Vue会警告 -->
</script>

<!-- 正确：用emit通知父组件修改 -->
<script setup>
const props = defineProps(['title'])
const emit = defineEmits(['update:title'])
emit('update:title', '新标题')  <!-- 通知父组件改 -->
</script>
```

### 误区3：在 `onMounted` 之前操作 DOM

```vue
<!-- 错误！组件还没挂载，DOM还不存在 -->
<script setup>
import { ref } from 'vue'
const el = ref(null)
console.log(el.value)  <!-- null！DOM还没渲染 -->

// 正确：在onMounted里操作DOM
import { ref, onMounted } from 'vue'
const el = ref(null)
onMounted(() => {
  console.log(el.value)  <!-- DOM元素，可以操作了 -->
})
</script>
```

### 误区4：忘记导入 composable

```vue
<!-- 错误：直接使用没导入的composable -->
<script setup>
const { questions } = useQuiz()  <!-- 报错！useQuiz未定义 -->
</script>

<!-- 正确：先导入再使用 -->
<script setup>
import { useQuiz } from '@/composables'
const { questions } = useQuiz()  <!-- 正确！ -->
</script>
```

### 误区5：在模板中写复杂逻辑

```vue
<!-- 不推荐：模板里写复杂逻辑，难以维护 -->
<template>
  <div v-if="plants.filter(p => p.category === '祛风湿药').length > 0">
    共有 {{ plants.filter(p => p.category === '祛风湿药').length }} 种祛风湿药
  </div>
</template>

<!-- 推荐：用computed，模板保持简洁 -->
<script setup>
const windHerbs = computed(() => plants.value.filter(p => p.category === '祛风湿药'))
</script>
<template>
  <div v-if="windHerbs.length > 0">
    共有 {{ windHerbs.length }} 种祛风湿药
  </div>
</template>
```

---

## 代码审查与改进建议

- [状态管理] 状态获取方式不统一：Pinia Store、provide/inject、sessionStorage三种方式并存
- [性能] echarts全量导入导致打包体积过大
- [错误处理] 部分composable静默吞没API错误
