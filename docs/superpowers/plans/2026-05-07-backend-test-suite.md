# Backend Test Suite Supplement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 24 Controller unit tests and 5 Service unit tests to achieve 27/27 controller and 18/18 service coverage.

**Architecture:** All tests follow the existing pattern: `@ExtendWith(MockitoExtension.class)` with `@Mock`/`@InjectMocks`. Controllers are tested as plain Java objects (no MockMvc HTTP layer). Sa-Token auth is mocked via `MockedStatic<SecurityUtils>`. Each test uses Chinese `@DisplayName` and the AAA pattern.

**Tech Stack:** JUnit 5, Mockito 5, `@ExtendWith(MockitoExtension.class)`, `MockedStatic<SecurityUtils>`, `R<T>` response assertions

---

## Conventions (apply to ALL tasks)

Every test file MUST:
1. Use `@ExtendWith(MockitoExtension.class)` at class level
2. Use `@DisplayName("中文描述")` at class and method level
3. Mock `SecurityUtils` in `@BeforeEach` via `mockStatic(SecurityUtils.class)`
4. Close `securityUtilsMock` in `@AfterEach`
5. Assert responses via `assertEquals(200, result.getCode())` and `assertTrue(result.isSuccess())`
6. Use `verify(mock).method(...)` to confirm service interactions
7. Follow AAA pattern: Arrange (when/thenReturn), Act (call controller method), Assert (assertEquals/verify)

### Auth mocking pattern (used in every test file):

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("XXX控制器测试")
class XxxControllerTest {

    @Mock
    private SomeService service;

    @InjectMocks
    private XxxController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }
}
```

### Admin auth pattern (for admin controller tests):

```java
// In setUp(), simulate logged-in admin user:
securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

// Note: @SaCheckRole("admin") is on the class, but since we're using
// @InjectMocks (not MockMvc), the annotation is NOT enforced at runtime.
// Tests verify the controller METHOD logic, not the AOP interceptor.
// The "admin auth" test verifies behavior when SecurityUtils returns admin userId.
```

---

## Task 1: Content Management Controllers (Knowledge, Inheritor, Qa)

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/KnowledgeControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/InheritorControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/QaControllerTest.java`

**Reference:** Existing `PlantControllerTest.java` follows the exact same pattern (list, search, detail, notFound, incrementView).

### Step 1: Create KnowledgeControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.service.BrowseHistoryService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PopularityAsyncService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("知识库Controller测试")
class KnowledgeControllerTest {

    @Mock
    private KnowledgeService service;
    @Mock
    private BrowseHistoryService browseHistoryService;
    @Mock
    private PopularityAsyncService popularityAsyncService;

