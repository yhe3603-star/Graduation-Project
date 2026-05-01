<template>
  <div class="compare-page module-page">
    <div class="module-header">
      <h1>药材对比</h1>
      <p class="subtitle">多维度对比药材特性，辅助精准识别与用药</p>
    </div>

    <div class="compare-container">
      <div v-if="pageLoading" class="loading-wrap">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="comparePlants.length === 0" class="empty-compare">
        <el-empty description="请选择要对比的药材" :image-size="160">
          <template #default>
            <el-button type="primary" size="large" @click="searchVisible = true">
              <el-icon><Search /></el-icon>添加药材对比
            </el-button>
          </template>
        </el-empty>
      </div>

      <div v-else class="comparison-wrapper">
        <div class="compare-actions-top">
          <el-button type="primary" :disabled="comparePlants.length >= MAX_COMPARE" @click="searchVisible = true">
            <el-icon><Plus /></el-icon>添加对比
          </el-button>
          <el-button text type="danger" @click="clearCompare">
            <el-icon><Delete /></el-icon>清空全部
          </el-button>
        </div>

        <div class="compare-table-wrap">
          <table class="compare-table">
            <thead>
              <tr>
                <th class="row-label-col">属性</th>
                <th
                  v-for="(plant, idx) in comparePlants"
                  :key="plant.id"
                  class="plant-col"
                >
                  <div class="col-header">
                    <div class="col-thumb">
                      <el-image
                        v-if="getImageUrl(plant)"
                        :src="getImageUrl(plant)"
                        fit="cover"
                        class="col-img"
                      >
                        <template #error>
                          <div class="col-img-placeholder">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                      <div v-else class="col-img-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </div>
                    <span class="col-name">{{ plant.nameCn || '未知' }}</span>
                    <el-button
                      size="small"
                      type="danger"
                      text
                      class="remove-btn"
                      @click="removeFromCompare(plant.id)"
                    >
                      <el-icon><Close /></el-icon>移除
                    </el-button>
                  </div>
                </th>
                <th
                  v-if="comparePlants.length < MAX_COMPARE"
                  class="add-col"
                >
                  <div class="add-card" @click="searchVisible = true">
                    <el-icon :size="32"><Plus /></el-icon>
                    <span>添加药材</span>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in compareRows"
                :key="row.key"
                :class="{ 'row-stripe': row.key === 'images' }"
              >
                <td class="row-label">
                  <span class="label-text">{{ row.label }}</span>
                </td>
                <td
                  v-for="plant in comparePlants"
                  :key="plant.id + '_' + row.key"
                  class="row-value"
                >
                  <template v-if="row.key === 'images'">
                    <div v-if="getAllImages(plant).length > 0" class="images-cell">
                      <el-image
                        v-for="(img, i) in getAllImages(plant).slice(0, 3)"
                        :key="i"
                        :src="img"
                        fit="cover"
                        class="thumb-img"
                        :preview-src-list="getAllImages(plant)"
                        :initial-index="i"
                      />
                    </div>
                    <span v-else class="no-data">暂无图片</span>
                  </template>
                  <template v-else-if="row.key === 'category'">
                    <el-tag size="small" effect="light">{{ plant.category || '--' }}</el-tag>
                  </template>
                  <template v-else>
                    <span :class="{ 'no-data': !plant[row.key] }">
                      {{ plant[row.key] || '--' }}
                    </span>
                  </template>
                </td>
                <td v-if="comparePlants.length < MAX_COMPARE" class="add-col"></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="searchVisible"
      title="选择药材进行对比"
      width="min(700px, 90vw)"
      destroy-on-close
    >
      <div class="search-dialog-content">
        <div class="dialog-search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索药材名称..."
            clearable
            @keyup.enter="doPlantSearch"
            @clear="onSearchClear"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="doPlantSearch">
            搜索
          </el-button>
        </div>

        <div v-if="searchLoading" class="search-loading">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else-if="searchResults.length > 0" class="search-results">
          <div
            v-for="plant in searchResults"
            :key="plant.id"
            class="search-result-item"
            :class="{ 'already-added': isInCompare(plant.id) }"
          >
            <div class="result-thumb">
              <el-image
                v-if="getImageUrl(plant)"
                :src="getImageUrl(plant)"
                fit="cover"
                class="result-img"
              >
                <template #error>
                  <el-icon><Picture /></el-icon>
                </template>
              </el-image>
              <el-icon v-else :size="28"><Picture /></el-icon>
            </div>
            <div class="result-info">
              <span class="result-name">{{ plant.nameCn }}</span>
              <span class="result-dong">{{ plant.nameDong || '--' }}</span>
              <span class="result-cat">{{ plant.category || '--' }}</span>
            </div>
            <el-button
              :type="isInCompare(plant.id) ? 'info' : 'primary'"
              :disabled="isInCompare(plant.id) || comparePlants.length >= MAX_COMPARE"
              size="small"
              @click="handleAddPlant(plant)"
            >
              {{ isInCompare(plant.id) ? '已添加' : '加入对比' }}
            </el-button>
          </div>
        </div>

        <el-empty
          v-else-if="searched && !searchLoading"
          description="未找到匹配的药材"
          :image-size="100"
        />
        <el-empty
          v-else
          description="输入关键词搜索药材"
          :image-size="100"
        />
      </div>
    </el-dialog>

    <Teleport to="body">
      <transition name="float-bar-fade">
        <div v-if="compareList.length >= 2" class="floating-compare-bar">
          <div class="bar-left">
            <span class="bar-label">已选择 <strong>{{ compareList.length }}</strong> 个药材</span>
            <div class="bar-thumbs">
              <span
                v-for="item in compareList"
                :key="item.id"
                class="bar-thumb-item"
              >
                {{ item.nameCn }}
                <el-icon class="bar-remove" @click.stop="removeFromCompare(item.id)"><Close /></el-icon>
              </span>
            </div>
          </div>
          <div class="bar-right">
            <el-button
              type="primary"
              size="default"
              :disabled="compareList.length < 2"
              @click="startCompare"
            >
              <el-icon><DataLine /></el-icon>开始对比
            </el-button>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close, DataLine, Delete, Picture, Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useCompare } from '@/composables/useCompare'
