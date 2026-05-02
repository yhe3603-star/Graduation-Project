# Controller 层 -- 控制器（24个）

> Controller 是三层架构的"入口层"，负责接收HTTP请求、参数校验、调用Service、用R\<T\>统一封装响应。
> 每个Controller = 一个URL前缀 + 多个HTTP方法接口。

---

## 一、Controller 清单（24个）

| # | Controller | 路径前缀 | 权限 | 核心职责 |
|---|-----------|---------|------|---------|
| 1 | **PlantController** | `/api/plants` | 公开 | 药用植物列表/搜索/详情/相似/随机/浏览量 |
| 2 | **KnowledgeController** | `/api/knowledge` | 公开 + @SaCheckLogin | 知识库列表/搜索/详情/收藏/反馈 |
| 3 | **InheritorController** | `/api/inheritors` | 公开 | 传承人列表/搜索/详情/浏览量 |
| 4 | **UserController** | `/api/user` | 公开 + @SaCheckLogin | 登录/注册/个人信息/改密/登出/Token验证 |
| 5 | **AdminController** | `/api/admin` | @SaCheckRole("admin") | 统一管理后台（用户/植物/知识/传承人/问答/资源/反馈/评论） |
| 6 | **CommentController** | `/api/comments` | 公开 + @SaCheckLogin | 评论发表/列表/我的评论 |
| 7 | **FavoriteController** | `/api/favorites` | @SaCheckLogin | 收藏/取消收藏/我的收藏 |
| 8 | **QuizController** | `/api/quiz` | 公开 + @SaCheckRole("admin") | 随机题目/提交答案/记录/题目CRUD |
| 9 | **ResourceController** | `/api/resources` | 公开 | 资源列表/搜索/热门/下载/文件类型 |
| 10 | **FeedbackController** | `/api/feedback` | 公开 + @SaCheckLogin | 提交反馈/我的反馈/反馈统计 |
| 11 | **QaController** | `/api/qa` | 公开 | 问答列表/搜索/浏览量 |
| 12 | **SearchController** | `/api/search` | 公开 | 搜索建议（植物+知识+传承人） |
| 13 | **StatisticsController** | `/api/stats` | 公开 | 访问趋势/各模块统计 |
| 14 | **StatsController** | `/api/stats` | 公开 | 图表数据（分类/分布/热门） |
| 15 | **LeaderboardController** | `/api/leaderboard` | 公开 | 答题/游戏排行榜 |
| 16 | **PlantGameController** | `/api/plant-game` | 公开 | 植物识别游戏提交/记录 |
| 17 | **CaptchaController** | `/api/captcha` | 公开 | 验证码生成 |
| 18 | **FileUploadController** | `/api/upload` | @SaCheckRole("admin") | 文件上传（图片/视频/文档） |
| 19 | **ExportController** | `/api/admin/export` | @SaCheckRole("admin") | 数据导出CSV |
| 20 | **ChatController** | `/api/chat` | 公开 | AI聊天统计 |
| 21 | **ChatHistoryController** | `/api/chat-history` | @SaCheckLogin | 聊天历史会话/消息 |
| 22 | **BrowseHistoryController** | `/api/browse-history` | @SaCheckLogin | 浏览历史/记录浏览 |
| 23 | **OperationLogController** | `/api/admin/logs` | @SaCheckRole("admin") | 操作日志查询/删除/统计 |
| 24 | **MetadataController** | `/api/metadata` | 公开 | 全平台筛选选项元数据 |

---

## 二、各个Controller的API端点

### 2.1 PlantController -- 药用植物（/api/plants）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/api/plants/list` | 植物列表（分页+筛选） | page, size, category, usageWay, keyword |
| GET | `/api/plants/search` | 搜索植物 | keyword(必填), page, size |
| GET | `/api/plants/{id}` | 植物详情（含故事，自动记录浏览历史） | id(路径) |
| GET | `/api/plants/{id}/similar` | 相似植物（同分类，最多4个） | id(路径) |
| GET | `/api/plants/random` | 随机植物 | limit(1-100, 默认20) |
| POST | `/api/plants/batch` | 批量获取植物 | Body: List\<Integer\> ids(最多50) |
| POST | `/api/plants/{id}/view` | 增加浏览量 | id(路径), @RateLimit(10) |

