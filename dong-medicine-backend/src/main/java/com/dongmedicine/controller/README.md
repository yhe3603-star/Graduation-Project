# 控制器目录说明

## 文件夹结构

本目录包含项目的所有控制器类，负责处理HTTP请求和响应。

```
controller/
├── AdminController.java        # 管理后台控制器
├── ChatController.java         # AI对话控制器
├── CommentController.java      # 评论控制器
├── FavoriteController.java     # 收藏控制器
├── FeedbackController.java     # 意见反馈控制器
├── FileUploadController.java   # 文件上传控制器
├── InheritorController.java    # 传承人控制器
├── KnowledgeController.java    # 知识库控制器
├── LeaderboardController.java  # 排行榜控制器
├── OperationLogController.java # 操作日志控制器
├── PlantController.java        # 药材控制器
├── PlantGameController.java    # 植物游戏控制器
├── QaController.java           # 问答控制器
├── QuizController.java         # 答题控制器
├── ResourceController.java     # 资源控制器
└── UserController.java         # 用户控制器
```

## 详细说明

### 1. AdminController.java

**功能**：管理后台控制器，处理管理后台相关的请求。

**主要接口**：
- `GET /api/admin/dashboard`：获取仪表盘数据
- `GET /api/admin/{module}`：获取模块数据
- `POST /api/admin/{module}`：创建数据
- `PUT /api/admin/{module}/{id}`：更新数据
- `DELETE /api/admin/{module}/{id}`：删除数据
- `GET /api/admin/{module}/{id}`：获取详情

**权限**：需要管理员权限

### 2. ChatController.java

**功能**：AI对话控制器，处理与AI的对话请求。

**主要接口**：
- `POST /api/chat`：发送对话消息
- `GET /api/chat/history`：获取对话历史

### 3. CommentController.java

**功能**：评论控制器，处理评论相关的请求。

**主要接口**：
- `GET /api/comments`：获取评论列表
- `POST /api/comments`：添加评论
- `PUT /api/comments/{id}`：更新评论
- `DELETE /api/comments/{id}`：删除评论
- `GET /api/comments/{targetType}/{targetId}`：获取目标对象的评论

### 4. FavoriteController.java

**功能**：收藏控制器，处理收藏相关的请求。

**主要接口**：
- `GET /api/favorites`：获取用户收藏列表
- `POST /api/favorites`：添加收藏
- `DELETE /api/favorites/{id}`：取消收藏
- `GET /api/favorites/{targetType}/{targetId}`：检查是否已收藏

### 5. FeedbackController.java

**功能**：意见反馈控制器，处理意见反馈相关的请求。

**主要接口**：
- `GET /api/feedback`：获取反馈列表
- `POST /api/feedback`：提交反馈
- `PUT /api/feedback/{id}`：更新反馈状态
- `DELETE /api/feedback/{id}`：删除反馈
- `GET /api/feedback/{id}`：获取反馈详情

### 6. FileUploadController.java

**功能**：文件上传控制器，处理文件上传相关的请求。

**主要接口**：
- `POST /api/upload/image`：上传图片
- `POST /api/upload/video`：上传视频
- `POST /api/upload/document`：上传文档
- `POST /api/upload/file`：上传通用文件
- `DELETE /api/upload/{fileId}`：删除文件

### 7. InheritorController.java

**功能**：传承人控制器，处理传承人相关的请求。

**主要接口**：
- `GET /api/inheritors`：获取传承人列表
- `POST /api/inheritors`：添加传承人
- `PUT /api/inheritors/{id}`：更新传承人信息
- `DELETE /api/inheritors/{id}`：删除传承人
- `GET /api/inheritors/{id}`：获取传承人详情
- `GET /api/inheritors/level/{level}`：按级别获取传承人

### 8. KnowledgeController.java

**功能**：知识库控制器，处理知识库相关的请求。

**主要接口**：
- `GET /api/knowledge`：获取知识列表
- `POST /api/knowledge`：添加知识
- `PUT /api/knowledge/{id}`：更新知识
- `DELETE /api/knowledge/{id}`：删除知识
- `GET /api/knowledge/{id}`：获取知识详情
- `GET /api/knowledge/type/{type}`：按类型获取知识
- `GET /api/knowledge/therapy/{therapy}`：按疗法分类获取知识
- `GET /api/knowledge/disease/{disease}`：按疾病分类获取知识

### 9. LeaderboardController.java

**功能**：排行榜控制器，处理排行榜相关的请求。

**主要接口**：
- `GET /api/leaderboard/quiz`：获取答题排行榜
- `GET /api/leaderboard/game`：获取游戏排行榜
- `GET /api/leaderboard/{type}`：获取指定类型的排行榜

