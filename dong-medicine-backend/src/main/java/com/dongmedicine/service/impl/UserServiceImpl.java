package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.User;
import com.dongmedicine.mapper.UserMapper;
import com.dongmedicine.service.UserService;
import com.dongmedicine.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new RuntimeException("用户名和密码不能为空");
        }
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }
        return jwtUtil.generateToken(username, user.getId(), user.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new RuntimeException("用户名和密码不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new RuntimeException("用户名长度必须在3-20个字符之间");
        }
        if (password.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        if (getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("user");
        user.setCreatedAt(LocalDateTime.now());
        save(user);
    }

    @Override
    @Cacheable(value = "users", key = "#userId")
    public User getUserInfo(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPasswordHash(null);
        return user;
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPasswordHash(null);
        return user;
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (!removeById(userId)) {
            throw new RuntimeException("删除用户失败");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return list(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getPasswordHash, User::getRole, User::getCreatedAt)
                .orderByDesc(User::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", key = "#userId")
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
            throw new RuntimeException("密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new RuntimeException("新密码长度必须在6-20个字符之间");
        }
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("当前密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        updateById(user);
    }
}
