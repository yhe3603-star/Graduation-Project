# 管理后台组件目录 (admin)

本目录存放管理后台专用的组件。

## 目录结构

```
admin/
│
├── dialogs/                           # 管理对话框
│   └── 各种编辑对话框
│
├── forms/                             # 管理表单
│   └── 各种数据表单
│
├── AdminDashboard.vue                 # 管理仪表盘
├── AdminDataTable.vue                 # 数据表格
└── AdminSidebar.vue                   # 管理侧边栏
```

---

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| AdminDashboard.vue | 管理仪表盘，数据统计概览 |
| AdminDataTable.vue | 数据表格组件，通用CRUD表格 |
| AdminSidebar.vue | 管理侧边栏导航 |

---

## AdminDashboard.vue - 管理仪表盘

展示后台数据统计，包括：
- 用户数量统计
- 内容数量统计
- 最近活动
- 数据图表

```vue
<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.userCount }}</div>
        <div class="stat-label">用户总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.plantCount }}</div>
        <div class="stat-label">植物总数</div>
      </div>
      <!-- 更多统计... -->
    </div>
    
    <!-- 图表区域 -->
    <div class="chart-area">
      <!-- ECharts图表 -->
    </div>
  </div>
</template>
```

---

## AdminDataTable.vue - 数据表格

通用的数据表格组件，支持：
- 数据展示
- 搜索过滤
- 分页
- 编辑/删除操作

```vue
<template>
  <div class="admin-data-table">
    <!-- 工具栏 -->
    <div class="table-toolbar">
      <input v-model="keyword" placeholder="搜索...">
      <button @click="handleAdd">添加</button>
    </div>
    
    <!-- 表格 -->
    <el-table :data="tableData">
      <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <button @click="handleEdit(row)">编辑</button>
          <button @click="handleDelete(row)">删除</button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      :current="page"
      :total="total"
      @change="handlePageChange"
    />
  </div>
</template>
```

---

## AdminSidebar.vue - 管理侧边栏

管理后台的导航菜单。

```vue
<template>
  <div class="admin-sidebar">
    <div class="sidebar-header">
      <h2>管理后台</h2>
    </div>
    
    <nav class="sidebar-nav">
      <router-link to="/admin">仪表盘</router-link>
      <router-link to="/admin/users">用户管理</router-link>
      <router-link to="/admin/plants">植物管理</router-link>
      <!-- 更多菜单... -->
    </nav>
  </div>
</template>
```

---

**最后更新时间**：2026年4月3日
