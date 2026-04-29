# Mapper 层 -- 数据访问层（仓库管理员）

> 本目录存放所有 Mapper 接口，负责与数据库进行交互。

---

## 一、什么是 Mapper？

**类比：仓库管理员，知道东西放在哪、怎么取**

想象数据库是一个巨大的仓库，里面整齐地摆放着各种数据（植物、知识、用户...）。
Mapper 就是那个仓库管理员：

- 你跟他说"帮我找 id=1 的植物"，他立刻去仓库找到给你
- 你跟他说"新增一个用户"，他帮你把数据放到正确的位置
- 你跟他说"删除这条评论"，他帮你把数据从仓库移走

你不需要知道仓库的内部结构，只需要告诉管理员你要什么，他帮你搞定。

```
  Service 层（业务员）         Mapper 层（仓库管理员）         数据库（仓库）
+------------------+         +------------------+         +------------------+
| 我要查钩藤的信息   |  ---->  | SELECT * FROM    |  ---->  | plants 表        |
|                  |         | plants WHERE     |         | id=1, name_cn=   |
| 我要新增一个用户   |  ---->  | id = 1           |         | "钩藤" ...       |
|                  |         | INSERT INTO      |  ---->  | users 表         |
+------------------+         | users ...        |         +------------------+
                             +------------------+
```

---

## 二、什么是 MyBatis-Plus？

**类比：自动翻译机，不用手写 SQL**

传统 MyBatis 需要你手写每一条 SQL 语句：

```xml
<!-- 传统 MyBatis：每个操作都要写 XML -->
<select id="selectById" resultType="Plant">
    SELECT * FROM plants WHERE id = #{id}
</select>
<insert id="insert">
    INSERT INTO plants (name_cn, name_dong, ...) VALUES (#{nameCn}, #{nameDong}, ...)
</insert>
<!-- 还有 update、delete、分页查询... 写到手软 -->
```

MyBatis-Plus 是 MyBatis 的增强版，像一台**自动翻译机**：

```
你写的代码：                          MyBatis-Plus 自动翻译成：
plantMapper.selectById(1)        -->  SELECT * FROM plants WHERE id = 1
plantMapper.insert(plant)        -->  INSERT INTO plants (name_cn, ...) VALUES (...)
plantMapper.updateById(plant)    -->  UPDATE plants SET name_cn = ... WHERE id = ...
plantMapper.deleteById(1)        -->  DELETE FROM plants WHERE id = 1
```

**核心思想**：常见的增删改查（CRUD）操作不用写 SQL，MyBatis-Plus 帮你自动生成！
只有特殊需求才需要手写 SQL。

---

## 三、BaseMapper<T> -- 继承即拥有 CRUD 超能力

### 3.1 什么是 BaseMapper？

MyBatis-Plus 提供了一个通用接口 `BaseMapper<T>`，只要你的 Mapper 继承它，
就自动拥有了以下方法，**一个字都不用写**：

```
                    BaseMapper<Plant>
                         |
          +--------------+--------------+
          |              |              |
     增(CREATE)      查(READ)      改/删(UPDATE/DELETE)
          |              |              |
    insert()        selectById()    updateById()
                    selectList()    deleteById()
                    selectPage()    deleteBatchIds()
                    selectCount()   ...
                    ...（共 17+ 个方法）
```

### 3.2 最简 Mapper 示例

```java
@Mapper                              // [1] 告诉 Spring：这是一个 Mapper 接口
public interface UserMapper extends BaseMapper<User> {   // [2] 继承 BaseMapper
    // [3] 里面什么都不用写！已经自动拥有了所有 CRUD 方法！
}
```

**三行代码，你就拥有了对 users 表的全部操作能力！**

### 3.3 继承 BaseMapper 后自动拥有的方法

以下方法**不用写任何代码**就能直接使用：

