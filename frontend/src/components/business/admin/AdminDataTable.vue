<template>
  <div class="data-table-section">
    <div class="table-header">
      <el-input
        v-model="searchKey"
        placeholder="搜索..."
        prefix-icon="Search"
        clearable
        style="width: 240px"
      />
      <el-button
        v-if="showAdd"
        type="primary"
        @click="$emit('add')"
      >
        <el-icon><Plus /></el-icon>
        添加{{ title }}
      </el-button>
    </div>
    
    <el-table 
      :data="paginatedData" 
      stripe 
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <slot name="extraColumns" />
      <el-table-column
        prop="id"
        label="ID"
        width="70"
      />
      <el-table-column
        v-if="showTitle"
        prop="title"
        :label="titleName"
        min-width="150"
      >
        <template #default="{ row }">
          {{ row.title || row.nameCn || row.name || row.question || row.content?.substring(0, 50) || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-for="col in columns"
        :key="col.prop || col.label"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
      >
        <template #default="{ row }">
          <slot v-if="col.slotName" :name="col.slotName" :row="row" />
          <span v-else-if="!col.type">{{ row[col.prop] || '-' }}</span>
          <el-tag
            v-else-if="col.type === 'tag'"
            :type="getTagType(row[col.prop])"
          >
            {{ getStatusText(row[col.prop]) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        :width="actionWidth"
        fixed="right"
      >
        <template #default="{ row }">
          <slot
            name="actions"
            :row="row"
          >
            <el-button
              type="info"
              size="small"
              style="color: var(--text-inverse);"
              @click="$emit('view', row)"
            >
              查看
            </el-button>
            <el-button
              v-if="showEdit"
              type="primary"
              size="small"
              style="color: var(--text-inverse);"
              @click="$emit('edit', row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="$emit('delete', row.id)"
            >
              删除
            </el-button>
          </slot>
        </template>
      </el-table-column>
    </el-table>

    <Pagination 
      v-if="filteredData.length > 0" 
      :page="currentPage" 
      :size="pageSize" 
      :total="filteredData.length" 
      :page-sizes="[12, 24, 50, 100]"
      @update:page="currentPage = $event" 
      @update:size="pageSize = $event" 
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { Plus } from "@element-plus/icons-vue";
import Pagination from "@/components/business/display/Pagination.vue";

const props = defineProps({
  title: String,
  titleName: { type: String, default: "名称" },
  data: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  showAdd: { type: Boolean, default: true },
  showEdit: { type: Boolean, default: true },
  showTitle: { type: Boolean, default: true },
  actionWidth: { type: String, default: "200" }
});

const emit = defineEmits(["add", "edit", "delete", "view", "selection-change"]);

const searchKey = ref("");
const currentPage = ref(1);
const pageSize = ref(12);

const filteredData = computed(() => {
  const data = props.data;
  if (!data) return [];
  if (!Array.isArray(data)) return [];
  if (!searchKey.value) return data;
  const key = searchKey.value.toLowerCase();
  return data.filter(item => 
    (item.title || item.nameCn || item.name || item.question || item.content || "").toLowerCase().includes(key)
  );
});

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredData.value.slice(start, start + pageSize.value);
});

watch(searchKey, () => { currentPage.value = 1; });

const handleSelectionChange = (selection) => {
  emit("selection-change", selection);
};

const TAG_TYPES = { 
  easy: "success", medium: "warning", hard: "danger", 
  "省级": "warning", "自治区级": "success", "州级": "primary", "市级": "primary", 
  approved: "success", pending: "warning", rejected: "danger",
  video: "primary", document: "success", image: "warning"
};

const STATUS_TEXTS = { 
  approved: "已审核", pending: "待审核", rejected: "已拒绝",
  resolved: "已解决", processing: "处理中",
  video: "视频", document: "文档", image: "图片"
};

const getTagType = (val) => TAG_TYPES[val] || "info";
const getStatusText = (val) => STATUS_TEXTS[val] || val;
</script>

<style scoped>
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
</style>
