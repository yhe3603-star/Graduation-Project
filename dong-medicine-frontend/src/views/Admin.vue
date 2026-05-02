<template>
  <div class="admin-page">
    <AdminSidebar
      :active-menu="activeMenu"
      :user-name="userName"
      :logout-loading="logoutLoading"
      @update:active-menu="activeMenu = $event"
      @logout="logout"
    />

    <div class="admin-main">
      <div class="admin-header">
        <div class="header-left">
          <h2>{{ menuTitles[activeMenu] }}</h2>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">
首页
</el-breadcrumb-item>
            <el-breadcrumb-item>管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ menuTitles[activeMenu] }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <el-button @click="$router.push('/')">
          <el-icon><HomeFilled /></el-icon>返回前台
        </el-button>
      </div>

      <div class="admin-content">
        <AdminDashboard
          v-if="activeMenu === 'dashboard'"
          :stats="adminStats"
          :users="users"
          :knowledge="knowledgeList"
          :inheritors="inheritorsList"
          :plants="plantsList"
          :qa="qaList"
          :resources="resourcesList"
          :feedback="feedbackList"
          :quiz="quizList"
          :comments="commentsList"
          @view-feedback="activeMenu = 'feedback'"
          @navigate="activeMenu = $event"
        />

        <AdminDataTable
          v-if="activeMenu === 'users'"
          v-bind="getTableConfig('users')"
          :data="sortedUsers"
          server-pagination
          :server-total="pagination.users.total"
          :page="pagination.users.page"
          :page-size="pagination.users.size"
          @page-change="(p) => handleAdminPage('users', p)"
          @size-change="(s) => handleAdminSize('users', s)"
          @view="viewUser"
          @delete="deleteUser"
        >
          <template #status="{ row }">
            <el-tag :type="row.status === 'banned' ? 'danger' : 'success'">
              {{ row.status === 'banned' ? '已封禁' : '正常' }}
            </el-tag>
          </template>
          <template #actions="{ row }">
            <el-button type="info" size="small" style="color: var(--text-inverse);" @click="viewUser(row)">
查看
</el-button>
            <el-button v-if="row.status !== 'banned' && row.role !== 'admin'" type="danger" size="small" @click="confirmBanUser(row)">
封禁
</el-button>
            <el-button v-if="row.status === 'banned'" type="success" size="small" style="color: var(--text-inverse);" @click="confirmUnbanUser(row)">
解封
</el-button>
            <el-button type="danger" size="small" @click="deleteUser(row.id)">
删除
</el-button>
          </template>
        </AdminDataTable>

        <template v-for="item in standardTables" :key="item.key">
          <AdminDataTable
            v-if="activeMenu === item.key"
            v-bind="getTableConfig(item.key)"
            :data="dataMap[item.key]"
            server-pagination
            :server-total="pagination[item.key].total"
            :page="pagination[item.key].page"
            :page-size="pagination[item.key].size"
            @page-change="(p) => handleAdminPage(item.key, p)"
            @size-change="(s) => handleAdminSize(item.key, s)"
            @add="openDialog(item.formType)"
            @edit="(row) => editItem(item.formType, row, item.extraData?.(row))"
            @view="(row) => viewDetail(item.formType, row)"
            @delete="(id) => confirmDelete('确定删除？', () => request.delete(`${item.deletePath}/${id}`))"
          >
            <template v-if="item.key === 'resources'" #fileType="{ row }">
              <el-tag :type="getResourceFileType(row).tagType">
{{ getResourceFileType(row).text }}
</el-tag>
            </template>
            <template v-if="item.key === 'resources'" #fileSize="{ row }">
{{ getResourceFileSize(row) }}
</template>
            <template v-if="item.key === 'quiz'" #correctAnswer="{ row }">
              <el-tag type="success">
{{ getCorrectAnswerContent(row) }}
</el-tag>
            </template>
          </AdminDataTable>
        </template>

        <AdminDataTable
          v-if="activeMenu === 'comments'"
          v-bind="getTableConfig('comments')"
          :data="sortedComments"
          server-pagination
          :server-total="pagination.comments.total"
          :page="pagination.comments.page"
          :page-size="pagination.comments.size"
          @page-change="(p) => handleAdminPage('comments', p)"
          @size-change="(s) => handleAdminSize('comments', s)"
          @view="viewComment"
          @delete="deleteComment"
        >
          <template #actions="{ row }">
            <el-button type="info" size="small" style="color: var(--text-inverse);" @click="viewComment(row)">
