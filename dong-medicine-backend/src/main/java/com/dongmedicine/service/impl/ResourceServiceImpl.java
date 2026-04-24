package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Resource;
import com.dongmedicine.mapper.ResourceMapper;
import com.dongmedicine.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;

    @Override
    public void incrementDownload(Integer id) {
        try {
            resourceMapper.incrementDownloadCount(id);
            log.debug("Resource download count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment download count for resource id: {}", id, e);
        }
    }

    @Override
    public void incrementViewCount(Integer id) {
        try {
            resourceMapper.incrementViewCount(id);
            log.debug("Resource view count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment view count for resource id: {}", id, e);
        }
    }

    @Override
    @Cacheable(value = "resources", key = "'list:' + (#category ?: 'all') + ':' + (#keyword ?: 'all') + ':' + (#fileType ?: 'all')")
    public List<Resource> listByCategoryAndKeywordAndType(String category, String keyword, String fileType) {
        LambdaQueryWrapper<Resource> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            qw.eq(Resource::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(wrapper -> wrapper.like(Resource::getTitle, escapedKeyword).or().like(Resource::getDescription, escapedKeyword));
        }
        qw.orderByDesc(Resource::getDownloadCount);
        return list(qw);
    }

    @Override
    public Page<Resource> pageByCategoryAndKeywordAndType(String category, String keyword, String fileType, Integer page, Integer size) {
        Page<Resource> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Resource> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            qw.eq(Resource::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(wrapper -> wrapper.like(Resource::getTitle, escapedKeyword).or().like(Resource::getDescription, escapedKeyword));
        }
        if (StringUtils.hasText(fileType)) {
            String escapedFileType = PageUtils.escapeLike(fileType);
            if ("document".equals(fileType)) {
                qw.and(wrapper -> wrapper
                    .like(Resource::getFiles, "\"type\":\"document\"")
                    .or().like(Resource::getFiles, "\"type\":\"pdf\"")
                    .or().like(Resource::getFiles, "\"type\":\"docx\"")
                    .or().like(Resource::getFiles, "\"type\":\"doc\"")
                    .or().like(Resource::getFiles, "\"type\":\"pptx\"")
                    .or().like(Resource::getFiles, "\"type\":\"xlsx\"")
                    .or().like(Resource::getFiles, "\"type\":\"txt\""));
            } else if ("video".equals(fileType)) {
                qw.and(wrapper -> wrapper
                    .like(Resource::getFiles, "\"type\":\"video\"")
                    .or().like(Resource::getFiles, "\"type\":\"mp4\"")
                    .or().like(Resource::getFiles, "\"type\":\"avi\"")
                    .or().like(Resource::getFiles, "\"type\":\"mov\""));
            } else if ("image".equals(fileType)) {
                qw.and(wrapper -> wrapper
                    .like(Resource::getFiles, "\"type\":\"image\"")
                    .or().like(Resource::getFiles, "\"type\":\"jpg\"")
                    .or().like(Resource::getFiles, "\"type\":\"png\"")
                    .or().like(Resource::getFiles, "\"type\":\"gif\""));
            } else {
                qw.like(Resource::getFiles, "\"type\":\"" + escapedFileType + "\"");
            }
        }
        qw.orderByDesc(Resource::getDownloadCount);
        return page(pageParam, qw);
    }

    @Override
    public List<Resource> listByCategoryAndKeyword(String category, String keyword) {
        return listByCategoryAndKeywordAndType(category, keyword, null);
    }

    @Override
    @Cacheable(value = "resources", key = "'hot'")
    public List<Resource> getHotResources() {
        return list(new LambdaQueryWrapper<Resource>()
                .orderByDesc(Resource::getDownloadCount)
                .last("LIMIT 6"));
    }

    @Override
    @Cacheable(value = "resources", key = "'categories'")
    public List<String> getAllCategories() {
        return list(new LambdaQueryWrapper<Resource>()
                .select(Resource::getCategory)
                .isNotNull(Resource::getCategory)
                .groupBy(Resource::getCategory)).stream()
                .map(Resource::getCategory)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "resources", allEntries = true)
    public void clearCache() {
        log.info("Resource cache cleared");
    }

    @Override
    @CacheEvict(value = "resources", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Resource resource = getById(id);
        if (resource == null) {
            return;
        }
        fileCleanupHelper.deleteFilesFromJson(resource.getFiles());
        removeById(id);
        log.info("Deleted resource {} with associated files", id);
    }

    @Override
    public Map<String, Object> getStats() {
        // 使用SQL聚合查询获取数值统计
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", count());
        stats.put("totalFavorites", resourceMapper.sumFavoriteCount());
        stats.put("totalViews", resourceMapper.sumViewCount());
        stats.put("totalDownloads", resourceMapper.sumDownloadCount());

        // 解析files JSON统计文件类型数量和总大小（仅查询files字段，避免全表加载）
        long videoCount = 0, documentCount = 0, imageCount = 0, totalSize = 0;
        List<String> filesList = resourceMapper.selectAllFiles();
        com.fasterxml.jackson.databind.ObjectMapper jsonMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        for (String files : filesList) {
            try {
                List<Map<String, Object>> fileList = jsonMapper.readValue(files, List.class);
                for (Map<String, Object> f : fileList) {
                    String type = f.get("type") != null ? f.get("type").toString() : "";
                    long size = f.get("size") != null ? Long.parseLong(f.get("size").toString()) : 0;
                    totalSize += size;
                    if (type.startsWith("video/")) videoCount++;
                    else if (type.startsWith("image/")) imageCount++;
                    else documentCount++;
                }
            } catch (Exception ignored) {}
        }
        stats.put("videoCount", videoCount);
        stats.put("documentCount", documentCount);
        stats.put("imageCount", imageCount);
        stats.put("totalSize", totalSize);
        return stats;
    }

    @Override
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("category", resourceMapper.selectDistinctCategory());
        return map;
    }
}
