# Mapper接口目录说明

## 文件夹结构

本目录包含项目的所有Mapper接口，用于数据库操作。

```
mapper/
├── UserMapper.java           # 用户Mapper
├── PlantMapper.java          # 药材Mapper
├── KnowledgeMapper.java      # 知识Mapper
├── InheritorMapper.java      # 传承人Mapper
├── ResourceMapper.java       # 资源Mapper
├── QaMapper.java             # 问答Mapper
├── QuizQuestionMapper.java   # 答题题目Mapper
├── QuizRecordMapper.java     # 答题记录Mapper
├── PlantGameRecordMapper.java# 植物游戏记录Mapper
├── CommentMapper.java        # 评论Mapper
├── FavoriteMapper.java       # 收藏Mapper
├── FeedbackMapper.java       # 反馈Mapper
├── OperationLogMapper.java   # 操作日志Mapper
└── README.md                 # 说明文档
```

## 详细说明

### 1. UserMapper.java - 用户Mapper

**对应实体**：`User`

**继承**：`BaseMapper<User>`

**基础方法**（继承自BaseMapper）：
| 方法 | 说明 |
|------|------|
| `selectById(id)` | 根据ID查询 |
| `selectList(wrapper)` | 条件查询列表 |
| `selectPage(page, wrapper)` | 分页查询 |
| `insert(entity)` | 插入 |
| `updateById(entity)` | 根据ID更新 |
| `deleteById(id)` | 根据ID删除 |

### 2. PlantMapper.java - 药材Mapper

**对应实体**：`Plant`

**继承**：`BaseMapper<Plant>`

**自定义方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `incrementViewCount(id)` | void | 增加浏览次数 |
| `incrementFavoriteCount(id, delta)` | void | 增加收藏次数（delta可为负数） |
| `selectRandomByDifficulty(difficulty, limit)` | List<Plant> | 按难度随机查询 |
| `searchByFullText(keyword, limit)` | List<Plant> | 全文搜索（需要FULLTEXT索引） |
| `searchByLike(keyword, limit)` | List<Plant> | 模糊搜索（LIKE查询） |

**SQL示例**：
```sql
-- 增加浏览次数
UPDATE plants SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}

-- 随机查询
SELECT * FROM plants WHERE difficulty = #{difficulty} 
AND id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM plants WHERE difficulty = #{difficulty})) + 1) 
LIMIT #{limit}

-- 全文搜索（需要FULLTEXT索引）
SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy, story) 
AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) ORDER BY id DESC LIMIT #{limit}
```

### 3. KnowledgeMapper.java - 知识Mapper

**对应实体**：`Knowledge`

**继承**：`BaseMapper<Knowledge>`

### 4. InheritorMapper.java - 传承人Mapper

**对应实体**：`Inheritor`

**继承**：`BaseMapper<Inheritor>`

### 5. ResourceMapper.java - 资源Mapper

**对应实体**：`Resource`

**继承**：`BaseMapper<Resource>`

### 6. QaMapper.java - 问答Mapper

**对应实体**：`Qa`

**继承**：`BaseMapper<Qa>`

### 7. QuizQuestionMapper.java - 答题题目Mapper

**对应实体**：`QuizQuestion`

**继承**：`BaseMapper<QuizQuestion>`

### 8. QuizRecordMapper.java - 答题记录Mapper

**对应实体**：`QuizRecord`

**继承**：`BaseMapper<QuizRecord>`

### 9. PlantGameRecordMapper.java - 植物游戏记录Mapper

**对应实体**：`PlantGameRecord`

**继承**：`BaseMapper<PlantGameRecord>`

### 10. CommentMapper.java - 评论Mapper

**对应实体**：`Comment`

**继承**：`BaseMapper<Comment>`

### 11. FavoriteMapper.java - 收藏Mapper

**对应实体**：`Favorite`

**继承**：`BaseMapper<Favorite>`

### 12. FeedbackMapper.java - 反馈Mapper

**对应实体**：`Feedback`

**继承**：`BaseMapper<Feedback>`

### 13. OperationLogMapper.java - 操作日志Mapper

**对应实体**：`OperationLog`

**继承**：`BaseMapper<OperationLog>`

## Mapper统计

| Mapper | 对应实体 | 自定义方法 | 主要用途 |
|--------|---------|-----------|---------|
| UserMapper | User | 0 | 用户数据操作 |
| PlantMapper | Plant | 5 | 药材数据操作 |
| KnowledgeMapper | Knowledge | 0 | 知识数据操作 |
| InheritorMapper | Inheritor | 0 | 传承人数据操作 |
| ResourceMapper | Resource | 0 | 资源数据操作 |
| QaMapper | Qa | 0 | 问答数据操作 |
| QuizQuestionMapper | QuizQuestion | 0 | 答题题目操作 |
| QuizRecordMapper | QuizRecord | 0 | 答题记录操作 |
| PlantGameRecordMapper | PlantGameRecord | 0 | 游戏记录操作 |
| CommentMapper | Comment | 0 | 评论数据操作 |
| FavoriteMapper | Favorite | 0 | 收藏数据操作 |
| FeedbackMapper | Feedback | 0 | 反馈数据操作 |
| OperationLogMapper | OperationLog | 0 | 日志数据操作 |
| **总计** | **13个** | **5个** | - |

