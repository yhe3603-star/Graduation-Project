# 前端源码目录 (src)

本目录包含侗乡医药数字展示平台的所有前端源代码。

## 📁 目录结构

```
src/
├── assets/           # 静态资源文件
├── components/       # 可复用的Vue组件
├── composables/      # 组合式函数 (Composables)
├── config/           # 配置文件
├── directives/       # 自定义指令
├── router/           # 路由配置
├── stores/           # 状态管理 (Pinia)
├── styles/           # 全局样式文件
├── utils/            # 工具函数
├── views/            # 页面视图组件
├── App.vue           # 根组件
└── main.js           # 应用入口文件
```

## 🚀 快速开始

### 开发环境运行
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

## 📖 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.x | 渐进式JavaScript框架 |
| Vue Router | 4.x | 官方路由管理器 |
| Pinia | 2.x | 新一代状态管理工具 |
| Element Plus | 2.x | Vue 3 UI组件库 |
| ECharts | 5.x | 数据可视化图表库 |
| Axios | 1.x | HTTP请求库 |
| Vite | 5.x | 下一代前端构建工具 |

## 🏗️ 架构设计

### 组件设计原则
- **组件复用**: 将通用功能封装为可复用组件
- **单一职责**: 每个组件只负责一个功能
- **组合式API**: 使用Vue 3 Composition API组织代码

### 目录职责

| 目录 | 职责说明 |
|------|----------|
| `components/` | 存放可复用的UI组件，按功能模块分类 |
| `composables/` | 存放组合式函数，封装可复用的逻辑 |
| `views/` | 存放页面级组件，对应路由 |
| `stores/` | 存放Pinia状态管理模块 |
| `utils/` | 存放纯函数工具方法 |
| `styles/` | 存放全局样式和CSS变量 |

## 🔗 相关文档

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Element Plus 文档](https://element-plus.org/zh-CN/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)
- [ECharts 文档](https://echarts.apache.org/zh/index.html)
