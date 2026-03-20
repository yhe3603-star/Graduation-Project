<template>
  <el-dialog
    :model-value="visible"
    title="测验题目"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form
      :model="form"
      label-width="80px"
    >
      <el-form-item label="题目">
        <el-input
          v-model="form.question"
          type="textarea"
          :rows="2"
          placeholder="请输入题目内容"
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
        </el-select>
      </el-form-item>
      <el-form-item label="难度">
        <el-select
          v-model="form.difficulty"
          style="width: 100%"
          placeholder="请选择难度"
        >
          <el-option
            label="简单"
            value="easy"
          />
          <el-option
            label="中等"
            value="medium"
          />
          <el-option
            label="困难"
            value="hard"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="选项A">
        <el-input
          v-model="form.optionA"
          placeholder="选项A的内容"
        />
      </el-form-item>
      <el-form-item label="选项B">
        <el-input
          v-model="form.optionB"
          placeholder="选项B的内容"
        />
      </el-form-item>
      <el-form-item label="选项C">
        <el-input
          v-model="form.optionC"
          placeholder="选项C的内容"
        />
      </el-form-item>
      <el-form-item label="选项D">
        <el-input
          v-model="form.optionD"
          placeholder="选项D的内容"
        />
      </el-form-item>
      <el-form-item label="正确答案">
        <el-select
          v-model="form.correctAnswer"
          style="width: 100%"
          placeholder="请选择正确答案"
        >
          <el-option
            label="A"
            value="A"
          />
          <el-option
            label="B"
            value="B"
          />
          <el-option
            label="C"
            value="C"
          />
          <el-option
            label="D"
            value="D"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="解析">
        <el-input
          v-model="form.explanation"
          type="textarea"
          :rows="2"
          placeholder="答案解析（可选）"
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

const getDefaultForm = () => ({ id: null, question: "", category: "", difficulty: "easy", optionA: "", optionB: "", optionC: "", optionD: "", correctAnswer: "A", explanation: "" });

const form = ref(getDefaultForm());

watch(() => props.visible, (val) => {
  if (val) {
    if (props.data) {
      const options = props.data.options || props.data.optionList || [];
      form.value = {
        id: props.data.id,
        question: props.data.question,
        category: props.data.category || "",
        difficulty: props.data.difficulty || "easy",
        optionA: options[0] || "",
        optionB: options[1] || "",
        optionC: options[2] || "",
        optionD: options[3] || "",
        correctAnswer: props.data.correctAnswer || props.data.answer || "A",
        explanation: props.data.explanation || ""
      };
    } else {
      form.value = getDefaultForm();
    }
  }
});

const handleSave = () => {
  emit('save', {
    id: form.value.id,
    question: form.value.question,
    category: form.value.category,
    difficulty: form.value.difficulty,
    options: [form.value.optionA, form.value.optionB, form.value.optionC, form.value.optionD],
    correctAnswer: form.value.correctAnswer,
    explanation: form.value.explanation
  });
};
</script>
