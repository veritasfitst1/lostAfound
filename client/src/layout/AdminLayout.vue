<template>
  <el-container>
    <el-aside width="220px" class="aside">
      <div class="logo">校园失物招领</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/items">
          <el-icon><Document /></el-icon>
          <span>物品管理</span>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><Warning /></el-icon>
          <span>举报审核</span>
        </el-menu-item>
        <el-menu-item index="/expired">
          <el-icon><Clock /></el-icon>
          <span>过期处理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ $route.meta?.title ?? '管理后台' }}</span>
        <el-dropdown @command="handleLogout">
          <span class="user-info">
            {{ userStore.userInfo?.nickname || userStore.userInfo?.username }} <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { DataLine, User, Document, Warning, Clock, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.aside {
  background: #1d1e1f;
  min-height: 100vh;
}

.logo {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #333;
}

.el-menu {
  border-right: none;
  background: transparent;
}

.el-menu-item {
  color: #a3a6ad;
}

.el-menu-item:hover,
.el-menu-item.is-active {
  color: #1989fa;
  background: rgba(25, 137, 250, 0.1);
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 500;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.main {
  background: #f5f6fa;
  padding: 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
