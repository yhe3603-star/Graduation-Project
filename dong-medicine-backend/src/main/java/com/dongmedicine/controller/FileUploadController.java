package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.dongmedicine.common.R;
import com.dongmedicine.dto.FileUploadResult;
import com.dongmedicine.service.FileUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "文件上传", description = "图片与附件上传")
@Slf4j
@RestController
@RequestMapping("/api/upload")
@SaCheckRole("admin")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public R<FileUploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @Parameter(name = "category", description = "分类") @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadSingle(() -> fileUploadService.uploadImage(file, category), "图片");
    }

    @Operation(summary = "批量上传图片")
    @PostMapping("/images")
    public R<List<FileUploadResult>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(name = "category", description = "分类") @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadMultiple(() -> fileUploadService.uploadImages(files, category), "批量图片");
    }

    @Operation(summary = "上传视频")
    @PostMapping("/video")
    public R<FileUploadResult> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @Parameter(name = "category", description = "分类") @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadSingle(() -> fileUploadService.uploadVideo(file, category), "视频");
    }

    @Operation(summary = "批量上传视频")
    @PostMapping("/videos")
    public R<List<FileUploadResult>> uploadVideos(
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(name = "category", description = "分类") @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadMultiple(() -> fileUploadService.uploadVideos(files, category), "批量视频");
    }

    @Operation(summary = "上传文档")
    @PostMapping("/document")
    public R<FileUploadResult> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @Parameter(name = "category", description = "分类") @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadSingle(() -> fileUploadService.uploadDocument(file, category), "文档");
    }

    @Operation(summary = "批量上传文档")
    @PostMapping("/documents")
    public R<List<FileUploadResult>> uploadDocuments(
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(name = "category", description = "分类") @RequestParam(value = "category", defaultValue = "common") String category) {
        return uploadMultiple(() -> fileUploadService.uploadDocuments(files, category), "批量文档");
    }

    @Operation(summary = "上传文件")
    @PostMapping("/file")
    public R<FileUploadResult> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Parameter(name = "category", description = "分类") @RequestParam(value = "category", defaultValue = "common") String category,
            @Parameter(name = "subDir", description = "子目录") @RequestParam(value = "subDir", defaultValue = "files") String subDir) {
        return uploadSingle(() -> fileUploadService.uploadFile(file, category, subDir), "文件");
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    public R<Boolean> deleteFile(@Parameter(name = "filePath", description = "文件路径") @RequestParam("filePath") String filePath) {
        try {
            boolean deleted = fileUploadService.deleteFile(filePath);
            return deleted ? R.ok("文件删除成功", true) : R.error("文件删除失败");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return R.error("文件删除失败");
        }
    }

    private R<FileUploadResult> uploadSingle(java.util.function.Supplier<FileUploadResult> uploader, String type) {
        try {
            FileUploadResult result = uploader.get();
            return result.isSuccess() ? R.ok(result) : R.error(result.getMessage());
        } catch (Exception e) {
            log.error("{}上传失败", type, e);
            return R.error(type + "上传失败");
        }
    }

    private R<List<FileUploadResult>> uploadMultiple(java.util.function.Supplier<List<FileUploadResult>> uploader, String type) {
        try {
            return R.ok(uploader.get());
        } catch (Exception e) {
            log.error("{}上传失败", type, e);
            return R.error(type + "上传失败");
        }
    }
}
