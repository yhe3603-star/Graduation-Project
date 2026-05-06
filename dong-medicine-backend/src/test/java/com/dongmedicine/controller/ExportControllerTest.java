package com.dongmedicine.controller;

import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.entity.QuizQuestion;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private ExportController exportController;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private MockHttpServletResponse response;

    private Plant testPlant;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(1);

        response = new MockHttpServletResponse();

        testPlant = new Plant();
        testPlant.setId(1);
        testPlant.setNameCn("钩藤");
        testPlant.setCategory("清热药");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("导出植物数据 - 成功")
    void testExportPlants_Success() throws IOException {
        List<Plant> plants = Arrays.asList(testPlant);
        when(plantService.list()).thenReturn(plants);

        exportController.export("plants", "csv", response);

        assertEquals(200, response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("钩藤"));
        assertTrue(content.contains("nameCn"));
        assertTrue(response.getHeader("Content-Disposition").contains("attachment"));
        assertTrue(response.getContentType().contains("text/csv"));
    }

    @Test
    @DisplayName("导出不支持的实体 - 返回400")
    void testExportUnknownEntity() throws IOException {
        exportController.export("unknown", "csv", response);

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("导出不支持的格式 - 返回400")
    void testExportUnsupportedFormat() throws IOException {
        exportController.export("plants", "json", response);

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("导出用户数据 - 成功")
    void testExportUsers_Success() throws IOException {
        com.dongmedicine.entity.User user = new com.dongmedicine.entity.User();
        user.setId(1);
        user.setUsername("admin");
        when(userService.list()).thenReturn(Arrays.asList(user));

        exportController.export("users", "csv", response);

        assertEquals(200, response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("admin"));
        // Verify password field is excluded
        assertFalse(content.contains("passwordHash"));
    }

    @Test
    @DisplayName("导出问答数据 - 成功")
    void testExportQa_Success() throws IOException {
        com.dongmedicine.entity.Qa qa = new com.dongmedicine.entity.Qa();
        qa.setId(1);
        qa.setQuestion("什么是侗药？");
        when(qaService.list()).thenReturn(Arrays.asList(qa));

        exportController.export("qa", "csv", response);

        assertEquals(200, response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("什么是侗药"));
    }

    @Test
    @DisplayName("导出测验题目 - 成功")
    void testExportQuizQuestions_Success() throws IOException {
        QuizQuestion question = new QuizQuestion();
        question.setId(1);
        question.setQuestion("侗药中常见的药材是？");
        when(quizService.getAllQuestions()).thenReturn(Arrays.asList(question));

        exportController.export("quiz-questions", "csv", response);

        assertEquals(200, response.getStatus());
        verify(quizService).getAllQuestions();
    }

    @Test
    @DisplayName("导出空列表 - 成功返回空CSV")
    void testExportEmptyList() throws IOException {
        when(plantService.list()).thenReturn(Collections.emptyList());

        exportController.export("plants", "csv", response);

        assertEquals(200, response.getStatus());
        // Empty list means no data rows written, but BOM is still present
        String content = response.getContentAsString();
        // Content may be empty or just have BOM
        assertNotNull(content);
    }

    @Test
    @DisplayName("导出知识数据 - 成功")
    void testExportKnowledge_Success() throws IOException {
        com.dongmedicine.entity.Knowledge knowledge = new com.dongmedicine.entity.Knowledge();
        knowledge.setId(1);
        knowledge.setTitle("侗药知识");
        when(knowledgeService.list()).thenReturn(Arrays.asList(knowledge));

        exportController.export("knowledge", "csv", response);

        assertEquals(200, response.getStatus());
        verify(knowledgeService).list();
    }

    @Test
    @DisplayName("导出传承人数据 - 成功")
    void testExportInheritors_Success() throws IOException {
        com.dongmedicine.entity.Inheritor inheritor = new com.dongmedicine.entity.Inheritor();
        inheritor.setId(1);
        inheritor.setName("传承人");
        when(inheritorService.list()).thenReturn(Arrays.asList(inheritor));

        exportController.export("inheritors", "csv", response);

        assertEquals(200, response.getStatus());
        verify(inheritorService).list();
    }

    @Test
    @DisplayName("导出资源数据 - 成功")
    void testExportResources_Success() throws IOException {
        com.dongmedicine.entity.Resource resource = new com.dongmedicine.entity.Resource();
        resource.setId(1);
        resource.setTitle("资源");
        when(resourceService.list()).thenReturn(Arrays.asList(resource));

        exportController.export("resources", "csv", response);

        assertEquals(200, response.getStatus());
        verify(resourceService).list();
    }

    @Test
    @DisplayName("导出评论数据 - 成功")
    void testExportComments_Success() throws IOException {
        com.dongmedicine.entity.Comment comment = new com.dongmedicine.entity.Comment();
        comment.setId(1);
        comment.setContent("好文章");
        when(commentService.list()).thenReturn(Arrays.asList(comment));

        exportController.export("comments", "csv", response);

        assertEquals(200, response.getStatus());
        verify(commentService).list();
    }

    @Test
    @DisplayName("导出反馈数据 - 成功")
    void testExportFeedback_Success() throws IOException {
        com.dongmedicine.entity.Feedback feedback = new com.dongmedicine.entity.Feedback();
        feedback.setId(1);
        feedback.setContent("建议");
        when(feedbackService.list()).thenReturn(Arrays.asList(feedback));

        exportController.export("feedback", "csv", response);

        assertEquals(200, response.getStatus());
        verify(feedbackService).list();
    }
}
