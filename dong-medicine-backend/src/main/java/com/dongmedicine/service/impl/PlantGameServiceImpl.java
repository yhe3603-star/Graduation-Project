package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.PlantGameAnswerDTO;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.service.PlantGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class PlantGameServiceImpl extends ServiceImpl<PlantGameRecordMapper, PlantGameRecord> implements PlantGameService {

    @Autowired
    private PlantMapper plantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer submit(Integer userId, PlantGameSubmitDTO dto) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        if (dto == null) {
            throw new IllegalArgumentException("提交数据不能为空");
        }

        int totalCount;
        int correctCount;

        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("答案列表不能为空");
        }

        totalCount = dto.getAnswers().size();
        correctCount = 0;
        for (PlantGameAnswerDTO answer : dto.getAnswers()) {
            if (answer.getPlantId() != null && StringUtils.hasText(answer.getUserAnswer())) {
                Plant plant = plantMapper.selectById(answer.getPlantId());
                if (plant != null && plant.getNameCn() != null
                        && plant.getNameCn().trim().equalsIgnoreCase(answer.getUserAnswer().trim())) {
                    correctCount++;
                }
            }
        }

        int score = totalCount > 0 ? (int) Math.round((double) correctCount / totalCount * 100) : 0;

        PlantGameRecord record = new PlantGameRecord();
        record.setUserId(userId);
        record.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : "easy");
        record.setScore(score);
        record.setCorrectCount(correctCount);
        record.setTotalCount(totalCount);

        baseMapper.insert(record);
        log.info("植物游戏记录已保存: userId={}, 得分={}/{}, 难度={}", userId, correctCount, totalCount, record.getDifficulty());

        return score;
    }

    @Override
    public Integer calculateScore(PlantGameSubmitDTO dto) {
        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            return 0;
        }

        int totalCount = dto.getAnswers().size();
        int correctCount = 0;
        for (PlantGameAnswerDTO answer : dto.getAnswers()) {
            if (answer.getPlantId() != null && StringUtils.hasText(answer.getUserAnswer())) {
                Plant plant = plantMapper.selectById(answer.getPlantId());
                if (plant != null && plant.getNameCn() != null
                        && plant.getNameCn().trim().equalsIgnoreCase(answer.getUserAnswer().trim())) {
                    correctCount++;
                }
            }
        }

        return totalCount > 0 ? (int) Math.round((double) correctCount / totalCount * 100) : 0;
    }

    @Override
    public List<PlantGameRecord> getUserRecords(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<PlantGameRecord>()
                .eq(PlantGameRecord::getUserId, userId)
                .orderByDesc(PlantGameRecord::getCreatedAt));
    }

    @Override
    public Page<PlantGameRecord> getUserRecordsPaged(Integer userId, Integer page, Integer size) {
        if (userId == null) {
            return new Page<>(page, size);
        }
        return baseMapper.selectPage(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<PlantGameRecord>()
                        .eq(PlantGameRecord::getUserId, userId)
                        .orderByDesc(PlantGameRecord::getCreatedAt));
    }
}