### 2.2 KnowledgeController -- 知识库（/api/knowledge）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/api/knowledge/list` | 知识列表（高级筛选） | page, size, sortBy, keyword, therapy, disease, herb |
| GET | `/api/knowledge/search` | 搜索知识 | keyword(必填), therapy, disease, herb, sortBy, page, size |
| GET | `/api/knowledge/{id}` | 知识详情（含相关内容） | id(路径) |
| POST | `/api/knowledge/{id}/view` | 增加浏览量 | id(路径), @RateLimit(10) |
| POST | `/api/knowledge/favorite/{id}` | 收藏知识 | id(路径), @SaCheckLogin |
| POST | `/api/knowledge/feedback` | 知识反馈 | knowledgeId, content(最多500字符) |

### 2.3 InheritorController -- 传承人（/api/inheritors）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/api/inheritors/list` | 传承人列表 | page, size, level, sortBy |
| GET | `/api/inheritors/search` | 搜索传承人 | keyword, page, size |
| GET | `/api/inheritors/{id}` | 传承人详情（含扩展信息） | id(路径) |
| POST | `/api/inheritors/{id}/view` | 增加浏览量 | id(路径), @RateLimit(10) |

### 2.4 UserController -- 用户认证（/api/user）

| 方法 | 路径 | 说明 | 限流/权限 |
|------|------|------|----------|
| POST | `/api/user/login` | 用户登录（验证码校验 → 密码校验 → 生成Token） | @RateLimit(5) |
| POST | `/api/user/register` | 用户注册（验证码校验 → 密码一致性 → 密码强度） | @RateLimit(3) |
| GET | `/api/user/me` | 获取当前用户信息 | @SaCheckLogin |
| POST | `/api/user/change-password` | 修改密码（验证码 + 旧密码校验 → 登出） | @SaCheckLogin |
| POST | `/api/user/logout` | 退出登录 | 公开 |
| GET | `/api/user/validate` | 验证Token有效性 | 公开 |
| POST | `/api/user/refresh-token` | 刷新Token（延长有效期） | 公开 |

### 2.5 AdminController -- 后台管理（/api/admin）

整个Controller用 `@SaCheckRole("admin")` 保护。

#### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表（分页，密码字段置null） |
| DELETE | `/api/admin/users/{id}` | 删除用户 |
| PUT | `/api/admin/users/{id}/role` | 修改角色（?role=admin/user） |
| PUT | `/api/admin/users/{id}/ban` | 封禁用户（?reason=原因） |
| PUT | `/api/admin/users/{id}/unban` | 解封用户 |

#### 传承人管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/inheritors` | 传承人列表 |
| POST | `/api/admin/inheritors` | 新增传承人（@Valid InheritorCreateDTO） |
| PUT | `/api/admin/inheritors/{id}` | 更新传承人（@Valid InheritorUpdateDTO） |
| DELETE | `/api/admin/inheritors/{id}` | 删除传承人（含关联文件） |

#### 知识管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/knowledge` | 知识列表 |
| POST | `/api/admin/knowledge` | 新增知识（@Valid KnowledgeCreateDTO） |
| PUT | `/api/admin/knowledge/{id}` | 更新知识（@Valid KnowledgeUpdateDTO） |
| DELETE | `/api/admin/knowledge/{id}` | 删除知识（含关联文件） |

#### 植物管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/plants` | 植物列表 |
| POST | `/api/admin/plants` | 新增植物（@Valid PlantCreateDTO） |
| PUT | `/api/admin/plants/{id}` | 更新植物（@Valid PlantUpdateDTO） |
| DELETE | `/api/admin/plants/{id}` | 删除植物（含关联文件） |

#### 问答管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/qa` | 问答列表 |
| POST | `/api/admin/qa` | 新增问答（@Valid QaCreateDTO） |
| PUT | `/api/admin/qa/{id}` | 更新问答（@Valid QaUpdateDTO） |
| DELETE | `/api/admin/qa/{id}` | 删除问答 |

#### 资源管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/resources` | 资源列表 |
| POST | `/api/admin/resources` | 新增资源（@Valid ResourceCreateDTO） |
| PUT | `/api/admin/resources/{id}` | 更新资源（@Valid ResourceUpdateDTO） |
| DELETE | `/api/admin/resources/{id}` | 删除资源（含关联文件） |

#### 反馈管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/feedback` | 反馈列表（?status=all/pending/resolved） |
| PUT | `/api/admin/feedback/{id}/reply` | 回复反馈（@Valid FeedbackReplyDTO） |
| DELETE | `/api/admin/feedback/{id}` | 删除反馈 |

#### 评论管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/comments` | 评论列表（?status=all/approved/rejected） |
| PUT | `/api/admin/comments/{id}/approve` | 审核通过 |
| PUT | `/api/admin/comments/{id}/reject` | 审核拒绝 |
| DELETE | `/api/admin/comments/{id}` | 删除评论 |

