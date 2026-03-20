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
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
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
          title="用户"
          :data="sortedUsers"
          :columns="[{ prop: 'username', label: '用户名', minWidth: 120 }, { prop: 'role', label: '角色', type: 'tag' }, { prop: 'createdAt', label: '创建时间', width: 160 }]"
          :show-add="false"
          :show-edit="false"
          :show-title="false"
          action-width="100"
          @view="viewUser"
          @delete="deleteUser"
        />

        <AdminDataTable
          v-if="activeMenu === 'knowledge'"
          title="知识"
          :data="knowledgeList"
          :columns="[{ prop: 'type', label: '类型', width: 80 }, { prop: 'therapyCategory', label: '疗法分类', width: 100 }, { prop: 'diseaseCategory', label: '疾病分类', width: 100 }, { prop: 'popularity', label: '热度', width: 70 }]"
          @add="openDialog('knowledge')"
          @edit="editKnowledge"
          @view="viewKnowledge"
          @delete="deleteKnowledge"
        />
        
        <AdminDataTable
          v-if="activeMenu === 'inheritors'"
          title="传承人"
          :data="inheritorsList"
          :columns="[{ prop: 'level', label: '级别', type: 'tag', width: 80 }, { prop: 'specialties', label: '技艺特色', minWidth: 150 }, { prop: 'experienceYears', label: '经验年限', width: 90 }]"
          @add="openDialog('inheritor')"
          @edit="editInheritor"
          @view="viewInheritor"
          @delete="deleteInheritor"
        />
        
        <AdminDataTable
          v-if="activeMenu === 'plants'"
          title="植物"
          :data="plantsList"
          :columns="[{ prop: 'scientificName', label: '学名', minWidth: 120 }, { prop: 'category', label: '分类', width: 80 }, { prop: 'usageWay', label: '用法', width: 70 }, { prop: 'difficulty', label: '难度', type: 'tag', width: 70 }]"
          @add="openDialog('plant')"
          @edit="editPlant"
          @view="viewPlant"
          @delete="deletePlant"
        />
        
        <AdminDataTable
          v-if="activeMenu === 'qa'"
          title="问答"
          title-name="问题"
          :data="qaList"
          :columns="[{ prop: 'category', label: '分类', width: 100 }, { prop: 'popularity', label: '热度', width: 70 }]"
          @add="openDialog('qa')"
          @edit="editQa"
          @view="viewQa"
          @delete="deleteQa"
        />
        
        <AdminDataTable
          v-if="activeMenu === 'resources'"
          title="资源"
          :data="resourcesList"
          :columns="[{ prop: 'category', label: '分类', width: 100 }, { slotName: 'fileType', label: '类型', width: 70 }, { slotName: 'fileSize', label: '文件大小', width: 90 }, { prop: 'downloadCount', label: '下载次数', width: 90 }, { prop: 'popularity', label: '热度', width: 70 }]"
          @add="openDialog('resource')"
          @edit="editResource"
          @view="viewResource"
          @delete="deleteResource"
        >
          <template #fileType="{ row }">
            <el-tag :type="getResourceFileType(row).tagType">{{ getResourceFileType(row).text }}</el-tag>
          </template>
          <template #fileSize="{ row }">{{ getResourceFileSize(row) }}</template>
        </AdminDataTable>

        <AdminDataTable
          v-if="activeMenu === 'quiz'"
          title="题目"
          title-name="问题"
          :data="quizList"
          :columns="[{ prop: 'category', label: '分类', width: 100 }, { prop: 'difficulty', label: '难度', type: 'tag', width: 70 }, { slotName: 'correctAnswer', label: '正确答案', width: 150 }]"
          @add="openDialog('quiz')"
          @edit="editQuiz"
          @view="viewQuiz"
          @delete="deleteQuiz"
        >
          <template #correctAnswer="{ row }">
            <el-tag type="success">{{ getCorrectAnswerContent(row) }}</el-tag>
          </template>
        </AdminDataTable>

        <AdminDataTable
          v-if="activeMenu === 'comments'"
          title="评论"
          title-name="评论内容"
          :data="sortedComments"
          :columns="[{ prop: 'username', label: '用户', width: 100 }, { prop: 'targetType', label: '类型', width: 80 }, { prop: 'status', label: '状态', type: 'tag', width: 80 }]"
          :show-add="false"
          :show-edit="false"
          action-width="250"
          @view="viewComment"
          @delete="deleteComment"
        >
          <template #actions="{ row }">
            <el-button type="info" size="small" style="color: var(--text-inverse);" @click="viewComment(row)">查看</el-button>
            <el-button v-if="row.status !== 'approved'" type="success" size="small" style="color: var(--text-inverse);" @click="approveComment(row)">通过</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" size="small" style="color: var(--text-inverse);" @click="rejectComment(row)">拒绝</el-button>
            <el-button type="danger" size="small" @click="deleteComment(row.id)">删除</el-button>
          </template>
        </AdminDataTable>

        <AdminDataTable
          v-if="activeMenu === 'feedback'"
          title="反馈"
          title-name="标题"
          :data="sortedFeedback"
          :columns="[{ prop: 'userName', label: '用户', width: 100 }, { prop: 'type', label: '类型', width: 80 }, { prop: 'contact', label: '联系方式', width: 120 }, { prop: 'status', label: '状态', type: 'tag', width: 80 }]"
          :show-add="false"
          :show-edit="false"
          action-width="200"
          @view="viewFeedback"
          @delete="deleteFeedback"
        >
          <template #actions="{ row }">
            <el-button type="primary" size="small" style="color: var(--text-inverse);" @click="viewFeedback(row)">查看</el-button>
            <el-button v-if="row.status !== 'resolved'" type="success" size="small" style="color: var(--text-inverse);" @click="resolveFeedback(row)">处理</el-button>
            <el-button type="danger" size="small" @click="deleteFeedback(row.id)">删除</el-button>
          </template>
        </AdminDataTable>

        <div v-if="activeMenu === 'logs'" class="logs-section">
          <div class="logs-toolbar">
            <el-button type="danger" :disabled="selectedLogs.length === 0" @click="batchDeleteLogs">
              <el-icon><Delete /></el-icon>批量删除 ({{ selectedLogs.length }})
            </el-button>
            <el-button type="danger" plain @click="clearAllLogs">
              <el-icon><DeleteFilled /></el-icon>清空所有日志
            </el-button>
          </div>
          <AdminDataTable
            title="操作日志"
            :data="logList"
            :columns="[{ prop: 'username', label: '用户', width: 100 }, { prop: 'operation', label: '操作', minWidth: 150 }, { prop: 'ip', label: 'IP', width: 120 }]"
            :show-add="false"
            :show-edit="false"
            :show-title="false"
            action-width="140"
            @view="viewLog"
            @delete="deleteLog"
            @selection-change="handleLogSelectionChange"
          >
            <template #extraColumns>
              <el-table-column type="selection" width="50" />
              <el-table-column prop="module" label="模块" width="90">
                <template #default="{ row }">
                  <el-tag :type="getLogModuleTagType(row.module)" size="small">{{ row.module }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="80">
                <template #default="{ row }">
                  <el-tag :type="getLogTypeTagType(row.type)" size="small">{{ row.type }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="耗时" width="80">
                <template #default="{ row }">
                  <span :class="{ 'slow-request': row.duration > 1000 }">{{ row.duration }}ms</span>
                </template>
              </el-table-column>
              <el-table-column prop="success" label="状态" width="70">
                <template #default="{ row }">
                  <el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.success ? '成功' : '失败' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="时间" width="160">
                <template #default="{ row }">{{ formatLogTime(row.createdAt) }}</template>
              </el-table-column>
            </template>
            <template #actions="{ row }">
              <el-button type="info" size="small" style="color: var(--text-inverse);" @click="viewLog(row)">查看</el-button>
              <el-button type="danger" size="small" @click="deleteLog(row.id)">删除</el-button>
            </template>
          </AdminDataTable>
        </div>
      </div>
    </div>

    <UserDetailDialog v-model:visible="detailVisible.user" :user="currentDetail" />
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
import { computed, inject, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, DeleteFilled, HomeFilled } from '@element-plus/icons-vue'
import AdminDashboard from '@/components/business/admin/AdminDashboard.vue'
import AdminDataTable from '@/components/business/admin/AdminDataTable.vue'
import AdminSidebar from '@/components/business/admin/AdminSidebar.vue'
import CommentDetailDialog from '@/components/business/admin/dialogs/CommentDetailDialog.vue'
import FeedbackDetailDialog from '@/components/business/admin/dialogs/FeedbackDetailDialog.vue'
import InheritorDetailDialog from '@/components/business/admin/dialogs/InheritorDetailDialog.vue'
import KnowledgeDetailDialog from '@/components/business/admin/dialogs/KnowledgeDetailDialog.vue'
import LogDetailDialog from '@/components/business/admin/dialogs/LogDetailDialog.vue'
import PlantDetailDialog from '@/components/business/admin/dialogs/PlantDetailDialog.vue'
import QaDetailDialog from '@/components/business/admin/dialogs/QaDetailDialog.vue'
import QuizDetailDialog from '@/components/business/admin/dialogs/QuizDetailDialog.vue'
import ResourceDetailDialog from '@/components/business/admin/dialogs/ResourceDetailDialog.vue'
import UserDetailDialog from '@/components/business/admin/dialogs/UserDetailDialog.vue'
import InheritorFormDialog from '@/components/business/admin/forms/InheritorFormDialog.vue'
import KnowledgeFormDialog from '@/components/business/admin/forms/KnowledgeFormDialog.vue'
import PlantFormDialog from '@/components/business/admin/forms/PlantFormDialog.vue'
import QaFormDialog from '@/components/business/admin/forms/QaFormDialog.vue'
import QuizFormDialog from '@/components/business/admin/forms/QuizFormDialog.vue'
import ResourceFormDialog from '@/components/business/admin/forms/ResourceFormDialog.vue'
import { useAdminData, useAdminDialogs, useAdminActions } from '@/composables/useAdminData'
import { getCorrectAnswerContent, menuTitles, formatFileSize, getFileTypeTagType, getFileTypeText } from '@/utils/adminUtils'
import { parseMediaList, getMediaType } from '@/utils/media'

const router = useRouter()
const request = inject('request')
const updateUserState = inject('updateUserState')

const userName = computed(() => localStorage.getItem('userName') || '管理员')
const activeMenu = ref('dashboard')
const logoutLoading = ref(false)

const {
  users, knowledgeList, inheritorsList, plantsList, qaList,
  resourcesList, feedbackList, quizList, commentsList, logList,
  sortedComments, sortedFeedback, sortedUsers, fetchData
} = useAdminData(request)

const {
  dialogVisible, detailVisible, currentDetail, formData,
  commentDetailVisible, currentComment, feedbackDetailVisible, currentFeedback,
  logDetailVisible, currentLog, openDialog, viewDetail, editItem
} = useAdminDialogs()

const { selectedLogs, confirmDelete, approveComment: doApproveComment, rejectComment: doRejectComment,
  handleLogSelectionChange, batchDeleteLogs, clearAllLogs, replyFeedback
} = useAdminActions(request, fetchData)

const getLogModuleTagType = (module) => ({ USER: 'primary', PLANT: 'success', KNOWLEDGE: 'warning', INHERITOR: 'info', RESOURCE: 'danger', QA: '', FEEDBACK: 'warning', COMMENT: 'info', FAVORITE: 'success', SYSTEM: 'danger' }[module] || '')
const getLogTypeTagType = (type) => ({ CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', QUERY: '' }[type] || '')
const formatLogTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : '无'

const viewUser = (row) => viewDetail('user', row)
const deleteUser = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/users/${id}`))

const viewKnowledge = (row) => viewDetail('knowledge', row)
const editKnowledge = (row) => editItem('knowledge', row)
const saveKnowledge = async (data) => {
  const success = await (data.id ? request.put(`/admin/knowledge/${data.id}`, data) : request.post('/admin/knowledge', data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); dialogVisible.value.knowledge = false; fetchData() }
  else ElMessage.error('保存失败')
}
const deleteKnowledge = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/knowledge/${id}`))

const viewInheritor = (row) => viewDetail('inheritor', row)
const editInheritor = (row) => editItem('inheritor', row, { experienceYears: row.experienceYears || 0 })
const saveInheritor = async (data) => {
  const success = await (data.id ? request.put(`/admin/inheritors/${data.id}`, data) : request.post('/admin/inheritors', data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); dialogVisible.value.inheritor = false; fetchData() }
  else ElMessage.error('保存失败')
}
const deleteInheritor = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/inheritors/${id}`))

const viewPlant = (row) => viewDetail('plant', row)
const editPlant = (row) => editItem('plant', row)
const savePlant = async (data) => {
  const success = await (data.id ? request.put(`/admin/plants/${data.id}`, data) : request.post('/admin/plants', data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); dialogVisible.value.plant = false; fetchData() }
  else ElMessage.error('保存失败')
}
const deletePlant = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/plants/${id}`))

