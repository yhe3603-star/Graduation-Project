<template>
  <div class="document-list-container">
    <div
      v-if="loading"
      class="loading-state"
    >
      <el-icon class="loading-icon">
        <Loading />
      </el-icon>
      <span>加载文档中...</span>
    </div>
    
    <div
      v-else-if="documents.length > 0"
      class="document-list"
    >
      <div
        v-for="doc in documents"
        :key="doc.id"
        class="document-item"
        @click="handleDocumentClick(doc)"
      >
        <div class="doc-icon">
          <el-icon :size="32">
            <component :is="getFileIcon(doc.fileType)" />
          </el-icon>
        </div>
        <div class="doc-info">
          <div class="doc-name">
            {{ doc.originalFileName || doc.fileName || doc.name }}
          </div>
          <div class="doc-meta">
            <span class="doc-type">{{ doc.type?.toUpperCase() || '文档' }}</span>
            <span class="doc-size">{{ formatFileSize(doc.size) }}</span>
            <span class="doc-date">{{ formatDate(doc.uploadTime) }}</span>
          </div>
          <div
            v-if="doc.description"
            class="doc-desc"
          >
            {{ doc.description }}
          </div>
        </div>
        <div
          v-if="showActions"
          class="doc-actions"
        >
          <el-button
            type="default"
            size="small"
            @click.stop="handleDocumentClick(doc)"
          >
            <el-icon><View /></el-icon>预览
          </el-button>
        </div>
      </div>
    </div>
    
    <div
      v-else
      class="empty-state"
    >
      <el-icon :size="48">
        <Document />
      </el-icon>
      <p>暂无相关文档</p>
    </div>
  </div>
</template>

<script setup>
import { Document, Loading } from '@element-plus/icons-vue'
import { formatFileSize } from '@/utils/adminUtils'
import { formatTime } from '@/utils/index'

const props = defineProps({
  documents: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showActions: { type: Boolean, default: true }
})

const emit = defineEmits(['document-click'])

const getFileIcon = (fileType) => Document

const formatDate = (dateStr) => formatTime(dateStr, { format: 'date' })

const handleDocumentClick = (doc) => emit('document-click', doc)
</script>

<style scoped>
.document-list-container { min-height: 200px; }
.loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 200px; color: #999; gap: 8px; }
.loading-icon { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.document-list { display: flex; flex-direction: column; gap: 12px; }
.document-item { display: flex; align-items: center; gap: 16px; padding: 16px; background: #f8f9fa; border-radius: 12px; cursor: pointer; transition: all 0.3s; border: 1px solid transparent; }
.document-item:hover { background: #eef4f7; border-color: var(--dong-blue, #1A5276); transform: translateX(4px); }
.doc-icon { width: 56px; height: 56px; background: linear-gradient(135deg, var(--dong-blue, #1A5276), var(--dong-indigo-dark)); border-radius: 12px; display: flex; align-items: center; justify-content: center; color: var(--text-inverse); flex-shrink: 0; }
.doc-info { flex: 1; min-width: 0; }
.doc-name { font-size: 15px; font-weight: 500; color: var(--text-primary); margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.doc-meta { display: flex; gap: 12px; font-size: 12px; color: var(--text-muted); }
.doc-meta span { display: flex; align-items: center; gap: 4px; }
.doc-type { background: #e8f4f8; color: var(--dong-blue, #1A5276); padding: 2px 8px; border-radius: 4px; font-weight: 500; }
.doc-actions { flex-shrink: 0; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 200px; color: #999; }
.empty-state p { margin: 12px 0 0; font-size: 14px; }

@media (max-width: 768px) {
  .document-item {
    flex-direction: column;
    text-align: center;
    padding: 12px;
    gap: 12px;
  }
  
  .doc-icon {
    width: 48px;
    height: 48px;
    margin: 0 auto;
  }
  
  .doc-icon .el-icon {
    font-size: 24px;
  }
  
  .doc-info {
    width: 100%;
  }
  
  .doc-name {
    font-size: 14px;
    margin-bottom: 8px;
  }
  
  .doc-meta {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    font-size: 11px;
  }
  
  .doc-desc {
    font-size: 12px;
    margin-top: 8px;
  }
  
  .doc-actions {
    width: 100%;
  }
  
  .doc-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .document-item {
    padding: 10px;
  }
  
  .doc-icon {
    width: 40px;
    height: 40px;
  }
  
  .doc-name {
    font-size: 13px;
  }
  
  .doc-meta {
    font-size: 10px;
  }
}
</style>
