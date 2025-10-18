<template>
  <el-scrollbar>
    <el-menu
      :default-active="activeMenu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      :collapse="isCollapse"
      :unique-opened="true"
      router
    >
      <sidebar-item 
        v-for="route in routes" 
        :key="route.path" 
        :item="route" 
        :base-path="route.path" 
      />
    </el-menu>
  </el-scrollbar>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SidebarItem from './SidebarItem.vue'
import router from '@/router'

// 获取路由实例
const route = useRoute()

// 获取路由表中的路由
const routes = computed(() => {
  return router.options.routes.filter(route => !route.hidden)
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 是否折叠菜单
const isCollapse = computed(() => {
  return false // 这里可以从全局状态管理中获取
})
</script>
