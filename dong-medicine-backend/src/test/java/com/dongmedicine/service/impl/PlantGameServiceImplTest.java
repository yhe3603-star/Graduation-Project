package com.dongmedicine.service.impl;

import com.dongmedicine.dto.PlantGameAnswerDTO;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.mapper.PlantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("植物游戏服务测试")
class PlantGameServiceImplTest {

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private PlantGameRecordMapper gameRecordMapper;

    @InjectMocks
    private PlantGameServiceImpl gameService;

    private Plant testPlant;

    @BeforeEach
    void setUp() throws Exception {
        setBaseMapper(gameService, gameRecordMapper);

        testPlant = new Plant();
        testPlant.setId(1);
        testPlant.setNameCn("钩藤");
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

    @Test
    @DisplayName("提交游戏 - 用户未登录抛异常")
    void testSubmit_UserNull() {
        PlantGameSubmitDTO dto = new PlantGameSubmitDTO();
        assertThrows(IllegalArgumentException.class, () -> gameService.submit(null, dto));
    }

    @Test
    @DisplayName("提交游戏 - DTO为空抛异常")
    void testSubmit_DtoNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.submit(1, null));
    }

    @Test
    @DisplayName("提交游戏 - 服务端验证全部正确")
    void testSubmit_ServerValidationAllCorrect() {
        PlantGameAnswerDTO answer = new PlantGameAnswerDTO();
        answer.setPlantId(1);
        answer.setUserAnswer("钩藤");

        PlantGameSubmitDTO dto = new PlantGameSubmitDTO();
        dto.setAnswers(Arrays.asList(answer));
        dto.setDifficulty("easy");

        when(plantMapper.selectById(1)).thenReturn(testPlant);
        when(gameRecordMapper.insert(any(PlantGameRecord.class))).thenReturn(1);

        Integer score = gameService.submit(1, dto);

        assertEquals(100, score);
        verify(gameRecordMapper).insert(any(PlantGameRecord.class));
    }

    @Test
    @DisplayName("提交游戏 - 兼容旧版前端")
    void testSubmit_LegacyFrontend() {
        PlantGameSubmitDTO dto = new PlantGameSubmitDTO();
        dto.setTotalCount(10);
        dto.setCorrectCount(8);
        dto.setDifficulty("hard");
        when(gameRecordMapper.insert(any(PlantGameRecord.class))).thenReturn(1);

        Integer score = gameService.submit(1, dto);

        assertEquals(80, score);
    }

    @Test
    @DisplayName("获取用户记录 - 成功")
    void testGetUserRecords_Success() {
        PlantGameRecord record = new PlantGameRecord();
        record.setId(1);
        record.setUserId(1);
        record.setScore(80);

        when(gameRecordMapper.selectList(any())).thenReturn(Arrays.asList(record));

        List<PlantGameRecord> result = gameService.getUserRecords(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取用户记录 - 用户ID为空返回空列表")
    void testGetUserRecords_NullUserId() {
        List<PlantGameRecord> result = gameService.getUserRecords(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