import { extractData, getImageUrl as getImgUrl, getFirstImage, PLACEHOLDER_IMG } from '@/utils'
import { logFetchError } from '@/utils'

const route = useRoute()
const router = useRouter()

const {
  compareList,
  isInCompare,
  addToCompare,
  removeFromCompare,
  clearCompare,
  MAX_COMPARE
} = useCompare()

const pageLoading = ref(false)
const comparePlants = ref([])
const searchVisible = ref(false)
const searchKeyword = ref('')
const searchLoading = ref(false)
const searched = ref(false)
const searchResults = ref([])

const compareRows = [
  { key: 'images', label: '图片' },
  { key: 'nameCn', label: '中文名' },
  { key: 'nameDong', label: '侗语名' },
  { key: 'scientificName', label: '学名' },
  { key: 'category', label: '类别' },
  { key: 'usageWay', label: '用法' },
  { key: 'habitat', label: '生境' },
  { key: 'efficacy', label: '功效' }
]

const getImageUrl = (plant) => {
  if (!plant) return PLACEHOLDER_IMG
  return getFirstImage(plant.images)
}

const getAllImages = (plant) => {
  if (!plant || !plant.images) return []
  if (typeof plant.images === 'string') {
    try {
      const parsed = JSON.parse(plant.images)
      return Array.isArray(parsed)
        ? parsed.map(i => getImgUrl(typeof i === 'string' ? i : i.url))
        : plant.images.split(',').map(s => getImgUrl(s.trim()))
    } catch {
      return plant.images.split(',').map(s => getImgUrl(s.trim()))
    }
  }
  if (Array.isArray(plant.images)) {
    return plant.images.map(i => getImgUrl(typeof i === 'string' ? i : i.url))
  }
  return []
}

