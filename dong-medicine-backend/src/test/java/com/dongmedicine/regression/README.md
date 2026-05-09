# 回归测试 (`regression`)

## 目录定位

本目录包含项目历史 Bug 的专项回归测试，确保已修复的问题不再复发。回归测试分为两类：纯工具类回归测试（无需 Spring 上下文）和 API 层回归测试（需要完整 Spring 上下文 + MockMvc）。每个测试类针对特定 Bug 场景设计，测试名称直接关联 Bug 描述。

## 测试设计思路

回归测试的核心原则是**针对 Bug 根因编写测试**，而非简单复现 Bug 表象：

1. **Bug 标注**：每个 `@Nested` 类用 `Bug: xxx` 命名，直接关联 Bug 描述
2. **根因验证**：测试验证修复 Bug 的底层机制（如工具类方法），而非仅验证 API 层表现
3. **边界强化**：对 Bug 涉及的边界条件进行参数化测试，增强防护
4. **双层覆盖**：工具类回归测试（快速）+ API 回归测试（端到端）

## 文件清单

### AuthRegressionTest.java - 认证相关回归测试

| Bug 场景 | 测试逻辑 |
|---------|---------|
| Bug: 401 Unauthorized - view接口需要登录 | 验证 `PageUtils.escapeLike` 正确转义复合字符串和 null/空 |
| Bug: 密码错误返回500而非400/401 | 验证 `XssUtils.sanitize` 处理 null/空/正常中文文本 |

**说明**：这两个 Bug 的根因分别与 SQL LIKE 注入和 XSS 清洗有关，回归测试验证底层工具方法的正确性。

### FeedbackRegressionTest.java - 反馈相关回归测试

| Bug 场景 | 测试逻辑 |
|---------|---------|
| Bug: 反馈提交logFetchError未定义 | 验证 `XssUtils.sanitizeForLog` 处理 null/截断超长输入/空字符串/清除控制字符 |
| Bug: 反馈提交缺少验证码字段导致500 | 验证 `XssUtils.containsSqlInjection/containsXss` 处理 null/空字符串 |

**说明**：反馈提交 Bug 的根因是日志记录和输入校验的空值处理缺失。

### PaginationRegressionTest.java - 分页参数回归测试

| Bug 场景 | 测试逻辑 |
|---------|---------|
| Bug: 列表接口默认查全量数据(size=9999) | 验证 size=9999/1000 限制为100、size=0 限制为1、page=-1/0 限制为1、null参数使用默认值、参数化测试各种 size 边界值(1/10/50/99/100/101/500/9999/MAX_VALUE) |

**说明**：这是最关键的回归测试之一，确保分页参数始终在安全范围内 [1, 100]。

### XssRegressionTest.java - XSS防护回归测试

| Bug 场景 | 测试逻辑 |
|---------|---------|
| Bug: XSS漏洞 - AiChatCard渲染恶意HTML | 验证 script 标签转义、javascript 协议检测、事件处理器检测、iframe 检测、URL 中 javascript/data 协议清除、SQL 注入检测、LIKE 通配符转义、img onerror 检测、svg 标签检测、大小写混合 XSS 检测、正常文本不误判、正常 URL 不清除 |

**说明**：这是安全类回归测试的核心，覆盖 XSS 攻击的12种变体和 SQL 注入防护。

### GeneralRegressionTest.java - 其他Bug场景回归测试

| Bug 场景 | 测试逻辑 |
|---------|---------|
| Bug: WebSocket DeepSeek调用阻塞 | 验证 PageUtils 分页 size 上限100 |
| Bug: 视频播放ERR_CACHE_OPERATION_NOT_SUPPORTED | 验证分页参数边界值处理 |
| Bug: 问答题目count参数无上限 | 验证所有 size 参数被限制 |
| Bug: 管理后台日志LIMIT 5000 | 验证 PageUtils 最大 size 为100 |
| Bug: RestTemplate无超时配置 | 验证 PageUtils 处理极端值(null/-1/0/MAX_VALUE)不抛异常 |
| Bug: 搜索特殊字符导致数据库错误 | 验证 escapeLike 转义 LIKE 通配符、sanitizeUrl 处理 null/保留合法 URL |

### ControllerRegressionTest.java - Controller层API回归测试

继承 `BaseIntegrationTest`，使用 MockMvc 进行端到端回归验证：

| Bug 场景 | 测试逻辑 |
|---------|---------|
| Bug: 浏览量递增POST不需要登录 | 植物/知识/传承人浏览量递增不返回401 |
| Bug: 反馈提交不需要登录 | 匿名提交反馈成功/反馈统计不需要登录 |
| Bug: 列表接口size参数无上限 | 植物 size=9999 限制/知识 size=0 限制/传承人 page=-1 限制 |
| Bug: XSS搜索导致服务器错误 | script 标签搜索不返回500/SQL 注入搜索不返回500 |
| Bug: 验证码接口不需要登录 | 返回200 |
| Bug: 评论列表不需要登录 | 不返回401 |
| Bug: 排行榜不需要登录 | 不返回401 |
| Bug: 测验题目不需要登录 | 不返回401 |
| Bug: 资源相关接口不需要登录 | 列表/分类/类型/热门不返回401 |
| Bug: 需要登录的接口应返回401 | 用户信息/我的反馈/我的评论/我的收藏返回401 |
| Bug: 登录后Token应有效 | Token 验证通过/获取用户信息成功 |
| Bug: 统一响应格式 | 植物/知识列表返回 code+data/分页数据包含 page/size/total/records |
| Bug: 元数据接口不需要登录 | 筛选选项不返回401 |
| Bug: 统计接口不需要登录 | 图表统计不返回401 |

**核心测试思路**：通过真实 HTTP 请求验证 API 层面的权限控制（公开接口 vs 需登录接口）、分页参数限制、XSS/SQL 注入防护和统一响应格式。

## 测试覆盖范围

| 类别 | Bug 数量 | 覆盖维度 |
|------|---------|---------|
| 认证回归 | 2 | SQL注入防护、XSS清洗 |
| 反馈回归 | 2 | 日志安全、输入校验空值 |
| 分页回归 | 1 | 参数边界约束(参数化测试9种值) |
| XSS回归 | 1 | 12种攻击变体+SQL注入 |
| 通用回归 | 6 | 分页/极端值/特殊字符/URL安全 |
| API回归 | 14 | 权限控制/分页限制/注入防护/响应格式 |

## 依赖关系

- `AuthRegressionTest`、`FeedbackRegressionTest`、`PaginationRegressionTest`、`XssRegressionTest`、`GeneralRegressionTest` 为纯工具类测试，无 Spring 上下文依赖
- `ControllerRegressionTest` 继承 `BaseIntegrationTest`，依赖完整 Spring 上下文和测试数据库
- 回归测试与 `integration/` 测试互补：集成测试验证正常流程，回归测试验证 Bug 修复
- 回归测试依赖 `common/util/` 中的 `PageUtils`、`XssUtils` 等工具类
