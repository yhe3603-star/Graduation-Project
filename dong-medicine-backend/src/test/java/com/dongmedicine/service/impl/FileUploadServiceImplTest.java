package com.dongmedicine.service.impl;

import com.dongmedicine.config.FileUploadProperties;
import com.dongmedicine.dto.FileUploadResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

    @Mock
    private FileUploadProperties properties;

    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("upload-test");
        lenient().when(properties.getBasePath()).thenReturn(tempDir.toString());
        lenient().when(properties.getAllowedImageExtensionsList())
                .thenReturn(java.util.Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp"));
        lenient().when(properties.getImageMaxSize()).thenReturn(10L * 1024 * 1024);
    }

    @AfterEach
    void tearDown() {
        deleteRecursive(tempDir.toFile());
    }

    private void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }

    @Test
    @DisplayName("上传图片 - 空文件返回失败")
    void testUploadImageEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);

        FileUploadResult result = fileUploadService.uploadImage(emptyFile, "medicine");

        assertFalse(result.isSuccess());
        assertEquals("文件不能为空", result.getMessage());
    }

    @Test
    @DisplayName("上传图片 - 成功上传")
    void testUploadImageSuccess() {
        byte[] pngBytes = createMinimalPng();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", pngBytes);

        FileUploadResult result = fileUploadService.uploadImage(file, "medicine");

        assertTrue(result.isSuccess());
        assertEquals("image", result.getFileType());
        assertNotNull(result.getFileName());
        assertTrue(result.getFileName().endsWith(".png"));
        assertNotNull(result.getFileUrl());
    }

    @Test
    @DisplayName("上传文件 - 空文件返回失败")
    void testUploadFileEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);

        FileUploadResult result = fileUploadService.uploadFile(emptyFile, "test", "files");

        assertFalse(result.isSuccess());
        assertEquals("文件不能为空", result.getMessage());
    }

    @Test
    @DisplayName("获取文件URL - 空路径返回空字符串")
    void testGetFileUrlEmpty() {
        assertEquals("", fileUploadService.getFileUrl(""));
        assertEquals("", fileUploadService.getFileUrl(null));
    }

    @Test
    @DisplayName("获取文件URL - 无前导斜杠自动添加")
    void testGetFileUrlWithoutSlash() {
        String result = fileUploadService.getFileUrl("images/test.png");
        assertEquals("/images/test.png", result);
    }

    @Test
    @DisplayName("获取文件URL - 已有斜杠保持不变")
    void testGetFileUrlWithSlash() {
        String result = fileUploadService.getFileUrl("/images/test.png");
        assertEquals("/images/test.png", result);
    }

    @Test
    @DisplayName("删除文件 - 文件不存在返回false")
    void testDeleteFileNotExists() {
        boolean result = fileUploadService.deleteFile("images/test.png");
        assertFalse(result);
    }

    private byte[] createMinimalPng() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // 1x1 pixel
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53, (byte) 0xDE,
                0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54, // IDAT chunk
                0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xC0, 0x00, 0x00,
                0x00, 0x02, 0x00, 0x01, (byte) 0xE2, 0x21, (byte) 0xBC, 0x33,
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, // IEND chunk
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
    }
}
