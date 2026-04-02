# 工具类目录 (util)

本目录存放通用的工具类。

## 文件列表

| 文件 | 功能描述 |
|------|----------|
| FileCleanupHelper.java | 文件清理助手 |
| FileTypeUtils.java | 文件类型工具 |
| PageUtils.java | 分页工具 |
| PasswordValidator.java | 密码验证器 |
| SensitiveDataUtils.java | 敏感信息脱敏工具 |
| XssUtils.java | XSS防护工具 |

---

## PageUtils.java - 分页工具

提供分页相关的工具方法。

```java
/**
 * 分页工具类
 */
public class PageUtils {
    
    /**
     * LIKE查询特殊字符转义
     * 防止SQL注入
     */
    public static String escapeLike(String keyword) {
        if (keyword == null) return null;
        return keyword
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
    
    /**
     * 构建分页对象
     */
    public static <T> Page<T> buildPage(int pageNum, int pageSize) {
        pageSize = Math.min(pageSize, 100);  // 限制最大值
        return new Page<>(pageNum, pageSize);
    }
}
```

---

## PasswordValidator.java - 密码验证器

验证密码格式是否符合要求。

```java
/**
 * 密码验证器
 */
public class PasswordValidator {
    
    // 密码正则：8-50位，必须包含字母和数字
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,50}$");
    
    /**
     * 验证密码格式
     */
    public static boolean isValid(String password) {
        if (password == null || password.isEmpty()) return false;
        if (password.length() < 8 || password.length() > 50) return false;
        if (password.contains(" ")) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
```

---

## XssUtils.java - XSS防护工具

检测和清理XSS攻击字符。

```java
/**
 * XSS防护工具
 */
public class XssUtils {
    
    // 危险模式列表
    private static final Pattern[] DANGEROUS_PATTERNS = {
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE)
    };
    
    /**
     * 检查是否包含XSS攻击
     */
    public static boolean containsXss(String value) {
        if (value == null) return false;
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(value).find()) return true;
        }
        return false;
    }
}
```

---

## SensitiveDataUtils.java - 敏感信息脱敏

对敏感信息进行脱敏处理。

```java
/**
 * 敏感信息脱敏工具
 */
public class SensitiveDataUtils {
    
    /**
     * 手机号脱敏
     * 13812345678 -> 138****5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 身份证号脱敏
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return idCard;
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }
}
```

---

## FileTypeUtils.java - 文件类型工具

判断文件类型。

```java
/**
 * 文件类型工具
 */
public class FileTypeUtils {
    
    // 图片类型
    private static final Set<String> IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif");
    
    // 视频类型
    private static final Set<String> VIDEO_TYPES = Set.of("mp4", "avi", "mov");
    
    /**
     * 判断是否为图片
     */
    public static boolean isImage(String extension) {
        return IMAGE_TYPES.contains(extension.toLowerCase());
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }
}
```

---

**最后更新时间**：2026年4月3日
