# Service 层 -- 业务逻辑（18个Service）

> Service 是三层架构的"业务逻辑层"，Controller只管传话，Service才是真正执行逻辑的地方。
> 所有Service接口继承 `IService<Entity>`，实现类继承 `ServiceImpl<Mapper, Entity>`。

---

## 一、Service 清单

| # | Service接口 | 实现类 | 继承IService | 核心职责 |
|---|-----------|--------|-------------|---------|
| 1 | PlantService | PlantServiceImpl | IService\<Plant\> | 植物搜索(全文+LIKE降级)、详情、缓存 |
| 2 | KnowledgeService | KnowledgeServiceImpl | IService\<Knowledge\> | 知识高级搜索、收藏、反馈、缓存 |
| 3 | InheritorService | InheritorServiceImpl | IService\<Inheritor\> | 传承人搜索、统计、缓存 |
| 4 | UserService | UserServiceImpl | IService\<User\> | 登录(BCrypt+Sa-Token)、注册、封禁 |
| 5 | CommentService | CommentServiceImpl | IService\<Comment\> | 评论发表、嵌套回复、审核 |
| 6 | FavoriteService | FavoriteServiceImpl | IService\<Favorite\> | 收藏添加/取消、列表查询 |
| 7 | FeedbackService | FeedbackServiceImpl | IService\<Feedback\> | 反馈提交(RabbitMQ异步)、回复 |
| 8 | QuizService | QuizServiceImpl | IService\<QuizQuestion\> | 随机题目、评分、CRUD |
| 9 | ResourceService | ResourceServiceImpl | IService\<Resource\> | 资源分类搜索、下载计数、缓存 |
| 10 | QaService | QaServiceImpl | IService\<Qa\> | 问答分类查询、统计 |
| 11 | OperationLogService | OperationLogServiceImpl | IService\<OperationLog\> | 日志查询、趋势统计、清理 |
| 12 | PlantGameService | PlantGameServiceImpl | 否 | 植物识别游戏评分、记录 |
| 13 | AiChatService | AiChatServiceImpl | 否 | DeepSeek AI对话(普通+流式) |
| 14 | ChatHistoryService | ChatHistoryServiceImpl | 否 | 聊天历史Redis暂存+MySQL持久化 |
| 15 | BrowseHistoryService | BrowseHistoryServiceImpl | IService\<BrowseHistory\> | 浏览历史记录与查询 |
| 16 | FileUploadService | FileUploadServiceImpl | 否 | 文件上传校验+存储+删除 |
| 17 | CaptchaService | --（无接口，直接@Service） | 否 | 验证码生成(Java Graphics2D) + Redis存储 |
| 18 | PopularityAsyncService | PopularityAsyncServiceImpl | 否 | 异步人气值更新 |
| 19 | RabbitMQOperationLogService | RabbitMQOperationLogServiceImpl | 否 | RabbitMQ异步操作日志发送 |

---

## 二、核心Service详细分析

### 2.1 PlantServiceImpl -- 药用植物服务

文件：`service/impl/PlantServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    private final FileCleanupHelper fileCleanupHelper;
    @Value("${app.search.use-fulltext:true}")
    private boolean useFullTextSearch;

    // ---- 查询方法（带缓存）----

    @Override
    @Cacheable(value = "searchResults", key = "'plants:' + (#keyword ?: 'all') + ':' + (#category ?: 'all')")
    public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);  // 防LIKE注入
            qw.and(w -> w.like(Plant::getNameCn, escapedKeyword)
                         .or().like(Plant::getNameDong, escapedKeyword)
                         .or().like(Plant::getEfficacy, escapedKeyword)
                         .or().like(Plant::getStory, escapedKeyword));
        }
        // ... category / usageWay等值过滤
        qw.orderByAsc(Plant::getNameCn);
        return list(qw);
    }

    // ---- 搜索方法（全文检索优先，降级到LIKE）----

    @Override
    public List<Plant> search(String keyword, int limit) {
        // 参数校验
        if (keyword == null || keyword.isBlank()) throw BusinessException.badRequest("关键词不能为空");
        if (limit < 1 || limit > 100) throw BusinessException.badRequest("限制数量1-100");

        String escapedKeyword = PageUtils.escapeLike(keyword);

        // 优先使用MySQL全文索引搜索
        try {
            if (useFullTextSearch) {
                List<Plant> results = baseMapper.searchByFullText(keyword, limit);
                if (!results.isEmpty()) return results;
            }
        } catch (Exception e) {
            log.warn("全文搜索失败，回退到LIKE搜索: {}", e.getMessage());
        }

        // 降级到LIKE搜索
        return baseMapper.searchByLike(escapedKeyword, limit);
    }

    // ---- 缓存清除 + 文件清理----

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "plants", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Plant plant = getById(id);
        if (plant == null) return;
        // 先清理文件
        fileCleanupHelper.deleteFilesFromJson(plant.getImages());
        fileCleanupHelper.deleteFilesFromJson(plant.getVideos());
        fileCleanupHelper.deleteFilesFromJson(plant.getDocuments());
        // 再删数据库
        removeById(id);
    }
}
```