查看
</el-button>
            <el-button v-if="row.status !== 'approved'" type="success" size="small" style="color: var(--text-inverse);" @click="approveComment(row)">
通过
</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" size="small" style="color: var(--text-inverse);" @click="rejectComment(row)">
拒绝
</el-button>
            <el-button type="danger" size="small" @click="deleteComment(row.id)">
删除
</el-button>
          </template>
        </AdminDataTable>

        <AdminDataTable
          v-if="activeMenu === 'feedback'"
          v-bind="getTableConfig('feedback')"
          :data="sortedFeedback"
          server-pagination
          :server-total="pagination.feedback.total"
          :page="pagination.feedback.page"
          :page-size="pagination.feedback.size"
          @page-change="(p) => handleAdminPage('feedback', p)"
          @size-change="(s) => handleAdminSize('feedback', s)"
          @view="viewFeedback"
          @delete="deleteFeedback"
        >
          <template #actions="{ row }">
            <el-button type="primary" size="small" style="color: var(--text-inverse);" @click="viewFeedback(row)">
查看
</el-button>
            <el-button v-if="row.status !== 'resolved'" type="success" size="small" style="color: var(--text-inverse);" @click="resolveFeedback(row)">
处理
</el-button>
            <el-button type="danger" size="small" @click="deleteFeedback(row.id)">
删除
</el-button>
          </template>
        </AdminDataTable>

        <div v-if="activeMenu === 'logs'" class="logs-section">
          <div class="logs-toolbar">
            <el-checkbox v-model="isAllLogsChecked" :indeterminate="isLogsIndeterminate" @change="toggleAllLogs">
全选
</el-checkbox>
            <el-button type="danger" :disabled="selectedLogIds.size === 0" @click="batchDeleteSelectedLogs">
              <el-icon><Delete /></el-icon>批量删除 ({{ selectedLogIds.size }})
            </el-button>
            <el-button type="danger" plain @click="clearAllLogs">
              <el-icon><DeleteFilled /></el-icon>清空所有日志
            </el-button>
          </div>
          <div class="virtual-log-wrapper">
            <div class="virtual-log-header">
              <span class="vlc vlc-check" />
              <span class="vlc vlc-module">模块</span>
              <span class="vlc vlc-type">类型</span>
              <span class="vlc vlc-user">用户</span>
              <span class="vlc vlc-op">操作</span>
              <span class="vlc vlc-ip">IP</span>
              <span class="vlc vlc-dur">耗时</span>
              <span class="vlc vlc-status">状态</span>
              <span class="vlc vlc-time">时间</span>
              <span class="vlc vlc-act">操作</span>
            </div>
            <VirtualList
              :items="logList"
              :item-size="48"
              :buffer="8"
              key-field="id"
              class="virtual-log-body"
            >
              <template #default="{ item }">
                <div class="virtual-log-row" :class="{ 'row-selected': selectedLogIds.has(item.id) }">
                  <span class="vlc vlc-check">
                    <el-checkbox :model-value="selectedLogIds.has(item.id)" @change="toggleLogSelect(item)" />
                  </span>
                  <span class="vlc vlc-module">
                    <el-tag :type="getLogModuleTagType(item.module)" size="small">{{ item.module }}</el-tag>
                  </span>
                  <span class="vlc vlc-type">
                    <el-tag :type="getLogTypeTagType(item.type)" size="small">{{ item.type }}</el-tag>
                  </span>
                  <span class="vlc vlc-user">{{ item.username || '-' }}</span>
                  <span class="vlc vlc-op" :title="item.operation">{{ item.operation }}</span>
                  <span class="vlc vlc-ip">{{ item.ip }}</span>
                  <span class="vlc vlc-dur" :class="{ 'slow-request': item.duration > 1000 }">{{ item.duration }}ms</span>
                  <span class="vlc vlc-status">
                    <el-tag :type="item.success ? 'success' : 'danger'" size="small">{{ item.success ? '成功' : '失败' }}</el-tag>
                  </span>
                  <span class="vlc vlc-time">{{ formatLogTime(item.createdAt) }}</span>
                  <span class="vlc vlc-act">
                    <el-button type="info" size="small" @click="viewLog(item)">查看</el-button>
                    <el-button type="danger" size="small" @click="deleteLog(item.id)">删除</el-button>
                  </span>
                </div>
              </template>
            </VirtualList>
          </div>
        </div>
      </div>
    </div>

    <UserDetailDialog v-model:visible="detailVisible.user" :user="currentDetail" @ban="handleBanUser" @unban="handleUnbanUser" />
    <KnowledgeDetailDialog v-model:visible="detailVisible.knowledge" :knowledge="currentDetail" />
    <InheritorDetailDialog v-model:visible="detailVisible.inheritor" :inheritor="currentDetail" />
    <PlantDetailDialog v-model:visible="detailVisible.plant" :plant="currentDetail" />
    <QaDetailDialog v-model:visible="detailVisible.qa" :qa="currentDetail" />
    <ResourceDetailDialog v-model:visible="detailVisible.resource" :resource="currentDetail" />
    <QuizDetailDialog v-model:visible="detailVisible.quiz" :quiz="currentDetail" />
    <CommentDetailDialog v-model:visible="commentDetailVisible" :comment="currentComment" @approve="approveComment" @delete="deleteComment" />
    <FeedbackDetailDialog v-model:visible="feedbackDetailVisible" :feedback="currentFeedback" @reply="handleReplyFeedback" />
    <LogDetailDialog v-model:visible="logDetailVisible" :log="currentLog" />

    <KnowledgeFormDialog v-model:visible="dialogVisible.knowledge" :data="formData.knowledge" @save="saveKnowledge" />
    <InheritorFormDialog v-model:visible="dialogVisible.inheritor" :data="formData.inheritor" @save="saveInheritor" />
    <PlantFormDialog v-model:visible="dialogVisible.plant" :data="formData.plant" @save="savePlant" />
    <QaFormDialog v-model:visible="dialogVisible.qa" :data="formData.qa" @save="saveQa" />
    <ResourceFormDialog v-model:visible="dialogVisible.resource" :data="formData.resource" @save="saveResource" />
    <QuizFormDialog v-model:visible="dialogVisible.quiz" :data="formData.quiz" @save="saveQuiz" />
  </div>
