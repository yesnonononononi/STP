import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const auth = () => import('../../auth/infrastructure/page/auth.vue')
const login = () => import('../../auth/infrastructure/page/loginPage.vue')
const register = () => import('../../auth/infrastructure/page/registerPage.vue')
const forget = () => import('../../auth/infrastructure/page/forgetPage.vue')
const home = () => import('../../main/infrastructure/page/home.vue')
const payResult = () => import('../../payment/infrastructure/page/PayResult.vue')
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/home',
    component: home,
    name: 'home',
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
  {
    path: '/payResult',
    component: payResult,
    name: 'payResult',
  },
]

const router = createRouter({
  history: createWebHistory('/'),
  routes,
})

export default router
