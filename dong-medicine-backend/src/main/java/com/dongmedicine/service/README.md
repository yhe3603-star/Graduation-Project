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

**最后更新时间**：2026年3月25日
