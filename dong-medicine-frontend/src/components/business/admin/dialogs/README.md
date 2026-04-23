# 管理详情弹窗（Admin Detail Dialogs）

## 什么是管理详情弹窗？

类比：**档案查看窗口**——就像在档案馆调阅一份档案，你只能看不能改。如果需要修改，要回到编辑表单去操作。

管理详情弹窗用于在后台管理页面**只读查看**数据的完整信息。和前台用户看到的详情弹窗不同，管理详情弹窗展示更多管理相关的字段（如创建时间、更新时间、状态等）。

```
前台详情弹窗（用户看到的）        管理详情弹窗（管理员看到的）
┌──────────────────┐          ┌──────────────────────┐
│ 🌿 钩藤          │          │ 🌿 钩藤（ID: 1）      │
│ 侗语名：gons jinl│          │ 侗语名：gons jinl     │
│ 功效：息风止痉    │          │ 功效：息风止痉         │
│ 主治：头痛眩晕    │          │ 主治：头痛眩晕         │
│                  │          │ 状态：已发布 ✅         │ ← 管理字段
│                  │          │ 创建时间：2025-03-15   │ ← 管理字段
│      [关闭]      │          │ 更新时间：2025-04-20   │ ← 管理字段
│                  │          │ 创建人：管理员A         │ ← 管理字段
└──────────────────┘          │                      │
                              │ [关闭] [编辑] [删除]   │ ← 管理操作
                              └──────────────────────┘
```

---

## 详情弹窗列表

| 组件名 | 用途 | 展示的关键信息 |
|--------|------|----------------|
| `UserDetailDialog` | 查看用户详情 | 用户名、邮箱、角色、注册时间、最后登录、状态 |
| `PlantDetailDialog` | 查看药用植物详情 | 名称、侗语名、学名、功效、主治、产地、采收时节、民间故事、图片、状态、创建时间 |
| `KnowledgeDetailDialog` | 查看知识条目详情 | 标题、分类、内容全文、来源、相关植物、浏览量、状态 |
| `InheritorDetailDialog` | 查看传承人详情 | 姓名、侗语名、从业经历、擅长领域、代表案例、荣誉资质、照片 |
| `QaDetailDialog` | 查看问答详情 | 问题、分类、答案、相关植物、浏览量、状态 |
| `QuizDetailDialog` | 查看测验题目详情 | 题目、选项A-D、正确答案、解析、难度、答题统计 |
| `ResourceDetailDialog` | 查看学习资源详情 | 名称、类型、难度级别、简介、文件信息、下载量、状态 |
| `CommentDetailDialog` | 查看评论详情 | 评论内容、评论者、关联内容、时间、点赞数、状态 |
| `FeedbackDetailDialog` | 查看反馈详情 | 反馈内容、反馈者、类型、处理状态、处理人、处理时间 |
| `LogDetailDialog` | 查看系统日志详情 | 操作类型、操作人、操作内容、IP地址、操作时间 |

---

## 统一的详情弹窗模式

所有管理详情弹窗遵循相同的设计模式，和前台详情弹窗类似，但增加了管理字段和操作按钮：

```
1. props 接收 visible 和 data
2. 只读展示所有字段
3. 底部提供管理操作按钮（编辑、删除等）
```

### 通用代码模式

```vue
<!-- 以 UserDetailDialog 为例 -->
<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: () => null }
})

const emit = defineEmits(['update:visible', 'edit', 'delete'])

const handleClose = () => {
  emit('update:visible', false)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="用户详情"
    width="600px"
    @close="handleClose"
  >
    <div v-if="!data" class="empty-tip">暂无数据</div>

    <div v-else class="detail-content">
      <!-- 使用描述列表展示信息，比普通文本更整齐 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">
          {{ data.username }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ data.email }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="data.role === 'admin' ? 'danger' : 'info'">
            {{ data.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="data.status === 1 ? 'success' : 'warning'">
            {{ data.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ data.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">
          {{ data.lastLoginTime }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="emit('edit', data)">编辑</el-button>
      <el-button type="danger" @click="emit('delete', data.id)">删除</el-button>
    </template>
  </el-dialog>
</template>
```

