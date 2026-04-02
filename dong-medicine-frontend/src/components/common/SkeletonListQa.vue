<template>
  <div class="skeleton-qa-list">
    <div
      v-for="i in count"
      :key="i"
      class="skeleton-qa-item"
    >
      <el-skeleton animated>
        <template #template>
          <div class="qa-question">
            <el-skeleton-item
              variant="circle"
              class="q-icon"
            />
            <el-skeleton-item
              variant="h3"
              style="width: 80%"
            />
          </div>
          <div class="qa-answer">
            <el-skeleton-item
              variant="text"
              style="width: 95%"
            />
            <el-skeleton-item
              variant="text"
              style="width: 60%"
            />
          </div>
          <div class="qa-footer">
            <el-skeleton-item
              variant="text"
              style="width: 80px"
            />
            <div class="qa-stats">
              <el-skeleton-item
                variant="text"
                style="width: 50px"
              />
              <el-skeleton-item
                variant="text"
                style="width: 50px"
              />
            </div>
            <div class="qa-actions">
              <el-skeleton-item
                variant="button"
                style="width: 70px"
              />
              <el-skeleton-item
                variant="button"
                style="width: 80px"
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
.skeleton-qa-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl, 20px);
}

.skeleton-qa-item {
  background: var(--text-inverse, #fff);
  border-radius: var(--radius-lg, 12px);
  padding: var(--space-xl, 20px);
  box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.08));
}

.qa-question {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md, 12px);
  margin-bottom: var(--space-md, 12px);
}

.q-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-top: 4px;
}

.qa-answer {
  padding-top: var(--space-md, 12px);
  border-top: 1px dashed var(--border-light, #e0e0e0);
  margin-bottom: var(--space-md, 12px);
}

.qa-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.qa-stats {
  display: flex;
  gap: var(--space-md, 12px);
  margin-left: var(--space-md, 12px);
}

.qa-actions {
  display: flex;
  gap: var(--space-sm, 8px);
}

@media (max-width: 768px) {
  .qa-footer {
    flex-wrap: wrap;
    gap: var(--space-sm, 8px);
  }

  .qa-stats {
    order: 3;
    width: 100%;
    margin-left: 0;
    margin-top: var(--space-sm, 8px);
  }

  .qa-actions {
    flex: 1;
    justify-content: flex-end;
  }
}
</style>
