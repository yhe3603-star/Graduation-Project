# 控制器目录说明

## 文件夹结构

本目录包含项目的所有控制器类，负责处理HTTP请求和响应。

```
controller/
├── UserController.java         # 用户控制器
├── AdminController.java        # 管理后台控制器
├── PlantController.java        # 药材控制器
├── KnowledgeController.java    # 知识库控制器
├── InheritorController.java    # 传承人控制器
├── ResourceController.java     # 资源控制器
├── QaController.java           # 问答控制器
├── QuizController.java         # 答题控制器
├── PlantGameController.java    # 植物游戏控制器
├── CommentController.java      # 评论控制器
├── FavoriteController.java     # 收藏控制器
├── FeedbackController.java     # 意见反馈控制器
├── FileUploadController.java   # 文件上传控制器
├── ChatController.java         # AI对话控制器
├── LeaderboardController.java  # 排行榜控制器
├── OperationLogController.java # 操作日志控制器
└── README.md                   # 说明文档
```

## 详细说明

### 1. UserController.java - 用户控制器

**请求路径**：`/api/user`

**接口列表**：
| 方法 | 路径 | 权限 | 限流 | 说明 |
|------|------|------|------|------|
| POST | /login | 公开 | 5次/秒 | 用户登录 |
| POST | /register | 公开 | 3次/秒 | 用户注册 |
| GET | /me | 需认证 | - | 获取当前用户信息 |
| POST | /change-password | 需认证 | - | 修改密码 |
| POST | /logout | 需认证 | - | 用户登出 |
| GET | /validate | 需认证 | - | 验证Token有效性 |
| POST | /refresh-token | 需认证 | - | 刷新Token |