</template>

<script setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, DeleteFilled, HomeFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import AdminDashboard from '@/components/business/admin/AdminDashboard.vue'
import AdminDataTable from '@/components/business/admin/AdminDataTable.vue'
import AdminSidebar from '@/components/business/admin/AdminSidebar.vue'
import VirtualList from '@/components/base/VirtualList.vue'

const CommentDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/CommentDetailDialog.vue'))
const FeedbackDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/FeedbackDetailDialog.vue'))
const InheritorDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/InheritorDetailDialog.vue'))
const KnowledgeDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/KnowledgeDetailDialog.vue'))
const LogDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/LogDetailDialog.vue'))
const PlantDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/PlantDetailDialog.vue'))
const QaDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/QaDetailDialog.vue'))
const QuizDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/QuizDetailDialog.vue'))
const ResourceDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/ResourceDetailDialog.vue'))
const UserDetailDialog = defineAsyncComponent(() => import('@/components/business/admin/dialogs/UserDetailDialog.vue'))
const InheritorFormDialog = defineAsyncComponent(() => import('@/components/business/admin/forms/InheritorFormDialog.vue'))
const KnowledgeFormDialog = defineAsyncComponent(() => import('@/components/business/admin/forms/KnowledgeFormDialog.vue'))
const PlantFormDialog = defineAsyncComponent(() => import('@/components/business/admin/forms/PlantFormDialog.vue'))
const QaFormDialog = defineAsyncComponent(() => import('@/components/business/admin/forms/QaFormDialog.vue'))
const QuizFormDialog = defineAsyncComponent(() => import('@/components/business/admin/forms/QuizFormDialog.vue'))
const ResourceFormDialog = defineAsyncComponent(() => import('@/components/business/admin/forms/ResourceFormDialog.vue'))

import { useAdminData, useAdminDialogs, useAdminActions } from '@/composables/useAdminData'
import {
  getCorrectAnswerContent, menuTitles, formatFileSize, getFileTypeTagType, getFileTypeText,
  TABLE_CONFIGS, getLogModuleTagType, getLogTypeTagType, formatLogTime
} from '@/utils/adminUtils'
import { parseMediaList, getMediaType } from '@/utils/media'

const router = useRouter()
const updateUserState = inject('updateUserState')
const userStore = useUserStore()

const userName = computed(() => userStore.username || '管理员')
const activeMenu = ref('dashboard')
const logoutLoading = ref(false)

const {
  users, knowledgeList, inheritorsList, plantsList, qaList,
  resourcesList, feedbackList, quizList, commentsList, logList,
  adminStats, pagination, sortedComments, sortedFeedback, sortedUsers,
  fetchData, switchSection, handleAdminPage, handleAdminSize
} = useAdminData(request)

