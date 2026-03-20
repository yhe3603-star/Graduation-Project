package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Inheritor;
import java.util.List;

public interface InheritorService extends IService<Inheritor> {
    List<Inheritor> listByLevel(String level, String sortBy);
    Inheritor getDetailWithExtras(Integer id);
    List<Inheritor> search(String keyword);
    void incrementViewCount(Integer id);
    void clearCache();
    void deleteWithFiles(Integer id);
}
