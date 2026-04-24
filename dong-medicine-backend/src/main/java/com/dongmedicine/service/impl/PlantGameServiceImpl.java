package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.service.PlantGameService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PlantGameServiceImpl extends ServiceImpl<PlantGameRecordMapper, PlantGameRecord> implements PlantGameService {

    @Override
    public Integer submit(Integer userId, PlantGameSubmitDTO dto) {
        PlantGameRecord record = new PlantGameRecord();
        record.setUserId(userId);
        record.setDifficulty(dto.getDifficulty());
        int calculatedScore = dto.getTotalCount() > 0
                ? (int) Math.round((double) dto.getCorrectCount() / dto.getTotalCount() * 100)
                : 0;
        record.setScore(calculatedScore);
        record.setCorrectCount(Math.min(dto.getCorrectCount(), dto.getTotalCount()));
        record.setTotalCount(Math.max(dto.getTotalCount(), 1));
        baseMapper.insert(record);
        return calculatedScore;
    }

    @Override
    public Integer calculateScore(PlantGameSubmitDTO dto) {
        int totalCount = Math.max(dto.getTotalCount(), 1);
        int correctCount = Math.min(dto.getCorrectCount(), dto.getTotalCount());
        return totalCount > 0
                ? (int) Math.round((double) correctCount / totalCount * 100)
                : 0;
    }

    @Override
    public List<PlantGameRecord> getUserRecords(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<PlantGameRecord>()
                .eq(PlantGameRecord::getUserId, userId)
                .orderByDesc(PlantGameRecord::getCreateTime));
    }
}