const {
  dialogVisible, detailVisible, currentDetail, formData,
  commentDetailVisible, currentComment, feedbackDetailVisible, currentFeedback,
  logDetailVisible, currentLog, openDialog, viewDetail, editItem
} = useAdminDialogs()

const { confirmDelete, approveComment: doApproveComment, rejectComment: doRejectComment,
  clearAllLogs, replyFeedback
} = useAdminActions(request, fetchData)

watch(activeMenu, (menu) => {
  if (menu !== 'dashboard') switchSection(menu)
})

const selectedLogIds = ref(new Set())
const isAllLogsSelected = computed(() => logList.value.length > 0 && selectedLogIds.value.size === logList.value.length)
const isAllLogsChecked = computed({
  get: () => isAllLogsSelected.value,
  set: () => {}
})
const isLogsIndeterminate = computed(() => selectedLogIds.value.size > 0 && selectedLogIds.value.size < logList.value.length)
const toggleLogSelect = (item) => {
  const s = new Set(selectedLogIds.value)
  s.has(item.id) ? s.delete(item.id) : s.add(item.id)
  selectedLogIds.value = s
}
const toggleAllLogs = (checked) => {
  selectedLogIds.value = checked ? new Set(logList.value.map(l => l.id)) : new Set()
}
const batchDeleteSelectedLogs = async () => {
  if (selectedLogIds.value.size === 0) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedLogIds.value.size} 条日志？`, '提示', { type: 'warning' })
    await request.delete('/admin/logs/batch', { data: [...selectedLogIds.value] })
    ElMessage.success('批量删除成功')
    selectedLogIds.value = new Set()
    fetchData()
  } catch (e) { if (e !== 'cancel') console.debug('批量删除日志失败:', e) }
}

const dataMap = computed(() => ({
  knowledge: knowledgeList.value,
  inheritors: inheritorsList.value,
  plants: plantsList.value,
  qa: qaList.value,
  resources: resourcesList.value,
  quiz: quizList.value
}))

const standardTables = [
  { key: 'knowledge', formType: 'knowledge', deletePath: '/admin/knowledge' },
  { key: 'inheritors', formType: 'inheritor', deletePath: '/admin/inheritors', extraData: (row) => ({ experienceYears: row.experienceYears || 0 }) },
  { key: 'plants', formType: 'plant', deletePath: '/admin/plants' },
  { key: 'qa', formType: 'qa', deletePath: '/admin/qa' },
  { key: 'resources', formType: 'resource', deletePath: '/admin/resources', extraData: (row) => ({ fileSize: row.fileSize || 0 }) },
  { key: 'quiz', formType: 'quiz', deletePath: '/quiz' }
]

const getTableConfig = (key) => {
  const config = TABLE_CONFIGS[key]
  return {
    title: config.title,
    titleName: config.titleName,
    columns: config.columns,
    showTitle: config.showTitle !== false,
    showAdd: config.showAdd !== false,
    showEdit: config.showEdit !== false,
    actionWidth: config.actionWidth || 220
  }
}

const viewUser = (row) => viewDetail('user', row)
const deleteUser = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/users/${id}`))

const confirmBanUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 "${user.username}" 吗？封禁后该用户将无法登录系统。`, '封禁确认', { type: 'warning', confirmButtonText: '确定封禁', cancelButtonText: '取消' })
    await handleBanUser(user)
  } catch {}
}

const confirmUnbanUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要解除用户 "${user.username}" 的封禁吗？`, '解封确认', { type: 'info', confirmButtonText: '确定解封', cancelButtonText: '取消' })
    await handleUnbanUser(user)
  } catch {}
}

const handleBanUser = async (user) => {
  try {
    await request.put(`/admin/users/${user.id}/ban`)
    ElMessage.success('用户已被封禁')
    detailVisible.value.user = false
    fetchData()
  } catch { ElMessage.error('封禁失败') }
}

const handleUnbanUser = async (user) => {
  try {
    await request.put(`/admin/users/${user.id}/unban`)
    ElMessage.success('用户已解封')
    detailVisible.value.user = false
    fetchData()
  } catch { ElMessage.error('解封失败') }
}

const getResourceFileData = (row) => {
  if (!row?.files) return null
  try { const files = parseMediaList(row.files); return files.length > 0 ? files[0] : null }
  catch { return null }
}

