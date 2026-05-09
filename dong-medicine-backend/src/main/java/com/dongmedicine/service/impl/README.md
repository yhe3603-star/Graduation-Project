# Service Impl 层 -- 业务逻辑实现（16个实现类）

> 本目录存放所有Service接口的实现类。每个实现类对应一个Service接口（CaptchaService除外，它没有接口，直接放在service包下）。
> 实现类是业务逻辑的真正执行者，包含数据库操作、缓存管理、消息队列、文件处理等具体实现。

---

## 一、实现类清单

| # | 实现类 | 实现接口 | 继承父类 | 核心业务逻辑 |
|---|--------|---------|---------|------------|
| 1 | **PlantServiceImpl** | PlantService | ServiceImpl\<PlantMapper, Plant\> | 全文搜索+LIKE降级、缓存管理、SQL聚合统计、筛选选项、文件清理删除 |
| 2 | **KnowledgeServiceImpl** | KnowledgeService | ServiceImpl\<KnowledgeMapper, Knowledge\> | 高级搜索(疗法/疾病/药材多维筛选)、收藏、反馈、缓存、统计 |
| 3 | **InheritorServiceImpl** | InheritorService | ServiceImpl\<InheritorMapper, Inheritor\> | 传承人搜索、级别筛选、统计、缓存、筛选选项、文件清理删除 |
| 4 | **UserServiceImpl** | UserService | ServiceImpl\<UserMapper, User\> | BCrypt密码加密、Sa-Token登录、注册校验(用户名+密码强度)、封禁/解封、改密 |
| 5 | **CommentServiceImpl** | CommentService | ServiceImpl\<CommentMapper, Comment\> | 评论发表(支持嵌套回复parentId)、审核(approve/reject)、DTO转换 |
| 6 | **FavoriteServiceImpl** | FavoriteService | ServiceImpl\<FavoriteMapper, Favorite\> | 收藏添加/取消、我的收藏列表、批量关联查询(inCollection) |
| 7 | **FeedbackServiceImpl** | FeedbackService | ServiceImpl\<FeedbackMapper, Feedback\> | 反馈提交(RabbitMQ异步+降级同步)、回复、状态统计(待处理/已解决) |
| 8 | **QuizServiceImpl** | QuizService | 无 | 随机题目(Mapper自定义SQL)、评分(逐题比对)、CRUD |
| 9 | **ResourceServiceImpl** | ResourceService | ServiceImpl\<ResourceMapper, Resource\> | 资源分类搜索、热门排序、下载计数、缓存、统计、筛选选项 |
| 10 | **QaServiceImpl** | QaService | ServiceImpl\<QaMapper, Qa\> | 问答分类查询、统计、筛选选项 |
| 11 | **OperationLogServiceImpl** | OperationLogService | ServiceImpl\<OperationLogMapper, OperationLog\> | 日志查询(分页+条件)、趋势统计(按日期分组)、自动清理超限日志 |
| 12 | **PlantGameServiceImpl** | PlantGameService | ServiceImpl\<PlantGameRecordMapper, PlantGameRecord\> | 游戏评分(服务端验证+客户端兼容)、记录保存 |
| 13 | **AiChatServiceImpl** | AiChatService | 无 | DeepSeek AI对话(同步RestTemplate+流式WebClient)、系统提示词 |
| 14 | **ChatHistoryServiceImpl** | ChatHistoryService | ServiceImpl\<ChatHistoryMapper, ChatHistory\> | 聊天历史Redis暂存+MySQL持久化、会话管理、消息flush |
| 15 | **BrowseHistoryServiceImpl** | BrowseHistoryService | ServiceImpl\<BrowseHistoryMapper, BrowseHistory\> | 浏览历史记录与查询（关联实体名称和图片） |
| 16 | **FileUploadServiceImpl** | FileUploadService | 无 | 文件上传5层校验+存储+删除+RabbitMQ文件处理任务 |
| 17 | **PopularityAsyncServiceImpl** | PopularityAsyncService | 无 | 异步人气值更新（浏览量+人气值联合递增） |
| 18 | **RabbitMQOperationLogServiceImpl** | RabbitMQOperationLogService | 无 | RabbitMQ异步操作日志发送（@ConditionalOnProperty条件加载） |

> 注：CaptchaService 没有接口，直接以 `@Service` 标注在 `service/CaptchaService.java`，不在impl目录中。

---

## 二、核心业务逻辑详解

### 2.1 PlantServiceImpl -- 植物服务实现

**搜索策略**：全文索引优先，LIKE降级

```
用户输入keyword
  ↓
配置开关 app.search.use-fulltext=true?
  ↓ 是
调用 baseMapper.searchByFullText(keyword, limit)
  ↓ 有结果?
  → 返回
  ↓ 无结果或异常
调用 baseMapper.searchByLike(escapedKeyword, limit)  ← 降级
```