const fetchPlantsByIds = async (ids) => {
  if (!ids || ids.length === 0) {
    comparePlants.value = []
    return
  }
  pageLoading.value = true
  try {
    const res = await request.post('/plants/batch', ids)
    const data = extractData(res)
    comparePlants.value = data.length > 0 ? data.slice(0, MAX_COMPARE) : []
  } catch (e) {
    logFetchError('药材对比数据', e)
    ElMessage.error('加载对比数据失败')
    comparePlants.value = []
  } finally {
    pageLoading.value = false
  }
}

const initFromQuery = () => {
  const idsParam = route.query.ids
  if (idsParam) {
    const ids = String(idsParam).split(',').map(Number).filter(Boolean)
    if (ids.length > 0) {
      compareList.value = []
      ids.slice(0, MAX_COMPARE).forEach(id => {
        compareList.value.push({ id, nameCn: '加载中...' })
      })
      fetchPlantsByIds(ids)
      return
    }
  }
  if (compareList.value.length > 0) {
    const ids = compareList.value.map(p => p.id)
    fetchPlantsByIds(ids)
  }
}

const doPlantSearch = async () => {
  if (!searchKeyword.value.trim()) return
  searchLoading.value = true
  searched.value = true
  try {
    const res = await request.get('/plants/search', {
      params: { keyword: searchKeyword.value.trim(), page: 1, size: 20 }
    })
    searchResults.value = extractData(res)
  } catch (e) {
    logFetchError('药材搜索', e)
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

const onSearchClear = () => {
  searchKeyword.value = ''
  searched.value = false
  searchResults.value = []
}

const handleAddPlant = (plant) => {
  const added = addToCompare(plant)
  if (added) {
    comparePlants.value.push(plant)
    ElMessage.success(`已添加"${plant.nameCn}"到对比列表`)
    if (compareList.value.length >= MAX_COMPARE) {
      searchVisible.value = false
    }
  }
}

const startCompare = () => {
  if (compareList.value.length < 2) return
  const ids = compareList.value.map(p => p.id).join(',')
  router.push({ path: '/compare', query: { ids } })
}

watch(() => route.query.ids, (newVal) => {
  if (!newVal) return
  const ids = String(newVal).split(',').map(Number).filter(Boolean)
  if (ids.length > 0) {
    fetchPlantsByIds(ids)
  }
})

onMounted(() => {
  initFromQuery()
})
</script>

<style scoped>
.compare-container {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-wrap {
  padding: var(--space-xl);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
}

.empty-compare {
  display: flex;
  justify-content: center;
  padding: var(--space-2xl) 0;
}

.compare-actions-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}

.compare-table-wrap {
  overflow-x: auto;
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.compare-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}

.compare-table thead th {
  background: var(--bg-rice);
  padding: var(--space-lg) var(--space-md);
  border-bottom: 2px solid var(--border-light);
  text-align: center;
  min-width: 180px;
}

.compare-table thead th.row-label-col {
  min-width: 80px;
  width: 80px;
  text-align: center;
}

.compare-table tbody td {
  padding: var(--space-md);
  border-bottom: 1px solid var(--border-light);
  text-align: center;
  vertical-align: middle;
}

.compare-table tbody td.row-label {
  background: var(--bg-rice);
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  text-align: center;
  width: 80px;
}

.row-label .label-text {
  display: block;
  white-space: nowrap;
}

.row-value {
  color: var(--text-primary);
  font-size: var(--font-size-sm);
  line-height: 1.5;
  word-break: break-word;
}

.row-stripe td {
  background: rgba(26, 82, 118, 0.02);
}

.col-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  position: relative;
}

.col-thumb {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 2px solid var(--border-light);
}

.col-img {
  width: 100%;
  height: 100%;
}

.col-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-rice);
  color: var(--text-muted);
  font-size: 24px;
}

