package com.dongmedicine.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileTypeUtils 回归测试")
class FileTypeUtilsTest {

    @Nested
    @DisplayName("getFileType")
    class GetFileType {
        @Test
        @DisplayName("视频文件应返回video")
        void videoTypes() {
            assertEquals("video", FileTypeUtils.getFileType("test.mp4"));
            assertEquals("video", FileTypeUtils.getFileType("test.avi"));
            assertEquals("video", FileTypeUtils.getFileType("test.mov"));
        }

        @Test
        @DisplayName("图片文件应返回image")
        void imageTypes() {
            assertEquals("image", FileTypeUtils.getFileType("test.jpg"));
            assertEquals("image", FileTypeUtils.getFileType("test.jpeg"));
            assertEquals("image", FileTypeUtils.getFileType("test.png"));
            assertEquals("image", FileTypeUtils.getFileType("test.gif"));
        }

        @Test
        @DisplayName("文档文件应返回document")
        void documentTypes() {
            assertEquals("document", FileTypeUtils.getFileType("test.pdf"));
            assertEquals("document", FileTypeUtils.getFileType("test.docx"));
            assertEquals("document", FileTypeUtils.getFileType("test.xlsx"));
        }

        @Test
        @DisplayName("未知类型应返回other")
        void unknownType() {
            assertEquals("other", FileTypeUtils.getFileType("test.xyz"));
            assertEquals("other", FileTypeUtils.getFileType("test.exe"));
        }

        @Test
        @DisplayName("null/空应返回other")
        void nullEmpty() {
            assertEquals("other", FileTypeUtils.getFileType(null));
            assertEquals("other", FileTypeUtils.getFileType(""));
        }
    }

    @Nested
    @DisplayName("getExtension")
    class GetExtension {
        @Test
        @DisplayName("应正确提取扩展名")
        void normalExtraction() {
            assertEquals("mp4", FileTypeUtils.getExtension("video.mp4"));
            assertEquals("pdf", FileTypeUtils.getExtension("doc.pdf"));
        }

        @Test
        @DisplayName("应处理大写扩展名")
        void uppercaseExtension() {
            assertEquals("mp4", FileTypeUtils.getExtension("video.MP4"));
        }

        @Test
        @DisplayName("无扩展名应返回空字符串")
        void noExtension() {
            assertEquals("", FileTypeUtils.getExtension("filename"));
        }

        @Test
        @DisplayName("null/空应返回空字符串")
        void nullEmpty() {
            assertEquals("", FileTypeUtils.getExtension(null));
            assertEquals("", FileTypeUtils.getExtension(""));
        }

        @Test
        @DisplayName("路径中的文件名应正确提取")
        void pathFilename() {
            assertEquals("jpg", FileTypeUtils.getExtension("/images/photo.jpg"));
        }
    }

    @Nested
    @DisplayName("isVideo/isImage/isDocument")
    class TypeChecks {
        @Test
        @DisplayName("视频类型判断")
        void videoCheck() {
            assertTrue(FileTypeUtils.isVideo("test.mp4"));
            assertTrue(FileTypeUtils.isVideo("test.avi"));
            assertFalse(FileTypeUtils.isVideo("test.jpg"));
        }

        @Test
        @DisplayName("图片类型判断")
        void imageCheck() {
            assertTrue(FileTypeUtils.isImage("test.png"));
            assertTrue(FileTypeUtils.isImage("test.jpg"));
            assertFalse(FileTypeUtils.isImage("test.mp4"));
        }

        @Test
        @DisplayName("文档类型判断")
        void documentCheck() {
            assertTrue(FileTypeUtils.isDocument("test.pdf"));
            assertTrue(FileTypeUtils.isDocument("test.docx"));
            assertFalse(FileTypeUtils.isDocument("test.mp4"));
        }
    }

    @Nested
    @DisplayName("isDangerousExtension")
    class DangerousExtension {
        @Test
        @DisplayName("危险扩展名应被检测")
        void dangerousExts() {
            assertTrue(FileTypeUtils.isDangerousExtension("exe"));
            assertTrue(FileTypeUtils.isDangerousExtension("bat"));
            assertTrue(FileTypeUtils.isDangerousExtension("sh"));
            assertTrue(FileTypeUtils.isDangerousExtension("jsp"));
            assertTrue(FileTypeUtils.isDangerousExtension("php"));
        }

        @Test
        @DisplayName("安全扩展名不应被误判")
        void safeExts() {
            assertFalse(FileTypeUtils.isDangerousExtension("jpg"));
            assertFalse(FileTypeUtils.isDangerousExtension("mp4"));
            assertFalse(FileTypeUtils.isDangerousExtension("pdf"));
        }

        @Test
        @DisplayName("大小写不敏感")
        void caseInsensitive() {
            assertTrue(FileTypeUtils.isDangerousExtension("EXE"));
            assertTrue(FileTypeUtils.isDangerousExtension("Bat"));
        }
    }

