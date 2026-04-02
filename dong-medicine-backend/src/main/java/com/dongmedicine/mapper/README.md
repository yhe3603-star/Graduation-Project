# 数据访问层 (mapper)

本目录存放 MyBatis Mapper 接口，负责与数据库交互。

## 目录

- [什么是Mapper？](#什么是mapper)
- [目录结构](#目录结构)
- [Mapper列表](#mapper列表)
- [开发规范](#开发规范)
- [常用Mapper详解](#常用mapper详解)

---

## 什么是Mapper？

### Mapper的概念

**Mapper** 是 MyBatis 中用于操作数据库的接口。它就像一个"翻译官"——把 Java 方法调用翻译成 SQL 语句，然后把 SQL 执行结果翻译成 Java 对象。

### Mapper的作用

```
┌─────────────────────────────────────────────────────────────────┐
│                        数据访问流程                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Service层调用Mapper方法                                         │
│       │                                                         │
│       ▼                                                         │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     Mapper 层                           │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  User user = userMapper.selectById(1);          │    │   │
│  │  │                                                   │    │   │
│  │  │  ↓ MyBatis自动翻译                               │    │   │
│  │  │                                                   │    │   │
│  │  │  SELECT * FROM users WHERE id = 1                │    │   │
│  │  │                                                   │    │   │
│  │  │  ↓ 执行SQL                                       │    │   │
│  │  │                                                   │    │   │
│  │  │  MySQL返回结果集                                  │    │   │
│  │  │                                                   │    │   │
│  │  │  ↓ MyBatis自动映射                               │    │   │
│  │  │                                                   │    │   │
│  │  │  User对象 { id: 1, username: "张三", ... }       │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### MyBatis Plus的优势

本项目使用 MyBatis Plus，它在 MyBatis 基础上提供了：

1. **无需写SQL**：常用CRUD操作自动生成
2. **条件构造器**：使用Java代码构建查询条件
3. **分页插件**：自动处理分页
4. **代码生成器**：自动生成Entity、Mapper代码

---

## 目录结构

```
mapper/
│
├── UserMapper.java                    # 用户数据访问
├── PlantMapper.java                   # 药用植物数据访问
├── KnowledgeMapper.java               # 知识库数据访问
├── InheritorMapper.java               # 传承人数据访问
├── ResourceMapper.java                # 学习资源数据访问
├── QaMapper.java                      # 问答数据访问
├── CommentMapper.java                 # 评论数据访问
├── FavoriteMapper.java                # 收藏数据访问
├── FeedbackMapper.java                # 反馈数据访问
├── QuizQuestionMapper.java            # 测验题目数据访问
├── QuizRecordMapper.java              # 测验记录数据访问
├── PlantGameRecordMapper.java         # 植物游戏记录数据访问
└── OperationLogMapper.java            # 操作日志数据访问
```

---

## Mapper列表

| Mapper | 对应表 | 功能描述 |
|--------|--------|----------|
| UserMapper | users | 用户数据访问 |
| PlantMapper | plants | 药用植物数据访问 |
| KnowledgeMapper | knowledge | 知识库数据访问 |
| InheritorMapper | inheritors | 传承人数据访问 |
| ResourceMapper | resources | 学习资源数据访问 |
| QaMapper | qa | 问答数据访问 |
| CommentMapper | comments | 评论数据访问 |
| FavoriteMapper | favorites | 收藏数据访问 |
| FeedbackMapper | feedback | 反馈数据访问 |
| QuizQuestionMapper | quiz_questions | 测验题目数据访问 |
| QuizRecordMapper | quiz_record | 测验记录数据访问 |
| PlantGameRecordMapper | plant_game_record | 植物游戏记录数据访问 |
| OperationLogMapper | operation_log | 操作日志数据访问 |

---

## 开发规范

### 1. Mapper接口规范

```java
/**
 * 用户数据访问接口
 * 继承 BaseMapper 自动获得 CRUD 方法
 */
@Mapper  // 标记为 Mapper 接口
public interface UserMapper extends BaseMapper<User> {
    
    // BaseMapper 已提供的方法（无需编写）：
    // - int insert(User entity)
    // - int deleteById(Serializable id)
    // - int updateById(User entity)
    // - User selectById(Serializable id)
    // - List<User> selectList(Wrapper<User> wrapper)
    // - Page<User> selectPage(Page<User> page, Wrapper<User> wrapper)
    
    // 自定义方法...
}
```

### 2. 自定义SQL规范

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     * 使用 @Select 注解直接写SQL
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    /**
     * 更新用户状态
     * 使用 @Update 注解
     */
    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 批量插入
     * 复杂SQL可以使用XML文件
     */
    void insertBatch(@Param("list") List<User> users);
}
```

### 3. 参数规范

```java
@Mapper
public interface ExampleMapper extends BaseMapper<Example> {
    
    // 单个参数：可以不加 @Param
    @Select("SELECT * FROM example WHERE id = #{id}")
    Example findById(Integer id);
    
    // 多个参数：必须加 @Param
    @Select("SELECT * FROM example WHERE name = #{name} AND status = #{status}")
    List<Example> findByNameAndStatus(
        @Param("name") String name,
        @Param("status") Integer status
    );
    
    // 对象参数：不需要 @Param
    @Insert("INSERT INTO example(name, status) VALUES(#{name}, #{status})")
    int insert(Example example);
}
```

---

## 常用Mapper详解

### UserMapper - 用户数据访问

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    /**
     * 更新用户状态
     */
    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 统计用户数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
    int countByRole(@Param("role") String role);
}
```

### PlantMapper - 植物数据访问

```java
@Mapper
public interface PlantMapper extends BaseMapper<Plant> {
    
    /**
     * 随机获取植物
     */
    @Select("SELECT * FROM plants ORDER BY RAND() LIMIT #{limit}")
    List<Plant> selectRandom(@Param("limit") int limit);
    
    /**
     * 增加浏览次数
     */
    @Update("UPDATE plants SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Integer id);
    
    /**
     * 全文搜索
     */
    @Select("SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)")
    List<Plant> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 按分类统计
     */
    @Select("SELECT category, COUNT(*) as count FROM plants GROUP BY category")
    List<Map<String, Object>> countByCategory();
}
```

### QuizQuestionMapper - 测验题目数据访问

```java
@Mapper
public interface QuizQuestionMapper extends BaseMapper<QuizQuestion> {
    
    /**
     * 随机获取问题
     */
    @Select("SELECT * FROM quiz_questions ORDER BY RAND() LIMIT #{limit}")
    List<QuizQuestion> selectRandomQuestions(@Param("limit") int limit);
}
```

### 使用 BaseMapper 的 CRUD 操作

```java
@Service
@RequiredArgsConstructor
public class PlantServiceImpl implements PlantService {
    
    private final PlantMapper plantMapper;
    
    // 插入
    public void add(Plant plant) {
        plantMapper.insert(plant);
    }
    
    // 根据ID删除
    public void delete(Integer id) {
        plantMapper.deleteById(id);
    }
    
    // 更新
    public void update(Plant plant) {
        plantMapper.updateById(plant);
    }
    
    // 根据ID查询
    public Plant getById(Integer id) {
        return plantMapper.selectById(id);
    }
    
    // 条件查询列表
    public List<Plant> list(String category) {
        LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), Plant::getCategory, category);
        return plantMapper.selectList(wrapper);
    }
    
    // 分页查询
    public Page<Plant> page(int pageNum, int pageSize) {
        Page<Plant> page = new Page<>(pageNum, pageSize);
        return plantMapper.selectPage(page, null);
    }
}
```

---

## 条件构造器

### LambdaQueryWrapper - 查询条件构造

```java
// 基本条件
LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Plant::getCategory, "根茎类");           // 等于
wrapper.like(Plant::getNameCn, "黄");               // 模糊查询
wrapper.in(Plant::getId, Arrays.asList(1, 2, 3));   // IN查询
wrapper.between(Plant::getViewCount, 100, 500);     // 范围查询
wrapper.isNull(Plant::getStory);                    // 为空
wrapper.isNotNull(Plant::getImages);                // 不为空

// 排序
wrapper.orderByDesc(Plant::getViewCount);           // 降序
wrapper.orderByAsc(Plant::getNameCn);               // 升序

// 组合条件
wrapper.and(w -> w
    .eq(Plant::getStatus, 1)
    .or()
    .isNull(Plant::getStatus)
);

// 执行查询
List<Plant> plants = plantMapper.selectList(wrapper);
```

### LambdaUpdateWrapper - 更新条件构造

```java
LambdaUpdateWrapper<Plant> wrapper = new LambdaUpdateWrapper<>();
wrapper.eq(Plant::getId, 1);                        // 条件
wrapper.set(Plant::getViewCount, 100);              // 设置值
wrapper.set(Plant::getUpdatedAt, LocalDateTime.now());

plantMapper.update(null, wrapper);
```

---

## 最佳实践

### 1. 使用 LambdaQueryWrapper

```java
// 好的做法：使用Lambda，类型安全
wrapper.eq(Plant::getCategory, category);

// 不好的做法：使用字符串，容易出错
wrapper.eq("category", category);
```

### 2. 避免SQL注入

```java
// 好的做法：使用参数化查询
@Select("SELECT * FROM plants WHERE name = #{name}")
Plant findByName(@Param("name") String name);

// 不好的做法：拼接SQL（有注入风险）
@Select("SELECT * FROM plants WHERE name = '" + name + "'")
Plant findByName(String name);
```

### 3. 合理使用索引

```java
// 查询条件应该使用有索引的字段
wrapper.eq(Plant::getCategory, category);  // category有索引

// 避免对无索引字段进行模糊查询
wrapper.like(Plant::getStory, keyword);    // story可能没有索引，性能差
```

---

**相关文档**

- [MyBatis Plus 官方文档](https://baomidou.com/)
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/zh/index.html)

---

**最后更新时间**：2026年4月3日
