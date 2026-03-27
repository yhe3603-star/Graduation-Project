# 服务层目录说明

## 文件夹结构

本目录包含项目的所有服务接口和实现类，负责业务逻辑处理。

```
service/
├── impl/                        # 服务实现类
│   ├── UserServiceImpl.java
│   ├── PlantServiceImpl.java
│   ├── KnowledgeServiceImpl.java
│   ├── InheritorServiceImpl.java
│   ├── ResourceServiceImpl.java
│   ├── QaServiceImpl.java
│   ├── QuizServiceImpl.java
│   ├── PlantGameServiceImpl.java
│   ├── CommentServiceImpl.java
│   ├── FavoriteServiceImpl.java
│   ├── FeedbackServiceImpl.java
│   ├── FileUploadServiceImpl.java
│   ├── TokenBlacklistServiceImpl.java
│   ├── OperationLogServiceImpl.java
│   └── AiChatServiceImpl.java
├── UserService.java              # 用户服务接口
├── PlantService.java             # 药材服务接口
├── KnowledgeService.java         # 知识服务接口
├── InheritorService.java         # 传承人服务接口
├── ResourceService.java          # 资源服务接口
├── QaService.java                # 问答服务接口
├── QuizService.java              # 答题服务接口
├── PlantGameService.java         # 植物游戏服务接口
├── CommentService.java           # 评论服务接口
├── FavoriteService.java          # 收藏服务接口
├── FeedbackService.java          # 反馈服务接口
├── FileUploadService.java        # 文件上传服务接口
├── TokenBlacklistService.java    # Token黑名单服务接口
├── OperationLogService.java      # 操作日志服务接口
└── README.md                     # 说明文档
```

## 详细说明

### 1. UserService.java - 用户服务接口

**功能**：用户相关的业务操作。

**方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `login(username, password)` | String | 用户登录，返回JWT令牌 |
| `register(username, password)` | void | 用户注册 |
| `getUserInfo(userId)` | User | 获取用户信息 |
| `getUserByUsername(username)` | User | 根据用户名获取用户 |
| `deleteUser(userId)` | void | 删除用户 |
| `getAllUsers()` | List<User> | 获取所有用户 |
| `changePassword(userId, currentPwd, newPwd)` | void | 修改密码 |
| `updateUserRole(userId, role)` | void | 更新用户角色 |
| `banUser(userId, reason)` | void | 封禁用户 |
| `unbanUser(userId)` | void | 解封用户 |
| `getUserToken(userId)` | String | 获取用户Token |

### 2. PlantService.java - 药材服务接口

**功能**：药用植物相关的业务操作。

**方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `advancedSearch(keyword, category, usageWay)` | List<Plant> | 高级搜索 |
| `advancedSearchPaged(...)` | Page<Plant> | 分页高级搜索 |
| `search(keyword)` | List<Plant> | 关键词搜索 |
| `searchPaged(keyword, page, size)` | Page<Plant> | 分页搜索 |
| `search(keyword, limit)` | List<Plant> | 限制数量搜索 |
| `getSimilarPlants(id)` | List<Plant> | 获取相似植物 |
| `getDetailWithStory(id)` | Plant | 获取详情（含故事） |
| `getRandomByDifficulty(difficulty, limit)` | List<Plant> | 按难度随机获取 |
| `incrementViewCount(id)` | void | 增加浏览次数 |
| `clearCache()` | void | 清除缓存 |
| `deleteWithFiles(id)` | void | 删除植物及关联文件 |

### 3. KnowledgeService.java - 知识服务接口

**功能**：知识库相关的业务操作。

**主要方法**：
- 分页查询知识列表
- 根据ID获取知识详情
- 增加浏览次数
- 搜索知识
- 删除知识及关联文件

### 4. InheritorService.java - 传承人服务接口

**功能**：传承人相关的业务操作。

**主要方法**：
- 分页查询传承人列表
- 根据ID获取传承人详情
- 增加浏览次数
- 删除传承人及关联文件

### 5. ResourceService.java - 资源服务接口

**功能**：学习资源相关的业务操作。

**主要方法**：
- 分页查询资源列表
- 根据ID获取资源详情
- 增加浏览/下载次数
- 删除资源及关联文件

### 6. QaService.java - 问答服务接口

**功能**：问答相关的业务操作。

**主要方法**：
- 分页查询问答列表
- 根据ID获取问答详情
- 增加浏览次数
- 搜索问答

### 7. QuizService.java - 答题服务接口

**功能**：答题相关的业务操作。