**登录响应格式**：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "id": 1,
    "username": "admin",
    "role": "admin"
  }
}
```

### 2. AdminController.java - 管理后台控制器

**请求路径**：`/api/admin`

**权限**：需要 `ADMIN` 角色

**接口列表**：

**用户管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /users | 分页获取用户列表 |
| DELETE | /users/{id} | 删除用户 |
| PUT | /users/{id}/role | 更新用户角色 |
| PUT | /users/{id}/ban | 封禁用户 |
| PUT | /users/{id}/unban | 解封用户 |

**传承人管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /inheritors | 分页获取传承人列表 |
| POST | /inheritors | 新增传承人 |
| PUT | /inheritors/{id} | 更新传承人 |
| DELETE | /inheritors/{id} | 删除传承人 |

**知识管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /knowledge | 分页获取知识列表 |
| POST | /knowledge | 新增知识 |
| PUT | /knowledge/{id} | 更新知识 |
| DELETE | /knowledge/{id} | 删除知识 |

**药材管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /plants | 分页获取药材列表 |
| POST | /plants | 新增药材 |
| PUT | /plants/{id} | 更新药材 |
| DELETE | /plants/{id} | 删除药材 |

**问答管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /qa | 分页获取问答列表 |
| POST | /qa | 新增问答 |
| PUT | /qa/{id} | 更新问答 |
| DELETE | /qa/{id} | 删除问答 |

**资源管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /resources | 分页获取资源列表 |
| POST | /resources | 新增资源 |
| PUT | /resources/{id} | 更新资源 |
| DELETE | /resources/{id} | 删除资源 |

**反馈管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /feedback | 分页获取反馈列表 |
| PUT | /feedback/{id}/reply | 回复反馈 |
| DELETE | /feedback/{id} | 删除反馈 |

**评论管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /comments | 分页获取评论列表 |
| PUT | /comments/{id}/approve | 审核通过 |
| PUT | /comments/{id}/reject | 审核拒绝 |
| DELETE | /comments/{id} | 删除评论 |

**统计接口**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /stats | 获取统计数据 |
| GET | /stats/plants-distribution | 药材分布统计 |
| GET | /stats/knowledge-popularity | 知识热度统计 |

### 3. PlantController.java - 药材控制器

**请求路径**：`/api/plants`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /list | 公开 | 分页获取药材列表（支持分类、用法筛选） |
| GET | /search | 公开 | 关键词搜索药材 |
| GET | /{id} | 公开 | 获取药材详情 |
| GET | /{id}/similar | 公开 | 获取相似药材 |
| GET | /random | 公开 | 按难度随机获取药材 |
| POST | /{id}/view | 公开 | 增加浏览次数 |

**请求参数**：
- `list`：page, size, category, usageWay, keyword
- `search`：keyword（必填）, page, size
- `random`：difficulty（必填）, limit（1-100）

### 4. KnowledgeController.java - 知识库控制器

**请求路径**：`/api/knowledge`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取知识列表 |
| GET | /{id} | 获取知识详情 |
| POST | /{id}/view | 增加浏览次数 |

### 5. InheritorController.java - 传承人控制器

**请求路径**：`/api/inheritors`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取传承人列表 |
| GET | /{id} | 获取传承人详情 |
| POST | /{id}/view | 增加浏览次数 |

### 6. ResourceController.java - 资源控制器

**请求路径**：`/api/resources`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取资源列表 |
| GET | /{id} | 获取资源详情 |
| GET | /download/{id} | 下载资源 |
| POST | /{id}/view | 增加浏览次数 |

### 7. QaController.java - 问答控制器

**请求路径**：`/api/qa`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取问答列表 |
| GET | /{id} | 获取问答详情 |
| POST | /{id}/view | 增加浏览次数 |

### 8. QuizController.java - 答题控制器

**请求路径**：`/api/quiz`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /questions | 公开 | 获取随机题目 |
| POST | /submit | 需认证 | 提交答案 |
| GET | /history | 需认证 | 获取答题历史 |
| GET | /list | 需ADMIN | 分页获取题目列表 |
| POST | /add | 需ADMIN | 添加题目 |
| PUT | /update | 需ADMIN | 更新题目 |
| DELETE | /{id} | 需ADMIN | 删除题目 |

### 9. PlantGameController.java - 植物游戏控制器

**请求路径**：`/api/plant-game`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /questions | 获取游戏题目 |
| POST | /submit | 提交游戏答案 |
| GET | /history | 获取游戏历史 |

### 10. CommentController.java - 评论控制器

**请求路径**：`/api/comments`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /list | 公开 | 获取评论列表 |
| POST | / | 需认证 | 添加评论 |
| DELETE | /{id} | 需认证 | 删除评论 |
| POST | /{id}/like | 需认证 | 点赞评论 |

### 11. FavoriteController.java - 收藏控制器

**请求路径**：`/api/favorites`

**权限**：需要认证

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /my | 获取用户收藏列表 |
| POST | /{type}/{id} | 添加收藏 |
| DELETE | /{type}/{id} | 取消收藏 |

**收藏类型**：plant, knowledge, inheritor, resource

### 12. FeedbackController.java - 意见反馈控制器

**请求路径**：`/api/feedback`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /list | 需ADMIN | 获取反馈列表 |
| POST | / | 需认证 | 提交反馈 |
| GET | /{id} | 需认证 | 获取反馈详情 |

### 13. FileUploadController.java - 文件上传控制器

**请求路径**：`/api/upload`

**权限**：需要ADMIN角色

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /image | 上传图片 |
| POST | /video | 上传视频 |
| POST | /document | 上传文档 |
| POST | /file | 上传通用文件 |
| DELETE | /file | 删除文件 |

**支持的文件类型**：
- 图片：jpg, jpeg, png, gif, bmp, webp, svg
- 视频：mp4, avi, mov, wmv, flv, mkv
- 文档：docx, doc, pdf, pptx, ppt, xlsx, xls, txt

### 14. ChatController.java - AI对话控制器

**请求路径**：`/api/chat`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | / | 发送对话消息 |
| GET | /history | 获取对话历史 |

### 15. LeaderboardController.java - 排行榜控制器

**请求路径**：`/api/leaderboard`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /quiz | 获取答题排行榜 |
| GET | /game | 获取游戏排行榜 |

### 16. OperationLogController.java - 操作日志控制器

**请求路径**：`/api/admin/logs`

**权限**：需要ADMIN角色

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页获取日志列表 |
| DELETE | /{id} | 删除单条日志 |
| DELETE | /batch | 批量删除日志 |
| DELETE | /clear | 清空所有日志 |

## 控制器统计

| 控制器 | 接口数量 | 主要功能 |
|--------|---------|---------|
| UserController | 7 | 用户认证与管理 |
| AdminController | 30+ | 管理后台CRUD操作 |
| PlantController | 6 | 药材信息管理 |
| KnowledgeController | 3 | 知识库管理 |
| InheritorController | 3 | 传承人管理 |
| ResourceController | 4 | 资源管理 |
| QaController | 3 | 问答管理 |
| QuizController | 7 | 答题功能 |
| PlantGameController | 3 | 游戏功能 |
| CommentController | 4 | 评论管理 |
| FavoriteController | 3 | 收藏功能 |
| FeedbackController | 3 | 反馈管理 |
| FileUploadController | 5 | 文件上传 |
| ChatController | 2 | AI对话 |
| LeaderboardController | 2 | 排行榜 |
| OperationLogController | 4 | 日志管理 |
| **总计** | **~90** | - |

## 开发规范

1. **注解使用**：
   - `@RestController`：标记为REST控制器
   - `@RequestMapping`：定义基础路径
   - `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`：定义HTTP方法
   - `@PreAuthorize`：权限控制
   - `@RateLimit`：接口限流
   - `@Validated`：启用参数验证

2. **命名规范**：
   - 类名使用大驼峰命名法，以Controller结尾
   - 方法名使用小驼峰命名法，动词+名词形式

3. **参数验证**：
   - 使用`@Valid`验证请求体
   - 使用`@NotNull`、`@NotBlank`等验证参数
   - 使用`@RequestParam`设置默认值

4. **响应格式**：
   - 所有响应使用`R<T>`封装
   - 成功：`R.ok(data)`
   - 失败：`R.error(message)`

5. **分页处理**：
   - 使用`PageUtils.getPage(page, size)`创建分页对象
   - 使用`PageUtils.toMap(pageResult)`转换为统一格式

## 分页响应格式

```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  }
}
```

---

**最后更新时间**：2026年3月25日
