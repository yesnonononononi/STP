import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const auth = () => import('@/views/auth/pages/auth.vue')
const login = () => import('@/views/auth/pages/loginPage.vue')
const register = () => import('@/views/auth/pages/registerPage.vue')
const forget = () => import('@/views/auth/pages/forgetPage.vue')
const home = () => import('@/views/home/pages/home.vue')
const payResult = () => import('@/views/payment/pages/PayResult.vue')
const userProfile = () => import('@/views/user/pages/user-profile.vue')
const homeFeed = () => import('@/views/home/pages/home-feed.vue')
const userSettings = () => import('@/views/user/pages/user-settings.vue')
const post = () => import('@/views/post/pages/post.vue')
const postInfo = () => import('@/views/post/pages/postInfo.vue')
const pay = () => import('@/views/payment/pages/pay.vue')
const rank_board = () => import('@/views/rank_board/pages/rank_layout.vue')

const coupon = () => import('@/views/coupon/pages/coupon.vue')
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
    redirect: '/home/homeFeed',
    name: 'home',

    children: [
      {
        path: 'userSettings',
        component: userSettings,
        name: 'userSettings',
      },
      {
        path: 'homeFeed',
        component: homeFeed,
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
      {
        path: '/coupon',
        component: coupon,
        name: 'coupon',
      },
    ],
  },
  {
    path: '/payment',
    name: 'payment',
    component: pay,
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
  {
    path: '/rank',
    component: rank_board,
    name: 'rank_board',
    meta: {},
  },
]

const router = createRouter({
  history: createWebHistory('/'),
  routes,
})

export default router
