package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.User;
import java.util.List;

public interface UserService extends IService<User> {

    String login(String username, String password);

    void register(String username, String password);

    User getUserInfo(Integer userId);

    User getUserByUsername(String username);

    void deleteUser(Integer userId);

    List<User> getAllUsers();

    void changePassword(Integer userId, String currentPassword, String newPassword);

    void updateUserRole(Integer userId, String role);
}
