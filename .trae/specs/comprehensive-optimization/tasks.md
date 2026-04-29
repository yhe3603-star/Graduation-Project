# Tasks

## 第一阶段：安全漏洞修复（紧急）

- [x] Task 1: SaTokenConfig认证强化
  - [x] 1.1: 修改SaTokenConfig.java，将写操作API从excludePathPatterns中移除
  - [x] 1.2: 仅保留只读GET接口的路径排除，通配符路径拆分为具体只读端点
  - [x] 1.3: 在SaInterceptor的lambda中添加StpUtil.checkLogin()鉴权逻辑（基于HTTP方法判断）
  - [x] 1.4: 验证未登录用户访问写操作API返回401

- [x] Task 2: UserServiceImpl.getUserToken()权限控制
  - [x] 2.1: 在getUserToken方法中添加StpUtil.checkRole("admin")权限校验
  - [x] 2.2: 添加操作审计日志记录（操作人ID、目标用户ID、时间戳）
  - [x] 2.3: 在AdminController中确认调用getUserToken的接口有权限保护

- [x] Task 3: PlantGameServiceImpl服务端验证
  - [x] 3.1: 修改PlantGameServiceImpl.submit()方法，接收答案列表而非分数
  - [x] 3.2: 根据提交的植物ID和用户选择，从数据库查询正确答案比对
  - [x] 3.3: 服务端计算correctCount和score，不信任客户端数据
  - [x] 3.4: 修改PlantGameSubmitDTO，新增answers字段，兼容旧版前端
  - [x] 3.5: 同步修改QuizServiceImpl.submit()，答案比较使用trim+equalsIgnoreCase，questionIds去重

- [x] Task 4: XssFilter安全修复
  - [x] 4.1: 移除XssFilter中对/api/admin/路径跳过XSS过滤的if判断
  - [x] 4.2: 修改getHeader方法，不对Header值做XSS清洗，直接返回原始值
  - [x] 4.3: 验证管理员接口正常工作且Authorization头不被破坏

- [x] Task 5: JWT密钥启动校验
  - [x] 5.1: 修改SecurityConfigValidator，移除开发环境的空密钥豁免逻辑
  - [x] 5.2: 所有环境下密钥为空或长度不足32均抛出IllegalStateException阻止启动
  - [x] 5.3: 在application-dev.yml中添加开发环境默认JWT密钥
  - [x] 5.4: 验证空密钥时应用启动失败

- [x] Task 6: CaptchaService安全随机数
  - [x] 6.1: 将Random替换为SecureRandom
  - [x] 6.2: 更新import语句
  - [x] 6.3: 验证验证码生成功能正常

- [x] Task 7: CI/CD密码安全
  - [x] 7.1: 修改ci-cd.yml，将所有硬编码密码替换为secrets引用
  - [x] 7.2: MYSQL_ROOT_PASSWORD改为${{ secrets.MYSQL_ROOT_PASSWORD }}
  - [x] 7.3: REDIS_PASSWORD改为${{ secrets.REDIS_PASSWORD }}
  - [x] 7.4: JWT_SECRET改为${{ secrets.JWT_SECRET }}
  - [x] 7.5: RABBITMQ_PASSWORD改为使用secrets
  - [x] 7.6: StrictHostKeyChecking改为accept-new

- [x] Task 8: Docker端口最小暴露
  - [x] 8.1: 修改docker-compose.yml，移除backend的ports映射
  - [x] 8.2: 移除rabbitmq的ports映射
  - [x] 8.3: 移除kkfileview的ports映射
  - [x] 8.4: 保留frontend的80端口映射
  - [x] 8.5: 修改depends_on添加condition: service_healthy/service_started

## 第二阶段：并发与逻辑修复（重要）

- [x] Task 9: RateLimitAspect原子限流
  - [x] 9.1: 创建Redis Lua脚本实现原子increment+expire操作
  - [x] 9.2: 修改RateLimitAspect使用RedisTemplate.execute()执行Lua脚本
  - [x] 9.3: 为localBuckets添加定时清理机制（超过30分钟未使用的bucket自动移除）
  - [x] 9.4: 修复lastRedisCheckTime缺少volatile的问题
  - [x] 9.5: 验证限流功能正确工作

