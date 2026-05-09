# 项目文件夹README文档体系全面更新 Spec

## Why
项目已有大量README文档，但需要基于现有代码进行全面审查和更新，确保所有文档内容与实际代码完全一致。同时存在部分代码目录缺少README文档（后端测试目录、mq子目录、前端ai-chat、personal-center、e2e、后端resources等）。需要形成一套完整、准确的项目文档体系，帮助全面理解项目的设计理念、实现方式及代码组织架构。

## What Changes
- 为缺少README的代码目录创建文档（后端test目录体系、mq/producer、mq/consumer、controller/admin、前端ai-chat、personal-center、e2e、后端resources）
- 基于实际代码全面审查并更新所有现有README文档，确保内容准确、详细、与代码一致
- 所有文档内容必须基于项目实际代码，严禁修改任何源代码文件

## Impact
- Affected specs: 文档体系完整性和准确性
- Affected code: 仅README.md文件，不涉及源代码修改

## ADDED Requirements

### Requirement: 缺失目录README创建
系统 SHALL 为以下缺少README的代码目录创建文档：
1. `dong-medicine-backend/src/test/java/com/dongmedicine/` 及其子目录（common、common/exception、common/util、common/constant、controller、controller/admin、integration、regression、service/impl、websocket）
2. `dong-medicine-backend/src/main/java/com/dongmedicine/controller/admin/`
3. `dong-medicine-backend/src/main/java/com/dongmedicine/mq/producer/`
4. `dong-medicine-backend/src/main/java/com/dongmedicine/mq/consumer/`
5. `dong-medicine-backend/src/main/resources/`
6. `dong-medicine-frontend/e2e/`
7. `dong-medicine-frontend/src/components/business/display/ai-chat/`
8. `dong-medicine-frontend/src/views/personal-center/`

#### Scenario: 创建缺失README
- **WHEN** 目录包含代码文件但缺少README.md
- **THEN** 基于目录内实际代码创建README，包含：整体功能定位、各文件功能说明、核心算法/业务逻辑、设计思路、文件间依赖关系

### Requirement: 后端现有README全面更新
系统 SHALL 基于实际代码审查并更新以下后端README文档：
1. `dong-medicine-backend/README.md` - 项目总览
2. `dong-medicine-backend/src/main/java/com/dongmedicine/README.md` - Java包总览
3. `dong-medicine-backend/src/main/java/com/dongmedicine/controller/README.md` - 控制器层
4. `dong-medicine-backend/src/main/java/com/dongmedicine/service/README.md` - 服务接口层
5. `dong-medicine-backend/src/main/java/com/dongmedicine/service/impl/README.md` - 服务实现层
6. `dong-medicine-backend/src/main/java/com/dongmedicine/mapper/README.md` - 数据访问层
7. `dong-medicine-backend/src/main/java/com/dongmedicine/entity/README.md` - 实体层
8. `dong-medicine-backend/src/main/java/com/dongmedicine/dto/README.md` - 数据传输对象层
9. `dong-medicine-backend/src/main/java/com/dongmedicine/config/README.md` - 配置层
10. `dong-medicine-backend/src/main/java/com/dongmedicine/config/health/README.md` - 健康检查配置
11. `dong-medicine-backend/src/main/java/com/dongmedicine/config/logging/README.md` - 日志配置
12. `dong-medicine-backend/src/main/java/com/dongmedicine/common/README.md` - 公共模块
13. `dong-medicine-backend/src/main/java/com/dongmedicine/common/constant/README.md` - 常量定义
14. `dong-medicine-backend/src/main/java/com/dongmedicine/common/exception/README.md` - 异常体系
15. `dong-medicine-backend/src/main/java/com/dongmedicine/common/util/README.md` - 工具类
16. `dong-medicine-backend/src/main/java/com/dongmedicine/websocket/README.md` - WebSocket模块
17. `dong-medicine-backend/src/main/java/com/dongmedicine/mq/README.md` - 消息队列模块
18. `dong-medicine-backend/sql/README.md` - SQL脚本

#### Scenario: 更新后端README
- **WHEN** 现有README内容与实际代码不一致或缺少关键信息
- **THEN** 基于实际代码更新功能说明、核心逻辑、设计思路、依赖关系等内容

### Requirement: 前端现有README全面更新
系统 SHALL 基于实际代码审查并更新以下前端README文档：
1. `dong-medicine-frontend/README.md` - 项目总览
2. `dong-medicine-frontend/src/README.md` - 源码总览
3. `dong-medicine-frontend/src/components/README.md` - 组件总览
4. `dong-medicine-frontend/src/components/base/README.md` - 基础组件
5. `dong-medicine-frontend/src/components/common/README.md` - 通用组件
6. `dong-medicine-frontend/src/components/business/README.md` - 业务组件
7. `dong-medicine-frontend/src/components/business/display/README.md` - 展示组件
8. `dong-medicine-frontend/src/components/business/interact/README.md` - 交互组件
9. `dong-medicine-frontend/src/components/business/media/README.md` - 媒体组件
10. `dong-medicine-frontend/src/components/business/upload/README.md` - 上传组件
11. `dong-medicine-frontend/src/components/business/layout/README.md` - 布局组件
12. `dong-medicine-frontend/src/components/business/dialogs/README.md` - 对话框组件
13. `dong-medicine-frontend/src/components/business/admin/README.md` - 管理后台组件
14. `dong-medicine-frontend/src/components/business/admin/forms/README.md` - 表单组件
15. `dong-medicine-frontend/src/components/business/admin/dialogs/README.md` - 管理对话框组件
16. `dong-medicine-frontend/src/views/README.md` - 页面视图
17. `dong-medicine-frontend/src/composables/README.md` - 组合式函数
18. `dong-medicine-frontend/src/utils/README.md` - 工具函数
19. `dong-medicine-frontend/src/stores/README.md` - 状态管理
20. `dong-medicine-frontend/src/router/README.md` - 路由配置
21. `dong-medicine-frontend/src/styles/README.md` - 样式体系
22. `dong-medicine-frontend/src/directives/README.md` - 自定义指令
23. `dong-medicine-frontend/src/__tests__/README.md` - 单元测试

#### Scenario: 更新前端README
- **WHEN** 现有README内容与实际代码不一致或缺少关键信息
- **THEN** 基于实际代码更新功能说明、核心逻辑、设计思路、依赖关系等内容

### Requirement: 根目录及部署文档更新
系统 SHALL 基于实际代码审查并更新以下文档：
1. `README.md` - 项目根目录总览
2. `deploy/README.md` - 部署文档

#### Scenario: 更新根目录文档
- **WHEN** 根目录README与项目实际情况不一致
- **THEN** 基于实际项目结构和配置更新文档

### Requirement: 文档编写规范
所有README文档 SHALL 遵循以下规范：
1. 包含文件夹整体功能定位及在项目架构中的作用
2. 列出文件夹内各文件的具体功能、核心算法实现及关键业务逻辑
3. 说明重要代码片段的设计思路、技术选型依据及实现细节
4. 描述文件间的依赖关系及交互流程
5. 所有说明必须基于项目实际代码
6. 采用工程化文档编写规范，结构清晰、逻辑严谨
7. 保持与项目现有README风格一致

#### Scenario: 文档质量验证
- **WHEN** README文档创建或更新完成
- **THEN** 文档内容与实际代码一致，结构完整，逻辑清晰
