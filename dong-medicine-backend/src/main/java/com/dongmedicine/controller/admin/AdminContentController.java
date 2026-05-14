package com.dongmedicine.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.common.R;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.*;
import com.dongmedicine.entity.*;
import com.dongmedicine.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "后台管理-内容", description = "管理员内容CRUD管理")
@RestController
@RequestMapping("/api/admin")
@Validated
@SaCheckRole("admin")
@RequiredArgsConstructor
public class AdminContentController {

    private final InheritorService inheritorService;
    private final KnowledgeService knowledgeService;
    private final PlantService plantService;
    private final QaService qaService;
    private final ResourceService resourceService;

    private <T> R<Map<String, Object>> listPaged(IService<T> service, LambdaQueryWrapper<T> query, Integer page, Integer size) {
        return R.ok(PageUtils.toMap(service.page(PageUtils.getPage(page, size), query)));
    }

    // ========== 传承人 ==========

    @Operation(summary = "获取传承人列表")
    @GetMapping("/inheritors")
    public R<Map<String, Object>> listInheritors(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        return listPaged(inheritorService, new LambdaQueryWrapper<Inheritor>().orderByAsc(Inheritor::getId), page, size);
    }

    @Operation(summary = "新增传承人")
    @PostMapping("/inheritors")
    public R<String> createInheritor(@RequestBody @Valid InheritorCreateDTO dto) {
        Inheritor inheritor = new Inheritor();
        BeanUtils.copyProperties(dto, inheritor);
        inheritorService.save(inheritor);
        inheritorService.clearCache();
        return R.ok("新增传承人成功");
    }

    @Operation(summary = "更新传承人")
    @PutMapping("/inheritors/{id}")
    public R<String> updateInheritor(@PathVariable @NotNull Integer id, @RequestBody @Valid InheritorUpdateDTO dto) {
        Inheritor inheritor = new Inheritor();
        BeanUtils.copyProperties(dto, inheritor);
        inheritor.setId(id);
        inheritorService.updateById(inheritor);
        inheritorService.clearCache();
        return R.ok("更新传承人成功");
    }

    @Operation(summary = "删除传承人")
    @DeleteMapping("/inheritors/{id}")
    public R<String> deleteInheritor(@PathVariable @NotNull Integer id) {
        inheritorService.deleteWithFiles(id);
        return R.ok("删除传承人成功");
    }

    // ========== 知识库 ==========

    @Operation(summary = "获取知识库列表")
    @GetMapping("/knowledge")
    public R<Map<String, Object>> listKnowledge(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        return listPaged(knowledgeService, new LambdaQueryWrapper<Knowledge>().orderByAsc(Knowledge::getId), page, size);
    }

    @Operation(summary = "新增知识条目")
    @PostMapping("/knowledge")
    public R<String> createKnowledge(@RequestBody @Valid KnowledgeCreateDTO dto) {
        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(dto, knowledge);
        knowledgeService.save(knowledge);
        knowledgeService.clearCache();
        return R.ok("新增知识条目成功");
    }

    @Operation(summary = "更新知识条目")
    @PutMapping("/knowledge/{id}")
    public R<String> updateKnowledge(@PathVariable @NotNull Integer id, @RequestBody @Valid KnowledgeUpdateDTO dto) {
        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(dto, knowledge);
        knowledge.setId(id);
        knowledgeService.updateById(knowledge);
        knowledgeService.clearCache();
        return R.ok("更新知识条目成功");
    }

    @Operation(summary = "删除知识条目")
    @DeleteMapping("/knowledge/{id}")
    public R<String> deleteKnowledge(@PathVariable @NotNull Integer id) {
        knowledgeService.deleteWithFiles(id);
        return R.ok("删除知识条目成功");
    }

    // ========== 药用植物 ==========

    @Operation(summary = "获取植物列表")
    @GetMapping("/plants")
    public R<Map<String, Object>> listPlants(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        return listPaged(plantService, new LambdaQueryWrapper<Plant>().orderByAsc(Plant::getId), page, size);
    }

    @Operation(summary = "新增药用植物")
    @PostMapping("/plants")
    public R<String> createPlant(@RequestBody @Valid PlantCreateDTO dto) {
        Plant plant = new Plant();
        BeanUtils.copyProperties(dto, plant);
        plantService.save(plant);
        plantService.clearCache();
        return R.ok("新增药用植物成功");
    }

    @Operation(summary = "更新药用植物")
    @PutMapping("/plants/{id}")
    public R<String> updatePlant(@PathVariable @NotNull Integer id, @RequestBody @Valid PlantUpdateDTO dto) {
        Plant plant = new Plant();
        BeanUtils.copyProperties(dto, plant);
        plant.setId(id);
        plantService.updateById(plant);
        plantService.clearCache();
        return R.ok("更新药用植物成功");
    }

    @Operation(summary = "删除药用植物")
    @DeleteMapping("/plants/{id}")
    public R<String> deletePlant(@PathVariable @NotNull Integer id) {
        plantService.deleteWithFiles(id);
        return R.ok("删除药用植物成功");
    }

    // ========== 问答 ==========

    @Operation(summary = "获取问答列表")
    @GetMapping("/qa")
    public R<Map<String, Object>> listQa(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        return listPaged(qaService, new LambdaQueryWrapper<Qa>().orderByAsc(Qa::getId), page, size);
    }

    @Operation(summary = "新增问答")
    @PostMapping("/qa")
    public R<String> createQa(@RequestBody @Valid QaCreateDTO dto) {
        Qa qa = new Qa();
        BeanUtils.copyProperties(dto, qa);
        qaService.save(qa);
        return R.ok("新增问答成功");
    }

    @Operation(summary = "更新问答")
    @PutMapping("/qa/{id}")
    public R<String> updateQa(@PathVariable @NotNull Integer id, @RequestBody @Valid QaUpdateDTO dto) {
        Qa qa = new Qa();
        BeanUtils.copyProperties(dto, qa);
        qa.setId(id);
        qaService.updateById(qa);
        return R.ok("更新问答成功");
    }

    @Operation(summary = "删除问答")
    @DeleteMapping("/qa/{id}")
    public R<String> deleteQa(@PathVariable @NotNull Integer id) {
        qaService.removeById(id);
        return R.ok("删除问答成功");
    }

    // ========== 学习资源 ==========

    @Operation(summary = "获取资源列表")
    @GetMapping("/resources")
    public R<Map<String, Object>> listResources(
            @Parameter(name = "page", description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(name = "size", description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        return listPaged(resourceService, new LambdaQueryWrapper<Resource>().orderByAsc(Resource::getId), page, size);
    }

    @Operation(summary = "新增学习资源")
    @PostMapping("/resources")
    public R<String> createResource(@RequestBody @Valid ResourceCreateDTO dto) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        resourceService.save(resource);
        resourceService.clearCache();
        return R.ok("新增学习资源成功");
    }

    @Operation(summary = "更新学习资源")
    @PutMapping("/resources/{id}")
    public R<String> updateResource(@PathVariable @NotNull Integer id, @RequestBody @Valid ResourceUpdateDTO dto) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        resource.setId(id);
        resourceService.updateById(resource);
        resourceService.clearCache();
        return R.ok("更新学习资源成功");
    }

    @Operation(summary = "删除学习资源")
    @DeleteMapping("/resources/{id}")
    public R<String> deleteResource(@PathVariable @NotNull Integer id) {
        resourceService.deleteWithFiles(id);
        return R.ok("删除学习资源成功");
    }
}
