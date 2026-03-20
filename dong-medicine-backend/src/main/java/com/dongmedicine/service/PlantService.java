package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Plant;
import java.util.List;

public interface PlantService extends IService<Plant> {
    List<Plant> listByDoubleFilter(String category, String usageWay);
    List<Plant> search(String keyword);
    List<Plant> getSimilarPlants(Integer id);
    Plant getDetailWithStory(Integer id);
    List<Plant> getRandomByDifficulty(String difficulty, int limit);
    void incrementViewCount(Integer id);
    void clearCache();
    void deleteWithFiles(Integer id);
}
