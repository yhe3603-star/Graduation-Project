# 侗乡医药数字展示平台前端

基于 Vue 3 + Vite 的侗族医药文化遗产数字化展示平台前端应用

## 环境要求

| 环境 | 版本 |
|-----|------|
| Node.js | 18+ |
| npm | 9+ |

## 本地启动

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

默认开发地址：http://localhost:5173

## 常用命令

```bash
# 开发模式
npm run dev

# 代码检查
npm run lint

# 运行测试
npm run test:run

# 构建生产版本
npm run build
```

## 目录结构

```
dong-medicine-frontend/
├── src/
│   ├── views/              # 页面组件
│   │   ├── Home.vue        # 首页
│   │   ├── Plants.vue      # 药用植物
│   │   ├── Inheritors.vue  # 传承人
│   │   ├── Knowledge.vue   # 知识库
│   │   ├── Qa.vue          # 问答
│   │   ├── Resources.vue   # 资源中心
│   │   ├── Interact.vue    # 互动
│   │   ├── Admin.vue       # 后台管理
│   │   └── ...
│   ├── components/         # 组件
│   │   ├── base/           # 基础组件
│   │   ├── business/       # 业务组件
│   │   │   ├── admin/      # 管理后台组件
│   │   │   ├── dialogs/    # 对话框组件
│   │   │   ├── display/    # 展示组件
│   │   │   ├── interact/   # 交互组件
│   │   │   ├── layout/     # 布局组件
│   │   │   ├── media/      # 媒体组件
│   │   │   └── upload/     # 上传组件
│   ├── composables/        # 组合式函数
│   ├── router/             # 路由配置
│   ├── stores/             # Pinia 状态管理
│   ├── utils/              # 工具函数
│   ├── styles/             # 样式文件
│   └── config/             # 配置文件
├── public/                 # 静态资源
└── index.html              # 入口 HTML
```

## 主要页面

| 页面 | 路由 | 说明 |
|-----|------|------|
| 首页 | `/` | 平台首页、数据概览 |
| 药用植物 | `/plants` | 侗族药用植物展示 |
| 传承人 | `/inheritors` | 非遗传承人介绍 |
| 知识库 | `/knowledge` | 文献资料查阅 |
| 问答 | `/qa` | AI 智能问答 |
| 资源中心 | `/resources` | 多媒体资源 |
| 互动 | `/interact` | 草药识别游戏 |
| 后台管理 | `/admin` | 内容管理系统 |
| 个人中心 | `/personal` | 用户个人中心 |

## 技术栈

| 技术 | 说明 |
|-----|------|
| Vue 3 | 渐进式 JavaScript 框架 |
| Vite | 下一代前端构建工具 |
| Vue Router | 官方路由管理器 |
| Pinia | Vue 状态管理 |
| Element Plus | Vue 3 UI 组件库 |
| Axios | HTTP 客户端 |
| ECharts | 数据可视化 |

## 构建产物

- `dist/`：生产环境静态资源
