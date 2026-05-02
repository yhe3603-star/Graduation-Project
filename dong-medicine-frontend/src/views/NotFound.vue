<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">
404
</div>
      <h1 class="error-title">
页面未找到
</h1>
      <p class="error-description">
抱歉，您访问的页面不存在或已被移除
</p>
    </div>
    <div class="decoration">
      <div class="circle circle-1" />
      <div class="circle circle-2" />
      <div class="circle circle-3" />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  position: relative;
  overflow: hidden;
}

.not-found-content {
  text-align: center;
  z-index: 1;
  padding: 40px;
}

.error-code {
  font-size: 150px;
  font-weight: 700;
  color: var(--dong-indigo, #1A5276);
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 32px;
  color: #333;
  margin-bottom: 16px;
  font-weight: 600;
}

.error-description {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
}

.suggestions {
  background: rgba(255, 255, 255, 0.8);
  padding: 24px 32px;
  border-radius: 12px;
  max-width: 400px;
  margin: 0 auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.suggestions h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
}

.suggestions ul {
  text-align: left;
  padding-left: 20px;
  margin: 0;
}

.suggestions li {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.6;
}

.decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: var(--dong-indigo, #1A5276);
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: var(--dong-jade, #28B463);
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: var(--dong-gold, #c9a227);
  top: 50%;
  left: 10%;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 100px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .suggestions {
    margin: 0 16px;
  }
}
</style>
