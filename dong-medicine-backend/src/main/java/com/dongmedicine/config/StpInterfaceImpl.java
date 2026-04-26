package com.dongmedicine.config;

import cn.dev33.satoken.stp.StpInterface;
import com.dongmedicine.entity.User;
import com.dongmedicine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();
        try {
            Integer userId;
            if (loginId instanceof Integer) {
                userId = (Integer) loginId;
            } else {
                userId = Integer.parseInt(loginId.toString());
            }
            User user = userService.getUserInfo(userId);
            if (user != null && user.getRole() != null) {
                roleList.add(user.getRole());
            }
        } catch (Exception e) {
            // ignore
        }
        return roleList;
    }
}
