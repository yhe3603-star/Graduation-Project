package com.dongmedicine.service;

public interface PopularityAsyncService {
    void incrementPlantPopularity(Integer id);
    void incrementKnowledgePopularity(Integer id);
    void incrementInheritorPopularity(Integer id);
}
