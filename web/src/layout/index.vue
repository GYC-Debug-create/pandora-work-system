<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside
        width="220px"
        class="sidebar-container"
      >
        <div class="logo-container">
          <span class="logo-title">潘多拉系统</span>
        </div>
        <Sidebar />
      </el-aside>

      <el-container>
        <!-- 头部 -->
        <el-header
          height="60px"
          class="header"
        >
          <div class="header-left">
            <el-icon
              class="hamburger"
              @click="toggleSidebar"
            >
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
            <Breadcrumb />
          </div>
          <div class="header-right">
            <!-- 主题切换按钮 -->
            <el-tooltip
              :content="isDark ? '切换到浅色模式' : '切换到暗色模式'"
              placement="bottom"
            >
              <el-icon
                class="theme-toggle"
                @click="toggleTheme"
              >
                <Sunny v-if="isDark" />
                <Moon v-else />
              </el-icon>
            </el-tooltip>

            <el-dropdown
              trigger="click"
              @command="handleCommand"
            >
              <div class="avatar-container">
                <span>管理员</span>
                <el-icon><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="logout"
                    divided
                  >
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主要内容区 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
  <ProfileModal v-model:visible="profileModalVisible" />
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Sunny, Moon } from '@element-plus/icons-vue'
import Sidebar from './components/Sidebar.vue'
import Breadcrumb from './components/Breadcrumb.vue'
import ProfileModal from './components/ProfileModal.vue'
import { useLogStore } from '@/store/log'
import { useThemeStore } from '@/store/theme'

const isCollapse = ref(false)
const router = useRouter()
const profileModalVisible = ref(false)
const logStore = useLogStore()
const themeStore = useThemeStore()

// 计算当前是否为暗色主题
const isDark = computed(() => themeStore.theme === 'dark')

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 切换主题
const toggleTheme = () => {
  themeStore.toggleTheme()
  const themeName = isDark.value ? '暗色' : '浅色'
  ElMessage.success(`已切换到${themeName}模式`)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('您确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      // 获取用户信息
      const userInfo = JSON.parse(sessionStorage.getItem('user-info') || '{}')
      const username = userInfo.username || '用户'
      
      // 记录登出日志
      logStore.addLog(`用户 ${username} 退出系统`, 'system')
      
      // 退出登录
      sessionStorage.removeItem('admin-token')
      sessionStorage.removeItem('user-info')
      router.push('/login')
      ElMessage.success('已成功退出登录')
    }).catch(() => {
      // 用户点击了取消
    })
  } else if (command === 'profile') {
    profileModalVisible.value = true
  }
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.sidebar-container {
  background-color: var(--sidebar-bg-color);
  height: 100vh;
  transition: width 0.28s, background-color 0.3s ease;
  overflow-x: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--sidebar-logo-bg-color);
  transition: background-color 0.3s ease;
}

.logo-title {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.header {
  background-color: var(--header-bg-color);
  box-shadow: var(--header-shadow);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
  color: var(--text-color-primary);
}

.header-left {
  display: flex;
  align-items: center;
}

.hamburger {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.theme-toggle {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s ease, transform 0.3s ease;
}

.theme-toggle:hover {
  color: #409EFF;
  transform: scale(1.1);
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.avatar-container span {
  margin-right: 5px;
}

.main-content {
  padding: 20px;
  background-color: var(--main-bg-color);
  height: calc(100vh - 60px);
  overflow-y: auto;
  transition: background-color 0.3s ease;
}
</style>
