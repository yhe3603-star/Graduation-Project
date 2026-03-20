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
    title: '用户管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'username', label: '用户名', minWidth: 120 },
      { prop: 'email', label: '邮箱', minWidth: 180 },
      { prop: 'role', label: '角色', width: 100 },
      { prop: 'createdAt', label: '注册时间', width: 160 }
    ]
  },
  knowledge: {
    title: '知识管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'title', label: '标题', minWidth: 150 },
      { prop: 'type', label: '类型', width: 100 },
      { prop: 'popularity', label: '热度', width: 80 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ]
  },
  inheritors: {
    title: '传承人管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '姓名', minWidth: 120 },
      { prop: 'level', label: '级别', width: 100 },
      { prop: 'experienceYears', label: '经验年限', width: 100 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ]
  },
  plants: {
    title: '植物管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'nameCn', label: '中文名', minWidth: 120 },
      { prop: 'nameDong', label: '侗语名', minWidth: 120 },
      { prop: 'category', label: '分类', width: 100 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ]
  },
  qa: {
    title: '问答管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'question', label: '问题', minWidth: 200 },
      { prop: 'category', label: '分类', width: 100 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ]
  },
  resources: {
    title: '资源管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'title', label: '标题', minWidth: 150 },
      { prop: 'category', label: '分类', width: 100 },
      { prop: 'fileType', label: '类型', width: 80 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ]
  },
  quiz: {
    title: '答题管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'question', label: '题目', minWidth: 200 },
      { prop: 'difficulty', label: '难度', width: 80 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ]
  },
  feedback: {
    title: '反馈管理',
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'title', label: '标题', minWidth: 150 },
      { prop: 'type', label: '类型', width: 100 },
      { prop: 'status', label: '状态', width: 100 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ]
  }
}
