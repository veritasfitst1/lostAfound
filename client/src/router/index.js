import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
//定义路由表
const routes = [
  { 
    path: '/login', 
    name: 'Login', 
    component: () => import('../views/Login.vue'), //访问login时加载
    meta: { guest: true } //只有未登录用户能访问
  },
  {
    path: '/',  //后台主布局
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', 
        name: 'AdminStats', 
        component: () => import('../views/Dashboard.vue'), 
        meta: { title: '统计信息' } 
      },
      { path: 'users', 
        name: 'UserManage', 
        component: () => import('../views/UserManage.vue'), 
        meta: { title: '用户管理' } 
      },
      { path: 'items', 
        name: 'ItemManage', 
        component: () => import('../views/ItemManage.vue'), 
        meta: { title: '物品管理' } 
      },
      { path: 'categories', 
        name: 'CategoryManage', 
        component: () => import('../views/CategoryManage.vue'), 
        meta: { title: '分类管理' } 
      },
      { path: 'reports', 
        name: 'ReportManage', 
        component: () => import('../views/ReportManage.vue'), 
        meta: { title: '举报审核' } 
      },
      { path: 'expired', 
        name: 'ExpiredManage', 
        component: () => import('../views/ExpiredManage.vue'), 
        meta: { title: '过期处理' } 
      }
    ]
  }
]

//路由实例
const router = createRouter({ history: createWebHistory(), routes })
//每次跳转前执行  （去哪 ，从哪来 ，是否放行）
router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.meta.requiresAuth && !store.isLoggedIn()) next('/login') //没登陆
  else if (to.meta.guest && store.isLoggedIn()) next('/')  //已登录回首页
  else next()  //放行
})

export default router  //给 main.js 使用
