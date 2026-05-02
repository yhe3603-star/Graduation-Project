package com.dongmedicine.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "后台管理-用户", description = "管理员用户管理")
@RestController
@RequestMapping("/api/admin")
@Validated
@SaCheckRole("admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

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
}
