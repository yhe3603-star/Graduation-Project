<template>
  <el-dialog
    :model-value="visible"
    title="问答"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form
      :model="form"
      label-width="80px"
    >
      <el-form-item label="问题">
        <el-input
          v-model="form.question"
          type="textarea"
          :rows="2"
          placeholder="请输入问题"
        />
      </el-form-item>
      <el-form-item label="分类">
        <el-select
          v-model="form.category"
          style="width: 100%"
          placeholder="请选择分类"
        >
          <el-option
            label="侗药常识"
            value="侗药常识"
          />
          <el-option
            label="侗医疗法"
            value="侗医疗法"
          />
          <el-option
            label="文化背景"
            value="文化背景"
          />
          <el-option
            label="其他"
            value="其他"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="答案">
        <el-input
          v-model="form.answer"
          type="textarea"
          :rows="4"
          placeholder="请输入答案"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">
        取消
      </el-button>
      <el-button
        type="primary"
        @click="handleSave"
      >
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: null }
});

const emit = defineEmits(['update:visible', 'save']);

const getDefaultForm = () => ({ id: null, question: "", category: "", answer: "" });

const form = ref(getDefaultForm());

watch(() => props.visible, (val) => {
  form.value = val && props.data ? { ...props.data } : getDefaultForm();
});

const handleSave = () => emit('save', { ...form.value });
</script>
