# 侗医药知识平台前端项目结构说明

## 目录结构概览

```
dong-medicine-frontend/
├── public/                     # 静态资源目录
│   └── static/
│       └── defaults/           # 默认图片资源
├── src/                        # 源代码目录
│   ├── assets/                 # 需要构建的静态资源
│   ├── components/             # 组件目录
│   │   ├── base/               # 基础组件（可复用的通用组件）
│   │   │   ├── Pagination.vue  # 分页组件
│   │   │   └── index.js        # 基础组件导出
│   │   └── business/           # 业务组件（业务相关组件）
│   │       ├── display/        # 展示组件
│   │       ├── interact/       # 交互组件
│   │       ├── media/          # 媒体组件
│   │       ├── upload/         # 上传组件
│   │       └── index.js        # 业务组件导出
│   ├── composables/            # 组合式函数（Composables）
│   │   ├── useAdminData.js     # 管理后台数据
│   │   ├── usePersonalCenter.js# 个人中心功能
│   │   ├── useInteraction.js   # 交互功能
│   │   ├── useMedia.js         # 媒体功能
│   │   ├── useQuiz.js          # 趣味答题
│   │   ├── usePlantGame.js     # 植物识别游戏
│   │   ├── useFavorite.js      # 收藏功能
│   │   ├── useFormDialog.js    # 表单对话框
│   │   ├── useUpdateLog.js     # 更新日志
│   │   └── index.js            # Composables导出
│   ├── router/                 # 路由配置
│   │   └── index.js            # 路由定义
│   ├── stores/                 # Pinia状态管理
│   │   ├── user.js             # 用户状态
│   │   └── index.js            # Store导出
│   ├── styles/                 # 样式文件
│   │   ├── variables.css       # CSS变量定义
│   │   ├── base.css            # 基础样式
│   │   ├── components.css      # 组件样式
│   │   ├── pages.css           # 页面样式
│   │   ├── media-common.css    # 媒体通用样式
│   │   ├── common.css          # 通用样式
│   │   └── index.css           # 样式入口
│   ├── utils/                  # 工具函数
│   │   ├── request.js          # HTTP请求封装
│   │   ├── media.js            # 媒体处理工具
│   │   ├── logger.js           # 日志工具
│   │   └── index.js            # 工具函数导出
│   ├── views/                  # 页面组件
│   │   ├── Home.vue            # 首页
│   │   ├── Admin.vue           # 管理后台
│   │   ├── PersonalCenter.vue  # 个人中心
│   │   ├── Knowledge.vue       # 知识库
│   │   ├── Plants.vue          # 药材库
│   │   ├── Inheritors.vue      # 传承人
│   │   ├── Resources.vue       # 资源中心
│   │   ├── Interact.vue        # 互动中心
│   │   ├── Qa.vue              # 问答系统
│   │   ├── About.vue           # 关于我们
│   │   ├── Feedback.vue        # 意见反馈
│   │   ├── Visual.vue          # 数据可视化
│   │   └── GlobalSearch.vue    # 全局搜索
│   ├── App.vue                 # 根组件
│   └── main.js                 # 应用入口
├── tests/                      # 测试文件
├── index.html                  # HTML入口
├── package.json                # 项目配置
├── vite.config.js              # Vite配置
└── README.md                   # 项目说明
```

## 组件层级结构

### 基础组件 (base/)
基础组件是可复用的通用组件，不包含业务逻辑，可在多个项目中复用。

| 组件名 | 功能描述 | Props |
|--------|----------|-------|
| Pagination | 分页组件 | page, size, total, sizes |

### 业务组件 (business/)
业务组件包含特定业务逻辑，按功能模块分类：

#### 展示组件 (display/)
| 组件名 | 功能描述 |
|--------|----------|
| MediaDisplay | 媒体展示组件 |
| CardGrid | 卡片网格组件 |
| ChartCard | 图表卡片组件 |
| SearchFilter | 搜索过滤组件 |
| PageSidebar | 页面侧边栏 |
| AiChatCard | AI对话卡片 |
| UpdateLogCard | 更新日志卡片 |

#### 交互组件 (interact/)
| 组件名 | 功能描述 |
|--------|----------|
| CommentSection | 评论组件 |
| PlantGame | 植物识别游戏 |
| QuizSection | 趣味答题组件 |

#### 媒体组件 (media/)
| 组件名 | 功能描述 |
|--------|----------|
| ImageCarousel | 图片轮播组件 |
| VideoPlayer | 视频播放组件 |
| DocumentPreview | 文档预览组件 |
| DocumentList | 文档列表组件 |

#### 上传组件 (upload/)
| 组件名 | 功能描述 |
|--------|----------|
| ImageUploader | 图片上传组件 |
| VideoUploader | 视频上传组件 |
| DocumentUploader | 文档上传组件 |
| FileUploader | 通用文件上传组件 |

## 样式层级结构

### 样式文件说明

| 文件名 | 层级 | 功能描述 |
|--------|------|----------|
| variables.css | 1 | CSS变量定义（颜色、字体、间距、阴影等） |
| base.css | 2 | 基础样式（重置、排版、通用布局） |
| components.css | 3 | 组件样式（卡片、按钮、表单等） |
| pages.css | 4 | 页面样式（各页面特定样式） |
| media-common.css | 3 | 媒体相关通用样式 |
| common.css | 2 | 原有通用样式（兼容） |
| index.css | 入口 | 统一导入所有样式 |

