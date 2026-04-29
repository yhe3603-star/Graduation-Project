# 服务实现目录 (service/impl/)

> 这里是整个后端的"厨房" -- 所有业务逻辑都在这里变成可执行的代码。

---

## 一、先搞懂核心概念

### 1.1 什么是服务实现？

**类比：菜谱的具体做法，把抽象步骤变成实际操作。**

你在 Controller 里写了"查询药用植物列表"这个接口，但具体怎么查？去哪查？查出来怎么处理？这些"具体怎么做"的逻辑就写在 Service 实现类里。

```
用户请求: "我要查钩藤的信息"
       |
       v
Controller: "好的，我调用 PlantService.search('钩藤')"  <-- 只说做什么
       |
       v
PlantServiceImpl: "让我来干！"                           <-- 具体怎么做
  1. 用 LambdaQueryWrapper 构建查询条件
  2. 调用 plantMapper 执行 SQL 查询
  3. 返回查询结果
```

### 1.2 为什么要"接口 + 实现"分开？

**类比：菜单上写"宫保鸡丁"是接口，厨房具体怎么做是实现。**

你去餐厅点菜，菜单上写着"宫保鸡丁 38元"，这就是**接口** -- 它告诉你有这道菜，但没告诉你厨师怎么做。厨房里厨师的具体做法（先炸花生还是先炒鸡丁），这就是**实现**。

为什么要分开？

| 好处 | 说明 | 生活类比 |
|------|------|---------|
| 解耦 | Controller 只依赖接口，不关心具体实现 | 顾客只看菜单，不进厨房 |
| 可替换 | 换实现类不影响调用方 | 换了厨师，菜单不变 |
| 可测试 | 可以用 Mock 替代真实实现 | 试菜时可以用替代食材 |
| 规范化 | 接口定义了"必须做什么" | 菜单规定了每道菜必须有名字和价格 |

```java
// 接口：只定义"做什么"，不写具体逻辑
public interface PlantService {
    List<Plant> search(String keyword);
}

// 实现类：具体"怎么做"
@Service
public class PlantServiceImpl implements PlantService {
    @Override
    public List<Plant> search(String keyword) {
        // 这里写具体的查询逻辑
    }
}

// Controller：只认接口，不管实现
@RestController
public class PlantController {
    @Autowired
    private PlantService plantService;  // 注入接口，不是实现类！
}
```

---

## 二、全部15个实现类一览

| # | 实现类 | 对应接口 | 一句话说明 | 关键技术点 |
|---|--------|---------|-----------|-----------|
| 1 | `UserServiceImpl` | `UserService` | 用户注册/登录/封禁/改密码 | BCrypt加密、JWT令牌、密码强度校验 |
| 2 | `PlantServiceImpl` | `PlantService` | 药用植物增删改查/搜索 | @Cacheable缓存、全文搜索、关联文件删除 |
| 3 | `KnowledgeServiceImpl` | `KnowledgeService` | 侗医药知识库管理 | 多维度分类查询、异步热度计数、收藏/反馈 |
| 4 | `InheritorServiceImpl` | `InheritorService` | 传承人信息管理 | 级别筛选、按经验年限排序、关联文件管理 |
| 5 | `ResourceServiceImpl` | `ResourceService` | 学习资源管理 | 文件类型筛选、下载计数、热门排行 |
| 6 | `QaServiceImpl` | `QaService` | 问答知识库 | 分类查询、浏览量计数 |
| 7 | `CommentServiceImpl` | `CommentService` | 评论系统 | 嵌套回复、审核状态管理、DTO转换 |
| 8 | `FavoriteServiceImpl` | `FavoriteService` | 收藏功能 | 唯一性校验、收藏计数同步、批量查询 |
| 9 | `FeedbackServiceImpl` | `FeedbackService` | 用户反馈 | 状态流转(pending->resolved)、管理员回复 |
| 10 | `QuizServiceImpl` | `QuizService` | 知识测验 | 随机出题、自动评分、@CacheEvict缓存清除 |
| 11 | `PlantGameServiceImpl` | `PlantGameService` | 植物识别游戏 | 游戏记录提交、用户成绩查询 |
| 12 | `AiChatServiceImpl` | `AiChatService` | AI智能聊天 | DeepSeek API调用、系统提示词、请求统计 |
| 13 | `FileUploadServiceImpl` | `FileUploadService` | 文件上传 | 多层安全校验、UUID文件名、路径遍历防护 |
| 14 | `TokenBlacklistServiceImpl` | `TokenBlacklistService` | Token黑名单 | Redis+Caffeine双层缓存、降级策略 |
| 15 | `OperationLogServiceImpl` | `OperationLogService` | 操作日志 | 自动清理超限日志、7天趋势统计 |

---

## 三、重点实现类代码详解

### 3.1 PlantServiceImpl -- 药用植物服务

这是本项目最核心的服务之一，管理侗族药用植物的所有数据操作。

