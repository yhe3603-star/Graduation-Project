package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.dongmedicine.common.R;
import com.dongmedicine.dto.FileUploadResult;
import com.dongmedicine.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@SaCheckRole("admin")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/image")
    public R<FileUploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadSingle(() -> fileUploadService.uploadImage(file, category), "图片");
    }

    @PostMapping("/images")
    public R<List<FileUploadResult>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadMultiple(() -> fileUploadService.uploadImages(files, category), "批量图片");
    }

    @PostMapping("/video")
    public R<FileUploadResult> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadSingle(() -> fileUploadService.uploadVideo(file, category), "视频");
    }

    @PostMapping("/videos")
    public R<List<FileUploadResult>> uploadVideos(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadMultiple(() -> fileUploadService.uploadVideos(files, category), "批量视频");
    }

    @PostMapping("/document")
    public R<FileUploadResult> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadSingle(() -> fileUploadService.uploadDocument(file, category), "文档");
    }

    @PostMapping("/documents")
    public R<List<FileUploadResult>> uploadDocuments(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadMultiple(() -> fileUploadService.uploadDocuments(files, category), "批量文档");
    }

    @PostMapping("/file")
    public R<FileUploadResult> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "common") String category,
            @RequestParam(value = "subDir", defaultValue = "files") String subDir) {
        return uploadSingle(() -> fileUploadService.uploadFile(file, category, subDir), "文件");
    }

    @DeleteMapping
    public R<Boolean> deleteFile(@RequestParam("filePath") String filePath) {
        try {
            boolean deleted = fileUploadService.deleteFile(filePath);
            return deleted ? R.ok("文件删除成功", true) : R.error("文件删除失败");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return R.error("文件删除失败: " + e.getMessage());
        }
    }

    private R<FileUploadResult> uploadSingle(java.util.function.Supplier<FileUploadResult> uploader, String type) {
        try {
            FileUploadResult result = uploader.get();
            return result.isSuccess() ? R.ok(result) : R.error(result.getMessage());
        } catch (Exception e) {
            log.error("{}上传失败", type, e);
            return R.error(type + "上传失败: " + e.getMessage());
        }
    }

    private R<List<FileUploadResult>> uploadMultiple(java.util.function.Supplier<List<FileUploadResult>> uploader, String type) {
        try {
            return R.ok(uploader.get());
        } catch (Exception e) {
            log.error("{}上传失败", type, e);
            return R.error(type + "上传失败: " + e.getMessage());
        }
    }
}
