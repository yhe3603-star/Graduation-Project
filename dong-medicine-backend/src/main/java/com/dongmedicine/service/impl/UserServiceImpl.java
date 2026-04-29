package com.dongmedicine.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.UserService;
import com.dongmedicine.common.SecurityUtils;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.PasswordValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        if (user.isBanned()) {
            throw BusinessException.forbidden("该用户已被封禁");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }

        StpUtil.login(String.valueOf(user.getId()));
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());

        return StpUtil.getTokenValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new BusinessException(ErrorCode.USERNAME_TOO_SHORT);
        }
        
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(password);
        if (!validationResult.isValid()) {
            throw BusinessException.passwordTooWeak();
        }
        
        if (getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) {
            throw BusinessException.userAlreadyExists();
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(RoleConstants.ROLE_USER);
        user.setStatus(User.STATUS_ACTIVE);
        save(user);
    }

    @Override
    public User getUserInfo(Integer userId) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw BusinessException.badRequest("用户名不能为空");
        }
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        user.setPasswordHash(null);
        return user;
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Integer userId) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        Integer currentId = SecurityUtils.getCurrentUserId();
        if (currentId != null && currentId.equals(userId)) {
            throw BusinessException.forbidden("不能删除当前登录账号");
        }
        User target = getById(userId);
        if (target == null) {
            throw BusinessException.userNotFound();
        }
        if (RoleConstants.ROLE_ADMIN.equals(target.getRole()) && countAdmins() <= 1) {
            throw BusinessException.forbidden("不能删除系统唯一的管理员账号");
        }
        if (!removeById(userId)) {
            throw BusinessException.notFound("删除用户失败");
        }
    }

    private long countAdmins() {
        return count(new LambdaQueryWrapper<User>().eq(User::getRole, RoleConstants.ROLE_ADMIN));
    }

    @Override
    public List<User> getAllUsers() {
        return list(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getRole, User::getStatus, User::getCreatedAt)
                .orderByDesc(User::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
            throw BusinessException.badRequest("密码不能为空");
        }
        
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(newPassword);
        if (!validationResult.isValid()) {
            throw BusinessException.passwordTooWeak();
        }
        
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void updateUserRole(Integer userId, String role) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        if (!RoleConstants.isValid(role)) {
            throw BusinessException.badRequest("角色值无效，只能是 user 或 admin");
        }
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        if (RoleConstants.ROLE_ADMIN.equals(user.getRole())
                && !RoleConstants.ROLE_ADMIN.equals(role)
                && countAdmins() <= 1) {
            throw BusinessException.forbidden("不能移除系统唯一的管理员角色");
        }
        user.setRole(role);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void banUser(Integer userId, String reason) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        Integer currentId = SecurityUtils.getCurrentUserId();
        if (currentId != null && currentId.equals(userId)) {
            throw BusinessException.forbidden("不能封禁当前登录账号");
        }
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        if (RoleConstants.ROLE_ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("不能封禁管理员账号");
        }
        user.setStatus(User.STATUS_BANNED);
        updateById(user);
        StpUtil.kickout(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void unbanUser(Integer userId) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        user.setStatus(User.STATUS_ACTIVE);
        updateById(user);
    }

    @Override
    public String getUserToken(Integer userId) {
        // 管理员权限检查：仅admin角色可获取其他用户Token
        StpUtil.checkRole("admin");

        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        StpUtil.login(String.valueOf(user.getId()));
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());

        // 安全审计日志：记录管理员获取用户Token的操作
        log.warn("[安全审计] 管理员获取用户Token - 操作人ID:{}, 目标用户ID:{}, 目标用户名:{}",
            StpUtil.getLoginIdAsString(), user.getId(), user.getUsername());

        return StpUtil.getTokenValue();
    }
}