```java
@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    // ========== 依赖注入 ==========
    // @Autowired 是 Spring 的自动注入注解，Spring 会自动把需要的对象传进来
    // 不需要你 new PlantMapper()，Spring 帮你创建和管理
    @Autowired
    private PlantMapper plantMapper;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;

    // 从配置文件读取是否使用全文搜索
    // application.yml 里配置 app.search.use-fulltext: true
    @Value("${app.search.use-fulltext:true}")
    private boolean useFullTextSearch;

    // ========== 高级搜索（带缓存） ==========
    // @Cacheable：第一次查询时执行方法并把结果存入缓存
    //              第二次相同参数查询时，直接从缓存取，不再执行方法
    // value = "plants"：缓存区域名称
    // key = "'list:' + ..."：缓存键，用参数拼接，保证不同参数有不同缓存
    @Override
    @Cacheable(value = "plants", key = "'list:' + (#keyword ?: 'all') + ':' + (#category ?: 'all') + ':' + (#usageWay ?: 'all')")
    public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
        // LambdaQueryWrapper 是 MyBatis-Plus 提供的查询构造器
        // 用 Lambda 表达式引用字段名，编译期就能发现拼写错误
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            // PageUtils.escapeLike() 防止 SQL 注入的特殊字符
            // 比如 keyword 里有 % 或 _ 会被转义
            String escapedKeyword = PageUtils.escapeLike(keyword);
            // .and() 表示括号内的条件用 AND 连接
            // .or() 表示括号内的条件用 OR 连接
            // 效果：WHERE (name_cn LIKE '%钩藤%' OR name_dong LIKE '%钩藤%' OR efficacy LIKE '%钩藤%')
            qw.and(wrapper -> wrapper
                .like(Plant::getNameCn, escapedKeyword)    // 中文名
                .or().like(Plant::getNameDong, escapedKeyword) // 侗语名
                .or().like(Plant::getEfficacy, escapedKeyword)  // 功效
                .or().like(Plant::getStory, escapedKeyword));   // 民间故事
        }
        if (StringUtils.hasText(category)) {
            qw.eq(Plant::getCategory, category);  // 精确匹配分类
        }
        if (StringUtils.hasText(usageWay)) {
            qw.eq(Plant::getUsageWay, usageWay);  // 精确匹配用法
        }
        qw.orderByAsc(Plant::getNameCn);  // 按中文名排序
        return list(qw);  // list() 方法继承自 ServiceImpl，执行查询并返回列表
    }

    // ========== 搜索（全文索引 + LIKE 双模式） ==========
    @Override
    public List<Plant> search(String keyword, int limit) {
        // 参数校验：防止传入空值或超出范围的limit
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");
        }
        if (limit < MIN_PAGE_SIZE || limit > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                String.format("限制数量必须在%d-%d之间", MIN_PAGE_SIZE, MAX_PAGE_SIZE));
        }

        String escapedKeyword = PageUtils.escapeLike(keyword);

        try {
            // 优先使用全文索引搜索（速度快，支持中文分词）
            if (useFullTextSearch) {
                List<Plant> results = plantMapper.searchByFullText(keyword, limit);
                if (!results.isEmpty()) {
                    return results;  // 全文搜索有结果就直接返回
                }
            }
        } catch (Exception e) {
            // 全文搜索失败不影响功能，回退到 LIKE 搜索
            log.warn("全文搜索失败，回退到LIKE搜索: {}", e.getMessage());
        }

        // LIKE 搜索作为兜底方案（速度慢，但保证能查到）
        return plantMapper.searchByLike(escapedKeyword, limit);
    }

    // ========== 删除植物（同时删除关联文件） ==========
    // @CacheEvict：删除/修改数据时，要把缓存也清掉
    // allEntries = true：清空 plants 缓存区域的所有条目
    // 为什么？因为删除一条数据可能影响列表查询的结果
    @Override
    @CacheEvict(value = "plants", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Plant plant = getById(id);
        if (plant == null) {
            return;  // 植物不存在，直接返回（不抛异常，幂等操作）
        }
        // 删除关联的图片、视频、文档文件
        // getImages()/getVideos()/getDocuments() 返回的是 JSON 字符串
        // fileCleanupHelper 会解析 JSON 并逐个删除文件
        fileCleanupHelper.deleteFilesFromJson(plant.getImages());
        fileCleanupHelper.deleteFilesFromJson(plant.getVideos());
        fileCleanupHelper.deleteFilesFromJson(plant.getDocuments());
        removeById(id);  // 最后删除数据库记录
        log.info("Deleted plant {} with associated files", id);
    }

    // ========== 增加浏览量 ==========
    // 浏览量增加不需要阻塞主流程，用 try-catch 包裹
    // 即使浏览量增加失败，也不影响用户查看植物详情
    @Override
    public void incrementViewCount(Integer id) {
        try {
            plantMapper.incrementViewCount(id);
        } catch (Exception e) {
            log.error("Failed to increment view count for plant id: {}", id, e);
            // 不抛异常！浏览量增加失败不应该影响用户体验
        }
    }
}
```