const viewQa = (row) => viewDetail('qa', row)
const editQa = (row) => editItem('qa', row)
const saveQa = async (data) => {
  const success = await (data.id ? request.put(`/admin/qa/${data.id}`, data) : request.post('/admin/qa', data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); dialogVisible.value.qa = false; fetchData() }
  else ElMessage.error('保存失败')
}
const deleteQa = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/qa/${id}`))

const viewResource = (row) => viewDetail('resource', row)
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
const editResource = (row) => editItem('resource', row, { fileSize: row.fileSize || 0 })
const saveResource = async (data) => {
  const success = await (data.id ? request.put(`/admin/resources/${data.id}`, data) : request.post('/admin/resources', data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); dialogVisible.value.resource = false; fetchData() }
  else ElMessage.error('保存失败')
}
const deleteResource = (id) => confirmDelete('确定删除？', () => request.delete(`/admin/resources/${id}`))

const viewQuiz = (row) => viewDetail('quiz', row)
const editQuiz = (row) => { formData.value.quiz = row; dialogVisible.value.quiz = true }
const saveQuiz = async (data) => {
  const success = await (data.id ? request.put('/quiz/update', data) : request.post('/quiz/add', data)).then(() => true).catch(() => false)
  if (success) { ElMessage.success('保存成功'); dialogVisible.value.quiz = false; fetchData() }
  else ElMessage.error('保存失败')
}
const deleteQuiz = (id) => confirmDelete('确定删除？', () => request.delete(`/quiz/${id}`))

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
  finally {
    localStorage.removeItem('token'); localStorage.removeItem('userName'); localStorage.removeItem('userId'); localStorage.removeItem('role'); localStorage.removeItem('userInfo')
    updateUserState(); ElMessage.success('已退出登录'); router.push('/')
  }
}

onMounted(() => {
  const role = localStorage.getItem('role')
  if (role !== 'admin' && role !== 'ADMIN') { ElMessage.error('您没有管理员权限'); router.push('/'); return }
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
.logs-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
:deep(.el-descriptions__body .el-descriptions__table .el-descriptions__cell) { word-break: break-all; }
</style>
