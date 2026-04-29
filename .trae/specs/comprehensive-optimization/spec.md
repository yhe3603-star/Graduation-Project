# 项目全面优化 Spec

## Why
代码审查发现项目存在23个严重(P0)安全漏洞、17个高优先级(P1)问题和大量中等/低优先级问题，涉及安全漏洞、并发缺陷、逻辑错误、代码质量等多个方面，需要分三个阶段系统性修复。

## What Changes

### 第一阶段：安全漏洞修复（紧急）
- **SaTokenConfig**：将写操作API从排除列表中移除，SaInterceptor添加实际鉴权逻辑
- **UserServiceImpl.getUserToken()**：添加管理员权限校验和操作审计日志
- **PlantGameServiceImpl**：服务端根据题目答案验证答题结果，不信任客户端分数
- **XssFilter**：移除管理员路径(/api/admin/)跳过XSS过滤的规则
- **JWT密钥**：SecurityConfigValidator在开发环境也强制要求配置密钥，空值快速失败
- **CaptchaService**：将java.util.Random替换为java.security.SecureRandom
- **CI/CD**：将硬编码密码迁移到GitHub Secrets
- **Docker**：移除RabbitMQ、kkfileview、backend端口暴露

### 第二阶段：并发与逻辑修复（重要）
- **RateLimitAspect**：使用Redis Lua脚本实现原子限流，修复localBuckets内存泄漏
- **FavoriteServiceImpl**：添加数据库唯一索引防重复收藏
- **@Async自调用**：将incrementPopularityAsync提取到独立的PopularityAsyncService
- **ResourceServiceImpl**：实现fileType过滤逻辑
- **Dockerfile**：修复ENTRYPOINT使用entrypoint.sh

### 第三阶段：代码质量提升（计划）
- 统一依赖注入方式为构造器注入
- 统一时间字段命名为createdAt
- 抽取通用Entity基类BaseEntity
- 消除代码重复（getClientIp提取到IpUtils、前端上传组件抽取composable、密码校验规则统一）
- 前端状态管理统一到Pinia
- echarts按需导入

## Impact
- Affected specs: 认证鉴权体系、文件上传安全、游戏/测验逻辑、限流机制、收藏功能、Docker部署
- Affected code:
  - 后端: SaTokenConfig, XssFilter, UserServiceImpl, PlantGameServiceImpl, CaptchaService, RateLimitAspect, FavoriteServiceImpl, KnowledgeServiceImpl, ResourceServiceImpl, SecurityConfigValidator, Dockerfile, entrypoint.sh, 所有Entity, 所有ServiceImpl
  - 前端: App.vue, AdminDashboard.vue, ChartCard.vue, ImageUploader.vue, VideoUploader.vue, DocumentUploader.vue, useFavorite.js, usePersonalCenter.js
  - 基础设施: docker-compose.yml, ci-cd.yml, dong_medicine.sql

---

## ADDED Requirements

### Requirement: 安全认证强化
系统 SHALL 对所有写操作API（submit/create/update/delete）强制要求登录认证，仅只读GET接口可免认证访问。

#### Scenario: 未登录用户访问写操作API
- **WHEN** 未登录用户请求 /api/quiz/submit 或 /api/plant-game/submit 或 /api/chat
- **THEN** 返回401未授权错误

#### Scenario: SaInterceptor执行鉴权
- **WHEN** 请求经过SaInterceptor
- **THEN** 对非排除路径执行StpUtil.checkLogin()校验

### Requirement: getUserToken权限控制
UserServiceImpl.getUserToken() SHALL 仅允许管理员角色调用，并记录操作审计日志。

#### Scenario: 管理员获取用户Token
- **WHEN** 管理员角色用户调用getUserToken
- **THEN** 生成Token并记录审计日志（操作人、目标用户、时间）

#### Scenario: 普通用户获取用户Token
- **WHEN** 非管理员用户调用getUserToken
- **THEN** 抛出403权限不足异常

### Requirement: 服务端验证游戏/测验结果
PlantGameServiceImpl和QuizServiceImpl SHALL 在服务端验证答题结果，不信任客户端提交的分数。

#### Scenario: 提交游戏结果
- **WHEN** 用户提交植物识别游戏结果
- **THEN** 服务端根据提交的答案与数据库中正确答案比对计算分数

#### Scenario: 提交测验结果
- **WHEN** 用户提交测验答案
- **THEN** 服务端逐题验证答案正确性，累计计算分数

### Requirement: XSS过滤全覆盖
XssFilter SHALL 对所有路径（包括管理员路径）执行XSS过滤，但不对HTTP Header执行清洗。

#### Scenario: 管理员路径XSS过滤
- **WHEN** 请求路径以/api/admin/开头
- **THEN** 仍然执行XSS过滤，不跳过

#### Scenario: Authorization头不被清洗
- **WHEN** 请求包含Authorization头
- **THEN** Header值不做XSS清洗，原样传递

