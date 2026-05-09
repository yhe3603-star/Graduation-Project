# 侗乡医药数字展示平台 - 测试总览

## 目录定位

本目录是 `com.dongmedicine` 测试包的根路径，包含项目所有后端测试代码。测试体系按照项目架构分层组织，覆盖从公共组件到业务逻辑的完整测试链路，是保障侗乡医药数字展示平台后端质量的核心基础设施。

## 测试架构总览

```
com.dongmedicine/
├── DongMedicineBackendApplicationTests.java   # 应用上下文加载测试
├── common/                                     # 公共组件测试
│   ├── RTest.java                              # 统一响应封装测试
│   ├── exception/                              # 异常体系测试
│   ├── util/                                   # 工具类测试
│   └── constant/                               # 常量类测试
├── controller/                                 # 控制器层测试
│   ├── *.java                                  # 前台API控制器测试
│   └── admin/                                  # 后台管理控制器测试
├── integration/                                # 集成测试
├── regression/                                 # 回归测试
├── service/impl/                               # 服务实现层测试
└── websocket/                                  # WebSocket测试
```

## 测试分类说明

### 1. 公共组件测试 (`common/`)
验证项目基础设施工具的正确性，包括统一响应封装 `R<T>`、异常体系 `BusinessException`/`ErrorCode`、工具类（XSS防护、文件处理、分页、密码验证、敏感数据脱敏、IP解析）和角色常量。这些是所有上层代码的依赖基础。

### 2. 控制器层测试 (`controller/`)
采用 Mockito 单元测试方式，通过 `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + `@Mock` 隔离 Service 层依赖，验证每个 Controller 的请求参数处理、权限校验、业务委托和响应封装逻辑。前台控制器覆盖植物、知识库、传承人、问答、测验、收藏、评论、反馈、搜索、导出、文件上传、通知、排行榜、统计、浏览历史、聊天等全部 API；后台管理控制器覆盖内容管理、互动管理、统计管理和用户管理。

### 3. 集成测试 (`integration/`)
继承 `BaseIntegrationTest`，使用 `@SpringBootTest` 启动完整 Spring 上下文，通过 `MockMvc` 发送真实 HTTP 请求，验证端到端的请求路由、参数校验、数据库交互、认证鉴权和响应格式。测试前自动执行 `schema-test.sql` 初始化数据库。

### 4. 回归测试 (`regression/`)
针对历史 Bug 的专项测试，确保已修复的问题不再复发。覆盖认证流程、分页参数边界、XSS/SQL注入防护、反馈提交、搜索特殊字符、API权限控制、统一响应格式等关键场景。

### 5. 服务实现层测试 (`service/impl/`)
验证各 Service 实现类的核心业务逻辑，包括数据查询、分页排序、异常抛出、文件清理、缓存清除、密码加密、异步操作等。通过反射注入 `baseMapper` 解决 MyBatis-Plus ServiceImpl 的依赖问题。

### 6. WebSocket测试 (`websocket/`)
验证 WebSocket 聊天处理器的连接建立/关闭、消息解析、错误处理和停止生成等场景。

## 技术栈与规范

| 项目 | 技术选型 |
|------|---------|
| 测试框架 | JUnit 5 |
| Mock框架 | Mockito（含 MockedStatic 静态方法Mock） |
| Spring测试 | Spring Boot Test、MockMvc、@SpringBootTest |
| 断言库 | JUnit Assertions、AssertJ |
| 参数化测试 | @ParameterizedTest + @ValueSource |
| 测试配置 | @ActiveProfiles("test") |
| 数据库初始化 | @Sql(scripts = "classpath:schema-test.sql") |

## 测试设计原则

1. **隔离性**：单元测试通过 Mockito 隔离外部依赖，确保测试只验证目标逻辑
2. **命名规范**：使用 `@DisplayName` 中文注解，方法名采用 `test方法名_场景_预期结果` 格式
3. **边界覆盖**：重点测试 null、空值、越界、极端参数等边界条件
4. **安全验证**：覆盖 XSS 防护、SQL 注入检测、路径遍历防护、敏感数据脱敏等安全场景
5. **权限校验**：区分已登录/未登录状态，验证 `SecurityUtils` 静态方法的 Mock 行为

## 文件清单

| 文件 | 功能 |
|------|------|
| `DongMedicineBackendApplicationTests.java` | 验证 Spring 应用上下文能成功加载，Mock SaTokenDao、RabbitTemplate、RedisConnectionFactory |

## 依赖关系

- `DongMedicineBackendApplicationTests` 是集成测试的入口验证点，确保 `@SpringBootTest` + `@ActiveProfiles("test")` 环境可用
- `common/` 下的测试无外部依赖，是测试链路的最底层
- `controller/` 依赖 `common/` 中的 `R`、`SecurityUtils`、`BusinessException` 等
- `integration/` 和 `regression/` 依赖完整 Spring 上下文和测试数据库
- `service/impl/` 依赖 MyBatis-Plus 的 Mapper 层