**缓存注解速查表：**

| 注解 | 什么时候用 | 效果 |
|------|-----------|------|
| `@Cacheable` | 查询方法 | 先查缓存，没有再执行方法，结果存入缓存 |
| `@CacheEvict` | 增删改方法 | 清除缓存，保证下次查询拿到最新数据 |
| `@CachePut` | 更新方法 | 执行方法，结果存入缓存（不管缓存有没有都执行） |

### 3.2 UserServiceImpl -- 用户服务

用户服务是安全要求最高的模块，涉及密码加密、JWT令牌、权限控制。

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // BCryptPasswordEncoder：密码加密工具
    // 为什么不用 MD5？因为 MD5 可以被彩虹表破解，BCrypt 自带盐值，更安全
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ========== 用户登录 ==========
    @Override
    public String login(String username, String password) {
        // 第一步：参数校验（永远不要信任前端传来的数据！）
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }

        // 第二步：根据用户名查询用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw BusinessException.userNotFound();
        }

        // 第三步：检查用户是否被封禁
        if (user.isBanned()) {
            throw BusinessException.forbidden("该用户已被封禁");
        }

        // 第四步：验证密码
        // matches(明文密码, 加密后的密码) --> 内部会自动提取盐值并比对
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }

        // 第五步：生成 JWT 令牌
        // JWT 里包含：用户名、用户ID、角色信息
        return jwtUtil.generateToken(username, user.getId(), user.getRole());
    }

    // ========== 用户注册 ==========
    // @Transactional：数据库事务注解
    // rollbackFor = Exception.class：任何异常都回滚
    // 比如注册过程中出了异常，已插入的数据会自动撤销
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        // 参数校验
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new BusinessException(ErrorCode.USERNAME_TOO_SHORT);
        }

        // 密码强度校验（长度、复杂度等）
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(password);
        if (!validationResult.isValid()) {
            throw BusinessException.passwordTooWeak();
        }

        // 检查用户名是否已存在
        if (getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) {
            throw BusinessException.userAlreadyExists();
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        // passwordEncoder.encode()：加密密码
        // 每次加密结果都不同（因为盐值随机），但 matches() 都能正确验证
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(RoleConstants.ROLE_USER);  // 默认角色是普通用户
        user.setStatus(User.STATUS_ACTIVE);      // 默认状态是活跃
        user.setCreatedAt(LocalDateTime.now());
        save(user);
    }

    // ========== 修改密码 ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        // ... 参数校验省略 ...

        User user = getById(userId);
        // 先验证旧密码是否正确
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }
        // 设置新密码（加密后存储）
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    // ========== 封禁用户 ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void banUser(Integer userId, String reason) {
        Integer currentId = SecurityUtils.getCurrentUserId();
        // 安全检查：不能封禁自己
        if (currentId != null && currentId.equals(userId)) {
            throw BusinessException.forbidden("不能封禁当前登录账号");
        }
        // 安全检查：不能封禁管理员
        User user = getById(userId);
        if (RoleConstants.ROLE_ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("不能封禁管理员账号");
        }
        user.setStatus(User.STATUS_BANNED);
        updateById(user);
    }

    // ========== 删除用户 ==========
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Integer userId) {
        // 安全检查：不能删除自己
        Integer currentId = SecurityUtils.getCurrentUserId();
        if (currentId != null && currentId.equals(userId)) {
            throw BusinessException.forbidden("不能删除当前登录账号");
        }
        // 安全检查：不能删除最后一个管理员
        User target = getById(userId);
        if (RoleConstants.ROLE_ADMIN.equals(target.getRole()) && countAdmins() <= 1) {
            throw BusinessException.forbidden("不能删除系统唯一的管理员账号");
        }
        removeById(userId);
    }
}
```

**密码加密对比：**

```
MD5 加密（不安全）：
  "123456" --> "e10adc3949ba59abbe56e057f20f883e"
  问题：同样的密码加密结果一样，彩虹表可以直接反查

BCrypt 加密（安全）：
  "123456" --> "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
  "123456" --> "$2a$10$RK5uMJcfHPCQ5b7v9tVYzu5B3R3d3Y3e5f6g7h8i9j0k1l2m3n4o5"
  同样的密码，每次加密结果不同！因为盐值是随机的
  验证时用 matches() 方法，它会自动提取盐值来比对
```

### 3.3 FileUploadServiceImpl -- 文件上传服务

文件上传是安全风险最高的功能之一，这个实现类做了5层安全校验。

```java
@Slf4j
@Service
@RequiredArgsConstructor  // Lombok注解：自动生成构造函数，用于依赖注入
public class FileUploadServiceImpl implements FileUploadService {

