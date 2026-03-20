package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.PlantGameRecord;
import com.dongmedicine.mapper.PlantGameRecordMapper;
import com.dongmedicine.service.PlantGameService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class PlantGameServiceImpl extends ServiceImpl<PlantGameRecordMapper, PlantGameRecord> implements PlantGameService {

    @Override
    public Integer submit(Integer userId, PlantGameSubmitDTO dto) {
        PlantGameRecord record = new PlantGameRecord();
        record.setUserId(userId);
        record.setDifficulty(dto.getDifficulty());
        record.setScore(dto.getScore());
        record.setCorrectCount(dto.getCorrectCount());
        record.setTotalCount(dto.getTotalCount());
        record.setCreateTime(LocalDateTime.now());
        baseMapper.insert(record);
        return dto.getScore();
    }

    @Override
    public Integer calculateScore(PlantGameSubmitDTO dto) {
        return dto.getScore();
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
