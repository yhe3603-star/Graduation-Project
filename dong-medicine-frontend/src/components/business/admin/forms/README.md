# 管理表单弹窗（Admin Form Dialogs）

## 什么是管理表单弹窗？

类比：**信息录入表**——就像填写入职登记表，你需要在表格里填写各项信息，提交后信息就录入系统了。

管理表单弹窗用于**新增**和**编辑**数据。管理员点击"新增"或"编辑"按钮后，弹出一个带表单的对话框，填写完信息后提交保存。

```
点击"新增"或"编辑" → 弹出表单弹窗 → 填写信息 → 提交保存 → 刷新列表

┌────────────────────────────────────┐
│  新增药用植物                       │
│                                    │
│  名称：    [            ] *必填     │
│  侗语名：  [            ]          │
│  学名：    [            ]          │
│  功效：    [            ] *必填     │
│  主治：    [            ]          │
│  产地：    [            ]          │
│  采收时节： [            ]          │
│  图片：    [选择图片]              │
│                                    │
│         [取消]  [保存]             │
└────────────────────────────────────┘
```

---

## 表单弹窗列表

| 组件名 | 用途 | 关键字段 |
|--------|------|----------|
| `PlantFormDialog` | 新增/编辑药用植物 | 名称*、侗语名、学名、功效*、主治、产地、采收时节、图片 |
| `KnowledgeFormDialog` | 新增/编辑知识条目 | 标题*、分类*、内容*、来源、相关植物 |
| `InheritorFormDialog` | 新增/编辑传承人 | 姓名*、侗语名、从业经历*、擅长领域、代表案例、荣誉资质、照片 |
| `QaFormDialog` | 新增/编辑问答 | 问题*、分类*、答案*、相关植物 |
| `QuizFormDialog` | 新增/编辑测验题目 | 题目*、选项A-D*、正确答案*、解析、难度* |
| `ResourceFormDialog` | 新增/编辑学习资源 | 名称*、类型*、难度级别*、简介、文件上传* |

> 带 * 的为必填字段

---

## 统一的表单模式

所有表单弹窗遵循相同的设计模式：

```
1. props 接收 mode（新增/编辑）和 data（编辑时的原始数据）
2. v-model:visible 控制弹窗显示
3. 表单验证（必填、格式、长度等）
4. emit('submit', formData) 提交数据
```

### 通用代码模式

```vue
<!-- 以 PlantFormDialog 为例 -->
<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  mode: {
    type: String,
    default: 'add',        // 'add' 新增 | 'edit' 编辑
    validator: (v) => ['add', 'edit'].includes(v)
  },
  data: { type: Object, default: () => null }  // 编辑时传入的原数据
})

const emit = defineEmits(['update:visible', 'submit'])

// 表单引用（用于调用验证方法）
const formRef = ref(null)

// 表单数据
const formData = ref({
  name: '',
  dongName: '',
  scientificName: '',
  efficacy: '',
  indications: '',
  origin: '',
  harvestSeason: '',
  images: []
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入植物名称', trigger: 'blur' },
    { min: 2, max: 20, message: '名称长度2-20个字符', trigger: 'blur' }
  ],
  efficacy: [
    { required: true, message: '请输入功效', trigger: 'blur' }
  ]
}

// 监听弹窗打开，编辑模式时填充数据
watch(() => props.visible, (val) => {
  if (val && props.mode === 'edit' && props.data) {
    // 编辑模式：用原数据填充表单
    formData.value = { ...props.data }
  } else if (val && props.mode === 'add') {
    // 新增模式：清空表单
    formData.value = {
      name: '', dongName: '', scientificName: '',
      efficacy: '', indications: '', origin: '',
      harvestSeason: '', images: []
    }
  }
})

// 提交表单
const handleSubmit = async () => {
  // 先验证表单
  try {
    await formRef.value.validate()
  } catch {
    // 验证不通过，Element Plus 会自动标红提示
    return
  }

  // 验证通过，通知父组件提交数据
  emit('submit', {
    mode: props.mode,
    data: formData.value
  })
}

// 关闭弹窗
const handleClose = () => {
  formRef.value?.resetFields()  // 重置表单验证状态
  emit('update:visible', false)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="mode === 'add' ? '新增药用植物' : '编辑药用植物'"
    width="600px"
    @close="handleClose"
  >
    <!-- 表单区域 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入植物名称" />
      </el-form-item>

      <el-form-item label="侗语名" prop="dongName">
        <el-input v-model="formData.dongName" placeholder="请输入侗语名" />
      </el-form-item>

      <el-form-item label="学名" prop="scientificName">
        <el-input v-model="formData.scientificName" placeholder="请输入学名" />
      </el-form-item>

      <el-form-item label="功效" prop="efficacy">
        <el-input
          v-model="formData.efficacy"
          type="textarea"
          :rows="3"
          placeholder="请输入功效"
        />
      </el-form-item>

      <el-form-item label="主治" prop="indications">
        <el-input
          v-model="formData.indications"
          type="textarea"
          :rows="2"
          placeholder="请输入主治"
        />
      </el-form-item>

      <el-form-item label="产地" prop="origin">
        <el-input v-model="formData.origin" placeholder="请输入产地" />
      </el-form-item>

      <el-form-item label="采收时节" prop="harvestSeason">
        <el-input v-model="formData.harvestSeason" placeholder="请输入采收时节" />
      </el-form-item>

      <el-form-item label="图片" prop="images">
        <ImageUploader v-model:file-list="formData.images" :max-count="6" />
      </el-form-item>
    </el-form>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">
        {{ mode === 'add' ? '新增' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>
```