**LIKE注入防护**：`PageUtils.escapeLike()` 转义 `%`、`_`、`\`

**缓存管理**：
- `@Cacheable(value = "plants", key = "'detail:' + #id")` -- 详情缓存
- `@CacheEvict(value = "plants", allEntries = true)` -- 写操作清缓存
- `deleteWithFiles()` -- 事务删除 + 文件清理 + 清缓存

**统计与筛选**：
- `getStats()` -- 调用Mapper自定义SQL（countDistinctCategory, sumViewCount, sumFavoriteCount）
- `getFilterOptions()` -- 调用Mapper自定义SQL（selectDistinctCategory, selectDistinctUsageWay）

### 2.2 UserServiceImpl -- 用户服务实现

**登录流程**：
```
1. 参数非空校验
2. 查询用户（LambdaQueryWrapper.eq(username)）
3. 封禁检查（user.isBanned()）
4. BCrypt密码验证（passwordEncoder.matches()）
5. Sa-Token登录（StpUtil.login(userId)）
6. Session存储（username, role）
7. 返回 token + id + username + role
```

**注册流程**：
```
1. 用户名长度校验（3-20字符）
2. 密码强度校验（PasswordValidator.validate()）
3. 用户名唯一性检查
4. BCrypt加密存储
5. 默认角色 ROLE_USER，状态 STATUS_ACTIVE
```

**改密流程**：
```
1. 验证码校验（CaptchaService.validateCaptchaOrThrow()）
2. 旧密码验证（BCrypt.matches()）
3. 新密码加密更新（BCrypt.encode()）
4. Sa-Token登出（StpUtil.logout()）
```

### 2.3 KnowledgeServiceImpl -- 知识服务实现

**高级搜索**：支持 therapy(疗法)、disease(疾病)、herb(药材) 三维筛选 + keyword + sortBy

**收藏功能**：
- 添加收藏：先查是否已收藏，避免重复
- 取消收藏：按 userId + knowledgeId 删除

**反馈功能**：创建Feedback记录，关联knowledgeId

### 2.4 CommentServiceImpl -- 评论服务实现

**嵌套回复**：通过 `parentId` 字段实现评论树结构
- `parentId = null` -- 顶级评论
- `parentId = 某评论ID` -- 回复该评论

**审核机制**：
- 新评论默认 `status = "pending"`
- `approve()` -- 设置 `status = "approved"`
- `reject()` -- 设置 `status = "rejected"`

**DTO转换**：`convertToDTO()` 将Comment转为CommentDTO，包含用户名和回复列表

### 2.5 FavoriteServiceImpl -- 收藏服务实现

**批量关联查询**：
```java
// 查询某用户对一批实体是否已收藏（用于列表页展示收藏状态）
public Map<Integer, Boolean> batchCheckInCollection(Integer userId, String targetType, List<Integer> targetIds) {
    // 1. 一次查询所有匹配的收藏记录
    // 2. 转为 Set<targetId>
    // 3. 遍历targetIds，判断是否在Set中
    // 4. 返回 Map<targetId, Boolean>
}
```

### 2.6 FeedbackServiceImpl -- 反馈服务实现

**异步提交 + 降级**：
```
submitFeedback()
  ↓
RabbitMQ可用?
  ↓ 是
FeedbackProducer.sendFeedback(feedback)  ← 异步
  ↓ 否（异常）
save(feedback)  ← 降级为同步保存
```

**匿名支持**：未登录用户提交反馈时，userName 设为 "anonymous"

### 2.7 QuizServiceImpl -- 答题服务实现

**随机题目**：调用 `QuizQuestionMapper.selectRandomQuestions(count)` -- MySQL `ORDER BY RAND()`

**评分逻辑**：
```java
// 逐题比对用户答案与正确答案
int correctCount = 0;
for (AnswerDTO answer : answers) {
    QuizQuestion question = questionMap.get(answer.getQuestionId());
    if (question != null && question.getCorrectAnswer().equals(answer.getSelectedAnswer())) {
        correctCount++;
    }
}
int score = correctCount * scorePerQuestion;
```

### 2.8 PlantGameServiceImpl -- 游戏服务实现

**服务端验证优先**：
```
submit()
  ↓
answers列表非空?
  ↓ 是
遍历answers:
  查库获取Plant中文名
  比对用户提交的answer与Plant.nameCn
  匹配则correctCount++
  ↓ 否（兼容旧版前端）
