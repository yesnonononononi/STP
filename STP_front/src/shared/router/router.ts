import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const auth = () => import('../../auth/infrastructure/page/auth.vue')
const login = () => import('../../auth/infrastructure/page/loginPage.vue')
const register = () => import('../../auth/infrastructure/page/registerPage.vue')
const forget = () => import('../../auth/infrastructure/page/forgetPage.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/auth/login',
  },
  {
    path: '/auth',
    component: auth,
    name: 'auth',
    redirect: '/auth/login',
    children: [
      {
        path: 'login',
        component: login,
        name: 'login',
      },
      {
        path: 'register',
        component: register,
        name: 'register',
      },
      {
        path: 'forget',
        component: forget,
        name: 'forget',
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory('/'),
  routes,
})

export default router
