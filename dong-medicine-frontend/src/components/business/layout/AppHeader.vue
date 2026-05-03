<template>
  <header class="dong-header">
    <div
      class="logo-area"
      @click="$router.push('/')"
    >
      <div class="logo-icon">
        <svg
          viewBox="0 0 40 40"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M20 4L22 12L30 14L22 16L20 24L18 16L10 14L18 12Z"
            fill="currentColor"
            opacity="0.9"
          />
          <circle
            cx="20"
            cy="20"
            r="6"
            stroke="currentColor"
            stroke-width="1.5"
            fill="none"
            opacity="0.6"
          />
        </svg>
      </div>
      <div class="logo-text">
        <span class="logo-title">侗乡医药</span>
        <span class="logo-sub">非遗数字展示平台</span>
      </div>
    </div>

    <nav class="nav-menu desktop-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: activeIndex === item.path }"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </router-link>
      <el-dropdown
        trigger="hover"
        class="nav-dropdown"
      >
        <span
          class="nav-item more-trigger"
          :class="{ active: morePages.includes(activeIndex) }"
        >
          <el-icon><More /></el-icon>
          <span>更多</span>
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="item in moreItems"
              :key="item.path"
              @click="$router.push(item.path)"
            >
              <el-icon><component :is="item.icon" /></el-icon>{{ item.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </nav>

    <div class="header-actions">
      <div class="search-box desktop-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索知识、植物、传承人..."
          clearable
          @focus="goToSearch"
          @keyup.enter="doSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-popover
        v-if="isLoggedIn"
        placement="bottom"
        :width="320"
        trigger="click"
        @show="loadNotifications"
      >
        <template #reference>
          <el-badge
            :value="unreadCount"
            :hidden="unreadCount === 0"
            :max="99"
            class="notification-badge"
          >
            <el-button
              class="action-btn"
              circle
            >
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
        </template>
        <div class="notification-panel">
          <div class="notification-header">
            <span>消息通知</span>
            <el-button
              v-if="unreadCount > 0"
              type="primary"
              link
              size="small"
              @click="markAllRead"
            >全部已读</el-button>
          </div>
          <div class="notification-list">
            <div
              v-if="notifications.length === 0"
              class="notification-empty"
            >
              暂无通知
            </div>
            <div
              v-for="(item, i) in notifications"
              :key="i"
              class="notification-item"
            >
              <div class="notification-title">
                {{ item.title }}
              </div>
              <div class="notification-content">
                {{ item.content }}
              </div>
              <div class="notification-time">
                {{ item.createTime }}
              </div>
            </div>
          </div>
        </div>
      </el-popover>
      <el-tooltip
        content="返回上一页"
        placement="bottom"
      >
        <el-button
          class="action-btn"
          circle
          @click="$router.go(-1)"
        >
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </el-tooltip>
      
      <el-dropdown
        trigger="click"
        class="user-dropdown"
      >
        <div class="user-avatar-wrap">
          <el-avatar
            :size="36"
            class="user-avatar"
          >
            {{ userName?.charAt(0) || '游' }}
          </el-avatar>
          <div class="user-info desktop-user-info">
            <span class="user-name">{{ userName || '游客' }}</span>
            <span
              v-if="isLoggedIn && isAdmin"
              class="user-role"
            >管理员</span>
          </div>
          <el-icon class="dropdown-arrow desktop-arrow">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <template v-if="isLoggedIn">
              <el-dropdown-item @click="$router.push('/personal')">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item
                v-if="isAdmin"
                @click="$router.push('/admin')"
              >
                <el-icon><Setting /></el-icon>管理后台
              </el-dropdown-item>
              <el-dropdown-item
                divided
                @click="$emit('logout')"
              >
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </template>
            <template v-else>
              <el-dropdown-item @click="$emit('showLogin')">
                <el-icon><User /></el-icon>登录
              </el-dropdown-item>
              <el-dropdown-item @click="$emit('showRegister')">
                <el-icon><User /></el-icon>注册
              </el-dropdown-item>
            </template>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-dropdown
        trigger="click"
        class="mobile-menu-btn"
      >
        <el-button
          class="action-btn"
          circle
        >
          <el-icon><Menu /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu class="mobile-menu">
            <el-dropdown-item
              v-for="item in allNavItems"
              :key="item.path"
              :class="{ 'is-active': activeIndex === item.path }"
              @click="$router.push(item.path)"
            >
              <el-icon><component :is="item.icon" /></el-icon>{{ item.label }}
            </el-dropdown-item>
            <el-dropdown-item
              divided
              @click="goToSearch"
            >
              <el-icon><Search /></el-icon>搜索
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { HomeFilled, Document, User, Picture, ChatDotRound, More, ArrowDown, Search, ArrowLeft, Setting, SwitchButton, Aim, Folder, DataLine, InfoFilled, Menu, Calendar, Bell } from "@element-plus/icons-vue";
import request from "@/utils/request";

const route = useRoute();
const router = useRouter();

const props = defineProps({ isLoggedIn: Boolean, userName: String, isAdmin: Boolean });
defineEmits(["logout", "showLogin", "showRegister"]);

const activeIndex = computed(() => route.path);
const searchKeyword = ref("");

const navItems = [
  { path: "/", label: "首页", icon: HomeFilled },
  { path: "/knowledge", label: "知识库", icon: Document },
  { path: "/inheritors", label: "传承人", icon: User },
  { path: "/plants", label: "药用图鉴", icon: Picture },
  { path: "/resources", label: "学习资源", icon: Folder },
  { path: "/qa", label: "问答", icon: ChatDotRound },
  { path: "/interact", label: "文化互动", icon: Aim },
  { path: "/visual", label: "数据可视化", icon: DataLine }
];

const moreItems = [
  { path: "/feedback", label: "意见反馈", icon: InfoFilled },
  { path: "/about", label: "关于非遗", icon: InfoFilled }
];

const morePages = computed(() => moreItems.map(item => item.path));
const allNavItems = [...navItems, ...moreItems];

// Notifications
const notifications = ref([]);
const unreadCount = ref(0);

const loadNotifications = async () => {
  try {
    const [listRes, countRes] = await Promise.all([
      request.get('/notifications'),
      request.get('/notifications/unread-count')
    ]);
    notifications.value = (listRes.data || listRes || []).map(s => {
      try { return JSON.parse(s); } catch { return { title: '通知', content: s, createTime: '' }; }
    });
    unreadCount.value = (countRes.data || countRes || {}).count || 0;
  } catch {}
};

const markAllRead = async () => {
  try {
    await request.post('/notifications/read');
    unreadCount.value = 0;
  } catch {}
};

onMounted(() => {
  if (props.isLoggedIn) loadNotifications();
});

const doSearch = () => {
  if (searchKeyword.value.trim()) router.push({ path: "/search", query: { q: searchKeyword.value.trim() } });
  else router.push("/search");
};

const goToSearch = () => {
  router.push("/search");
};
</script>

<style scoped>
.dong-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  background:
    /* 侗锦纹样叠加 */
    repeating-linear-gradient(
      45deg,
      transparent 0px,
      transparent 8px,
      rgba(255, 255, 255, 0.015) 8px,
      rgba(255, 255, 255, 0.015) 10px
    ),
    repeating-linear-gradient(
      -45deg,
      transparent 0px,
      transparent 8px,
      rgba(255, 255, 255, 0.015) 8px,
      rgba(255, 255, 255, 0.015) 10px
    ),
    linear-gradient(135deg, #0a2a3f 0%, var(--dong-indigo) 40%, var(--dong-indigo-dark) 100%);
  color: var(--text-inverse);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
  border-bottom: 2px solid rgba(201, 162, 39, 0.3);
}

.logo-area { display: flex; align-items: center; gap: 12px; cursor: pointer; flex-shrink: 0; }
.logo-icon { width: 38px; height: 38px; color: var(--text-inverse); filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3)); }
.logo-text { display: flex; flex-direction: column; }
.logo-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #fff 0%, #e8d5a3 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.logo-sub { font-size: 10px; opacity: 0.65; letter-spacing: 1px; }
.nav-menu { display: flex; align-items: center; gap: 4px; }
.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 14px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.25s ease;
  font-size: 14px;
  position: relative;
}
.nav-item:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}
.nav-item.active {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}
.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 2px;
  background: var(--dong-jade);
  border-radius: 1px;
}
.more-trigger .arrow { font-size: 10px; margin-left: 2px; }
.header-actions { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.search-box { width: 220px; }
.search-box :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow: none;
  transition: all 0.3s;
}
.search-box :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.2);
}
.search-box :deep(.el-input__inner) { color: var(--text-inverse); }
.search-box :deep(.el-input__inner::placeholder) { color: rgba(255, 255, 255, 0.4); }
.action-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text-inverse);
  transition: all 0.25s;
}
.action-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
}
.user-avatar-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 14px;
  border-radius: 20px;
  transition: all 0.25s;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.user-avatar-wrap:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.18);
}
.user-avatar {
  background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.3);
}
.user-info { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-size: 14px; font-weight: 600; color: var(--text-inverse); }
.user-role { font-size: 11px; color: #ffd700; font-weight: 500; }
.dropdown-arrow { font-size: 10px; opacity: 0.6; }
.mobile-menu-btn { display: none; }

@media (max-width: 1200px) {
  .nav-item { padding: 8px 10px; font-size: 13px; }
  .search-box { width: 170px; }
}

@media (max-width: 1024px) {
  .nav-item span:not(.arrow) { display: none; }
  .nav-item { padding: 8px; }
  .more-trigger span { display: none; }
  .more-trigger { padding: 8px; }
  .search-box { width: 140px; }
  .desktop-user-info { display: none; }
  .desktop-arrow { display: none; }
}

@media (max-width: 768px) {
  .dong-header { padding: 0 16px; }
  .desktop-nav { display: none; }
  .desktop-search { display: none; }
  .mobile-menu-btn { display: inline-block; }
  .logo-title { font-size: 15px; }
  .logo-sub { display: none; }
  .logo-icon { width: 32px; height: 32px; }
}

@media (max-width: 480px) {
  .dong-header { padding: 0 12px; gap: 4px; }
  .logo-area { gap: 8px; }
  .logo-title { font-size: 14px; }
  .header-actions { gap: 6px; }
  .action-btn { width: 32px; height: 32px; }
  .user-avatar-wrap { padding: 4px 10px; }
}

.notification-badge :deep(.el-badge__content) { font-size: 10px; }
.notification-panel { max-height: 400px; }
.notification-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px solid #eee; margin-bottom: 8px; font-weight: 600; }
.notification-list { max-height: 320px; overflow-y: auto; }
.notification-empty { text-align: center; color: #999; padding: 24px 0; font-size: 13px; }
.notification-item { padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.notification-item:last-child { border-bottom: none; }
.notification-title { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.notification-content { font-size: 12px; color: #666; white-space: pre-line; }
.notification-time { font-size: 11px; color: #999; margin-top: 4px; }
</style>