## BaseMapper常用方法

MyBatis-Plus的`BaseMapper`提供了丰富的CRUD方法：

### 查询方法
| 方法 | 说明 |
|------|------|
| `selectById(Serializable id)` | 根据ID查询 |
| `selectBatchIds(Collection ids)` | 根据ID批量查询 |
| `selectByMap(Map columnMap)` | 根据列条件查询 |
| `selectOne(Wrapper wrapper)` | 根据条件查询一条 |
| `selectList(Wrapper wrapper)` | 根据条件查询列表 |
| `selectPage(Page page, Wrapper wrapper)` | 分页查询 |
| `selectCount(Wrapper wrapper)` | 查询总数 |

### 插入方法
| 方法 | 说明 |
|------|------|
| `insert(T entity)` | 插入一条记录 |

### 更新方法
| 方法 | 说明 |
|------|------|
| `updateById(T entity)` | 根据ID更新 |
| `update(T entity, Wrapper wrapper)` | 根据条件更新 |

### 删除方法
| 方法 | 说明 |
|------|------|
| `deleteById(Serializable id)` | 根据ID删除 |
| `deleteBatchIds(Collection ids)` | 根据ID批量删除 |
| `deleteByMap(Map columnMap)` | 根据列条件删除 |
| `delete(Wrapper wrapper)` | 根据条件删除 |

## 开发规范

1. **注解使用**：
   - `@Mapper`：MyBatis注解，标记为Mapper接口
   - `@Select/@Update/@Insert/@Delete`：自定义SQL注解
   - `@Param`：参数命名注解

2. **命名规范**：
   - 接口名使用大驼峰命名法，以Mapper结尾
   - 方法名使用小驼峰命名法

3. **继承规范**：
   - 所有Mapper接口继承`BaseMapper<T>`
   - 泛型T为对应的实体类

4. **自定义方法**：
   - 简单SQL使用注解方式
   - 复杂SQL使用XML映射文件

## 使用示例

### 基础CRUD
```java
// 查询
User user = userMapper.selectById(1);
List<User> users = userMapper.selectList(null);

// 插入
User newUser = new User();
newUser.setUsername("test");
userMapper.insert(newUser);

// 更新
user.setUsername("newName");
userMapper.updateById(user);

// 删除
userMapper.deleteById(1);
```

### 条件查询
```java
// 使用QueryWrapper
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("username", "admin")
       .eq("status", "active");
List<User> users = userMapper.selectList(wrapper);

// 使用LambdaQueryWrapper
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, "admin")
       .eq(User::getStatus, "active");
List<User> users = userMapper.selectList(wrapper);
```

### 分页查询
```java
Page<User> page = new Page<>(1, 10); // 第1页，每页10条
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, "active");
Page<User> result = userMapper.selectPage(page, wrapper);
```

---

## 已知限制

| Mapper | 限制 | 影响 |
|--------|------|------|
| PlantMapper | 全文搜索依赖FULLTEXT索引 | 需要先创建索引 |
| PlantMapper | 随机查询性能较低 | 大数据量时较慢 |
| 所有Mapper | 不支持多租户 | SaaS场景需扩展 |
| 所有Mapper | 不支持读写分离 | 高并发需扩展 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **性能优化**
   - 添加查询缓存
   - 优化随机查询算法
   - 添加批量操作方法

2. **功能增强**
   - 添加逻辑删除支持
   - 实现数据权限过滤
   - 添加审计字段自动填充

### 中期改进 (1-2月)

1. **多数据源支持**
   - 配置读写分离
   - 支持动态数据源切换

2. **监控集成**
   - 添加SQL执行监控
   - 慢查询分析

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| MyBatis-Plus | 3.5+ | ORM框架 |
| MySQL Connector | 8.0+ | 数据库驱动 |

---

## 常见问题

### 1. 如何添加自定义SQL？

```java
@Select("SELECT * FROM plants WHERE category = #{category} LIMIT #{limit}")
List<Plant> selectByCategory(@Param("category") String category, @Param("limit") int limit);

@Update("UPDATE plants SET view_count = view_count + 1 WHERE id = #{id}")
void incrementViewCount(@Param("id") Integer id);
```

### 2. 如何使用XML映射文件？

```xml
<!-- resources/mapper/PlantMapper.xml -->
<mapper namespace="com.dongmedicine.mapper.PlantMapper">
    <select id="selectByCategory" resultType="Plant">
        SELECT * FROM plants WHERE category = #{category}
    </select>
</mapper>
```

### 3. 如何实现批量插入？

```java
// 使用MyBatis-Plus提供的批量方法
List<Plant> plants = Arrays.asList(plant1, plant2, plant3);
plantMapper.insertBatch(plants);

// 或使用Service层的saveBatch
plantService.saveBatch(plants, 500);  // 每批500条
```

### 4. 如何处理复杂查询？

```java
// 使用QueryWrapper链式调用
LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
wrapper.like(StringUtils.hasText(keyword), Plant::getNameCn, keyword)
       .eq(StringUtils.hasText(category), Plant::getCategory, category)
       .orderByDesc(Plant::getViewCount)
       .last("LIMIT 10");
List<Plant> plants = plantMapper.selectList(wrapper);
```

---

**最后更新时间**：2026年3月30日
