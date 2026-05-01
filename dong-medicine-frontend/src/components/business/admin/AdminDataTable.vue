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
      <div class="table-header-right">
        <el-button
          v-if="entity"
          :loading="exportLoading"
          @click="handleExportCSV"
        >
          <el-icon><Download /></el-icon>
          导出CSV
        </el-button>
        <el-button
          v-if="showAdd"
          type="primary"
          @click="$emit('add')"
        >
          <el-icon><Plus /></el-icon>
          添加{{ title }}
        </el-button>
      </div>
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
      v-if="paginationTotal > 0"
      :page="displayPage"
      :size="displayPageSize"
      :total="paginationTotal"
      :page-sizes="[20, 50, 100, 200]"
      @update:page="onPageUpdate"
      @update:size="onSizeUpdate"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { Plus, Download } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import Pagination from "@/components/business/display/Pagination.vue";
import request from "@/utils/request";

const props = defineProps({
  title: { type: String, default: "" },
  titleName: { type: String, default: "名称" },
  /** Entity name for admin export API, e.g. "plants", "knowledge", "resources" */
  entity: { type: String, default: "" },
  data: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  showAdd: { type: Boolean, default: true },
  showEdit: { type: Boolean, default: true },
  showTitle: { type: Boolean, default: true },
  actionWidth: { type: String, default: "200" },
  /** 为 true 时由服务端分页，total/page/pageSize 来自父组件 */
  serverPagination: { type: Boolean, default: false },
  serverTotal: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 12 }
});

const emit = defineEmits(["add", "edit", "delete", "view", "selection-change", "page-change", "size-change"]);

const searchKey = ref("");
const currentPage = ref(1);
const internalPageSize = ref(12);
const exportLoading = ref(false);

const displayPage = computed(() => (props.serverPagination ? props.page : currentPage.value));
const displayPageSize = computed(() => (props.serverPagination ? props.pageSize : internalPageSize.value));

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

const paginationTotal = computed(() =>
  props.serverPagination ? props.serverTotal : filteredData.value.length
);

const paginatedData = computed(() => {
  if (props.serverPagination) return filteredData.value;
  const start = (currentPage.value - 1) * internalPageSize.value;
  return filteredData.value.slice(start, start + internalPageSize.value);
});

function onPageUpdate(p) {
  if (props.serverPagination) emit("page-change", p);
  else currentPage.value = p;
}

function onSizeUpdate(s) {
  if (props.serverPagination) emit("size-change", s);
  else internalPageSize.value = s;
}

watch(searchKey, () => {
  if (!props.serverPagination) currentPage.value = 1;
});

const handleSelectionChange = (selection) => {
  emit("selection-change", selection);
};

// ========== CSV Export ==========
const handleExportCSV = async () => {
  if (!props.entity) {
    ElMessage.warning("未配置导出实体");
    return;
  }

  exportLoading.value = true;
  try {
    const response = await request.get(`/admin/export/${props.entity}`, {
      params: { format: 'csv' },
      responseType: 'blob'
    });

    const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    const timestamp = new Date().toISOString().slice(0, 10);
    link.download = `${props.entity}_export_${timestamp}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    ElMessage.success(`${props.title || props.entity} 数据导出成功`);
  } catch (e) {
    console.error('导出CSV失败:', e);
    // Try to read error from blob
    if (e instanceof Blob || (e.data instanceof Blob)) {
      ElMessage.error('导出失败，服务器返回错误');
    } else {
      ElMessage.error('导出失败，请重试');
    }
  } finally {
    exportLoading.value = false;
  }
};

const TAG_TYPES = {
  easy: "success", medium: "warning", hard: "danger",
  "省级": "warning", "自治区级": "success", "州级": "primary", "市级": "primary",
  approved: "success", pending: "warning", rejected: "danger",
  resolved: "success", processing: "warning",
  video: "primary", document: "success", image: "warning",
  active: "success", banned: "danger",
  user: "info", admin: "warning"
};

const STATUS_TEXTS = {
  approved: "已审核", pending: "待审核", rejected: "已拒绝",
  resolved: "已解决", processing: "处理中",
  video: "视频", document: "文档", image: "图片",
  active: "正常", banned: "已封禁",
  user: "普通用户", admin: "管理员"
};

const getTagType = (val) => TAG_TYPES[val] || "info";
const getStatusText = (val) => STATUS_TEXTS[val] || val;
</script>

<style scoped>
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.table-header-right {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    align-items: stretch;
  }

  .table-header .el-input {
    width: 100% !important;
  }

  .table-header-right {
    justify-content: flex-end;
  }
}
</style>
