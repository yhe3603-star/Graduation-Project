import { Aim, ArrowRight, ChatDotRound, Compass, DataLine, Document, Folder, InfoFilled, Medal, Picture, User } from '@element-plus/icons-vue'

export const quickEntries = [
  { title: '知识库', desc: '理论知识体系', path: '/knowledge', color: 'linear-gradient(135deg, #1A5276, var(--dong-indigo-dark))', icon: Document },
  { title: '传承人', desc: '非遗传承档案', path: '/inheritors', color: 'linear-gradient(135deg, #28B463, var(--dong-jade-dark))', icon: User },
  { title: '药用图鉴', desc: '道地药材详解', path: '/plants', color: 'linear-gradient(135deg, var(--dong-gold-light), #e8930c)', icon: Picture },
  { title: '问答社区', desc: '互动答疑解惑', path: '/qa', color: 'linear-gradient(135deg, #667eea, #764ba2)', icon: ChatDotRound }
]

export const coreModules = [
  { title: '知识库', desc: '系统整理侗医药理论知识，包含病因病机、诊断方法、治疗原则等内容', path: '/knowledge', count: '50+ 条目', bgColor: 'linear-gradient(135deg, #1A5276, var(--dong-indigo-dark))', icon: Document },
  { title: '传承人', desc: '记录各级非遗传承人档案，展示传承谱系与技艺特色', path: '/inheritors', count: '10+ 位', bgColor: 'linear-gradient(135deg, #28B463, var(--dong-jade-dark))', icon: User },
  { title: '药用图鉴', desc: '收录黔东南道地药材，配以精美图片与详细功效说明', path: '/plants', count: '50+ 种', bgColor: 'linear-gradient(135deg, var(--dong-gold-light), #e8930c)', icon: Picture },
  { title: '问答社区', desc: '互动问答平台，解答侗医药相关问题', path: '/qa', count: '60+ 问题', bgColor: 'linear-gradient(135deg, #667eea, #764ba2)', icon: ChatDotRound }
]

export const extendModules = [
  { title: '文化互动', desc: '趣味答题、植物识别游戏，寓教于乐', path: '/interact', icon: Aim },
  { title: '学习资源', desc: '视频教程、文档资料，深入学习', path: '/resources', icon: Folder },
  { title: '数据可视化', desc: '统计分析，直观展示平台数据', path: '/visual', icon: DataLine }
]

export const levelClassMap = {
  '省级': 'level-gold',
  '自治区级': 'level-green',
  '州级': 'level-purple'
}

export const getLevelClass = (level) => levelClassMap[level] || 'level-blue'

export const createHeroStats = (stats) => [
  { icon: Picture, value: stats.plants, label: '种药用植物' },
  { icon: Document, value: stats.formulas, label: '项经典药方' },
  { icon: User, value: stats.inheritors, label: '名传承人' },
  { icon: Medal, value: stats.therapies, label: '类核心疗法' }
]
