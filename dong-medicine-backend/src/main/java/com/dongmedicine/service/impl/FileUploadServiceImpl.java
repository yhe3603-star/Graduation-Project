package com.dongmedicine.service.impl;

import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.FileTypeUtils;
import com.dongmedicine.config.FileUploadProperties;
import com.dongmedicine.dto.FileUploadResult;
import com.dongmedicine.mq.producer.FileProcessProducer;
import com.dongmedicine.mq.producer.FileProcessProducer.FileProcessTask;
import com.dongmedicine.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
    
    private final FileUploadProperties properties;

    @Autowired(required = false)
    private FileProcessProducer fileProcessProducer;
    
    @Override
    public FileUploadResult uploadImage(MultipartFile file, String category) {
        return uploadFile(file, category, "images");
    }
    
    @Override
    public List<FileUploadResult> uploadImages(List<MultipartFile> files, String category) {
        List<FileUploadResult> results = new ArrayList<>();
        files.forEach(file -> results.add(uploadImage(file, category)));
        return results;
    }
    
    @Override
    public FileUploadResult uploadVideo(MultipartFile file, String category) {
        return uploadFile(file, category, "videos");
    }
    
    @Override
    public List<FileUploadResult> uploadVideos(List<MultipartFile> files, String category) {
        List<FileUploadResult> results = new ArrayList<>();
        files.forEach(file -> results.add(uploadVideo(file, category)));
        return results;
    }
    
    @Override
    public FileUploadResult uploadDocument(MultipartFile file, String category) {
        String extension = FileTypeUtils.getExtension(file.getOriginalFilename());
        return uploadFile(file, category, "documents/" + getDocumentSubDir(extension));
    }
    
    @Override
    public List<FileUploadResult> uploadDocuments(List<MultipartFile> files, String category) {
        List<FileUploadResult> results = new ArrayList<>();
        files.forEach(file -> results.add(uploadDocument(file, category)));
        return results;
    }
    
    @Override
    public FileUploadResult uploadFile(MultipartFile file, String category, String subDir) {
        if (file == null || file.isEmpty()) {
            return FileUploadResult.fail("文件不能为空");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            return FileUploadResult.fail("文件名不能为空");
        }
        
        originalFileName = FileTypeUtils.sanitizeFileName(originalFileName);
        String extension = FileTypeUtils.getExtension(originalFileName);
        String fileType = FileTypeUtils.getFileType(originalFileName);
        
        validateFile(file, extension, fileType);
        
        String safeCategory = normalizePathSegment(category, "common");
        String safeSubDir = normalizePathSegment(subDir, "files");

        if (!FileTypeUtils.isPathSafe(safeCategory) || !FileTypeUtils.isPathSafe(safeSubDir)) {
            log.warn("检测到路径遍历攻击尝试: category={}, subDir={}", category, subDir);
            return FileUploadResult.fail("非法的文件路径");
        }
        
        try {
            validateFileContent(file.getInputStream(), extension);
        } catch (IOException e) {
            log.error("文件内容校验失败: {}", originalFileName, e);
            return FileUploadResult.fail("文件内容校验失败");
        }
        
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = safeSubDir + "/" + safeCategory;
        String fullPath = properties.getBasePath() + "/" + relativePath;
        
        try {
            Path directory = Paths.get(fullPath).normalize();
            Path basePath = Paths.get(properties.getBasePath()).normalize();
            
            if (!directory.startsWith(basePath)) {
                log.warn("检测到路径遍历攻击: fullPath={}, basePath={}", fullPath, properties.getBasePath());
                return FileUploadResult.fail("非法的文件路径");
            }
            
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            Path filePath = directory.resolve(fileName).normalize();
            
            if (!filePath.startsWith(directory)) {
                log.warn("检测到路径遍历攻击: filePath={}", filePath);
                return FileUploadResult.fail("非法的文件路径");
            }
            
            file.transferTo(filePath.toFile());
            
            String storedPath = "/" + (relativePath + "/" + fileName).replace("\\", "/");
            log.info("文件上传成功: {} -> {}", originalFileName, storedPath);
            
            submitFileProcessTask(fileName, filePath.toString(), originalFileName, fileType, file.getSize());
            
            return FileUploadResult.success(fileName, originalFileName, storedPath, storedPath, fileType, file.getSize());
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalFileName, e);
            return FileUploadResult.fail("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteFile(String filePath) {
        String safeFilePath = normalizeFilePath(filePath);
        if (!StringUtils.hasText(safeFilePath) || !FileTypeUtils.isPathSafe(safeFilePath)) {
            log.warn("检测到路径遍历攻击尝试: filePath={}", filePath);
            return false;
        }
        
        try {
            Path basePath = Paths.get(properties.getBasePath()).normalize();
            Path path = Paths.get(properties.getBasePath(), safeFilePath).normalize();
            
            if (!path.startsWith(basePath)) {
                log.warn("检测到路径遍历攻击: filePath={}", filePath);
                return false;
            }
            
            File file = path.toFile();
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", safeFilePath);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("文件删除失败: {}", safeFilePath, e);
            return false;
        }
    }
    
    @Override
    public String getFileUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) return "";
        String normalized = filePath.replace("\\", "/");
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }
    
    private void validateFile(MultipartFile file, String extension, String fileType) {
        if (FileTypeUtils.isDangerousExtension(extension)) {
            log.warn("检测到危险文件扩展名: {}", extension);
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "不允许上传此类型的文件: " + extension);
        }
        
        long fileSize = file.getSize();
        
        switch (fileType) {
            case "image":
                if (!properties.getAllowedImageExtensionsList().contains(extension)) {
                    throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "不支持的图片格式: " + extension + "，支持的格式: " + properties.getAllowedImageExtensions());
                }
                if (fileSize > properties.getImageMaxSize()) {
                    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "图片大小超出限制，最大允许: " + formatFileSize(properties.getImageMaxSize()));
                }
                break;
            case "video":
                if (!properties.getAllowedVideoExtensionsList().contains(extension)) {
                    throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "不支持的视频格式: " + extension + "，支持的格式: " + properties.getAllowedVideoExtensions());
                }
                if (fileSize > properties.getVideoMaxSize()) {
                    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "视频大小超出限制，最大允许: " + formatFileSize(properties.getVideoMaxSize()));
                }
                break;
            case "document":
                if (!properties.getAllowedDocumentExtensionsList().contains(extension)) {
                    throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "不支持的文档格式: " + extension + "，支持的格式: " + properties.getAllowedDocumentExtensions());
                }
                if (fileSize > properties.getDocumentMaxSize()) {
                    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "文档大小超出限制，最大允许: " + formatFileSize(properties.getDocumentMaxSize()));
                }
                break;
            default:
                throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "不支持的文件类型: " + fileType);
        }
    }
    
    private void validateFileContent(InputStream inputStream, String extension) throws IOException {
        if (!FileTypeUtils.IMAGE_EXTENSIONS.contains(extension) && !FileTypeUtils.DOCUMENT_EXTENSIONS.contains(extension)) {
            return;
        }
        
        try {
            boolean isValid = FileTypeUtils.validateFileContent(inputStream, extension);
            if (!isValid) {
                log.warn("文件内容校验失败: 扩展名 {} 与实际内容不匹配，拒绝上传", extension);
                throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, 
                    "文件扩展名 " + extension + " 与实际内容不匹配，疑似伪造文件类型");
            }
            log.info("文件内容验证通过: extension={}", extension);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件内容校验异常: extension={}, error={}", extension, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "文件内容校验失败，无法确认文件类型");
        }
    }
    
    private String getDocumentSubDir(String extension) {
        if ("pdf".equals(extension)) return "pdf";
        if ("docx".equals(extension) || "doc".equals(extension)) return "docx";
        if ("xlsx".equals(extension) || "xls".equals(extension)) return "xlsx";
        if ("pptx".equals(extension) || "ppt".equals(extension)) return "pptx";
        return "other";
    }

    private String normalizePathSegment(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        String normalized = value.replace("\\", "/").trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9/_-]", "_");
        normalized = normalized.replaceAll("/{2,}", "/");
        normalized = normalized.replaceAll("^/+", "").replaceAll("/+$", "");
        return StringUtils.hasText(normalized) ? normalized : defaultValue;
    }

    private String normalizeFilePath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }
        return filePath.replace("\\", "/").trim().replaceAll("^/+", "");
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
    
    private void submitFileProcessTask(String fileName, String filePath, String originalFileName, String fileType, long fileSize) {
        if (fileProcessProducer == null) return;
        try {
            String processType = determineProcessType(fileType);
            
            FileProcessTask task = new FileProcessTask();
            task.setFileId(UUID.randomUUID().toString());
            task.setFilePath(filePath);
            task.setFileName(fileName);
            task.setFileType(fileType);
            task.setProcessType(processType);
            task.setOriginalFileName(originalFileName);
            task.setFileSize(fileSize);
            
            fileProcessProducer.sendFileProcessTask(task);
            log.debug("文件处理任务已发送到 RabbitMQ, fileName={}, processType={}", fileName, processType);
            
        } catch (Exception e) {
            log.warn("发送文件处理任务到 RabbitMQ 失败, fileName={}, error={}", fileName, e.getMessage());
        }
    }
    
    private String determineProcessType(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "image" -> "image-compress";
            case "document" -> "document-convert";
            default -> "file-validate";
        };
    }
}
