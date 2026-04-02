# 布局组件目录 (layout)

本目录存放页面布局相关的组件，如头部、底部、侧边栏等。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| AppHeader.vue | 应用顶部导航栏，包含Logo、菜单、用户信息 |
| AppFooter.vue | 应用底部，包含版权信息、友情链接 |

---

## AppHeader.vue - 应用头部

顶部导航栏组件，包含：
- Logo和网站名称
- 导航菜单
- 搜索入口
- 用户信息/登录按钮

```vue
<template>
  <header class="app-header">
    <div class="header-left">
      <router-link to="/" class="logo">
        <img src="@/assets/logo.png" alt="Logo">
        <span>侗乡医药</span>
      </router-link>
    </div>
    
    <nav class="header-nav">
      <router-link to="/plants">药用植物</router-link>
      <router-link to="/knowledge">知识库</router-link>
      <router-link to="/inheritors">传承人</router-link>
    </nav>
    
    <div class="header-right">
      <button @click="goToSearch">搜索</button>
      <div v-if="userStore.isLoggedIn">
        <span>{{ userStore.username }}</span>
        <button @click="logout">退出</button>
      </div>
      <button v-else @click="goToLogin">登录</button>
    </div>
  </header>
</template>
```

---

## AppFooter.vue - 应用底部

底部组件，包含：
- 版权信息
- 友情链接
- 联系方式

```vue
<template>
  <footer class="app-footer">
    <div class="footer-content">
      <p>© 2024 侗乡医药数字展示平台</p>
      <div class="footer-links">
        <a href="#">关于我们</a>
        <a href="#">联系方式</a>
      </div>
    </div>
  </footer>
</template>
```

---

**最后更新时间**：2026年4月3日
