<template>
  <div class="relative flex flex-col items-center bg-white shadow-[0_2px_12px_rgba(0,0,0,0.04)] rounded-xl w-full border border-gray-100 overflow-hidden hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)] transition-all duration-200">
    <!-- 优惠券打孔半圆凹槽与垂直虚线分栏 -->
    <div class="absolute -top-2 left-[106px] -translate-x-1/2 w-4 h-4 bg-gray-50 border border-gray-100 rounded-full z-10"></div>
    <div class="absolute -bottom-2 left-[106px] -translate-x-1/2 w-4 h-4 bg-gray-50 border border-gray-100 rounded-full z-10"></div>
    <div class="absolute top-3 bottom-3 left-[106px] border-r border-dashed border-gray-200/80 z-5"></div>

    <div class="flex items-center justify-between w-full p-2.5 gap-2 relative z-1">
      <div class="flex items-center gap-4 flex-1 min-w-0">
        <!-- 优惠金额/折扣展示 -->
        <div class="flex flex-col items-center justify-center shrink-0 w-24 pr-4 py-1 bg-gradient-to-r from-red-50/20 via-transparent to-transparent rounded-l-lg">
          <span class="text-red-500 font-bold flex items-baseline gap-0.5 drop-shadow-2xs">
            <template v-if="item.amount">
              <span class="text-sm">¥</span>
              <span class="text-3xl font-black tracking-tight">{{ item.amount }}</span>
            </template>
            <template v-else-if="item.discount">
              <span class="text-2xl font-black tracking-tight">{{ Number(item.discount) * 10 }}</span>
              <span class="text-xs font-bold">折</span>
            </template>
            <template v-else>
              <span class="text-xl font-bold">免费</span>
            </template>
          </span>
          <span class="text-[10px] text-red-400 mt-1 font-medium bg-red-50/40 px-1 py-0.2 rounded border border-red-100/10">
            {{ item.scopeType === 1 || item.scopeType === 0 ? '无门槛' : '限制使用' }}
          </span>
        </div>

        <!-- 优惠券名称及有效期 -->
        <div class="flex flex-col gap-1 justify-start flex-1 min-w-0">
          <span v-if="!props.me && item.name" class="text-[10px] text-blue-600 font-bold tracking-wide bg-blue-50/70 border border-blue-100/40 px-1.5 py-0.5 rounded w-max truncate max-w-full">
            {{ item.name }}
          </span>
          <div class="text-sm font-semibold text-gray-800 flex gap-1.5 items-center">
            <img v-if="item.image" :src="item.image" class="w-5 h-5 rounded-md object-cover shrink-0 border border-gray-100 shadow-2xs" />
            <span class="truncate">{{ item.couponName || item.name }}</span>

            <!-- 通用气泡提示 (高层级顶部气泡) -->
            <Tooltip :content="item.description || '全场通用规则'" placement="top" theme="dark">
              <svg t="1781775325917"
                class="icon size-4 cursor-pointer text-blue-500/80 hover:text-blue-600 transition-colors duration-200 shrink-0"
                viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M511.333 127.333c51.868 0 102.15 10.144 149.451 30.15 45.719 19.337 86.792 47.034 122.078 82.321 35.287 35.286 62.983 76.359 82.321 122.078 20.006 47.3 30.15 97.583 30.15 149.451s-10.144 102.15-30.15 149.451c-19.337 45.719-47.034 86.792-82.321 122.078-35.286 35.287-76.359 62.983-122.078 82.321-47.3 20.006-97.583 30.15-149.451 30.15s-102.15-10.144-149.451-30.15c-45.719-19.337-86.792-47.034-122.078-82.321-35.287-35.286-62.983-76.359-82.321-122.078-20.006-47.3-30.15-97.583-30.15-149.451s10.144-102.15 30.15-149.451c19.337-45.719 47.034-86.792 82.321-122.078 35.286-35.287 76.359-62.983 122.078-82.321 47.301-20.006 97.583-30.15 149.451-30.15m0-64c-247.424 0-448 200.576-448 448s200.576 448 448 448 448-200.576 448-448-200.576-448-448-448z"
                  fill="currentColor"></path>
                <path d="M543.334 576h-64.001l-31.246-320.047h128.025z" fill="currentColor"></path>
                <path d="M512.099 702.965m-40 0a40 40 0 1 0 80 0 40 40 0 1 0-80 0Z" fill="currentColor"></path>
              </svg>
            </Tooltip>
          </div>
          <span class="text-gray-400 text-xs truncate">
            {{ validityText }}
          </span>
        </div>
      </div>

      <!-- 右侧交互区域 -->
      <div class="w-20 flex flex-col items-center gap-1.5 justify-center shrink-0">
        <!-- 1. 通用领取模式 (活动中心) -->
        <template v-if="!props.me && (props.item.type === 1 || !props.item.type)">
          <button v-if="!isItemAvailable" disabled
            class="rounded-full w-full py-1 text-center bg-gray-100 text-gray-400 border border-gray-200 text-xs font-medium cursor-not-allowed select-none">
            {{ (item.stock !== undefined && item.stock <= 0) ? '已抢光' : '已达限额' }}
          </button>
          <button v-else @click="$emit('receive', item.id)"
            class="rounded-full w-full py-1 text-center text-white bg-linear-to-br from-blue-400 to-blue-600 hover:from-blue-500 hover:to-blue-700 hover:shadow-[0px_2px_6px_rgba(59,130,246,0.4)] transition-all duration-200 cursor-pointer text-xs font-medium">
            领取
          </button>
        </template>

        <!-- 2. 秒杀倒计时模式 (活动中心) -->
        <template v-else-if="!props.me && props.item.type === 2">
          <button v-if="!isItemAvailable" disabled
            class="rounded-full w-full py-1 text-center bg-gray-100 text-gray-400 border border-gray-200 text-xs font-medium cursor-not-allowed select-none">
            {{ (item.stock !== undefined && item.stock <= 0) ? '已抢光' : '已达限额' }}
          </button>
          <button v-else-if="state.countdownSeconds > 0" disabled
            class="rounded-full w-full py-1 text-center bg-gray-200 text-gray-500 border border-gray-300 text-xs font-semibold select-none cursor-not-allowed">
            {{ state.countdownText }}
          </button>
          <button v-else @click="$emit('receive', item.id)"
            class="rounded-full w-full py-1 text-center text-white bg-linear-to-br from-red-400 to-red-600 hover:from-red-500 hover:to-red-700 hover:shadow-[0px_2px_6px_rgba(239,68,68,0.4)] transition-all duration-200 cursor-pointer text-xs font-medium">
            抢券
          </button>
        </template>

        <!-- 3. 我的已持有优惠券模式 -->
        <template v-else-if="props.me">
          <button v-if="state.isExpired || props.item.status === CouponStatus.EXPIRED" disabled
            class="rounded-full w-full py-1 text-center bg-gray-100 text-gray-400 border border-gray-200 text-xs font-medium cursor-not-allowed select-none">
            已过期
          </button>
          <button v-else-if="state.isUsed || props.item.status === CouponStatus.USED" disabled
            class="rounded-full w-full py-1 text-center bg-gray-100 text-gray-400 border border-gray-200 text-xs font-medium cursor-not-allowed select-none">
            已使用
          </button>
          <button v-else @click="$emit('use', item)"
            class="rounded-full w-full py-1 text-center bg-linear-to-br from-emerald-400 to-emerald-600 hover:from-emerald-500 hover:to-emerald-700 text-white hover:shadow-[0px_2px_6px_rgba(16,185,129,0.4)] transition-all duration-200 cursor-pointer text-xs font-medium">
            去使用
          </button>
        </template>

        <!-- 下拉详情触发箭头 -->
        <span class="cursor-pointer text-gray-400 hover:text-gray-600 transition-colors duration-200 pt-0.5"
          @click="state.showMore = !state.showMore">
          <svg t="1781791086826" class="icon size-4.5 transition-transform duration-300"
            :class="state.showMore ? 'rotate-180' : ''" viewBox="0 0 1024 1024" version="1.1"
            xmlns="http://www.w3.org/2000/svg">
            <path d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z">
            </path>
          </svg>
        </span>
      </div>
    </div>

    <!-- 详情展开容器 -->
    <div class="grid transition-[grid-template-rows] duration-300 ease-in-out w-full border-t border-gray-100"
      :class="state.showMore ? 'grid-rows-[1fr]' : 'grid-rows-[0fr]'">
      <div class="overflow-hidden">
        <div class="flex flex-col text-xs text-gray-500 gap-1.5 w-full p-3 bg-gray-50/80">
          <div class="flex gap-2 items-center justify-start">
            <span class="font-medium text-gray-600 shrink-0">使用范围:</span>
            <span>
              {{
                item.scopeType === 1 || item.scopeType === 0 ? '全场通用' :
                  (item.scopeType === 2 ? `仅限特定类别使用(${item.scopeDescription || '未知'})` :
                    (item.scopeType === 3 ? `仅限特定套餐使用(${item.scopeDescription || '未知'})` : ''))
              }}
            </span>
          </div>
          <div class="flex gap-2 items-start justify-start">
            <span class="font-medium text-gray-600 shrink-0">计算规则:</span>
            <span class="leading-relaxed">
              {{ item.description }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Tooltip from '@/presentation/components/Tooltip.vue';
import {TimeUtils} from '@/utils/time';
import {computed, onMounted, onUnmounted, reactive, ref} from 'vue';
import {CouponStatus} from '@/services/coupon/type';

const state = reactive({
  isExpired: computed(() => {
    return props.me && props.item.status === CouponStatus.EXPIRED;
  }),
  isUsed: computed(() => {
    return props.me && props.item.status === CouponStatus.USED;
  }),
  relatedOrderId: computed(() => {
    return props.item.relatedOrderId;
  }),
  showMore: ref(false),
  countdownSeconds: ref(0),
  countdownText: ref('00:00'),
});

interface Props {
  item: any;
  me: boolean;
  relatedOrderId?: string;
}

const props = defineProps<Props>();
defineEmits<{
  (e: 'receive', id: string | number): void;
  (e: 'use', item: any): void;
}>();

let timer: ReturnType<typeof setInterval> | null = null;

// 统一解析当前条目是否可用/可领取
const isItemAvailable = computed(() => {
  if (props.me) {
    return props.item.status === CouponStatus.UNUSED;
  }
  if (props.item.available !== undefined) {
    return props.item.available === true;
  }
  if (props.item.isAvailable !== undefined) {
    return props.item.isAvailable === true;
  }
  return true;
});

// 格式化有效期文本
const validityText = computed(() => {
  const item = props.item;
  // 1. 我的已持有优惠券模式
  if (props.me) {
    if (state.isUsed) {
      return state.relatedOrderId ? `订单ID: ${state.relatedOrderId}` : '已使用';
    }
    return item.endTime ? `${TimeUtils.formatExpireDate(item.endTime)} 后过期` : '';
  }

  // 2. 优惠券活动投放中心模式
  const expireText = item.activityEndTime ? `活动将于 ${TimeUtils.formatExpireDate(item.activityEndTime)} 结束` : '';
  let durationText = '';
  if (item.timeType === 1) {
    durationText = `有效期: ${TimeUtils.formatExpireDate(item.startTime || item.activityStartTime)} - ${TimeUtils.formatExpireDate(item.endTime || item.activityEndTime)}`;
  } else if (item.timeType === 2) {
    durationText = `领券后 ${item.validDays || 0} 天内有效`;
  } else if (item.timeType === 3) {
    durationText = `领券后 ${item.validHours || 0} 小时内有效`;
  }

  if (!isItemAvailable.value) {
    return `${expireText} · 已达领取限额`;
  }
  return durationText ? `${expireText} · ${durationText}` : expireText;
});

// 计算倒数格式
function formatCountdown(seconds: number): string {
  if (seconds <= 0) return '00:00';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  const pad = (n: number) => String(n).padStart(2, '0');

  if (h > 0) {
    return `${pad(h)}:${pad(m)}:${pad(s)}`;
  }
  return `${pad(m)}:${pad(s)}`;
}

// 更新倒数
function updateCountdown() {
  if (props.item.type !== 2) return;
  const diff = TimeUtils.calculateTimeByNow(props.item.activityStartTime);
  state.countdownSeconds = Math.max(0, diff);
  state.countdownText = formatCountdown(state.countdownSeconds);

  if (state.countdownSeconds <= 0 && timer) {
    clearInterval(timer);
    timer = null;
  }
}

onMounted(() => {
  if (props.item.type === 2) {
    updateCountdown();
    if (state.countdownSeconds > 0) {
      timer = setInterval(updateCountdown, 1000);
    }
  }
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>
