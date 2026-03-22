package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Plant;
import java.util.List;

/**
 * 药用植物服务接口
 * 提供药用植物相关的业务操作，包括查询、搜索、浏览统计等功能
 */
public interface PlantService extends IService<Plant> {

    /**
     * 根据分类和用法双重过滤获取植物列表
     * 
     * @param category 植物分类（可选）
     * @param usageWay 用法方式（可选）
     * @return 过滤后的植物列表（按中文名称排序）
     */
    List<Plant> listByDoubleFilter(String category, String usageWay);

    /**
     * 搜索植物（使用默认限制数量）
     * 
     * @param keyword 搜索关键词
     * @return 匹配的植物列表
     * @throws com.dongmedicine.common.exception.BusinessException 当关键词为空时抛出
     */
    List<Plant> search(String keyword);

    /**
     * 搜索植物
     * 
     * @param keyword 搜索关键词
     * @param limit 返回结果数量限制（1-100）
     * @return 匹配的植物列表
     * @throws com.dongmedicine.common.exception.BusinessException 当关键词为空或限制数量超出范围时抛出
     */
    List<Plant> search(String keyword, int limit);

    /**
     * 获取相似植物
     * 根据植物分类查找同类植物
     * 
     * @param id 植物ID
     * @return 相似植物列表（最多4个）
     */
    List<Plant> getSimilarPlants(Integer id);

    /**
     * 获取植物详情（包含故事）
     * 
     * @param id 植物ID
     * @return 植物详情，不存在则返回null
     */
    Plant getDetailWithStory(Integer id);

    /**
     * 根据难度随机获取植物
     * 
     * @param difficulty 难度级别
     * @param limit 返回数量（1-100）
     * @return 随机植物列表
     * @throws com.dongmedicine.common.exception.BusinessException 当难度为空或限制数量超出范围时抛出
     */
    List<Plant> getRandomByDifficulty(String difficulty, int limit);

    /**
     * 增加浏览次数
     * 
     * @param id 植物ID
     */
    void incrementViewCount(Integer id);

    /**
     * 清除植物相关缓存
     */
    void clearCache();

    /**
     * 删除植物及其关联文件
     * 会同时删除植物关联的图片、视频、文档文件
     * 
     * @param id 植物ID
     */
    void deleteWithFiles(Integer id);
}