### 10. OperationLogController.java

**功能**：操作日志控制器，处理操作日志相关的请求。

**主要接口**：
- `GET /api/logs`：获取操作日志列表
- `GET /api/logs/{id}`：获取操作日志详情
- `DELETE /api/logs/{id}`：删除操作日志
- `GET /api/logs/user/{userId}`：获取用户操作日志
- `GET /api/logs/module/{module}`：获取模块操作日志

**权限**：需要管理员权限

### 11. PlantController.java

**功能**：药材控制器，处理药材相关的请求。

**主要接口**：
- `GET /api/plants`：获取药材列表
- `POST /api/plants`：添加药材
- `PUT /api/plants/{id}`：更新药材信息
- `DELETE /api/plants/{id}`：删除药材
- `GET /api/plants/{id}`：获取药材详情
- `GET /api/plants/category/{category}`：按分类获取药材
- `GET /api/plants/usage/{usage}`：按用法获取药材

### 12. PlantGameController.java

**功能**：植物游戏控制器，处理植物识别游戏相关的请求。

**主要接口**：
- `GET /api/game/questions`：获取游戏题目
- `POST /api/game/submit`：提交游戏答案
- `GET /api/game/result/{recordId}`：获取游戏结果
- `GET /api/game/history`：获取游戏历史

### 13. QaController.java

**功能**：问答控制器，处理问答相关的请求。

**主要接口**：
- `GET /api/qa`：获取问答列表
- `POST /api/qa`：添加问题
- `PUT /api/qa/{id}`：更新问题
- `DELETE /api/qa/{id}`：删除问题
- `GET /api/qa/{id}`：获取问题详情
- `POST /api/qa/{id}/answer`：回答问题
- `GET /api/qa/category/{category}`：按分类获取问答

### 14. QuizController.java

**功能**：答题控制器，处理答题相关的请求。

**主要接口**：
- `GET /api/quiz/questions`：获取答题题目
- `POST /api/quiz/submit`：提交答题答案
- `GET /api/quiz/result/{recordId}`：获取答题结果
- `GET /api/quiz/history`：获取答题历史
- `GET /api/quiz/questions/category/{category}`：按分类获取题目

### 15. ResourceController.java

**功能**：资源控制器，处理资源相关的请求。

**主要接口**：
- `GET /api/resources`：获取资源列表
- `POST /api/resources`：添加资源
- `PUT /api/resources/{id}`：更新资源信息
- `DELETE /api/resources/{id}`：删除资源
- `GET /api/resources/{id}`：获取资源详情
- `GET /api/resources/category/{category}`：按分类获取资源
- `GET /api/resources/download/{id}`：下载资源

### 16. UserController.java

**功能**：用户控制器，处理用户相关的请求。

**主要接口**：
- `POST /api/auth/login`：用户登录
- `POST /api/auth/register`：用户注册
- `POST /api/auth/logout`：用户登出
- `GET /api/users/me`：获取当前用户信息
- `PUT /api/users/me`：更新用户信息
- `PUT /api/users/password`：修改密码
- `GET /api/users`：获取用户列表
- `GET /api/users/{id}`：获取用户详情
- `PUT /api/users/{id}`：更新用户信息
- `DELETE /api/users/{id}`：删除用户

**权限**：部分接口需要管理员权限

## 控制器开发规范

1. **命名规范**：控制器类名使用大驼峰命名法，以`Controller`结尾
2. **路径规范**：API路径使用小写字母，单词间用连字符分隔
3. **方法命名**：方法名使用小驼峰命名法，动词+名词形式
4. **参数验证**：使用`@Valid`和`@NotNull`等注解验证参数
5. **异常处理**：使用统一的异常处理，返回统一格式的响应
6. **权限控制**：使用`@PreAuthorize`注解控制权限
7. **日志记录**：使用`@OperationLog`注解记录操作日志
8. **速率限制**：使用`@RateLimit`注解限制接口访问频率

## 响应格式

所有控制器的响应都使用`R`类封装，格式如下：

```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

## 错误处理

控制器中的异常会被`GlobalExceptionHandler`捕获，返回统一格式的错误响应：

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null
}
```

## 注意事项

- 控制器应该只负责处理HTTP请求和响应，业务逻辑应该放在Service层
- 控制器方法应该简洁明了，避免复杂的业务逻辑
- 应该使用适当的HTTP方法（GET、POST、PUT、DELETE）
- 应该使用适当的状态码
- 应该添加适当的注释和文档

---

**最后更新时间**：2026年3月23日