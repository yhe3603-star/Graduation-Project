package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.entity.Inheritor;
import com.dongmedicine.service.InheritorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inheritors")
@RequiredArgsConstructor
public class InheritorController {

    private final InheritorService service;

    @GetMapping("/list")
    public R<List<Inheritor>> list(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "name") String sortBy) {
        return R.ok(service.listByLevel(level, sortBy));
    }

    @GetMapping("/search")
    public R<List<Inheritor>> search(@RequestParam String keyword) {
        return R.ok(service.search(keyword));
    }

    @GetMapping("/{id}")
    public R<Inheritor> detail(@PathVariable Integer id) {
        return R.ok(service.getDetailWithExtras(id));
    }

    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
