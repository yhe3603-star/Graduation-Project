<template>
  <div class="search-filter">
    <el-input 
      v-model="keyword" 
      :placeholder="placeholder" 
      clearable 
      class="search-input" 
      size="large"
      @input="handleSearch"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
    
    <div
      v-if="filters?.length"
      class="filter-row"
    >
      <div
        v-for="filter in filters"
        :key="filter.key"
        class="filter-group"
      >
        <span class="filter-label">{{ filter.label }}：</span>
        <el-tag 
          v-for="t in filter.options" 
          :key="t.value" 
          :effect="activeFilters[filter.key] === t.value ? 'dark' : 'plain'"
          :type="filter.type || ''"
          class="filter-tag"
          @click="selectFilter(filter.key, t.value)"
        >
          {{ t.label }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue"
import { Search } from "@element-plus/icons-vue"

const props = defineProps({
  placeholder: { type: String, default: "搜索..." },
  filters: { type: Array, default: () => [] },
  modelValue: String
})

const emit = defineEmits(["update:modelValue", "search", "filter"])

const keyword = ref(props.modelValue || "")
const activeFilters = ref({})

watch(() => props.modelValue, (v) => keyword.value = v || "")

let timer = null
const handleSearch = () => {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    emit("update:modelValue", keyword.value)
    emit("search", keyword.value)
  }, 400)
}

const selectFilter = (key, value) => {
  activeFilters.value[key] = activeFilters.value[key] === value ? "" : value
  emit("filter", { ...activeFilters.value })
}
</script>

<style scoped>
.search-filter { margin-bottom: 24px; }
.search-input { margin-bottom: 12px; }
.search-input :deep(.el-input__wrapper) { border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); }
.filter-row { display: flex; flex-wrap: wrap; gap: 16px; }
.filter-group { display: flex; align-items: center; gap: 8px; }
.filter-label { font-size: 14px; color: #666; flex-shrink: 0; }
.filter-tag { cursor: pointer; transition: all 0.2s; }
.filter-tag:hover { transform: translateY(-1px); }

@media (max-width: 768px) {
  .search-filter {
    margin-bottom: var(--space-lg);
  }
  
  .filter-row {
    gap: var(--space-md);
  }
  
  .filter-group {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .filter-label {
    width: 100%;
    margin-bottom: var(--space-xs);
    font-size: var(--font-size-sm);
  }
  
  .filter-tag {
    font-size: var(--font-size-xs);
    padding: 4px 10px;
  }
}

@media (max-width: 480px) {
  .filter-tag {
    padding: 3px 8px;
  }
}
</style>
