package com.dongmedicine.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.dto.*;
import com.dongmedicine.entity.*;
import com.dongmedicine.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("后台管理-内容Controller测试")
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
    private AdminContentController adminContentController;

    // ========== 传承人 ==========

    @Nested
    @DisplayName("传承人管理")
    class InheritorTests {

        @Test
        @DisplayName("传承人列表 - 成功")
        void testListInheritors_Success() {
            Inheritor inheritor = new Inheritor();
            inheritor.setId(1);
            inheritor.setName("张三");
            Page<Inheritor> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(inheritor));
            when(inheritorService.page(any(), any())).thenReturn(pageResult);

            R<Map<String, Object>> result = adminContentController.listInheritors(1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(inheritorService).page(any(), any());
        }

        @Test
        @DisplayName("新增传承人 - 成功")
        void testCreateInheritor_Success() {
            InheritorCreateDTO dto = new InheritorCreateDTO();
            dto.setName("张三");
            dto.setExperienceYears(20);
            when(inheritorService.save(any(Inheritor.class))).thenReturn(true);

            R<String> result = adminContentController.createInheritor(dto);

            assertEquals(200, result.getCode());
            assertEquals("新增传承人成功", result.getData());
            verify(inheritorService).save(any(Inheritor.class));
            verify(inheritorService).clearCache();
        }

        @Test
        @DisplayName("更新传承人 - 成功")
        void testUpdateInheritor_Success() {
            InheritorUpdateDTO dto = new InheritorUpdateDTO();
            dto.setName("张三更新");
            dto.setExperienceYears(25);
            when(inheritorService.updateById(any(Inheritor.class))).thenReturn(true);

            R<String> result = adminContentController.updateInheritor(1, dto);

            assertEquals(200, result.getCode());
            assertEquals("更新传承人成功", result.getData());
            verify(inheritorService).updateById(any(Inheritor.class));
            verify(inheritorService).clearCache();
        }

        @Test
        @DisplayName("删除传承人 - 成功")
        void testDeleteInheritor_Success() {
            doNothing().when(inheritorService).deleteWithFiles(1);

            R<String> result = adminContentController.deleteInheritor(1);

            assertEquals(200, result.getCode());
            assertEquals("删除传承人成功", result.getData());
            verify(inheritorService).deleteWithFiles(1);
        }
    }

    // ========== 知识库 ==========

    @Nested
    @DisplayName("知识库管理")
    class KnowledgeTests {

        @Test
        @DisplayName("知识列表 - 成功")
        void testListKnowledge_Success() {
            Knowledge knowledge = new Knowledge();
            knowledge.setId(1);
            knowledge.setTitle("侗医草药");
            Page<Knowledge> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(knowledge));
            when(knowledgeService.page(any(), any())).thenReturn(pageResult);

            R<Map<String, Object>> result = adminContentController.listKnowledge(1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(knowledgeService).page(any(), any());
        }

        @Test
        @DisplayName("新增知识条目 - 成功")
        void testCreateKnowledge_Success() {
            KnowledgeCreateDTO dto = new KnowledgeCreateDTO();
            dto.setTitle("侗医草药");
            dto.setContent("草药内容");
            when(knowledgeService.save(any(Knowledge.class))).thenReturn(true);

            R<String> result = adminContentController.createKnowledge(dto);

            assertEquals(200, result.getCode());
            assertEquals("新增知识条目成功", result.getData());
            verify(knowledgeService).save(any(Knowledge.class));
            verify(knowledgeService).clearCache();
        }

        @Test
        @DisplayName("更新知识条目 - 成功")
        void testUpdateKnowledge_Success() {
            KnowledgeUpdateDTO dto = new KnowledgeUpdateDTO();
            dto.setTitle("侗医草药更新");
            dto.setContent("更新内容");
            when(knowledgeService.updateById(any(Knowledge.class))).thenReturn(true);

            R<String> result = adminContentController.updateKnowledge(1, dto);

            assertEquals(200, result.getCode());
            assertEquals("更新知识条目成功", result.getData());
            verify(knowledgeService).updateById(any(Knowledge.class));
            verify(knowledgeService).clearCache();
        }

        @Test
        @DisplayName("删除知识条目 - 成功")
        void testDeleteKnowledge_Success() {
            doNothing().when(knowledgeService).deleteWithFiles(1);

            R<String> result = adminContentController.deleteKnowledge(1);

            assertEquals(200, result.getCode());
            assertEquals("删除知识条目成功", result.getData());
            verify(knowledgeService).deleteWithFiles(1);
        }
    }

    // ========== 药用植物 ==========

    @Nested
    @DisplayName("药用植物管理")
    class PlantTests {

        @Test
        @DisplayName("植物列表 - 成功")
        void testListPlants_Success() {
            Plant plant = new Plant();
            plant.setId(1);
            plant.setNameCn("钩藤");
            Page<Plant> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(plant));
            when(plantService.page(any(), any())).thenReturn(pageResult);

            R<Map<String, Object>> result = adminContentController.listPlants(1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(plantService).page(any(), any());
        }

        @Test
        @DisplayName("新增药用植物 - 成功")
        void testCreatePlant_Success() {
            PlantCreateDTO dto = new PlantCreateDTO();
            dto.setNameCn("钩藤");
            dto.setCategory("清热药");
            when(plantService.save(any(Plant.class))).thenReturn(true);

            R<String> result = adminContentController.createPlant(dto);

            assertEquals(200, result.getCode());
            assertEquals("新增药用植物成功", result.getData());
            verify(plantService).save(any(Plant.class));
            verify(plantService).clearCache();
        }

        @Test
        @DisplayName("更新药用植物 - 成功")
        void testUpdatePlant_Success() {
            PlantUpdateDTO dto = new PlantUpdateDTO();
            dto.setNameCn("钩藤更新");
            dto.setCategory("清热药");
            when(plantService.updateById(any(Plant.class))).thenReturn(true);

            R<String> result = adminContentController.updatePlant(1, dto);

            assertEquals(200, result.getCode());
            assertEquals("更新药用植物成功", result.getData());
            verify(plantService).updateById(any(Plant.class));
            verify(plantService).clearCache();
        }

        @Test
        @DisplayName("删除药用植物 - 成功")
        void testDeletePlant_Success() {
            doNothing().when(plantService).deleteWithFiles(1);

            R<String> result = adminContentController.deletePlant(1);

            assertEquals(200, result.getCode());
            assertEquals("删除药用植物成功", result.getData());
            verify(plantService).deleteWithFiles(1);
        }
    }

    // ========== 问答 ==========

    @Nested
    @DisplayName("问答管理")
    class QaTests {

        @Test
        @DisplayName("问答列表 - 成功")
        void testListQa_Success() {
            Qa qa = new Qa();
            qa.setId(1);
            qa.setQuestion("什么是侗医");
            Page<Qa> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(qa));
            when(qaService.page(any(), any())).thenReturn(pageResult);

            R<Map<String, Object>> result = adminContentController.listQa(1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(qaService).page(any(), any());
        }

        @Test
        @DisplayName("新增问答 - 成功")
        void testCreateQa_Success() {
            QaCreateDTO dto = new QaCreateDTO();
            dto.setQuestion("什么是侗医");
            dto.setAnswer("侗医是侗族传统医学");
            when(qaService.save(any(Qa.class))).thenReturn(true);

            R<String> result = adminContentController.createQa(dto);

            assertEquals(200, result.getCode());
            assertEquals("新增问答成功", result.getData());
            verify(qaService).save(any(Qa.class));
        }

        @Test
        @DisplayName("更新问答 - 成功")
        void testUpdateQa_Success() {
            QaUpdateDTO dto = new QaUpdateDTO();
            dto.setQuestion("什么是侗医");
            dto.setAnswer("更新的答案");
            when(qaService.updateById(any(Qa.class))).thenReturn(true);

            R<String> result = adminContentController.updateQa(1, dto);

            assertEquals(200, result.getCode());
            assertEquals("更新问答成功", result.getData());
            verify(qaService).updateById(any(Qa.class));
        }

        @Test
        @DisplayName("删除问答 - 成功")
        void testDeleteQa_Success() {
            when(qaService.removeById(1)).thenReturn(true);

            R<String> result = adminContentController.deleteQa(1);

            assertEquals(200, result.getCode());
            assertEquals("删除问答成功", result.getData());
            verify(qaService).removeById(1);
        }
    }

    // ========== 学习资源 ==========

    @Nested
    @DisplayName("学习资源管理")
    class ResourceTests {

        @Test
        @DisplayName("资源列表 - 成功")
        void testListResources_Success() {
            Resource resource = new Resource();
            resource.setId(1);
            resource.setTitle("侗医教材");
            Page<Resource> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(Arrays.asList(resource));
            when(resourceService.page(any(), any())).thenReturn(pageResult);

            R<Map<String, Object>> result = adminContentController.listResources(1, 20);

            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(resourceService).page(any(), any());
        }

        @Test
        @DisplayName("新增学习资源 - 成功")
        void testCreateResource_Success() {
            ResourceCreateDTO dto = new ResourceCreateDTO();
            dto.setTitle("侗医教材");
            when(resourceService.save(any(Resource.class))).thenReturn(true);

            R<String> result = adminContentController.createResource(dto);

            assertEquals(200, result.getCode());
            assertEquals("新增学习资源成功", result.getData());
            verify(resourceService).save(any(Resource.class));
            verify(resourceService).clearCache();
        }

        @Test
        @DisplayName("更新学习资源 - 成功")
        void testUpdateResource_Success() {
            ResourceUpdateDTO dto = new ResourceUpdateDTO();
            dto.setTitle("侗医教材更新");
            when(resourceService.updateById(any(Resource.class))).thenReturn(true);

            R<String> result = adminContentController.updateResource(1, dto);

            assertEquals(200, result.getCode());
            assertEquals("更新学习资源成功", result.getData());
            verify(resourceService).updateById(any(Resource.class));
            verify(resourceService).clearCache();
        }

        @Test
        @DisplayName("删除学习资源 - 成功")
        void testDeleteResource_Success() {
            doNothing().when(resourceService).deleteWithFiles(1);

            R<String> result = adminContentController.deleteResource(1);

            assertEquals(200, result.getCode());
            assertEquals("删除学习资源成功", result.getData());
            verify(resourceService).deleteWithFiles(1);
        }
    }
}
