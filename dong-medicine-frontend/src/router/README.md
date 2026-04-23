# 路由目录 (router/)

> 类比：想象一家侗族文化餐厅的**导航图**。客人进来后，导航图告诉他们"大厅往左走"、"包间在二楼"、"厨房只有员工能进"。**路由就是这张导航图**，告诉浏览器"这个 URL 应该显示哪个页面"。

## 什么是路由？

在传统网站中，每个 URL 对应服务器上的一个 HTML 文件：

```
传统多页网站：
  /index.html    --> 服务器返回首页 HTML
  /plants.html   --> 服务器返回植物页 HTML
  /about.html    --> 服务器返回关于页 HTML
```

每次点击链接，浏览器都会**刷新整个页面**，重新加载所有资源。

## 什么是 SPA 路由？

SPA（单页应用，Single Page Application）只有一个 HTML 文件，通过 JavaScript 动态切换显示的内容：

```
SPA 单页应用：
  /         --> JavaScript 把 Home.vue 渲染到页面上
  /plants   --> JavaScript 把 Plants.vue 渲染到页面上
  /about    --> JavaScript 把 About.vue 渲染到页面上
```

**关键区别**：
- 传统网站：换页 = 整个页面刷新（像换电视频道）
- SPA 路由：换页 = 只替换内容区域（像换幻灯片的一页）

**SPA 路由的好处**：
- 切换页面不需要重新加载 CSS、JS 等公共资源
- 切换速度极快，用户体验流畅
- 可以在页面切换时加过渡动画

---

## 文件清单

| 文件 | 职责 |
|------|------|
| `index.js` | 路由配置、导航守卫、token 验证缓存 |

---

## 全部 14 条路由

| 路由路径 | 路由名称 | 页面组件 | 权限 | 说明 |
|---------|---------|---------|------|------|
| `/` | `Home` | `Home.vue` | 公开 | 首页 |
| `/knowledge` | `Knowledge` | `Knowledge.vue` | 公开 | 知识库 |
| `/inheritors` | `Inheritors` | `Inheritors.vue` | 公开 | 传承人 |
| `/plants` | `Plants` | `Plants.vue` | 公开 | 药用植物 |
| `/qa` | `Qa` | `Qa.vue` | 公开 | 问答 |
| `/interact` | `Interact` | `Interact.vue` | 公开 | 互动专区 |
| `/resources` | `Resources` | `Resources.vue` | 公开 | 学习资源 |
| `/visual` | `Visual` | `Visual.vue` | 公开 | 数据可视化 |
| `/personal` | `Personal` | `PersonalCenter.vue` | 需登录 | 个人中心 |
| `/admin` | `Admin` | `Admin.vue` | 需登录+管理员 | 管理后台 |
| `/about` | `About` | `About.vue` | 公开 | 关于平台 |
| `/feedback` | `Feedback` | `Feedback.vue` | 公开 | 意见反馈 |
| `/search` | `Search` | `GlobalSearch.vue` | 公开 | 全局搜索 |
| `/:pathMatch(.*)*` | `NotFound` | `NotFound.vue` | 公开 | 404 页面 |

### 路由配置代码

```javascript
const routes = [
  // 公开页面：任何人都能访问
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/plants', name: 'Plants', component: () => import('@/views/Plants.vue') },
  { path: '/knowledge', name: 'Knowledge', component: () => import('@/views/Knowledge.vue') },
  // ... 其他公开页面

  // 需要登录的页面：meta.requiresAuth = true
  { path: '/personal', name: 'Personal', component: () => import('@/views/PersonalCenter.vue'),
    meta: { requiresAuth: true } },

  // 需要管理员权限的页面：meta.requiresAuth + meta.requiresAdmin
  { path: '/admin', name: 'Admin', component: () => import('@/views/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true } },

  // 404 兜底路由：放在最后，匹配所有未定义的路径
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') }
]
```

**注意**：`/:pathMatch(.*)*` 是 Vue Router 4 的写法，会匹配所有未定义的路径，必须放在路由数组的最后。

---

## 导航守卫 -- 路由的"保安"

导航守卫是在**路由跳转之前**执行的检查函数，决定用户能不能进入目标页面。

类比：导航守卫就像餐厅门口的保安，检查你有没有门票（token）、是不是 VIP（管理员），不符合条件就不让进。

### 守卫执行流程

```javascript
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 第 1 步：恢复登录状态
  // 如果 store 里没有 token，但 sessionStorage 里有，说明页面刷新了，需要恢复
  if (!userStore.token && sessionStorage.getItem('token')) {
    userStore.initializeFromStorage()
  }

  // 第 2 步：公开页面直接放行
  if (!to.meta.requiresAuth) {
    return next()  // 不需要登录的页面，谁都能看
  }

  // 第 3 步：检查 token 是否存在
  if (!userStore.token) {
    // 没有 token，跳转首页并标记需要登录
    return next({ path: '/', query: { redirect: to.fullPath, needLogin: 'true' } })
    // redirect 参数让登录后能跳回原来想去的页面
  }

  // 第 4 步：本地检查 token 是否过期
  if (isTokenLocallyExpired(userStore)) {
    userStore.clearAuth()
    return next({ path: '/', query: { redirect: to.fullPath, needLogin: 'true' } })
  }

  // 第 5 步：远程验证 token（带缓存）
  const isValid = await validateTokenWithCache(userStore)
  if (!isValid) {
    return next({ path: '/', query: { redirect: to.fullPath, needLogin: 'true' } })
  }

  // 第 6 步：检查管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next({ path: '/', query: { noPermission: 'true' } })
  }

  // 所有检查通过，放行
  next()
})
```

