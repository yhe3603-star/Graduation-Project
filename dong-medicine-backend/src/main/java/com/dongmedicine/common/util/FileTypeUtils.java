package com.dongmedicine.common.util;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public final class FileTypeUtils {

    private static final Map<String, String> EXTENSION_TO_TYPE = Map.ofEntries(
        Map.entry("mp4", "video"), Map.entry("avi", "video"), Map.entry("mov", "video"),
        Map.entry("wmv", "video"), Map.entry("flv", "video"), Map.entry("mkv", "video"),
        Map.entry("docx", "document"), Map.entry("doc", "document"), Map.entry("pdf", "document"),
        Map.entry("pptx", "document"), Map.entry("ppt", "document"), Map.entry("xlsx", "document"),
        Map.entry("xls", "document"), Map.entry("txt", "document"),
        Map.entry("jpg", "image"), Map.entry("jpeg", "image"), Map.entry("png", "image"),
        Map.entry("gif", "image"), Map.entry("bmp", "image"), Map.entry("webp", "image"), Map.entry("svg", "image")
    );

    private static final Map<String, String> TYPE_TO_ICON = Map.of(
        "video", "VideoPlay", "document", "Document", "image", "Picture"
    );

    private static final Map<String, String> TYPE_TO_MIME = Map.ofEntries(
        Map.entry("mp4", "video/mp4"), Map.entry("avi", "video/x-msvideo"), Map.entry("mov", "video/quicktime"),
        Map.entry("pdf", "application/pdf"),
        Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        Map.entry("doc", "application/msword"),
        Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        Map.entry("ppt", "application/vnd.ms-powerpoint"),
        Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        Map.entry("xls", "application/vnd.ms-excel"),
        Map.entry("jpg", "image/jpeg"), Map.entry("jpeg", "image/jpeg"),
        Map.entry("png", "image/png"), Map.entry("gif", "image/gif")
    );

    private static final Map<String, byte[]> FILE_SIGNATURES = Map.ofEntries(
        Map.entry("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        Map.entry("jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        Map.entry("png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
        Map.entry("gif", new byte[]{'G', 'I', 'F'}),
        Map.entry("bmp", new byte[]{'B', 'M'}),
        Map.entry("webp", new byte[]{'R', 'I', 'F', 'F'}),
        Map.entry("pdf", new byte[]{'%', 'P', 'D', 'F'}),
        Map.entry("doc", new byte[]{(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0}),
        Map.entry("xls", new byte[]{(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0}),
        Map.entry("ppt", new byte[]{(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0}),
        Map.entry("docx", new byte[]{0x50, 0x4B, 0x03, 0x04}),
        Map.entry("xlsx", new byte[]{0x50, 0x4B, 0x03, 0x04}),
        Map.entry("pptx", new byte[]{0x50, 0x4B, 0x03, 0x04}),
        Map.entry("mp4", new byte[]{0x00, 0x00, 0x00}),
        Map.entry("avi", new byte[]{'R', 'I', 'F', 'F'})
    );

    public static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mov", "wmv", "flv", "mkv");
    public static final Set<String> DOCUMENT_EXTENSIONS = Set.of("docx", "doc", "pdf", "pptx", "ppt", "xlsx", "xls", "txt");
    public static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");

    private FileTypeUtils() {
    }

    public static String getFileType(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "other";
        }
        return EXTENSION_TO_TYPE.getOrDefault(getExtension(filename).toLowerCase(), "other");
    }

    public static String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 && lastDot < filename.length() - 1 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    public static String getMimeType(String filename) {
        return TYPE_TO_MIME.getOrDefault(getExtension(filename), "application/octet-stream");
    }

    public static String getTypeIcon(String fileType) {
        return TYPE_TO_ICON.getOrDefault(fileType, "Document");
    }

    public static String getTypeName(String fileType) {
        return switch (fileType) {
            case "video" -> "视频";
            case "document" -> "文档";
            case "image" -> "图片";
            default -> "其他";
        };
    }

    public static boolean isPreviewable(String filename) {
        String ext = getExtension(filename);
        return VIDEO_EXTENSIONS.contains(ext) || IMAGE_EXTENSIONS.contains(ext) || "pdf".equals(ext);
    }

    public static boolean isVideo(String filename) {
        return VIDEO_EXTENSIONS.contains(getExtension(filename));
    }

    public static boolean isDocument(String filename) {
        return DOCUMENT_EXTENSIONS.contains(getExtension(filename));
    }

    public static boolean isImage(String filename) {
        return IMAGE_EXTENSIONS.contains(getExtension(filename));
    }

    public static String getFileName(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return "";
        }
        int lastSlash = fileUrl.lastIndexOf('/');
        return lastSlash >= 0 && lastSlash < fileUrl.length() - 1 ? fileUrl.substring(lastSlash + 1) : fileUrl;
    }

    public static String getDisplayName(String filename, String title) {
        if (!StringUtils.hasText(title)) {
            return filename;
        }
        String ext = getExtension(filename);
        return StringUtils.hasText(ext) ? title + "." + ext : title;
    }

    public static String detectFileTypeByContent(InputStream inputStream, String extension) throws IOException {
        String ext = extension.toLowerCase();
        byte[] expectedSignature = FILE_SIGNATURES.get(ext);
        
        if (expectedSignature == null) {
            return ext;
        }

        int maxSignatureLen = 8;
        byte[] fileHeader = new byte[maxSignatureLen];
        
        java.io.BufferedInputStream bufferedStream = null;
        boolean needClose = false;
        
        try {
            if (!inputStream.markSupported()) {
                bufferedStream = new java.io.BufferedInputStream(inputStream);
                needClose = false;
                inputStream = bufferedStream;
            }
            
            inputStream.mark(maxSignatureLen + 1);
            int bytesRead = inputStream.read(fileHeader);
            inputStream.reset();

            if (bytesRead < expectedSignature.length) {
                return null;
            }

            if (matchesSignature(fileHeader, expectedSignature)) {
                return ext;
            }
            
            return null;
        } finally {
            if (bufferedStream != null && needClose) {
                try {
                    bufferedStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static boolean validateFileContent(InputStream inputStream, String extension) throws IOException {
        String detectedType = detectFileTypeByContent(inputStream, extension);
        if (detectedType == null) {
            return false;
        }
        String ext = extension.toLowerCase();
        if (detectedType.equalsIgnoreCase(ext)) {
            return true;
        }
        return isCompatibleExtension(ext, detectedType);
    }

    private static boolean isCompatibleExtension(String ext, String detectedType) {
        return ("jpg".equals(ext) && "jpeg".equals(detectedType)) ||
               ("jpeg".equals(ext) && "jpg".equals(detectedType)) ||
               (Set.of("docx", "xlsx", "pptx").contains(ext) && "docx".equals(detectedType)) ||
               (Set.of("doc", "xls", "ppt").contains(ext) && "doc".equals(detectedType));
    }

    private static boolean matchesSignature(byte[] fileHeader, byte[] signature) {
        if (fileHeader.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (fileHeader[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    public static String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        String sanitized = fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.+|\\.+$", "");

        if (sanitized.length() > 255) {
            String ext = getExtension(sanitized);
            String baseName = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = baseName.substring(0, 250 - ext.length()) + "." + ext;
        }
        return sanitized;
    }

    public static boolean isPathSafe(String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return !path.contains("..") && !path.contains("~") &&
               !path.contains("//") && !path.contains("\\\\") &&
               !path.startsWith("/") && !path.startsWith("\\") &&
               !path.matches("^[A-Za-z]:.*");
    }

    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar",
        "sh", "bash", "zsh", "php", "asp", "aspx", "jsp", "cgi",
        "dll", "so", "dylib", "app", "deb", "rpm", "dmg", "pkg"
    );

    private static final Set<String> DANGEROUS_CONTENT_PATTERNS = Set.of(
        "<script", "javascript:", "vbscript:", "onload=", "onerror=",
        "eval(", "document.cookie", "window.location"
    );

    public static boolean isDangerousExtension(String extension) {
        return DANGEROUS_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static boolean containsDangerousContent(byte[] content) {
        if (content == null || content.length == 0) {
            return false;
        }
        
        try {
            String contentStr = new String(content, 0, Math.min(content.length, 4096));
            String lowerContent = contentStr.toLowerCase();
            
            for (String pattern : DANGEROUS_CONTENT_PATTERNS) {
                if (lowerContent.contains(pattern.toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }

    public static boolean containsDangerousContent(InputStream inputStream) throws IOException {
        inputStream.mark(4096);
        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        inputStream.reset();
        
        if (bytesRead <= 0) {
            return false;
        }
        
        return containsDangerousContent(java.util.Arrays.copyOf(buffer, bytesRead));
    }

    public static String generateSafeFileName(String originalFileName) {
        String ext = getExtension(originalFileName);
        String baseName = originalFileName;
        
        if (StringUtils.hasText(ext)) {
            baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        }
        
        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_");
        
        if (baseName.length() > 200) {
            baseName = baseName.substring(0, 200);
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        if (StringUtils.hasText(ext)) {
            return baseName + "_" + timestamp + "_" + random + "." + ext;
        }
        return baseName + "_" + timestamp + "_" + random;
    }
}
