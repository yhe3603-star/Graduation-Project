package com.dongmedicine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult {
    
    private boolean success;
    
    private String fileName;
    
    private String originalFileName;
    
    private String filePath;
    
    private String fileUrl;
    
    private String fileType;
    
    private Long fileSize;
    
    private String message;
    
    public static FileUploadResult success(String fileName, String originalFileName, String filePath, 
                                           String fileUrl, String fileType, Long fileSize) {
        return FileUploadResult.builder()
                .success(true)
                .fileName(fileName)
                .originalFileName(originalFileName)
                .filePath(filePath)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .fileSize(fileSize)
                .message("上传成功")
                .build();
    }
    
    public static FileUploadResult fail(String message) {
        return FileUploadResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
