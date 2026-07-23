import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const home = () => import('@/views/home/pages/home.vue')
const payResult = () => import('@/views/payment/pages/PayResult.vue')
const userProfile = () => import('@/views/user/pages/user-profile.vue')
const userSettings = () => import('@/views/user/pages/user-settings.vue')
const post = () => import('@/views/post/pages/post.vue')
const postInfo = () => import('@/views/post/pages/postInfo.vue')
const pay = () => import('@/views/payment/pages/pay.vue')
const rank_board = () => import('@/views/rank_board/pages/rank_layout.vue')
const message = () => import('@/views/message/pages/message.vue')
const coupon = () => import('@/views/coupon/pages/coupon.vue')
const postFeed = () => import('@/views/home/pages/post-feed.vue')
const orders = () => import('@/views/payment/pages/MyOrders.vue')
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
    redirect: '/home/feed',
    name: 'home',

    children: [
      {
        path: 'postTagInfo/:tagName',
        component: () => import('@/views/post/pages/postTagInfo.vue'),
        name: 'postTagInfo',
      },
      {
        path: 'userSettings',
        component: userSettings,
        name: 'userSettings',
      },
      {
        path: 'feed',
        component: postFeed,
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
        path: '/message',
        component: message,
        name: 'message',
        meta: {
          title: '消息',
        },
      },
      {
        path: '/coupon',
        component: coupon,
        name: 'coupon',
      },
      {
        path: '/orders',
        component: orders,
        name: 'orders',
        meta: {
          title: '我的订单',
        },
      },
    ],
  },
  {
    path: '/payment',
    name: 'payment',
    component: pay,
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

import { useAuthStore } from '@/views/auth/store'
import { log } from '@/utils/log'

const router = createRouter({
  history: createWebHistory('/'),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

router.beforeEach((to, from, next) => {
  const authPages = ['post', 'message', 'coupon', 'userSettings', 'payment', 'payResult', 'orders']
  if (authPages.includes(to.name as string)) {
    const authStore = useAuthStore()
    if (!authStore.token) {
      log.warning('请先登录后访问该页面')
      authStore.showLoginDialog()
      if (from.name) {
        next(false)
      } else {
        next({ name: 'homeMain' })
      }
      return
    }
  }
  next()
})

export default router
