import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '仪表盘', icon: 'Odometer' }
        }
      ]
    },
    {
      path: '/user',
      component: Layout,
      children: [
        {
          path: '',
          name: 'UserList',
          component: () => import('@/views/user/index.vue'),
          meta: { title: '用户管理', icon: 'User' }
        }
      ]
    },
    {
      path: '/tasks',
      component: Layout,
      children: [
        {
          path: '',
          name: 'TaskList',
          component: () => import('@/views/task/index.vue'),
          meta: { title: '公司十大事项', icon: 'List' }
        }
      ]
    },
    // 404页面
    {
      path: '/:pathMatch(.*)*',
      component: () => import('@/views/error/404.vue'),
      hidden: true
    },
    {
      path: '/login',
      component: () => import('@/views/login/index.vue'),
      hidden: true
    }
  ]
})

// 导航守卫
router.beforeEach((to, from, next) => {
  const hasToken = sessionStorage.getItem('admin-token')

  if (hasToken) {
    if (to.path === '/login') {
      // 如果已登录，访问登录页则重定向到首页
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    // 没有token
    if (to.path !== '/login') {
      // 如果访问的不是登录页，则重定向到登录页
      next({ path: '/login' })
    } else {
      next()
    }
  }
})

export default router
