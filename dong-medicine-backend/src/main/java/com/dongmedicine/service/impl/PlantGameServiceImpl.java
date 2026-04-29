package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

        // 优先使用 answers 列表进行服务端验证
        if (dto.getAnswers() != null && !dto.getAnswers().isEmpty()) {
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
        } else {
            // 兼容旧版前端：直接使用客户端提交的计数，做合理性校验
            totalCount = Math.max(dto.getTotalCount() != null ? dto.getTotalCount() : 0, 1);
            correctCount = dto.getCorrectCount() != null ? Math.min(dto.getCorrectCount(), totalCount) : 0;
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
        int totalCount;
        int correctCount;

        if (dto.getAnswers() != null && !dto.getAnswers().isEmpty()) {
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
        } else {
            totalCount = Math.max(dto.getTotalCount() != null ? dto.getTotalCount() : 0, 1);
            correctCount = dto.getCorrectCount() != null ? Math.min(dto.getCorrectCount(), totalCount) : 0;
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
}