### Requirement: JWT密钥启动校验
应用启动时 SHALL 校验JWT密钥非空且长度>=32，校验失败时拒绝启动。

#### Scenario: JWT密钥为空
- **WHEN** 应用启动且JWT_SECRET环境变量未设置
- **THEN** 抛出IllegalStateException阻止应用启动

#### Scenario: JWT密钥长度不足
- **WHEN** JWT密钥长度小于32字符
- **THEN** 抛出IllegalStateException阻止应用启动

### Requirement: 安全随机数生成
CaptchaService SHALL 使用java.security.SecureRandom生成验证码。

#### Scenario: 生成验证码
- **WHEN** 调用generateCaptcha()
- **THEN** 使用SecureRandom生成不可预测的验证码数字

### Requirement: CI/CD密码安全
CI/CD部署脚本 SHALL 使用GitHub Secrets存储所有敏感信息，不在代码中硬编码。

#### Scenario: 部署使用Secrets
- **WHEN** CI/CD部署执行
- **THEN** 所有密码从secrets中读取，不硬编码在yaml中

### Requirement: Docker端口最小暴露
Docker部署 SHALL 仅暴露前端80端口，其他服务端口不暴露到宿主机。

#### Scenario: 后端端口不暴露
- **WHEN** docker-compose启动
- **THEN** backend 8080端口仅在内网可访问，不映射到宿主机

### Requirement: 原子限流
RateLimitAspect SHALL 使用Redis Lua脚本实现原子increment+expire操作。

#### Scenario: 限流计数原子操作
- **WHEN** 请求到达且Redis可用
- **THEN** increment和expire在单个Lua脚本中原子执行

#### Scenario: localBuckets内存回收
- **WHEN** bucket超过30分钟未使用
- **THEN** 自动从localBuckets Map中移除

### Requirement: 收藏唯一性保障
FavoriteServiceImpl SHALL 通过数据库唯一索引防止并发重复收藏。

#### Scenario: 并发收藏同一目标
- **WHEN** 两个请求同时收藏同一目标
- **THEN** 数据库唯一索引阻止第二次插入，抛出友好错误

### Requirement: 异步热度更新
热度更新 SHALL 通过独立的Service Bean实现@Async生效。

#### Scenario: 查看详情时异步更新热度
- **WHEN** 用户查看知识详情
- **THEN** 热度更新在独立线程中异步执行，不阻塞详情查询

### Requirement: 资源文件类型过滤
ResourceServiceImpl.listByCategoryAndKeywordAndType() SHALL 实现fileType参数的过滤逻辑。

#### Scenario: 按文件类型搜索
- **WHEN** 传入fileType参数
- **THEN** 查询结果按文件类型过滤，与分页版本行为一致

### Requirement: Dockerfile修复
后端Dockerfile SHALL 使用entrypoint.sh作为ENTRYPOINT，实现数据库等待和初始化。

#### Scenario: 容器启动
- **WHEN** 后端容器启动
- **THEN** 先等待MySQL就绪，再初始化数据库（如需要），最后启动应用

### Requirement: 构造器注入统一
所有ServiceImpl和Controller SHALL 使用@RequiredArgsConstructor构造器注入，不使用@Autowired字段注入。

### Requirement: 时间字段命名统一
所有Entity的时间字段 SHALL 统一命名为createdAt，不使用createTime。

### Requirement: 通用Entity基类
提取BaseEntity基类，包含id、createdAt、updatedAt通用字段。

### Requirement: 消除代码重复
- getClientIp方法提取到IpUtils工具类
- 前端上传组件抽取useFileUpload composable
- 密码校验规则提取为共享工厂函数

### Requirement: 前端状态管理统一
前端 SHALL 统一使用Pinia Store管理状态，不使用provide/inject传递全局状态，不直接读取sessionStorage。

### Requirement: echarts按需导入
前端 SHALL 使用echarts按需导入，仅引入使用的图表类型和组件。

## MODIFIED Requirements

### Requirement: SaTokenConfig认证路径配置
原配置排除所有/api/plants/**等通配符路径，现改为仅排除GET请求的只读路径，写操作路径必须认证。

### Requirement: SecurityConfigValidator校验逻辑
原逻辑在开发环境允许空密钥，现改为所有环境均要求密钥非空且长度>=32。

### Requirement: PlantGameServiceImpl分数计算
原逻辑直接使用客户端提交的correctCount/totalCount计算分数，现改为服务端根据题目答案验证。

### Requirement: ResourceServiceImpl搜索方法
原listByCategoryAndKeywordAndType方法忽略fileType参数，现实现与分页版本一致的fileType过滤。

## REMOVED Requirements

### Requirement: 管理员路径XSS过滤豁免
**Reason**: 管理员接口同样需要XSS防护，跳过过滤是安全漏洞
**Migration**: 移除XssFilter中对/api/admin/路径的特殊处理
