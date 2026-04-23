# 布局组件（Layout Components）

## 什么是布局组件？

类比：**房子的框架——天花板和地板**——不管你把家具怎么摆，天花板和地板始终在那里。每个房间都有天花板和地板，就像每个页面都有头部导航和底部版权。

布局组件定义了页面的整体结构，所有页面共享相同的头部和底部，确保整个平台风格统一。

```
┌──────────────────────────────────────────┐
│  AppHeader（天花板）                      │
│  ┌────────────────────────────────────┐  │
│  │  Logo  |  导航菜单  |  搜索  |  用户 │  │
│  └────────────────────────────────────┘  │
├──────────────────────────────────────────┤
│                                          │
│              页面主内容区域                │
│          （每个页面内容不同）               │
│                                          │
├──────────────────────────────────────────┤
│  AppFooter（地板）                        │
│  ┌────────────────────────────────────┐  │
│  │  版权信息  |  友情链接  |  联系方式  │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## AppHeader —— 页面头部

**用途：** 顶部导航栏，出现在所有页面的最上方

### 包含的功能区域

```
┌─────────────────────────────────────────────────────┐
│  🌿 侗乡医药  │ 首页 药用植物 传统疗法 传承人 │ 🔍 👤 │
│               │                    学习资源 问答    │      │
│    Logo       │      导航菜单       │ 搜索 用户    │
└─────────────────────────────────────────────────────┘
```

| 区域 | 功能 | 说明 |
|------|------|------|
| Logo | 品牌标识 | 点击回到首页 |
| 导航菜单 | 页面跳转 | 药用植物、传统疗法、传承人、学习资源、知识问答 |
| 搜索框 | 全站搜索 | 输入关键词搜索药用植物、知识等 |
| 用户区 | 用户状态 | 未登录显示登录按钮，已登录显示头像和下拉菜单 |

### 用户状态切换

```
未登录时：
┌────────────────────────────────────────────┐
│  🌿 侗乡医药  │  导航菜单  │  🔍  [登录]   │
└────────────────────────────────────────────┘

已登录时：
┌────────────────────────────────────────────┐
│  🌿 侗乡医药  │  导航菜单  │  🔍  [头像 ▼] │
│                                       │ 个人中心 │
│                                       │ 退出登录 │
│                                       └─────────│
└────────────────────────────────────────────┘
```

### 使用示例

```vue
<!-- AppHeader 会自动根据路由高亮当前导航项 -->
<template>
  <AppHeader />
</template>

<!-- 通常在 App.vue 中全局使用，所有页面共享 -->
```

---

## AppFooter —— 页面底部

**用途：** 底部版权信息栏，出现在所有页面的最下方

### 包含的信息

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│  关于我们    友情链接          联系方式               │
│  平台介绍    贵州民族大学       邮箱：contact@...     │
│  使用条款    侗医药研究会       电话：0855-xxx       │
│  隐私政策    非遗保护中心       地址：贵州省...       │
│                                                     │
│  ─────────────────────────────────────────────────  │
│  Copyright © 2025 侗乡医药数字展示平台  All Rights Reserved │
└─────────────────────────────────────────────────────┘
```

### 使用示例

```vue
<!-- AppFooter 通常和 AppHeader 一起在 App.vue 中使用 -->
<template>
  <div class="app">
    <AppHeader />
    <main class="main-content">
      <router-view />  <!-- 页面内容在这里切换 -->
    </main>
    <AppFooter />
  </div>
</template>
```

---

## 完整布局示例

```vue
<!-- App.vue -->
<script setup>
import AppHeader from '@/components/business/layout/AppHeader.vue'
import AppFooter from '@/components/business/layout/AppFooter.vue'
</script>

<template>
  <div class="app-layout">
    <!-- 顶部导航（天花板） -->
    <AppHeader />

    <!-- 主内容区域（房间里的空间，每个页面不同） -->
    <main class="main-content">
      <!-- router-view 是 Vue Router 提供的组件 -->
      <!-- 它会根据当前路由自动渲染对应的页面组件 -->
      <router-view />
    </main>

    <!-- 底部版权（地板） -->
    <AppFooter />
  </div>
</template>

<style scoped>
.app-layout {
  /* 最小高度100vh，确保footer始终在页面底部 */
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  /* flex: 1 让主内容区域自动填满剩余空间 */
  flex: 1;
  padding: 20px;
}
</style>
```

---

## 常见错误

### 错误1：每个页面都单独引入 Header 和 Footer

```vue
<!-- ❌ 每个页面都写一遍，修改时要改10个文件 -->
<!-- HomeView.vue -->
<template>
  <AppHeader />
  <div>首页内容</div>
  <AppFooter />
</template>

<!-- PlantView.vue -->
<template>
  <AppHeader />  <!-- 重复了！ -->
  <div>植物内容</div>
  <AppFooter />  <!-- 重复了！ -->
</template>

<!-- ✅ 在 App.vue 中统一引入，所有页面自动拥有 -->
<!-- App.vue -->
<template>
  <AppHeader />
  <router-view />  <!-- 所有页面共享头部和底部 -->
  <AppFooter />
</template>
```

### 错误2：导航高亮没有跟随路由变化

```vue
<script setup>
// ❌ 用变量手动控制高亮，切换页面时忘记更新
const activeNav = ref('home')

// ✅ 用当前路由自动判断高亮
import { useRoute } from 'vue-router'
const route = useRoute()

// 根据当前路由路径自动判断哪个导航项高亮
const isActive = (path) => {
  return route.path === path
}
</script>

<template>
  <!-- ❌ 写死高亮 -->
  <nav-item :class="{ active: true }">首页</nav-item>

  <!-- ✅ 根据路由自动高亮 -->
  <nav-item :class="{ active: isActive('/home') }">首页</nav-item>
  <nav-item :class="{ active: isActive('/plants') }">药用植物</nav-item>
</template>
```

### 错误3：Footer 没有固定在底部

```css
/* ❌ 内容少时，footer 会跑到页面中间 */
.app-layout {
  /* 没有设置最小高度 */
}

/* ✅ 用 flex 布局确保 footer 始终在底部 */
.app-layout {
  min-height: 100vh;      /* 最小高度 = 整个视口高度 */
  display: flex;
  flex-direction: column;  /* 垂直排列 */
}

.main-content {
  flex: 1;  /* 自动填满 header 和 footer 之间的空间 */
}
```
