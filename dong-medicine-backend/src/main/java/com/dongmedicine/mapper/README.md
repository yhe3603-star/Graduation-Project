# 数据访问层目录 (mapper)

本目录存放MyBatis Mapper接口，负责与数据库交互。

## 📖 什么是Mapper？

Mapper是MyBatis的数据访问层接口，它定义了操作数据库的方法。每个Mapper对应一个实体类，提供该实体类的CRUD操作。

## 📁 文件列表

| 文件名 | 对应实体 | 功能说明 |
|--------|----------|----------|
| `UserMapper.java` | User | 用户数据访问 |
| `PlantMapper.java` | Plant | 植物数据访问 |
| `KnowledgeMapper.java` | Knowledge | 知识数据访问 |
| `InheritorMapper.java` | Inheritor | 传承人数据访问 |
| `ResourceMapper.java` | Resource | 资源数据访问 |
| `QaMapper.java` | Qa | 问答数据访问 |
| `QuizQuestionMapper.java` | QuizQuestion | 答题题目数据访问 |
| `QuizRecordMapper.java` | QuizRecord | 答题记录数据访问 |
| `PlantGameRecordMapper.java` | PlantGameRecord | 植物游戏记录数据访问 |
| `CommentMapper.java` | Comment | 评论数据访问 |
| `FavoriteMapper.java` | Favorite | 收藏数据访问 |
| `FeedbackMapper.java` | Feedback | 反馈数据访问 |
| `OperationLogMapper.java` | OperationLog | 操作日志数据访问 |

## 📦 详细说明

### 1. UserMapper.java - 用户数据访问

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `selectByUsername(String username)` | 根据用户名查询 |
| `selectByEmail(String email)` | 根据邮箱查询 |
| `updatePassword(Integer id, String password)` | 更新密码 |

### 2. PlantMapper.java - 植物数据访问

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `selectRandomPlants(int limit)` | 随机获取植物 |
| `selectByCategory(String category)` | 按分类查询 |
| `incrementViewCount(Integer id)` | 增加浏览次数 |

### 3. QuizQuestionMapper.java - 答题题目数据访问

**主要方法:**
| 方法名 | 功能说明 |
|--------|----------|
| `selectRandomQuestions(int limit)` | 随机获取题目 |
| `selectByCategory(String category)` | 按分类查询 |

## 🎯 Mapper规范

### 基本结构
```java
@Mapper
public interface ExampleMapper extends BaseMapper<Example> {
    
    // 继承BaseMapper后，自动拥有以下方法：
    // - insert(entity)
    // - deleteById(id)
    // - updateById(entity)
    // - selectById(id)
    // - selectList(wrapper)
    // - selectPage(page, wrapper)
    
    // 自定义方法
    @Select("SELECT * FROM example WHERE name = #{name}")
    Example selectByName(@Param("name") String name);
    
    @Select("SELECT * FROM example WHERE status = #{status} ORDER BY create_time DESC")
    List<Example> selectByStatus(@Param("status") Integer status);
}
```

### 注解说明

| 注解 | 说明 | 示例 |
|------|------|------|
| `@Mapper` | 标记为Mapper接口 | `@Mapper` |
| `@Select` | 查询SQL | `@Select("SELECT * FROM table")` |
| `@Insert` | 插入SQL | `@Insert("INSERT INTO ...")` |
| `@Update` | 更新SQL | `@Update("UPDATE ...")` |
| `@Delete` | 删除SQL | `@Delete("DELETE FROM ...")` |
| `@Param` | 参数命名 | `@Param("name")` |

### 查询示例
```java
// 简单查询
@Select("SELECT * FROM users WHERE username = #{username}")
User selectByUsername(@Param("username") String username);

// 多条件查询
@Select("SELECT * FROM plants WHERE category = #{category} AND status = 1")
List<Plant> selectByCategory(@Param("category") String category);

// 分页查询
@Select("SELECT * FROM knowledge WHERE type = #{type} ORDER BY create_time DESC")
IPage<Knowledge> selectPageByType(Page<Knowledge> page, @Param("type") String type);

// 随机查询
@Select("SELECT * FROM plants ORDER BY RAND() LIMIT #{limit}")
List<Plant> selectRandomPlants(@Param("limit") int limit);

// 统计查询
@Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
int countByRole(@Param("role") String role);
```

### 更新示例
```java
// 简单更新
@Update("UPDATE users SET status = #{status} WHERE id = #{id}")
int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

// 增加字段值
@Update("UPDATE plants SET view_count = view_count + 1 WHERE id = #{id}")
int incrementViewCount(@Param("id") Integer id);

// 批量更新
@Update("<script>" +
        "UPDATE users SET status = 0 WHERE id IN " +
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
        "#{id}" +
        "</foreach>" +
        "</script>")
int batchDisable(@Param("ids") List<Integer> ids);
```

### 使用Wrapper查询
```java
// 在Service中使用Wrapper
public List<Plant> searchPlants(String keyword) {
    LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.hasText(keyword), Plant::getNameCn, keyword)
           .or()
           .like(StringUtils.hasText(keyword), Plant::getScientificName, keyword)
           .eq(Plant::getStatus, 1)
           .orderByDesc(Plant::getViewCount);
    return plantMapper.selectList(wrapper);
}
```

### 最佳实践
1. **继承BaseMapper**: 继承后自动拥有基础CRUD方法
2. **使用注解**: 简单SQL使用注解，复杂SQL使用XML
3. **参数命名**: 使用@Param注解命名参数
4. **返回类型**: 查询返回实体或集合，更新返回影响行数
5. **命名规范**: 方法名以select/insert/update/delete开头

## 📚 扩展阅读

- [MyBatis Plus Mapper](https://baomidou.com/guide/mapper.html)
- [MyBatis 注解开发](https://mybatis.org/mybatis-3/zh/java-api.html)
- [MyBatis Plus 条件构造器](https://baomidou.com/guide/wrapper.html)
