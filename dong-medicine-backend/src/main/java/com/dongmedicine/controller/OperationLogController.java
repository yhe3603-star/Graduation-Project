package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dongmedicine.common.R;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.service.OperationLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "操作日志", description = "后台操作日志查询")
@RestController
@RequestMapping("/api/admin/logs")
@SaCheckRole("admin")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService logService;

    @Operation(summary = "获取操作日志列表")
    @GetMapping("/list")
    public R<List<OperationLog>> list(
            @Parameter(name = "module", description = "模块名称") @RequestParam(required = false) String module,
            @Parameter(name = "type", description = "操作类型") @RequestParam(required = false) String type,
            @Parameter(name = "username", description = "用户名") @RequestParam(required = false) String username,
            @Parameter(name = "limit", description = "返回数量", example = "100") @RequestParam(defaultValue = "100") Integer limit) {
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        if (module != null && !module.isEmpty()) wrapper.eq("module", module);
        if (type != null && !type.isEmpty()) wrapper.eq("type", type);
        if (username != null && !username.isEmpty()) wrapper.like("username", username);
        int safeLimit = Math.min(Math.max(limit == null ? 100 : limit, 1), 100);
        wrapper.last("LIMIT " + safeLimit);
        wrapper.orderByDesc("created_at");
        return R.ok(logService.list(wrapper));
    }

    @Operation(summary = "获取操作日志详情")
    @GetMapping("/{id}")
    public R<OperationLog> getById(@Parameter(name = "id", description = "日志ID") @PathVariable Integer id) {
        return R.ok(logService.getById(id));
    }

    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    public R<String> delete(@Parameter(name = "id", description = "日志ID") @PathVariable Integer id) {
        logService.removeById(id);
        return R.ok("删除成功");
    }

    @Operation(summary = "批量删除操作日志")
    @DeleteMapping("/batch")
    public R<String> batchDelete(@RequestBody Integer[] ids) {
        Arrays.stream(ids).forEach(logService::removeById);
        return R.ok("批量删除成功");
    }

    @Operation(summary = "清空操作日志")
    @DeleteMapping("/clear")
    public R<String> clearAll() {
        logService.clearAll();
        return R.ok("清空成功");
    }

    @Operation(summary = "获取操作日志统计")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        QueryWrapper<OperationLog> groupedWrapper = new QueryWrapper<>();
        groupedWrapper.select("type", "COUNT(1) AS total").groupBy("type");
        List<Map<String, Object>> grouped = logService.listMaps(groupedWrapper);
        Map<String, Long> typeCounts = new HashMap<>();
        for (Map<String, Object> item : grouped) {
            String key = String.valueOf(item.get("type"));
            Object value = item.get("total");
            long count = value instanceof Number ? ((Number) value).longValue() : 0L;
            typeCounts.put(key, count);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", logService.count());
        result.put("CREATE", typeCounts.getOrDefault("CREATE", 0L));
        result.put("UPDATE", typeCounts.getOrDefault("UPDATE", 0L));
        result.put("DELETE", typeCounts.getOrDefault("DELETE", 0L));
        result.put("QUERY", typeCounts.getOrDefault("QUERY", 0L));
        return R.ok(result);
    }
}