    // @RequiredArgsConstructor + final 字段 = 构造器注入
    // 等价于写了一个构造函数：
    // public FileUploadServiceImpl(FileUploadProperties properties) {
    //     this.properties = properties;
    // }
    private final FileUploadProperties properties;

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String category, String subDir) {
        // ========== 第一层校验：文件非空检查 ==========
        if (file == null || file.isEmpty()) {
            return FileUploadResult.fail("文件不能为空");
        }

        // ========== 第二层校验：文件名安全处理 ==========
        String originalFileName = file.getOriginalFilename();
        originalFileName = FileTypeUtils.sanitizeFileName(originalFileName); // 清洗文件名
        String extension = FileTypeUtils.getExtension(originalFileName);
        String fileType = FileTypeUtils.getFileType(originalFileName);

        // ========== 第三层校验：扩展名 + 文件大小 ==========
        validateFile(file, extension, fileType);
        // validateFile 内部做了：
        //   - 检查是否是危险扩展名（.exe, .bat, .sh 等）
        //   - 检查扩展名是否在允许列表内
        //   - 检查文件大小是否超限

        // ========== 第四层校验：路径遍历防护 ==========
        String safeCategory = normalizePathSegment(category, "common");
        String safeSubDir = normalizePathSegment(subDir, "files");
        // normalizePathSegment 会：
        //   - 转小写
        //   - 去掉特殊字符（只保留字母数字下划线）
        //   - 防止 ../../ 这种路径遍历攻击

        if (!FileTypeUtils.isPathSafe(safeCategory) || !FileTypeUtils.isPathSafe(safeSubDir)) {
            log.warn("检测到路径遍历攻击尝试: category={}, subDir={}", category, subDir);
            return FileUploadResult.fail("非法的文件路径");
        }

        // ========== 第五层校验：文件内容（Magic Byte） ==========
        try {
            validateFileContent(file.getInputStream(), extension);
            // 读取文件头部字节，判断实际类型是否与扩展名一致
            // 比如有人把 .exe 改名为 .jpg，Magic Byte 检查能发现
        } catch (IOException e) {
            return FileUploadResult.fail("文件内容校验失败");
        }

        // ========== 安全校验通过，开始保存文件 ==========
        // 用 UUID 生成新文件名，避免文件名冲突和中文乱码
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = safeSubDir + "/" + safeCategory;
        String fullPath = properties.getBasePath() + "/" + relativePath;

        try {
            Path directory = Paths.get(fullPath).normalize();
            Path basePath = Paths.get(properties.getBasePath()).normalize();

            // 再次检查：确保文件保存路径在允许的根目录下
            // 防止通过符号链接等方式逃逸出上传目录
            if (!directory.startsWith(basePath)) {
                log.warn("检测到路径遍历攻击: fullPath={}, basePath={}", fullPath, properties.getBasePath());
                return FileUploadResult.fail("非法的文件路径");
            }

            // 创建目录（如果不存在）
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 保存文件
            Path filePath = directory.resolve(fileName).normalize();
            file.transferTo(filePath.toFile());

            // 返回上传结果
            String storedPath = "/" + (relativePath + "/" + fileName).replace("\\", "/");
            return FileUploadResult.success(fileName, originalFileName, storedPath, storedPath, fileType, file.getSize());

        } catch (IOException e) {
            log.error("文件上传失败: {}", originalFileName, e);
            return FileUploadResult.fail("文件上传失败: " + e.getMessage());
        }
    }
}
```

**5层安全校验总结：**

```
用户上传文件
     |
     v
[第1层] 文件非空检查 --> 空文件直接拒绝
     |
     v
[第2层] 文件名清洗 --> 去掉特殊字符，防止注入
     |
     v
[第3层] 扩展名+大小检查 --> 危险类型直接拒绝，超限直接拒绝
     |
     v
[第4层] 路径遍历防护 --> 防止 ../../ 攻击
     |
     v
[第5层] Magic Byte检查 --> 防止文件类型伪装
     |
     v
