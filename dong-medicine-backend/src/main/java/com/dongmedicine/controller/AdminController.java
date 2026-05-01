package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.dto.*;
import com.dongmedicine.entity.*;
import com.dongmedicine.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "后台管理", description = "管理员数据管理台")
@RestController
@RequestMapping("/api/admin")
@Validated
@SaCheckRole("admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final InheritorService inheritorService;
    private final KnowledgeService knowledgeService;
    private final PlantService plantService;
    private final QaService qaService;
    private final ResourceService resourceService;
    private final FeedbackService feedbackService;
    private final CommentService commentService;
    private final QuizService quizService;

    @GetMapping("/users")
    public R<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<User> pageResult = userService.page(PageUtils.getPage(page, size));
        pageResult.getRecords().forEach(u -> u.setPasswordHash(null));
        return R.ok(PageUtils.toMap(pageResult));
    }

    @DeleteMapping("/users/{id}")
    public R<String> deleteUser(@PathVariable @NotNull Integer id) {
        userService.deleteUser(id);
        return R.ok("删除用户成功");
    }

    @PutMapping("/users/{id}/role")
    public R<String> updateUserRole(@PathVariable @NotNull Integer id,
                                    @RequestParam @NotBlank(message = "角色不能为空") String role) {
        userService.updateUserRole(id, role);
        return R.ok("角色更新成功");
    }

    @PutMapping("/users/{id}/ban")
    public R<String> banUser(@PathVariable @NotNull Integer id,
                             @RequestParam(required = false) String reason) {
        userService.banUser(id, reason);
        return R.ok("用户已被封禁");
    }

    @PutMapping("/users/{id}/unban")
    public R<String> unbanUser(@PathVariable @NotNull Integer id) {
        userService.unbanUser(id);
        return R.ok("用户已解封");
    }

    @GetMapping("/inheritors")
    public R<Map<String, Object>> listInheritors(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Inheritor> pageResult = inheritorService.page(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<Inheritor>().orderByAsc(Inheritor::getId));
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PostMapping("/inheritors")
    public R<String> createInheritor(@RequestBody @Valid InheritorCreateDTO dto) {
        Inheritor inheritor = new Inheritor();
        BeanUtils.copyProperties(dto, inheritor);
        inheritorService.save(inheritor);
        inheritorService.clearCache();
        return R.ok("新增传承人成功");
    }

    @PutMapping("/inheritors/{id}")
    public R<String> updateInheritor(@PathVariable @NotNull Integer id, @RequestBody @Valid InheritorUpdateDTO dto) {
        Inheritor inheritor = new Inheritor();
        BeanUtils.copyProperties(dto, inheritor);
        inheritor.setId(id);
        inheritorService.updateById(inheritor);
        inheritorService.clearCache();
        return R.ok("更新传承人成功");
    }

    @DeleteMapping("/inheritors/{id}")
    public R<String> deleteInheritor(@PathVariable @NotNull Integer id) {
        inheritorService.deleteWithFiles(id);
        return R.ok("删除传承人成功");
    }

    @GetMapping("/knowledge")
    public R<Map<String, Object>> listKnowledge(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Knowledge> pageResult = knowledgeService.page(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<Knowledge>().orderByAsc(Knowledge::getId));
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PostMapping("/knowledge")
    public R<String> createKnowledge(@RequestBody @Valid KnowledgeCreateDTO dto) {
        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(dto, knowledge);
        knowledgeService.save(knowledge);
        knowledgeService.clearCache();
        return R.ok("新增知识条目成功");
    }

    @PutMapping("/knowledge/{id}")
    public R<String> updateKnowledge(@PathVariable @NotNull Integer id, @RequestBody @Valid KnowledgeUpdateDTO dto) {
        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(dto, knowledge);
        knowledge.setId(id);
        knowledgeService.updateById(knowledge);
        knowledgeService.clearCache();
        return R.ok("更新知识条目成功");
    }

    @DeleteMapping("/knowledge/{id}")
    public R<String> deleteKnowledge(@PathVariable @NotNull Integer id) {
        knowledgeService.deleteWithFiles(id);
        return R.ok("删除知识条目成功");
    }

    @GetMapping("/plants")
    public R<Map<String, Object>> listPlants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Plant> pageResult = plantService.page(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<Plant>().orderByAsc(Plant::getId));
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PostMapping("/plants")
    public R<String> createPlant(@RequestBody @Valid PlantCreateDTO dto) {
        Plant plant = new Plant();
        BeanUtils.copyProperties(dto, plant);
        plantService.save(plant);
        plantService.clearCache();
        return R.ok("新增药用植物成功");
    }

    @PutMapping("/plants/{id}")
    public R<String> updatePlant(@PathVariable @NotNull Integer id, @RequestBody @Valid PlantUpdateDTO dto) {
        Plant plant = new Plant();
        BeanUtils.copyProperties(dto, plant);
        plant.setId(id);
        plantService.updateById(plant);
        plantService.clearCache();
        return R.ok("更新药用植物成功");
    }

    @DeleteMapping("/plants/{id}")
    public R<String> deletePlant(@PathVariable @NotNull Integer id) {
        plantService.deleteWithFiles(id);
        return R.ok("删除药用植物成功");
    }

    @GetMapping("/qa")
    public R<Map<String, Object>> listQa(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Qa> pageResult = qaService.page(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<Qa>().orderByAsc(Qa::getId));
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PostMapping("/qa")
    public R<String> createQa(@RequestBody @Valid QaCreateDTO dto) {
        Qa qa = new Qa();
        BeanUtils.copyProperties(dto, qa);
        qaService.save(qa);
        return R.ok("新增问答成功");
    }

    @PutMapping("/qa/{id}")
    public R<String> updateQa(@PathVariable @NotNull Integer id, @RequestBody @Valid QaUpdateDTO dto) {
        Qa qa = new Qa();
        BeanUtils.copyProperties(dto, qa);
        qa.setId(id);
        qaService.updateById(qa);
        return R.ok("更新问答成功");
    }

    @DeleteMapping("/qa/{id}")
    public R<String> deleteQa(@PathVariable @NotNull Integer id) {
        qaService.removeById(id);
        return R.ok("删除问答成功");
    }

    @GetMapping("/resources")
    public R<Map<String, Object>> listResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Resource> pageResult = resourceService.page(PageUtils.getPage(page, size),
                new LambdaQueryWrapper<Resource>().orderByAsc(Resource::getId));
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PostMapping("/resources")
    public R<String> createResource(@RequestBody @Valid ResourceCreateDTO dto) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        resourceService.save(resource);
        resourceService.clearCache();
        return R.ok("新增学习资源成功");
    }

    @PutMapping("/resources/{id}")
    public R<String> updateResource(@PathVariable @NotNull Integer id, @RequestBody @Valid ResourceUpdateDTO dto) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        resource.setId(id);
        resourceService.updateById(resource);
        resourceService.clearCache();
        return R.ok("更新学习资源成功");
    }

    @DeleteMapping("/resources/{id}")
    public R<String> deleteResource(@PathVariable @NotNull Integer id) {
        resourceService.deleteWithFiles(id);
        return R.ok("删除学习资源成功");
    }

    @GetMapping("/feedback")
    public R<Map<String, Object>> listFeedback(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .orderByDesc(Feedback::getCreatedAt);
        if (!"all".equalsIgnoreCase(status)) {
            wrapper.eq(Feedback::getStatus, status);
        }
        Page<Feedback> pageResult = feedbackService.page(PageUtils.getPage(page, size), wrapper);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PutMapping("/feedback/{id}/reply")
    public R<String> replyFeedback(@PathVariable @NotNull Integer id, @RequestBody @Valid FeedbackReplyDTO dto) {
        feedbackService.replyFeedback(id, dto.getReply().trim());
        return R.ok("回复成功");
    }

    @DeleteMapping("/feedback/{id}")
    public R<String> deleteFeedback(@PathVariable @NotNull Integer id) {
        feedbackService.removeById(id);
        return R.ok("删除反馈成功");
    }

    @GetMapping("/comments")
    public R<Map<String, Object>> listComments(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<CommentDTO> pageResult = commentService.pageAllDTO(status, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    @PutMapping("/comments/{id}/approve")
    public R<String> approveComment(@PathVariable @NotNull Integer id) {
        commentService.approveComment(id);
        return R.ok("审核通过");
    }

    @PutMapping("/comments/{id}/reject")
    public R<String> rejectComment(@PathVariable @NotNull Integer id) {
        commentService.rejectComment(id);
        return R.ok("已拒绝");
    }

    @DeleteMapping("/comments/{id}")
    public R<String> deleteComment(@PathVariable @NotNull Integer id) {
        commentService.removeById(id);
        return R.ok("删除评论成功");
    }

    @GetMapping("/stats")
    public R<Map<String, Long>> stats() {
        Map<String, Long> data = new HashMap<>();
        data.put("users", userService.count());
        data.put("knowledge", knowledgeService.count());
        data.put("inheritors", inheritorService.count());
        data.put("plants", plantService.count());
        data.put("qa", qaService.count());
        data.put("resources", resourceService.count());
        data.put("quiz", quizService.countQuestions());
        data.put("comments", commentService.count());
        data.put("feedback", feedbackService.count());
        return R.ok(data);
    }

    @GetMapping("/stats/plants-distribution")
    public R<List<Map<String, Object>>> getPlantDistribution() {
        return R.ok(plantService.listMaps(new QueryWrapper<Plant>()
                .select("distribution as name", "count(*) as value")
                .groupBy("distribution")
                .orderByDesc("distribution")));
    }

    @GetMapping("/stats/knowledge-popularity")
    public R<List<Map<String, Object>>> getKnowledgePopularity() {
        return R.ok(knowledgeService.listMaps(new QueryWrapper<Knowledge>()
                .select("title as name", "popularity as value")
                .orderByDesc("popularity")
                .last("LIMIT 10")));
    }
}
