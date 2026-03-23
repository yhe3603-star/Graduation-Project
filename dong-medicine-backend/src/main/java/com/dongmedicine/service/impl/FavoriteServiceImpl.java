package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.entity.*;
import com.dongmedicine.mapper.*;
import com.dongmedicine.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    @Autowired
    private PlantMapper plantMapper;
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    @Autowired
    private InheritorMapper inheritorMapper;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private QaMapper qaMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Integer userId, String targetType, Integer targetId) {
        Long count = count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId));
        if (count > 0) {
            throw BusinessException.conflict("已经收藏过了");
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setTargetType(targetType);
        f.setTargetId(targetId);
        save(f);
        incrementFavoriteCount(targetType, targetId, 1);
        log.info("User {} added favorite: type={}, id={}", userId, targetType, targetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Integer userId, String targetType, Integer targetId) {
        Favorite favorite = getOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId));
        if (favorite != null) {
            removeById(favorite.getId());
            incrementFavoriteCount(targetType, targetId, -1);
            log.info("User {} removed favorite: type={}, id={}", userId, targetType, targetId);
        }
    }

    private void incrementFavoriteCount(String targetType, Integer targetId, int delta) {
        switch (targetType) {
            case "plant" -> plantMapper.incrementFavoriteCount(targetId, delta);
            case "knowledge" -> knowledgeMapper.incrementFavoriteCount(targetId, delta);
            case "inheritor" -> inheritorMapper.incrementFavoriteCount(targetId, delta);
            case "resource" -> resourceMapper.incrementFavoriteCount(targetId, delta);
            case "qa" -> qaMapper.incrementFavoriteCount(targetId, delta);
            default -> throw BusinessException.badRequest("不支持的收藏类型: " + targetType);
        }
    }

    @Override
    public List<Map<String, Object>> getMyFavorites(Integer userId) {
        List<Favorite> favorites = list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt));

        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<Integer>> groupedIds = favorites.stream()
                .collect(Collectors.groupingBy(
                        Favorite::getTargetType,
                        Collectors.mapping(Favorite::getTargetId, Collectors.toList())
                ));

        Map<String, Map<Integer, Object>> dataCache = new HashMap<>();
        
        groupedIds.forEach((type, ids) -> {
            Map<Integer, Object> typeData = batchQueryByType(type, ids);
            dataCache.put(type, typeData);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite f : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", f.getId());
            item.put("type", f.getTargetType());
            item.put("targetId", f.getTargetId());
            item.put("createTime", f.getCreatedAt());

            Map<Integer, Object> typeCache = dataCache.get(f.getTargetType());
            if (typeCache != null && typeCache.containsKey(f.getTargetId())) {
                fillItemFromCache(item, f.getTargetType(), typeCache.get(f.getTargetId()));
            }
            result.add(item);
        }
        return result;
    }

    private Map<Integer, Object> batchQueryByType(String type, List<Integer> ids) {
        Map<Integer, Object> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }

        switch (type) {
            case "plant":
                List<Plant> plants = plantMapper.selectBatchIds(ids);
                plants.forEach(p -> result.put(p.getId(), p));
                break;
            case "knowledge":
                List<Knowledge> knowledgeList = knowledgeMapper.selectBatchIds(ids);
                knowledgeList.forEach(k -> result.put(k.getId(), k));
                break;
            case "inheritor":
                List<Inheritor> inheritors = inheritorMapper.selectBatchIds(ids);
                inheritors.forEach(i -> result.put(i.getId(), i));
                break;
            case "resource":
                List<Resource> resources = resourceMapper.selectBatchIds(ids);
                resources.forEach(r -> result.put(r.getId(), r));
                break;
            case "qa":
                List<Qa> qaList = qaMapper.selectBatchIds(ids);
                qaList.forEach(q -> result.put(q.getId(), q));
                break;
        }
        return result;
    }

    private void fillItemFromCache(Map<String, Object> item, String targetType, Object data) {
        switch (targetType) {
            case "plant":
                Plant plant = (Plant) data;
                item.put("nameCn", plant.getNameCn());
                item.put("name", plant.getNameCn());
                item.put("description", plant.getEfficacy());
                break;
            case "knowledge":
                Knowledge knowledge = (Knowledge) data;
                item.put("title", knowledge.getTitle());
                item.put("name", knowledge.getTitle());
                item.put("description", knowledge.getContent());
                break;
            case "inheritor":
                Inheritor inheritor = (Inheritor) data;
                item.put("name", inheritor.getName());
                item.put("description", inheritor.getBio());
                break;
            case "resource":
                Resource resource = (Resource) data;
                item.put("title", resource.getTitle());
                item.put("name", resource.getTitle());
                item.put("description", resource.getDescription());
                break;
            case "qa":
                Qa qa = (Qa) data;
                item.put("question", qa.getQuestion());
                item.put("name", qa.getQuestion());
                item.put("description", qa.getAnswer());
                break;
        }
    }
}
