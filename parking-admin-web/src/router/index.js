import { createRouter, createWebHistory } from 'vue-router'
import { getAuth } from '../store/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '概览' }
      },
      {
        path: 'lots',
        name: 'Lots',
        component: () => import('../views/Lots.vue'),
        meta: { title: '车场管理' }
      },
      {
        path: 'vehicles/:lotId',
        name: 'LotVehicles',
        component: () => import('../views/LotVehicles.vue'),
        meta: { title: '车辆管理' }
      },
      {
        path: 'admins',
        name: 'Admins',
        component: () => import('../views/Admins.vue'),
        meta: { title: '管理员管理', role: 'SUPER_ADMIN' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(to => {
  const { token, role } = getAuth()
  if (to.name !== 'Login' && !token) return '/login'
  if (to.name === 'Login' && token) return '/dashboard'
  if (to.meta.role && role !== to.meta.role) return '/dashboard'
  return true
})

export default router
