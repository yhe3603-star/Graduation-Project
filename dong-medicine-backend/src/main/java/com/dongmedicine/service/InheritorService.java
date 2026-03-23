package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Inheritor;

import java.util.List;

/**
 * 传承人服务接口
 * 提供侗族医药传承人相关的业务操作，包括查询、搜索、管理等功能
 */
public interface InheritorService extends IService<Inheritor> {

    /**
     * 获取所有传承人列表
     * 
     * @return 传承人列表（按排序字段排序）
     */
    List<Inheritor> getAllInheritors();

    /**
     * 根据ID获取传承人详情
     * 
     * @param id 传承人ID
     * @return 传承人详情
     */
    Inheritor getDetailById(Integer id);

    /**
     * 搜索传承人
     * 
     * @param keyword 搜索关键词（匹配姓名、专长、简介）
     * @return 匹配的传承人列表
     */
    List<Inheritor> search(String keyword);

    /**
     * 根据级别获取传承人列表
     * 
     * @param level 传承人级别（国家级/省级/市级等）
     * @return 指定级别的传承人列表
     */
    List<Inheritor> getByLevel(String level);

    /**
     * 根据级别和排序方式获取传承人列表
     * 
     * @param level 传承人级别（可选）
     * @param sortBy 排序方式（name/experience）
     * @return 传承人列表
     */
    List<Inheritor> listByLevel(String level, String sortBy);

    /**
     * 根据级别和排序方式分页获取传承人列表
     * 
     * @param level 传承人级别（可选）
     * @param sortBy 排序方式（name/experience）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Inheritor> pageByLevel(String level, String sortBy, Integer page, Integer size);

    /**
     * 分页搜索传承人
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Inheritor> searchPaged(String keyword, Integer page, Integer size);

    /**
     * 根据ID获取传承人详情（包含额外信息）
     * 
     * @param id 传承人ID
     * @return 传承人详情
     */
    Inheritor getDetailWithExtras(Integer id);

    /**
     * 增加浏览次数
     * 
     * @param id 传承人ID
     */
    void incrementViewCount(Integer id);

    /**
     * 清除传承人相关缓存
     */
    void clearCache();

    /**
     * 删除传承人及其关联文件
     * 
     * @param id 传承人ID
     */
    void deleteWithFiles(Integer id);
}
