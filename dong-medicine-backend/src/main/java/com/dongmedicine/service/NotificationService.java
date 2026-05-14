package com.dongmedicine.service;

import java.util.List;

public interface NotificationService {

    List<String> getNotifications(Integer userId);

    int getUnreadCount(Integer userId);

    void markAllRead(Integer userId);
}
