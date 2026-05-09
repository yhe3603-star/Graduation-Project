# 工具类测试 (`common.util`)

## 目录定位

本目录包含项目所有工具类的单元测试，验证 `com.dongmedicine.common.util` 包下的6个工具类。这些工具类提供了文件处理、安全防护、分页、密码验证、敏感数据脱敏、IP解析等基础能力，是系统安全性和功能正确性的关键保障。

## 文件清单

### FileCleanupHelperTest.java - 文件清理辅助类测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `deleteFilesFromJson_emptyString_noDeletion` | 空字符串/null/空白不执行删除，验证 FileUploadService 无交互 |
| `deleteFilesFromJson_validJsonArray_deletesFiles` | 有效JSON数组 `["file1.pdf","file2.docx"]` 逐个调用 deleteFile |
| `deleteSingleFile_emptyPath_noDeletion` | 空路径/null 不执行删除 |
| `deleteSingleFile_validPath_deletesFile` | 正常路径去除前导斜杠后调用 deleteFile |
| `deleteSingleFile_deleteFails_noException` | 删除失败时捕获异常，不向外抛出 |

**核心测试思路**：验证文件清理的空值防护、JSON解析删除、路径处理和异常容错。通过反射注入 Mock 的 FileUploadService。

### FileTypeUtilsTest.java - 文件类型工具类测试

| 测试组 | 测试逻辑 |
|--------|---------|
| `GetFileType` | 验证视频(mp4/avi/mov)->"video"、图片(jpg/jpeg/png/gif)->"image"、文档(pdf/docx/xlsx)->"document"、未知类型->"other"、null/空->"other" |
| `GetExtension` | 验证扩展名提取、大写转小写、无扩展名返回空、路径中的文件名提取 |
| `TypeChecks` | 验证 isVideo/isImage/isDocument 的类型判断 |
| `DangerousExtension` | 验证危险扩展名检测(exe/bat/sh/jsp/php)，大小写不敏感，安全扩展名不误判 |
| `PathSafety` | 验证路径遍历防护(../)、绝对路径拦截、正常相对路径放行、null/空不安全 |
| `SanitizeFileName` | 验证非法字符替换、多点号处理、null/空返回空 |
| `DangerousContent` | 验证 script 标签检测、javascript 协议检测、正常中文内容不误判、null/空返回 false |
| `GenerateSafeFileName` | 验证安全文件名生成（含时间戳+随机数、保留扩展名、清除特殊字符） |
| `GetMimeType` | 验证 MIME 类型映射（mp4->video/mp4, jpg->image/jpeg, pdf->application/pdf）、未知类型默认值 |
| `IsPreviewable` | 验证视频/图片/PDF 可预览、docx/xlsx 不可预览 |

**核心测试思路**：使用 `@Nested` 内嵌类按功能分组，覆盖文件类型识别、安全检测（危险扩展名/路径遍历/恶意内容）、文件名清理、MIME映射、预览判断等10个功能维度。

### IpUtilsTest.java - IP解析工具类测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `nullRequest` | null 请求返回 "unknown" |
| `xForwardedFor` | X-Forwarded-For 头优先使用，取第一个IP |
| `xRealIp` | X-Real-IP 头次优先使用 |
| `remoteAddr` | 无代理头使用 remoteAddr |
| `xForwardedForFirst` | 多IP时取第一个（逗号分隔） |
| `unknownValue` | "unknown" 值应跳过，回退到 remoteAddr |

**核心测试思路**：验证代理环境下的IP获取优先级链（X-Forwarded-For > X-Real-IP > remoteAddr），使用 MockHttpServletRequest 模拟请求头。

### PageUtilsTest.java - 分页工具类测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testGetPage_DefaultValues` | null参数默认 page=1, size=20 |
| `testGetPage_NormalParams` | 正常参数直接使用 |
| `testGetPage_PageZero/PageNegative` | page<=0 限制为1 |
| `testGetPage_SizeZero/SizeNegative` | size<=0 限制为1 |
| `testGetPage_SizeOverMax` | size>100 限制为100 |
| `testGetPage_SizeExactlyMax` | size=100 正常使用 |
| `testEscapeLike_*` | 验证 LIKE 通配符转义（%->\\%, _->\\_, \\->\\\\），复合转义，null/空处理 |
| `testToMap` | 验证 Page 对象转 Map（records/total/page/size） |

