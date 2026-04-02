# 管理后台表单目录 (admin/forms)

本目录存放管理后台的表单对话框组件，用于添加和编辑数据。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| InheritorFormDialog.vue | 传承人表单对话框 |
| KnowledgeFormDialog.vue | 知识表单对话框 |
| PlantFormDialog.vue | 植物表单对话框 |
| QaFormDialog.vue | 问答表单对话框 |
| QuizFormDialog.vue | 测验题目表单对话框 |
| ResourceFormDialog.vue | 资源表单对话框 |

---

## 组件结构

所有表单对话框遵循统一的结构：

```vue
<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑' : '新增'" width="600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      
      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category">
          <el-option label="分类1" value="1" />
          <el-option label="分类2" value="2" />
        </el-select>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: () => null }  // 编辑时传入
})

const emit = defineEmits(['update:visible', 'submit'])

const formRef = ref(null)
const form = ref({})
const isEdit = computed(() => !!props.data)

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const handleSubmit = async () => {
  await formRef.value.validate()
  emit('submit', form.value)
  handleClose()
}

const handleClose = () => {
  emit('update:visible', false)
}
</script>
```

---

## 使用示例

```vue
<template>
  <!-- 工具栏按钮 -->
  <el-button @click="handleAdd">新增</el-button>
  <el-button @click="handleEdit(row)">编辑</el-button>
  
  <!-- 表单对话框 -->
  <PlantFormDialog 
    v-model:visible="formVisible" 
    :data="currentData"
    @submit="handleSubmit"
  />
</template>

<script setup>
import PlantFormDialog from './PlantFormDialog.vue'

const formVisible = ref(false)
const currentData = ref(null)

// 新增
const handleAdd = () => {
  currentData.value = null
  formVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  currentData.value = row
  formVisible.value = true
}

// 提交
const handleSubmit = async (formData) => {
  if (currentData.value) {
    await updatePlant(formData)
  } else {
    await addPlant(formData)
  }
}
</script>
```

---

## 表单验证规则

```javascript
const rules = {
  // 必填
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
  ],
  
  // 长度限制
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  
  // 正则验证
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  
  // 数字范围
  age: [
    { type: 'number', min: 0, max: 150, message: '年龄必须在0-150之间', trigger: 'blur' }
  ]
}
```

---

**最后更新时间**：2026年4月3日
