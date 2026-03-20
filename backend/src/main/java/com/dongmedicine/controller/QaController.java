package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.entity.Qa;
import com.dongmedicine.service.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService service;

    @GetMapping("/list")
    public R<List<Qa>> list(@RequestParam(required = false) String category) {
        return R.ok(category != null && !category.isEmpty() ? service.listByCategory(category) : service.list());
    }

    @GetMapping("/search")
    public R<List<Qa>> search(@RequestParam String keyword) {
        return R.ok(service.search(keyword));
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
