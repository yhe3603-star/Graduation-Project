<template>
  <div class="admin-sidebar">
    <div class="sidebar-header">
      <div class="admin-logo">
        <el-icon :size="28">
          <Setting />
        </el-icon>
      </div>
      <div class="admin-title">
        <span class="title-text">管理后台</span>
        <span class="title-sub">Admin Panel</span>
      </div>
    </div>
    
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="transparent"
      text-color="rgba(255,255,255,0.8)"
      active-text-color="var(--text-inverse)"
      @select="$emit('update:activeMenu', $event)"
    >
      <el-menu-item index="dashboard">
        <el-icon><DataLine /></el-icon>
        <span>数据概览</span>
      </el-menu-item>
      <el-menu-item index="users">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="knowledge">
        <el-icon><Document /></el-icon>
        <span>知识管理</span>
      </el-menu-item>
      <el-menu-item index="inheritors">
        <el-icon><Avatar /></el-icon>
        <span>传承人管理</span>
      </el-menu-item>
      <el-menu-item index="plants">
        <el-icon><Picture /></el-icon>
        <span>植物管理</span>
      </el-menu-item>
      <el-menu-item index="qa">
        <el-icon><ChatDotRound /></el-icon>
        <span>问答管理</span>
      </el-menu-item>
      <el-menu-item index="resources">
        <el-icon><Folder /></el-icon>
        <span>资源管理</span>
      </el-menu-item>
      <el-menu-item index="quiz">
        <el-icon><EditPen /></el-icon>
        <span>答题管理</span>
      </el-menu-item>
      <el-menu-item index="comments">
        <el-icon><ChatDotRound /></el-icon>
        <span>评论管理</span>
      </el-menu-item>
      <el-menu-item index="feedback">
        <el-icon><ChatLineSquare /></el-icon>
        <span>反馈管理</span>
      </el-menu-item>
      <el-menu-item index="logs">
        <el-icon><Tickets /></el-icon>
        <span>日志管理</span>
      </el-menu-item>
    </el-menu>

    <div class="sidebar-footer">
      <div class="admin-user">
        <el-avatar :size="36">
          {{ userName?.charAt(0) }}
        </el-avatar>
        <div class="user-info">
          <span class="user-name">{{ userName }}</span>
          <span class="user-role">管理员</span>
        </div>
      </div>
      <el-button
        type="danger"
        text
        :loading="logoutLoading"
        @click="$emit('logout')"
      >
        <el-icon><SwitchButton /></el-icon>
        退出
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { Setting, DataLine, User, Document, Avatar, Picture, ChatDotRound, Folder, SwitchButton, EditPen, ChatLineSquare, Tickets } from "@element-plus/icons-vue";

defineProps({
  activeMenu: String,
  userName: String,
  logoutLoading: Boolean
});

defineEmits(["update:active-menu", "logout"]);
</script>

<style scoped>
.admin-sidebar {
  width: 240px;
  background: linear-gradient(180deg, var(--dong-blue), var(--dong-indigo-dark));
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  height: 100vh;
  overflow: hidden;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.admin-logo {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.admin-title {
  display: flex;
  flex-direction: column;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-inverse);
}

.title-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding: 12px;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 4px;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.15);
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.admin-user .el-avatar {
  background: linear-gradient(135deg, var(--dong-green), var(--dong-jade-dark));
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-inverse);
}

.user-role {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}
</style>
