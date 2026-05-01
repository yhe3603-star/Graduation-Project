package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.service.FavoriteService;
import com.dongmedicine.service.FeedbackService;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.service.PopularityAsyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("KnowledgeService 单元测试")
class KnowledgeServiceImplTest {

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private FileCleanupHelper fileCleanupHelper;

    @Mock
    private PopularityAsyncService popularityAsyncService;

    @InjectMocks
    private KnowledgeServiceImpl knowledgeService;

    @BeforeEach
    void injectBaseMapper() throws Exception {
        Class<?> clazz = knowledgeService.getClass();
        while (clazz != null) {
            try {
                Field baseMapperField = clazz.getDeclaredField("baseMapper");
                baseMapperField.setAccessible(true);
                baseMapperField.set(knowledgeService, knowledgeMapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    private Knowledge createKnowledge(Integer id, String title, String type) {
        Knowledge k = new Knowledge();
        k.setId(id);
        k.setTitle(title);
        k.setType(type);
        k.setContent("Content for " + title);
        k.setPopularity(100);
        k.setCreatedAt(LocalDateTime.now());
        return k;
    }

    @Nested
    @DisplayName("getByType - 按类型查询测试")
    class GetByType {

        @Test
        @DisplayName("空类型参数应返回全部")
        void shouldReturnAllWhenTypeIsEmpty() {
            List<Knowledge> expected = List.of(createKnowledge(1, "K1", "therapy"), createKnowledge(2, "K2", "disease"));
            when(knowledgeMapper.selectList(any())).thenReturn(expected);

            List<Knowledge> result = knowledgeService.getByType("");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("null类型参数应返回全部")
        void shouldReturnAllWhenTypeIsNull() {
            List<Knowledge> expected = List.of(createKnowledge(1, "K1", "therapy"));
            when(knowledgeMapper.selectList(any())).thenReturn(expected);

            List<Knowledge> result = knowledgeService.getByType(null);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("search - 关键词搜索测试")
    class Search {

        @Test
        @DisplayName("空关键词应返回全部")
        void shouldReturnAllForEmptyKeyword() {
            List<Knowledge> expected = List.of(createKnowledge(1, "K1", "therapy"));
            when(knowledgeMapper.selectList(any())).thenReturn(expected);

            List<Knowledge> result = knowledgeService.search("");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("null关键词应返回全部")
        void shouldReturnAllForNullKeyword() {
            List<Knowledge> expected = List.of(createKnowledge(1, "K1", "therapy"));
            when(knowledgeMapper.selectList(any())).thenReturn(expected);

            List<Knowledge> result = knowledgeService.search(null);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("advancedSearch - 高级搜索测试")
    class AdvancedSearch {

        @Test
        @DisplayName("多条件组合搜索应正常工作")
        void shouldCombineMultipleConditions() {
            when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<Knowledge> result = knowledgeService.advancedSearch("鼻炎", "鼻炎疗法", "呼吸道", "艾草", "popularity");

            assertThat(result).isEmpty();
            verify(knowledgeMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("全部条件为空时应返回全部按热度排序")
        void shouldReturnAllSortedByPopularityWhenAllEmpty() {
            when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            knowledgeService.advancedSearch("", "", "", "", "popularity");

            verify(knowledgeMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("pageAll - 分页查询测试")
    class PageAll {

        @Test
        @DisplayName("null pageNumber应被PageUtils归一化为1")
        void shouldNormalizeNullPageNumber() {
            when(knowledgeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<>());

            knowledgeService.pageAll(null, 10, "time");

            verify(knowledgeMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("submitFeedback - 反馈提交测试")
    class SubmitFeedback {

        @Test
        @DisplayName("空反馈内容应抛出异常")
        void shouldThrowForEmptyFeedback() {
            assertThatThrownBy(() -> knowledgeService.submitFeedback(1, ""))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("null反馈内容应抛出异常")
        void shouldThrowForNullFeedback() {
            assertThatThrownBy(() -> knowledgeService.submitFeedback(1, null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("超长反馈应抛出异常")
        void shouldThrowForTooLongFeedback() {
            String longFeedback = "x".repeat(1001);

            assertThatThrownBy(() -> knowledgeService.submitFeedback(1, longFeedback))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("null knowledgeId应抛出异常")
        void shouldThrowForNullKnowledgeId() {
            assertThatThrownBy(() -> knowledgeService.submitFeedback(null, "good"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("知识不存在应抛出异常")
        void shouldThrowForNonExistentKnowledge() {
            when(knowledgeMapper.selectById(999)).thenReturn(null);

            assertThatThrownBy(() -> knowledgeService.submitFeedback(999, "good"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("deleteWithFiles - 删除测试")
    class DeleteWithFiles {

        @Test
        @DisplayName("知识不存在应直接返回")
        void shouldReturnWhenNotExists() {
            when(knowledgeMapper.selectById(999)).thenReturn(null);

            knowledgeService.deleteWithFiles(999);

            verify(knowledgeMapper, never()).deleteById(any(Serializable.class));
            verify(fileCleanupHelper, never()).deleteFilesFromJson(any());
        }

        @Test
        @DisplayName("存在时应删除知识并清理关联文件")
        void shouldDeleteWithFiles() {
            Knowledge k = createKnowledge(1, "K", "therapy");
            k.setVideos("[{\"url\":\"/files/v1.mp4\"}]");
            k.setDocuments("[{\"url\":\"/files/d1.pdf\"}]");
            when(knowledgeMapper.selectById(1)).thenReturn(k);

            knowledgeService.deleteWithFiles(1);

            verify(fileCleanupHelper).deleteFilesFromJson(k.getVideos());
            verify(fileCleanupHelper).deleteFilesFromJson(k.getDocuments());
        }
    }
}
