package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.entity.Favorite;
import com.dongmedicine.mapper.*;
import com.dongmedicine.mq.producer.StatisticsProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FavoriteServiceImplTest {

    @Mock private FavoriteMapper favoriteMapper;
    @Mock private PlantMapper plantMapper;
    @Mock private KnowledgeMapper knowledgeMapper;
    @Mock private InheritorMapper inheritorMapper;
    @Mock private ResourceMapper resourceMapper;
    @Mock private QaMapper qaMapper;
    @Mock private StatisticsProducer statisticsProducer;

    private FavoriteServiceImpl favoriteService;

    @BeforeEach
    void setUp() throws Exception {
        favoriteService = new FavoriteServiceImpl(
                plantMapper, knowledgeMapper, inheritorMapper,
                resourceMapper, qaMapper);
        setField(favoriteService, "statisticsProducer", statisticsProducer);
        setBaseMapper(favoriteService, favoriteMapper);
    }

    private void setBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(service, mapper);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Test
    @DisplayName("添加收藏 - 已收藏应抛异常")
    void addFavoriteAlreadyExists() {
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(Exception.class, () -> {
            favoriteService.addFavorite(1, "plant", 1);
        });
    }

    @Test
    @DisplayName("取消收藏 - 不存在时应正常返回")
    void removeFavoriteNotExists() {
        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);

        assertDoesNotThrow(() -> {
            favoriteService.removeFavorite(1, "plant", 999);
        });
    }

    @Test
    @DisplayName("我的收藏 - 无收藏应返回空列表")
    void myFavoritesEmpty() {
        when(favoriteMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        var result = favoriteService.getMyFavorites(1);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("添加收藏 - 不支持的类型应抛异常")
    void addFavoriteUnsupportedType() {
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(favoriteMapper.insert(any(Favorite.class))).thenReturn(1);

        assertThrows(Exception.class, () -> {
            favoriteService.addFavorite(1, "unsupported", 1);
        });
    }

    @Test
    @DisplayName("添加收藏 - 成功添加plant类型")
    void addFavoritePlantSuccess() {
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(favoriteMapper.insert(any(Favorite.class))).thenReturn(1);
        doNothing().when(plantMapper).incrementFavoriteCount(anyInt(), anyInt());
        doNothing().when(statisticsProducer).sendStatisticsTask(any(StatisticsProducer.StatisticsTask.class));

        assertDoesNotThrow(() -> {
            favoriteService.addFavorite(1, "plant", 1);
        });

        verify(plantMapper).incrementFavoriteCount(1, 1);
    }

    @Test
    @DisplayName("取消收藏 - 存在时正常删除")
    void removeFavoriteExists() {
        Favorite favorite = new Favorite();
        favorite.setId(1);
        favorite.setUserId(1);
        favorite.setTargetType("plant");
        favorite.setTargetId(1);
        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(favorite);
        when(favoriteMapper.deleteById(1)).thenReturn(1);
        doNothing().when(plantMapper).incrementFavoriteCount(anyInt(), anyInt());
        doNothing().when(statisticsProducer).sendStatisticsTask(any(StatisticsProducer.StatisticsTask.class));

        assertDoesNotThrow(() -> {
            favoriteService.removeFavorite(1, "plant", 1);
        });

        verify(plantMapper).incrementFavoriteCount(1, -1);
    }
}