#### 统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats` | 仪表盘统计（用户/知识/传承人/植物/问答/资源/测验/评论/反馈 总数） |
| GET | `/api/admin/stats/plants-distribution` | 植物分布统计 |
| GET | `/api/admin/stats/knowledge-popularity` | 知识热度Top10 |

### 2.6 CommentController -- 评论（/api/comments）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/comments` | 发表评论（支持嵌套回复） | @SaCheckLogin |
| GET | `/api/comments/list/{targetType}/{targetId}` | 获取评论列表（手动分页） | 公开 |
| GET | `/api/comments/list/all` | 获取所有已审核评论 | 公开 |
| GET | `/api/comments/my` | 我的评论 | @SaCheckLogin |

### 2.7 FavoriteController -- 收藏（/api/favorites）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/favorites/{targetType}/{targetId}` | 添加收藏 | @SaCheckLogin |
| DELETE | `/api/favorites/{targetType}/{targetId}` | 取消收藏 | @SaCheckLogin |
| GET | `/api/favorites/my` | 我的收藏（手动分页） | @SaCheckLogin |

### 2.8 QuizController -- 趣味答题（/api/quiz）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/quiz/questions` | 随机获取题目（?count=10&scorePerQuestion=10） | 公开 |
| POST | `/api/quiz/submit` | 提交答案（?scorePerQuestion=10） | 公开 |
| GET | `/api/quiz/records` | 我的答题记录 | 公开（未登录返回空） |
| GET | `/api/quiz/list` | 题目列表（后台管理） | 公开 |
| POST | `/api/quiz/add` | 新增题目 | @SaCheckRole("admin") |
| PUT | `/api/quiz/update` | 更新题目 | @SaCheckRole("admin") |
| DELETE | `/api/quiz/{id}` | 删除题目 | @SaCheckRole("admin") |

### 2.9 ResourceController -- 学习资源（/api/resources）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/resources/list` | 资源列表（?category=&keyword=&fileType=） |
| GET | `/api/resources/hot` | 热门资源 |
| GET | `/api/resources/search` | 搜索资源 |
| GET | `/api/resources/{id}` | 资源详情 |
| GET | `/api/resources/download/{id}` | 下载资源文件（流式传输） |
| POST | `/api/resources/batch-download` | 批量下载（ZIP打包） |
| POST | `/api/resources/{id}/download` | 增加下载计数 |
| POST | `/api/resources/{id}/view` | 增加浏览量 |
| GET | `/api/resources/types` | 文件类型列表 |
| GET | `/api/resources/categories` | 资源分类列表 |

### 2.10 FeedbackController -- 用户反馈（/api/feedback）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/feedback` | 提交反馈 | 公开（支持匿名） |
| GET | `/api/feedback/my` | 我的反馈 | @SaCheckLogin |
| GET | `/api/feedback/stats` | 反馈统计（总数/待处理/已解决） | 公开 |

### 2.11 QaController -- 非遗问答（/api/qa）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/qa/list` | 问答列表（?category=） |
| GET | `/api/qa/search` | 搜索问答 |
| POST | `/api/qa/{id}/view` | 增加浏览量 |

### 2.12 SearchController -- 搜索建议（/api/search）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/search/suggest` | 搜索建议（植物+知识+传承人，按相关性排序，最多15条） |

### 2.13 StatisticsController -- 数据统计（/api/stats）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stats/trend` | 访问趋势（最近7天） |
| GET | `/api/stats/plants` | 植物统计 |
| GET | `/api/stats/knowledge` | 知识统计 |
| GET | `/api/stats/qa` | 问答统计 |
| GET | `/api/stats/inheritors` | 传承人统计 |
| GET | `/api/stats/resources` | 资源统计 |

### 2.14 StatsController -- 图表统计（/api/stats）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stats/chart` | 综合图表数据（分类/分布/热门/趋势） |

### 2.15 LeaderboardController -- 排行榜（/api/leaderboard）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/leaderboard/combined` | 综合排行榜（答题+游戏，?sortBy=total/quiz/game） |
| GET | `/api/leaderboard/quiz` | 答题排行榜 |
| GET | `/api/leaderboard/game` | 游戏排行榜 |

### 2.16 PlantGameController -- 植物识别游戏（/api/plant-game）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/plant-game/submit` | 提交游戏结果 |
| GET | `/api/plant-game/records` | 我的游戏记录 |

### 2.17 CaptchaController -- 验证码（/api/captcha）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/captcha` | 获取验证码（返回captchaKey + Base64图片） |

