package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.util.FileTypeUtils;
import com.dongmedicine.entity.Resource;
import com.dongmedicine.service.ResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @GetMapping("/list")
    public R<List<Resource>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fileType) {
        return R.ok(service.listByCategoryAndKeywordAndType(category, keyword, fileType));
    }

    @GetMapping("/hot")
    public R<List<Resource>> hot() {
        return R.ok(service.getHotResources());
    }

    @GetMapping("/{id}")
    public R<Resource> getById(@PathVariable Integer id) {
        Resource resource = service.getById(id);
        return resource == null ? R.notFound("资源不存在") : R.ok(resource);
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Integer id) {
        Resource resource = service.getById(id);
        if (resource == null || resource.getFiles() == null || resource.getFiles().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 解析files字段获取第一个文件的path
            JsonNode filesArray = objectMapper.readTree(resource.getFiles());
            if (!filesArray.isArray() || filesArray.size() == 0) {
                return ResponseEntity.notFound().build();
            }
            JsonNode firstFile = filesArray.get(0);
            if (!firstFile.has("path")) {
                return ResponseEntity.notFound().build();
            }
            String fileUrl = firstFile.get("path").asText();

            if (fileUrl.startsWith("http")) {
                service.incrementDownload(id);
                return ResponseEntity.status(302).header(HttpHeaders.LOCATION, fileUrl).build();
            }

            Path filePath = fileUrl.startsWith("/") ? Paths.get(uploadPath, fileUrl.substring(1)) : Paths.get(uploadPath, fileUrl);
            File file = filePath.toFile();
            if (!file.exists()) {
                log.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            service.incrementDownload(id);
            String mimeType = FileTypeUtils.getMimeType(file.getName());
            String displayName = FileTypeUtils.getDisplayName(file.getName(), resource.getTitle());
            byte[] content = Files.readAllBytes(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(displayName, StandardCharsets.UTF_8))
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length))
                    .body(content);
        } catch (IOException e) {
            log.error("Failed to download file: {}", resource.getFiles(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/download")
    public R<String> incrementDownload(@PathVariable Integer id) {
        service.incrementDownload(id);
        return R.ok("ok");
    }

    @GetMapping("/types")
    public R<List<TypeInfo>> getFileTypes() {
        return R.ok(List.of(
                new TypeInfo("video", "视频", List.of("mp4", "avi", "mov", "wmv", "flv", "mkv")),
                new TypeInfo("document", "文档", List.of("docx", "doc", "pdf", "pptx", "ppt", "xlsx", "xls", "txt")),
                new TypeInfo("image", "图片", List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"))
        ));
    }

    @GetMapping("/categories")
    public R<List<String>> getCategories() {
        return R.ok(service.getAllCategories());
    }

    public record TypeInfo(String type, String name, List<String> extensions) {}
}
