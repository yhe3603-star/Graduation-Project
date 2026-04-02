<template>
  <div class="skeleton-list">
    <div
      v-for="i in count"
      :key="i"
      class="skeleton-item"
    >
      <el-skeleton animated>
        <template #template>
          <div class="item-layout">
            <el-skeleton-item
              variant="circle"
              class="item-icon"
            />
            <div class="item-content">
              <el-skeleton-item
                variant="h3"
                style="width: 40%"
              />
              <el-skeleton-item
                variant="text"
                style="width: 80%"
              />
              <div class="item-meta">
                <el-skeleton-item
                  variant="text"
                  style="width: 60px"
                />
                <el-skeleton-item
                  variant="text"
                  style="width: 50px"
                />
                <el-skeleton-item
                  variant="text"
                  style="width: 40px"
                />
              </div>
            </div>
            <div class="item-actions">
              <el-skeleton-item
                variant="button"
                style="width: 60px"
              />
              <el-skeleton-item
                variant="button"
                style="width: 60px"
              />
              <el-skeleton-item
                variant="button"
                style="width: 60px"
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
    default: 6
  }
});
</script>

<style scoped>
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl, 20px);
}

.skeleton-item {
  background: var(--text-inverse, #fff);
  border-radius: var(--radius-lg, 12px);
  padding: var(--space-xl, 20px);
  box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.08));
}

.item-layout {
  display: flex;
  align-items: center;
  gap: var(--space-xl, 20px);
}

.item-icon {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
}

.item-meta {
  display: flex;
  gap: var(--space-md, 12px);
  margin-top: var(--space-sm, 8px);
}

.item-actions {
  display: flex;
  gap: var(--space-sm, 8px);
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .item-layout {
    flex-wrap: wrap;
    gap: var(--space-md, 12px);
  }

  .item-icon {
    width: 48px;
    height: 48px;
  }

  .item-actions {
    width: 100%;
    justify-content: flex-end;
    margin-top: var(--space-sm, 8px);
  }
}
</style>
