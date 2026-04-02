<template>
  <div class="skeleton-grid">
    <div
      v-for="i in count"
      :key="i"
      class="skeleton-card"
    >
      <el-skeleton animated>
        <template #template>
          <div class="skeleton-header">
            <el-skeleton-item
              variant="circle"
              class="skeleton-icon"
            />
            <div class="skeleton-meta">
              <el-skeleton-item
                variant="text"
                style="width: 60px"
              />
              <el-skeleton-item
                variant="text"
                style="width: 40px"
              />
            </div>
          </div>
          <div class="skeleton-content">
            <el-skeleton-item
              variant="h3"
              style="width: 80%"
            />
            <el-skeleton-item
              variant="text"
              style="width: 95%"
            />
            <el-skeleton-item
              variant="text"
              style="width: 70%"
            />
          </div>
          <div class="skeleton-footer">
            <el-skeleton-item
              variant="text"
              style="width: 80px"
            />
            <el-skeleton-item
              variant="circle"
              style="width: 28px; height: 28px"
            />
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>

<script setup>
defineProps({
  count: {
    type: Number,
    default: 12
  }
});
</script>

<style scoped>
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-xl, 20px);
}

.skeleton-card {
  background: var(--text-inverse, #fff);
  border-radius: var(--radius-lg, 12px);
  padding: var(--space-xl, 20px);
  box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.08));
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md, 12px);
}

.skeleton-icon {
  width: 36px;
  height: 36px;
}

.skeleton-meta {
  display: flex;
  gap: var(--space-sm, 8px);
}

.skeleton-content {
  margin-bottom: var(--space-md, 12px);
}

.skeleton-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .skeleton-grid {
    grid-template-columns: 1fr;
    gap: var(--space-md, 12px);
  }

  .skeleton-card {
    padding: var(--space-lg, 16px);
  }
}
</style>