| 方法 | 作用 | 等价 SQL |
|------|------|---------|
| `insert(entity)` | 插入一条记录 | `INSERT INTO ...` |
| `deleteById(id)` | 根据 ID 删除 | `DELETE FROM ... WHERE id = ?` |
| `updateById(entity)` | 根据 ID 更新 | `UPDATE ... SET ... WHERE id = ?` |
| `selectById(id)` | 根据 ID 查询 | `SELECT * FROM ... WHERE id = ?` |
| `selectBatchIds(idList)` | 批量查询 | `SELECT * FROM ... WHERE id IN (...)` |
| `selectList(wrapper)` | 条件查询列表 | `SELECT * FROM ... WHERE ...` |
| `selectPage(page, wrapper)` | 分页查询 | `SELECT * FROM ... WHERE ... LIMIT ...` |
| `selectCount(wrapper)` | 查询总数 | `SELECT COUNT(*) FROM ... WHERE ...` |

---

## 四、完整示例：PlantMapper -- 带自定义方法

当 BaseMapper 提供的方法不够用时，可以在 Mapper 中自定义方法：

```java
package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;   // [1] 导入 BaseMapper
import com.dongmedicine.entity.Plant;                     // [2] 导入实体类
import org.apache.ibatis.annotations.Mapper;              // [3] 导入 @Mapper 注解
import org.apache.ibatis.annotations.Param;               // [4] 导入参数注解
import org.apache.ibatis.annotations.Select;              // [5] 导入查询注解
import org.apache.ibatis.annotations.Update;              // [6] 导入更新注解

import java.util.List;

@Mapper   // [7] 必须加！告诉 Spring 这是一个 Mapper，让 Spring 自动扫描注册
public interface PlantMapper extends BaseMapper<Plant> {  // [8] 继承 BaseMapper<Plant>

    // ========== 以下是自定义方法 ==========

    // [9] 增加浏览量：自定义 UPDATE 语句
    @Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    // [10] 增加收藏量：delta 可以是 +1 或 -1
    @Update("UPDATE plants SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    // [11] 随机查询植物：用于植物识别游戏
    @Select("SELECT * FROM plants WHERE id >= " +
            "(SELECT FLOOR(RAND() * (SELECT MAX(id) FROM plants)) + 1) LIMIT #{limit}")
    List<Plant> selectRandomPlants(@Param("limit") int limit);

    // [12] 全文搜索：利用 MySQL 全文索引，速度快
    @Select("SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy, story) " +
            "AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByFullText(@Param("keyword") String keyword,
                                  @Param("limit") int limit);

    // [13] 模糊搜索：LIKE 查询，全文索引不可用时的备选方案
    @Select("SELECT * FROM plants WHERE " +
            "name_cn LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "name_dong LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "efficacy LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "story LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByLike(@Param("keyword") String keyword,
                              @Param("limit") int limit);
}
```

**关键注解说明**：

| 注解 | 作用 | 示例 |
|------|------|------|
| `@Mapper` | 标记接口为 Mapper，让 Spring 自动扫描 | `@Mapper` 放在接口上 |
| `@Select` | 编写 SELECT 查询 SQL | `@Select("SELECT * FROM ...")` |
| `@Update` | 编写 UPDATE 更新 SQL | `@Update("UPDATE ... SET ...")` |
| `@Param` | 给 SQL 中的参数起名字 | `@Param("keyword") String keyword` |

---

## 五、LambdaQueryWrapper -- 类型安全的查询构造器

### 5.1 什么是 LambdaQueryWrapper？

**类比：搭积木式的查询构建器**

传统 SQL 查询是写字符串，容易写错列名，而且编译器不会帮你检查。
LambdaQueryWrapper 让你用 Java 代码"搭积木"一样构建查询条件：

```java
// 传统方式：写字符串，列名写错了编译器不会报错！
// "name_cn LIKE '%钩%'"  写成 "name_cn LIKE '%钩%'"  少个下划线？运行时才报错！

// LambdaQueryWrapper 方式：用 Java 方法引用，写错了编译器直接报错！
LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
wrapper.like(Plant::getNameCn, "钩")       // name_cn LIKE '%钩%'
       .eq(Plant::getCategory, "藤本类")    // AND category = '藤本类'
       .orderByDesc(Plant::getPopularity);  // ORDER BY popularity DESC
```

### 5.2 常用查询方法对照表