**方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getRandomQuestions()` | List<QuizQuestionDTO> | 获取随机题目 |
| `submit(userId, answers)` | Integer | 提交答案并返回得分 |
| `calculateScore(answers)` | Integer | 计算得分 |
| `getUserRecords(userId)` | List<QuizRecord> | 获取用户答题记录 |
| `deleteQuestion(questionId)` | void | 删除题目 |
| `getAllQuestions()` | List<QuizQuestion> | 获取所有题目 |
| `pageQuestions(page, size)` | Page<QuizQuestion> | 分页获取题目 |
| `countQuestions()` | long | 统计题目数量 |
| `addQuestionDirect(question)` | void | 直接添加题目 |
| `updateQuestionDirect(question)` | void | 直接更新题目 |

### 8. PlantGameService.java - 植物游戏服务接口

**功能**：植物识别游戏相关的业务操作。

**主要方法**：
- 获取游戏题目
- 提交游戏答案
- 获取用户游戏记录
- 获取排行榜

### 9. CommentService.java - 评论服务接口

**功能**：评论相关的业务操作。

**主要方法**：
- 分页查询评论列表
- 添加评论
- 删除评论
- 点赞评论
- 审核评论

### 10. FavoriteService.java - 收藏服务接口

**功能**：收藏相关的业务操作。

**主要方法**：
- 添加收藏
- 取消收藏
- 获取用户收藏列表
- 检查是否已收藏

### 11. FeedbackService.java - 反馈服务接口

**功能**：意见反馈相关的业务操作。

**主要方法**：
- 提交反馈
- 获取反馈列表
- 回复反馈
- 更新反馈状态

### 12. FileUploadService.java - 文件上传服务接口

**功能**：文件上传相关的业务操作。

**主要方法**：
- 上传图片
- 上传视频
- 上传文档
- 删除文件

### 13. TokenBlacklistService.java - Token黑名单服务接口

**功能**：JWT令牌黑名单管理。

**主要方法**：
- 添加Token到黑名单
- 检查Token是否在黑名单
- 清理过期Token

### 14. OperationLogService.java - 操作日志服务接口

**功能**：操作日志相关的业务操作。

**主要方法**：
- 保存操作日志
- 分页查询日志
- 删除日志
- 批量删除日志
- 清空所有日志

### 15. AiChatService.java - AI对话服务接口

**功能**：AI对话相关的业务操作。

**主要方法**：
- 发送对话消息
- 获取对话历史

## 服务统计

| 服务接口 | 实现类 | 主要用途 |
|---------|-------|---------|
| UserService | UserServiceImpl | 用户管理 |
| PlantService | PlantServiceImpl | 药材管理 |
| KnowledgeService | KnowledgeServiceImpl | 知识管理 |
| InheritorService | InheritorServiceImpl | 传承人管理 |
| ResourceService | ResourceServiceImpl | 资源管理 |
| QaService | QaServiceImpl | 问答管理 |
| QuizService | QuizServiceImpl | 答题管理 |
| PlantGameService | PlantGameServiceImpl | 游戏管理 |
| CommentService | CommentServiceImpl | 评论管理 |
| FavoriteService | FavoriteServiceImpl | 收藏管理 |
| FeedbackService | FeedbackServiceImpl | 反馈管理 |
| FileUploadService | FileUploadServiceImpl | 文件上传 |
| TokenBlacklistService | TokenBlacklistServiceImpl | Token管理 |
| OperationLogService | OperationLogServiceImpl | 日志管理 |
| AiChatService | AiChatServiceImpl | AI对话 |
| **总计** | **15个接口 + 15个实现** | - |

## 开发规范

1. **接口定义**：
   - 服务接口继承`IService<T>`（MyBatis-Plus提供）
   - 接口定义业务方法，不包含实现细节
   - 方法命名清晰，参数和返回值明确

2. **实现类规范**：
   - 实现类放在`impl`子目录
   - 使用`@Service`注解
   - 注入Mapper或其他依赖
   - 包含缓存、事务等处理

3. **事务管理**：
   - 使用`@Transactional`注解
   - 合理设置事务传播行为
   - 注意事务边界

4. **缓存使用**：
   - 使用`@Cacheable`、`@CacheEvict`等注解
   - 合理设置缓存过期时间
   - 注意缓存一致性

5. **异常处理**：
   - 抛出`BusinessException`业务异常
   - 异常信息清晰明确
   - 记录异常日志

## 使用示例

### 注入服务
```java
@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {
    
    @Autowired
    private PlantMapper plantMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    @Cacheable(value = "plants", key = "#id")
    public Plant getDetailWithStory(Integer id) {
        return plantMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public void deleteWithFiles(Integer id) {
        Plant plant = plantMapper.selectById(id);
        if (plant != null) {
            // 删除关联文件
            deleteFiles(plant);
            // 删除数据库记录
            plantMapper.deleteById(id);
        }
    }
}
```

### 调用服务
```java
@RestController
@RequestMapping("/api/plants")
public class PlantController {
    
    @Autowired
    private PlantService plantService;
    
    @GetMapping("/{id}")
    public R<Plant> getDetail(@PathVariable Integer id) {
        Plant plant = plantService.getDetailWithStory(id);
        return R.ok(plant);
    }
}
```

---

## 事务管理详解

### 事务注解使用

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        save(user);
        
        if (dto.getRoles() != null) {
            userRoleMapper.insertBatch(user.getId(), dto.getRoles());
        }
    }
}
```

### 事务传播行为

| 传播行为 | 说明 | 使用场景 |
|---------|------|---------|
| `REQUIRED`（默认） | 有事务则加入，无则新建 | 大部分场景 |
| `REQUIRES_NEW` | 总是新建事务 | 独立日志记录 |
| `SUPPORTS` | 有事务则加入，无则非事务执行 | 查询操作 |
| `NOT_SUPPORTED` | 非事务执行 | 不需要事务的操作 |
| `MANDATORY` | 必须在事务中调用 | 强制事务 |
| `NEVER` | 必须非事务调用 | 禁止事务 |
| `NESTED` | 嵌套事务 | 部分回滚 |

### 事务示例

#### 1. 基本事务

```java
@Transactional(rollbackFor = Exception.class)
public void deletePlant(Integer id) {
    Plant plant = getById(id);
    if (plant == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    
    removeById(id);
    favoriteMapper.deleteByPlantId(id);
    commentMapper.deleteByPlantId(id);
}
```

#### 2. 嵌套事务

```java
@Service
public class OrderServiceImpl {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Transactional
    public void createOrder(OrderDTO dto) {
        Order order = saveOrder(dto);
        
        try {
            inventoryService.deductStock(order);
        } catch (Exception e) {
            throw new BusinessException("库存扣减失败");
        }
        
        paymentService.processPayment(order);
    }
}

@Service
public class InventoryServiceImpl {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deductStock(Order order) {
        // 独立事务，失败不影响外部事务
    }
}
```

#### 3. 只读事务

```java
@Transactional(readOnly = true)
public Page<Plant> searchPlants(String keyword, Integer page, Integer size) {
    LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(Plant::getName, keyword)
           .or()
           .like(Plant::getAlias, keyword);
    return page(new Page<>(page, size), wrapper);
}
```

#### 4. 超时设置

```java
@Transactional(timeout = 30)  // 30秒超时
public void longRunningOperation() {
    // 长时间运行的操作
}
```

### 事务注意事项

```java
// 错误示例：同类方法调用，事务不生效
@Service
public class UserServiceImpl {
    
    public void methodA() {
        methodB();  // 事务不生效
    }
    
    @Transactional
    public void methodB() {
        // ...
    }
}

// 正确示例：通过注入自身调用
@Service
public class UserServiceImpl {
    
    @Autowired
    private UserService self;
    
    public void methodA() {
        self.methodB();  // 事务生效
    }
    
    @Transactional
    public void methodB() {
        // ...
    }
}
```

---

## 缓存使用详解

### 缓存注解

| 注解 | 说明 |
|------|------|
| `@Cacheable` | 查询时使用缓存 |
| `@CachePut` | 更新缓存 |
| `@CacheEvict` | 删除缓存 |
| `@Caching` | 组合多个缓存操作 |

### 缓存示例

#### 1. 查询缓存

```java
@Cacheable(value = "plants", key = "#id")
public Plant getById(Integer id) {
    return plantMapper.selectById(id);
}

@Cacheable(value = "plants", key = "'list:' + #page + ':' + #size")
public Page<Plant> page(Integer page, Integer size) {
    return page(new Page<>(page, size), null);
}
```

#### 2. 更新缓存

```java
@CachePut(value = "plants", key = "#id")
@CacheEvict(value = "plants", key = "'list:*'")
public Plant update(Integer id, PlantDTO dto) {
    Plant plant = getById(id);
    BeanUtils.copyProperties(dto, plant);
    updateById(plant);
    return plant;
}
```

#### 3. 删除缓存

```java
@CacheEvict(value = "plants", key = "#id")
public void delete(Integer id) {
    removeById(id);
}

@CacheEvict(value = "plants", allEntries = true)
public void clearAllCache() {
    // 清除所有plants缓存
}
```

#### 4. 组合缓存

```java
@Caching(
    evict = {
        @CacheEvict(value = "plants", key = "#id"),
        @CacheEvict(value = "plantDetail", key = "#id")
    }
)
public void deleteWithCache(Integer id) {
    removeById(id);
}
```

---

## 已知限制

| 服务 | 限制 | 影响 |
|------|------|------|
| UserService | 不支持OAuth2 | 第三方登录需扩展 |
| PlantService | 搜索不支持模糊匹配拼音 | 拼音搜索需扩展 |
| QuizService | 题目随机算法简单 | 可能出现重复 |
| FileUploadService | 单文件最大100MB | 大文件需分片 |
| AiChatService | 依赖DeepSeek服务 | 服务不可用时无法使用 |
| FavoriteService | 不支持批量操作 | 批量收藏需多次请求 |
| CommentService | 不支持楼中楼 | 无法嵌套回复 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **缓存优化**
   - 实现多级缓存
   - 添加缓存预热
   - 优化缓存命中率

2. **事务优化**
   - 添加事务监控
   - 优化事务边界
   - 实现分布式事务

3. **性能优化**
   - 添加批量操作
   - 优化查询性能
   - 实现异步处理

### 中期改进 (1-2月)

1. **功能增强**
   - 添加拼音搜索
   - 实现批量收藏
   - 支持嵌套评论

2. **可观测性**
   - 添加服务监控
   - 实现链路追踪
   - 性能统计分析

3. **测试覆盖**
   - 编写单元测试
   - 添加集成测试
   - 性能测试

### 长期规划 (3-6月)

1. **微服务拆分**
   - 服务边界划分
   - 独立部署
   - 服务治理

2. **分布式支持**
   - 分布式事务
   - 分布式缓存
   - 消息队列

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2+ | 框架基础 |
| MyBatis-Plus | 3.5+ | ORM框架 |
| Spring Data Redis | 3.2+ | 缓存 |
| Spring TX | 6.1+ | 事务管理 |

---

## 常见问题

### 1. 事务不生效怎么办？

```java
// 检查以下几点：
// 1. 方法是否为public
// 2. 是否通过代理调用（不是this调用）
// 3. 异常是否被捕获（需要抛出RuntimeException）
// 4. 是否配置了事务管理器

@Service
public class MyServiceImpl {
    
    @Transactional(rollbackFor = Exception.class)
    public void myMethod() {
        try {
            // 业务逻辑
        } catch (Exception e) {
            throw new BusinessException(e);  // 必须抛出才能回滚
        }
    }
}
```

### 2. 如何处理并发问题？

```java
@Service
public class PlantServiceImpl {
    
    @Transactional
    public void incrementViewCount(Integer id) {
        // 使用乐观锁
        Plant plant = getById(id);
        plant.setViewCount(plant.getViewCount() + 1);
        updateById(plant);
    }
    
    // 或使用Redis原子操作
    public void incrementViewCountAsync(Integer id) {
        String key = "plant:view:" + id;
        redisTemplate.opsForValue().increment(key);
    }
}
```

### 3. 如何实现软删除？

```java
@Service
public class PlantServiceImpl {
    
    @Override
    public void delete(Integer id) {
        Plant plant = getById(id);
        plant.setDeleted(true);
        plant.setDeletedAt(LocalDateTime.now());
        updateById(plant);
    }
    
    // MyBatis-Plus配置
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}

// Entity配置
@TableLogic
private Boolean deleted;
```

### 4. 如何实现批量操作？

```java
@Service
public class PlantServiceImpl {
    
    @Transactional
    public void batchInsert(List<PlantDTO> dtos) {
        List<Plant> plants = dtos.stream()
            .map(dto -> {
                Plant plant = new Plant();
                BeanUtils.copyProperties(dto, plant);
                return plant;
            })
            .collect(Collectors.toList());
        saveBatch(plants, 500);  // 每批500条
    }
    
    @Transactional
    public void batchDelete(List<Integer> ids) {
        removeByIds(ids);
    }
}
```

### 5. 如何实现异步处理？

```java
@Service
public class PlantServiceImpl {
    
    @Async
    @Transactional
    public CompletableFuture<Void> processAsync(Integer id) {
        Plant plant = getById(id);
        // 耗时操作
        processPlant(plant);
        return CompletableFuture.completedFuture(null);
    }
}
```

### 6. 如何处理大文件上传？

```java
@Service
public class FileUploadServiceImpl {
    
    public String uploadLargeFile(MultipartFile file) {
        String tempPath = saveTempFile(file);
        
        // 分片上传到OSS
        String url = ossService.uploadByChunk(tempPath);
        
        // 清理临时文件
        cleanupTempFile(tempPath);
        
        return url;
    }
}
```

---

**最后更新时间**：2026年3月28日
