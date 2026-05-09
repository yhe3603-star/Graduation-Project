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
│  │  关于我们  |  快速导航  |  联系方式  │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 文件结构

```
layout/
├── AppHeader.vue   # 页面顶部导航栏
├── AppFooter.vue   # 页面底部版权栏
└── README.md       # 本文档
```

> **注意**：layout 目录没有 `index.js` 统一导出文件，使用时需直接引入组件。

---

## AppHeader —— 页面头部

**用途：** 顶部导航栏，出现在所有页面的最上方，包含品牌标识、导航菜单、搜索、通知、用户操作等完整功能

### 包含的功能区域

```
┌─────────────────────────────────────────────────────────┐
│  🌿 侗乡医药   │ 首页 知识库 传承人 药用图鉴  │ 🔔 🔍 ← 👤 │
│  非遗数字展示  │ 学习资源 问答 文化互动 数据可视化│            │
│    Logo       │      导航菜单(8项)    [更多▼]  │ 搜索 用户  │
└─────────────────────────────────────────────────────────┘
```

| 区域 | 功能 | 说明 |
|------|------|------|
| Logo | 品牌标识 | SVG 图标 + "侗乡医药" + "非遗数字展示平台"，点击回到首页 |
| 导航菜单 | 页面跳转 | 8个主菜单项 + "更多"下拉菜单（意见反馈、关于非遗） |
| 搜索框 | 全站搜索 | 输入关键词后回车跳转搜索页，点击也跳转搜索页 |
| 通知铃铛 | 消息通知 | 已登录用户可见，显示未读数量，点击展开通知面板 |
| 返回按钮 | 导航回退 | 点击返回上一页 |
| 用户区域 | 用户状态 | 未登录显示登录/注册，已登录显示头像+下拉菜单（个人中心/管理后台/退出） |
| 移动端菜单 | 响应式导航 | 768px 以下隐藏桌面导航，显示汉堡菜单按钮 |

### 导航菜单项

| 路径 | 标签 | 图标 |
|------|------|------|
| `/` | 首页 | HomeFilled |
| `/knowledge` | 知识库 | Document |
| `/inheritors` | 传承人 | User |
| `/plants` | 药用图鉴 | Picture |
| `/resources` | 学习资源 | Folder |
| `/qa` | 问答 | ChatDotRound |
| `/interact` | 文化互动 | Aim |
| `/visual` | 数据可视化 | DataLine |
| `/feedback` | 意见反馈 | InfoFilled（"更多"下拉） |
| `/about` | 关于非遗 | InfoFilled（"更多"下拉） |

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| isLoggedIn | Boolean | - | 用户是否已登录 |
| userName | String | - | 当前用户名，用于显示头像首字和用户名 |
| isAdmin | Boolean | - | 是否为管理员（控制"管理后台"入口显示） |

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| logout | - | 用户点击"退出登录" |
| showLogin | - | 未登录用户点击"登录" |
| showRegister | - | 未登录用户点击"注册" |

### 核心逻辑

- **路由高亮**：`activeIndex` 使用 `computed(() => route.path)` 自动匹配当前路由，"更多"下拉项也参与高亮判断
- **搜索功能**：`doSearch` 将关键词作为 query 参数跳转到 `/search` 页面，`goToSearch` 直接跳转搜索页
- **通知系统**：
  - 点击铃铛时加载通知列表（`GET /notifications`）和未读数（`GET /notifications/unread-count`）
  - 通知数据为 JSON 字符串，需 `JSON.parse` 解析
  - "全部已读"调用 `POST /notifications/read`
  - 组件挂载时，已登录用户自动加载通知
- **响应式适配**：
  - `<=1200px`：缩小导航间距和搜索框宽度
  - `<=1024px`：导航只显示图标，隐藏用户名和下拉箭头
  - `<=768px`：隐藏桌面导航和搜索框，显示移动端汉堡菜单
  - `<=480px`：进一步缩小间距和按钮尺寸

### 用户状态切换

```
未登录时：
┌────────────────────────────────────────────┐
│  🌿 侗乡医药  │  导航菜单  │  🔔 🔍 ← [游客 ▼] │
│                                       │ 登录  │
│                                       │ 注册  │
└────────────────────────────────────────────┘

已登录时：
┌────────────────────────────────────────────┐
│  🌿 侗乡医药  │  导航菜单  │  🔔 🔍 ← [张三 ▼] │
│                                       │ 个人中心 │
│                                       │ 管理后台 │ ← 仅管理员可见
│                                       │ 退出登录 │
└────────────────────────────────────────────┘
```

### 使用示例

```vue
<!-- 通常在 App.vue 中全局使用 -->
<AppHeader
  :is-logged-in="isLoggedIn"
  :user-name="currentUser"
  :is-admin="isAdmin"
  @logout="handleLogout"
  @show-login="showLoginDialog"
  @show-register="showRegisterDialog"
/>
```