**关键设计点**：
- **全文搜索优先**：利用MySQL `MATCH ... AGAINST` 全文索引，速度快；不可用时自动降级到 `LIKE` 查询
- **LIKE注入防护**：使用 `PageUtils.escapeLike()` 转义 `%`、`_`、`\` 三个特殊字符
- **缓存策略**：`@Cacheable` 在查询方法上，`@CacheEvict` 在写操作上
- **事务+文件清理**：`deleteWithFiles` 先用 `FileCleanupHelper` 清理磁盘文件，再删除数据库记录

### 2.2 UserServiceImpl -- 用户服务

文件：`service/impl/UserServiceImpl.java`

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ---- 登录 ----
    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. 参数校验
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }
        // 2. 查用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) throw BusinessException.userNotFound();
        // 3. 查封禁
        if (user.isBanned()) throw BusinessException.forbidden("账号已被封禁");
        // 4. 验证密码（BCrypt）
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }
        // 5. Sa-Token登录
        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());

        return Map.of("token", StpUtil.getTokenValue(),
                      "id", user.getId(),
                      "username", user.getUsername(),
                      "role", user.getRole());
    }

    // ---- 注册 ----
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        // 1. 用户名校验（长度3-20）
        if (username.length() < 3 || username.length() > 20)
            throw new BusinessException(ErrorCode.USERNAME_TOO_SHORT);
        // 2. 密码强度校验（PasswordValidator）
        PasswordValidator.ValidationResult result = PasswordValidator.validate(password);
        if (!result.isValid()) throw BusinessException.passwordTooWeak();
        // 3. 唯一性检查
        if (getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null)
            throw BusinessException.userAlreadyExists();
        // 4. 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));  // BCrypt加密
        user.setRole(RoleConstants.ROLE_USER);
        user.setStatus(User.STATUS_ACTIVE);
        save(user);
    }

    // ---- 获取用户信息（清除敏感字段） ----
    @Override
    public User getUserInfo(Integer userId) {
        User user = getById(userId);
        if (user == null) throw BusinessException.userNotFound();
        user.setPasswordHash(null);  // 重要！返回前端前清除密码
        return user;
    }
}
```

**关键设计点**：
- **BCrypt密码加密**：不可逆存储，`matches()` 验证
- **Sa-Token Session**：登录后存储 username 和 role 到会话，供 `SecurityUtils` 读取
- **事务保护**：注册等写操作加 `@Transactional`
- **敏感信息清除**：`getUserInfo()` 返回前端前必清 `passwordHash`

### 2.3 AiChatServiceImpl -- AI聊天服务

文件：`service/impl/AiChatServiceImpl.java`

```java
@Service
public class AiChatServiceImpl implements AiChatService {
    // 基于DeepSeek API，通过WebClient实现流式HTTP调用
    // chat() → 同步调用，返回完整回复
    // chatStream() → 流式调用，通过WebFlux逐token推送
}
```

### 2.4 CaptchaService -- 验证码服务

文件：`service/CaptchaService.java`

这个Service比较特殊：没有接口，直接使用 `@Service` 标注实现类。