使用客户端提交的correctCount/totalCount
做合理性校验（correctCount <= totalCount, totalCount <= maxQuestions）
```

**得分计算**：`score = correctCount * scorePerQuestion`

### 2.9 AiChatServiceImpl -- AI聊天服务实现

**双模式对话**：
- `chat()` -- 同步模式：RestTemplate调用DeepSeek API，等待完整回复
- `chatStream()` -- 流式模式：WebClient调用SSE接口，逐token通过StreamCallback推送

**系统提示词**：内置侗族医药领域知识提示词，引导AI回答侗医药相关问题

### 2.10 ChatHistoryServiceImpl -- 聊天历史服务实现

**双存储架构**：
```
用户发送消息
  ↓
1. saveMessageToRedis() -- 即时写入Redis（快速响应）
2. saveMessage() -- 写入MySQL（持久化）
   ↓
flushSessionToMysql() -- 将Redis中整个会话flush到MySQL
```

**会话管理**：
- `getUserSessions()` -- 查询用户所有会话（按时间倒序）
- `getSessionHistory()` -- 查询某会话的所有消息
- `deleteSession()` -- 删除会话（Redis + MySQL双删）

### 2.11 FileUploadServiceImpl -- 文件上传服务实现

**5层校验**：
```
1. 文件非空检查
2. 文件类型白名单校验（图片/视频/文档各自的扩展名列表）
3. 文件大小限制校验（图片5MB/视频100MB/文档20MB）
4. 文件名安全校验（防止路径遍历）
5. 存储目录合法性校验
```

**存储策略**：按日期分目录 `uploads/{category}/{yyyy-MM-dd}/{uuid}.{ext}`

**RabbitMQ集成**：上传成功后发送文件处理任务到消息队列（缩略图生成等）

### 2.12 OperationLogServiceImpl -- 操作日志服务实现

**自动清理**：查询时检查日志总数，超过阈值（默认10000条）自动清理最旧的日志

**趋势统计**：按日期分组统计最近7天的操作数量

### 2.13 BrowseHistoryServiceImpl -- 浏览历史服务实现

**关联查询**：查询浏览历史时，关联查询实体名称和图片，返回完整的浏览记录信息

### 2.14 PopularityAsyncServiceImpl -- 人气值异步更新

**联合递增**：一次SQL同时更新 `view_count` 和 `popularity` 字段
- `view_count += 1`
- `popularity += 1`
- 使用 `@Async` 异步执行，不阻塞主流程

### 2.15 RabbitMQOperationLogServiceImpl -- RabbitMQ操作日志

**条件加载**：`@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)`
- RabbitMQ启用时：通过OperationLogProducer发送日志到消息队列
- RabbitMQ禁用时：此Bean不加载，AOP切面降级为同步保存

---

## 三、依赖关系图

```
Controller
  ↓ 调用
ServiceImpl
  ↓ 依赖
┌─────────────────────────────────────┐
│  Mapper (数据库访问)                  │
│  RedisTemplate / StringRedisTemplate │
│  RabbitMQ Producer (异步消息)         │
│  FileCleanupHelper (文件清理)         │
│  PopularityAsyncService (异步人气)    │
│  BCryptPasswordEncoder (密码加密)     │
│  Sa-Token (会话管理)                  │
│  DeepSeekConfig (AI配置)             │
│  PageUtils (分页工具)                 │
│  PasswordValidator (密码校验)         │
└─────────────────────────────────────┘
```

---

## 四、编程规范

### 注解模板

```java
@Slf4j                                          // 日志
@Service                                         // Spring Bean
@RequiredArgsConstructor                         // 构造器注入
public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> implements XxxService {

    private final FileCleanupHelper fileCleanupHelper;  // final字段，构造器注入
    private final PopularityAsyncService popularityAsyncService;

    // 只读方法：不加@Transactional
    @Override
    @Cacheable(value = "xxx", key = "'detail:' + #id")
    public Xxx getDetail(Integer id) { ... }

    // 写方法：加@Transactional + @CacheEvict
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "xxx", allEntries = true)
    public void deleteWithFiles(Integer id) { ... }
}
```

### 异常处理

所有业务异常统一使用 `BusinessException`：
```java
// 参数错误
throw BusinessException.badRequest("关键词不能为空");
// 用户不存在
throw BusinessException.userNotFound();
// 密码错误
throw BusinessException.passwordWrong();
// 权限不足
throw BusinessException.forbidden("账号已被封禁");
// 数据已存在
throw BusinessException.userAlreadyExists();
```

### 分页查询

统一使用 `PageUtils.getPage(page, size)` 创建分页参数：
```java
Page<Xxx> pageParam = PageUtils.getPage(page, size);
LambdaQueryWrapper<Xxx> qw = new LambdaQueryWrapper<>();
// ... 构建查询条件
return page(pageParam, qw);  // 返回Page对象，Controller用PageUtils.toMap()转换
```
