# 侗乡医药数字展示平台 — 项目全面审查报告

**日期**: 2026-05-03
**审查范围**: 全栈项目（Vue 3 前端 + Spring Boot 后端）

---

## 总览评分

| 维度 | 评分 | 等级 |
|------|------|------|
| 架构设计 | **8.0/10** | 优秀 — 分层清晰，模式一致 |
| 代码质量 | **7.5/10** | 良好 — 有巨石组件优化后大幅改善 |
| 安全性 | **7.0/10** | 良好 — 防御编码扎实，配置有隐患 |
| 性能 | **7.0/10** | 良好 — 有缓存和分页，细节可优化 |
| 测试覆盖 | **7.5/10** | 良好 — 三层测试体系，前端组件测试薄弱 |
| 可维护性 | **8.0/10** | 优秀 — 拆分后组件 <250行，composable模式一致 |
| 文档完整性 | **8.5/10** | 优秀 — README详尽，各包有说明文档 |
| **综合** | **7.6/10** | **良好 — 毕业设计项目中属于上等水平** |

---

## 一、架构设计 (8.0/10)

### 亮点

- **三层架构严格遵循**：Controller → Service(Interface) → ServiceImpl → Mapper，控制层薄而专注
- **统一响应格式**：`R.java` 包装所有响应为 `{code, msg, data, requestId}`，全项目一致
- **AOP 横切关注点**：`OperationLogAspect`（审计）、`RateLimitAspect`（限流）、`LoggingAspect`（性能日志）、`XssFilter`（XSS防护）均在 config 层独立管理
- **消息队列解耦**：RabbitMQ 5对生产者/消费者处理操作日志、统计、通知、反馈、文件处理等异步任务
- **前端 Composable 模式**：业务逻辑从组件中提取到 composables，可复用可测试
- **前端路由懒加载**：14条路由全部使用 `() => import(...)` 动态导入

### 待优化

1. **缓存是退化模式，非真正的两级缓存**：Redis 与 Caffeine 不能同时工作，Redis 故障时 Caffeine 顶替但无法自动恢复
2. **缓存驱逐粒度过粗**：写操作时 `@CacheEvict(allEntries = true)` 导致全量缓存失效，高并发下引发缓存击穿
3. **存在未分页的内部 Service 方法**：`getAllTherapies`、`advancedSearch`（未分页版本）直接返回全量 List

---

## 二、代码质量 (7.5/10)

### 亮点

- **本次重构后大幅改善**：PersonalCenter.vue 1131→187行，AiChatCard.vue 916→247行，抽出11个子组件+4个composables
- **命名规范统一**：Java 遵循驼峰命名，Vue 组件 PascalCase，composables useXxx 模式
- **Lombok 合理使用**：`@Data`、`@Builder`、`@RequiredArgsConstructor` 减少样板代码
- **MyBatis-Plus LambdaQueryWrapper**：类型安全的查询构建，避免字符串拼SQL
- **E2E 测试已拆分**：8个独立 spec 文件，支持 Playwright 并行执行

### 待优化

1. **仍有个别大文件**：Home.vue 1660行（含大量CSS）、GlobalSearch.vue 752行、SolarTerms.vue 641行
2. **CSS 与组件耦合松散**：5个全局CSS文件共3554行，样式与组件分离增加维护心智负担
3. **部分 composable 过大**：`useAdminData.js` 391行含4个函数，`useInteraction.js` 可进一步拆分
4. **存在冗余的 console.log**：AiChatCard 中有多处调试日志未清理

---

## 三、安全性 (7.0/10)

### 亮点

- **多层 XSS 防护**：后端 `XssFilter.java` 递归清理 JSON 实体，前端 `xss.js` 通过 DOMParser + 模式匹配清理请求参数
- **速率限制到位**：`RateLimitAspect` 实现 Redis 令牌桶，登录端点 5次/秒，注册 3次/秒，降级到本地限流
- **输入验证**：Controller 使用 `@Validated` + `@Valid` + DTO 注解进行参数校验
- **自动 Token 刷新**：`request.js` 拦截器实现 Token 自动续期和请求去重
- **JWT 密钥强度校验**：`SecurityConfigValidator` 启动时校验 JWT 密钥长度（最少32字符）
- **动态密码强度策略**：`PasswordValidator` 根据配置动态调整密码复杂度要求

### 待优化（🔴 高优先级）

1. **`.env` 文件包含真实密钥**：`DEEPSEEK_API_KEY`、`JWT_SECRET`、`DB_PASSWORD` 以明文存储在 `.env` 中，虽被 `.gitignore` 忽略但在开发机上可读
2. **Actuator/Swagger 端点公开**：`/actuator/**`、`/swagger-ui/**` 在 `SaTokenConfig` 中被排除认证，生产环境可被外部访问泄漏内部状态
3. **CORS 配置仅限 localhost**：`SaTokenConfig.addCorsMappings` 只允许 `localhost:*`，部署到生产域名时 CORS 策略会阻止正常请求

---

## 四、性能 (7.0/10)

### 亮点

- **两级缓存设计**：Redis + Caffeine，按数据类型设置差异化 TTL（搜索5分钟、植物6小时等）
- **全量分页**：所有列表 API 均返回分页数据 `{records, total, page, size}`，分页上限 100
- **Vite 代码分割**：`manualChunks` 将 echarts、element-plus、vue 生态拆分为独立 vendor chunks
- **前端图片懒加载**：自定义 `v-lazy` 指令实现图片懒加载
- **RabbitMQ 异步处理**：操作日志、统计计算、文件处理通过消息队列异步执行
- **N+1 查询风险低**：`FavoriteServiceImpl.buildFavoriteResults()` 按类型批量查询，生成 IN 查询

