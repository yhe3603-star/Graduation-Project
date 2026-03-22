package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.service.InheritorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class InheritorServiceImpl extends ServiceImpl<InheritorMapper, Inheritor> implements InheritorService {

    private static final Logger log = LoggerFactory.getLogger(InheritorServiceImpl.class);

    @Autowired
    private InheritorMapper inheritorMapper;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;

    @Override
    public List<Inheritor> getAllInheritors() {
        return list(new LambdaQueryWrapper<Inheritor>()
                .orderByDesc(Inheritor::getExperienceYears)
                .orderByAsc(Inheritor::getName));
    }

    @Override
    public Inheritor getDetailById(Integer id) {
        return getById(id);
    }

    @Override
    public List<Inheritor> getByLevel(String level) {
        if (!StringUtils.hasText(level)) {
            return getAllInheritors();
        }
        return list(new LambdaQueryWrapper<Inheritor>()
                .eq(Inheritor::getLevel, level)
                .orderByDesc(Inheritor::getExperienceYears));
    }

    @Override
    @Cacheable(value = "inheritors", key = "'list:' + #level + ':' + #sortBy")
    public List<Inheritor> listByLevel(String level, String sortBy) {
        LambdaQueryWrapper<Inheritor> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(level)) {
            qw.eq(Inheritor::getLevel, level);
        }
        qw.orderByDesc("experience".equals(sortBy), Inheritor::getExperienceYears)
          .orderByAsc(!"experience".equals(sortBy), Inheritor::getName);
        return list(qw);
    }

    @Override
    @Cacheable(value = "inheritors", key = "'detail:' + #id")
    public Inheritor getDetailWithExtras(Integer id) {
        return getById(id);
    }

    @Override
    public List<Inheritor> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw BusinessException.badRequest("搜索关键词不能为空");
        }
        return list(new LambdaQueryWrapper<Inheritor>()
                .like(Inheritor::getName, keyword)
                .or().like(Inheritor::getSpecialties, keyword)
                .orderByDesc(Inheritor::getExperienceYears));
    }

    @Override
    public void incrementViewCount(Integer id) {
        try {
            inheritorMapper.incrementViewCount(id);
            log.debug("Inheritor view count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment view count for inheritor id: {}", id, e);
        }
    }

    @Override
    @CacheEvict(value = "inheritors", allEntries = true)
    public void clearCache() {
        log.info("Inheritor cache cleared");
    }

    @Override
    @CacheEvict(value = "inheritors", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Inheritor inheritor = getById(id);
        if (inheritor == null) {
            return;
        }
        fileCleanupHelper.deleteFilesFromJson(inheritor.getImages());
        fileCleanupHelper.deleteFilesFromJson(inheritor.getVideos());
        fileCleanupHelper.deleteFilesFromJson(inheritor.getDocuments());
        removeById(id);
        log.info("Deleted inheritor {} with associated files", id);
    }
}