| LambdaQueryWrapper 方法 | 含义 | 等价 SQL |
|------------------------|------|---------|
| `eq(字段, 值)` | 等于 | `WHERE 字段 = 值` |
| `ne(字段, 值)` | 不等于 | `WHERE 字段 != 值` |
| `like(字段, 值)` | 模糊查询（前后%） | `WHERE 字段 LIKE '%值%'` |
| `likeLeft(字段, 值)` | 左模糊（前%） | `WHERE 字段 LIKE '%值'` |
| `likeRight(字段, 值)` | 右模糊（后%） | `WHERE 字段 LIKE '值%'` |
| `gt(字段, 值)` | 大于 | `WHERE 字段 > 值` |
| `ge(字段, 值)` | 大于等于 | `WHERE 字段 >= 值` |
| `lt(字段, 值)` | 小于 | `WHERE 字段 < 值` |
| `le(字段, 值)` | 小于等于 | `WHERE 字段 <= 值` |
| `between(字段, v1, v2)` | 在...之间 | `WHERE 字段 BETWEEN v1 AND v2` |
| `in(字段, 列表)` | 在列表中 | `WHERE 字段 IN (v1, v2, ...)` |
| `isNull(字段)` | 为空 | `WHERE 字段 IS NULL` |
| `isNotNull(字段)` | 不为空 | `WHERE 字段 IS NOT NULL` |
| `orderByDesc(字段)` | 降序排列 | `ORDER BY 字段 DESC` |
| `orderByAsc(字段)` | 升序排列 | `ORDER BY 字段 ASC` |
| `last("LIMIT 10")` | 拼接 SQL 片段 | 追加 `LIMIT 10` |

### 5.3 实际使用示例

```java
// 示例1：查询分类为"藤本类"的所有植物，按热度降序
LambdaQueryWrapper<Plant> wrapper1 = new LambdaQueryWrapper<>();
wrapper1.eq(Plant::getCategory, "藤本类")
        .orderByDesc(Plant::getPopularity);
List<Plant> plants = plantMapper.selectList(wrapper1);

// 示例2：查询名字包含"钩"且浏览量大于100的植物
LambdaQueryWrapper<Plant> wrapper2 = new LambdaQueryWrapper<>();
wrapper2.like(Plant::getNameCn, "钩")
        .gt(Plant::getViewCount, 100);
List<Plant> plants2 = plantMapper.selectList(wrapper2);

// 示例3：动态条件查询（条件可选）
String keyword = "钩藤";  // 可能是 null
String category = "藤本类"; // 可能是 null

LambdaQueryWrapper<Plant> wrapper3 = new LambdaQueryWrapper<>();
// 条件不为空时才添加查询条件
wrapper3.like(keyword != null, Plant::getNameCn, keyword)
        .eq(category != null, Plant::getCategory, category);
List<Plant> plants3 = plantMapper.selectList(wrapper3);
```

---

## 六、分页查询 -- Page<T>

### 6.1 什么是分页？

**类比：读书翻页**

数据库可能有几百条植物数据，你不可能一次全查出来（太慢、太占内存）。
分页就像翻书一样，每次只看一页：

```
第1页：植物1 ~ 植物10     （current=1, size=10）
第2页：植物11 ~ 植物20    （current=2, size=10）
第3页：植物21 ~ 植物30    （current=3, size=10）
```

### 6.2 分页查询代码

```java
// 1. 创建分页对象：第1页，每页10条
Page<Plant> page = new Page<>(1, 10);

// 2. 创建查询条件
LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Plant::getCategory, "藤本类")
       .orderByDesc(Plant::getPopularity);

// 3. 执行分页查询
Page<Plant> result = plantMapper.selectPage(page, wrapper);

// 4. 获取结果
List<Plant> records = result.getRecords();     // 当前页的数据列表
long total = result.getTotal();                // 总记录数
long pages = result.getPages();                // 总页数
long current = result.getCurrent();            // 当前页码
long size = result.getSize();                  // 每页条数
```

### 6.3 分页原理图

