# Router 路由配置目录

本目录包含项目的 Vue Router 路由配置。

## 文件结构

```
router/
├── index.js              # 路由配置入口
└── README.md             # 说明文档
```

## 路由配置说明

### index.js - 路由配置入口

**路由定义**:

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

---

## 路由守卫

### Token验证缓存机制

```javascript
const VALIDATION_CACHE_TTL = 60 * 1000  // 60秒缓存
const validationCache = {
  promise: null,
  token: null,
  timestamp: 0
}
```

### 本地Token过期检查

```javascript
function isTokenLocallyExpired(userStore) {
  const token = userStore.token
  if (!token) return true
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    if (!payload || !payload.exp) return true
    return Date.now() >= payload.exp * 1000
  } catch {
    return true
  }
}
```

### Token验证（带缓存）

```javascript
async function validateTokenWithCache(userStore) {
  const now = Date.now()
  const token = userStore.token
  
  if (!token) return false
  
  // 本地过期检查
  if (isTokenLocallyExpired(userStore)) {
    userStore.clearAuth()
    return false
  }
  
  // 缓存命中
  if (validationCache.token === token && 
      validationCache.promise && 
      now - validationCache.timestamp < VALIDATION_CACHE_TTL) {
    return validationCache.promise
  }
  
  // 发起验证请求
  validationCache.token = token
  validationCache.timestamp = now
  validationCache.promise = userStore.validateToken().then(isValid => {
    if (!isValid) userStore.clearAuth()
    return isValid
  })
  
  return validationCache.promise
}
```

### 路由守卫逻辑

```javascript
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 1. 初始化用户状态
  if (!userStore.token && sessionStorage.getItem('token')) {
    userStore.initializeFromStorage()
  }

  // 2. 检查是否需要认证
  if (!to.meta.requiresAuth) {
    return next()
  }

  // 3. 检查是否有token
  if (!userStore.token) {
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  // 4. 本地Token过期检查
  if (isTokenLocallyExpired(userStore)) {
    userStore.clearAuth()
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  // 5. 服务端Token验证（带缓存）
  const isValid = await validateTokenWithCache(userStore)
  if (!isValid) {
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  // 6. 管理员权限验证
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next({ path: "/", query: { noPermission: "true" } })
  }

  next()
})
```

---

## 路由元信息

| 字段 | 类型 | 说明 |
|------|------|------|
| `requiresAuth` | boolean | 是否需要登录 |
| `requiresAdmin` | boolean | 是否需要管理员权限 |
| `title` | string | 页面标题 |

---

## 开发规范

1. **路由命名**: 使用大驼峰命名法
2. **懒加载**: 使用动态导入 `() => import('@/views/xxx.vue')`
3. **权限控制**: 通过 `meta` 字段配置
4. **重定向**: 登录后重定向使用 `query.redirect`

---

**最后更新时间**: 2026年3月27日
