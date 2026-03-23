package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Knowledge;

import java.util.List;

/**
 * 知识库服务接口
 * 提供侗族医药知识相关的业务操作，包括疗法、疾病分类的查询和管理
 */
public interface KnowledgeService extends IService<Knowledge> {

    /**
     * 获取所有疗法分类
     * 
     * @return 疗法分类列表
     */
    List<Knowledge> getAllTherapies();

    /**
     * 获取所有疾病分类
     * 
     * @return 疾病分类列表
     */
    List<Knowledge> getAllDiseases();

    /**
     * 根据类型获取知识列表
     * 
     * @param type 知识类型（therapy/disease）
     * @return 指定类型的知识列表
     */
    List<Knowledge> getByType(String type);

    /**
     * 搜索知识
     * 
     * @param keyword 搜索关键词
     * @return 匹配的知识列表
     */
    List<Knowledge> search(String keyword);

    /**
     * 高级搜索知识
     * 
     * @param keyword 关键词（可选）
     * @param therapy 疗法分类（可选）
     * @param disease 疾病分类（可选）
     * @param sortBy 排序方式（popularity/time）
     * @return 匹配的知识列表
     */
    List<Knowledge> advancedSearch(String keyword, String therapy, String disease, String herb, String sortBy);

    /**
     * 分页获取所有知识
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序方式
     * @return 分页结果
     */
    Page<Knowledge> pageAll(Integer page, Integer size, String sortBy);

    /**
     * 分页高级搜索知识
     * 
     * @param keyword 关键词（可选）
     * @param therapy 疗法分类（可选）
     * @param disease 疾病分类（可选）
     * @param herb 药材分类（可选）
     * @param sortBy 排序方式（popularity/time）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Knowledge> advancedSearchPaged(String keyword, String therapy, String disease, String herb, String sortBy, Integer page, Integer size);

    /**
     * 根据ID获取知识详情
     * 
     * @param id 知识ID
     * @return 知识详情
     */
    Knowledge getDetailById(Integer id);

    /**
     * 根据ID获取知识详情（包含相关内容）
     * 
     * @param id 知识ID
     * @return 知识详情
     */
    Knowledge getDetailWithRelated(Integer id);

    /**
     * 增加浏览次数
     * 
     * @param id 知识ID
     */
    void incrementViewCount(Integer id);

    /**
     * 添加收藏
     * 
     * @param userId 用户ID
     * @param knowledgeId 知识ID
     */
    void addFavorite(Integer userId, Integer knowledgeId);

    /**
     * 提交反馈
     * 
     * @param knowledgeId 知识ID
     * @param feedback 反馈内容
     */
    void submitFeedback(Integer knowledgeId, String feedback);

    /**
     * 清除知识相关缓存
     */
    void clearCache();

    /**
     * 删除知识及其关联文件
     * 会同时删除知识关联的视频、文档文件
     * 
     * @param id 知识ID
     */
    void deleteWithFiles(Integer id);
}
