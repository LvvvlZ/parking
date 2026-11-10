<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">停车管理平台</div>
      <el-menu :default-active="$route.path" router background-color="#001529"
               text-color="#a6adb4" active-text-color="#ffffff" style="border-right: none">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon><span>概览</span>
        </el-menu-item>
        <el-menu-item index="/lots">
          <el-icon><Location /></el-icon><span>车场管理</span>
        </el-menu-item>
        <el-menu-item v-if="auth.role === 'SUPER_ADMIN'" index="/admins">
          <el-icon><User /></el-icon><span>管理员管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="page-title">{{ $route.meta.title }}</span>
        <el-dropdown @command="onCommand">
          <span class="user">{{ auth.name || '管理员' }}（{{ ROLE_TEXT[auth.role] || auth.role }}）</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { Odometer, Location, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getAuth, clearAuth, ROLE_TEXT } from '../store/auth'

const router = useRouter()
const auth = getAuth()

function onCommand(cmd) {
  if (cmd === 'logout') {
    clearAuth()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #001529; }
.logo {
  color: #fff; font-weight: 600; text-align: center;
  line-height: 56px; height: 56px; font-size: 16px;
}
.header {
  background: #fff; display: flex; align-items: center;
  justify-content: space-between; border-bottom: 1px solid #e6e6e6;
}
.page-title { font-size: 16px; font-weight: 600; }
.user { cursor: pointer; color: #409eff; font-size: 14px; }
.main { background: #f0f2f5; }
</style>