### 待优化

1. **`chunkSizeWarningLimit: 1500`**：屏蔽了构建警告，掩盖了潜在的包体积问题（echarts ~1MB、element-plus ~600KB）
2. **`maxParallelFileOps: 2`**：不必要地限制了构建并行度，拖慢构建速度
3. **无组件级异步加载**：Admin 管理后台的 Tab 子页面、Visual 数据可视化（依赖 echarts）可做按需加载
4. **缓存与分页冲突**：未分页的 `advancedSearch` 结果被缓存，分页版本反而不走缓存

---

## 五、测试覆盖 (7.5/10)

### 亮点

- **三层测试体系**：42个Java测试文件（单元+集成+回归），13个Vitest测试文件，7个Playwright E2E文件
- **关键路径全链路覆盖**：认证、植物、知识、反馈均有单元+集成+回归+E2E覆盖
- **Mock 使用规范**：后端统一使用 Mockito `@Mock`/`@InjectMocks`，前端使用 `vi.fn()`/`vi.mock()`
- **边界测试充分**：`QuizServiceImplTest` 覆盖 null、空列表、大小写、空白字符、不存在的ID等边界
- **回归测试有文档**：`@DisplayName` 标注历史 Bug 场景，每个测试可追溯到具体问题
- **E2E 可并行**：8个独立 spec 文件支持 Playwright 并行执行

### 待优化

1. **前端组件级测试薄弱**：仅有 `components.test.js` 覆盖 Vue 组件，缺少单个组件的渲染/Props/Events/状态测试
2. **无性能/压力测试**：没有后端 JMeter/Gatling 压测，没有前端 Lighthouse 性能基准
3. **AI 聊天无 E2E 覆盖**：WebSocket 聊天全流程没有端到端测试覆盖

---

## 六、可维护性 (8.0/10)

### 亮点

- **组件拆分后文件大小合理**：绝大多数组件 <250行，11个子组件 <120行
- **模式高度一致**：所有 Service 遵循 Interface+Impl，所有 Composable 遵循 useXxx 命名
- **目录结构清晰**：前端按 `base/business(admin/dialogs/display/interact/layout/media/upload)/common` 三层组织
- **配置文件分离**：application-dev.yml / application-prod.yml / application-test.yml 环境隔离
- **Docker Compose 一键部署**：6服务编排，entrypoint.sh 自动初始化数据库
- **GitHub Actions CI/CD**：推送即自动测试→构建→部署

### 待优化

1. **全局 CSS 3554行分散在5个文件**：样式修改需要跨文件搜索，CSS 与组件耦合松散
2. **部分 View 组件仍可进一步拆分**：Home、GlobalSearch、SolarTerms 等
3. **SQL 文件 934行单体**：未拆分为 DDL/DML/种子数据

---

## 七、文档完整性 (8.5/10)

### 亮点

- **项目级 README**：600行，面向初学者，含架构图、数据流示例、环境搭建步骤、学习路线
- **每个包有 README**：entity/、mapper/、service/、controller/、config/、deploy/ 均有详细说明文档
- **API 文档自动生成**：SpringDoc/Swagger UI 提供在线 API 文档
- **代码注释实用**：关键配置有中文注释说明业务含义

---

## 优先级排序的优化建议

| 优先级 | 类别 | 建议 | 预估工时 |
|--------|------|------|---------|
| 🔴 P0 | 安全 | 生产环境禁用 Swagger/Actuator 公开访问 | 0.5h |
| 🔴 P0 | 安全 | 修复 CORS 配置支持生产域名 | 0.5h |
| 🟡 P1 | 性能 | 移除 `chunkSizeWarningLimit: 1500` 和 `maxParallelFileOps: 2` | 0.2h |
| 🟡 P1 | 架构 | 修复缓存为真正的两级缓存（Redis + Caffeine 同时工作） | 3h |
| 🟡 P1 | 代码质量 | 继续拆分大 View 组件（Home、GlobalSearch、SolarTerms） | 6h |
| 🟢 P2 | 测试 | 增加前端组件级单元测试 | 8h |
| 🟢 P2 | 测试 | 增加 AI 聊天 E2E 测试 | 4h |
| 🟢 P2 | 性能 | 对 Admin/Visual 页面做组件级按需加载 | 2h |
| 🔵 P3 | 可维护性 | SQL 文件拆分为 schema + seed data | 1h |
| 🔵 P3 | 代码质量 | 清理 AiChatCard 中残留的调试 console.log | 0.5h |

---

## 审查总结

这是一个**结构良好的毕业设计项目**，在架构设计、文档完整性和可维护性方面表现突出。前端采用 Vue 3 Composition API + Composable 模式，后端遵循 Spring Boot 三层架构 + AOP 横切，技术选型合理。

**最大亮点**：统一的 R 响应格式、AOP 实现的限流/审计/日志、RabbitMQ 异步解耦、前端多层 XSS 防护、15 个 Dialog 的统一样式管理。

**最需改进**：生产环境 Actuator/Swagger 的公开暴露是安全隐患，缓存策略需要从退化模式升级为真正的两级缓存，前端组件测试薄弱需要补强。
