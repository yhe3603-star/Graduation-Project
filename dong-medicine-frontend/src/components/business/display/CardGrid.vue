<template>
  <div class="card-grid">
    <transition-group name="card-fade">
      <div 
        v-for="item in items" 
        :key="item.id" 
        class="card-item"
        @click="$emit('click', item)"
      >
        <div
          v-if="showImage"
          class="card-image"
        >
          <el-image
            :src="getImage(item)"
            fit="cover"
            class="item-img"
            lazy
            :scroll-container="scrollContainer"
          >
            <template #placeholder>
              <div class="img-placeholder">
                加载中...
              </div>
            </template>
            <template #error>
              <div class="img-error">
                {{ imageErrorText }}
              </div>
            </template>
          </el-image>
          <div
            v-if="item.badge || item.difficulty || item.level"
            class="card-badge"
            :class="getBadgeClass(item)"
          >
            {{ item.badge || item.difficulty || item.level }}
          </div>
        </div>
        
        <div class="card-content">
          <div class="card-title-row">
            <h3 class="card-title">
              {{ getTitle(item) }}
            </h3>
            <span
              v-if="item.subtitle || item.nameDong"
              class="card-subtitle"
            >{{ item.subtitle || item.nameDong }}</span>
          </div>
          <p
            v-if="item.desc || item.efficacy || item.bio"
            class="card-desc"
          >
            {{ (item.desc || item.efficacy || item.bio || '').substring(0, descLength) }}...
          </p>
          <div class="card-footer">
            <slot
              name="footer"
              :item="item"
            >
              <el-tag
                v-if="item.tag || item.category"
                size="small"
                effect="light"
              >
                {{ item.tag || item.category }}
              </el-tag>
            </slot>
          </div>
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
const props = defineProps({
  items: { type: Array, default: () => [] },
  showImage: { type: Boolean, default: true },
  imageField: { type: String, default: "images" },
  imageErrorText: { type: String, default: "图片" },
  titleField: { type: String, default: "title" },
  descLength: { type: Number, default: 50 }
})

defineEmits(["click"])

const getImage = (item) => {
  const imgs = item[props.imageField]
  if (!imgs) return ""
  try {
    const parsed = typeof imgs === "string" ? JSON.parse(imgs) : imgs
    if (Array.isArray(parsed)) {
      if (parsed.length === 0) return ""
      const firstItem = parsed[0]
      const path = typeof firstItem === "object" && firstItem.path ? firstItem.path : firstItem
      if (path.startsWith('http://') || path.startsWith('https://')) return path
      return path.startsWith('/') ? path : '/' + path
    }
    if (imgs.startsWith('http://') || imgs.startsWith('https://')) return imgs
    return imgs.startsWith('/') ? imgs : '/' + imgs
  } catch {
    if (imgs.startsWith('http://') || imgs.startsWith('https://')) return imgs
    return imgs.startsWith('/') ? imgs : '/' + imgs
  }
}

const getTitle = (item) => item[props.titleField] || item.nameCn || item.name || item.title || "未命名"

const BADGE_CLASS_MAP = {
  gold: ["easy", "beginner", "初级", "省级", "自治区级"],
  green: ["medium", "advanced", "中级"],
  blue: ["hard", "professional", "高级", "市级"]
}

const getBadgeClass = (item) => {
  const val = (item.badge || item.difficulty || item.level || "").toLowerCase()
  for (const [cls, keys] of Object.entries(BADGE_CLASS_MAP)) {
    if (keys.includes(val)) return `badge-${cls}`
  }
  return ""
}
</script>

<style scoped>
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.card-item { background: var(--text-inverse); border-radius: 16px; overflow: hidden; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); cursor: pointer; transition: all 0.3s; }
.card-item:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(26, 82, 118, 0.12); }
.card-image { height: 180px; position: relative; overflow: hidden; }
.item-img { width: 100%; height: 100%; }
.img-placeholder, .img-error { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; background: linear-gradient(135deg, #e8f4f8, #d4e8ed); color: var(--dong-blue); }
.card-badge { position: absolute; top: 12px; right: 12px; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; color: var(--text-inverse); }
.badge-gold { background: var(--dong-gold-light); }
.badge-green { background: var(--dong-green); }
.badge-blue { background: #3498db; }
.card-content { padding: 16px; }
.card-title-row { display: flex; align-items: baseline; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }
.card-title { font-size: 16px; font-weight: 600; color: var(--text-primary); margin: 0; }
.card-subtitle { font-size: 12px; color: var(--text-muted); }
.card-desc { font-size: 13px; color: #666; line-height: 1.6; margin: 0 0 12px 0; }
.card-footer { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.card-fade-enter-active, .card-fade-leave-active { transition: all 0.3s ease; }
.card-fade-enter-from, .card-fade-leave-to { opacity: 0; transform: translateY(-10px); }

@media (max-width: 768px) {
  .card-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .card-image { height: 140px; }
  .card-content { padding: 12px; }
  .card-title { font-size: 14px; }
  .card-desc { font-size: 12px; margin-bottom: 8px; }
  .card-badge { top: 8px; right: 8px; padding: 3px 8px; font-size: 11px; }
}

@media (max-width: 480px) {
  .card-grid { grid-template-columns: 1fr; gap: 16px; }
  .card-image { height: 180px; }
  .card-content { padding: 16px; }
  .card-title { font-size: 16px; }
  .card-desc { font-size: 13px; }
}
</style>
