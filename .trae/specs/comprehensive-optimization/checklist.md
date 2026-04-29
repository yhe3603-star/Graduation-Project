# 项目全面优化 Checklist

## 第一阶段：安全漏洞修复

- [x] SaTokenConfig写操作API已从排除列表移除，仅保留只读GET路径
- [x] SaInterceptor添加了StpUtil.checkLogin()鉴权逻辑（基于HTTP方法判断）
- [x] 未登录用户访问/api/quiz/submit返回401
- [x] 未登录用户访问/api/plant-game/submit返回401
- [x] 未登录用户访问/api/chat返回401
- [x] UserServiceImpl.getUserToken()添加了管理员权限校验（StpUtil.checkRole("admin")）
- [x] getUserToken()调用时记录了操作审计日志
- [x] PlantGameServiceImpl.submit()服务端验证答题结果，不信任客户端分数
- [x] QuizServiceImpl.submit()服务端逐题验证答案（trim+equalsIgnoreCase）
- [x] XssFilter不再跳过/api/admin/路径的XSS过滤
- [x] XssFilter不再对HTTP Header做XSS清洗，Authorization头正常传递
- [x] SecurityConfigValidator在所有环境下密钥为空时阻止启动
- [x] application-dev.yml配置了开发环境默认JWT密钥
- [x] CaptchaService使用SecureRandom替代Random
- [x] ci-cd.yml中所有密码使用secrets引用，无硬编码
- [x] docker-compose.yml中backend 8080端口不再暴露到宿主机
- [x] docker-compose.yml中rabbitmq端口不再暴露到宿主机
- [x] docker-compose.yml中kkfileview端口不再暴露到宿主机
- [x] docker-compose.yml中depends_on使用condition: service_healthy/service_started

## 第二阶段：并发与逻辑修复

- [x] RateLimitAspect使用Redis Lua脚本实现原子increment+expire
- [x] localBuckets添加了定时清理机制（30分钟未使用自动移除），无内存泄漏
- [x] lastRedisCheckTime已声明为volatile
- [x] favorites表已有(user_id, target_type, target_id)唯一索引
- [x] FavoriteServiceImpl.addFavorite()捕获DuplicateKeyException返回友好错误
- [x] incrementPopularityAsync已提取到独立的PopularityAsyncService
- [x] KnowledgeServiceImpl通过注入PopularityAsyncService调用异步方法
- [x] ResourceServiceImpl.listByCategoryAndKeywordAndType()实现了fileType过滤
- [x] 后端Dockerfile使用entrypoint.sh作为ENTRYPOINT
- [x] entrypoint.sh中MySQL等待超时后报错退出
- [x] entrypoint.sh中数据库初始化失败后报错退出
- [x] Dockerfile中安装了default-mysql-client依赖

## 第三阶段：代码质量提升

- [x] 7个ServiceImpl使用@RequiredArgsConstructor构造器注入
- [x] Controller层暂保留@Autowired（后续专项处理）
- [x] 冗余的Mapper注入已移除（KnowledgeServiceImpl/PlantServiceImpl/QaServiceImpl/ResourceServiceImpl使用baseMapper）
- [x] Feedback.java的createTime改为createdAt
- [x] QuizRecord.java的createTime改为createdAt
- [x] PlantGameRecord.java的createTime改为createdAt
- [x] 所有引用时间字段的代码已同步修改
- [x] BaseEntity基类已创建，包含id/createdAt/updatedAt字段
- [x] 8个主要Entity已继承BaseEntity（User/Plant/Knowledge/Inheritor/Comment/Resource/Qa/OperationLog）
- [x] MyMetaObjectHandler已配置updatedAt自动填充
- [x] IpUtils工具类已创建，getClientIp方法已提取
- [x] LoggingAspect/OperationLogAspect/RateLimitAspect已使用IpUtils
- [ ] 前端useFileUpload composable（后续专项处理）
- [ ] 前端密码校验规则提取（后续专项处理）
- [x] App.vue的provide/inject已添加弃用注释
- [x] useFavorite.js使用useUserStore而非sessionStorage
- [x] usePersonalCenter.js使用useUserStore而非sessionStorage
- [x] AdminDashboard.vue使用echarts按需导入+onUnmounted销毁
- [x] ChartCard.vue使用echarts按需导入
