package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Qa;
import java.util.List;

public interface QaService extends IService<Qa> {
    List<Qa> listByCategory(String category);
    List<Qa> search(String keyword);
    void incrementViewCount(Integer id);
}
