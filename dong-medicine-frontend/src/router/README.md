# 路由配置目录 (router)

本目录存放 Vue Router 路由配置，用于管理页面导航。

## 目录

- [什么是路由？](#什么是路由)
- [目录结构](#目录结构)
- [路由配置](#路由配置)
- [路由守卫](#路由守卫)
- [路由元信息](#路由元信息)

---

## 什么是路由？

### 路由的概念

**路由**是控制URL和页面对应关系的机制。它就像一个"导航系统"——当用户访问不同的URL时，路由决定显示哪个页面。

### 路由的作用

```
┌─────────────────────────────────────────────────────────────────┐
│                        路由工作原理                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  用户访问 URL                                                    │
│       │                                                         │
│       ▼                                                         │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Vue Router                           │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  URL匹配：                                       │    │   │
│  │  │  /           → Home.vue                         │    │   │
│  │  │  /plants    → Plants.vue                        │    │   │
│  │  │  /knowledge → Knowledge.vue                     │    │   │
│  │  │  /admin     → Admin.vue (需要登录)               │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│       │                                                         │
│       ▼                                                         │
│  渲染对应的页面组件                                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 单页应用（SPA）的路由

本项目是单页应用（SPA），路由切换不会刷新整个页面，而是动态替换内容区域。

```
传统网站：每次切换页面都会刷新整个页面
SPA：只更新内容区域，保持头部、底部不变
```

---

## 目录结构

```
router/
│
└── index.js                           # 路由配置入口
```

---

## 路由配置

### 完整路由配置

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// 创建路由实例
const router = createRouter({
  // 使用HTML5历史模式（URL不带#号）
  history: createWebHistory(),
  
  // 路由规则
  routes: [
    // 首页
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { title: '首页' }
    },
    
    // 药用植物
    {
      path: '/plants',
      name: 'Plants',
      component: () => import('@/views/Plants.vue'),
      meta: { title: '药用植物' }
    },
    
    // 传承人
    {
      path: '/inheritors',
      name: 'Inheritors',
      component: () => import('@/views/Inheritors.vue'),
      meta: { title: '传承人' }
    },
    
    // 知识库
    {
      path: '/knowledge',
      name: 'Knowledge',
      component: () => import('@/views/Knowledge.vue'),
      meta: { title: '知识库' }
    },
    
    // 问答社区
    {
      path: '/qa',
      name: 'Qa',
      component: () => import('@/views/Qa.vue'),
      meta: { title: '问答社区' }
    },
    
    // 互动专区
    {
      path: '/interact',
      name: 'Interact',
      component: () => import('@/views/Interact.vue'),
      meta: { title: '互动专区' }
    },
    
    // 学习资源
    {
      path: '/resources',
      name: 'Resources',
      component: () => import('@/views/Resources.vue'),
      meta: { title: '学习资源' }
    },
    
    // 数据可视化
    {
      path: '/visual',
      name: 'Visual',
      component: () => import('@/views/Visual.vue'),
      meta: { title: '数据可视化' }
    },
    
    // 个人中心（需要登录）
    {
      path: '/personal',
      name: 'PersonalCenter',
      component: () => import('@/views/PersonalCenter.vue'),
      meta: { 
        title: '个人中心',
        requiresAuth: true  // 需要登录
      }
    },
    
    // 管理后台（需要登录+管理员权限）
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/views/Admin.vue'),
      meta: { 
        title: '管理后台',
        requiresAuth: true,
        requiresAdmin: true  // 需要管理员权限
      }
    },
    
    // 关于
    {
      path: '/about',
      name: 'About',
      component: () => import('@/views/About.vue'),
      meta: { title: '关于' }
    },
    
    // 意见反馈
    {
      path: '/feedback',
      name: 'Feedback',
      component: () => import('@/views/Feedback.vue'),
      meta: { title: '意见反馈' }
    },
    
    // 全局搜索
    {
      path: '/search',
      name: 'GlobalSearch',
      component: () => import('@/views/GlobalSearch.vue'),
      meta: { title: '搜索' }
    },
    
    // 404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: { title: '页面不存在' }
    }
  ],
  
  // 滚动行为
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router
```

### 路由懒加载

```javascript
// 使用动态导入实现懒加载
// 只有访问该路由时才会加载对应的组件
component: () => import('@/views/Plants.vue')

// 好处：
// 1. 减少首屏加载时间
// 2. 按需加载，提高性能
```

---

## 路由守卫

### 全局前置守卫

```javascript
// router/index.js
import { useUserStore } from '@/stores/user'
import { usePageLoading } from '@/composables/usePageLoading'

// Token本地过期检查
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

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 1. 显示页面加载动画
  // ...
  
  // 2. 更新页面标题
  document.title = to.meta.title ? `${to.meta.title} - 侗乡医药` : '侗乡医药'
  
  // 3. 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 检查Token是否存在
    if (!userStore.token) {
      next('/')
      return
    }
    
    // 检查Token是否本地过期
    if (isTokenLocallyExpired(userStore)) {
      userStore.clearAuth()
      next('/')
      return
    }
    
    // 验证Token有效性（带缓存）
    const isValid = await userStore.validateTokenAction()
    if (!isValid) {
      next('/')
      return
    }
    
    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next('/')
      return
    }
  }
  
  next()
})

// 全局后置守卫
router.afterEach(() => {
  // 隐藏页面加载动画
  // ...
})
```

### 路由守卫执行顺序

```
1. 全局前置守卫 beforeEach
   ↓
2. 路由独享守卫 beforeEnter（如果有）
   ↓
3. 组件内守卫 beforeRouteEnter（如果有）
   ↓
4. 全局后置守卫 afterEach
```

---

## 路由元信息

### meta 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| title | String | 页面标题 |
| requiresAuth | Boolean | 是否需要登录 |
| requiresAdmin | Boolean | 是否需要管理员权限 |

### 使用路由元信息

```javascript
// 在路由配置中定义
{
  path: '/admin',
  component: () => import('@/views/Admin.vue'),
  meta: { 
    title: '管理后台',
    requiresAuth: true,
    requiresAdmin: true
  }
}

// 在守卫中使用
router.beforeEach((to, from, next) => {
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    // ...
  }
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    // ...
  }
  
  // 设置页面标题
  document.title = to.meta.title || '侗乡医药'
})
```

---

## 编程式导航

### 基本导航

```javascript
import { useRouter } from 'vue-router'

const router = useRouter()

// 跳转到指定路径
router.push('/plants')

// 跳转到命名路由
router.push({ name: 'Plants' })

// 带参数跳转
router.push({ 
  name: 'PlantDetail', 
  params: { id: 1 } 
})

// 带查询参数
router.push({ 
  path: '/plants', 
  query: { category: '根茎类' } 
})

// 替换当前路由（不留历史记录）
router.replace('/plants')

// 后退
router.back()

// 前进
router.forward()

// 前进/后退指定步数
router.go(-2)  // 后退2步
```

### 获取路由信息

```javascript
import { useRoute } from 'vue-router'

const route = useRoute()

// 当前路径
console.log(route.path)  // /plants/1

// 当前路由名称
console.log(route.name)  // PlantDetail

// 路由参数
console.log(route.params)  // { id: '1' }

// 查询参数
console.log(route.query)  // { category: '根茎类' }

// 路由元信息
console.log(route.meta)  // { title: '植物详情' }
```

---

## 最佳实践

### 1. 使用命名路由

```javascript
// ✅ 推荐：使用命名路由
router.push({ name: 'Plants' })

// ❌ 不推荐：使用路径
router.push('/plants')
```

### 2. 懒加载路由组件

```javascript
// ✅ 推荐：懒加载
component: () => import('@/views/Plants.vue')

// ❌ 不推荐：直接导入（会增加首屏加载时间）
import Plants from '@/views/Plants.vue'
component: Plants
```

### 3. 合理使用路由守卫

```javascript
// 全局守卫：用于通用逻辑（如登录验证）
router.beforeEach((to, from, next) => { ... })

// 路由独享守卫：用于特定路由的逻辑
{
  path: '/admin',
  beforeEnter: (to, from, next) => { ... }
}

// 组件内守卫：用于组件特定逻辑
onBeforeRouteLeave((to, from, next) => {
  // 离开前确认
  if (hasUnsavedChanges) {
    const answer = confirm('确定要离开吗？未保存的更改将丢失。')
    if (!answer) return
  }
  next()
})
```

---

**相关文档**

- [Vue Router 官方文档](https://router.vuejs.org/zh/)
- [Vue 3 路由](https://cn.vuejs.org/guide/scaling-up/routing.html)

---

**最后更新时间**：2026年4月3日
