package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.*;
import com.dongmedicine.mapper.*;
import com.dongmedicine.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            throw new RuntimeException("已经收藏过了");
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
        try {
            switch (targetType) {
                case "plant": plantMapper.incrementFavoriteCount(targetId, delta); break;
                case "knowledge": knowledgeMapper.incrementFavoriteCount(targetId, delta); break;
                case "inheritor": inheritorMapper.incrementFavoriteCount(targetId, delta); break;
                case "resource": resourceMapper.incrementFavoriteCount(targetId, delta); break;
                case "qa": qaMapper.incrementFavoriteCount(targetId, delta); break;
            }
        } catch (Exception e) {
            log.error("Failed to increment favorite count for type={}, id={}", targetType, targetId, e);
        }
    }

    @Override
    public List<Map<String, Object>> getMyFavorites(Integer userId) {
        List<Favorite> favorites = list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite f : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", f.getId());
            item.put("type", f.getTargetType());
            item.put("targetId", f.getTargetId());
            item.put("createTime", f.getCreatedAt());

            fillFavoriteItem(item, f.getTargetType(), f.getTargetId());
            result.add(item);
        }
        return result;
    }

    private void fillFavoriteItem(Map<String, Object> item, String targetType, Integer targetId) {
        switch (targetType) {
            case "plant":
                Plant plant = plantMapper.selectById(targetId);
                if (plant != null) {
                    item.put("nameCn", plant.getNameCn());
                    item.put("name", plant.getNameCn());
                    item.put("description", plant.getEfficacy());
                }
                break;
            case "knowledge":
                Knowledge knowledge = knowledgeMapper.selectById(targetId);
                if (knowledge != null) {
                    item.put("title", knowledge.getTitle());
                    item.put("name", knowledge.getTitle());
                    item.put("description", knowledge.getContent());
                }
                break;
            case "inheritor":
                Inheritor inheritor = inheritorMapper.selectById(targetId);
                if (inheritor != null) {
                    item.put("name", inheritor.getName());
                    item.put("description", inheritor.getBio());
                }
                break;
            case "resource":
                Resource resource = resourceMapper.selectById(targetId);
                if (resource != null) {
                    item.put("title", resource.getTitle());
                    item.put("name", resource.getTitle());
                    item.put("description", resource.getDescription());
                }
                break;
            case "qa":
                Qa qa = qaMapper.selectById(targetId);
                if (qa != null) {
                    item.put("question", qa.getQuestion());
                    item.put("name", qa.getQuestion());
                    item.put("description", qa.getAnswer());
                }
                break;
        }
    }
}