### 父组件中使用

```vue
<script setup>
import { ref } from 'vue'
import UserDetailDialog from '@/components/business/admin/dialogs/UserDetailDialog.vue'
import UserFormDialog from '@/components/business/admin/forms/UserFormDialog.vue'

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref(null)

// 表单弹窗
const formVisible = ref(false)
const formMode = ref('add')
const editData = ref(null)

// 查看详情
const handleView = (row) => {
  detailData.value = row
  detailVisible.value = true
}

// 从详情弹窗跳转到编辑表单
const handleEditFromDetail = (data) => {
  detailVisible.value = false  // 关闭详情弹窗
  formMode.value = 'edit'
  editData.value = data
  formVisible.value = true     // 打开编辑表单
}

// 删除
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除？', '警告', { type: 'warning' })
  await api.deleteUser(id)
  ElMessage.success('删除成功')
  detailVisible.value = false
  loadTableData()
}
</script>

<template>
  <AdminDataTable @view="handleView" />

  <!-- 详情弹窗 -->
  <UserDetailDialog
    v-model:visible="detailVisible"
    :data="detailData"
    @edit="handleEditFromDetail"
    @delete="handleDelete"
  />

  <!-- 表单弹窗 -->
  <UserFormDialog
    v-model:visible="formVisible"
    :mode="formMode"
    :data="editData"
    @submit="handleFormSubmit"
  />
</template>
```

---

## 详情弹窗 vs 表单弹窗

```
详情弹窗（dialogs/）              表单弹窗（forms/）
┌──────────────────┐          ┌──────────────────┐
│ 只读查看          │          │ 可编辑            │
│                  │          │                  │
│ 名称：钩藤        │          │ 名称：[钩藤    ]  │
│ 功效：息风止痉    │          │ 功效：[息风止痉 ] │
│ 状态：已发布 ✅   │          │ 状态：[已发布 ▼]  │
│                  │          │                  │
│ [关闭] [编辑]    │          │ [取消] [保存]     │
└──────────────────┘          └──────────────────┘
  用 el-descriptions 展示       用 el-form 编辑
  不能修改                       可以修改
```

**典型工作流：** 列表 → 点击"查看" → 详情弹窗 → 点击"编辑" → 表单弹窗 → 保存

---

## 常见错误

### 错误1：在详情弹窗里直接编辑数据

```vue
<template>
  <!-- ❌ 详情弹窗应该是只读的，不应该有输入框 -->
  <el-descriptions-item label="名称">
    <el-input v-model="data.name" />  <!-- 不应该在详情弹窗里放输入框！ -->
  </el-descriptions-item>

  <!-- ✅ 详情弹窗只展示文本，编辑功能交给表单弹窗 -->
  <el-descriptions-item label="名称">
    {{ data.name }}  <!-- 只读展示 -->
  </el-descriptions-item>
</template>
```

### 错误2：删除后没有关闭弹窗

```vue
<script setup>
// ❌ 删除成功后弹窗还开着，显示已删除的数据
const handleDelete = async (id) => {
  await api.deleteUser(id)
  ElMessage.success('删除成功')
  // 缺少关闭弹窗的代码！
}

// ✅ 删除后关闭弹窗并刷新列表
const handleDelete = async (id) => {
  await api.deleteUser(id)
  ElMessage.success('删除成功')
  detailVisible.value = false  // 关闭弹窗
  loadTableData()              // 刷新列表
}
</script>
```

### 错误3：状态字段没有用 Tag 组件高亮

```vue
<template>
  <!-- ❌ 状态用纯文本显示，不够醒目 -->
  <el-descriptions-item label="状态">
    {{ data.status === 1 ? '正常' : '禁用' }}
  </el-descriptions-item>

  <!-- ✅ 用 Tag 组件，不同状态用不同颜色，一目了然 -->
  <el-descriptions-item label="状态">
    <el-tag :type="data.status === 1 ? 'success' : 'danger'">
      {{ data.status === 1 ? '正常' : '禁用' }}
    </el-tag>
  </el-descriptions-item>
</template>
```