---

## AppFooter —— 页面底部

**用途：** 底部版权信息栏，出现在所有页面的最下方，展示平台简介、快速导航和联系方式

### 包含的信息

```
┌─────────────────────────────────────────────────────┐
│  ━━━━━━━━━ 侗锦纹样装饰线 ━━━━━━━━━                │
│                                                     │
│  关于我们          快速导航        联系方式           │
│  侗乡医药非遗      知识库          邮箱：1805369567qq.com│
│  数字展示平台      传承人          地址：桂林电子科技大学│
│  致力于保护和      药用图鉴                            │
│  传承侗族医药      问答社区                            │
│  文化...                                               │
│                                                     │
│  ◆ © 2026 侗乡医药非遗数字展示平台 | 保护和传承侗族医药文化 ◆ │
└─────────────────────────────────────────────────────┘
```

### 核心逻辑

- **三栏布局**：使用 CSS Grid（`grid-template-columns: 2fr 1fr 1fr`），"关于我们"占双倍宽度
- **快速导航**：4个 router-link 链接（知识库、传承人、药用图鉴、问答社区），悬停时左移 6px 并变为玉色
- **装饰线**：
  - 顶部 3px 渐变线（玉色 → 金色 → 靛蓝 → 金色 → 玉色），模拟侗锦纹样
  - 底部版权两侧的 ◆ 菱形装饰，使用 `::before` 和 `::after` 伪元素
- **标题下划线**：每个 section 标题下方有 32px 宽的玉色下划线（`::after` 伪元素）
- **响应式适配**：768px 以下三栏变为单栏

### 使用示例

```vue
<!-- AppFooter 无需任何 props，直接使用 -->
<AppFooter />
```

---

## 完整布局示例

```vue
<!-- App.vue -->
<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/business/layout/AppHeader.vue'
import AppFooter from '@/components/business/layout/AppFooter.vue'

const isLoggedIn = ref(false)
const userName = ref('')
const isAdmin = ref(false)
</script>

<template>
  <div class="app-layout">
    <!-- 顶部导航（天花板） -->
    <AppHeader
      :is-logged-in="isLoggedIn"
      :user-name="userName"
      :is-admin="isAdmin"
      @logout="handleLogout"
      @show-login="showLoginDialog"
      @show-register="showRegisterDialog"
    />

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

## 依赖关系

```
AppHeader.vue
  ├── vue (ref, computed, onMounted)
  ├── vue-router (useRoute, useRouter)
  ├── @element-plus/icons-vue (HomeFilled, Document, User, Picture, ChatDotRound,
  │   More, ArrowDown, Search, ArrowLeft, Setting, SwitchButton, Aim, Folder,
  │   DataLine, InfoFilled, Menu, Calendar, Bell)
  └── @/utils/request

AppFooter.vue
  └── vue-router (router-link)  ← 隐式依赖，模板中直接使用 <router-link>
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

// ✅ AppHeader 已实现：用当前路由自动判断高亮
import { useRoute } from 'vue-router'
const route = useRoute()
const activeIndex = computed(() => route.path)
</script>

<template>
  <!-- ❌ 写死高亮 -->
  <nav-item :class="{ active: true }">首页</nav-item>

  <!-- ✅ AppHeader 根据路由自动高亮 -->
  <nav-item :class="{ active: activeIndex === '/' }">首页</nav-item>
  <nav-item :class="{ active: activeIndex === '/plants' }">药用图鉴</nav-item>
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

### 错误4：AppHeader 的通知数据未做容错处理

```vue
<script setup>
// ❌ 直接访问通知对象属性，如果数据格式不对会报错
const title = notification.title  // 如果 notification 是字符串而非对象？

// ✅ AppHeader 已实现：try-catch 包裹 JSON.parse，解析失败返回默认对象
notifications.value = (listRes.data || []).map(s => {
  try { return JSON.parse(s) }
  catch { return { title: '通知', content: s, createTime: '' } }
})
</script>
```

---

## 代码审查与改进建议

- [可访问性] AppHeader.vue 中 Logo 区域使用 div + @click 而非 router-link，键盘用户无法聚焦和导航
- [数据] AppFooter.vue 中邮箱地址格式错误：`1805369567qq.com` 缺少 `@` 符号
- [安全] AppHeader.vue 中通知数据使用 `JSON.parse` 解析，如果数据被篡改可能执行恶意代码
- [性能] AppHeader.vue 每次点击铃铛都重新请求通知列表，应考虑轮询或 WebSocket 推送
- [结构] layout 目录缺少 `index.js` 统一导出文件，与其他组件目录风格不一致
