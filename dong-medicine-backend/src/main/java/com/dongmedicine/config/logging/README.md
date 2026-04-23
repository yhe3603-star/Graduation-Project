# 日志配置目录 (config/logging/)

> 类比：日志就像**日记本** -- 每天记录发生了什么事、什么时候发生的、结果怎么样。而 SensitiveDataConverter 就像日记中的**隐私保护** -- 涉及密码、手机号等敏感信息时，用 `***` 代替，防止泄露。

## 什么是日志？

在程序运行过程中，我们需要记录各种事件，比如：
- 用户什么时候登录了
- 哪个接口被调用了，花了多长时间
- 有没有出错，错误信息是什么

这些记录就是**日志**。没有日志，系统出了问题就像闭着眼睛找药 -- 完全不知道发生了什么。

---

## 目录结构

```
logging/
└── SensitiveDataConverter.java   # 日志敏感数据脱敏转换器
```

---

## 日志级别

在 Spring Boot 中，日志分为5个级别，从低到高：

| 级别 | 用途 | 类比 | 示例 |
|------|------|------|------|
| **DEBUG** | 调试信息，开发时看 | 详细的实验记录 | "查询参数: keyword=钩藤, page=1" |
| **INFO** | 重要业务信息 | 日常日记 | "用户 zhangsan 登录成功" |
| **WARN** | 警告，不影响运行但需注意 | 身体有点不舒服 | "Redis连接失败，降级到内存缓存" |
| **ERROR** | 错误，影响功能 | 生病了 | "数据库连接超时" |
| **OFF** | 关闭日志 | 不写日记了 | -- |

```java
// 使用示例
log.debug("查询参数: keyword={}, page={}", keyword, page);  // 开发调试用
log.info("用户 {} 登录成功", username);                       // 记录重要操作
log.warn("Redis连接失败，降级到内存缓存: {}", e.getMessage()); // 警告
log.error("数据库连接超时", e);                                // 错误
```

**日志级别规则**：设置了某个级别后，只输出该级别及更高级别的日志。比如设置了 INFO，就看不到 DEBUG 日志。

```yaml
# application.yml 中配置日志级别
logging:
  level:
    root: INFO                           # 全局日志级别
    com.dongmedicine: DEBUG              # 项目代码用DEBUG级别（开发时）
    com.dongmedicine.config: INFO        # 配置包用INFO级别
```

---

## SensitiveDataConverter -- 日志隐私保护

> 类比：你写日记时，不会把银行卡密码原样写上去，而是写"我的密码是***"。SensitiveDataConverter 做的就是这件事 -- 自动把日志中的敏感信息替换成星号。

### 为什么需要脱敏？

想象一下，如果日志中打印了这样的内容：

```
用户登录: username=zhangsan, password=abc123
发送验证码: phone=13812345678
Token生成: eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjF9.xxx
```

如果有人看到了这些日志（比如运维人员、或者日志文件被泄露），就能获取用户的密码、手机号、Token等敏感信息。这是严重的安全隐患！

### 工作原理

SensitiveDataConverter 是一个 Logback 转换器，它会在日志输出之前，自动扫描日志内容，把敏感信息替换掉。

```java
// SensitiveDataConverter.java
public class SensitiveDataConverter extends MessageConverter {

    @Override
    public String convert(ILoggingEvent event) {
        // 第1步：先获取原始日志消息
        String message = super.convert(event);
        if (message == null) {
            return null;
        }
        // 第2步：调用 SensitiveDataUtils.autoMask() 自动脱敏
        return SensitiveDataUtils.autoMask(message);
    }
}
```

### 脱敏效果

| 原始内容 | 脱敏后 | 规则 |
|----------|--------|------|
| `phone=13812345678` | `phone=138****5678` | 手机号中间4位用星号代替 |
| `email=test@qq.com` | `email=te***@qq.com` | 邮箱前缀只保留前2位 |
| `idCard=430102199901011234` | `idCard=430102********1234` | 身份证号中间8位用星号 |
| `password=abc123` | `password=a****3` | 敏感字段值部分隐藏 |
| `token=eyJhbGciOi...long...xxx` | `token=eyJhbGciOi...xxx` | JWT Token 只显示首尾 |

### 如何配置 logback-spring.xml

要让 SensitiveDataConverter 生效，需要在日志配置文件中注册它：

```xml
<!-- logback-spring.xml -->
<configuration>

    <!-- 第1步：注册自定义转换器 -->
    <conversionRule conversionWord="msg"
                    converterClass="com.dongmedicine.config.logging.SensitiveDataConverter" />

    <!-- 第2步：定义日志格式（使用 %msg 引用转换器） -->
    <property name="CONSOLE_LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n" />

    <!-- 第3步：控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!-- 第4步：文件输出（按日期滚动） -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/dong-medicine.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/dong-medicine.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>  <!-- 保留30天 -->
        </rollingPolicy>
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!-- 第5步：设置日志级别 -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

</configuration>
```

### 日志格式说明

```
2024-04-23 14:30:15.123 [http-nio-8080-exec-1] INFO  c.d.service.impl.UserServiceImpl - 用户 zhangsan 登录成功
|                           |                       |     |                                |
|____ 时间戳 _______________|  线程名               |     |____ 类名 _______________________|  消息内容
                                                     |
                                                 日志级别
```

---

## 脱敏覆盖的敏感字段

SensitiveDataConverter 底层调用了 `SensitiveDataUtils.autoMask()`，它会自动检测并脱敏以下类型的数据：

| 数据类型 | 检测方式 | 脱敏规则 |
|----------|----------|----------|
| 手机号 | 正则匹配 `1[3-9]开头的11位数字` | `138****5678` |
| 邮箱 | 正则匹配 `xxx@xxx.xxx` | `te***@qq.com` |
| 身份证号 | 正则匹配18位身份证格式 | `430102********1234` |
| 银行卡号 | 正则匹配16-19位连续数字 | `6222****1234` |
| JWT Token | 正则匹配 `eyJ...eyJ...xxx` 格式 | 只显示首尾10位 |
| SQL敏感值 | 匹配 `password='xxx'` 等模式 | `password='***'` |

---

## 常见错误

**Q: 日志中还是能看到明文密码？**
A: 检查 `logback-spring.xml` 中是否正确注册了 `conversionRule`。如果用的是 `%message` 而不是 `%msg`，转换器不会生效。

**Q: 脱敏太激进了，把正常数据也替换了？**
A: `autoMask()` 使用正则匹配，可能会误判。比如一串11位数字可能被当成手机号脱敏。如果遇到这种情况，可以在日志中避免直接打印长数字串，或者用结构化日志。

**Q: 日志文件太大怎么办？**
A: 配置 `RollingFileAppender` 的滚动策略和 `maxHistory`。建议日志保留30天，单文件超过100MB就滚动。

**Q: 生产环境应该用什么日志级别？**
A: 生产环境建议用 `INFO` 级别。`DEBUG` 级别会产生大量日志，影响性能。只在排查问题时临时切换到 `DEBUG`。