```
数据库 plants 表（共 65 条数据）
+----+--------+--------+-----+
| id | name_cn| efficacy| ... |  总共 65 条
+----+--------+--------+-----+
| 1  | 钩藤   | 清热... | ... |  <-- 第1页（1-10）
| 2  | 透骨草 | 祛风... | ... |
| ...| ...    | ...    | ... |
| 10 | ...    | ...    | ... |
+----+--------+--------+-----+
| 11 | ...    | ...    | ... |  <-- 第2页（11-20）
| ...| ...    | ...    | ... |
+----+--------+--------+-----+
        ...
+----+--------+--------+-----+
| 61 | ...    | ...    | ... |  <-- 第7页（61-65）
| 62 | ...    | ...    | ... |
| 65 | ...    | ...    | ... |  <-- 最后一页只有5条
+----+--------+--------+-----+

等价 SQL: SELECT * FROM plants WHERE category = '藤本类'
         ORDER BY popularity DESC LIMIT 0, 10
         （LIMIT 起始位置, 每页条数）
```

---

## 七、本项目全部 13 个 Mapper 一览

| 序号 | Mapper 接口 | 对应实体 | 自定义方法 | 说明 |
|------|------------|---------|-----------|------|
| 1 | `PlantMapper` | `Plant` | incrementViewCount, incrementFavoriteCount, selectRandomPlants, searchByFullText, searchByLike | 药用植物，自定义方法最多 |
| 2 | `UserMapper` | `User` | 无 | 纯 CRUD，最简洁 |
| 3 | `KnowledgeMapper` | `Knowledge` | 无 | 知识库 |
| 4 | `InheritorMapper` | `Inheritor` | 无 | 传承人 |
| 5 | `QaMapper` | `Qa` | 无 | 问答 |
| 6 | `ResourceMapper` | `Resource` | 无 | 学习资源 |
| 7 | `CommentMapper` | `Comment` | 无 | 评论 |
| 8 | `FavoriteMapper` | `Favorite` | 无 | 收藏 |
| 9 | `FeedbackMapper` | `Feedback` | 无 | 反馈 |
| 10 | `QuizQuestionMapper` | `QuizQuestion` | 无 | 测验题目 |
| 11 | `QuizRecordMapper` | `QuizRecord` | 无 | 测验记录 |
| 12 | `PlantGameRecordMapper` | `PlantGameRecord` | 无 | 植物游戏记录 |
| 13 | `OperationLogMapper` | `OperationLog` | selectTrendLast7Days | 操作日志，有自定义统计方法 |

### 特殊 Mapper 说明

**OperationLogMapper** -- 自定义了 7 天趋势统计方法：

```java
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    // 查询最近7天每天的操作数量，用于后台统计图表
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM operation_log " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> selectTrendLast7Days();
}
```

返回结果示例：

```
date       | count
-----------+------
2026-03-20 | 156
2026-03-21 | 203
2026-03-22 | 178
2026-03-23 | 245
2026-03-24 | 198
2026-03-25 | 167
2026-03-26 | 234
```

---

## 八、@Mapper 注解的重要性

### 为什么必须加 @Mapper？

```java
@Mapper   // <-- 这个注解千万不能忘！
public interface UserMapper extends BaseMapper<User> {}
```

**@Mapper 的作用**：告诉 Spring 框架"这是一个 Mapper 接口，请帮我创建它的实现类"。

```
没有 @Mapper：
+-------------------+          +-------------------+
| UserMapper 接口    |   ???    | 谁来实现这个接口？   |
| （只有方法签名）    |  ----->  | Spring 不知道      |
+-------------------+          | -> 运行时报错！     |
                               +-------------------+

有 @Mapper：
+-------------------+          +-------------------+
| @Mapper           |          | MyBatis 自动生成    |
| UserMapper 接口    |  ----->  | UserMapperImpl     |
| （只有方法签名）    |          | （包含所有 SQL 逻辑）|
+-------------------+          +-------------------+
```

### 忘记加 @Mapper 会怎样？

```
报错：Field userMapper in com.dongmedicine.service.UserService
      required a bean of type 'com.dongmedicine.mapper.UserMapper'
      that could not be found.

含义：Spring 找不到 UserMapper 的实现类，因为没加 @Mapper
解决：在接口上加 @Mapper 注解
```

