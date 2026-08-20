import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/views/auth/store";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("@/views/auth/pages/loginPage.vue"),
    },
    {
      path: "/",
      component: () => import("@/views/layout/index.vue"),
      redirect: "/dashboard",
      children: [
        {
          path: "dashboard",
          name: "dashboard",
          component: () => import("@/views/dashboard/index.vue"),
          meta: { title: "仪表盘 Dash" },
        },
        {
          path: "admin",
          name: "admin-list",
          component: () => import("@/views/admin/list.vue"),
          meta: { title: "管理员管理" },
        },
        {
          path: "user",
          name: "user",
          component: () => import("@/views/user/pages/list.vue"),
          meta: { title: "用户管理" },
        },
        {
          path: "user/report",
          name: "user-report",
          component: () => import("@/views/user/report.vue"),
          meta: { title: "举报审核管理" },
        },
        // 会员管理 (子路由 - 完全基于后端已有 member_level_config, member_package, user_member 三张表)
        {
          path: "member/level",
          name: "member-level",
          component: () => import("@/views/member/level.vue"),
          meta: { title: "等级配置 (level_config)" },
        },
        {
          path: "member/package",
          name: "member-package",
          component: () => import("@/views/member/package.vue"),
          meta: { title: "套餐配置 (package)" },
        },
        {
          path: "member/user",
          name: "member-user",
          component: () => import("@/views/member/user.vue"),
          meta: { title: "用户会员记录 (user_member)" },
        },
        // 兼容旧路径重定向
        {
          path: "member/config",
          redirect: "/member/package",
        },
        {
          path: "member/decoration",
          redirect: "/member/level",
        },
        // 优惠券管理 (子路由)
        {
          path: "coupon/manage",
          name: "coupon-manage",
          component: () => import("@/views/coupon/manage/index.vue"),
          meta: { title: "优惠券管理" },
        },
        {
          path: "coupon/activity",
          name: "coupon-activity",
          component: () => import("@/views/coupon/activity/index.vue"),
          meta: { title: "活动管理" },
        },
        // 帖子管理
        {
          path: "post",
          name: "post",
          component: () => import("@/views/post/index.vue"),
          meta: { title: "帖子管理" },
        },
        // 评论管理
        {
          path: "comment",
          name: "comment",
          component: () => import("@/views/comment/index.vue"),
          meta: { title: "评论管理" },
        },
        // 订单管理
        {
          path: "order",
          name: "order",
          component: () => import("@/views/order/index.vue"),
          meta: { title: "订单管理" },
        },
        // 系统通知管理
        {
          path: "notification",
          name: "notification",
          component: () => import("@/views/notification/index.vue"),
          meta: { title: "系统通知管理" },
        },
      ],
    },
    // 全局通配兜底重定向，消除未匹配路径引起的白屏
    {
      path: "/:pathMatch(.*)*",
      redirect: "/dashboard",
    },
  ],
});

// 导航守卫进行登录校验
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore();
  if (to.name !== "login" && !authStore.token) {
    next({ name: "login" });
  } else {
    next();
  }
});

export default router;
