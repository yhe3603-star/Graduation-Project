# 页面视图目录 (views)

本目录存放页面级组件，每个文件对应一个路由页面。

## 📁 文件列表

| 文件名 | 功能说明 | 路由路径 |
|--------|----------|----------|
| `Home.vue` | 首页 | `/` |
| `About.vue` | 关于非遗 | `/about` |
| `Plants.vue` | 药用植物 | `/plants` |
| `Knowledge.vue` | 知识库 | `/knowledge` |
| `Inheritors.vue` | 传承人 | `/inheritors` |
| `Resources.vue` | 学习资源 | `/resources` |
| `Qa.vue` | 问答社区 | `/qa` |
| `Interact.vue` | 文化互动 | `/interact` |
| `Visual.vue` | 数据可视化 | `/visual` |
| `PersonalCenter.vue` | 个人中心 | `/personal` |
| `Feedback.vue` | 意见反馈 | `/feedback` |
| `Admin.vue` | 管理后台 | `/admin` |
| `GlobalSearch.vue` | 全局搜索 | `/search` |
| `NotFound.vue` | 404页面 | `*` |

## 📦 页面详细说明

### 1. Home.vue - 首页

**功能:**
- 展示平台概览
- 快速导航入口
- 统计数据展示
- 特色内容推荐

**主要组件:**
- Hero区域
- 统计卡片
- 导航入口网格
- 特色模块展示

### 2. About.vue - 关于非遗

**功能:**
- 侗族医药文化介绍
- 平台数据展示
- 功能特色说明

**主要内容:**
- 文化背景介绍
- 平台统计数据
- 功能模块说明

### 3. Plants.vue - 药用植物

**功能:**
- 植物列表展示
- 搜索筛选
- 植物详情查看
- 收藏功能

**主要组件:**
- 搜索过滤器
- 植物卡片网格
- 分页组件
- 详情对话框

### 4. Knowledge.vue - 知识库

**功能:**
- 知识内容列表
- 分类筛选
- 知识详情查看
- 收藏功能

**主要组件:**
- 搜索过滤器
- 知识卡片网格
- 分页组件
- 详情对话框

### 5. Inheritors.vue - 传承人

**功能:**
- 传承人列表
- 搜索筛选
- 传承人详情

**主要组件:**
- 搜索过滤器
- 传承人卡片网格
- 分页组件
- 详情对话框

### 6. Resources.vue - 学习资源

**功能:**
- 资源列表展示
- 资源分类筛选
- 资源预览/下载

**主要组件:**
- 搜索过滤器
- 资源列表
- 分页组件
- 预览对话框

### 7. Qa.vue - 问答社区

**功能:**
- 问答列表展示
- 问题分类筛选
- AI智能问答

**主要组件:**
- 搜索过滤器
- 问答列表
- AI聊天卡片
- 详情对话框

### 8. Interact.vue - 文化互动

**功能:**
- 植物识别游戏
- 趣味答题
- 游戏记录查看

**主要组件:**
- 难度选择
- 植物游戏组件
- 答题组件
- 游戏记录

### 9. Visual.vue - 数据可视化

**功能:**
- 平台数据统计
- 图表可视化展示
- 数据分析

**主要组件:**
- 统计卡片
- 各类图表
- 数据表格

### 10. PersonalCenter.vue - 个人中心

**功能:**
- 用户信息管理
- 收藏管理
- 游戏记录查看

**主要组件:**
- 用户信息卡片
- 收藏列表
- 记录列表

### 11. Feedback.vue - 意见反馈

**功能:**
- 提交反馈意见
- 查看反馈历史

**主要组件:**
- 反馈表单
- 反馈列表

### 12. Admin.vue - 管理后台

**功能:**
- 数据管理
- 用户管理
- 系统配置

**主要组件:**
- 侧边栏导航
- 数据表格
- 表单对话框
- 详情对话框

### 13. GlobalSearch.vue - 全局搜索

**功能:**
- 全站内容搜索
- 搜索结果展示

**主要组件:**
- 搜索框
- 结果列表
- 分类筛选

### 14. NotFound.vue - 404页面

**功能:**
- 显示页面不存在提示
- 提供返回首页入口

## 🎯 页面结构规范

### 基本结构
```vue
<template>
  <div class="page-name module-page">
    <!-- 页面头部 -->
    <div class="module-header">
      <h1>页面标题</h1>
      <p class="subtitle">页面描述</p>
    </div>
    
    <!-- 页面内容 -->
    <div class="page-content">
      <!-- 内容区域 -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// 导入组件和工具

// 页面逻辑
</script>

<style scoped>
/* 页面样式 */
</style>
```

### 最佳实践
1. **单一职责**: 每个页面只负责一个功能模块
2. **组件复用**: 将可复用的部分提取为组件
3. **逻辑分离**: 使用composables分离业务逻辑
4. **样式隔离**: 使用scoped避免样式污染

## 📚 扩展阅读

- [Vue 3 单文件组件](https://cn.vuejs.org/guide/scaling-up/sfc.html)
- [Vue Router 路由](https://router.vuejs.org/zh/)
