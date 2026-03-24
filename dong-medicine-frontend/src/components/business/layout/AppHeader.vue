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
import { ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { HomeFilled, Document, User, Picture, ChatDotRound, More, ArrowDown, Search, ArrowLeft, Setting, SwitchButton, Aim, Folder, DataLine, InfoFilled, Menu } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();

defineProps({ isLoggedIn: Boolean, userName: String, isAdmin: Boolean });
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

const doSearch = () => {
  if (searchKeyword.value.trim()) router.push({ path: "/search", query: { q: searchKeyword.value.trim() } });
  else router.push("/search");
};

const goToSearch = () => {
  router.push("/search");
};
</script>

<style scoped>
.dong-header { display: flex; align-items: center; justify-content: space-between; padding: 0 16px; height: 64px; background: linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark)); color: var(--text-inverse); position: sticky; top: 0; z-index: 1000; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15); }
.logo-area { display: flex; align-items: center; gap: 10px; cursor: pointer; flex-shrink: 0; }
.logo-icon { width: 36px; height: 36px; color: var(--text-inverse); }
.logo-text { display: flex; flex-direction: column; }
.logo-title { font-size: 16px; font-weight: 700; letter-spacing: 1px; }
.logo-sub { font-size: 10px; opacity: 0.7; }
.nav-menu { display: flex; align-items: center; gap: 2px; }
.nav-item { display: flex; align-items: center; gap: 4px; padding: 8px 12px; color: rgba(255, 255, 255, 0.85); text-decoration: none; border-radius: 8px; transition: all 0.2s; font-size: 14px; }
.nav-item:hover, .nav-item.active { background: rgba(255, 255, 255, 0.15); color: var(--text-inverse); }
.more-trigger .arrow { font-size: 10px; margin-left: 2px; }
.header-actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.search-box { width: 200px; }
.search-box :deep(.el-input__wrapper) { background: rgba(255, 255, 255, 0.1); border: none; box-shadow: none; }
.search-box :deep(.el-input__inner) { color: var(--text-inverse); }
.search-box :deep(.el-input__inner::placeholder) { color: rgba(255, 255, 255, 0.5); }
.action-btn { background: rgba(255, 255, 255, 0.1); border: none; color: var(--text-inverse); }
.action-btn:hover { background: rgba(255, 255, 255, 0.2); }
.user-avatar-wrap { display: flex; align-items: center; gap: 10px; cursor: pointer; padding: 6px 12px; border-radius: 10px; transition: background 0.2s; background: rgba(255, 255, 255, 0.08); }
.user-avatar-wrap:hover { background: rgba(255, 255, 255, 0.15); }
.user-avatar { background: linear-gradient(135deg, var(--dong-green), var(--dong-jade-dark)); flex-shrink: 0; border: 2px solid rgba(255, 255, 255, 0.3); }
.user-info { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-size: 14px; font-weight: 600; color: var(--text-inverse); }
.user-role { font-size: 11px; color: #ffd700; font-weight: 500; }
.dropdown-arrow { font-size: 10px; opacity: 0.8; }
.mobile-menu-btn { display: none; }

@media (max-width: 1200px) {
  .nav-item { padding: 8px 10px; font-size: 13px; }
  .search-box { width: 160px; }
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
  .dong-header { padding: 0 12px; }
  .desktop-nav { display: none; }
  .desktop-search { display: none; }
  .mobile-menu-btn { display: inline-block; }
  .logo-title { font-size: 15px; }
  .logo-sub { display: none; }
  .logo-icon { width: 32px; height: 32px; }
}

@media (max-width: 480px) {
  .dong-header { padding: 0 8px; gap: 4px; }
  .logo-area { gap: 6px; }
  .logo-title { font-size: 14px; }
  .header-actions { gap: 4px; }
  .action-btn { width: 32px; height: 32px; }
  .user-avatar-wrap { padding: 4px; }
}
</style>
