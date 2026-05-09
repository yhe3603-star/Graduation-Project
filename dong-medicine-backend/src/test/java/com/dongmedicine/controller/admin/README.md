# 后台管理控制器测试 (`controller.admin`)

## 目录定位

本目录包含项目后台管理系统的控制器单元测试，验证 `com.dongmedicine.controller.admin` 包下的4个管理控制器。这些控制器为管理员提供内容管理、互动管理、统计分析和用户管理功能，是后台管理系统的核心 API 层。

## 测试设计思路

与前台 Controller 测试保持一致的模式：
- 使用 `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + `@Mock`
- 使用 `@Nested` + `@DisplayName` 按业务模块分组测试
- 验证 CRUD 操作的正确性和缓存清除逻辑
- 管理端操作不涉及登录态 Mock（权限校验由 Sa-Token 拦截器处理）

## 文件清单

### AdminContentControllerTest.java - 后台内容管理控制器测试

按业务模块使用 `@Nested` 分组，覆盖5大内容模块的 CRUD 操作：

| 模块 | 测试场景 | 测试逻辑 |
|------|---------|---------|
| 传承人管理 | 列表/新增/更新/删除 | 分页查询、save后clearCache、updateById后clearCache、deleteWithFiles |
| 知识库管理 | 列表/新增/更新/删除 | 分页查询、save后clearCache、updateById后clearCache、deleteWithFiles |
| 药用植物管理 | 列表/新增/更新/删除 | 分页查询、save后clearCache、updateById后clearCache、deleteWithFiles |
| 问答管理 | 列表/新增/更新/删除 | 分页查询、save、updateById、removeById |
| 学习资源管理 | 列表/新增/更新/删除 | 分页查询、save后clearCache、updateById后clearCache、deleteWithFiles |

**核心测试思路**：

1. **CRUD 完整性**：每个模块都验证列表查询、新增、更新、删除四个操作
2. **缓存清除**：新增和更新操作后验证 `clearCache()` 被调用，确保前端数据一致性
3. **文件清理**：删除操作使用 `deleteWithFiles()` 而非 `removeById()`，确保关联文件被清理
4. **DTO 转换**：验证各 CreateDTO/UpdateDTO 到 Entity 的转换

### AdminInteractionControllerTest.java - 后台互动管理控制器测试

按业务模块使用 `@Nested` 分组：

| 模块 | 测试场景 | 测试逻辑 |
|------|---------|---------|
| 反馈管理 | 列表(全部状态)/列表(按状态筛选)/回复/删除 | 分页查询、replyFeedback、removeById |
| 评论管理 | 列表(全部状态)/列表(按状态筛选)/审核通过/拒绝/删除 | pageAllDTO、approveComment、rejectComment、removeById |

**核心测试思路**：

1. **状态筛选**：反馈和评论都支持按状态（all/pending等）筛选
2. **审核流程**：评论支持 approve/reject 两种审核操作
3. **回复功能**：管理员可回复用户反馈

### AdminStatsControllerTest.java - 后台统计控制器测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testStats_Success` | 获取9个模块的统计数据（users/knowledge/inheritors/plants/qa/resources/quiz/comments/feedback） |
| `testGetUserGrowth_Success` | 获取用户增长趋势（日期+计数），验证 dates/counts 列表 |
| `testGetUserGrowth_EmptyData` | 空数据返回空列表 |
| `testGetContentViews_Success` | 获取内容浏览量排行，按浏览量降序排列（植物>知识>传承人） |
| `testGetSearchKeywords_Success` | 获取搜索热词，调用 searchHistoryMapper.topKeywords(10) |

**核心测试思路**：

1. **多维度统计**：覆盖用户、内容、互动等9个维度的计数
2. **趋势数据**：验证时间序列数据的格式（dates + counts）
3. **排行排序**：内容浏览量排行按 value 降序
4. **空数据容错**：增长趋势为空时返回空列表而非报错

### AdminUserControllerTest.java - 后台用户管理控制器测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testListUsers_Success` | 用户列表查询，验证 passwordHash 被清空（安全防护） |
| `testDeleteUser_Success` | 删除用户（调用 deleteUser 而非 removeById） |
| `testUpdateUserRole_Success` | 更新用户角色 |
| `testBanUser_Success` | 封禁用户（带原因） |
| `testBanUser_NoReason` | 封禁用户（无原因） |
| `testUnbanUser_Success` | 解封用户 |

**核心测试思路**：

1. **敏感信息清除**：列表查询后验证 passwordHash 被设为 null，防止密码泄露
2. **封禁/解封**：支持带原因和无原因两种封禁方式
3. **安全删除**：使用 deleteUser 而非直接 removeById，可能包含关联数据清理

## 测试覆盖范围

| 控制器 | 覆盖模块 | 测试方法数 |
|--------|---------|-----------|
| AdminContentController | 传承人/知识库/植物/问答/资源 CRUD | 20 |
| AdminInteractionController | 反馈管理/评论管理 | 9 |
| AdminStatsController | 统计概览/用户增长/浏览排行/搜索热词 | 5 |
| AdminUserController | 用户列表/删除/角色更新/封禁/解封 | 6 |

## 依赖关系

- `AdminContentControllerTest` 依赖5个 Service（InheritorService/KnowledgeService/PlantService/QaService/ResourceService）及对应的 CreateDTO/UpdateDTO
- `AdminInteractionControllerTest` 依赖 FeedbackService 和 CommentService
- `AdminStatsControllerTest` 依赖8个 Service + UserMapper + SearchHistoryMapper
- `AdminUserControllerTest` 依赖 UserService
- 所有管理控制器测试不依赖 SecurityUtils（权限由 Sa-Token 拦截器在运行时校验）
