package com.dongmedicine.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动时初始化一个默认管理员账号：
 * 用户名：admin
 * 密码：从环境变量ADMIN_INIT_PASSWORD获取，默认123456
 *
 * 仅在不存在任何 ADMIN 角色用户时创建，方便本地调试与毕业设计演示。
 */
@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456";
    
    private final UserService userService;

    public AdminDataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User existing = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, DEFAULT_ADMIN_USERNAME));

        if (existing != null) {
            return;
        }

        String initPassword = System.getenv("ADMIN_INIT_PASSWORD");
        if (initPassword == null || initPassword.isBlank()) {
            initPassword = DEFAULT_ADMIN_PASSWORD;
        }
        User admin = new User();
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setPasswordHash(encoder.encode(initPassword));
        admin.setRole(RoleConstants.ROLE_ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userService.save(admin);
    }
}
