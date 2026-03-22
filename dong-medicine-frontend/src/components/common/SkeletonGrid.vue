<template>
  <div class="skeleton-grid">
    <div
      v-for="i in count"
      :key="i"
      class="skeleton-card"
    >
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item
            variant="image"
            class="skeleton-image"
          />
          <div class="skeleton-content">
            <el-skeleton-item
              variant="h3"
              style="width: 70%"
            />
            <el-skeleton-item
              variant="text"
              style="width: 90%"
            />
            <el-skeleton-item
              variant="text"
              style="width: 60%"
            />
            <div class="skeleton-footer">
              <el-skeleton-item
                variant="text"
                style="width: 30%"
              />
              <el-skeleton-item
                variant="text"
                style="width: 20%"
              />
            </div>
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
  gap: var(--space-lg, 20px);
}

.skeleton-card {
  background: var(--card-bg, #fff);
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
  box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.08));
}

.skeleton-image {
  width: 100%;
  height: 180px;
}

.skeleton-content {
  padding: var(--space-md, 16px);
}

.skeleton-footer {
  display: flex;
  justify-content: space-between;
  margin-top: var(--space-sm, 8px);
}

@media (max-width: 768px) {
  .skeleton-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: var(--space-md, 12px);
  }

  .skeleton-image {
    height: 120px;
  }
}
</style>
