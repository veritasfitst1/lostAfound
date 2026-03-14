import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { guest: true } },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'users', name: 'UserManage', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'items', name: 'ItemManage', component: () => import('../views/ItemManage.vue'), meta: { title: '物品管理' } },
      { path: 'reports', name: 'ReportManage', component: () => import('../views/ReportManage.vue'), meta: { title: '举报审核' } },
      { path: 'expired', name: 'ExpiredManage', component: () => import('../views/ExpiredManage.vue'), meta: { title: '过期处理' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.meta.requiresAuth && !store.isLoggedIn()) next('/login')
  else if (to.meta.guest && store.isLoggedIn()) next('/')
  else next()
})

export default router
