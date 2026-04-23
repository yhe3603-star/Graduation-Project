# 页面组件目录 (views/)

> 类比：想象一家侗族文化餐厅。餐厅有不同的区域——大厅（首页）、包间（详情页）、厨房（管理后台）。每个区域有自己独特的装修和功能，**views 就是这些区域**，每个 `.vue` 文件就是一个独立的"房间"。

## 什么是页面组件（View）？

页面组件是**直接对应一个 URL 路由**的 Vue 组件。当用户在浏览器地址栏输入 `/plants` 时，路由器就会把 `Plants.vue` 渲染到页面上。

简单来说：
- 用户**能看到的一个完整网页** = 一个 View
- URL 路径和 View 是**一一对应**的关系

## views 和 components 有什么区别？

这是新手最容易混淆的概念，用表格说清楚：

| 对比项 | views/（页面组件） | components/（通用组件） |
|--------|-------------------|----------------------|
| 类比 | 餐厅的各个区域（大厅、包间） | 餐厅里的桌椅、灯具（可复用） |
| 对应路由 | 有，一个 View = 一个 URL | 没有，不会单独出现在地址栏 |
| 复用性 | 通常不复用，每个页面独一份 | 高度复用，多个页面都能用 |
| 文件大小 | 较大，组装各种子组件 | 较小，专注单一功能 |
| 例子 | `Plants.vue`（药用植物页） | `SearchFilter.vue`（搜索筛选组件） |

**一句话总结**：views 是"搭积木的图纸"，components 是"积木块"。views 把各种 components 组装成完整页面。

## 全部 14 个页面组件

| 页面组件 | 路由路径 | 访问权限 | 功能说明 |
|---------|---------|---------|---------|
| `Home.vue` | `/` | 公开 | 首页，数据概览、快速导航、传承人风采 |
| `Plants.vue` | `/plants` | 公开 | 药用植物图鉴，分类筛选、搜索、收藏 |
| `Inheritors.vue` | `/inheritors` | 公开 | 传承人档案，级别筛选、代表案例 |
| `Knowledge.vue` | `/knowledge` | 公开 | 知识库，多维度分类、搜索 |
| `Qa.vue` | `/qa` | 公开 | 问答社区，AI 智能回答 |
| `Resources.vue` | `/resources` | 公开 | 学习资源，多媒体展示与下载 |
| `Interact.vue` | `/interact` | 公开 | 互动专区，答题、植物游戏、评论 |
| `Visual.vue` | `/visual` | 公开 | 数据可视化，ECharts 图表展示 |
| `PersonalCenter.vue` | `/personal` | 需登录 | 个人中心，收藏/记录/设置 |
| `Admin.vue` | `/admin` | 需登录 + 管理员 | 管理后台，内容 CRUD/用户管理/审核 |
| `About.vue` | `/about` | 公开 | 关于平台，项目介绍与统计数据 |
| `Feedback.vue` | `/feedback` | 公开 | 意见反馈提交 |
| `GlobalSearch.vue` | `/search` | 公开 | 全局搜索结果页 |
| `NotFound.vue` | `/:pathMatch(.*)*` | 公开 | 404 页面，找不到路由时显示 |

## 路由懒加载 -- 为什么要用 `() => import()`？

打开 `router/index.js`，你会看到所有路由都长这样：

```javascript
// 懒加载写法：用箭头函数包裹 import
{ path: '/plants', component: () => import('@/views/Plants.vue') }

// 千万不要这样写（同步加载）：
// { path: '/plants', component: PlantsVue }  // 这会把所有页面打包到一个文件里！
```

**为什么要懒加载？**

类比：你不会一次性把餐厅所有区域的灯都打开，而是客人走到哪个区域，再开哪个区域的灯。

- 如果不用懒加载，用户打开首页时，浏览器会**一次性下载所有 14 个页面的代码**
- 用了懒加载，用户访问 `/plants` 时，**只下载 Plants.vue 的代码**
- 这让首页加载速度大幅提升，特别是用户可能永远不会访问 Admin 页面

**技术原理**：`() => import()` 是 ES6 的动态导入语法，Webpack/Vite 会自动把它拆分成单独的 JS 文件（chunk），只有路由被访问时才加载。

## 认证守卫 -- 谁能进哪个"房间"？

路由通过 `meta` 字段标记权限，导航守卫在进入页面前检查：

