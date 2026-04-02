# 业务组件目录 (business)

本目录存放包含业务逻辑的组件，是项目特有的组件。

## 目录

- [什么是业务组件？](#什么是业务组件)
- [目录结构](#目录结构)
- [子目录说明](#子目录说明)

---

## 什么是业务组件？

**业务组件**是包含具体业务逻辑的组件，它们：
- 与项目业务相关
- 可能调用API
- 可能使用状态管理
- 可能包含复杂的业务规则

```
┌─────────────────────────────────────────────────────────────────┐
│                     业务组件分类                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  layout/     - 布局组件（头部、底部、侧边栏）                     │
│  display/    - 展示组件（卡片、图表、分页）                       │
│  interact/   - 交互组件（评论、答题、游戏）                       │
│  media/      - 媒体组件（图片、视频、文档）                       │
│  upload/     - 上传组件（图片上传、文件上传）                     │
│  dialogs/    - 详情对话框（各种详情弹窗）                         │
│  admin/      - 管理后台组件（仪表盘、数据表格）                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 目录结构

```
business/
│
├── layout/                             # 布局组件
│   ├── AppHeader.vue                   # 应用头部
│   └── AppFooter.vue                   # 应用底部
│
├── display/                            # 展示组件
│   ├── AiChatCard.vue                  # AI对话卡片
│   ├── CardGrid.vue                    # 卡片网格
│   ├── ChartCard.vue                   # 图表卡片
│   ├── PageSidebar.vue                 # 页面侧边栏
│   ├── Pagination.vue                  # 分页组件
│   ├── SearchFilter.vue                # 搜索过滤
│   └── index.js
│
├── interact/                           # 交互组件
│   ├── CaptchaInput.vue                # 验证码输入
│   ├── CommentSection.vue              # 评论组件
│   ├── InteractSidebar.vue             # 互动侧边栏
│   ├── PlantGame.vue                   # 植物识别游戏
│   ├── QuizSection.vue                 # 趣味答题
│   └── index.js
│
├── media/                              # 媒体组件
│   ├── DocumentList.vue                # 文档列表
│   ├── DocumentPreview.vue             # 文档预览
│   ├── ImageCarousel.vue               # 图片轮播
│   ├── MediaDisplay.vue                # 媒体展示
│   ├── VideoPlayer.vue                 # 视频播放
│   └── index.js
│
├── upload/                             # 上传组件
│   ├── DocumentUploader.vue            # 文档上传
│   ├── FileUploader.vue                # 通用文件上传
│   ├── ImageUploader.vue               # 图片上传
│   ├── VideoUploader.vue               # 视频上传
│   └── index.js
│
├── dialogs/                            # 详情对话框
│   ├── InheritorDetailDialog.vue
│   ├── KnowledgeDetailDialog.vue
│   ├── PlantDetailDialog.vue
│   ├── QuizDetailDialog.vue
│   └── ResourceDetailDialog.vue
│
└── admin/                              # 管理后台组件
    ├── dialogs/                        # 管理对话框
    ├── forms/                          # 管理表单
    ├── AdminDashboard.vue              # 管理仪表盘
    ├── AdminDataTable.vue              # 数据表格
    └── AdminSidebar.vue                # 管理侧边栏
```

---

## 子目录说明

| 目录 | 说明 | 典型组件 |
|------|------|----------|
| layout/ | 页面布局相关组件 | AppHeader, AppFooter |
| display/ | 数据展示相关组件 | CardGrid, ChartCard, Pagination |
| interact/ | 用户交互相关组件 | CommentSection, QuizSection, PlantGame |
| media/ | 媒体文件相关组件 | ImageCarousel, VideoPlayer, DocumentPreview |
| upload/ | 文件上传相关组件 | ImageUploader, VideoUploader, DocumentUploader |
| dialogs/ | 详情弹窗组件 | PlantDetailDialog, KnowledgeDetailDialog |
| admin/ | 管理后台专用组件 | AdminDashboard, AdminDataTable |

---

## 使用示例

### 导入业务组件

```javascript
// 从子目录导入
import AppHeader from '@/components/business/layout/AppHeader.vue'
import CardGrid from '@/components/business/display/CardGrid.vue'
import CommentSection from '@/components/business/interact/CommentSection.vue'

// 或从index.js导入
import { CardGrid, Pagination, SearchFilter } from '@/components/business/display'
```

---

**最后更新时间**：2026年4月3日