### 替代方案：@MapperScan

如果觉得每个 Mapper 都加 @Mapper 太麻烦，可以在启动类上加 `@MapperScan`：

```java
@SpringBootApplication
@MapperScan("com.dongmedicine.mapper")  // 扫描这个包下所有接口，自动注册为 Mapper
public class Application { ... }
```

这样每个 Mapper 就不用单独加 @Mapper 了。但本项目选择在每个 Mapper 上加 @Mapper，更加**明确和直观**。

---

## 九、常见错误与解决方案

### 错误 1：忘记加 @Mapper

```
报错：required a bean of type 'xxxMapper' that could not be found
原因：Spring 没有扫描到 Mapper 接口
解决：在接口上加 @Mapper 注解，或在启动类上加 @MapperScan
```

### 错误 2：泛型类型写错

```
错误：public interface PlantMapper extends BaseMapper<User> { }
                                        ^^^^^^^^^^^^^^^^
                                      Plant 的 Mapper 却写了 User！

报错：Column 'password_hash' not found in 'plants'
原因：泛型类型和实际操作的数据表不匹配
解决：确保 BaseMapper<T> 的 T 和 @TableName 对应的实体类一致
```

### 错误 3：@Select 中的参数名和 @Param 不一致

```
错误：
@Select("SELECT * FROM plants WHERE name_cn = #{name}")  // 用了 #{name}
List<Plant> findByName(@Param("keyword") String keyword); // 参数名是 keyword

报错：Parameter 'name' not found
解决：#{name} 改为 #{keyword}，或 @Param("name")
```

### 错误 4：分页查询没有配置分页插件

```
报错：分页查询返回了全部数据，没有生效
原因：MyBatis-Plus 需要配置分页插件才能生效
解决：在 config 包下添加 MybatisPlusConfig 配置类
```

### 错误 5：LambdaQueryWrapper 中用了不存在的字段

```
错误：wrapper.eq(Plant::getDifficulty, "easy");
      但 Plant 类中没有 difficulty 字段（已被移除）

报错：编译错误或运行时异常
解决：确认实体类中有对应的字段，编译器会自动检查
```

---

## 十、速记口诀

```
Mapper 继承 BaseMapper，CRUD 方法全都有
@Mapper 注解不能忘，Spring 才能找到它
自定义方法用 @Select，参数名字 @Param 配
查询条件用 Lambda，类型安全不怕错
分页就用 Page<T>，current 和 size 搞定它
```

---

## 十一、代码审查与改进建议

以下是对 Mapper 层代码的审查发现：

### 安全级别

| # | 级别 | 问题 | 涉及 Mapper | 说明 |
|---|------|------|-----------|------|
| 1 | 安全 | `searchByFullText()` 中 `keyword` 直接传入全文搜索 | `PlantMapper` | `keyword` 参数直接传入 `MATCH ... AGAINST(#{keyword} ...)` 语句。虽然 MyBatis 使用 `#{}` 占位符是参数化查询，不会产生 SQL 注入，但全文搜索的 `AGAINST` 子句对特殊字符和布尔操作符有特殊解析逻辑，恶意构造的关键词可能改变搜索行为。需确认 Mapper XML（如果存在）中使用的是 `#{keyword}` 参数化查询而非 `${keyword}` 字符串拼接，后者会导致 SQL 注入。 |

### 结构级别

| # | 级别 | 问题 | 涉及 Mapper | 说明 |
|---|------|------|-----------|------|
| 2 | 结构 | 缺少 Mapper XML 文件 | 全部 Mapper | 当前所有自定义 SQL 均使用 `@Select`/`@Update` 注解写在 Java 接口中。对于简单查询这没有问题，但复杂查询（如全文搜索 `MATCH AGAINST`、`JSON_SEARCH` 等）使用注解方式可读性差、难以维护，且无法实现动态 SQL。应创建对应的 Mapper XML 文件（如 `PlantMapper.xml`），将复杂查询迁移到 XML 中，利用 MyBatis 的 `<script>`、`<if>`、`<where>` 等标签实现动态条件查询，提高代码可维护性。 |
