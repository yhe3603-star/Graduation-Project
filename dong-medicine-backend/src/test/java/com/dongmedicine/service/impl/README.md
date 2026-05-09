# 服务实现层测试 (`service.impl`)

## 目录定位

本目录包含项目所有 Service 实现类的单元测试，验证 `com.dongmedicine.service.impl` 包下的核心业务逻辑。Service 层是系统的业务核心，承载了数据查询、分页排序、异常处理、文件清理、缓存管理、密码加密、异步操作等关键逻辑。

## 测试设计思路

Service 层测试面临的核心挑战是 MyBatis-Plus 的 `ServiceImpl` 依赖注入问题。由于 `ServiceImpl<Mapper, Entity>` 的 `baseMapper` 字段是 private 且无 setter，测试采用**反射注入**方式解决：

```java
private void setBaseMapper(Object service, Object mapper) throws Exception {
    Class<?> clazz = service.getClass();
    while (clazz != null) {
        try {
            Field field = clazz.getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(service, mapper);
            return;
        } catch (NoSuchFieldException e) {
            clazz = clazz.getSuperclass();
        }
    }
}
```

部分测试还使用 `@MockitoSettings(strictness = Strictness.LENIENT)` 避免未使用 Mock 的严格模式报错。

## 文件清单

### UserServiceImplTest.java - 用户服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 用户登录 | 正确密码登录成功返回token/错误密码抛异常/用户不存在抛异常 |
| 用户注册 | 正常注册成功/用户名已存在抛异常/密码不符合策略抛异常 |
| 密码修改 | 正确旧密码修改成功/旧密码错误抛异常 |
| 用户信息 | 获取用户信息/用户不存在抛异常 |
| 角色管理 | 更新用户角色 |
| 封禁/解封 | 封禁用户/解封用户 |
| 删除用户 | 删除用户 |

**特殊说明**：使用真实 `BCryptPasswordEncoder` 验证密码加密/校验逻辑，而非 Mock。

### PlantServiceImplTest.java - 药用植物服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 高级搜索分页 | 关键词+分类+用法组合查询 |
| 搜索分页 | 关键词搜索 |
| 详情查询 | 含民间故事的详情/不存在抛异常 |
| 相似植物 | 按分类查询相似植物 |
| 随机植物 | 获取随机推荐 |
| 浏览量递增 | 调用 mapper 更新 |
| 删除含文件 | 删除记录并清理关联文件 |

### KnowledgeServiceImplTest.java - 知识库服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 高级搜索分页 | 多条件组合查询（关键词/疗法分类/类型/难度/排序） |
| 详情查询 | 含关联数据的详情/不存在抛异常 |
| 收藏/反馈 | 添加收藏/提交反馈 |
| 浏览量递增 | 调用 mapper 更新 |
| 删除含文件 | 删除记录并清理关联文件+清除缓存 |

**特殊说明**：使用 AssertJ 的 `assertThat`/`assertThatThrownBy` 风格断言。

### InheritorServiceImplTest.java - 传承人服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 高级搜索分页 | 姓名+级别组合查询 |
| 搜索分页 | 关键词搜索 |
| 详情查询 | 含附加信息的详情/不存在抛异常 |
| 浏览量递增 | 调用 mapper 更新 |
| 删除含文件 | 删除记录并清理关联文件 |

### QaServiceImplTest.java - 问答服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 高级搜索分页 | 关键词+分类组合查询 |
| 搜索分页 | 关键词搜索 |
| 详情查询 | 获取详情/不存在抛异常 |
| 浏览量递增 | 调用 mapper 更新 |

### QuizServiceImplTest.java - 测验服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 获取随机题目 | 指定数量/超过总数返回全部 |
| 提交答案 | 正确答案/错误答案/部分正确 |
| 答题记录 | 保存用户答题记录 |
| 题目分页 | 分页获取全部题目 |

**特殊说明**：使用 AssertJ 断言 + `@Nested` 分组 + `ArgumentCaptor` 验证保存的对象字段。

### ResourceServiceImplTest.java - 学习资源服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 分类关键词类型分页 | 多条件组合查询 |
| 热门资源 | 按热度排序 |
| 详情查询 | 获取详情 |
| 浏览量/下载量递增 | 调用 mapper 更新 |
| 分类列表 | 获取所有分类 |

### CommentServiceImplTest.java - 评论服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 添加评论 | 普通评论/回复评论 |
| 审核评论 | 通过/拒绝 |
| 分页查询 | 按目标/按用户/全部已审核 |
| 删除评论 | 删除评论 |

