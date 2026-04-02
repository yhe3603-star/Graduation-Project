# 服务层 (service)

本目录存放服务接口和服务实现类，负责处理核心业务逻辑。

## 📖 什么是服务层？

服务层是业务逻辑的核心，它位于控制器和数据访问层之间。服务层的职责：
- 处理复杂的业务逻辑
- 管理事务
- 调用多个Mapper完成业务操作
- 提供可复用的业务方法

## 📁 文件列表

### 服务接口

| 文件名 | 功能说明 |
|--------|----------|
| `UserService.java` | 用户服务接口 |
| `PlantService.java` | 植物服务接口 |
| `KnowledgeService.java` | 知识库服务接口 |
| `InheritorService.java` | 传承人服务接口 |
| `ResourceService.java` | 资源服务接口 |
| `QaService.java` | 问答服务接口 |
| `QuizService.java` | 答题服务接口 |
| `PlantGameService.java` | 植物游戏服务接口 |
| `CommentService.java` | 评论服务接口 |
| `FavoriteService.java` | 收藏服务接口 |
| `FeedbackService.java` | 反馈服务接口 |
| `FileUploadService.java` | 文件上传服务接口 |
| `AiChatService.java` | AI聊天服务接口 |
| `OperationLogService.java` | 操作日志服务接口 |

### 服务实现 (impl/)

| 文件名 | 功能说明 |
|--------|----------|
| `UserServiceImpl.java` | 用户服务实现 |
| `PlantServiceImpl.java` | 植物服务实现 |
| `KnowledgeServiceImpl.java` | 知识库服务实现 |
| `InheritorServiceImpl.java` | 传承人服务实现 |
| `ResourceServiceImpl.java` | 资源服务实现 |
| `QaServiceImpl.java` | 问答服务实现 |
| `QuizServiceImpl.java` | 答题服务实现 |
| `PlantGameServiceImpl.java` | 植物游戏服务实现 |
| `CommentServiceImpl.java` | 评论服务实现 |
| `FavoriteServiceImpl.java` | 收藏服务实现 |
| `FeedbackServiceImpl.java` | 反馈服务实现 |
| `FileUploadServiceImpl.java` | 文件上传服务实现 |
| `AiChatServiceImpl.java` | AI聊天服务实现 |
| `OperationLogServiceImpl.java` | 操作日志服务实现 |

## 📦 详细说明

### 1. UserService - 用户服务

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `register(RegisterDTO dto)` | 用户注册 |
| `login(LoginDTO dto)` | 用户登录 |
| `getById(Integer id)` | 根据ID获取用户 |
| `updateUser(UserUpdateDTO dto)` | 更新用户信息 |
| `changePassword(ChangePasswordDTO dto)` | 修改密码 |
| `checkUsername(String username)` | 检查用户名是否存在 |

### 2. PlantService - 植物服务

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `getPageList(int page, int size, String keyword)` | 分页查询植物列表 |
| `getById(Integer id)` | 获取植物详情 |
| `getRandomPlants(int limit)` | 随机获取植物 |
| `addPlant(Plant plant)` | 添加植物 |
| `updatePlant(Plant plant)` | 更新植物 |
| `deletePlant(Integer id)` | 删除植物 |
| `incrementViewCount(Integer id)` | 增加浏览次数 |

### 3. QuizService - 答题服务

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `getRandomQuestions(int count)` | 随机获取题目 |
| `submit(Integer userId, List<AnswerDTO> answers, int scorePerQuestion)` | 提交答案 |
| `calculateScore(List<AnswerDTO> answers, int scorePerQuestion)` | 计算得分 |
| `getUserRecords(Integer userId)` | 获取用户答题记录 |

### 4. FavoriteService - 收藏服务

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `addFavorite(Integer userId, String targetType, Integer targetId)` | 添加收藏 |
| `removeFavorite(Integer userId, String targetType, Integer targetId)` | 取消收藏 |
| `isFavorited(Integer userId, String targetType, Integer targetId)` | 检查是否已收藏 |
| `getUserFavorites(Integer userId, String targetType)` | 获取用户收藏列表 |

## 🎯 服务层规范

### 服务接口定义
```java
public interface ExampleService {
    /**
     * 获取列表
     * @param page 页码
     * @param size 每页数量
     * @return 分页数据
     */
    Page<Example> getPageList(int page, int size);
    
    /**
     * 根据ID获取
     * @param id 主键ID
     * @return 实体对象
     */
    Example getById(Integer id);
    
    /**
     * 添加
     * @param dto 数据传输对象
     */
    void add(ExampleDTO dto);
    
    /**
     * 更新
     * @param dto 数据传输对象
     */
    void update(ExampleDTO dto);
    
    /**
     * 删除
     * @param id 主键ID
     */
    void delete(Integer id);
}
```

### 服务实现类
```java
@Service
@RequiredArgsConstructor
public class ExampleServiceImpl implements ExampleService {
    
    private final ExampleMapper exampleMapper;
    
    @Override
    public Page<Example> getPageList(int page, int size) {
        return exampleMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Example>()
                .orderByDesc(Example::getCreateTime)
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ExampleDTO dto) {
        Example example = new Example();
        BeanUtils.copyProperties(dto, example);
        exampleMapper.insert(example);
    }
}
```

### 事务管理
```java
@Service
public class OrderServiceImpl implements OrderService {
    
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderDTO dto) {
        // 1. 创建订单
        orderMapper.insert(order);
        
        // 2. 扣减库存
        inventoryService.reduceStock(dto.getProductId(), dto.getQuantity());
        
        // 3. 扣减余额
        userService.deductBalance(dto.getUserId(), dto.getAmount());
        
        // 如果任何一步失败，整个事务都会回滚
    }
}
```

### 最佳实践
1. **接口与实现分离**: 定义接口便于扩展和测试
2. **单一职责**: 每个服务只负责一个业务领域
3. **事务管理**: 涉及多表操作时使用@Transactional
4. **异常处理**: 抛出BusinessException业务异常
5. **日志记录**: 关键操作添加日志

## 📚 扩展阅读

- [Spring Service 层设计](https://spring.io/guides/gs/rest-service/)
- [Spring 事务管理](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [MyBatis Plus 服务层](https://baomidou.com/guide/service.html)
