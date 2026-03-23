package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Qa;
import java.util.List;

public interface QaService extends IService<Qa> {
    List<Qa> listByCategory(String category);
    Page<Qa> pageByCategory(String category, Integer page, Integer size);
    List<Qa> search(String keyword);
    Page<Qa> searchPaged(String keyword, Integer page, Integer size);
    void incrementViewCount(Integer id);
}