全部通过 --> UUID重命名 --> 保存文件
```

### 3.4 TokenBlacklistServiceImpl -- Token黑名单服务

用户退出登录时，JWT令牌还没过期，怎么让它失效？答案就是黑名单。

```java
@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final int LOCAL_CACHE_MAX_SIZE = 10000;
    private static final long MAX_TOKEN_EXPIRY_HOURS = 2;

    // AtomicBoolean：线程安全的布尔值，用于标记 Redis 是否可用
    private final AtomicBoolean redisAvailable = new AtomicBoolean(true);
    private long lastRedisCheckTime = 0;
    private static final long REDIS_CHECK_INTERVAL = 60000; // 60秒检查一次

    // Caffeine：高性能本地缓存（比 ConcurrentHashMap 更强，支持过期、大小限制）
    private final Cache<String, Boolean> localBlacklist = Caffeine.newBuilder()
            .maximumSize(LOCAL_CACHE_MAX_SIZE)           // 最多缓存10000条
            .expireAfterWrite(MAX_TOKEN_EXPIRY_HOURS, TimeUnit.HOURS) // 2小时后自动过期
            .recordStats()                               // 记录缓存命中率等统计
            .build();

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ========== 添加 Token 到黑名单 ==========
    @Override
    public void addToBlacklist(String token) {
        if (token == null || token.isEmpty()) return;

        String cleanToken = token.replace("Bearer ", ""); // 去掉 Bearer 前缀

        // 解析 Token，获取过期时间
        JwtUtil.TokenInfo tokenInfo = jwtUtil.parseToken(cleanToken);
        if (tokenInfo != null && tokenInfo.getExpiration() != null) {
            Long expirationTime = tokenInfo.getExpiration().getTime();
            long ttl = expirationTime - System.currentTimeMillis(); // 剩余有效时间

            if (ttl > 0) {
                boolean addedToRedis = false;

                // 优先存到 Redis（多实例共享）
                if (isRedisAvailable()) {
                    try {
                        String key = BLACKLIST_PREFIX + cleanToken;
                        // set key value ex ttl：设置键值对，ttl毫秒后自动过期
                        stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
                        addedToRedis = true;
                        markRedisAvailable();
                    } catch (Exception e) {
                        // Redis 挂了？没关系，降级到本地缓存
                        log.warn("Redis 不可用，降级到本地缓存: {}", e.getMessage());
                        markRedisUnavailable();
                    }
                }

                // Redis 不可用时，存到本地 Caffeine 缓存
                if (!addedToRedis) {
                    localBlacklist.put(cleanToken, Boolean.TRUE);
                }
            }
        }
    }

    // ========== 检查 Token 是否在黑名单 ==========
    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) return false;

        String cleanToken = token.replace("Bearer ", "");

        // 先查本地缓存（快，纳秒级）
        Boolean localResult = localBlacklist.getIfPresent(cleanToken);
        if (Boolean.TRUE.equals(localResult)) {
            return true;  // 本地缓存命中，直接返回
        }

        // 再查 Redis（稍慢，毫秒级，但多实例共享）
        if (isRedisAvailable()) {
            try {
                boolean result = Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + cleanToken));
                markRedisAvailable();
                return result;
            } catch (Exception e) {
                log.warn("Redis 不可用，降级到本地缓存查询: {}", e.getMessage());
                markRedisUnavailable();
            }
        }

        // Redis 也不可用，再查一次本地缓存
        return Boolean.TRUE.equals(localBlacklist.getIfPresent(cleanToken));
    }

    // ========== Redis 可用性检测（60秒间隔） ==========
    // 为什么不每次都试？因为 Redis 挂了的时候，每次请求都去试会拖慢响应
    // 60秒试一次，如果恢复了就切回来
    private boolean isRedisAvailable() {
        long now = System.currentTimeMillis();
        if (!redisAvailable.get() && (now - lastRedisCheckTime) < REDIS_CHECK_INTERVAL) {
            return false;  // 上次检测不可用，且不到60秒，不再试
        }
        return true;  // 要么可用，要么超过60秒该重试了
    }
}
```

**双层缓存架构图：**

```
用户退出登录 --> Token 加入黑名单
                      |
                      v
              Redis 可用吗？
               /          \
             是             否
             |              |
      存入 Redis          存入 Caffeine
      (多实例共享)         (仅本机可见)
             |              |
             v              v
      下次请求检查黑名单：
        1. 先查 Caffeine（快）
        2. 再查 Redis（全）
        3. 任一命中 = 已失效
```

---

## 四、常见代码模式

### 4.1 @RequiredArgsConstructor -- 构造器注入

```java
// ===== 推荐写法（本项目 FileUploadServiceImpl 等使用） =====
@Service
@RequiredArgsConstructor  // Lombok 自动生成包含所有 final 字段的构造函数
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {
    private final FileUploadProperties properties;  // final 字段必须通过构造函数赋值
    // 等价于手写：
    // public FileUploadServiceImpl(FileUploadProperties properties) {
    //     this.properties = properties;
    // }
}

// ===== 旧写法（本项目部分类使用） =====
@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {
    @Autowired  // 字段注入，Spring 自动赋值
    private PlantMapper plantMapper;
}

// 两种写法都可以，推荐 @RequiredArgsConstructor + final，因为：
// 1. final 字段不可变，更安全
// 2. 构造器注入是 Spring 官方推荐的方式
// 3. 不用写 @Autowired（Spring 4.3+ 单构造函数自动注入）
```

### 4.2 @Slf4j -- 日志记录

```java
// ===== 推荐写法（用 Lombok） =====
@Slf4j  // 自动生成 private static final Logger log = LoggerFactory.getLogger(当前类.class);
@Service
public class PlantServiceImpl implements PlantService {
    public void someMethod() {
        log.info("查询植物列表");           // 普通信息
        log.debug("搜索关键词: {}", keyword); // 调试信息（生产环境默认不输出）
        log.warn("全文搜索失败，回退到LIKE搜索"); // 警告
        log.error("浏览量增加失败", e);       // 错误（带异常堆栈）
    }
}

