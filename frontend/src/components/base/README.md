# 基础组件目录

## 职责范围

基础组件是可复用的通用组件，具有以下特点：

1. **通用性**：不包含特定业务逻辑，可在多个项目中复用
2. **可配置性**：通过Props提供丰富的配置选项
3. **独立性**：不依赖业务组件，仅依赖UI框架和工具函数
4. **一致性**：遵循统一的接口规范和命名约定

## 组件列表

| 组件名 | 文件 | 功能描述 |
|--------|------|----------|
| ErrorBoundary | ErrorBoundary.vue | 错误边界组件，捕获子组件渲染错误 |

## 使用规范

### 导入方式

```javascript
// 推荐方式：从index.js导入
import { ErrorBoundary } from '@/components/base'

// 或直接导入
import ErrorBoundary from '@/components/base/ErrorBoundary.vue'
```

### 组件开发规范

1. 所有基础组件必须包含详细的JSDoc注释
2. Props必须定义类型和默认值
3. 事件使用emits声明
4. 样式使用scoped隔离

### 新增组件流程

1. 在本目录创建组件文件
2. 添加标准化文件注释
3. 在index.js中导出组件
4. 更新本README文档

## 依赖关系

- 外部依赖：Element Plus、Vue 3
- 内部依赖：无（基础组件不应依赖其他内部组件）