```javascript
// 需要登录才能访问
{ path: '/personal', component: () => import('@/views/PersonalCenter.vue'), meta: { requiresAuth: true } }

// 需要登录 + 管理员身份才能访问
{ path: '/admin', component: () => import('@/views/Admin.vue'), meta: { requiresAuth: true, requiresAdmin: true } }

// 公开页面，不需要任何权限
{ path: '/plants', component: () => import('@/views/Plants.vue') }
```

守卫的执行流程（简化版）：

```
用户访问 /admin
    |
    v
页面需要登录吗？（meta.requiresAuth）
    |
    +-- 不需要 --> 直接放行
    |
    +-- 需要 --> 有 token 吗？
                    |
                    +-- 没有 --> 跳转首页，提示登录
                    |
                    +-- 有 --> token 过期了吗？
                                    |
                                    +-- 过期了 --> 清除登录状态，跳转首页
                                    |
                                    +-- 没过期 --> 需要管理员吗？（meta.requiresAdmin）
                                                    |
                                                    +-- 不需要 --> 放行
                                                    |
                                                    +-- 需要 --> 是管理员吗？
                                                                    |
                                                                    +-- 是 --> 放行
                                                                    |
                                                                    +-- 否 --> 跳转首页，提示无权限
```

## 如何创建一个新页面？-- 五步走

假设你要创建一个"侗族药浴"页面：

### 第 1 步：创建 Vue 文件

在 `views/` 目录下新建 `HerbalBath.vue`：

```vue
<template>
  <div class="module-page">
    <!-- 页面头部 -->
    <div class="module-header">
      <h1>侗族药浴</h1>
      <p class="subtitle">传承千年的养生智慧</p>
    </div>

    <!-- 页面主体内容 -->
    <div class="page-container">
      <div class="page-main">
        <!-- 在这里组装你的子组件 -->
        <p>药浴内容区域</p>
      </div>

      <!-- 侧边栏 -->
      <div class="page-sidebar">
        <p>侧边栏区域</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 在这里引入需要的 composable 和组件
import { ref, onMounted } from 'vue'

// 页面数据
const bathList = ref([])

// 页面加载时获取数据
onMounted(async () => {
  // 调用 API 获取药浴数据
})
</script>

<style scoped>
/* 页面专属样式写在这里 */
/* 推荐使用 CSS 变量，保持风格统一 */
.herbal-bath-card {
  background: var(--bg-cream);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
}
</style>
```

### 第 2 步：注册路由

打开 `router/index.js`，在 `routes` 数组中添加：

```javascript
{
  path: '/herbal-bath',
  name: 'HerbalBath',
  component: () => import('@/views/HerbalBath.vue')
  // 如果需要登录才能访问，加上 meta：
  // meta: { requiresAuth: true }
}
```

### 第 3 步：添加导航链接

在 `AppHeader.vue` 的导航菜单中添加入口：

```vue
<el-menu-item index="/herbal-bath">侗族药浴</el-menu-item>
```

### 第 4 步：测试访问

启动开发服务器后，访问 `http://localhost:5173/herbal-bath` 查看页面。

### 第 5 步：完善功能

根据需要引入 composable（如 `useFavorite`、`useInteraction`）和子组件，逐步丰富页面内容。

## 常见错误

### 错误 1：忘记用懒加载

```javascript
// 错误：同步导入，所有页面打包到一起
import PlantsVue from '@/views/Plants.vue'
{ path: '/plants', component: PlantsVue }

// 正确：懒加载，按需加载
{ path: '/plants', component: () => import('@/views/Plants.vue') }
```

### 错误 2：在 views 里写过多业务逻辑

页面组件应该是"组装者"，而不是"实现者"。复杂的逻辑应该抽到 composable 里：

```vue
<!-- 不好的做法：页面里塞满逻辑 -->
<script setup>
const data = ref([])
const loading = ref(false)
const fetchData = async () => { /* 50行逻辑 */ }
const handleSearch = () => { /* 30行逻辑 */ }
const handleFilter = () => { /* 20行逻辑 */ }
</script>

<!-- 好的做法：逻辑抽到 composable -->
<script setup>
import { useAdminData } from '@/composables'
const { data, loading, fetchData } = useAdminData(request)
</script>
```

### 错误 3：路由 path 不一致

路由的 `path` 和导航菜单的 `index` 必须完全一致，否则点击菜单无法跳转：

```javascript
// router/index.js
{ path: '/herbal-bath', ... }  // 注意连字符

// AppHeader.vue
<el-menu-item index="/herbalBath">  // 错误！驼峰命名不一致
<el-menu-item index="/herbal-bath">  // 正确！和路由 path 一致
```
