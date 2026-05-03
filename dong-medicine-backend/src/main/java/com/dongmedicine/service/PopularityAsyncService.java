package com.dongmedicine.service;

public interface PopularityAsyncService {
    void incrementPlantViewAndPopularity(Integer id);
    void incrementKnowledgeViewAndPopularity(Integer id);
    void incrementInheritorViewAndPopularity(Integer id);
    void incrementQaViewAndPopularity(Integer id);
    void incrementResourceViewAndPopularity(Integer id);
}
