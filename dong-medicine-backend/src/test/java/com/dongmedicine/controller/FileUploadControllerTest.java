package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.FileUploadResult;
import com.dongmedicine.service.FileUploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文件上传Controller测试")
class FileUploadControllerTest {

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private FileUploadController fileUploadController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private MockMultipartFile testFile;
    private FileUploadResult successResult;
    private FileUploadResult failResult;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

        testFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());

        successResult = FileUploadResult.success(
                "test.jpg", "test.jpg", "/uploads/images/test.jpg",
                "/api/images/test.jpg", "image/jpeg", 12L);

        failResult = FileUploadResult.fail("上传失败");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("上传图片 - 成功")
    void testUploadImage_Success() {
        when(fileUploadService.uploadImage(eq(testFile), eq("common"))).thenReturn(successResult);

        R<FileUploadResult> result = fileUploadController.uploadImage(testFile, "common");

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isSuccess());
        verify(fileUploadService).uploadImage(testFile, "common");
    }

    @Test
    @DisplayName("上传图片 - 服务返回失败")
    void testUploadImage_ServiceFail() {
        when(fileUploadService.uploadImage(eq(testFile), eq("common"))).thenReturn(failResult);

        R<FileUploadResult> result = fileUploadController.uploadImage(testFile, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("上传图片 - 服务抛异常")
    void testUploadImage_Exception() {
        when(fileUploadService.uploadImage(any(), anyString())).thenThrow(new RuntimeException("IO error"));

        R<FileUploadResult> result = fileUploadController.uploadImage(testFile, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("批量上传图片 - 成功")
    void testUploadImages_Success() {
        List<MultipartFile> files = Arrays.asList(testFile, testFile);
        List<FileUploadResult> results = Arrays.asList(successResult, successResult);
        when(fileUploadService.uploadImages(anyList(), eq("common"))).thenReturn(results);

        R<List<FileUploadResult>> result = fileUploadController.uploadImages(files, "common");

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
        verify(fileUploadService).uploadImages(files, "common");
    }

    @Test
    @DisplayName("批量上传图片 - 服务抛异常")
    void testUploadImages_Exception() {
        List<MultipartFile> files = Arrays.asList(testFile);
        when(fileUploadService.uploadImages(anyList(), anyString())).thenThrow(new RuntimeException("IO error"));

        R<List<FileUploadResult>> result = fileUploadController.uploadImages(files, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("上传视频 - 成功")
    void testUploadVideo_Success() {
        when(fileUploadService.uploadVideo(eq(testFile), eq("common"))).thenReturn(successResult);

        R<FileUploadResult> result = fileUploadController.uploadVideo(testFile, "common");

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isSuccess());
        verify(fileUploadService).uploadVideo(testFile, "common");
    }

    @Test
    @DisplayName("上传视频 - 服务返回失败")
    void testUploadVideo_ServiceFail() {
        when(fileUploadService.uploadVideo(eq(testFile), eq("common"))).thenReturn(failResult);

        R<FileUploadResult> result = fileUploadController.uploadVideo(testFile, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("上传视频 - 服务抛异常")
    void testUploadVideo_Exception() {
        when(fileUploadService.uploadVideo(any(), anyString())).thenThrow(new RuntimeException("IO error"));

        R<FileUploadResult> result = fileUploadController.uploadVideo(testFile, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("批量上传视频 - 成功")
    void testUploadVideos_Success() {
        List<MultipartFile> files = Arrays.asList(testFile);
        List<FileUploadResult> results = Arrays.asList(successResult);
        when(fileUploadService.uploadVideos(anyList(), eq("common"))).thenReturn(results);

        R<List<FileUploadResult>> result = fileUploadController.uploadVideos(files, "common");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(fileUploadService).uploadVideos(files, "common");
    }

    @Test
    @DisplayName("批量上传视频 - 服务抛异常")
    void testUploadVideos_Exception() {
        List<MultipartFile> files = Arrays.asList(testFile);
        when(fileUploadService.uploadVideos(anyList(), anyString())).thenThrow(new RuntimeException("IO error"));

        R<List<FileUploadResult>> result = fileUploadController.uploadVideos(files, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("上传文档 - 成功")
    void testUploadDocument_Success() {
        when(fileUploadService.uploadDocument(eq(testFile), eq("common"))).thenReturn(successResult);

        R<FileUploadResult> result = fileUploadController.uploadDocument(testFile, "common");

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isSuccess());
        verify(fileUploadService).uploadDocument(testFile, "common");
    }

    @Test
    @DisplayName("上传文档 - 服务返回失败")
    void testUploadDocument_ServiceFail() {
        when(fileUploadService.uploadDocument(eq(testFile), eq("common"))).thenReturn(failResult);

        R<FileUploadResult> result = fileUploadController.uploadDocument(testFile, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("上传文档 - 服务抛异常")
    void testUploadDocument_Exception() {
        when(fileUploadService.uploadDocument(any(), anyString())).thenThrow(new RuntimeException("IO error"));

        R<FileUploadResult> result = fileUploadController.uploadDocument(testFile, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("批量上传文档 - 成功")
    void testUploadDocuments_Success() {
        List<MultipartFile> files = Arrays.asList(testFile, testFile);
        List<FileUploadResult> results = Arrays.asList(successResult, successResult);
        when(fileUploadService.uploadDocuments(anyList(), eq("common"))).thenReturn(results);

        R<List<FileUploadResult>> result = fileUploadController.uploadDocuments(files, "common");

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
        verify(fileUploadService).uploadDocuments(files, "common");
    }

    @Test
    @DisplayName("批量上传文档 - 服务抛异常")
    void testUploadDocuments_Exception() {
        List<MultipartFile> files = Arrays.asList(testFile);
        when(fileUploadService.uploadDocuments(anyList(), anyString())).thenThrow(new RuntimeException("IO error"));

        R<List<FileUploadResult>> result = fileUploadController.uploadDocuments(files, "common");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("上传文件 - 成功")
    void testUploadFile_Success() {
        when(fileUploadService.uploadFile(eq(testFile), eq("common"), eq("files"))).thenReturn(successResult);

        R<FileUploadResult> result = fileUploadController.uploadFile(testFile, "common", "files");

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isSuccess());
        verify(fileUploadService).uploadFile(testFile, "common", "files");
    }

    @Test
    @DisplayName("上传文件 - 服务返回失败")
    void testUploadFile_ServiceFail() {
        when(fileUploadService.uploadFile(eq(testFile), eq("common"), eq("files"))).thenReturn(failResult);

        R<FileUploadResult> result = fileUploadController.uploadFile(testFile, "common", "files");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("上传文件 - 服务抛异常")
    void testUploadFile_Exception() {
        when(fileUploadService.uploadFile(any(), anyString(), anyString())).thenThrow(new RuntimeException("IO error"));

        R<FileUploadResult> result = fileUploadController.uploadFile(testFile, "common", "files");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("删除文件 - 成功")
    void testDeleteFile_Success() {
        when(fileUploadService.deleteFile("/uploads/images/test.jpg")).thenReturn(true);

        R<Boolean> result = fileUploadController.deleteFile("/uploads/images/test.jpg");

        assertEquals(200, result.getCode());
        assertTrue(result.getData());
        verify(fileUploadService).deleteFile("/uploads/images/test.jpg");
    }

    @Test
    @DisplayName("删除文件 - 删除失败")
    void testDeleteFile_Fail() {
        when(fileUploadService.deleteFile("/uploads/images/test.jpg")).thenReturn(false);

        R<Boolean> result = fileUploadController.deleteFile("/uploads/images/test.jpg");

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("删除文件 - 服务抛异常")
    void testDeleteFile_Exception() {
        when(fileUploadService.deleteFile(anyString())).thenThrow(new RuntimeException("IO error"));

        R<Boolean> result = fileUploadController.deleteFile("/uploads/images/test.jpg");

        assertEquals(500, result.getCode());
    }
}
