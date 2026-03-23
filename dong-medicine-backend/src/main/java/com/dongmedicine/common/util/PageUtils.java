package com.dongmedicine.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.Map;

public class PageUtils {

    /**
     * Create a standardized pagination object with bounds checking
     * @param page Current page number
     * @param size Page size
     * @param <T>  Entity type
     * @return Normalized Page object
     */
    public static <T> Page<T> getPage(Integer page, Integer size) {
        int normalizedPage = Math.max(page != null ? page : 1, 1);
        int normalizedSize = Math.min(Math.max(size != null ? size : 20, 1), 100);
        return new Page<>(normalizedPage, normalizedSize);
    }

    /**
     * Convert Page result to a Map structure required by frontend
     * @param pageResult MyBatis-Plus Page result
     * @return Map containing pagination data
     */
    public static Map<String, Object> toMap(Page<?> pageResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return data;
    }
}
