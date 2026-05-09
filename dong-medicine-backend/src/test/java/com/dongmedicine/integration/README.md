# 集成测试 (`integration`)

## 目录定位

本目录包含项目的端到端集成测试，验证 `com.dongmedicine` 各模块在完整 Spring 上下文下的协同工作。与单元测试不同，集成测试启动真实的 Spring Boot 应用，通过 MockMvc 发送 HTTP 请求，经过完整的请求路由、参数校验、数据库交互、认证鉴权和响应序列化流程，是验证系统整体行为的关键测试层。

## 测试基础设施

### BaseIntegrationTest.java - 集成测试基类

所有集成测试的抽象基类，提供统一的测试环境配置：

| 配置项 | 说明 |
|--------|------|
| `@SpringBootTest(webEnvironment = RANDOM_PORT)` | 启动完整 Spring 上下文，随机端口 |
| `@AutoConfigureMockMvc` | 自动配置 MockMvc |
| `@ActiveProfiles("test")` | 使用 test 配置文件 |
| `@Sql(scripts = "classpath:schema-test.sql")` | 每个测试方法前执行数据库初始化 |
| `MockConfig` | Mock 外部依赖：SaTokenDao、RabbitTemplate、RedisConnectionFactory、5个 MQ Producer、CaptchaService |

**Mock 策略**：
- **SaTokenDao**：使用 `SaTokenDaoDefaultImpl` 内存实现替代 Redis 存储
- **RabbitTemplate**：Mock 掉消息队列，避免依赖 RabbitMQ 服务
- **RedisConnectionFactory**：Mock Redis 连接，避免依赖 Redis 服务
- **MQ Producer**（5个）：Mock 所有消息生产者，避免消息发送
- **CaptchaService**：Mock 验证码服务，`validateCaptchaOrThrow` 永远通过，`generateCaptcha` 返回固定结果

**测试常量**：
- `TEST_USERNAME = "testuser"` / `TEST_PASSWORD = "Test1234"`
- `ADMIN_USERNAME = "admin"` / `ADMIN_PASSWORD = "Admin1234"`
- `TEST_CAPTCHA_KEY = "test-captcha-key"` / `TEST_CAPTCHA_CODE = "1234"`

## 文件清单

### AuthIntegrationTest.java - 认证模块集成测试

| 测试组 | 测试场景 | 测试逻辑 |
|--------|---------|---------|
| POST /api/user/login | 正确凭据登录 | 返回200、token非空、username/role正确 |
| | 管理员登录 | 返回200、role="admin" |
| | 错误密码 | 返回500、code非200 |
| | 不存在用户 | 返回404 |
| | 空用户名 | 返回400 |
| | 缺少验证码字段 | 返回400 |
| POST /api/user/register | 已存在用户名 | 返回409 |
| | 用户名太短 | 返回400 |
| | 两次密码不一致 | 返回400 |
| | 注册新用户 | 返回200 |
| GET /api/captcha | 获取验证码 | 返回200、captchaKey/captchaImage非空 |
| POST /api/user/logout | 登录后退出 | 返回200 |
| GET /api/user/validate | 有效Token验证 | valid=true、id为数字 |
| | 无效Token验证 | valid=false |

**核心测试思路**：覆盖完整的认证流程（注册->登录->验证->退出），验证 HTTP 状态码、响应格式和业务逻辑的正确性。

### PlantIntegrationTest.java - 植物模块集成测试

| 测试组 | 测试场景 | 测试逻辑 |
|--------|---------|---------|
| GET /api/plants/list | 植物列表 | 返回分页数据(records/total/page) |
| | size=9999限制为100 | data.size=100 |
| | size=0限制为1 | data.size=1 |
| | page=-1限制为1 | data.page=1 |
| | 按分类过滤 | 返回对应分类植物 |
| | 默认参数 | 正常工作 |
| GET /api/plants/search | 搜索钩藤 | 返回搜索结果 |
| GET /api/plants/{id} | 存在的植物 | 返回详情(nameCn非空) |
| | 不存在的植物 | 返回404 |
| POST /api/plants/{id}/view | 浏览量递增 | 无需登录，返回200 |
| GET /api/plants/random | 随机植物 | 返回数组 |
| GET /api/plants/{id}/similar | 相似植物 | 返回列表 |

**核心测试思路**：重点验证分页参数边界约束在真实 HTTP 请求中的表现，以及资源不存在时的 404 响应。

### KnowledgeIntegrationTest.java - 知识库模块集成测试

| 测试组 | 测试场景 | 测试逻辑 |
|--------|---------|---------|
| GET /api/knowledge/list | 知识列表 | 返回分页数据 |
| | 按疗法分类过滤 | 返回筛选结果 |
| GET /api/knowledge/{id} | 知识详情 | 返回详情(title非空) |
| POST /api/knowledge/{id}/view | 浏览量递增 | 无需登录，返回200 |

### InheritorAndResourceIntegrationTest.java - 传承人与资源模块集成测试

| 测试组 | 测试场景 | 测试逻辑 |
|--------|---------|---------|
| GET /api/inheritors/list | 传承人列表 | 返回分页数据 |
| | 按级别过滤 | 返回筛选结果 |
| GET /api/inheritors/{id} | 传承人详情 | 返回详情(name非空) |
| GET /api/resources/list | 资源列表 | 返回分页数据 |
| GET /api/resources/hot | 热门资源 | 返回200 |
| GET /api/resources/categories | 资源分类 | 返回200 |

### QaAndQuizIntegrationTest.java - 问答与测验模块集成测试

| 测试组 | 测试场景 | 测试逻辑 |
|--------|---------|---------|
| GET /api/qa/list | 问答列表 | 返回分页数据 |
| GET /api/qa/search | 搜索问答 | 返回搜索结果 |
| GET /api/quiz/questions | 随机题目 | 返回题目数组 |
| | count超过50限制 | 数组长度<=50 |
| GET /api/quiz/list | 题目列表分页 | 返回分页数据 |

### FeedbackIntegrationTest.java - 反馈模块集成测试

| 测试组 | 测试场景 | 测试逻辑 |
|--------|---------|---------|
| POST /api/feedback | 匿名提交反馈 | 返回200 |
| | 空内容 | 返回400 |
| | 缺少类型 | 返回400 |
| | 缺少标题 | 返回400 |
| GET /api/feedback/stats | 反馈统计 | 无需登录，返回total/pending/resolved |

**核心测试思路**：验证反馈提交的字段校验（type/title/content 必填）和匿名访问能力。

## 测试覆盖范围

| 模块 | 覆盖 API | 重点验证 |
|------|---------|---------|
| 认证 | 登录/注册/验证码/退出/Token验证 | HTTP状态码、响应格式、认证流程 |
| 植物 | 列表/搜索/详情/浏览量/随机/相似 | 分页边界、404处理 |
| 知识库 | 列表/详情/浏览量 | 分类过滤 |
| 传承人+资源 | 列表/详情/热门/分类 | 联合测试 |
| 问答+测验 | 列表/搜索/题目 | count限制 |
| 反馈 | 提交/统计 | 字段校验、匿名访问 |

## 依赖关系

- 所有集成测试继承 `BaseIntegrationTest`，共享 Mock 配置和测试常量
- 依赖 `schema-test.sql` 初始化测试数据库（含测试用户、植物、知识等基础数据）
- `AuthIntegrationTest` 提供的 `loginAndGetToken()` 方法被 `ControllerRegressionTest` 复用
- 集成测试与 `regression/ControllerRegressionTest` 互补：集成测试验证正常流程，回归测试验证 Bug 修复