// ===== 不用 Lombok 的写法 =====
@Service
public class PlantServiceImpl implements PlantService {
    private static final Logger log = LoggerFactory.getLogger(PlantServiceImpl.class);
    // 效果一样，但每个类都要写这一行，太麻烦
}
```

**日志级别从低到高：**

| 级别 | 用途 | 生产环境是否输出 |
|------|------|----------------|
| `trace` | 最详细的调试信息 | 否 |
| `debug` | 调试信息 | 否（默认） |
| `info` | 重要业务信息 | 是 |
| `warn` | 警告（不影响运行但需关注） | 是 |
| `error` | 错误（影响功能） | 是 |

### 4.3 LambdaQueryWrapper -- 查询构造器

```java
// LambdaQueryWrapper 用 Lambda 表达式引用字段，编译期检查字段名
// 如果字段名拼错了，编译就报错，而不是运行时才发现

// ===== 基本查询 =====
// SQL: SELECT * FROM plant WHERE category = '藤本' ORDER BY name_cn ASC
LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
qw.eq(Plant::getCategory, "藤本")       // 等于
  .orderByAsc(Plant::getNameCn);        // 升序
List<Plant> list = plantMapper.selectList(qw);

// ===== 模糊查询 =====
// SQL: SELECT * FROM plant WHERE name_cn LIKE '%钩藤%'
qw.like(Plant::getNameCn, "钩藤");      // LIKE '%值%'

// ===== 组合条件 =====
// SQL: WHERE (name_cn LIKE '%钩藤%' OR efficacy LIKE '%钩藤%') AND category = '藤本'
qw.and(wrapper -> wrapper
        .like(Plant::getNameCn, "钩藤")
        .or().like(Plant::getEfficacy, "钩藤"))
  .eq(Plant::getCategory, "藤本");

// ===== 限制条数 =====
qw.last("LIMIT 4");  // 直接拼接 SQL 片段（慎用，确保参数安全）
```

### 4.4 ServiceImpl<Mapper, Entity> -- MyBatis-Plus 通用服务

```java
// 继承 ServiceImpl 后，自动获得大量常用方法，不用自己写 SQL：
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> {

    // 直接可用的方法（继承自 ServiceImpl）：
    public void example() {
        Plant plant = getById(1);              // 根据ID查询
        List<Plant> all = list();              // 查询全部
        boolean saved = save(plant);           // 插入一条
        boolean updated = updateById(plant);   // 根据ID更新
        boolean removed = removeById(1);       // 根据ID删除
        long count = count();                  // 查询总数
        Page<Plant> page = page(pageParam, qw); // 分页查询
    }
}
```

---

## 五、常见错误与避坑指南

### 5.1 忘记加 @CacheEvict 导致数据不一致

```java
// ===== 错误：更新了数据但没清缓存 =====
@Override
public void updatePlant(Plant plant) {
    updateById(plant);
    // 缓存里还是旧数据！下次查询会返回过期的信息
}

// ===== 正确：更新数据后清除缓存 =====
@Override
@CacheEvict(value = "plants", allEntries = true)  // 加上这个注解！
public void updatePlant(Plant plant) {
    updateById(plant);
}
```

**规则**：所有增删改方法都要考虑是否需要清缓存。查询方法用 `@Cacheable`，增删改方法用 `@CacheEvict`。

### 5.2 密码明文存储

```java
// ===== 致命错误：密码明文存储 =====
user.setPasswordHash(password);  // 绝对不行！数据库泄露=所有用户密码泄露

// ===== 正确：BCrypt 加密后存储 =====
user.setPasswordHash(passwordEncoder.encode(password));
```

### 5.3 查询结果不判空

```java
// ===== 错误：不判断查询结果就直接使用 =====
public Plant getPlantDetail(Integer id) {
    Plant plant = getById(id);
    return plant.getNameCn();  // 如果 plant 是 null，这里会 NullPointerException！
}

// ===== 正确：先判空 =====
public Plant getPlantDetail(Integer id) {
    Plant plant = getById(id);
    if (plant == null) {
        throw BusinessException.notFound("植物不存在");  // 抛出业务异常
    }
    return plant;
}
```

### 5.4 忘记 @Transactional 导致数据不一致

```java
// ===== 错误：多步操作没有事务保护 =====
public void transferFavorite(Integer fromUserId, Integer toUserId, Integer targetId) {
    removeFavorite(fromUserId, "plant", targetId);   // 第1步成功
    addFavorite(toUserId, "plant", targetId);         // 第2步失败 --> 数据不一致！
    // fromUser 的收藏被删了，但 toUser 没收到
}

