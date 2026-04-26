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
import org.springframework.transaction.annotation.Transactional;
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
    @Cacheable(value = "searchResults", key = "'resources:' + (#category ?: 'all') + ':' + (#keyword ?: 'all') + ':' + (#fileType ?: 'all')")
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
            String engType = switch (fileType) {
                case "视频", "video" -> "video";
                case "文档", "document" -> "document";
                case "图片", "image" -> "image";
                default -> fileType;
            };
            qw.and(wrapper -> wrapper
                .apply("files IS NOT NULL AND JSON_SEARCH(files, 'one', {0}, NULL, '$[*].type') IS NOT NULL", engType + "/%"));
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "resources", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Resource resource = getById(id);
        if (resource == null) {
            return;
        }
        removeById(id);
        fileCleanupHelper.deleteFilesFromJson(resource.getFiles());
        log.info("Deleted resource {} with associated files", id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", count());
        stats.put("totalFavorites", resourceMapper.sumFavoriteCount());
        stats.put("totalViews", resourceMapper.sumViewCount());
        stats.put("totalDownloads", resourceMapper.sumDownloadCount());

        stats.put("videoCount", resourceMapper.countByFileType("video/"));
        stats.put("imageCount", resourceMapper.countByFileType("image/"));
        stats.put("documentCount", resourceMapper.countByFileType("application/"));
        stats.put("totalSize", resourceMapper.sumFileSize());
        return stats;
    }

    @Override
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("category", resourceMapper.selectDistinctCategory());
        map.put("type", List.of("视频", "文档", "图片"));
        return map;
    }
}
