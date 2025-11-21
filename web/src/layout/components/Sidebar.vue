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
        v-for="menuRoute in routes"
        :key="menuRoute.path"
        :item="menuRoute"
        :base-path="menuRoute.path"
      />
    </el-menu>
  </el-scrollbar>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SidebarItem from './SidebarItem.vue'
import router from '@/router'

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

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
  return props.isCollapse
})
</script>
