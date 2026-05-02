# 路由目录 (router/)

使用 **Vue Router 4** 实现客户端路由。采用 `createWebHistory` History 模式，通过懒加载动态导入页面组件。

## 路由配置

**文件：** `index.js`

### 全部 16 条路由

| 路径 | 路由名称 | 页面组件 | 特殊配置 | 说明 |
|------|---------|---------|---------|------|
| `/` | `Home` | `views/Home.vue` | -- | 平台首页 |
| `/knowledge` | `Knowledge` | `views/Knowledge.vue` | -- | 非遗医药知识库 |
| `/inheritors` | `Inheritors` | `views/Inheritors.vue` | -- | 传承人风采 |
| `/plants` | `Plants` | `views/Plants.vue` | -- | 药用资源图鉴 |
| `/qa` | `Qa` | `views/Qa.vue` | `keepAlive: true` | 问答社区（缓存） |
| `/interact` | `Interact` | `views/Interact.vue` | -- | 文化互动专区 |
| `/resources` | `Resources` | `views/Resources.vue` | -- | 学习资源 |
| `/visual` | `Visual` | `views/Visual.vue` | -- | 数据可视化 |
| `/personal` | `Personal` | `views/PersonalCenter.vue` | `requiresAuth: true` | 个人中心（需登录） |
| `/admin` | `Admin` | `views/Admin.vue` | `requiresAuth: true, requiresAdmin: true` | 管理后台（需管理员） |
| `/about` | `About` | `views/About.vue` | -- | 关于平台 |
| `/feedback` | `Feedback` | `views/Feedback.vue` | -- | 意见反馈 |
| `/search` | `Search` | `views/GlobalSearch.vue` | -- | 全局搜索 |
| `/compare` | `Compare` | `views/PlantCompare.vue` | `keepAlive: true` | 药材对比（缓存） |
| `/solar-terms` | `SolarTerms` | `views/SolarTerms.vue` | `keepAlive: true` | 节气采药（缓存） |
| `/:pathMatch(.*)*` | `NotFound` | `views/NotFound.vue` | -- | 404 页面（通配符） |

### 路由配置特点

1. **懒加载：** 所有页面组件使用动态 `import()` 语法，Vite 自动进行代码分割

   ```js
   { path: "/plants", name: "Plants", component: () => import("@/views/Plants.vue") }
   ```

2. **keepAlive 缓存：** 3 个页面启用了 `keepAlive`（问答、对比、节气），在 `App.vue` 中通过 `<keep-alive>` 包裹避免切换时销毁组件状态

3. **认证控制：** 2 个页面需要登录（个人中心、管理后台），通过路由元信息 `meta.requiresAuth` 控制

4. **角色控制：** 管理后台额外需要 `meta.requiresAdmin`，非管理员访问会被重定向

5. **通配符路由：** `/:pathMatch(.*)*` 匹配所有未定义路径，显示 404 页面

---

## 导航守卫 (Navigation Guards)

### beforeEach -- 全局前置守卫

在 `router/index.js` 中定义的 `beforeEach` 守卫处理以下逻辑：

```js
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 1. 尝试从 localStorage 恢复 token（如果 store 中还没有）
  if (!userStore.token && localStorage.getItem('token')) {
    userStore.initializeFromStorage()
  }

  // 2. 不需要认证的页面直接放行
  if (!to.meta.requiresAuth) {
    return next()
  }

  // 3. 需要认证但无 token -> 重定向到首页
  if (!userStore.token) {
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  // 4. 本地检查 token 是否过期（解码 JWT exp）
  if (isTokenLocallyExpired(userStore)) {
    userStore.clearAuth()
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  // 5. 服务端验证 token（带 60 秒缓存）
  const isValid = await validateTokenWithCache(userStore)
  if (!isValid) {
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  // 6. 需要管理员权限但用户不是 admin -> 重定向
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next({ path: "/", query: { noPermission: "true" } })
  }

  next()
})
```

### App.vue 中的路由守卫

`App.vue` 中还有一个 `beforeEach` 用于页面加载状态：

```js
// 路由切换时显示加载状态
router.beforeEach((to, from, next) => {
  if (to.path !== from.path) {
    pageLoading.value = true
  }
  next()
})

// 路由加载完成后延迟 100ms 隐藏（防止闪烁）
router.afterEach(() => {
  setTimeout(() => {
    pageLoading.value = false
  }, 100)
})
```

### Token 验证缓存

为了减少服务端请求，token 验证结果缓存 60 秒（`VALIDATION_CACHE_TTL = 60 * 1000`）：

```js
const validationCache = {
  promise: null,   // 缓存的验证 Promise
  token: null,     // 对应的 token 值
  timestamp: 0     // 缓存时间戳
}
```

缓存 key 为 token 值，同一 token 在 60 秒内不会重复请求服务端验证。

### JWT 本地检查

客户端解码 JWT 的 payload 检查 `exp` 字段：

```js
function isTokenLocallyExpired(userStore) {
  const token = userStore.token
  if (!token) return true
  
  const parts = token.split('.')
  if (parts.length !== 3) return false
  const payload = JSON.parse(atob(parts[1]))
  if (!payload || !payload.exp) return false
  return Date.now() >= payload.exp * 1000
}
```

---

## App.vue 中的布局条件

`App.vue` 根据当前路由控制布局元素的显隐：

| 条件 | Header | Footer | 说明 |
|------|--------|--------|------|
| 普通页面 | 显示 | 显示 | 默认布局 |
| `/admin` | 隐藏 | 隐藏 | 管理后台使用独立布局 |
| `NotFound` | 隐藏 | 隐藏 | 404 页面简洁布局 |

```js
const isAdminPage = computed(() => route.path === "/admin")
const isNotFoundPage = computed(() => route.name === "NotFound")
```

---

## 路由与组件映射

```
/ ──────────────────> Home.vue
/knowledge ─────────> Knowledge.vue
/inheritors ────────> Inheritors.vue
/plants ────────────> Plants.vue
/qa ────────────────> Qa.vue (keepAlive)
/interact ──────────> Interact.vue
/resources ─────────> Resources.vue
/visual ────────────> Visual.vue
/personal ──────────> PersonalCenter.vue (requiresAuth)
/admin ─────────────> Admin.vue (requiresAuth + requiresAdmin)
/about ─────────────> About.vue
/feedback ──────────> Feedback.vue
/search ────────────> GlobalSearch.vue
/compare ───────────> PlantCompare.vue (keepAlive)
/solar-terms ──────> SolarTerms.vue (keepAlive)
/* ─────────────────> NotFound.vue
```