    @InjectMocks
    private KnowledgeController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Knowledge testKnowledge;
    private Page<Knowledge> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testKnowledge = new Knowledge();
        testKnowledge.setId(1);
        testKnowledge.setTitle("侗药炮制方法");
        testKnowledge.setType("prescription");
        testKnowledge.setContent("详细内容");

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testKnowledge));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.advancedSearchPaged(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.list(1, 12, null, null, null, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("列表查询 - 带筛选条件")
    void testList_WithFilters() {
        when(service.advancedSearchPaged(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.list(1, 12, "popularity", "侗药", "骨伤", "风湿", "钩藤");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.searchPagedAdvanced(any(), any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.search("侗药", null, null, null, "popularity", 1, 12);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("详情 - 成功")
    void testDetail_Success() {
        when(service.getById(1)).thenReturn(testKnowledge);

        R<Knowledge> result = controller.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("侗药炮制方法", result.getData().getTitle());
    }

    @Test
    @DisplayName("详情 - 不存在")
    void testDetail_NotFound() {
        when(service.getById(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> controller.detail(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        R<String> result = controller.incrementView(1);

        assertEquals(200, result.getCode());
        verify(popularityAsyncService).incrementKnowledgeViewAndPopularity(1);
    }

    @Test
    @DisplayName("收藏 - 需要登录")
    void testFavorite_RequiresLogin() {
        // SecurityUtils returns null (not logged in)
        R<String> result = controller.favorite(1);

        // Controller checks login and returns error
        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("收藏 - 登录用户成功")
    void testFavorite_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(service.getById(1)).thenReturn(testKnowledge);
        doNothing().when(browseHistoryService).record(eq(1), eq("knowledge"), eq(1));

        R<String> result = controller.favorite(1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("反馈 - 需要登录")
    void testFeedback_RequiresLogin() {
        R<String> result = controller.feedback(1, "内容不错");

        assertEquals(401, result.getCode());
    }
}
```

### Step 2: Create InheritorControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.service.BrowseHistoryService;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.PopularityAsyncService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("传承人Controller测试")
class InheritorControllerTest {

    @Mock
    private InheritorService service;
    @Mock
    private BrowseHistoryService browseHistoryService;
    @Mock
    private PopularityAsyncService popularityAsyncService;

    @InjectMocks
    private InheritorController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Inheritor testInheritor;
    private Page<Inheritor> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testInheritor = new Inheritor();
        testInheritor.setId(1);
        testInheritor.setName("吴某某");
        testInheritor.setLevel("国家级");
        testInheritor.setSpecialties("骨伤科");

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testInheritor));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.advancedSearchPaged(any(), any(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.list(1, 12, null, null, "name");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("列表查询 - 按等级筛选")
    void testList_WithLevel() {
        when(service.advancedSearchPaged(eq("国家级"), isNull(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.list(1, 12, "国家级", null, "name");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.searchPaged(eq("吴"), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.search("吴", 1, 12);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("详情 - 成功")
    void testDetail_Success() {
        when(service.getById(1)).thenReturn(testInheritor);

        R<Inheritor> result = controller.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("吴某某", result.getData().getName());
    }

    @Test
    @DisplayName("详情 - 不存在")
    void testDetail_NotFound() {
        when(service.getById(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> controller.detail(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        R<String> result = controller.incrementView(1);

        assertEquals(200, result.getCode());
        verify(popularityAsyncService).incrementInheritorViewAndPopularity(1);
    }
}
```

### Step 3: Create QaControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.service.PopularityAsyncService;
import com.dongmedicine.service.QaService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("问答Controller测试")
class QaControllerTest {

    @Mock
    private QaService service;
    @Mock
    private PopularityAsyncService popularityAsyncService;

    @InjectMocks
    private QaController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Qa testQa;
    private Page<Qa> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testQa = new Qa();
        testQa.setId(1);
        testQa.setQuestion("什么是侗医药？");
        testQa.setAnswer("侗医药是...");
        testQa.setCategory("基础知识");

        testPage = new Page<>(1, 12, 1);
        testPage.setRecords(Arrays.asList(testQa));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        when(service.advancedSearchPaged(any(), any(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.list(1, 12, null, null);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("列表查询 - 带分类")
    void testList_WithCategory() {
        when(service.advancedSearchPaged(eq("基础知识"), isNull(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.list(1, 12, "基础知识", null);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.searchPaged(eq("侗医药"), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.search("侗医药", 1, 12);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("详情 - 成功")
    void testDetail_Success() {
        when(service.getById(1)).thenReturn(testQa);

        R<Qa> result = controller.detail(1);

        assertEquals(200, result.getCode());
        assertEquals("什么是侗医药？", result.getData().getQuestion());
    }

    @Test
    @DisplayName("详情 - 不存在")
    void testDetail_NotFound() {
        when(service.getById(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> controller.detail(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        R<String> result = controller.incrementView(1);

        assertEquals(200, result.getCode());
        verify(popularityAsyncService).incrementQaViewAndPopularity(1);
    }
}
```

### Step 4: Run tests to verify they pass

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="KnowledgeControllerTest,InheritorControllerTest,QaControllerTest" -pl .`

Expected: All tests PASS. If any fail due to method signature mismatches, adjust the controller method call to match the actual signature.

### Step 5: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/controller/KnowledgeControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/InheritorControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/QaControllerTest.java
git commit -m "test: add Knowledge, Inheritor, Qa controller unit tests"
```

---

## Task 2: Resource Controller + Chat Controller

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/ResourceControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/ChatControllerTest.java`

### Step 1: Create ResourceControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Resource;
import com.dongmedicine.service.PopularityAsyncService;
import com.dongmedicine.service.ResourceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("资源Controller测试")
class ResourceControllerTest {

    @Mock
    private ResourceService service;
    @Mock
    private PopularityAsyncService popularityAsyncService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ResourceController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Resource testResource;
    private Page<Resource> testPage;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);

        testResource = new Resource();
        testResource.setId(1);
        testResource.setTitle("侗医药教材");
        testResource.setCategory("教材");
        testResource.setFiles("[\"/documents/test.pdf\"]");

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
        when(service.advancedSearchPaged(any(), any(), any(), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.list(1, 12, null, null, null);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("热门资源 - 成功")
    void testHot_Success() {
        when(service.getHotResources()).thenReturn(Arrays.asList(testResource));

        R<List<Resource>> result = controller.hot();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("搜索 - 成功")
    void testSearch_Success() {
        when(service.searchPaged(eq("侗医药"), anyInt(), anyInt())).thenReturn(testPage);

        R<Map<String, Object>> result = controller.search("侗医药", 1, 12);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("详情 - 成功")
    void testGetById_Success() {
        when(service.getById(1)).thenReturn(testResource);

        R<Resource> result = controller.getById(1);

        assertEquals(200, result.getCode());
        assertEquals("侗医药教材", result.getData().getTitle());
    }

    @Test
    @DisplayName("详情 - 不存在")
    void testGetById_NotFound() {
        when(service.getById(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> controller.getById(999));
    }

    @Test
    @DisplayName("增加浏览量 - 成功")
    void testIncrementView_Success() {
        R<String> result = controller.incrementView(1);

        assertEquals(200, result.getCode());
        verify(popularityAsyncService).incrementResourceViewAndPopularity(1);
    }

    @Test
    @DisplayName("增加下载量 - 成功")
    void testIncrementDownload_Success() {
        doNothing().when(service).incrementDownloadCount(1);

        R<String> result = controller.incrementDownload(1);

        assertEquals(200, result.getCode());
        verify(service).incrementDownloadCount(1);
    }

    @Test
    @DisplayName("文件类型列表 - 成功")
    void testGetFileTypes_Success() {
        R<List<ResourceController.TypeInfo>> result = controller.getFileTypes();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("分类列表 - 成功")
    void testGetCategories_Success() {
        when(service.getAllCategories()).thenReturn(Arrays.asList("教材", "视频"));

        R<List<String>> result = controller.getCategories();

        assertEquals(200, result.getCode());
    }
}
```

### Step 2: Create ChatControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AI聊天Controller测试")
class ChatControllerTest {

    @InjectMocks
    private ChatController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("获取聊天统计 - 成功")
    void testStats_Success() {
        R<ChatController.ChatStats> result = controller.stats();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("记录请求 - 成功")
    void testRecordRequest_Success() {
        assertDoesNotThrow(() -> ChatController.recordRequest(true));
        assertDoesNotThrow(() -> ChatController.recordRequest(false));
    }
}
```

### Step 3: Run tests

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="ResourceControllerTest,ChatControllerTest" -pl .`

Expected: All tests PASS.

### Step 4: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/controller/ResourceControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/ChatControllerTest.java
git commit -m "test: add Resource and Chat controller unit tests"
```

---

## Task 3: Interaction Controllers (Comment, Favorite)

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/CommentControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/FavoriteControllerTest.java`

### Step 1: Create CommentControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.CommentAddDTO;
import com.dongmedicine.service.CommentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("评论Controller测试")
class CommentControllerTest {

    @Mock
    private CommentService service;

    @InjectMocks
    private CommentController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("添加评论 - 需要登录")
    void testAdd_RequiresLogin() {
        CommentAddDTO dto = new CommentAddDTO();
        dto.setTargetType("plant");
        dto.setTargetId(1);
        dto.setContent("很好的内容");

        R<String> result = controller.add(dto);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("添加评论 - 登录用户成功")
    void testAdd_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        doNothing().when(service).addComment(any());

        CommentAddDTO dto = new CommentAddDTO();
        dto.setTargetType("plant");
        dto.setTargetId(1);
        dto.setContent("很好的内容");

        R<String> result = controller.add(dto);

        assertEquals(200, result.getCode());
        verify(service).addComment(any());
    }

    @Test
    @DisplayName("列表查询 - 成功")
    void testList_Success() {
        Page<?> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(service.getCommentsByTarget(eq("plant"), eq(1), anyInt(), anyInt())).thenReturn(page);

        R<Map<String, Object>> result = controller.list("plant", 1, 1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("全部评论列表 - 成功")
    void testListAll_Success() {
        Page<?> page = new Page<>(1, 12, 0);
        page.setRecords(Collections.emptyList());
        when(service.getAllComments(anyInt(), anyInt())).thenReturn(page);

        R<Map<String, Object>> result = controller.listAll(1, 12);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("我的评论 - 需要登录")
    void testMyComments_RequiresLogin() {
        R<Map<String, Object>> result = controller.myComments(1, 20);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("我的评论 - 登录用户成功")
    void testMyComments_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

        Page<?> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(service.getMyComments(eq(1), anyInt(), anyInt())).thenReturn(page);

        R<Map<String, Object>> result = controller.myComments(1, 20);

        assertEquals(200, result.getCode());
    }
}
```

### Step 2: Create FavoriteControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.FavoriteService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("收藏Controller测试")
class FavoriteControllerTest {

    @Mock
    private FavoriteService service;

    @InjectMocks
    private FavoriteController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("添加收藏 - 需要登录")
    void testAdd_RequiresLogin() {
        R<String> result = controller.add("plant", 1);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("添加收藏 - 成功")
    void testAdd_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(service.addFavorite(1, "plant", 1)).thenReturn(true);

        R<String> result = controller.add("plant", 1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("取消收藏 - 需要登录")
    void testRemove_RequiresLogin() {
        R<String> result = controller.remove("plant", 1);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("取消收藏 - 成功")
    void testRemove_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(service.removeFavorite(1, "plant", 1)).thenReturn(true);

        R<String> result = controller.remove("plant", 1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("我的收藏 - 需要登录")
    void testMyFavorites_RequiresLogin() {
        R<Map<String, Object>> result = controller.myFavorites(1, 20);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("我的收藏 - 成功")
    void testMyFavorites_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

        Page<?> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(service.getMyFavorites(eq(1), anyInt(), anyInt())).thenReturn(page);

        R<Map<String, Object>> result = controller.myFavorites(1, 20);

        assertEquals(200, result.getCode());
    }
}
```

### Step 3: Run tests

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="CommentControllerTest,FavoriteControllerTest" -pl .`

### Step 4: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/controller/CommentControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/FavoriteControllerTest.java
git commit -m "test: add Comment and Favorite controller unit tests"
```

---

## Task 4: User-Related Controllers (User, Captcha, ChatHistory, BrowseHistory)

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/UserControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/CaptchaControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/ChatHistoryControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/BrowseHistoryControllerTest.java`

### Step 1: Create UserControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.CaptchaDTO;
import com.dongmedicine.dto.ChangePasswordDTO;
import com.dongmedicine.dto.LoginDTO;
import com.dongmedicine.dto.RegisterDTO;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.CaptchaService;
import com.dongmedicine.service.UserService;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户Controller测试")
class UserControllerTest {

    @Mock
    private UserService service;
    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private UserController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
        stpUtilMock = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stpUtilMock.close();
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("登录 - 成功")
    void testLogin_Success() {
        when(captchaService.verify(anyString(), anyString())).thenReturn(true);

        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "test-token");
        loginResult.put("user", new User());
        when(service.login(any(LoginDTO.class))).thenReturn(loginResult);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setCaptchaKey("key");
        dto.setCaptchaCode("code");

        R<Map<String, Object>> result = controller.login(dto);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("登录 - 验证码错误")
    void testLogin_InvalidCaptcha() {
        when(captchaService.verify(anyString(), anyString())).thenReturn(false);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setCaptchaKey("key");
        dto.setCaptchaCode("wrong");

        R<Map<String, Object>> result = controller.login(dto);

        assertEquals(400, result.getCode());
    }

    @Test
    @DisplayName("注册 - 成功")
    void testRegister_Success() {
        when(captchaService.verify(anyString(), anyString())).thenReturn(true);
        doNothing().when(service).register(any(RegisterDTO.class));

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");
        dto.setCaptchaKey("key");
        dto.setCaptchaCode("code");

        R<String> result = controller.register(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("获取当前用户 - 需要登录")
    void testMe_RequiresLogin() {
        R<User> result = controller.me();

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("获取当前用户 - 成功")
    void testMe_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        when(service.getById(1)).thenReturn(user);

        R<User> result = controller.me();

        assertEquals(200, result.getCode());
        assertEquals("testuser", result.getData().getUsername());
    }

    @Test
    @DisplayName("修改密码 - 需要登录")
    void testChangePassword_RequiresLogin() {
        ChangePasswordDTO dto = new ChangePasswordDTO();

        R<String> result = controller.changePassword(dto);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("登出 - 成功")
    void testLogout_Success() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::logout).then(invocation -> null);

        R<String> result = controller.logout();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("验证token - 未登录")
    void testValidate_NotLoggedIn() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(false);

        R<Map<String, Object>> result = controller.validate();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("刷新token - 成功")
    void testRefreshToken_Success() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getTokenInfo).thenReturn(null);

        R<Map<String, Object>> result = controller.refreshToken();

        assertEquals(200, result.getCode());
    }
}
```

### Step 2: Create CaptchaControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.CaptchaDTO;
import com.dongmedicine.service.CaptchaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("验证码Controller测试")
class CaptchaControllerTest {

    @Mock
    private CaptchaService service;

    @InjectMocks
    private CaptchaController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("获取验证码 - 成功")
    void testGetCaptcha_Success() {
        CaptchaDTO captcha = new CaptchaDTO();
        captcha.setCaptchaKey("test-key");
        captcha.setCaptchaImage("data:image/png;base64,abc123");
        when(service.generateCaptcha()).thenReturn(captcha);

        R<CaptchaDTO> result = controller.getCaptcha();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getCaptchaKey());
        assertNotNull(result.getData().getCaptchaImage());
    }
}
```

### Step 3: Create ChatHistoryControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.ChatHistory;
import com.dongmedicine.service.ChatHistoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("聊天历史Controller测试")
class ChatHistoryControllerTest {

    @Mock
    private ChatHistoryService chatHistoryService;

    @InjectMocks
    private ChatHistoryController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("会话列表 - 需要登录")
    void testListSessions_RequiresLogin() {
        R<List<Map<String, Object>>> result = controller.listSessions();

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("会话列表 - 成功")
    void testListSessions_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(chatHistoryService.getUserSessions(1)).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.listSessions();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("会话消息 - 需要登录")
    void testGetSessionMessages_RequiresLogin() {
        R<List<ChatHistory>> result = controller.getSessionMessages("session-1");

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("会话消息 - 成功")
    void testGetSessionMessages_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(chatHistoryService.getSessionHistory(1, "session-1")).thenReturn(new ArrayList<>());

        R<List<ChatHistory>> result = controller.getSessionMessages("session-1");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除会话 - 需要登录")
    void testDeleteSession_RequiresLogin() {
        R<String> result = controller.deleteSession("session-1");

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("删除会话 - 成功")
    void testDeleteSession_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        doNothing().when(chatHistoryService).deleteSession(1, "session-1");

        R<String> result = controller.deleteSession("session-1");

        assertEquals(200, result.getCode());
        verify(chatHistoryService).deleteSession(1, "session-1");
    }
}
```

### Step 4: Create BrowseHistoryControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.service.BrowseHistoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("浏览历史Controller测试")
class BrowseHistoryControllerTest {

    @Mock
    private BrowseHistoryService browseHistoryService;

    @InjectMocks
    private BrowseHistoryController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("我的浏览历史 - 需要登录")
    void testMyHistory_RequiresLogin() {
        R<List<Map<String, Object>>> result = controller.myHistory(50);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("我的浏览历史 - 成功")
    void testMyHistory_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(browseHistoryService.getMyHistory(eq(1), eq(50))).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.myHistory(50);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("记录浏览 - 需要登录")
    void testRecord_RequiresLogin() {
        R<String> result = controller.record("plant", 1);

        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("记录浏览 - 成功")
    void testRecord_Success() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        doNothing().when(browseHistoryService).record(eq(1), eq("plant"), eq(1));

        R<String> result = controller.record("plant", 1);

        assertEquals(200, result.getCode());
        verify(browseHistoryService).record(1, "plant", 1);
    }
}
```

### Step 5: Run tests

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="UserControllerTest,CaptchaControllerTest,ChatHistoryControllerTest,BrowseHistoryControllerTest" -pl .`

### Step 6: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/controller/UserControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/CaptchaControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/ChatHistoryControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/BrowseHistoryControllerTest.java
git commit -m "test: add User, Captcha, ChatHistory, BrowseHistory controller unit tests"
```

---

## Task 5: Common Function Controllers (Search, Leaderboard, Metadata, Statistics, Notification)

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/SearchControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/LeaderboardControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/MetadataControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/StatisticsControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/NotificationControllerTest.java`

### Step 1: Create SearchControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.mapper.SearchHistoryMapper;
import com.dongmedicine.service.InheritorService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PlantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("搜索Controller测试")
class SearchControllerTest {

    @Mock
    private PlantService plantService;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private InheritorService inheritorService;
    @Mock
    private SearchHistoryMapper searchHistoryMapper;

    @InjectMocks
    private SearchController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("搜索建议 - 成功")
    void testSuggest_Success() {
        when(plantService.suggest(anyString(), anyInt())).thenReturn(new ArrayList<>());
        when(knowledgeService.suggest(anyString(), anyInt())).thenReturn(new ArrayList<>());
        when(inheritorService.suggest(anyString(), anyInt())).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.suggest("侗");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("搜索建议 - 空关键词")
    void testSuggest_EmptyKeyword() {
        R<List<Map<String, Object>>> result = controller.suggest("");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("热门搜索 - 成功")
    void testHotKeywords_Success() {
        when(searchHistoryMapper.selectHotKeywords(anyInt())).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.hotKeywords(20);

        assertEquals(200, result.getCode());
    }
}
```

### Step 2: Create LeaderboardControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.mapper.QuizRecordMapper;
import com.dongmedicine.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("排行榜Controller测试")
class LeaderboardControllerTest {

    @Mock
    private QuizRecordMapper quizRecordMapper;
    @Mock
    private PlantGameRecordMapper plantGameRecordMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LeaderboardController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("综合排行榜 - 成功")
    void testCombinedLeaderboard_Success() {
        when(quizRecordMapper.selectLeaderboard(anyInt())).thenReturn(new ArrayList<>());
        when(plantGameRecordMapper.selectLeaderboard(anyInt())).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.getCombinedLeaderboard("total", 100);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("答题排行榜 - 成功")
    void testQuizLeaderboard_Success() {
        when(quizRecordMapper.selectLeaderboard(anyInt())).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.getQuizLeaderboard(100);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("游戏排行榜 - 成功")
    void testGameLeaderboard_Success() {
        when(plantGameRecordMapper.selectLeaderboard(anyInt())).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.getGameLeaderboard(100);

        assertEquals(200, result.getCode());
    }
}
```

### Step 3: Create MetadataControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("元数据Controller测试")
class MetadataControllerTest {

    @Mock
    private PlantService plantService;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private QaService qaService;
    @Mock
    private InheritorService inheritorService;
    @Mock
    private ResourceService resourceService;
    @Mock
    private PlantMapper plantMapper;
    @Mock
    private KnowledgeMapper knowledgeMapper;
    @Mock
    private InheritorMapper inheritorMapper;
    @Mock
    private MetadataController self;

    @InjectMocks
    private MetadataController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("筛选条件 - 成功")
    void testGetAllFilters_Success() {
        when(self.getAllFiltersData()).thenReturn(new HashMap<>());

        R<Map<String, Object>> result = controller.getAllFilters();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("精选内容 - 成功")
    void testGetFeatured_Success() {
        when(self.getFeaturedData()).thenReturn(new HashMap<>());

        R<Map<String, Object>> result = controller.getFeatured();

        assertEquals(200, result.getCode());
    }
}
```

### Step 4: Create StatisticsControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.mapper.*;
import com.dongmedicine.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("统计Controller测试")
class StatisticsControllerTest {

    @Mock
    private OperationLogService logService;
    @Mock
    private PlantService plantService;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private QaService qaService;
    @Mock
    private InheritorService inheritorService;
    @Mock
    private ResourceService resourceService;
    @Mock
    private PlantMapper plantMapper;
    @Mock
    private KnowledgeMapper knowledgeMapper;
    @Mock
    private InheritorMapper inheritorMapper;
    @Mock
    private QaMapper qaMapper;
    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private StatisticsController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("平台概览 - 成功")
    void testGetOverview_Success() {
        when(plantService.count()).thenReturn(65L);
        when(knowledgeService.count()).thenReturn(30L);
        when(inheritorService.count()).thenReturn(10L);
        when(qaService.count()).thenReturn(50L);

        R<Map<String, Object>> result = controller.getOverview();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("图表数据 - 成功")
    void testGetChartData_Success() {
        when(plantMapper.selectCategoryStats()).thenReturn(new ArrayList<>());
        when(plantMapper.selectDistributionStats()).thenReturn(new ArrayList<>());
        when(inheritorMapper.selectLevelStats()).thenReturn(new ArrayList<>());
        when(knowledgeMapper.selectTherapyCategoryStats()).thenReturn(new ArrayList<>());
        when(qaMapper.selectCategoryStats()).thenReturn(new ArrayList<>());
        when(plantMapper.selectPopularityTop(anyInt())).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.getChartData();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("访问趋势 - 成功")
    void testGetVisitTrend_Success() {
        when(logService.getTrendLast7Days()).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.getVisitTrend();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("植物统计 - 成功")
    void testPlantStats_Success() {
        when(plantMapper.selectCategoryStats()).thenReturn(new ArrayList<>());
        when(plantMapper.selectUsageWayStats()).thenReturn(new ArrayList<>());
        when(plantMapper.selectDistributionStats()).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.plantStats();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("知识统计 - 成功")
    void testKnowledgeStats_Success() {
        when(knowledgeMapper.selectTherapyCategoryStats()).thenReturn(new ArrayList<>());
        when(knowledgeMapper.selectDiseaseCategoryStats()).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.knowledgeStats();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("问答统计 - 成功")
    void testQaStats_Success() {
        when(qaMapper.selectCategoryStats()).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.qaStats();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("传承人统计 - 成功")
    void testInheritorStats_Success() {
        when(inheritorMapper.selectLevelStats()).thenReturn(new ArrayList<>());
        when(inheritorMapper.selectSpecialtyStats()).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.inheritorStats();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("资源统计 - 成功")
    void testResourceStats_Success() {
        when(resourceMapper.selectCategoryStats()).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.resourceStats();

        assertEquals(200, result.getCode());
    }
}
```

### Step 5: Create NotificationControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.HashOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("通知Controller测试")
class NotificationControllerTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;
    @Mock
    private SetOperations<String, String> setOps;
    @Mock
    private HashOperations<String, String, String> hashOps;

    @InjectMocks
    private NotificationController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("通知列表 - 未登录返回空")
    void testList_NotLoggedIn() {
        R<List<String>> result = controller.list();

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("通知列表 - 登录用户")
    void testList_LoggedIn() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries(anyString())).thenReturn(new HashMap<>());

        R<List<String>> result = controller.list();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("未读数量 - 未登录返回0")
    void testUnreadCount_NotLoggedIn() {
        R<Map<String, Object>> result = controller.unreadCount();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("标记全部已读 - 未登录")
    void testMarkAllRead_NotLoggedIn() {
        R<String> result = controller.markAllRead();

        assertEquals(200, result.getCode());
    }
}
```

### Step 6: Run tests

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="SearchControllerTest,LeaderboardControllerTest,MetadataControllerTest,StatisticsControllerTest,NotificationControllerTest" -pl .`

### Step 7: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/controller/SearchControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/LeaderboardControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/MetadataControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/StatisticsControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/NotificationControllerTest.java
git commit -m "test: add Search, Leaderboard, Metadata, Statistics, Notification controller tests"
```

---

## Task 6: Admin Controllers (AdminContent, AdminUser, AdminInteraction, AdminStats)

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/admin/AdminContentControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/admin/AdminUserControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/admin/AdminInteractionControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/admin/AdminStatsControllerTest.java`

### Step 1: Create AdminContentControllerTest.java

```java
package com.dongmedicine.controller.admin;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.*;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.entity.Resource;
import com.dongmedicine.service.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台内容管理Controller测试")
class AdminContentControllerTest {

    @Mock
    private InheritorService inheritorService;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private PlantService plantService;
    @Mock
    private QaService qaService;
    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private AdminContentController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    // --- Inheritor CRUD ---

    @Test
    @DisplayName("传承人列表 - 成功")
    void testListInheritors() {
        Page<Inheritor> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(inheritorService.page(any())).thenReturn(page);

        R<Map<String, Object>> result = controller.listInheritors(1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建传承人 - 成功")
    void testCreateInheritor() {
        InheritorCreateDTO dto = new InheritorCreateDTO();
        dto.setName("新传承人");
        when(inheritorService.save(any(Inheritor.class))).thenReturn(true);

        R<String> result = controller.createInheritor(dto);

        assertEquals(200, result.getCode());
        verify(inheritorService).save(any(Inheritor.class));
    }

    @Test
    @DisplayName("更新传承人 - 成功")
    void testUpdateInheritor() {
        InheritorUpdateDTO dto = new InheritorUpdateDTO();
        dto.setName("更新名称");
        when(inheritorService.updateWithFiles(anyInt(), any(InheritorUpdateDTO.class))).thenReturn(true);

        R<String> result = controller.updateInheritor(1, dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除传承人 - 成功")
    void testDeleteInheritor() {
        doNothing().when(inheritorService).deleteWithFiles(1);

        R<String> result = controller.deleteInheritor(1);

        assertEquals(200, result.getCode());
        verify(inheritorService).deleteWithFiles(1);
    }

    // --- Knowledge CRUD ---

    @Test
    @DisplayName("知识列表 - 成功")
    void testListKnowledge() {
        Page<Knowledge> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(knowledgeService.page(any())).thenReturn(page);

        R<Map<String, Object>> result = controller.listKnowledge(1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建知识 - 成功")
    void testCreateKnowledge() {
        KnowledgeCreateDTO dto = new KnowledgeCreateDTO();
        dto.setTitle("新知识");
        dto.setContent("内容");
        when(knowledgeService.save(any(Knowledge.class))).thenReturn(true);

        R<String> result = controller.createKnowledge(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新知识 - 成功")
    void testUpdateKnowledge() {
        KnowledgeUpdateDTO dto = new KnowledgeUpdateDTO();
        dto.setTitle("更新标题");
        when(knowledgeService.updateWithFiles(anyInt(), any(KnowledgeUpdateDTO.class))).thenReturn(true);

        R<String> result = controller.updateKnowledge(1, dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除知识 - 成功")
    void testDeleteKnowledge() {
        doNothing().when(knowledgeService).deleteWithFiles(1);

        R<String> result = controller.deleteKnowledge(1);

        assertEquals(200, result.getCode());
    }

    // --- Plant CRUD ---

    @Test
    @DisplayName("植物列表 - 成功")
    void testListPlants() {
        Page<Plant> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(plantService.page(any())).thenReturn(page);

        R<Map<String, Object>> result = controller.listPlants(1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建植物 - 成功")
    void testCreatePlant() {
        PlantCreateDTO dto = new PlantCreateDTO();
        dto.setNameCn("新植物");
        when(plantService.save(any(Plant.class))).thenReturn(true);

        R<String> result = controller.createPlant(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新植物 - 成功")
    void testUpdatePlant() {
        PlantUpdateDTO dto = new PlantUpdateDTO();
        dto.setNameCn("更新名称");
        when(plantService.updateWithFiles(anyInt(), any(PlantUpdateDTO.class))).thenReturn(true);

        R<String> result = controller.updatePlant(1, dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除植物 - 成功")
    void testDeletePlant() {
        doNothing().when(plantService).deleteWithFiles(1);

        R<String> result = controller.deletePlant(1);

        assertEquals(200, result.getCode());
    }

    // --- QA CRUD ---

    @Test
    @DisplayName("问答列表 - 成功")
    void testListQa() {
        Page<Qa> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(qaService.page(any())).thenReturn(page);

        R<Map<String, Object>> result = controller.listQa(1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建问答 - 成功")
    void testCreateQa() {
        QaCreateDTO dto = new QaCreateDTO();
        dto.setQuestion("新问题");
        dto.setAnswer("答案");
        when(qaService.save(any(Qa.class))).thenReturn(true);

        R<String> result = controller.createQa(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新问答 - 成功")
    void testUpdateQa() {
        QaUpdateDTO dto = new QaUpdateDTO();
        dto.setQuestion("更新问题");
        when(qaService.updateById(any(Qa.class))).thenReturn(true);

        R<String> result = controller.updateQa(1, dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除问答 - 成功")
    void testDeleteQa() {
        when(qaService.removeById(1)).thenReturn(true);

        R<String> result = controller.deleteQa(1);

        assertEquals(200, result.getCode());
    }

    // --- Resource CRUD ---

    @Test
    @DisplayName("资源列表 - 成功")
    void testListResources() {
        Page<Resource> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(resourceService.page(any())).thenReturn(page);

        R<Map<String, Object>> result = controller.listResources(1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建资源 - 成功")
    void testCreateResource() {
        ResourceCreateDTO dto = new ResourceCreateDTO();
        dto.setTitle("新资源");
        when(resourceService.save(any(Resource.class))).thenReturn(true);

        R<String> result = controller.createResource(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新资源 - 成功")
    void testUpdateResource() {
        ResourceUpdateDTO dto = new ResourceUpdateDTO();
        dto.setTitle("更新资源");
        when(resourceService.updateWithFiles(anyInt(), any(ResourceUpdateDTO.class))).thenReturn(true);

        R<String> result = controller.updateResource(1, dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除资源 - 成功")
    void testDeleteResource() {
        doNothing().when(resourceService).deleteWithFiles(1);

        R<String> result = controller.deleteResource(1);

        assertEquals(200, result.getCode());
    }
}
```

### Step 2: Create AdminUserControllerTest.java

```java
package com.dongmedicine.controller.admin;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台用户管理Controller测试")
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("用户列表 - 成功")
    void testListUsers() {
        Page<User> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(userService.page(any())).thenReturn(page);

        R<Map<String, Object>> result = controller.listUsers(1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void testDeleteUser() {
        when(userService.removeById(2)).thenReturn(true);

        R<String> result = controller.deleteUser(2);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新角色 - 成功")
    void testUpdateUserRole() {
        when(userService.updateRole(2, "admin")).thenReturn(true);

        R<String> result = controller.updateUserRole(2, "admin");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("封禁用户 - 成功")
    void testBanUser() {
        when(userService.banUser(eq(2), anyString())).thenReturn(true);

        R<String> result = controller.banUser(2, "违规操作");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("解封用户 - 成功")
    void testUnbanUser() {
        when(userService.unbanUser(2)).thenReturn(true);

        R<String> result = controller.unbanUser(2);

        assertEquals(200, result.getCode());
    }
}
```

### Step 3: Create AdminInteractionControllerTest.java

```java
package com.dongmedicine.controller.admin;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.dto.FeedbackReplyDTO;
import com.dongmedicine.service.CommentService;
import com.dongmedicine.service.FeedbackService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台互动管理Controller测试")
class AdminInteractionControllerTest {

    @Mock
    private FeedbackService feedbackService;
    @Mock
    private CommentService commentService;

    @InjectMocks
    private AdminInteractionController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("反馈列表 - 成功")
    void testListFeedback() {
        Page<?> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(feedbackService.listByStatus(anyString(), anyInt(), anyInt())).thenReturn(page);

        R<Map<String, Object>> result = controller.listFeedback("all", 1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("回复反馈 - 成功")
    void testReplyFeedback() {
        FeedbackReplyDTO dto = new FeedbackReplyDTO();
        dto.setReply("已处理");
        doNothing().when(feedbackService).replyFeedback(eq(1), anyString());

        R<String> result = controller.replyFeedback(1, dto);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除反馈 - 成功")
    void testDeleteFeedback() {
        when(feedbackService.removeById(1)).thenReturn(true);

        R<String> result = controller.deleteFeedback(1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("评论列表 - 成功")
    void testListComments() {
        Page<?> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(commentService.listByStatus(anyString(), anyInt(), anyInt())).thenReturn(page);

        R<Map<String, Object>> result = controller.listComments("all", 1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("审批评论 - 成功")
    void testApproveComment() {
        doNothing().when(commentService).approveComment(1);

        R<String> result = controller.approveComment(1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("拒绝评论 - 成功")
    void testRejectComment() {
        doNothing().when(commentService).rejectComment(1);

        R<String> result = controller.rejectComment(1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除评论 - 成功")
    void testDeleteComment() {
        when(commentService.removeById(1)).thenReturn(true);

        R<String> result = controller.deleteComment(1);

        assertEquals(200, result.getCode());
    }
}
```

### Step 4: Create AdminStatsControllerTest.java

```java
package com.dongmedicine.controller.admin;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.mapper.SearchHistoryMapper;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台统计Controller测试")
class AdminStatsControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private InheritorService inheritorService;
    @Mock
    private PlantService plantService;
    @Mock
    private QaService qaService;
    @Mock
    private ResourceService resourceService;
    @Mock
    private QuizService quizService;
    @Mock
    private CommentService commentService;
    @Mock
    private FeedbackService feedbackService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SearchHistoryMapper searchHistoryMapper;

    @InjectMocks
    private AdminStatsController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("管理统计 - 成功")
    void testStats() {
        when(userService.count()).thenReturn(100L);
        when(knowledgeService.count()).thenReturn(30L);
        when(inheritorService.count()).thenReturn(10L);
        when(plantService.count()).thenReturn(65L);
        when(qaService.count()).thenReturn(50L);
        when(resourceService.count()).thenReturn(20L);
        when(quizService.count()).thenReturn(200L);
        when(commentService.count()).thenReturn(500L);
        when(feedbackService.count()).thenReturn(50L);

        R<Map<String, Long>> result = controller.stats();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("用户增长 - 成功")
    void testGetUserGrowth() {
        when(userMapper.selectUserGrowthLast7Days()).thenReturn(new ArrayList<>());

        R<Map<String, Object>> result = controller.getUserGrowth();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("内容浏览量 - 成功")
    void testGetContentViews() {
        when(plantMapper.selectContentViews(anyInt())).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.getContentViews();

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("搜索关键词 - 成功")
    void testGetSearchKeywords() {
        when(searchHistoryMapper.selectHotKeywords(anyInt())).thenReturn(new ArrayList<>());

        R<List<Map<String, Object>>> result = controller.getSearchKeywords();

        assertEquals(200, result.getCode());
    }
}
```

**Note:** The `AdminStatsControllerTest` references `plantMapper` which is not a field of `AdminStatsController`. Adjust the mock injection based on the actual controller implementation. If the controller uses service methods instead of direct mapper calls, mock the services accordingly.

### Step 5: Run tests

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="AdminContentControllerTest,AdminUserControllerTest,AdminInteractionControllerTest,AdminStatsControllerTest" -pl .`

### Step 6: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/controller/admin/
git commit -m "test: add AdminContent, AdminUser, AdminInteraction, AdminStats controller tests"
```

---

## Task 7: Utility Controllers (FileUpload, Export, OperationLog)

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/FileUploadControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/ExportControllerTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/controller/OperationLogControllerTest.java`

### Step 1: Create FileUploadControllerTest.java

```java
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
    private FileUploadController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

        testFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("上传图片 - 成功")
    void testUploadImage_Success() {
        FileUploadResult uploadResult = FileUploadResult.success("/images/test.jpg", "test.jpg", 1024);
        when(fileUploadService.uploadImage(any(), eq("common"))).thenReturn(uploadResult);

        R<FileUploadResult> result = controller.uploadImage(testFile, "common");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("批量上传图片 - 成功")
    void testUploadImages_Success() {
        FileUploadResult uploadResult = FileUploadResult.success("/images/test.jpg", "test.jpg", 1024);
        when(fileUploadService.uploadImages(anyList(), eq("common"))).thenReturn(Arrays.asList(uploadResult));

        R<List<FileUploadResult>> result = controller.uploadImages(Arrays.asList(testFile), "common");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("上传视频 - 成功")
    void testUploadVideo_Success() {
        FileUploadResult uploadResult = FileUploadResult.success("/videos/test.mp4", "test.mp4", 10240);
        when(fileUploadService.uploadVideo(any(), eq("common"))).thenReturn(uploadResult);

        MockMultipartFile videoFile = new MockMultipartFile("file", "test.mp4", "video/mp4", "data".getBytes());
        R<FileUploadResult> result = controller.uploadVideo(videoFile, "common");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("上传文档 - 成功")
    void testUploadDocument_Success() {
        FileUploadResult uploadResult = FileUploadResult.success("/documents/test.pdf", "test.pdf", 2048);
        when(fileUploadService.uploadDocument(any(), eq("common"))).thenReturn(uploadResult);

        MockMultipartFile docFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        R<FileUploadResult> result = controller.uploadDocument(docFile, "common");

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除文件 - 成功")
    void testDeleteFile_Success() {
        when(fileUploadService.deleteFile("/images/test.jpg")).thenReturn(true);

        R<Boolean> result = controller.deleteFile("/images/test.jpg");

        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }
}
```

### Step 2: Create ExportControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.service.*;
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
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("导出Controller测试")
class ExportControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private PlantService plantService;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private InheritorService inheritorService;
    @Mock
    private ResourceService resourceService;
    @Mock
    private QaService qaService;
    @Mock
    private CommentService commentService;
    @Mock
    private FeedbackService feedbackService;
    @Mock
    private QuizService quizService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExportController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("导出植物数据 - 成功")
    void testExportPlants_Success() throws Exception {
        Plant plant = new Plant();
        plant.setId(1);
        plant.setNameCn("钩藤");
        when(plantService.list(any())).thenReturn(Arrays.asList(plant));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.export("plants", "csv", response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().contains("csv"));
    }

    @Test
    @DisplayName("导出未知实体 - 返回错误")
    void testExportUnknownEntity() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.export("unknown", "csv", response);

        assertEquals(400, response.getStatus());
    }
}
```

### Step 3: Create OperationLogControllerTest.java

```java
package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.service.OperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志Controller测试")
class OperationLogControllerTest {

    @Mock
    private OperationLogService logService;

    @InjectMocks
    private OperationLogController controller;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("日志列表 - 成功")
    void testList_Success() {
        when(logService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        R<List<OperationLog>> result = controller.list(null, null, null, 100);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("日志列表 - 带筛选条件")
    void testList_WithFilters() {
        when(logService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        R<List<OperationLog>> result = controller.list("用户管理", "CREATE", "admin", 50);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("日志详情 - 成功")
    void testGetById_Success() {
        OperationLog log = new OperationLog();
        log.setId(1);
        log.setModule("用户管理");
        when(logService.getById(1)).thenReturn(log);

        R<OperationLog> result = controller.getById(1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除日志 - 成功")
    void testDelete_Success() {
        when(logService.removeById(1)).thenReturn(true);

        R<String> result = controller.delete(1);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("批量删除日志 - 成功")
    void testBatchDelete_Success() {
        when(logService.removeByIds(anyList())).thenReturn(true);

        R<String> result = controller.batchDelete(new Integer[]{1, 2, 3});

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("清空日志 - 成功")
    void testClearAll_Success() {
        doNothing().when(logService).clearAll();

        R<String> result = controller.clearAll();

        assertEquals(200, result.getCode());
        verify(logService).clearAll();
    }

    @Test
    @DisplayName("日志统计 - 成功")
    void testStats_Success() {
        when(logService.count()).thenReturn(100L);

        R<Map<String, Object>> result = controller.stats();

        assertEquals(200, result.getCode());
    }
}
```

### Step 4: Run tests

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="FileUploadControllerTest,ExportControllerTest,OperationLogControllerTest" -pl .`

### Step 5: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/controller/FileUploadControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/ExportControllerTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/controller/OperationLogControllerTest.java
git commit -m "test: add FileUpload, Export, OperationLog controller unit tests"
```

---

## Task 8: Service Unit Tests (5 missing services)

**Files:**
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/ChatHistoryServiceImplTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/FileUploadServiceImplTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/OperationLogServiceImplTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/PopularityAsyncServiceImplTest.java`
- Create: `dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/RabbitMQOperationLogServiceImplTest.java`

### Step 1: Create PopularityAsyncServiceImplTest.java

```java
package com.dongmedicine.service.impl;

import com.dongmedicine.mapper.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("热度异步Service测试")
class PopularityAsyncServiceImplTest {

    @Mock
    private PlantMapper plantMapper;
    @Mock
    private KnowledgeMapper knowledgeMapper;
    @Mock
    private InheritorMapper inheritorMapper;
    @Mock
    private QaMapper qaMapper;
    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private PopularityAsyncServiceImpl service;

    @Test
    @DisplayName("增加植物浏览热度 - 成功")
    void testIncrementPlantView() {
        doNothing().when(plantMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> service.incrementPlantViewAndPopularity(1));
        verify(plantMapper).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加知识浏览热度 - 成功")
    void testIncrementKnowledgeView() {
        doNothing().when(knowledgeMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> service.incrementKnowledgeViewAndPopularity(1));
        verify(knowledgeMapper).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加传承人浏览热度 - 成功")
    void testIncrementInheritorView() {
        doNothing().when(inheritorMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> service.incrementInheritorViewAndPopularity(1));
        verify(inheritorMapper).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加问答浏览热度 - 成功")
    void testIncrementQaView() {
        doNothing().when(qaMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> service.incrementQaViewAndPopularity(1));
        verify(qaMapper).incrementViewCount3AndPopularity(1);
    }

    @Test
    @DisplayName("增加资源浏览热度 - 成功")
    void testIncrementResourceView() {
        doNothing().when(resourceMapper).incrementViewCount3AndPopularity(1);

        assertDoesNotThrow(() -> service.incrementResourceViewAndPopularity(1));
        verify(resourceMapper).incrementViewCount3AndPopularity(1);
    }
}
```

### Step 2: Create RabbitMQOperationLogServiceImplTest.java

```java
package com.dongmedicine.service.impl;

import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mq.producer.OperationLogProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMQ操作日志Service测试")
class RabbitMQOperationLogServiceImplTest {

    @Mock
    private OperationLogProducer operationLogProducer;

    @InjectMocks
    private RabbitMQOperationLogServiceImpl service;

    @Test
    @DisplayName("异步保存日志 - 成功")
    void testSaveLogAsync_Success() {
        OperationLog log = new OperationLog();
        log.setModule("测试");
        log.setOperation("测试操作");

        doNothing().when(operationLogProducer).sendOperationLog(any(OperationLog.class));

        assertDoesNotThrow(() -> service.saveLogAsync(log));
        verify(operationLogProducer).sendOperationLog(log);
    }

    @Test
    @DisplayName("异步保存日志 - 发送失败抛异常")
    void testSaveLogAsync_Failure() {
        OperationLog log = new OperationLog();
        log.setModule("测试");

        doThrow(new RuntimeException("MQ连接失败")).when(operationLogProducer).sendOperationLog(any(OperationLog.class));

        assertThrows(RuntimeException.class, () -> service.saveLogAsync(log));
    }
}
```

### Step 3: Create OperationLogServiceImplTest.java

This service extends `ServiceImpl<OperationLogMapper, OperationLog>` which requires setting the `baseMapper` field via reflection, following the existing pattern in `PlantServiceImplTest.java`.

```java
package com.dongmedicine.service.impl;

import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志Service测试")
class OperationLogServiceImplTest {

    @Mock
    private OperationLogMapper logMapper;

    @InjectMocks
    private OperationLogServiceImpl service;

    private void setBaseMapper(Object svc, Object mapper) throws Exception {
        Class<?> clazz = svc.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(svc, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        setBaseMapper(service, logMapper);
    }

    @Test
    @DisplayName("保存日志 - 成功")
    void testSave_Success() {
        OperationLog log = new OperationLog();
        log.setModule("测试");

        when(logMapper.insert(any(OperationLog.class))).thenReturn(1);

        boolean result = service.save(log);

        assertTrue(result);
    }

    @Test
    @DisplayName("清空所有日志 - 成功")
    void testClearAll_Success() {
        when(logMapper.delete(any(QueryWrapper.class))).thenReturn(10);

        assertDoesNotThrow(() -> service.clearAll());
    }

    @Test
    @DisplayName("获取7天趋势 - 成功")
    void testGetTrendLast7Days_Success() {
        when(logMapper.selectTrendLast7Days()).thenReturn(new ArrayList<>());

        List<Map<String, Object>> result = service.getTrendLast7Days();

        assertNotNull(result);
    }
}
```

### Step 4: Create FileUploadServiceImplTest.java

```java
package com.dongmedicine.service.impl;

import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.config.FileUploadProperties;
import com.dongmedicine.dto.FileUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文件上传Service测试")
class FileUploadServiceImplTest {

    @Mock
    private FileUploadProperties properties;

    @InjectMocks
    private FileUploadServiceImpl service;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("upload-test");
        when(properties.getBasePath()).thenReturn(tempDir.toString());
        when(properties.getAllowedImageExtensions()).thenReturn(new String[]{"jpg", "jpeg", "png", "gif", "bmp", "webp"});
        when(properties.getAllowedVideoExtensions()).thenReturn(new String[]{"mp4", "avi", "mov", "wmv", "flv", "mkv"});
        when(properties.getAllowedDocumentExtensions()).thenReturn(new String[]{"pdf", "docx", "doc", "xlsx", "xls", "pptx", "ppt", "txt"});
        when(properties.getMaxImageSize()).thenReturn(10L * 1024 * 1024);
        when(properties.getMaxVideoSize()).thenReturn(50L * 1024 * 1024);
        when(properties.getMaxDocumentSize()).thenReturn(50L * 1024 * 1024);
    }

    @Test
    @DisplayName("上传图片 - 成功")
    void testUploadImage_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake image data".getBytes());

        FileUploadResult result = service.uploadImage(file, "common");

        assertNotNull(result);
    }

    @Test
    @DisplayName("上传空文件 - 抛异常")
    void testUploadFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        assertThrows(BusinessException.class, () -> service.uploadImage(file, "common"));
    }

    @Test
    @DisplayName("上传null文件 - 抛异常")
    void testUploadFile_NullFile() {
        assertThrows(BusinessException.class, () -> service.uploadImage(null, "common"));
    }

    @Test
    @DisplayName("获取文件URL - 成功")
    void testGetFileUrl_Success() {
        String url = service.getFileUrl("/images/test.jpg");

        assertEquals("/images/test.jpg", url);
    }

    @Test
    @DisplayName("获取文件URL - 空路径")
    void testGetFileUrl_Blank() {
        String url = service.getFileUrl("");

        assertEquals("", url);
    }

    @Test
    @DisplayName("获取文件URL - null")
    void testGetFileUrl_Null() {
        String url = service.getFileUrl(null);

        assertEquals("", url);
    }

    @Test
    @DisplayName("删除文件 - 文件不存在")
    void testDeleteFile_NotExists() {
        boolean result = service.deleteFile("/images/nonexistent.jpg");

        assertFalse(result);
    }
}
```

### Step 5: Create ChatHistoryServiceImplTest.java

```java
package com.dongmedicine.service.impl;

import com.dongmedicine.entity.ChatHistory;
import com.dongmedicine.mapper.ChatHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("聊天历史Service测试")
class ChatHistoryServiceImplTest {

    @Mock
    private ChatHistoryMapper chatHistoryMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private HashOperations<String, String, String> hashOps;
    @Mock
    private SetOperations<String, String> setOps;

    @InjectMocks
    private ChatHistoryServiceImpl service;

    private void setBaseMapper(Object svc, Object mapper) throws Exception {
        Class<?> clazz = svc.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(svc, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        setBaseMapper(service, chatHistoryMapper);
    }

    @Test
    @DisplayName("保存消息到MySQL - 成功")
    void testSaveMessage_Success() {
        when(chatHistoryMapper.insert(any(ChatHistory.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.saveMessage(1, "session-1", "user", "你好"));
        verify(chatHistoryMapper).insert(any(ChatHistory.class));
    }

    @Test
    @DisplayName("保存消息到Redis - 成功")
    void testSaveMessageToRedis_Success() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        assertDoesNotThrow(() -> service.saveMessageToRedis(1, "session-1", "user", "你好"));
        verify(hashOps).put(eq("chat:session:1:session-1"), anyString(), anyString());
    }

    @Test
    @DisplayName("获取会话历史 - 从MySQL获取")
    void testGetSessionHistory_FromMysql() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries("chat:session:1:session-1")).thenReturn(new HashMap<>());
        when(chatHistoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        List<ChatHistory> result = service.getSessionHistory(1, "session-1");

        assertNotNull(result);
    }

    @Test
    @DisplayName("删除会话 - 成功")
    void testDeleteSession_Success() {
        when(chatHistoryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        assertDoesNotThrow(() -> service.deleteSession(1, "session-1"));
    }

    @Test
    @DisplayName("获取用户会话列表 - 成功")
    void testGetUserSessions_Success() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members("chat:active:1")).thenReturn(new HashSet<>());
        when(chatHistoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        List<Map<String, Object>> result = service.getUserSessions(1);

        assertNotNull(result);
    }
}
```

### Step 6: Run all service tests

Run: `cd dong-medicine-backend && ./mvnw test -B -Dtest="PopularityAsyncServiceImplTest,RabbitMQOperationLogServiceImplTest,OperationLogServiceImplTest,FileUploadServiceImplTest,ChatHistoryServiceImplTest" -pl .`

### Step 7: Commit

```bash
git add dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/PopularityAsyncServiceImplTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/RabbitMQOperationLogServiceImplTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/OperationLogServiceImplTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/FileUploadServiceImplTest.java \
        dong-medicine-backend/src/test/java/com/dongmedicine/service/impl/ChatHistoryServiceImplTest.java
git commit -m "test: add PopularityAsync, RabbitMQ, OperationLog, FileUpload, ChatHistory service tests"
```

---

## Task 9: Final Verification

### Step 1: Run the full test suite

Run: `cd dong-medicine-backend && ./mvnw test -B`

Expected: ALL tests pass (existing 42 test files + new 29 test files = ~71 total test files).

### Step 2: Check test count

Run: `cd dong-medicine-backend && ./mvnw test -B 2>&1 | grep -E "Tests run:|test"`

Expected: Significant increase in total test count (from ~200 to ~350+).

### Step 3: Verify no regressions

If any existing tests fail after adding new tests, investigate and fix:
- Check for mock conflicts (e.g., two test files mocking the same static class differently)
- Check for bean injection issues
- Check for test isolation problems

### Step 4: Final commit (if any fixes needed)

```bash
git add -A
git commit -m "test: fix regressions after adding comprehensive test suite"
```

---

## Known Risks and Mitigations

1. **Method signature mismatches:** The test code is written based on inferred controller signatures. If actual method names differ (e.g., `advancedSearchPaged` vs `searchPaged`), adjust the `when()` calls to match the actual service method names.

2. **MockedStatic conflicts:** If tests fail with "static mocking already active" errors, ensure each test class properly closes its `MockedStatic` instances in `@AfterEach`.

3. **BaseEntity reflection:** For services extending `ServiceImpl` (OperationLogServiceImpl, ChatHistoryServiceImpl), the `setBaseMapper` reflection pattern is needed. If the field name changes, update the reflection code.

4. **Admin controller auth:** Since we use `@InjectMocks` (not MockMvc), `@SaCheckRole("admin")` annotations are NOT enforced at runtime. Tests verify business logic, not AOP interceptors. This is by design.
