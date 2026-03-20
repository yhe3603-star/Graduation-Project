# 侗乡医药数字展示平台前端

## 环境要求

- Node.js 18+
- npm 9+

## 环境变量

复制 `.env.example` 为 `.env.development` 或 `.env.production` 后按环境修改：

- `VITE_API_BASE_URL`：后端 API 基础路径

## 本地启动

```bash
npm install
npm run dev
```

默认开发地址：

- `http://localhost:5173`

## 质量命令

```bash
npm run lint
npm run test:run
npm run build
```

## 目录说明

- `src/views`：路由页面
- `src/components`：通用组件和业务组件
- `src/utils`：工具函数
- `src/composables`：组合式逻辑
- `src/router`：路由配置

## 构建产物

- `dist/`：生产环境静态资源
