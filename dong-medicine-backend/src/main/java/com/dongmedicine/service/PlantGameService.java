package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.dto.PlantGameSubmitDTO;
import com.dongmedicine.entity.PlantGameRecord;

import java.util.List;

public interface PlantGameService {
    Integer submit(Integer userId, PlantGameSubmitDTO dto);

    Integer calculateScore(PlantGameSubmitDTO dto);

    List<PlantGameRecord> getUserRecords(Integer userId);

    Page<PlantGameRecord> getUserRecordsPaged(Integer userId, Integer page, Integer size);
}
