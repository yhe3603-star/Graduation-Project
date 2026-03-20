package com.dongmedicine.service;

import com.dongmedicine.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileUploadService {
    
    FileUploadResult uploadImage(MultipartFile file, String category);
    
    List<FileUploadResult> uploadImages(List<MultipartFile> files, String category);
    
    FileUploadResult uploadVideo(MultipartFile file, String category);
    
    List<FileUploadResult> uploadVideos(List<MultipartFile> files, String category);
    
    FileUploadResult uploadDocument(MultipartFile file, String category);
    
    List<FileUploadResult> uploadDocuments(List<MultipartFile> files, String category);
    
    FileUploadResult uploadFile(MultipartFile file, String category, String subDir);
    
    boolean deleteFile(String filePath);
    
    String getFileUrl(String filePath);
}
