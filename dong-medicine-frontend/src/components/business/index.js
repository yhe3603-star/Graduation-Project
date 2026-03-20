/**
 * @file 业务组件导出
 * @description 统一导出所有业务组件，便于引用
 * @author Dong Medicine Team
 * @version 1.0.0
 * 
 * 业务组件分类：
 * 
 * 1. 上传组件 (upload/)
 * - ImageUploader: 图片上传组件
 * - VideoUploader: 视频上传组件
 * - DocumentUploader: 文档上传组件
 * - FileUploader: 通用文件上传组件
 * 
 * 2. 媒体组件 (media/)
 * - ImageCarousel: 图片轮播组件
 * - VideoPlayer: 视频播放组件
 * - DocumentPreview: 文档预览组件
 * - DocumentList: 文档列表组件
 * - MediaDisplay: 媒体展示组件
 * 
 * 3. 交互组件 (interact/)
 * - CommentSection: 评论组件
 * - PlantGame: 植物识别游戏
 * - QuizSection: 趣味答题组件
 * - InteractSidebar: 互动侧边栏
 * 
 * 4. 展示组件 (display/)
 * - AiChatCard: AI对话卡片
 * - UpdateLogCard: 更新日志卡片
 * - UpdateLogDialog: 更新日志对话框
 * - MediaDisplay: 媒体展示组件
 * - CardGrid: 卡片网格组件
 * - ChartCard: 图表卡片组件
 * - SearchFilter: 搜索过滤组件
 * - PageSidebar: 页面侧边栏
 * - Pagination: 分页组件
 * 
 * 5. 布局组件 (layout/)
 * - AppHeader: 应用头部
 * - AppFooter: 应用底部
 * 
 * 6. 管理后台组件 (admin/)
 * - AdminDashboard: 管理仪表盘
 * - AdminDataTable: 数据表格
 * - AdminSidebar: 管理侧边栏
 * 
 * 7. 对话框组件 (dialogs/)
 * - InheritorDetailDialog: 传承人详情对话框
 * - KnowledgeDetailDialog: 知识详情对话框
 * - PlantDetailDialog: 药材详情对话框
 * - QuizDetailDialog: 答题详情对话框
 * - ResourceDetailDialog: 资源详情对话框
 */

export { default as ImageUploader } from './upload/ImageUploader.vue'
export { default as VideoUploader } from './upload/VideoUploader.vue'
export { default as DocumentUploader } from './upload/DocumentUploader.vue'
export { default as FileUploader } from './upload/FileUploader.vue'

export { default as ImageCarousel } from './media/ImageCarousel.vue'
export { default as VideoPlayer } from './media/VideoPlayer.vue'
export { default as DocumentPreview } from './media/DocumentPreview.vue'
export { default as DocumentList } from './media/DocumentList.vue'
export { default as MediaDisplay } from './media/MediaDisplay.vue'

export { default as CommentSection } from './interact/CommentSection.vue'
export { default as PlantGame } from './interact/PlantGame.vue'
export { default as QuizSection } from './interact/QuizSection.vue'
export { default as InteractSidebar } from './interact/InteractSidebar.vue'

export { default as AiChatCard } from './display/AiChatCard.vue'
export { default as UpdateLogCard } from './display/UpdateLogCard.vue'
export { default as UpdateLogDialog } from './display/UpdateLogDialog.vue'
export { default as CardGrid } from './display/CardGrid.vue'
export { default as ChartCard } from './display/ChartCard.vue'
export { default as SearchFilter } from './display/SearchFilter.vue'
export { default as PageSidebar } from './display/PageSidebar.vue'
export { default as Pagination } from './display/Pagination.vue'

export { default as AppHeader } from './layout/AppHeader.vue'
export { default as AppFooter } from './layout/AppFooter.vue'

export { default as AdminDashboard } from './admin/AdminDashboard.vue'
export { default as AdminDataTable } from './admin/AdminDataTable.vue'
export { default as AdminSidebar } from './admin/AdminSidebar.vue'

export { default as InheritorDetailDialog } from './dialogs/InheritorDetailDialog.vue'
export { default as KnowledgeDetailDialog } from './dialogs/KnowledgeDetailDialog.vue'
export { default as PlantDetailDialog } from './dialogs/PlantDetailDialog.vue'
export { default as QuizDetailDialog } from './dialogs/QuizDetailDialog.vue'
export { default as ResourceDetailDialog } from './dialogs/ResourceDetailDialog.vue'

export { default as CommentDetailDialog } from './admin/dialogs/CommentDetailDialog.vue'
export { default as FeedbackDetailDialog } from './admin/dialogs/FeedbackDetailDialog.vue'
export { default as LogDetailDialog } from './admin/dialogs/LogDetailDialog.vue'
export { default as UserDetailDialog } from './admin/dialogs/UserDetailDialog.vue'
export { default as QaDetailDialog } from './admin/dialogs/QaDetailDialog.vue'

export { default as InheritorFormDialog } from './admin/forms/InheritorFormDialog.vue'
export { default as KnowledgeFormDialog } from './admin/forms/KnowledgeFormDialog.vue'
export { default as PlantFormDialog } from './admin/forms/PlantFormDialog.vue'
export { default as QaFormDialog } from './admin/forms/QaFormDialog.vue'
export { default as QuizFormDialog } from './admin/forms/QuizFormDialog.vue'
export { default as ResourceFormDialog } from './admin/forms/ResourceFormDialog.vue'