    @Nested
    @DisplayName("isPathSafe")
    class PathSafety {
        @Test
        @DisplayName("路径遍历应不安全")
        void pathTraversal() {
            assertFalse(FileTypeUtils.isPathSafe("../etc/passwd"));
            assertFalse(FileTypeUtils.isPathSafe("foo/../../bar"));
        }

        @Test
        @DisplayName("绝对路径应不安全")
        void absolutePath() {
            assertFalse(FileTypeUtils.isPathSafe("/etc/passwd"));
            assertFalse(FileTypeUtils.isPathSafe("C:\\Windows\\system32"));
        }

        @Test
        @DisplayName("正常相对路径应安全")
        void normalRelativePath() {
            assertTrue(FileTypeUtils.isPathSafe("images/photo.jpg"));
            assertTrue(FileTypeUtils.isPathSafe("uploads/document.pdf"));
        }

        @Test
        @DisplayName("null/空应不安全")
        void nullEmpty() {
            assertFalse(FileTypeUtils.isPathSafe(null));
            assertFalse(FileTypeUtils.isPathSafe(""));
        }
    }

    @Nested
    @DisplayName("sanitizeFileName")
    class SanitizeFileName {
        @Test
        @DisplayName("应替换非法字符")
        void illegalChars() {
            String result = FileTypeUtils.sanitizeFileName("file<>name.txt");
            assertFalse(result.contains("<"));
            assertFalse(result.contains(">"));
        }

        @Test
        @DisplayName("应处理多个点号")
        void multipleDots() {
            String result = FileTypeUtils.sanitizeFileName("file...txt");
            assertFalse(result.contains("..."));
        }

        @Test
        @DisplayName("null/空应返回空字符串")
        void nullEmpty() {
            assertEquals("", FileTypeUtils.sanitizeFileName(null));
            assertEquals("", FileTypeUtils.sanitizeFileName(""));
        }
    }

    @Nested
    @DisplayName("containsDangerousContent")
    class DangerousContent {
        @Test
        @DisplayName("script标签应被检测")
        void scriptTag() {
            byte[] content = "<script>alert(1)</script>".getBytes();
            assertTrue(FileTypeUtils.containsDangerousContent(content));
        }

        @Test
        @DisplayName("javascript协议应被检测")
        void javascriptProtocol() {
            byte[] content = "javascript:alert(1)".getBytes();
            assertTrue(FileTypeUtils.containsDangerousContent(content));
        }

        @Test
        @DisplayName("正常内容不应被误判")
        void normalContent() {
            byte[] content = "侗族医药文化".getBytes();
            assertFalse(FileTypeUtils.containsDangerousContent(content));
        }

        @Test
        @DisplayName("null/空应返回false")
        void nullEmpty() {
            assertFalse(FileTypeUtils.containsDangerousContent((byte[]) null));
            assertFalse(FileTypeUtils.containsDangerousContent(new byte[0]));
        }
    }

    @Nested
    @DisplayName("generateSafeFileName")
    class GenerateSafeFileName {
        @Test
        @DisplayName("应生成安全文件名")
        void safeName() {
            String result = FileTypeUtils.generateSafeFileName("photo.jpg");
            assertTrue(result.endsWith(".jpg"));
            assertNotEquals("photo.jpg", result);
        }

        @Test
        @DisplayName("应包含时间戳和随机数")
        void containsTimestamp() {
            String result = FileTypeUtils.generateSafeFileName("doc.pdf");
            assertTrue(result.contains("_"));
        }

        @Test
        @DisplayName("应处理特殊字符")
        void specialChars() {
            String result = FileTypeUtils.generateSafeFileName("file<>name.txt");
            assertFalse(result.contains("<"));
            assertFalse(result.contains(">"));
        }
    }

    @Nested
    @DisplayName("getMimeType")
    class GetMimeType {
        @Test
        @DisplayName("应返回正确的MIME类型")
        void correctMimeTypes() {
            assertEquals("video/mp4", FileTypeUtils.getMimeType("test.mp4"));
            assertEquals("image/jpeg", FileTypeUtils.getMimeType("test.jpg"));
            assertEquals("application/pdf", FileTypeUtils.getMimeType("test.pdf"));
        }

        @Test
        @DisplayName("未知类型应返回默认MIME")
        void unknownMime() {
            assertEquals("application/octet-stream", FileTypeUtils.getMimeType("test.xyz"));
        }
    }

    @Nested
    @DisplayName("isPreviewable")
    class IsPreviewable {
        @Test
        @DisplayName("视频/图片/PDF应可预览")
        void previewable() {
            assertTrue(FileTypeUtils.isPreviewable("test.mp4"));
            assertTrue(FileTypeUtils.isPreviewable("test.jpg"));
            assertTrue(FileTypeUtils.isPreviewable("test.pdf"));
        }

        @Test
        @DisplayName("其他文档不应可预览")
        void notPreviewable() {
            assertFalse(FileTypeUtils.isPreviewable("test.docx"));
            assertFalse(FileTypeUtils.isPreviewable("test.xlsx"));
        }
    }
}
