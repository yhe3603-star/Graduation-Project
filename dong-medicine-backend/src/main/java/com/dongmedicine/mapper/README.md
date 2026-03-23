# Mapper接口目录说明

## 文件夹结构

本目录包含项目中使用的MyBatis Plus Mapper接口，用于数据库操作。

```
mapper/
├── CommentMapper.java           # 评论Mapper
├── FavoriteMapper.java          # 收藏Mapper
├── FeedbackMapper.java          # 意见反馈Mapper
├── InheritorMapper.java         # 传承人Mapper
├── KnowledgeMapper.java         # 知识Mapper
├── OperationLogMapper.java      # 操作日志Mapper
├── PlantGameRecordMapper.java   # 植物游戏记录Mapper
├── PlantMapper.java             # 药材Mapper
├── QaMapper.java                # 问答Mapper
├── QuizQuestionMapper.java      # 答题题目Mapper
├── QuizRecordMapper.java        # 答题记录Mapper
├── ResourceMapper.java          # 资源Mapper
└── UserMapper.java              # 用户Mapper
```

## 详细说明

### 1. CommentMapper.java

**功能**：评论数据访问接口，对应`comments`表。

**继承**：`BaseMapper<Comment>`

**主要方法**：
- `selectByTarget`：根据目标类型和ID查询评论
- `selectByUserId`：根据用户ID查询评论
- `selectByStatus`：根据状态查询评论

### 2. FavoriteMapper.java

**功能**：收藏数据访问接口，对应`favorites`表。

**继承**：`BaseMapper<Favorite>`

**主要方法**：
- `selectByUserAndTarget`：根据用户ID和目标信息查询收藏
- `selectByUserId`：根据用户ID查询收藏列表

### 3. FeedbackMapper.java

**功能**：意见反馈数据访问接口，对应`feedback`表。

**继承**：`BaseMapper<Feedback>`

**主要方法**：
- `selectByStatus`：根据状态查询反馈
- `selectByUserId`：根据用户ID查询反馈

### 4. InheritorMapper.java

**功能**：传承人数据访问接口，对应`inheritors`表。

**继承**：`BaseMapper<Inheritor>`

**主要方法**：
- `selectByLevel`：根据级别查询传承人
- `selectBySpecialty`：根据技艺特色查询传承人

### 5. KnowledgeMapper.java

**功能**：知识数据访问接口，对应`knowledge`表。

**继承**：`BaseMapper<Knowledge>`

**主要方法**：
- `selectByType`：根据类型查询知识
- `selectByTherapyCategory`：根据疗法分类查询知识
- `selectByDiseaseCategory`：根据疾病分类查询知识

### 6. OperationLogMapper.java

**功能**：操作日志数据访问接口，对应`operation_log`表。

**继承**：`BaseMapper<OperationLog>`

**主要方法**：
- `selectByModule`：根据模块查询操作日志
- `selectByUserId`：根据用户ID查询操作日志
- `selectByType`：根据操作类型查询操作日志

### 7. PlantGameRecordMapper.java

**功能**：植物游戏记录数据访问接口，对应`plant_game_record`表。

**继承**：`BaseMapper<PlantGameRecord>`

**主要方法**：
- `selectByUserId`：根据用户ID查询游戏记录
- `selectTopScores`：查询最高分记录

### 8. PlantMapper.java

**功能**：药材数据访问接口，对应`plants`表。

**继承**：`BaseMapper<Plant>`

**主要方法**：
- `selectByCategory`：根据分类查询药材
- `selectByUsageWay`：根据用法查询药材
- `selectByViewCount`：根据浏览量排序查询
- `selectByFavoriteCount`：根据收藏量排序查询

### 9. QaMapper.java

**功能**：问答数据访问接口，对应`qa`表。

**继承**：`BaseMapper<Qa>`

**主要方法**：
- `selectByCategory`：根据分类查询问答
- `selectByPopularity`：根据热度排序查询

### 10. QuizQuestionMapper.java

**功能**：答题题目数据访问接口，对应`quiz_questions`表。

**继承**：`BaseMapper<QuizQuestion>`

**主要方法**：
- `selectByCategory`：根据分类查询题目
- `selectByDifficulty`：根据难度查询题目

### 11. QuizRecordMapper.java

**功能**：答题记录数据访问接口，对应`quiz_record`表。

**继承**：`BaseMapper<QuizRecord>`

**主要方法**：
- `selectByUserId`：根据用户ID查询答题记录
- `selectTopScores`：查询最高分记录

### 12. ResourceMapper.java

**功能**：资源数据访问接口，对应`resources`表。

**继承**：`BaseMapper<Resource>`

**主要方法**：
- `selectByCategory`：根据分类查询资源
- `selectByDownloadCount`：根据下载量排序查询

### 13. UserMapper.java

**功能**：用户数据访问接口，对应`users`表。

**继承**：`BaseMapper<User>`

**主要方法**：
- `selectByUsername`：根据用户名查询用户
- `selectByEmail`：根据邮箱查询用户
- `selectByPhone`：根据手机号查询用户
- `selectByRole`：根据角色查询用户

## Mapper使用指南

### 1. 注入Mapper

```java
@Autowired
private PlantMapper plantMapper;
```

### 2. 基本CRUD操作

```java
// 查询
Plant plant = plantMapper.selectById(1L);

// 插入
Plant plant = new Plant();
plant.setName("人参");
plantMapper.insert(plant);

// 更新
Plant plant = plantMapper.selectById(1L);
plant.setName("西洋参");
plantMapper.updateById(plant);

// 删除
plantMapper.deleteById(1L);
```

### 3. 条件查询

```java
// 使用QueryWrapper
QueryWrapper<Plant> wrapper = new QueryWrapper<>();
wrapper.eq("category", "根茎类")
       .like("name", "参")
       .orderByDesc("view_count");
List<Plant> plants = plantMapper.selectList(wrapper);

// 使用LambdaQueryWrapper
LambdaQueryWrapper<Plant> lambdaWrapper = new LambdaQueryWrapper<>();
lambdaWrapper.eq(Plant::getCategory, "根茎类")
             .like(Plant::getName, "参")
             .orderByDesc(Plant::getViewCount);
List<Plant> plants = plantMapper.selectList(lambdaWrapper);
```

### 4. 自定义SQL

```java
// 在Mapper接口中定义方法
@Select("SELECT * FROM plants WHERE category = #{category} ORDER BY view_count DESC LIMIT #{limit}")
List<Plant> selectPopularByCategory(@Param("category") String category, @Param("limit") int limit);

// 调用方法
List<Plant> popularPlants = plantMapper.selectPopularByCategory("根茎类", 10);
```

## 开发规范

1. **命名规范**：Mapper接口名使用大驼峰命名法，以`Mapper`结尾
2. **方法命名**：方法名使用小驼峰命名法，动词+名词形式
3. **参数规范**：参数应该有明确的含义，使用`@Param`注解指定参数名
4. **返回值规范**：返回值应该明确，避免使用`Object`类型
5. **SQL规范**：SQL语句应该简洁明了，避免复杂的嵌套查询
6. **注释规范**：为每个方法添加注释说明

## 注意事项

- Mapper接口应该只包含数据库操作方法，不包含业务逻辑
- 避免在Mapper中编写复杂的业务逻辑
- 合理使用索引，提高查询性能
- 注意SQL注入问题，使用参数化查询
- 定期检查和优化SQL语句

---

**最后更新时间**：2026年3月23日