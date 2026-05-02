<template>
  <div class="result-card" @click="$emit('click', item)">
    <div class="result-icon">
      <el-icon><component :is="icon" /></el-icon>
    </div>
    <div class="result-info">
      <h4 v-html="highlightedTitle" />
      <p v-html="highlightedDesc" />
    </div>
    <el-tag size="small" :type="tagType">{{ tagName }}</el-tag>
  </div>
</template>

<script setup>
defineProps({
  item: { type: Object, required: true },
  icon: { type: Object, default: null },
  highlightedTitle: { type: String, default: '' },
  highlightedDesc: { type: String, default: '' },
  tagType: { type: String, default: 'info' },
  tagName: { type: String, default: '' }
})

defineEmits(['click'])
</script>
