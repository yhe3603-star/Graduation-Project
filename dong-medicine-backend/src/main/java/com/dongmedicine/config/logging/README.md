# 日志配置目录 (logging)

本目录存放日志相关的配置类。

## 文件列表

| 文件 | 功能描述 |
|------|----------|
| SensitiveDataConverter.java | 敏感数据转换器，用于日志脱敏 |

---

## SensitiveDataConverter.java - 敏感数据转换器

在日志输出时自动脱敏敏感信息。

```java
/**
 * 敏感数据转换器
 * 在日志中自动脱敏敏感信息
 */
@Component
public class SensitiveDataConverter extends ClassicConverter {
    
    // 需要脱敏的字段名
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "password", "token", "secret", "apiKey"
    );
    
    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        
        // 对敏感信息进行脱敏
        for (String field : SENSITIVE_FIELDS) {
            message = maskField(message, field);
        }
        
        return message;
    }
    
    private String maskField(String message, String field) {
        // 正则替换敏感信息
        return message.replaceAll(
            "(" + field + "['\"]?\\s*[:=]\\s*['\"]?)([^'\"\\s,}]+)",
            "$1******"
        );
    }
}
```

---

## 日志配置示例

```xml
<!-- logback-spring.xml -->
<configuration>
    <conversionRule conversionWord="masked" 
                    converterClass="com.dongmedicine.config.logging.SensitiveDataConverter"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %masked%n</pattern>
        </encoder>
    </appender>
</configuration>
```

---

## 日志级别说明

| 级别 | 说明 | 使用场景 |
|------|------|----------|
| TRACE | 最详细 | 调试时使用 |
| DEBUG | 调试信息 | 开发环境 |
| INFO | 一般信息 | 生产环境 |
| WARN | 警告信息 | 潜在问题 |
| ERROR | 错误信息 | 需要关注的问题 |

---

**最后更新时间**：2026年4月3日