### 2.18 FileUploadController -- 文件上传（/api/upload）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/upload/image` | 上传图片（?category=） |
| POST | `/api/upload/images` | 批量上传图片 |
| POST | `/api/upload/video` | 上传视频 |
| POST | `/api/upload/videos` | 批量上传视频 |
| POST | `/api/upload/document` | 上传文档 |
| POST | `/api/upload/documents` | 批量上传文档 |
| POST | `/api/upload/file` | 通用文件上传（?category=&subDir=） |
| DELETE | `/api/upload` | 删除文件（?filePath=） |

### 2.19 ExportController -- 数据导出（/api/admin/export）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/export/{entity}` | 导出CSV（entity=users/plants/knowledge/inheritors/resources/qa/comments/feedback/quiz-questions） |

### 2.20 ChatController -- AI聊天（/api/chat）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/chat/stats` | 聊天统计（总请求/成功/失败） |

### 2.21 ChatHistoryController -- 聊天历史（/api/chat-history）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/chat-history/sessions` | 我的会话列表 | @SaCheckLogin |
| GET | `/api/chat-history/sessions/{sessionId}` | 会话消息列表 | @SaCheckLogin |
| DELETE | `/api/chat-history/sessions/{sessionId}` | 删除会话 | @SaCheckLogin |

### 2.22 BrowseHistoryController -- 浏览历史（/api/browse-history）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/browse-history/my` | 我的浏览历史（?limit=50） | @SaCheckLogin |
| POST | `/api/browse-history/record` | 手动记录浏览（?targetType=&targetId=） | @SaCheckLogin |

### 2.23 OperationLogController -- 操作日志（/api/admin/logs）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/logs/list` | 日志列表（?module=&type=&username=&limit=100） |
| GET | `/api/admin/logs/{id}` | 日志详情 |
| DELETE | `/api/admin/logs/{id}` | 删除日志 |
| DELETE | `/api/admin/logs/batch` | 批量删除 |
| DELETE | `/api/admin/logs/clear` | 清空所有日志 |
| GET | `/api/admin/logs/stats` | 日志统计（按操作类型分组） |

### 2.24 MetadataController -- 元数据（/api/metadata）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/metadata/filters` | 全平台筛选选项（植物/知识/问答/传承人/资源的分类筛选选项） |

---

## 三、Controller编程规范

### 注解模板

```java
@Tag(name = "模块中文名", description = "模块描述")    // Swagger分组
@RestController                                        // REST控制器
@RequestMapping("/api/xxx")                            // 路径前缀
@Validated                                             // 启用参数校验
@RequiredArgsConstructor                                // 构造器注入
public class XxxController {
    private final XxxService service;                  // final字段，构造器注入
    // ...
}
```

### 参数获取方式

```java
// @PathVariable: 从URL路径提取
@GetMapping("/{id}")
public R<Entity> detail(@PathVariable @NotNull Integer id) { ... }

// @RequestParam: 从URL查询字符串提取
@GetMapping("/list")
public R<Map> list(@RequestParam(defaultValue = "1") Integer page,
                   @RequestParam(required = false) String keyword) { ... }

// @RequestBody: 从JSON请求体提取
@PostMapping
public R<String> create(@Valid @RequestBody CreateDTO dto) { ... }
```

### 分页返回统一格式

```java
// PlantController中使用PageUtils
Page<Plant> pageResult = service.advancedSearchPaged(...);
return R.ok(PageUtils.toMap(pageResult));

// 返回结构: {"code":200, "data": {"records":[...], "total":65, "page":1, "size":12}}
```

### 手动分页（部分Controller使用）

```java
// CommentController、FavoriteController等使用手动分页
int safeSize = Math.min(Math.max(size, 1), 100);
List<DTO> all = service.listXxx();
int start = (page - 1) * safeSize;
int end = Math.min(start + safeSize, all.size());
return R.ok(start < all.size() ? all.subList(start, end) : List.of());
```

---

## 四、权限控制说明

本项目使用 **Sa-Token** 权限框架：

| 控制方式 | 说明 |
|---------|------|
| `@SaCheckLogin`（方法级） | 要求登录，未登录返回401 |
| `@SaCheckRole("admin")`（类级/方法级） | 要求admin角色，权限不足返回403 |
| 无注解 | 公开接口，GET请求免登录 |
| SaTokenConfig排除路径 | 在excludePathPatterns中配置的写操作路径也免登录 |
| WRITE_METHODS拦截 | POST/PUT/DELETE/PATCH方法默认需要登录（除非在排除列表） |
