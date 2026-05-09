# 控制器层测试 (`controller`)

## 目录定位

本目录包含项目所有前台 API 控制器的单元测试，验证 `com.dongmedicine.controller` 包下各 Controller 的请求处理逻辑。采用 Mockito 隔离 Service 层依赖，专注测试 Controller 层的参数接收、权限校验、业务委托和响应封装。

## 测试设计思路

所有 Controller 测试遵循统一模式：

1. **Mock 隔离**：使用 `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + `@Mock`，Controller 被注入，Service 被 Mock
2. **静态方法 Mock**：通过 `MockedStatic<SecurityUtils>` 模拟登录状态，`setUp()` 中默认未登录，按需覆盖为已登录
3. **生命周期管理**：`@BeforeEach` 初始化 Mock 和测试数据，`@AfterEach` 关闭 MockedStatic
4. **验证维度**：成功路径（返回码200、数据正确）、异常路径（assertThrows）、权限校验（未登录抛异常）、边界条件

## 文件清单

### PlantControllerTest.java - 药用植物控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 列表查询 | 无筛选/带关键词和分类的高级搜索 |
| 搜索 | 关键词搜索分页 |
| 详情 | 存在的植物返回详情，不存在抛 BusinessException |
| 相似植物 | 根据ID获取相似植物列表 |
| 随机植物 | 获取随机推荐植物 |
| 批量查询 | 成功/空列表/超过50条限制 |
| 浏览量递增 | 调用 service.incrementViewCount |

### KnowledgeControllerTest.java - 知识库控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 列表查询 | 无筛选/带疗法分类和关键词的高级搜索 |
| 搜索 | 关键词搜索 |
| 详情 | 未登录不记录浏览历史，已登录记录浏览历史+增加热度，不存在抛异常 |
| 浏览量递增 | 调用 service.incrementViewCount |
| 收藏 | 已登录调用 service.addFavorite |
| 反馈 | 成功提交/内容超过500字符抛异常 |

### InheritorControllerTest.java - 传承人控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 列表查询 | 无筛选/带级别和姓名筛选 |
| 搜索 | 关键词搜索分页 |
| 详情 | 未登录只增加热度，已登录额外记录浏览历史，不存在抛异常 |
| 浏览量递增 | 调用 service.incrementViewCount |

### QaControllerTest.java - 非遗问答控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 列表查询 | 无筛选/带分类和关键词 |
| 搜索 | 关键词搜索 |
| 详情 | 返回问答详情+增加热度，不存在抛异常 |
| 浏览量递增 | 调用 service.incrementViewCount |

### QuizControllerTest.java - 测验控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 随机题目 | 获取指定数量题目，count=50正常处理 |
| 题目列表分页 | 分页获取全部题目 |
| 答题记录 | 未登录返回空记录，已登录返回分页数据 |

### FavoriteControllerTest.java - 收藏控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 添加收藏 | 已登录成功(plant/knowledge类型)，未登录抛异常 |
| 取消收藏 | 已登录成功(plant/knowledge类型)，未登录抛异常 |
| 我的收藏 | 已登录分页查询，自定义分页参数，未登录抛异常 |

### CommentControllerTest.java - 评论控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 列表查询 | 按目标类型和ID分页查询已审核评论 |
| 全部评论列表 | 分页获取全部已审核评论 |
| 发表评论 | 已登录成功/带回复信息/未登录抛异常 |
| 我的评论 | 已登录分页查询/未登录抛异常 |

### FeedbackControllerTest.java - 反馈控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 提交反馈 | 缺少必填字段返回400（使用 @WebMvcTest + MockMvc） |
| 反馈统计 | 无需登录返回统计数据 |

**特殊说明**：此测试使用 `@WebMvcTest` + `MockMvc` 进行 Web 层切片测试，而非 Mockito 单元测试。

### FileUploadControllerTest.java - 文件上传控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 上传图片 | 成功/服务返回失败/服务抛异常 |
| 批量上传图片 | 成功/服务抛异常 |
| 上传视频 | 成功/服务返回失败/服务抛异常 |
| 批量上传视频 | 成功/服务抛异常 |
| 上传文档 | 成功/服务返回失败/服务抛异常 |
| 批量上传文档 | 成功/服务抛异常 |
| 上传文件 | 成功/服务返回失败/服务抛异常 |
| 删除文件 | 成功/删除失败/服务抛异常 |

### BrowseHistoryControllerTest.java - 浏览历史控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 我的浏览历史 | 已登录成功/默认limit/limit上限截断/未登录抛异常 |
| 记录浏览历史 | 已登录成功/未登录抛异常 |

### ChatControllerTest.java - AI聊天控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 统计信息 | 初始状态/记录成功请求后/记录失败请求后/混合请求记录 |

**特殊说明**：通过反射重置 `ChatController` 的静态 AtomicLong 计数器，验证请求统计功能。

### ChatHistoryControllerTest.java - 聊天历史控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 会话列表 | 已登录成功/未登录抛异常 |
| 会话消息 | 已登录成功/未登录抛异常 |
| 删除会话 | 已登录成功/未登录抛异常 |

### CaptchaControllerTest.java - 验证码控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 获取验证码 | 成功返回 captchaKey 和 captchaImage |

### ExportControllerTest.java - 数据导出控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 导出植物数据 | 成功返回CSV（含BOM），内容包含植物名称 |
| 导出不支持的实体 | 返回400 |
| 导出不支持的格式 | 返回400 |
| 导出用户数据 | 成功且排除 passwordHash 字段 |
| 导出问答/测验/知识/传承人/资源/评论/反馈数据 | 各类型成功导出 |
| 导出空列表 | 成功返回空CSV |

### SearchControllerTest.java - 搜索建议控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 搜索建议 | 有关键词返回结果/空/null/空白关键词返回空列表/结果限制15条/搜索历史写入失败不影响结果 |
| 热门关键词 | 成功返回/limit超过50限制为50/默认limit为20 |

### LeaderboardControllerTest.java - 排行榜控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 综合排行榜 | 默认排序(总分)/按答题排序/空数据/limit边界值/limit上限200 |
| 答题排行榜 | 成功/空数据/null limit使用默认值 |
| 游戏排行榜 | 成功/空数据/null limit使用默认值 |

### MetadataControllerTest.java - 元数据控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 全部筛选条件 | 成功返回5个模块的筛选选项 |
| 精选内容 | 有数据/直接调用 getAllFiltersData/直接调用 getFeaturedData(有数据/空数据) |

### NotificationControllerTest.java - 消息通知控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 通知列表 | 未登录返回空列表/已登录有数据/已登录无数据 |
| 未读数量 | 未登录返回0/已登录有未读/已登录无未读 |
| 标记全部已读 | 未登录返回ok/已登录删除未读key |

### OperationLogControllerTest.java - 操作日志控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 日志列表 | 无筛选/带模块/带类型/带用户名/全部筛选/limit超上限截断100 |
| 根据ID查询 | 成功/不存在返回null |
| 删除/批量删除/清空所有 | 各操作成功 |
| 日志统计 | 有数据/空数据，统计 CREATE/UPDATE/DELETE/QUERY 各类型数量 |

### ResourceControllerTest.java - 学习资源控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 列表查询 | 无筛选/带分类和关键词和文件类型 |
| 热门资源 | 成功返回 |
| 搜索 | 关键词搜索 |
| 详情 | 成功/不存在抛异常 |
| 浏览量/下载量递增 | 各操作成功 |
| 文件类型/分类列表 | 返回类型信息和分类列表 |

### StatisticsControllerTest.java - 数据统计控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 数据概览 | 返回各模块计数和疗法分类数 |
| 图表数据 | 返回完整图表数据(9个维度)/药方数据为空时回退到 topByViewCount |
| 访问趋势 | 成功返回7天趋势/空数据全0 |
| 各模块统计 | 植物/知识/问答/传承人/资源各自统计 |

### UserControllerTest.java - 用户控制器测试

| 测试场景 | 测试逻辑 |
|---------|---------|
| 登录 | 成功返回token/验证码失败 |
| 注册 | 成功/两次密码不一致 |
| 获取当前用户信息 | 已登录成功/未登录抛异常 |
| 修改密码 | 已登录成功(含StpUtil.logout)/未登录抛异常 |
| 退出登录 | 成功 |
| 验证登录状态 | 已登录valid=true/未登录valid=false |
| 刷新Token | 已登录成功/未登录抛异常 |

## 依赖关系

- 所有 Controller 测试依赖 `com.dongmedicine.common.R`（统一响应封装）
- 大部分测试依赖 `SecurityUtils`（静态方法 Mock 模拟登录状态）
- 部分测试依赖 `BusinessException`/`ErrorCode`（验证异常抛出）
- `ExportControllerTest` 依赖多个 Service 和 ObjectMapper
- `StatisticsControllerTest` 和 `MetadataControllerTest` 依赖 Mapper 层直接查询
- `FeedbackControllerTest` 使用 `@WebMvcTest` Web 层切片测试