// ===== 正确：加事务注解 =====
@Transactional(rollbackFor = Exception.class)  // 任何异常都回滚
public void transferFavorite(Integer fromUserId, Integer toUserId, Integer targetId) {
    removeFavorite(fromUserId, "plant", targetId);
    addFavorite(toUserId, "plant", targetId);
    // 第2步失败时，第1步的操作也会自动撤销
}
```

### 5.5 SQL 注入风险

```java
// ===== 危险：直接拼接用户输入 =====
qw.last("LIMIT " + userInput);  // 如果 userInput 是 "1; DROP TABLE plant;" 就完了

// ===== 安全：使用参数化查询 =====
qw.last("LIMIT " + Math.max(1, Math.min(limit, 100)));  // 强制转为安全数字

// ===== 安全：LIKE 查询时转义特殊字符 =====
String escapedKeyword = PageUtils.escapeLike(keyword);  // % -> \%, _ -> \_
qw.like(Plant::getNameCn, escapedKeyword);
```

### 5.6 异常处理不当

```java
// ===== 错误1：吞掉异常，不记录日志 =====
try {
    plantMapper.incrementViewCount(id);
} catch (Exception e) {
    // 什么都没做！出了问题完全不知道
}

// ===== 错误2：抛出大异常，暴露内部信息 =====
try {
    // ...
} catch (Exception e) {
    throw new RuntimeException("数据库查询失败: " + e.getMessage());
    // e.getMessage() 可能包含表名、字段名等敏感信息
}

// ===== 正确：记录日志 + 抛出业务异常 =====
try {
    plantMapper.incrementViewCount(id);
} catch (Exception e) {
    log.error("浏览量增加失败, plantId={}", id, e);  // 记录完整日志
    // 浏览量增加失败不影响主流程，不抛异常
    // 如果是关键操作，应该抛出 BusinessException
}
```

### 5.7 返回敏感信息

```java
// ===== 错误：把密码哈希返回给前端 =====
public User getUserInfo(Integer userId) {
    return getById(userId);  // User 对象包含 passwordHash 字段！
}

// ===== 正确：返回前清除敏感字段 =====
public User getUserInfo(Integer userId) {
    User user = getById(userId);
    if (user != null) {
        user.setPasswordHash(null);  // 清除密码哈希
    }
    return user;
}
```

---

## 六、实现类继承关系图

```
                    ServiceImpl<Mapper, Entity>
                    (MyBatis-Plus 通用服务基类)
                           |
           +---------------+---------------+---------------+
           |               |               |               |
   PlantServiceImpl  KnowledgeServiceImpl  InheritorServiceImpl  ...
   (继承通用CRUD)     (继承通用CRUD)       (继承通用CRUD)
           |               |               |               |
           +-- getById()   +-- getById()   +-- getById()
           +-- list()      +-- list()      +-- list()
           +-- save()      +-- save()      +-- save()
           +-- updateById()+-- updateById()+-- updateById()
           +-- removeById()+-- removeById()+-- removeById()
           +-- page()      +-- page()      +-- page()
           +-- count()     +-- count()     +-- count()
           |               |               |
           +-- 自己的业务方法  +-- 自己的业务方法  +-- 自己的业务方法

   不继承 ServiceImpl 的：
   FileUploadServiceImpl  (没有对应的数据库表)
   TokenBlacklistServiceImpl  (操作 Redis 和 Caffeine，不操作数据库)
   AiChatServiceImpl  (调用外部 API，不操作数据库)
