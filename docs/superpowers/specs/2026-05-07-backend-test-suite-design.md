# 后端测试套件补充设计文档

## 概述

补充Dong Medicine项目后端缺失的单元测试，覆盖24个未测试的Controller和5个未测试的Service实现。

## 当前状态

| 类型 | 已有 | 缺失 | 总计 |
|------|------|------|------|
| Controller测试 | 3 | 24 | 27 |
| Service测试 | 13 | 5 | 18 |
| 工具类测试 | 11 | 0 | 11 |
| 集成测试 | 6 | 0 | 6 |
| 回归测试 | 6 | 0 | 6 |

## 方案：混合测试策略

### Controller单元测试（24个，@WebMvcTest）

使用Spring Boot的 `@WebMvcTest` 切片测试，只加载Web层，mock Service层依赖。

**技术方案：**
- `@WebMvcTest(XxxController.class)` 注解限定只加载被测Controller
- `@MockBean` 注入mock的Service实例
- `MockMvc` 发送HTTP请求验证响应
- Sa-Token认证通过 `SaHolder` 或 `StpUtil` 手动设置会话上下文

**每个Controller的标准覆盖点：**

| # | 测试场景 | 预期行为 |
|---|---------|---------|
| 1 | 正常GET请求（列表/详情） | 200 + R.success + 正确JSON结构 |
| 2 | 正常POST/PUT/DELETE请求 | 200 + 业务成功 |
| 3 | 未登录访问@SaCheckLogin接口 | 401或拦截 |
| 4 | 普通用户访问@SaCheckRole("admin")接口 | 403或拦截 |
| 5 | 无效参数（缺失必填字段、类型错误） | 400 + 错误消息 |
| 6 | 分页边界（page=0/负数, size=0, size>100） | 正确处理或默认值 |
| 7 | 资源不存在（id=不存在） | 404或业务错误码 |

**Controller分组和文件列表：**

**内容管理组（5个）：**
- `PlantControllerTest.java` — 已有，需扩展（当前覆盖不足）
- `KnowledgeControllerTest.java` — 新建
- `InheritorControllerTest.java` — 新建
- `QaControllerTest.java` — 新建
- `ResourceControllerTest.java` — 新建

**交互功能组（3个）：**
- `CommentControllerTest.java` — 新建
- `FavoriteControllerTest.java` — 新建
- `FeedbackControllerTest.java` — 已有，需验证覆盖度

**用户相关组（4个）：**
- `UserControllerTest.java` — 新建（登录/注册/修改密码/token刷新）
- `CaptchaControllerTest.java` — 新建
- `ChatHistoryControllerTest.java` — 新建
- `BrowseHistoryControllerTest.java` — 新建

**通用功能组（5个）：**
- `SearchControllerTest.java` — 新建
- `LeaderboardControllerTest.java` — 新建
- `MetadataControllerTest.java` — 新建
- `StatisticsControllerTest.java` — 新建
- `NotificationControllerTest.java` — 新建

**管理后台组（4个）：**
- `AdminContentControllerTest.java` — 新建（植物/知识/传承人/QA/资源CRUD）
- `AdminUserControllerTest.java` — 新建（用户管理：列表/删除/角色/封禁）
- `AdminInteractionControllerTest.java` — 新建（反馈/评论管理）
- `AdminStatsControllerTest.java` — 新建（统计面板）

**辅助功能组（3个）：**
- `FileUploadControllerTest.java` — 新建（文件类型校验、大小限制）
- `ExportControllerTest.java` — 新建（CSV导出、实体类型校验）
- `OperationLogControllerTest.java` — 新建（日志查询/删除/统计）

**ChatControllerTest.java** — 新建（AI聊天统计接口）

### Service单元测试（5个，Mockito）

遵循现有 `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` 模式。

| Service | 测试重点 |
|---------|---------|
| `ChatHistoryServiceImpl` | 会话列表、消息查询、会话删除、空会话处理 |
| `FileUploadServiceImpl` | 文件类型校验、路径安全检查、文件删除 |
| `OperationLogServiceImpl` | 日志查询、分页、按类型/模块筛选 |
| `PopularityAsyncServiceImpl` | 热度计算逻辑、异步更新触发 |
| `RabbitMQOperationLogServiceImpl` | 消息发送、序列化、异常处理 |

### 测试目录结构

```
src/test/java/com/dongmedicine/
├── controller/
│   ├── PlantControllerTest.java          (已有，扩展)
│   ├── QuizControllerTest.java           (已有)
│   ├── FeedbackControllerTest.java       (已有)
│   ├── KnowledgeControllerTest.java      (新建)
│   ├── InheritorControllerTest.java      (新建)
│   ├── QaControllerTest.java             (新建)
│   ├── ResourceControllerTest.java       (新建)
│   ├── CommentControllerTest.java        (新建)
│   ├── FavoriteControllerTest.java       (新建)
│   ├── UserControllerTest.java           (新建)
│   ├── CaptchaControllerTest.java        (新建)
│   ├── ChatHistoryControllerTest.java    (新建)
│   ├── BrowseHistoryControllerTest.java  (新建)
│   ├── SearchControllerTest.java         (新建)
│   ├── LeaderboardControllerTest.java    (新建)
│   ├── MetadataControllerTest.java       (新建)
│   ├── StatisticsControllerTest.java     (新建)
│   ├── NotificationControllerTest.java   (新建)
│   ├── AdminContentControllerTest.java   (新建)
│   ├── AdminUserControllerTest.java      (新建)
│   ├── AdminInteractionControllerTest.java (新建)
│   ├── AdminStatsControllerTest.java     (新建)
│   ├── FileUploadControllerTest.java     (新建)
│   ├── ExportControllerTest.java         (新建)
│   ├── OperationLogControllerTest.java   (新建)
│   └── ChatControllerTest.java           (新建)
├── service/impl/
│   ├── ... (13 existing)
│   ├── ChatHistoryServiceImplTest.java         (新建)
│   ├── FileUploadServiceImplTest.java          (新建)
│   ├── OperationLogServiceImplTest.java        (新建)
│   ├── PopularityAsyncServiceImplTest.java     (新建)
│   └── RabbitMQOperationLogServiceImplTest.java (新建)
├── integration/          (保持不变)
├── regression/           (保持不变)
└── common/               (保持不变)
```

### 编码规范

- 中文 `@DisplayName` 注解（与现有测试一致）
- 每个测试方法一个断言场景（AAA模式：Arrange-Act-Assert）
- 使用现有 `R.java` 响应结构进行断言
- Controller测试不依赖数据库，全部mock
- Service测试不依赖Spring上下文，纯Mockito

### 预期产出

- 24个新建/扩展的Controller测试文件
- 5个新建的Service测试文件
- 总计新增约 150-200 个测试方法
- 所有测试可通过 `./mvnw test -B` 一次性运行通过

### 验收标准

1. `./mvnw test -B` 全部通过，无失败
2. 后端测试文件总数从42个增加到约66个
3. Controller测试覆盖从3/27提升到27/27
4. Service测试覆盖从13/18提升到18/18