const getResourceFileType = (row) => {
  const fileData = getResourceFileData(row)
  if (!fileData) return { tagType: 'info', text: '未知' }
  const type = fileData.type || getMediaType(fileData.path || fileData.url || '')
  return { tagType: getFileTypeTagType(type), text: getFileTypeText(type) }
}

const getResourceFileSize = (row) => {
  const fileData = getResourceFileData(row)
  return fileData?.size ? formatFileSize(fileData.size) : '-'
}

const createSaveHandler = (endpoint) => async (data) => {
  const success = await (data.id ? request.put(`${endpoint}/${data.id}`, data) : request.post(endpoint, data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); fetchData() }
  else ElMessage.error('保存失败')
}

const saveKnowledge = createSaveHandler('/admin/knowledge')
const saveInheritor = createSaveHandler('/admin/inheritors')
const savePlant = createSaveHandler('/admin/plants')
const saveQa = createSaveHandler('/admin/qa')
const saveResource = createSaveHandler('/admin/resources')
const saveQuiz = async (data) => {
  const success = await (data.id ? request.put('/quiz/update', data) : request.post('/quiz/add', data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); dialogVisible.value.quiz = false; fetchData() }
  else ElMessage.error('保存失败')
}

const viewComment = (row) => { currentComment.value = row; commentDetailVisible.value = true }
const deleteComment = (id) => confirmDelete('确定删除该评论？', () => request.delete(`/admin/comments/${id}`))
const approveComment = async (row) => { await doApproveComment(row) }
const rejectComment = async (row) => { await doRejectComment(row) }

const viewFeedback = (row) => { currentFeedback.value = row; feedbackDetailVisible.value = true }
const resolveFeedback = (row) => { currentFeedback.value = row; feedbackDetailVisible.value = true }
const deleteFeedback = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/feedback/${id}`))
const handleReplyFeedback = async ({ feedback, reply }) => {
  const success = await replyFeedback({ feedback, reply })
  if (success) { feedbackDetailVisible.value = false; fetchData() }
}

const viewLog = (row) => { currentLog.value = row; logDetailVisible.value = true }
const deleteLog = (id) => confirmDelete('确定删除该日志？', () => request.delete(`/admin/logs/${id}`))

const logout = async () => {
  try { await ElMessageBox.confirm('确定要退出登录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }) }
  catch { return }
  logoutLoading.value = true
  try { await request.post('/user/logout').catch(() => {}) }
  finally { userStore.clearAuth(); updateUserState(); ElMessage.success('已退出登录'); router.push('/') }
}

onMounted(() => {
  if (!userStore.isAdmin) { ElMessage.error('您没有管理员权限'); router.push('/'); return }
  fetchData()
})
</script>

<style scoped>
.admin-page { display: flex; min-height: 100vh; background: #f5f7fa; }
.admin-main { flex: 1; margin-left: 240px; display: flex; flex-direction: column; min-height: 100vh; }
.admin-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: var(--text-inverse); border-bottom: 1px solid #e8e8e8; flex-shrink: 0; }
.header-left h2 { margin: 0 0 4px 0; font-size: 20px; color: var(--text-primary); }
.admin-content { flex: 1; padding: 24px; overflow-y: auto; }
.slow-request { color: #f56c6c; font-weight: 500; }
.logs-section { background: var(--text-inverse); border-radius: 8px; padding: 16px; }
.logs-toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.virtual-log-wrapper { border: 1px solid #ebeef5; border-radius: 6px; overflow: hidden; }
.virtual-log-header {
  display: grid;
  grid-template-columns: 40px 90px 80px 100px 1fr 120px 80px 70px 160px 140px;
  align-items: center;
  padding: 0 12px;
  height: 40px;
  background: #f5f7fa;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  border-bottom: 1px solid #ebeef5;
}
.virtual-log-body { height: calc(100vh - 360px); }
.virtual-log-row {
  display: grid;
  grid-template-columns: 40px 90px 80px 100px 1fr 120px 80px 70px 160px 140px;
  align-items: center;
  padding: 0 12px;
  font-size: 13px;
  color: #606266;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}
.virtual-log-row:hover { background: #f5f7fa; }
.virtual-log-row.row-selected { background: #ecf5ff; }
.vlc { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.vlc-op { font-size: 12px; }
.vlc-act { display: flex; gap: 4px; }
.vlc-act .el-button { padding: 4px 8px; font-size: 12px; }
:deep(.el-descriptions__body .el-descriptions__table .el-descriptions__cell) { word-break: break-all; }
</style>
