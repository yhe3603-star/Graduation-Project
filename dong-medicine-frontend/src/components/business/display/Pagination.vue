<template>
  <div
    v-if="total > 0"
    class="pagination-wrapper"
  >
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :background="background"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { computed } from "vue"

const props = defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 6 },
  total: { type: Number, default: 0 },
  pageSizes: { type: Array, default: () => [6, 9, 12, 24, 48] },
  layout: { type: String, default: "total, sizes, prev, pager, next, jumper" },
  background: { type: Boolean, default: true }
})

const emit = defineEmits(["update:page", "update:size", "change"])

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit("update:page", val)
})

const pageSize = computed({
  get: () => props.size,
  set: (val) => emit("update:size", val)
})

const handleSizeChange = (val) => emit("change", { page: currentPage.value, size: val })
const handleCurrentChange = (val) => emit("change", { page: val, size: pageSize.value })
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  padding: 16px 0;
}

@media (max-width: 768px) {
  .pagination-wrapper {
    margin-top: var(--space-lg);
    padding: var(--space-md) 0;
    overflow-x: auto;
    justify-content: flex-start;
  }
  
  .pagination-wrapper :deep(.el-pagination) {
    flex-wrap: wrap;
    gap: var(--space-sm);
  }
  
  .pagination-wrapper :deep(.el-pagination__total),
  .pagination-wrapper :deep(.el-pagination__sizes),
  .pagination-wrapper :deep(.el-pagination__jump) {
    display: none;
  }
  
  .pagination-wrapper :deep(.el-pager li) {
    min-width: 28px;
    height: 28px;
    font-size: var(--font-size-sm);
  }
  
  .pagination-wrapper :deep(.btn-prev),
  .pagination-wrapper :deep(.btn-next) {
    min-width: 28px;
    height: 28px;
  }
}

@media (max-width: 480px) {
  .pagination-wrapper :deep(.el-pager li) {
    min-width: 24px;
    height: 24px;
    font-size: var(--font-size-xs);
  }
  
  .pagination-wrapper :deep(.btn-prev),
  .pagination-wrapper :deep(.btn-next) {
    min-width: 24px;
    height: 24px;
  }
}
</style>