### CSS变量命名规范

```css
/* 颜色变量 */
--dong-blue: #1A5276;      /* 主色调 */
--dong-green: #28B463;     /* 强调色 */
--dong-light: #f8f5f0;     /* 背景色 */
--dong-gold: #c9a227;      /* 金色 */
--dong-silver: #c0c0c0;    /* 银色 */

/* 文本颜色 */
--text-primary: #1a1a1a;   /* 主要文本 */
--text-secondary: #666;    /* 次要文本 */
--text-muted: #888;        /* 弱化文本 */

/* 阴影 */
--shadow-sm: 0 2px 12px rgba(0, 0, 0, 0.06);
--shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
--shadow-lg: 0 8px 24px rgba(26, 82, 118, 0.12);

/* 圆角 */
--radius-sm: 8px;
--radius-md: 12px;
--radius-lg: 16px;

/* 间距 */
--gap-sm: 12px;
--gap-md: 16px;
--gap-lg: 24px;

/* 过渡 */
--transition-fast: 0.2s ease;
--transition-normal: 0.3s ease;
--transition-slow: 0.4s ease;
```

### BEM命名规范

组件样式采用BEM命名规范：

```css
/* Block（块） */
.card { }

/* Element（元素） */
.card__header { }
.card__title { }
.card__content { }

/* Modifier（修饰符） */
.card--featured { }
.card--compact { }
```

## 文件注释规范

### JavaScript/Vue文件注释模板

```javascript
/**
 * @file 文件名称
 * @description 文件功能描述
 * @author 作者名称
 * @version 版本号
 * 
 * @requires 依赖模块
 * 
 * 功能说明：
 * 1. 功能点一
 * 2. 功能点二
 * 
 * @example
 * 使用示例
 */
```

### CSS文件注释模板

```css
/**
 * @file 样式文件名称
 * @description 样式功能描述
 * @author 作者名称
 * @version 版本号
 * @requires 依赖的样式文件
 */
```

## Composables使用指南

### 分类说明

| 类别 | Composables | 功能描述 |
|------|-------------|----------|
| 核心功能 | useAdminData | 管理后台数据获取与操作 |
| 核心功能 | usePersonalCenter | 个人中心功能 |
| 交互功能 | useInteraction | 交互相关（倒计时、评论、分页） |
| 交互功能 | useQuiz | 趣味答题功能 |
| 交互功能 | usePlantGame | 植物识别游戏功能 |
| 媒体功能 | useMedia | 媒体相关（文档预览、媒体显示） |
| 通用功能 | useFavorite | 收藏功能 |
| 通用功能 | useFormDialog | 表单对话框 |
| 通用功能 | useUpdateLog | 更新日志 |

### 使用示例

```javascript
import { useInteraction } from '@/composables'

const { 
  countdown, 
  comments, 
  pagination, 
  filter 
} = useInteraction()
```

## 工具函数使用指南

### 数据处理

```javascript
import { formatTime, extractData, truncate } from '@/utils'

// 时间格式化
formatTime(new Date()) // "刚刚"
formatTime(date, { format: 'date' }) // "2024/1/1"

// 响应数据提取
extractData(response) // 返回数组数据

// 文本截断
truncate(longText, 100) // 截断到100字符
```

### 媒体处理

```javascript
import { 
  parseMediaList, 
  getMediaType, 
  downloadMedia 
} from '@/utils'

// 解析媒体列表
const files = parseMediaList(mediaData)

// 获取媒体类型
getMediaType('video.mp4') // 'video'

// 下载媒体
downloadMedia({ url: '/path/to/file', name: 'file.pdf' })
```

## 重构前后对比

### 文件组织对比

| 方面 | 重构前 | 重构后 |
|------|--------|--------|
| 组件分类 | 单一目录 | 基础/业务分层 |
| 样式管理 | 分散在各组件 | 统一styles目录 |
| Composables | 无统一导出 | 分类导出 |
| 工具函数 | 分散定义 | 统一导出 |
| 注释规范 | 不统一 | JSDoc标准 |

### 代码质量提升

1. **可维护性**：清晰的文件组织，便于定位和修改
2. **可扩展性**：模块化设计，易于添加新功能
3. **可读性**：标准化注释，降低理解成本
4. **复用性**：基础组件和工具函数可在多处复用

## 开发规范

### 命名规范

- **文件命名**：PascalCase（组件）、camelCase（工具函数）
- **组件命名**：PascalCase，反映功能用途
- **变量命名**：camelCase
- **常量命名**：UPPER_SNAKE_CASE
- **CSS类名**：BEM规范

### 导入规范

```javascript
// 优先使用别名导入
import { Pagination } from '@/components/base'
import { useInteraction } from '@/composables'
import { formatTime } from '@/utils'
```

### 组件开发规范

1. 单一职责：每个组件只负责一个功能
2. Props验证：使用TypeScript或PropTypes
3. 事件命名：使用kebab-case
4. 样式隔离：使用scoped样式
