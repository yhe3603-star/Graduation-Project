package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Favorite;
import java.util.List;
import java.util.Map;

public interface FavoriteService extends IService<Favorite> {
    void addFavorite(Integer userId, String targetType, Integer targetId);
    void removeFavorite(Integer userId, String targetType, Integer targetId);
    List<Map<String, Object>> getMyFavorites(Integer userId);
    Page<Map<String, Object>> getMyFavoritesPaged(Integer userId, Integer page, Integer size);
}