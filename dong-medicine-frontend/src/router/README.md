# 路由配置目录 (router)

本目录存放Vue Router路由配置文件，管理应用的所有页面路由。

## 📖 什么是路由？

路由是单页应用(SPA)中管理页面导航的机制。它允许用户通过URL访问不同的页面，而无需重新加载整个页面。

## 📁 文件列表

| 文件名 | 功能说明 |
|--------|----------|
| `index.js` | 路由配置主文件 |

## 📦 详细说明

### index.js - 路由配置

**主要功能:**
- 定义所有页面路由
- 配置路由导航守卫
- 处理路由权限控制

**路由配置列表:**

| 路由路径 | 页面组件 | 说明 | 权限 |
|----------|----------|------|------|
| `/` | Home.vue | 首页 | 公开 |
| `/about` | About.vue | 关于非遗 | 公开 |
| `/plants` | Plants.vue | 药用植物 | 公开 |
| `/knowledge` | Knowledge.vue | 知识库 | 公开 |
| `/inheritors` | Inheritors.vue | 传承人 | 公开 |
| `/resources` | Resources.vue | 学习资源 | 公开 |
| `/qa` | Qa.vue | 问答社区 | 公开 |
| `/interact` | Interact.vue | 文化互动 | 公开 |
| `/visual` | Visual.vue | 数据可视化 | 公开 |
| `/personal` | PersonalCenter.vue | 个人中心 | 需登录 |
| `/feedback` | Feedback.vue | 意见反馈 | 需登录 |
| `/admin` | Admin.vue | 管理后台 | 需管理员 |
| `/search` | GlobalSearch.vue | 全局搜索 | 公开 |
| `*` | NotFound.vue | 404页面 | 公开 |

**代码结构:**
```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  // ... 其他路由
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return { top: 0 }
  }
})

// 导航守卫
router.beforeEach((to, from, next) => {
  // 权限验证逻辑
  next()
})

export default router
```

## 🎯 使用规范

### 路由定义
```javascript
// 基础路由
{
  path: '/page',
  name: 'PageName',
  component: () => import('@/views/Page.vue'),
  meta: { title: '页面标题' }
}

// 嵌套路由
{
  path: '/parent',
  component: Parent,
  children: [
    {
      path: 'child',
      component: Child
    }
  ]
}

// 动态路由
{
  path: '/detail/:id',
  component: Detail,
  props: true
}
```

### 路由跳转
```javascript
// 编程式导航
import { useRouter } from 'vue-router'

const router = useRouter()

// 跳转到指定路由
router.push('/page')
router.push({ name: 'PageName' })
router.push({ path: '/detail/123' })

// 替换当前路由
router.replace('/page')

// 返回上一页
router.back()
router.go(-1)
```

### 获取路由参数
```javascript
import { useRoute } from 'vue-router'

const route = useRoute()

// 获取路径参数
const id = route.params.id

// 获取查询参数
const keyword = route.query.keyword
```

### 导航守卫
```javascript
// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 检查权限
  if (to.meta.requiresAuth && !isLoggedIn()) {
    next('/login')
  } else {
    next()
  }
})

// 全局后置守卫
router.afterEach((to, from) => {
  // 更新页面标题
  document.title = to.meta.title || '侗乡医药'
})
```

### 路由元信息 (meta)
```javascript
meta: {
  title: '页面标题',        // 页面标题
  requiresAuth: true,      // 是否需要登录
  requiresAdmin: true,     // 是否需要管理员权限
  keepAlive: true          // 是否缓存组件
}
```

## 📚 扩展阅读

- [Vue Router 官方文档](https://router.vuejs.org/zh/)
- [Vue Router 导航守卫](https://router.vuejs.org/zh/guide/advanced/navigation-guards.html)
- [Vue Router 路由懒加载](https://router.vuejs.org/zh/guide/advanced/lazy-loading.html)