### FavoriteServiceImplTest.java - 收藏服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 添加收藏 | 成功/重复收藏抛异常 |
| 取消收藏 | 成功/未收藏抛异常 |
| 我的收藏 | 分页查询/按类型筛选 |
| 收藏状态 | 检查是否已收藏 |

### FeedbackServiceImplTest.java - 反馈服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 提交反馈 | 成功/内容为空抛异常 |
| 回复反馈 | 管理员回复 |
| 反馈统计 | 总数/待处理/已解决 |
| 分页查询 | 按状态筛选 |

### BrowseHistoryServiceImplTest.java - 浏览历史服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 记录浏览 | 成功记录 |
| 我的浏览历史 | 分页查询/按类型筛选 |
| 清除历史 | 清除指定/全部历史 |

### ChatHistoryServiceImplTest.java - 聊天历史服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 保存消息 | 成功保存 |
| 获取会话列表 | 按用户查询 |
| 获取会话消息 | 按会话ID查询 |
| 删除会话 | 成功删除 |

### AiChatServiceImplTest.java - AI聊天服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 发送消息 | 成功/服务异常处理 |
| 流式响应 | 验证流式返回 |

### FileUploadServiceImplTest.java - 文件上传服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 上传图片 | 成功/文件类型不允许/文件过大 |
| 上传视频 | 成功/文件类型不允许 |
| 上传文档 | 成功/文件类型不允许 |
| 删除文件 | 成功/文件不存在 |

**特殊说明**：使用真实临时目录（`Files.createTempDirectory`）测试文件写入，`@AfterEach` 清理临时文件。

### PlantGameServiceImplTest.java - 植物游戏服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 开始游戏 | 生成游戏题目 |
| 提交答案 | 正确/错误答案 |
| 游戏记录 | 保存游戏记录 |

### PopularityAsyncServiceImplTest.java - 热度异步服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 各模块热度递增 | 植物/知识/传承人/问答/资源的浏览量+热度递增 |

**核心测试思路**：验证异步方法正确委托给对应 Mapper 的自定义 SQL 方法。

### OperationLogServiceImplTest.java - 操作日志服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 保存日志 | 成功保存 |
| 查询日志 | 按条件筛选/分页 |
| 统计日志 | 按类型统计 |
| 清空日志 | 清空所有 |

### RabbitMQOperationLogServiceImplTest.java - RabbitMQ操作日志服务测试

| 测试维度 | 测试逻辑 |
|---------|---------|
| 异步保存日志 | 成功发送到 RabbitMQ/发送失败抛异常 |

**核心测试思路**：验证日志异步写入的消息队列委托逻辑。

## 测试覆盖范围

| 服务类 | 核心测试维度 |
|--------|-------------|
| UserServiceImpl | 登录/注册/密码修改/角色管理/封禁/删除 |
| PlantServiceImpl | 高级搜索/详情/相似/随机/删除含文件 |
| KnowledgeServiceImpl | 高级搜索/详情/收藏/反馈/删除含文件 |
| InheritorServiceImpl | 高级搜索/详情/删除含文件 |
| QaServiceImpl | 高级搜索/详情 |
| QuizServiceImpl | 随机题目/提交答案/答题记录 |
| ResourceServiceImpl | 分类查询/热门/详情/下载量 |
| CommentServiceImpl | 添加/审核/分页/删除 |
| FavoriteServiceImpl | 添加/取消/我的收藏/收藏状态 |
| FeedbackServiceImpl | 提交/回复/统计/分页 |
| BrowseHistoryServiceImpl | 记录/查询/清除 |
| ChatHistoryServiceImpl | 保存/会话列表/会话消息/删除 |
| AiChatServiceImpl | 发送消息/流式响应 |
| FileUploadServiceImpl | 图片/视频/文档上传/删除 |
| PlantGameServiceImpl | 开始游戏/提交答案/记录 |
| PopularityAsyncServiceImpl | 5个模块热度递增 |
| OperationLogServiceImpl | 保存/查询/统计/清空 |
| RabbitMQOperationLogServiceImpl | 异步保存/MQ发送 |

## 依赖关系

- 所有 Service 测试依赖对应的 Mapper 接口（Mock）
- 部分服务依赖 `FileCleanupHelper`（文件清理）、`PopularityAsyncService`（热度递增）、`FavoriteService`/`FeedbackService`（收藏/反馈）
- `FileUploadServiceImplTest` 使用真实文件系统（临时目录）
- `UserServiceImplTest` 使用真实 `BCryptPasswordEncoder`
- Service 测试通过反射注入 `baseMapper`，绕过 MyBatis-Plus 的依赖注入限制