```java
@Service
public class CaptchaService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 核心方法
    public CaptchaResult generateCaptcha() {
        // 1. 生成UUID作为captchaKey
        // 2. 生成4位随机数字
        // 3. 用Java Graphics2D绘制验证码图片（120x40px）
        // 4. 添加干扰线(8条) + 噪点(50个) + 字符随机旋转
        // 5. 转Base64图片
        // 6. 存入Redis，5分钟过期，key=captcha:{uuid}
        // 7. 返回captchaKey + captchaImage
    }

    public void validateCaptchaOrThrow(String captchaKey, String captchaCode) {
        // 从Redis取值 → 比对 → 删除（一次性使用）→ 不匹配则抛异常
    }
}
```

**关键设计点**：
- **一次性使用**：验证后立即从Redis删除，防止重放攻击
- **5分钟过期**：Redis TTL自动清理未使用的验证码
- **干扰设计**：8条随机线 + 50个噪点 + 字符随机旋转，防OCR识别

### 2.5 ChatHistoryService -- 聊天历史服务

文件：`service/ChatHistoryService.java`

**两级存储**：聊天中暂存Redis → 连接断开时刷入MySQL。

```java
// Redis临时存储（聊天过程中）
void saveMessageToRedis(Integer userId, String sessionId, String role, String content);

// 查询Redis中的会话消息
List<ChatHistory> getSessionHistory(Integer userId, String sessionId);

// 断开连接时将Redis中的消息批量写入MySQL
void flushSessionToMysql(Integer userId, String sessionId);
```

---

## 三、缓存策略

### Redis缓存TTL配置（CacheConfig中定义）

| 缓存区 | TTL | 原因 |
|--------|-----|------|
| plants | 6小时 | 植物数据变化不频繁 |
| knowledges | 6小时 | 知识数据变化不频繁 |
| inheritors | 6小时 | 传承人数据变化不频繁 |
| resources | 4小时 | 资源偶尔更新 |
| users | 30分钟 | 用户信息较常变化 |
| quizQuestions | 12小时 | 题目很少变化 |
| searchResults | 5分钟 | 搜索结果时效性高 |
| hotData | 1小时 | 热门数据中等 |

### 缓存注解使用

```java
// 查询：先查缓存，未命中再查数据库
@Cacheable(value = "plants", key = "'detail:' + #id")
public Plant getDetailWithStory(Integer id) { ... }

// 清缓存：数据变更后清除相关缓存
@CacheEvict(value = "plants", allEntries = true)
public void clearCache() { ... }

// 事务+清缓存：删除数据时
@Transactional
@CacheEvict(value = "plants", allEntries = true)
public void deleteWithFiles(Integer id) { ... }
```

---

## 四、事务管理

使用 `@Transactional(rollbackFor = Exception.class)`：

- **注册**：用户唯一性检查 + 密码加密 + 保存（多步骤必须原子性）
- **修改密码**：密码验证 + BCrypt加密 + 更新（多步骤必须原子性）
- **封禁/解封**：查询 + 修改状态
- **删除资源**：数据库删除 + 文件清理
- 只读方法（list/page/search/detail）**不加** `@Transactional`

---

## 五、Service编程模板

```java
// === 接口 ===
// service/XxxService.java
public interface XxxService extends IService<Xxx> {
    Page<Xxx> searchPaged(String keyword, Integer page, Integer size);
    void clearCache();
    void deleteWithFiles(Integer id);
}

// === 实现 ===
// service/impl/XxxServiceImpl.java
@Slf4j
@Service
@RequiredArgsConstructor
public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> implements XxxService {

    private final FileCleanupHelper fileCleanupHelper;

    @Override
    @Cacheable(value = "xxx", key = "'search:' + (#keyword ?: 'all')")
    public Page<Xxx> searchPaged(String keyword, Integer page, Integer size) {
        // 1. 参数校验
        // 2. 构建LambdaQueryWrapper + LIKE防注入
        // 3. 调用page()或baseMapper自定义方法
        // 4. 返回分页结果
    }

    @Override
    @CacheEvict(value = "xxx", allEntries = true)
    public void clearCache() { }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "xxx", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Xxx entity = getById(id);
        if (entity == null) return;
        fileCleanupHelper.deleteFilesFromJson(entity.getFiles());
        removeById(id);
    }
}
```
