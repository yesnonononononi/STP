import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const auth = () => import('@/views/auth/pages/auth.vue')
const login = () => import('@/views/auth/pages/loginPage.vue')
const register = () => import('@/views/auth/pages/registerPage.vue')
const forget = () => import('@/views/auth/pages/forgetPage.vue')
const home = () => import('@/views/home/pages/home.vue')
const payResult = () => import('@/views/payment/pages/PayResult.vue')
const userProfile = () => import('@/views/user/pages/user-profile.vue')
const homeTabMain = () => import('@/views/home/pages/home-tab-main.vue')
const userSettings = () => import('@/views/user/pages/user-settings.vue')
const post = () => import('@/views/post/pages/post.vue')
const postInfo = () => import('@/views/post/pages/postInfo.vue')
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/post',
    component: post,
    name: 'post',
    meta: {
      title: '发布帖子',
    },
  },
  {
    path: '/home',
    component: home,
    redirect: '/home/homeTabMain',
    name: 'home',

    children: [
      {
        path: 'userSettings',
        component: userSettings,
        name: 'userSettings',
      },
      {
        path: 'homeTabMain',
        component: homeTabMain,
        name: 'homeMain',
        meta: {
          title: '首页',
        },
      },
      {
        path: 'userProfile/:id',
        component: userProfile,
        name: 'userProfile',
      },
    ],
  },

  {
    path: '/auth',
    component: auth,
    name: 'auth',
    meta: {
      title: '用户认证',
    },
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
    meta: {
      title: '支付结果',
    },
  },
]

const router = createRouter({
  history: createWebHistory('/'),
  routes,
})

export default router
