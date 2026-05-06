package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Resource;
import com.dongmedicine.service.PopularityAsyncService;
import com.dongmedicine.service.ResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceController测试")
class ResourceControllerTest {

    @Mock
    private ResourceService service;

    @Mock
    private PopularityAsyncService popularityAsyncService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ResourceController resourceController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private Resource testResource;
    private Page<Resource> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        ReflectionTestUtils.setField(resourceController, "uploadPath", "./public");

        testResource = new Resource();
        testResource.setId(1);
        testResource.setTitle("侗药资源");
        testResource.setCategory("视频");
        testResource.setFiles("[{\"path\":\"/files/test.mp4\"}]");
        testResource.setViewCount(100);
        testResource.setDownloadCount(50);

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testResource));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.pageByCategoryAndKeywordAndType(isNull(), isNull(), isNull(), eq(1), eq(12)))
                .thenReturn(testPage);

        R<Map<String, Object>> result = resourceController.list(1, 12, null, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).pageByCategoryAndKeywordAndType(isNull(), isNull(), isNull(), eq(1), eq(12));
    }

    @Test
    @DisplayName("列表查询 - 带分类和关键词")
    void testList_WithFilters() {
        when(service.pageByCategoryAndKeywordAndType(eq("视频"), eq("侗药"), eq("mp4"), eq(1), eq(12)))
                .thenReturn(testPage);

        R<Map<String, Object>> result = resourceController.list(1, 12, "视频", "侗药", "mp4");

        assertEquals(200, result.getCode());
        verify(service).pageByCategoryAndKeywordAndType("视频", "侗药", "mp4", 1, 12);
    }

    @Test
    @DisplayName("热门资源 - 成功")
    void testHot_Success() {
        when(service.getHotResources()).thenReturn(Arrays.asList(testResource));

        R<List<Resource>> result = resourceController.hot();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        verify(service).getHotResources();
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.pageByCategoryAndKeywordAndType(isNull(), eq("侗药"), isNull(), eq(1), eq(12)))
                .thenReturn(testPage);

        R<Map<String, Object>> result = resourceController.search("侗药", 1, 12);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(service).pageByCategoryAndKeywordAndType(isNull(), eq("侗药"), isNull(), eq(1), eq(12));
    }

    @Test
    @DisplayName("详情 - 成功")
    void testGetById_Success() {
        when(service.getDetail(1)).thenReturn(testResource);
        doNothing().when(popularityAsyncService).incrementResourceViewAndPopularity(1);

        R<Resource> result = resourceController.getById(1);

        assertEquals(200, result.getCode());
        assertEquals("侗药资源", result.getData().getTitle());
        verify(service).getDetail(1);
    }

    @Test
    @DisplayName("详情 - 不存在抛异常")
    void testGetById_NotFound() {
        when(service.getDetail(999)).thenReturn(null);
        doNothing().when(popularityAsyncService).incrementResourceViewAndPopularity(999);

        assertThrows(BusinessException.class, () -> resourceController.getById(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        doNothing().when(service).incrementViewCount(1);

        R<String> result = resourceController.incrementView(1);

        assertEquals(200, result.getCode());
        assertEquals("ok", result.getData());
        verify(service).incrementViewCount(1);
    }

    @Test
    @DisplayName("增加下载量 - 成功")
    void testIncrementDownload_Success() {
        doNothing().when(service).incrementDownload(1);

        R<String> result = resourceController.incrementDownload(1);

        assertEquals(200, result.getCode());
        assertEquals("ok", result.getData());
        verify(service).incrementDownload(1);
    }

    @Test
    @DisplayName("获取文件类型 - 成功")
    void testGetFileTypes_Success() {
        R<List<ResourceController.TypeInfo>> result = resourceController.getFileTypes();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
        assertEquals("video", result.getData().get(0).type());
        assertEquals("document", result.getData().get(1).type());
        assertEquals("image", result.getData().get(2).type());
    }

    @Test
    @DisplayName("获取分类列表 - 成功")
    void testGetCategories_Success() {
        when(service.getAllCategories()).thenReturn(Arrays.asList("视频", "文档", "图片"));

        R<List<String>> result = resourceController.getCategories();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
        verify(service).getAllCategories();
    }
}
