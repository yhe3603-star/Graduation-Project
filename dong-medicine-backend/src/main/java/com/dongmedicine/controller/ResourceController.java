package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.FileTypeUtils;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.config.RateLimit;
import com.dongmedicine.entity.Resource;
import com.dongmedicine.service.PopularityAsyncService;
import com.dongmedicine.service.ResourceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "学习资源", description = "侗乡医药学习资源下载与管理")
@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService service;
    private final PopularityAsyncService popularityAsyncService;
    private final ObjectMapper objectMapper;

    @Value("${file.upload.base-path:./public}")
    private String uploadPath;

    private static final int STREAM_BUFFER_SIZE = 8192;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fileType) {
        Page<Resource> pageResult = service.pageByCategoryAndKeywordAndType(category, keyword, fileType, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/hot")
    public R<List<Resource>> hot() {
        return R.ok(service.getHotResources());
    }

    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<Resource> pageResult = service.pageByCategoryAndKeywordAndType(null, keyword, null, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/{id}")
    public R<Resource> getById(@PathVariable Integer id) {
        Resource resource = service.getDetail(id);
        if (resource == null) throw BusinessException.notFound("资源不存在");
        try { popularityAsyncService.incrementResourceViewAndPopularity(id); } catch (Exception e) { log.debug("更新浏览量失败", e); }
        return R.ok(resource);
    }

    @PostMapping("/{id}/view")
    @RateLimit(value = 10, key = "resource_view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Resource resource = service.getById(id);
        if (resource == null || resource.getFiles() == null || resource.getFiles().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "资源不存在");
            return;
        }

        JsonNode filesArray = objectMapper.readTree(resource.getFiles());
        if (!filesArray.isArray() || filesArray.size() == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        JsonNode firstFile = filesArray.get(0);
        if (!firstFile.has("path")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件路径不存在");
            return;
        }

        String fileUrl = firstFile.get("path").asText();

        if (fileUrl.startsWith("http")) {
            service.incrementDownload(id);
            response.sendRedirect(fileUrl);
            return;
        }

        Path basePath = Paths.get(uploadPath).normalize();
        Path filePath = (fileUrl.startsWith("/")
                ? Paths.get(uploadPath, fileUrl.substring(1))
                : Paths.get(uploadPath, fileUrl)).normalize();

        if (!filePath.startsWith(basePath)) {
            log.warn("检测到路径遍历攻击: fileUrl={}, resolvedPath={}", fileUrl, filePath);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "非法文件路径");
            return;
        }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            log.warn("文件不存在: {}", filePath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        service.incrementDownload(id);

        String mimeType = FileTypeUtils.getMimeType(file.getName());
        String displayName = FileTypeUtils.getDisplayName(file.getName(), resource.getTitle());
        long fileSize = file.length();

        response.setContentType(mimeType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename*=UTF-8''" + URLEncoder.encode(displayName, StandardCharsets.UTF_8));
        response.setContentLengthLong(fileSize);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

        try (InputStream is = Files.newInputStream(filePath);
             OutputStream os = response.getOutputStream()) {
            
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            
            log.info("文件下载成功: {} ({} 字节)", displayName, fileSize);
        } catch (IOException e) {
            log.error("文件下载失败: {}", filePath, e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败");
            }
        }
    }

    @PostMapping("/batch-download")
    public void batchDownload(@RequestBody Map<String, List<Integer>> body, HttpServletResponse response) throws IOException {
        List<Integer> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "请提供要下载的资源ID列表");
            return;
        }

        String zipFileName = "resources_batch_" + java.time.LocalDate.now() + ".zip";
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + URLEncoder.encode(zipFileName, StandardCharsets.UTF_8));

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Integer id : ids) {
                Resource resource = service.getById(id);
                if (resource == null || resource.getFiles() == null || resource.getFiles().isEmpty()) {
                    log.warn("批量下载跳过不存在的资源: id={}", id);
                    continue;
                }

                JsonNode filesArray = objectMapper.readTree(resource.getFiles());
                if (!filesArray.isArray()) continue;

                for (int i = 0; i < filesArray.size(); i++) {
                    JsonNode fileNode = filesArray.get(i);
                    if (!fileNode.has("path")) continue;

                    String fileUrl = fileNode.get("path").asText();
                    if (fileUrl.startsWith("http")) {
                        log.info("批量下载跳过远程URL: {}", fileUrl);
                        continue;
                    }

                    Path basePath = Paths.get(uploadPath).normalize();
                    Path filePath = (fileUrl.startsWith("/")
                            ? Paths.get(uploadPath, fileUrl.substring(1))
                            : Paths.get(uploadPath, fileUrl)).normalize();

                    if (!filePath.startsWith(basePath)) {
                        log.warn("批量下载检测到路径遍历攻击: fileUrl={}, resolvedPath={}", fileUrl, filePath);
                        continue;
                    }

                    File file = filePath.toFile();
                    if (!file.exists() || !file.isFile()) {
                        log.warn("批量下载文件不存在: {}", filePath);
                        continue;
                    }

                    // Create entry with resource folder to avoid name collisions
                    String entryName = "resource_" + id + "/" + file.getName();
                    zos.putNextEntry(new ZipEntry(entryName));

                    try (InputStream is = Files.newInputStream(filePath)) {
                        byte[] buffer = new byte[STREAM_BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    zos.closeEntry();

                    service.incrementDownload(id);
                }
            }
            zos.finish();
        } catch (IOException e) {
            log.error("批量下载失败", e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "批量下载失败");
            }
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
