# 管理组件（Admin Components）

## 什么是管理组件？

类比：**后台管理工具箱**——前台是给普通用户看的展示厅，后台是管理员专用的工具间。工具间里有仪表盘（看数据总览）、数据表格（管理所有数据）、侧边栏（切换管理功能）。

管理组件只在后台管理页面使用，普通用户看不到。它们负责数据的增删改查（CRUD）和系统管理。

```
前台（用户看到的）              后台（管理员看到的）
┌──────────────────┐          ┌──────────────────────────┐
│  🌿 药用植物展示   │          │  📊 数据仪表盘            │
│  📖 知识问答       │          │  📋 数据管理表格           │
│  👨‍⚕️ 传承人介绍    │          │  ✏️ 信息录入表单           │
│  📚 学习资源       │          │  🔍 详情查看弹窗           │
└──────────────────┘          └──────────────────────────┘
   普通用户浏览                    管理员操作
```

---

## 组件列表

### AdminDashboard —— 管理仪表盘

**用途：** 后台首页，展示平台数据总览

```
┌────────────────────────────────────────────────┐
│  📊 平台数据总览                                │
│                                                │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐      │
│  │ 128  │  │  45  │  │  23  │  │ 892  │      │
│  │药用植物│  │传承人 │  │知识条目│  │用户数 │      │
│  └──────┘  └──────┘  └──────┘  └──────┘      │
│                                                │
│  ┌─────────────────┐  ┌─────────────────┐     │
│  │ 访问量趋势图表   │  │ 热门内容排行     │     │
│  │ 📈              │  │ 1. 钩藤 324次    │     │
│  │                 │  │ 2. 透骨草 289次   │     │
│  └─────────────────┘  └─────────────────┘     │
└────────────────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<AdminDashboard :stats="platformStats" />
```

---

### AdminDataTable —— 管理数据表格

**用途：** 以表格形式展示和管理数据，支持搜索、排序、分页、批量操作

```
┌──────────────────────────────────────────────────────┐
│  药用植物管理    🔍 [搜索...]    [+ 新增]  [🗑 批量删除] │
│                                                      │
│  ☐ │ ID │ 名称  │ 侗语名      │ 功效     │ 操作      │
│  ──┼────┼───────┼────────────┼─────────┼────────── │
│  ☐ │ 1  │ 钩藤  │ gons jinl  │ 息风止痉 │ 查看 编辑 删│
│  ☐ │ 2  │ 透骨草│ touc gus   │ 祛风除湿 │ 查看 编辑 删│
│  ☐ │ 3  │ 九节茶│ jius jiuc  │ 清热解毒 │ 查看 编辑 删│
│                                                      │
│  ← 上一页  1  2  3  4  5  下一页 →   共 128 条       │
└──────────────────────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<AdminDataTable
  :columns="tableColumns"
  :data="tableData"
  :loading="isLoading"
  :total="total"
  :page="currentPage"
  :page-size="pageSize"
  @page-change="handlePageChange"
  @add="handleAdd"
  @edit="handleEdit"
  @delete="handleDelete"
  @batch-delete="handleBatchDelete"
/>
```

---

### AdminSidebar —— 管理侧边栏

**用途：** 后台管理页面的导航菜单，切换不同的管理功能

```
┌──────────┬─────────────────────────────┐
│ 🛠 管理   │                             │
│          │                             │
│ 📊 仪表盘 │      管理内容区域             │
│ 🌿 药用植物│                             │
│ 📖 知识管理│                             │
│ 👨‍⚕️ 传承人 │                             │
│ 📚 学习资源│                             │
│ ❓ 问答管理│                             │
│ 🎮 测验管理│                             │
│ 👥 用户管理│                             │
│ 💬 评论管理│                             │
│ 📝 反馈管理│                             │
│ 📋 系统日志│                             │
└──────────┴─────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<AdminSidebar
  :menu-items="adminMenuItems"
  :active-key="currentMenu"
  @menu-click="handleMenuClick"
/>
```

---

## 子目录结构

```
admin/
├── AdminDashboard.vue      仪表盘
├── AdminDataTable.vue      数据表格
├── AdminSidebar.vue        侧边栏
├── forms/                  表单弹窗（新增/编辑数据）
│   ├── PlantFormDialog.vue
│   ├── KnowledgeFormDialog.vue
│   ├── InheritorFormDialog.vue
│   └── ...
└── dialogs/                详情弹窗（查看数据详情）
    ├── UserDetailDialog.vue
    ├── PlantDetailDialog.vue
    └── ...
```

| 子目录 | 用途 | 类比 |
|--------|------|------|
| `forms/` | 新增和编辑数据的表单弹窗 | 信息录入表，填写后提交 |
| `dialogs/` | 查看数据详情的只读弹窗 | 档案查看窗口，只读展示 |

---

## forms/ 和 dialogs/ 的区别

```
forms/（表单弹窗）              dialogs/（详情弹窗）
┌──────────────────┐          ┌──────────────────┐
│ 编辑药用植物      │          │ 药用植物详情       │
│                  │          │                  │
│ 名称：[钩藤    ] │ ← 可编辑  │ 名称：钩藤        │ ← 只读
│ 侗语名：[gons  ] │          │ 侗语名：gons jinl │
│ 功效：[息风止痉 ] │          │ 功效：息风止痉     │
│                  │          │                  │
│ [取消] [保存]     │          │ [关闭]            │
└──────────────────┘          └──────────────────┘
  可以修改数据                    只能看不能改
```

---

## 常见错误

### 错误1：管理页面没有权限控制

```vue
<script setup>
// ❌ 任何人都能访问管理页面，普通用户也能删除数据！

// ✅ 在路由守卫中检查用户角色
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
if (userStore.role !== 'admin') {
  // 不是管理员，跳转到首页
  router.push('/')
  ElMessage.error('无权访问管理页面')
}
</script>
```

### 错误2：删除操作没有二次确认

```vue
<script setup>
// ❌ 点一下就删除了，手滑误点无法挽回
const handleDelete = (id) => {
  api.deletePlant(id)
}

// ✅ 弹出确认框，用户确认后才删除
import { ElMessageBox } from 'element-plus'

const handleDelete = async (id) => {
  try {
    // 弹出确认框
    await ElMessageBox.confirm(
      '确定要删除这条数据吗？删除后不可恢复！',
      '警告',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    // 用户点击了"确定删除"
    await api.deletePlant(id)
    ElMessage.success('删除成功')
  } catch {
    // 用户点击了"取消"，什么都不做
  }
}
</script>
```

### 错误3：表格数据修改后没有刷新

```vue
<script setup>
// ❌ 编辑或删除数据后，表格还显示旧数据
const handleEdit = async (data) => {
  await api.updatePlant(data)
  // 缺少刷新表格的代码！
}

// ✅ 操作完成后重新加载表格数据
const handleEdit = async (data) => {
  await api.updatePlant(data)
  ElMessage.success('修改成功')
  loadTableData()  // 重新加载表格
}
</script>
```

---

## 代码审查与改进建议

- [组件设计] AdminDataTable.vue搜索逻辑硬编码字段名(title/nameCn/name/question/content)，无法根据数据类型自定义
- [组件设计] AdminDataTable.vue状态映射(TAG_TYPES)硬编码在通用组件中，违反组件通用性原则
- [性能] AdminDashboard.vue全量导入echarts，ECharts实例未在组件卸载时dispose