### Token 验证缓存 -- 避免重复请求

每次路由跳转都请求后端验证 token 太浪费了，所以加了缓存：

```javascript
const VALIDATION_CACHE_TTL = 60 * 1000  // 缓存 60 秒

const validationCache = {
  promise: null,    // 上次验证的 Promise
  token: null,      // 上次验证时的 token
  timestamp: 0      // 上次验证的时间戳
}

async function validateTokenWithCache(userStore) {
  const now = Date.now()
  const token = userStore.token

  // 没有 token 直接返回失败
  if (!token) return false

  // 先做本地过期检查（快速，不请求后端）
  if (isTokenLocallyExpired(userStore)) {
    userStore.clearAuth()
    return false
  }

  // 如果 token 没变，且缓存没过期（60 秒内），直接用上次的结果
  if (validationCache.token === token &&
      validationCache.promise &&
      now - validationCache.timestamp < VALIDATION_CACHE_TTL) {
    return validationCache.promise
  }

  // 缓存过期或 token 变了，重新验证
  validationCache.token = token
  validationCache.timestamp = now
  validationCache.promise = userStore.validateToken().then(isValid => {
    if (!isValid) userStore.clearAuth()
    return isValid
  })

  return validationCache.promise
}
```

**为什么缓存 60 秒？**
- 太短（如 5 秒）：频繁请求后端，浪费资源
- 太长（如 5 分钟）：管理员禁用用户后，该用户还能继续访问
- 60 秒是平衡点：既减少请求，又能及时响应权限变更

### 本地过期检查 -- isTokenLocallyExpired

```javascript
function isTokenLocallyExpired(userStore) {
  const token = userStore.token
  if (!token) return true

  try {
    // 解析 JWT 的载荷部分，提取过期时间
    const payload = JSON.parse(atob(token.split('.')[1]))
    if (!payload || !payload.exp) return true
    // 比较当前时间和过期时间
    return Date.now() >= payload.exp * 1000
  } catch {
    return true  // 解析失败，视为已过期
  }
}
```

这个函数**不请求后端**，只检查 JWT 自身的过期时间，速度极快。

---

## 如何添加一个新路由

### 第 1 步：在 routes 数组中添加路由配置

打开 `router/index.js`，在 404 路由**之前**添加：

```javascript
const routes = [
  // ... 已有路由

  // 新增路由（必须在 404 路由之前！）
  {
    path: '/herbal-bath',                    // URL 路径
    name: 'HerbalBath',                      // 路由名称（用于编程式导航）
    component: () => import('@/views/HerbalBath.vue'),  // 懒加载页面组件
    // meta: { requiresAuth: true }          // 如果需要登录才能访问
    // meta: { requiresAuth: true, requiresAdmin: true }  // 如果需要管理员权限
  },

  // 404 路由必须放在最后
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') }
]
```

### 第 2 步：在组件中使用编程式导航

```vue
<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

// 方式 1：通过路径跳转
router.push('/herbal-bath')

// 方式 2：通过路由名称跳转（推荐，更稳定）
router.push({ name: 'HerbalBath' })

// 方式 3：带参数跳转
router.push({ name: 'HerbalBath', query: { type: 'foot' } })
// URL 变成 /herbal-bath?type=foot
</script>
```

### 第 3 步：在模板中使用声明式导航

```vue
<template>
  <!-- router-link 会渲染成 <a> 标签，但不会刷新页面 -->
  <router-link to="/herbal-bath">侗族药浴</router-link>

  <!-- 也可以用命名路由 -->
  <router-link :to="{ name: 'HerbalBath' }">侗族药浴</router-link>
</template>
```

---

## 常见错误

### 错误 1：新路由放在 404 路由之后

```javascript
const routes = [
  // ... 已有路由
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') },
  { path: '/herbal-bath', name: 'HerbalBath', component: () => import('@/views/HerbalBath.vue') }
  // 错误！这个路由永远不会被匹配到，因为 404 路由已经拦截了所有路径
]

// 正确：新路由必须在 404 路由之前
const routes = [
  // ... 已有路由
  { path: '/herbal-bath', name: 'HerbalBath', component: () => import('@/views/HerbalBath.vue') },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') }
]
```

### 错误 2：用 `<a>` 标签而不是 `<router-link>`

```vue
<!-- 错误：使用 <a> 标签会导致整页刷新，失去 SPA 的优势 -->
<a href="/plants">药用植物</a>

<!-- 正确：使用 <router-link> 只替换内容区域，不刷新页面 -->
<router-link to="/plants">药用植物</router-link>
```

### 错误 3：忘记恢复 sessionStorage 中的登录状态

页面刷新后，Pinia store 的数据会丢失（内存中的数据），但 sessionStorage 中的数据还在。导航守卫的第一步就是恢复：

```javascript
// 如果 store 里没有 token，但 sessionStorage 里有
if (!userStore.token && sessionStorage.getItem('token')) {
  userStore.initializeFromStorage()  // 从 sessionStorage 恢复到 store
}
```

**如果不做这一步**：用户登录后刷新页面，store 里的 token 丢失，导航守卫会认为用户未登录，跳转到首页。

### 错误 4：路由跳转后页面不滚动到顶部

```javascript
// 默认情况下，路由跳转后会保持原来的滚动位置
// 比如你在 /plants 页面滚到底部，点击链接到 /knowledge，新页面也在底部

// 解决方案：在创建路由时配置滚动行为
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（浏览器后退），回到那个位置
    if (savedPosition) return savedPosition
    // 否则滚动到顶部
    return { top: 0 }
  }
})
```
