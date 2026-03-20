package com.dongmedicine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {
    
    private String basePath = "public";
    
    private Long imageMaxSize = 10 * 1024 * 1024L;
    
    private Long videoMaxSize = 100 * 1024 * 1024L;
    
    private Long documentMaxSize = 50 * 1024 * 1024L;
    
    private String allowedImageExtensions = "jpg,jpeg,png,gif,bmp,webp";
    
    private String allowedVideoExtensions = "mp4,avi,mov,wmv,flv,mkv";
    
    private String allowedDocumentExtensions = "pdf,docx,doc,xlsx,xls,pptx,ppt,txt";
    
    public List<String> getAllowedImageExtensionsList() {
        return normalizeExtensions(allowedImageExtensions);
    }
    
    public List<String> getAllowedVideoExtensionsList() {
        return normalizeExtensions(allowedVideoExtensions);
    }
    
    public List<String> getAllowedDocumentExtensionsList() {
        return normalizeExtensions(allowedDocumentExtensions);
    }

    private List<String> normalizeExtensions(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