- [x] Task 10: FavoriteServiceImpl唯一索引
  - [x] 10.1: 确认favorites表已有uk_user_target唯一索引
  - [x] 10.2: 修改FavoriteServiceImpl.addFavorite()，捕获DuplicateKeyException返回友好错误
  - [x] 10.3: 验证并发收藏不会产生重复记录

- [x] Task 11: @Async自调用修复
  - [x] 11.1: 创建PopularityAsyncService接口和PopularityAsyncServiceImpl实现类
  - [x] 11.2: 在KnowledgeServiceImpl中注入PopularityAsyncService，调用其异步方法
  - [x] 11.3: 为PlantMapper和InheritorMapper添加incrementPopularity方法
  - [x] 11.4: 验证热度更新在独立Service中异步执行

- [x] Task 12: ResourceServiceImpl fileType过滤
  - [x] 12.1: 在listByCategoryAndKeywordAndType方法中添加fileType过滤逻辑
  - [x] 12.2: 验证fileType过滤在非分页搜索中生效

- [x] Task 13: Dockerfile修复
  - [x] 13.1: 修改后端Dockerfile，ENTRYPOINT改为使用entrypoint.sh
  - [x] 13.2: 在Dockerfile中安装default-mysql-client
  - [x] 13.3: COPY entrypoint.sh到镜像并赋予执行权限
  - [x] 13.4: 修复entrypoint.sh中MySQL等待超时后报错退出
  - [x] 13.5: 修复entrypoint.sh中数据库初始化失败报错退出
  - [x] 13.6: 添加set -e和MYSQL_READY标志变量

## 第三阶段：代码质量提升（计划）

- [x] Task 14: 统一依赖注入方式
  - [x] 14.1: 将7个ServiceImpl的@Autowired字段注入改为@RequiredArgsConstructor构造器注入
  - [x] 14.2: Controller层暂保留（改动范围大，后续专项处理）
  - [x] 14.3: 移除冗余的Mapper注入（KnowledgeServiceImpl/PlantServiceImpl/QaServiceImpl/ResourceServiceImpl改用baseMapper）

- [x] Task 15: 统一时间字段命名
  - [x] 15.1: 将Feedback.java的createTime改为createdAt
  - [x] 15.2: 将QuizRecord.java的createTime改为createdAt
  - [x] 15.3: 将PlantGameRecord.java的createTime改为createdAt
  - [x] 15.4: 同步修改QuizServiceImpl/PlantGameServiceImpl/FeedbackServiceImpl/AdminController/MyMetaObjectHandler中的引用

- [x] Task 16: 抽取通用Entity基类
  - [x] 16.1: 创建BaseEntity类，包含id(Integer)、createdAt、updatedAt通用字段
  - [x] 16.2: 让User、Plant、Knowledge、Inheritor、Comment、Resource、Qa、OperationLog继承BaseEntity
  - [x] 16.3: 更新MyMetaObjectHandler支持updatedAt自动填充

- [x] Task 17: 消除代码重复
  - [x] 17.1: 创建IpUtils工具类，LoggingAspect/OperationLogAspect/RateLimitAspect已使用
  - [x] 17.2: 前端useFileUpload composable（复杂度高，后续专项处理）
  - [x] 17.3: 前端密码校验规则（后续专项处理）
  - [x] 17.4: 后端advancedSearch查询构建逻辑（后续专项处理）

- [x] Task 18: 前端状态管理统一到Pinia
  - [x] 18.1: App.vue的provide/inject添加弃用注释
  - [x] 18.2: 修改useFavorite.js，sessionStorage读取改为useUserStore
  - [x] 18.3: 修改usePersonalCenter.js，sessionStorage读取改为useUserStore
  - [x] 18.4: inject('request')消费方后续专项迁移

- [x] Task 19: echarts按需导入
  - [x] 19.1: 修改AdminDashboard.vue，使用echarts/core按需导入
  - [x] 19.2: 修改ChartCard.vue，使用echarts/core按需导入
  - [x] 19.3: AdminDashboard添加onUnmounted图表实例销毁

# Task Dependencies
- [Task 3] depends on [Task 1] ✅
- [Task 9] depends on [Task 8] ✅
- [Task 10] depends on [Task 10.1] ✅
- [Task 13] depends on [Task 8] ✅
- [Task 15] before [Task 16] ✅