### 父组件中使用

```vue
<!-- AdminPlantPage.vue -->
<script setup>
import { ref } from 'vue'
import PlantFormDialog from '@/components/business/admin/forms/PlantFormDialog.vue'

const formVisible = ref(false)
const formMode = ref('add')       // 'add' 或 'edit'
const editData = ref(null)        // 编辑时的原始数据

// 点击新增按钮
const handleAdd = () => {
  formMode.value = 'add'
  editData.value = null
  formVisible.value = true
}

// 点击编辑按钮
const handleEdit = (row) => {
  formMode.value = 'edit'
  editData.value = row
  formVisible.value = true
}

// 表单提交
const handleFormSubmit = async ({ mode, data }) => {
  if (mode === 'add') {
    await api.addPlant(data)
    ElMessage.success('新增成功')
  } else {
    await api.updatePlant(data.id, data)
    ElMessage.success('修改成功')
  }
  formVisible.value = false
  loadTableData()  // 刷新列表
}
</script>

<template>
  <AdminDataTable @add="handleAdd" @edit="handleEdit" />

  <PlantFormDialog
    v-model:visible="formVisible"
    :mode="formMode"
    :data="editData"
    @submit="handleFormSubmit"
  />
</template>
```

---

## 常见错误

### 错误1：编辑时直接修改了原始数据

```vue
<script setup>
// ❌ 编辑时直接引用原对象，修改表单会同时修改列表中的数据
const handleEdit = (row) => {
  formData.value = row  // formData 和 row 指向同一个对象！
  // 用户在表单里修改名称，列表里的名称也跟着变了
  // 如果用户取消编辑，列表里的名称已经被改了，无法恢复！
}

// ✅ 用展开运算符复制一份数据，修改表单不影响原数据
const handleEdit = (row) => {
  formData.value = { ...row }  // 复制一份，互不影响
}
</script>
```

### 错误2：表单验证规则写错

```vue
<script setup>
// ❌ trigger 写错，验证不会触发
const rules = {
  name: [{ required: true, message: '请输入名称' }]  // 缺少 trigger！
}

// ✅ 必须指定 trigger，告诉 Element Plus 什么时候触发验证
const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
    // trigger: 'blur' 表示失去焦点时验证
    // trigger: 'change' 表示值改变时验证
  ]
}
</script>
```

### 错误3：关闭弹窗时没有重置表单

```vue
<script setup>
// ❌ 关闭弹窗后，表单还保留着上次填写的数据和验证错误提示
// 下次打开弹窗时，会看到上次的数据和红色错误提示
const handleClose = () => {
  emit('update:visible', false)
}

// ✅ 关闭时重置表单
const handleClose = () => {
  formRef.value?.resetFields()  // 重置数据和验证状态
  emit('update:visible', false)
}
</script>
```
