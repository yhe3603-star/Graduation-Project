package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.service.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService service;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String category) {
        Page<Qa> pageResult = service.pageByCategory(category, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<Qa> pageResult = service.searchPaged(keyword, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