.col-name {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
  color: var(--text-primary);
}

.remove-btn {
  font-size: var(--font-size-xs);
  padding: 2px 4px;
}

.add-col {
  background: var(--bg-rice) !important;
  vertical-align: middle;
  min-width: 120px !important;
  width: 120px !important;
}

.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  padding: var(--space-xl);
  cursor: pointer;
  color: var(--text-muted);
  border: 2px dashed var(--border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  min-height: 120px;
}

.add-card:hover {
  border-color: var(--dong-blue);
  color: var(--dong-blue);
  background: rgba(26, 82, 118, 0.04);
}

.images-cell {
  display: flex;
  gap: var(--space-xs);
  justify-content: center;
  flex-wrap: wrap;
}

.thumb-img {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  object-fit: cover;
}

.no-data {
  color: var(--text-muted);
  font-style: italic;
  font-size: var(--font-size-xs);
}

.search-dialog-content {
  min-height: 200px;
}

.dialog-search-box {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}

.dialog-search-box .el-input {
  flex: 1;
}

.search-loading {
  padding: var(--space-lg);
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  max-height: 400px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.search-result-item:hover {
  background: rgba(26, 82, 118, 0.06);
}

.search-result-item.already-added {
  opacity: 0.6;
  background: var(--bg-hover);
}

.result-thumb {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-rice);
  color: var(--text-muted);
}

.result-img {
  width: 100%;
  height: 100%;
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.result-name {
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  color: var(--text-primary);
}

.result-dong {
  font-size: var(--font-size-xs);
  color: var(--dong-indigo);
}

.result-cat {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.floating-compare-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-md) var(--space-xl);
  background: var(--text-inverse);
  border-radius: 48px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-light);
}

.bar-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.bar-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  white-space: nowrap;
}

.bar-label strong {
  color: var(--dong-blue);
  font-size: var(--font-size-md);
}

.bar-thumbs {
  display: flex;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.bar-thumb-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--bg-rice);
  border-radius: 20px;
  font-size: var(--font-size-xs);
  color: var(--text-primary);
}

.bar-remove {
  cursor: pointer;
  font-size: 12px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.bar-remove:hover {
  color: var(--color-danger);
}

.bar-right {
  flex-shrink: 0;
}

.float-bar-fade-enter-active,
.float-bar-fade-leave-active {
  transition: all 0.3s ease;
}

.float-bar-fade-enter-from,
.float-bar-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}

@media (max-width: 768px) {
  .compare-table-wrap {
    border-radius: var(--radius-md);
  }

  .compare-table thead th {
    padding: var(--space-md) var(--space-sm);
    min-width: 140px;
  }

  .compare-table thead th.row-label-col {
    min-width: 60px;
    width: 60px;
  }

  .compare-table tbody td {
    padding: var(--space-sm);
    font-size: var(--font-size-xs);
  }

  .compare-table tbody td.row-label {
    width: 60px;
  }

  .col-thumb {
    width: 60px;
    height: 60px;
  }

  .floating-compare-bar {
    bottom: 12px;
    left: 12px;
    right: 12px;
    transform: none;
    flex-direction: column;
    gap: var(--space-md);
    padding: var(--space-md);
    border-radius: var(--radius-lg);
  }

  .bar-left {
    flex-direction: column;
    gap: var(--space-sm);
    width: 100%;
  }

  .bar-thumbs {
    justify-content: center;
  }

  .bar-right {
    width: 100%;
  }

  .bar-right .el-button {
    width: 100%;
  }

  .dialog-search-box {
    flex-direction: column;
  }

  .search-result-item {
    flex-wrap: wrap;
  }

  .result-info {
    flex: 1 1 60%;
  }
}

@media (max-width: 480px) {
  .compare-table thead th {
    min-width: 120px;
    padding: var(--space-sm);
  }

  .col-thumb {
    width: 48px;
    height: 48px;
  }

  .col-name {
    font-size: var(--font-size-xs);
  }
}
</style>