```

---

## 七、代码审查与改进建议

以下是对 Service 实现层代码的审查发现，按严重程度排序：

### 严重级别

| # | 级别 | 问题 | 涉及实现类 | 说明 |
|---|------|------|-----------|------|
| 1 | 严重-安全 | `getUserToken()` 允许通过用户ID直接生成登录Token | `UserServiceImpl` | 完全绕过身份验证，是严重权限提升漏洞。任何知道用户ID的人都可以伪造登录态，应删除此方法或增加严格的权限校验。 |
| 2 | 严重-安全 | `submit()` 中 `correctCount` 和 `totalCount` 完全由客户端提供 | `PlantGameServiceImpl` | 恶意用户可提交任意分数，破坏游戏公平性。应在服务端根据题目答案重新计算正确数量和总数，而非信任客户端提交的值。 |
| 3 | 严重-安全 | 验证码生成使用 `java.util.Random` 而非 `SecureRandom` | `CaptchaService` | `java.util.Random` 是伪随机数生成器，种子可被推算，验证码可预测。应替换为 `java.security.SecureRandom`，确保验证码的不可预测性。 |

### 高级别

| # | 级别 | 问题 | 涉及实现类 | 说明 |
|---|------|------|-----------|------|
| 4 | 高-并发 | `addFavorite()` 先查询后插入不是原子操作 | `FavoriteServiceImpl` | 并发场景下多个请求可能同时通过 `getOne()` 唯一性检查，然后都执行 `save()`，导致重复收藏。应依赖数据库唯一索引（user_id + target_type + target_id）来保证原子性，或在 Service 层使用分布式锁。 |
| 5 | 高-逻辑 | `@Async` 自调用失效 | `KnowledgeServiceImpl` | `incrementPopularityAsync()` 在同类内部被调用，Spring AOP 基于代理机制，内部调用 `this.incrementPopularityAsync()` 绕过了代理，`@Async` 注解不生效，实际仍为同步执行。解决方式：将异步方法提取到独立的 Service 类中，或通过 `ApplicationContext` 获取代理对象调用。 |
| 6 | 高-安全 | `login()` 无登录失败次数限制 | `UserServiceImpl` | 没有对登录失败进行计数和限制，攻击者可无限次尝试密码，容易遭受暴力破解。应增加失败计数机制，如5次失败后锁定账号15分钟，或引入验证码二次验证。 |
| 7 | 高-安全 | Sa-Token 的 `kickout` 参数类型(Integer)与 `login` 参数类型(String)不匹配 | `UserServiceImpl` | `StpUtil.login()` 接收的参数类型为 `Object`，但 `StpUtil.kickout()` 期望的参数类型与登录时传入的类型一致。若 `login` 传入 `String` 而 `kickout` 传入 `Integer`，会导致踢人功能失效，用户无法被正确踢下线。 |
| 8 | 高-逻辑 | `listByCategoryAndKeywordAndType()` 忽略了 `fileType` 参数 | `ResourceServiceImpl` | 方法签名接收 `fileType` 参数，但查询逻辑中未使用该参数构建过滤条件。而缓存 key 却包含了 `fileType`，导致不同 `fileType` 的请求缓存了相同的错误结果，且缓存 key 不同造成缓存浪费。 |

### 中等级别

| # | 级别 | 问题 | 涉及实现类 | 说明 |
|---|------|------|-----------|------|
| 9 | 中等-性能 | 每次 `save` 都触发全表 `COUNT(*)` 查询 | `OperationLogServiceImpl` | 在保存日志时调用 `count()` 检查日志总数是否超限，高频操作场景下每次都执行 `SELECT COUNT(*) FROM operation_log`，性能极差。应改用定时任务异步清理超限日志，或将总数缓存到 Redis 中。 |
| 10 | 中等-内存 | `listAllDTO()` 和 `listAllApproved()` 无分页限制 | `CommentServiceImpl` | 查询全部评论数据并转为 DTO 列表返回，没有分页限制。当评论数据量增长到数万条时，可能导致 OOM（内存溢出）。应强制分页查询或限制最大返回条数。 |
| 11 | 中等-安全 | 异常信息泄露文件系统路径 | `FileUploadServiceImpl` | 文件上传失败时，异常消息中包含服务器文件系统的完整路径信息（如 `d:\uploads\...`），攻击者可利用此信息进行路径遍历攻击。应返回通用错误信息如"文件上传失败"，将详细路径仅记录到服务器日志。 |
| 12 | 中等-逻辑 | 答案比较使用 `equals` 过于严格 | `QuizServiceImpl` | 测验评分时直接使用 `String.equals()` 比较用户答案与正确答案，不考虑前后空格和大小写差异。用户输入 "钩藤 " 或 "钩藤"（含空格）会被判错。应先 `trim()` 并统一转为小写后再比较。 |
| 13 | 中等-逻辑 | 日志说"降级为同步保存"但实际只是抛出异常 | `RabbitMQOperationLogServiceImpl` | 当 RabbitMQ 不可用时，日志记录"降级为同步保存"，但实际代码只是抛出异常，并未真正实现同步降级写入数据库。日志信息具有误导性，且 RabbitMQ 不可用时操作日志会丢失。应真正实现降级逻辑，回退到直接调用 `OperationLogService.save()` 同步写入。 |

### 低级别

| # | 级别 | 问题 | 涉及实现类 | 说明 |
|---|------|------|-----------|------|
| 14 | 低-规范 | 依赖注入方式不一致 | 多个 `ServiceImpl` | 部分类使用 `@Autowired` 字段注入（如 `PlantServiceImpl`、`UserServiceImpl`），部分类使用 `@RequiredArgsConstructor` 构造器注入（如 `FileUploadServiceImpl`）。应统一为构造器注入（Spring 官方推荐），确保依赖不可变且便于测试。 |
| 15 | 低-规范 | 冗余 Mapper 注入 | 多个 `ServiceImpl` | 如 `PlantServiceImpl` 继承了 `ServiceImpl<PlantMapper, Plant>`，同时 `@Autowired` 注入了 `PlantMapper`。`ServiceImpl` 已内置 `baseMapper` 字段提供相同的 Mapper 实例，无需重复注入。 |
| 16 | 低-规范 | 缓存策略不一致 | 多个 `ServiceImpl` | 部分查询方法有 `@Cacheable` 注解（如 `advancedSearch()`），但对应的分页版本方法（如 `advancedSearchPaged()`）却没有缓存，导致分页请求每次都查数据库。同一业务场景的缓存策略应保持一致。 |
