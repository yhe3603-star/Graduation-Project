package com.dongmedicine.common.util;

import com.dongmedicine.service.FileUploadService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileCleanupHelper {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupHelper.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FileUploadService fileUploadService;

    public void deleteFilesFromJson(String jsonPath) {
        if (!StringUtils.hasText(jsonPath)) {
            return;
        }
        parseJsonArray(jsonPath).forEach(this::deleteSingleFile);
    }

    public void deleteSingleFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        String normalizedPath = normalizePath(filePath);
        try {
            boolean deleted = fileUploadService.deleteFile(normalizedPath);
            if (deleted) {
                log.info("Deleted file: {}", normalizedPath);
            } else {
                log.warn("File not found or could not be deleted: {}", normalizedPath);
            }
        } catch (Exception e) {
            log.warn("Failed to delete file: {}", normalizedPath, e);
        }
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        return path.replaceFirst("^/+", "");
    }

    private List<String> parseJsonArray(String json) {
        if (!StringUtils.hasText(json) || !json.trim().startsWith("[")) {
            return new ArrayList<>();
        }
        try {
            // 尝试解析为字符串数组
            try {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                // 如果失败，尝试解析为对象数组
                List<FileInfo> fileInfos = objectMapper.readValue(json, new TypeReference<List<FileInfo>>() {});
                return fileInfos.stream()
                        .map(FileInfo::getPath)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to parse JSON array: {}", json, e);
            return new ArrayList<>();
        }
    }

    // 内部类用于解析文件对象
    private static class FileInfo {
        private String path;
        private String url;
        
        public String getPath() {
            return path != null ? path : url;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
    }
}
