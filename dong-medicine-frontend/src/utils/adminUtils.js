import { formatTime as baseFormatTime, formatFileSize as baseFormatFileSize } from './index'

export const formatTime = baseFormatTime
export const formatFileSize = baseFormatFileSize

const STATUS_MAPS = {
  feedback: {
    pending: { tag: "warning", text: "待处理" },
    processing: { tag: "primary", text: "处理中" },
    resolved: { tag: "success", text: "已解决" }
  },
  difficulty: {
    easy: { tag: "success", text: "入门" },
    medium: { tag: "warning", text: "进阶" },
    advanced: { tag: "warning", text: "进阶" },
    hard: { tag: "danger", text: "专业" },
    beginner: { tag: "success", text: "入门" },
    intermediate: { tag: "warning", text: "进阶" },
    professional: { tag: "danger", text: "专业" }
  },
  fileType: {
    video: { tag: "primary", text: "视频" },
    document: { tag: "success", text: "文档" },
    image: { tag: "warning", text: "图片" },
    audio: { tag: "info", text: "音频" },
    other: { tag: "info", text: "其他" }
  }
}

const LEVEL_TAG_MAP = { "省级": "warning", "自治区级": "success", "州级": "primary", "市级": "primary" }

export const createTagGetter = (mapOrName, fieldOrDefault = 'tag') => {
  if (typeof mapOrName === 'string') {
    const mapName = mapOrName
    const field = fieldOrDefault
    return (val) => STATUS_MAPS[mapName]?.[val]?.[field] ?? (field === 'tag' ? "info" : val)
  }
  const map = mapOrName && typeof mapOrName === 'object' ? mapOrName : {}
  const defaultValue = fieldOrDefault
  return (val) => map[val] ?? defaultValue
}

export const getFeedbackStatusTag = createTagGetter('feedback', 'tag')
export const getFeedbackStatusText = createTagGetter('feedback', 'text')
export const getDifficultyTagType = createTagGetter('difficulty', 'tag')
export const getDifficultyText = createTagGetter('difficulty', 'text')
export const getFileTypeTagType = createTagGetter('fileType', 'tag')
export const getFileTypeText = createTagGetter('fileType', 'text')
export const getLevelTagType = (val) => LEVEL_TAG_MAP[val] ?? "info"

export const getCorrectAnswerContent = (row) => {
  const options = row.options || row.optionList || []
  const answer = row.correctAnswer || row.answer || 'A'
  const index = answer.charCodeAt(0) - 65
  return options[index] || answer
}

export const menuTitles = {
  dashboard: "数据概览",
  users: "用户管理",
  knowledge: "知识管理",
  inheritors: "传承人管理",
  plants: "植物管理",
  qa: "问答管理",
  resources: "资源管理",
  quiz: "答题管理",
  comments: "评论管理",
  feedback: "反馈管理",
  logs: "日志管理"
}

export const TABLE_CONFIGS = {
  users: {
    title: '用户',
    showTitle: false,
    columns: [
      { prop: 'username', label: '用户名', minWidth: 120 },
      { prop: 'role', label: '角色', type: 'tag' },
      { prop: 'status', label: '状态', slotName: 'status', width: 80 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ],
    showAdd: false,
    showEdit: false,
    actionWidth: 250
  },
  knowledge: {
    title: '知识',
    showTitle: true,
    titleName: '标题',
    columns: [
      { prop: 'type', label: '类型', width: 80 },
      { prop: 'therapyCategory', label: '疗法分类', width: 100 },
      { prop: 'diseaseCategory', label: '疾病分类', width: 100 },
      { prop: 'popularity', label: '热度', width: 70 }
    ]
  },
  inheritors: {
    title: '传承人',
    showTitle: true,
    titleName: '姓名',
    columns: [
      { prop: 'level', label: '级别', type: 'tag', width: 80 },
      { prop: 'specialties', label: '技艺特色', minWidth: 150 },
      { prop: 'experienceYears', label: '经验年限', width: 90 }
    ]
  },
  plants: {
    title: '植物',
    showTitle: true,
    titleName: '名称',
    columns: [
      { prop: 'scientificName', label: '学名', minWidth: 120 },
      { prop: 'category', label: '分类', width: 80 },
      { prop: 'usageWay', label: '用法', width: 70 }
    ]
  },
  qa: {
    title: '问答',
    showTitle: true,
    titleName: '问题',
    columns: [
      { prop: 'category', label: '分类', width: 100 },
      { prop: 'popularity', label: '热度', width: 70 }
    ]
  },
  resources: {
    title: '资源',
    showTitle: true,
    titleName: '标题',
    columns: [
      { prop: 'category', label: '分类', width: 100 },
      { slotName: 'fileType', label: '类型', width: 70 },
      { slotName: 'fileSize', label: '文件大小', width: 90 },
      { prop: 'downloadCount', label: '下载次数', width: 90 },
      { prop: 'popularity', label: '热度', width: 70 }
    ]
  },
  quiz: {
    title: '题目',
    showTitle: true,
    titleName: '问题',
    columns: [
      { prop: 'category', label: '分类', width: 100 },
      { slotName: 'correctAnswer', label: '正确答案', width: 150 }
    ]
  },
  comments: {
    title: '评论',
    showTitle: true,
    titleName: '评论内容',
    columns: [
      { prop: 'username', label: '用户', width: 100 },
      { prop: 'targetType', label: '类型', width: 80 },
      { prop: 'status', label: '状态', type: 'tag', width: 80 }
    ],
    showAdd: false,
    showEdit: false,
    actionWidth: 250
  },
  feedback: {
    title: '反馈',
    showTitle: true,
    titleName: '标题',
    columns: [
      { prop: 'userName', label: '用户', width: 100 },
      { prop: 'type', label: '类型', width: 80 },
      { prop: 'contact', label: '联系方式', width: 120 },
      { prop: 'status', label: '状态', type: 'tag', width: 80 }
    ],
    showAdd: false,
    showEdit: false,
    actionWidth: 200
  }
}

export const CRUD_ENDPOINTS = {
  knowledge: { create: '/admin/knowledge', update: '/admin/knowledge', delete: '/admin/knowledge' },
  inheritor: { create: '/admin/inheritors', update: '/admin/inheritors', delete: '/admin/inheritors' },
  plant: { create: '/admin/plants', update: '/admin/plants', delete: '/admin/plants' },
  qa: { create: '/admin/qa', update: '/admin/qa', delete: '/admin/qa' },
  resource: { create: '/admin/resources', update: '/admin/resources', delete: '/admin/resources' },
  quiz: { create: '/quiz/add', update: '/quiz/update', delete: '/quiz' }
}

export const LOG_MODULE_TAG_MAP = {
  USER: 'primary', PLANT: 'success', KNOWLEDGE: 'warning', INHERITOR: 'info',
  RESOURCE: 'danger', QA: '', FEEDBACK: 'warning', COMMENT: 'info',
  FAVORITE: 'success', SYSTEM: 'danger'
}

export const LOG_TYPE_TAG_MAP = {
  CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', QUERY: ''
}

export const getLogModuleTagType = (module) => LOG_MODULE_TAG_MAP[module] || ''
export const getLogTypeTagType = (type) => LOG_TYPE_TAG_MAP[type] || ''
export const formatLogTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : '无'