**核心测试思路**：重点验证分页参数的边界约束（page>=1, 1<=size<=100）和 SQL LIKE 注入防护。

### PasswordValidatorTest.java - 密码验证器测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testValidate_Null/Empty` | null/空密码验证失败 |
| `testValidate_TooShort/TooLong` | 长度<8或>50验证失败 |
| `testValidate_OnlyLetters/OnlyDigits` | 纯字母或纯数字验证失败（需字母+数字混合） |
| `testValidate_ContainsWhitespace` | 包含空格验证失败 |
| `testValidate_Valid/Strong` | 有效密码验证成功，强密码强度>=3 |
| `testIsValid_Valid/Invalid` | 便捷方法验证 |
| `testStrengthLabel_Weak` | 弱密码强度标签 |

**核心测试思路**：覆盖密码策略的全部规则（长度8-50、字母+数字混合、无空格），验证 ValidationResult 的 isValid/message/strength/strengthLabel 字段。

### SensitiveDataUtilsTest.java - 敏感数据脱敏工具类测试

| 测试组 | 测试逻辑 |
|--------|---------|
| `IsSensitiveField` | 识别 password/passwd/pwd、token/accesstoken、phone/mobile/email/idcard 等敏感字段，大小写不敏感，部分匹配，null返回false |
| `MaskValue` | 短值全遮盖、中等长度保留首尾、长值保留前2后2、null/空原样返回 |
| `Mask` | 敏感字段自动遮盖、非敏感字段原样返回、null值原样返回 |
| `AutoMask` | 自动遮盖手机号(138****5678)、邮箱、JWT令牌，null/空原样返回，正常中文文本不修改 |
| `MaskJson` | 遮盖JSON中的敏感字段值，null/空原样返回 |

**核心测试思路**：验证敏感字段识别的全面性（密码/令牌/个人信息三大类）、脱敏规则的正确性、自动检测与手动指定两种模式、JSON字符串中的脱敏处理。

### XssUtilsTest.java - XSS防护工具类测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testSanitize_*` | HTML特殊字符转义、引号转义、null/空处理 |
| `testContainsXss_*` | 检测 script 标签、javascript 协议、事件处理器(onclick)、iframe，正常中文文本不误判，null返回false |
| `testContainsSqlInjection_*` | 检测 SQL 注入(1 OR 1=1)，正常文本不误判 |
| `testStripHtmlTags` | 移除HTML标签保留文本内容 |
| `testSanitizeUrl_*` | 正常HTTP/HTTPS URL和相对路径保留，javascript/data 协议返回空 |
| `testSanitizeFileName_*` | 移除非法字符，null返回空 |
| `testEscapeJavaScript` | 转义JS特殊字符（引号、换行） |
| `testIsSafeInput_*` | 正常输入安全、超长输入不安全、XSS输入不安全 |
| `testSanitizeHtml_Script` | 移除script标签保留文本 |
| `testSanitizeForLog_TooLong` | 超长输入截断并添加"..."后缀 |

**核心测试思路**：覆盖 XSS 防护全链路（检测->清洗->转义），SQL 注入检测，URL 协议白名单，文件名清理，日志安全输出。

## 测试覆盖范围

| 工具类 | 覆盖维度 |
|--------|---------|
| FileCleanupHelper | JSON文件删除、单文件删除、空值防护、异常容错 |
| FileTypeUtils | 类型识别(10组)、扩展名提取、安全检测(危险扩展名/路径/内容)、文件名清理、MIME映射、预览判断 |
| IpUtils | 代理头优先级链、多IP取第一个、unknown值跳过、null处理 |
| PageUtils | 分页参数边界约束、LIKE通配符转义、Page转Map |
| PasswordValidator | 密码策略全规则、强度评估、便捷方法 |
| SensitiveDataUtils | 敏感字段识别(3大类)、脱敏规则、自动检测、JSON脱敏 |
| XssUtils | XSS检测/清洗/转义、SQL注入检测、URL协议白名单、日志安全 |

## 依赖关系

- `FileCleanupHelperTest` 依赖 `FileUploadService`（Mock注入）
- 其余5个工具类测试均为纯静态方法测试，无外部依赖
- `PageUtils` 被 `controller/` 和 `integration/` 测试间接使用
- `XssUtils` 被 `regression/XssRegressionTest` 专项回归测试
- `SensitiveDataUtils` 和 `XssUtils` 共同构成系统的安全防护层
