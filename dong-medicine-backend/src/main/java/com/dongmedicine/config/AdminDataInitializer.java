package com.dongmedicine.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongmedicine.common.constant.RoleConstants;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    
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
            System.err.println("警告: 未设置 ADMIN_INIT_PASSWORD 环境变量，跳过默认管理员创建。请设置环境变量后重启应用。");
            return;
        }
        User admin = new User();
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setPasswordHash(encoder.encode(initPassword));
        admin.setRole(RoleConstants.ROLE_ADMIN);
        admin.setStatus(User.STATUS_ACTIVE);
        userService.save(admin);
        System.out.println("默认管理员账号创建成功: " + DEFAULT_ADMIN_USERNAME);
    }
}
