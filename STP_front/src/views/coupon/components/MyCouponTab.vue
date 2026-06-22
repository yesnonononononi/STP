<template>
  <div class="flex flex-col h-full">
    <!-- 二级 Tab 栏 -->
    <div class="sub-tabs flex gap-4 p-2 justify-center bg-gray-50/50 border-b border-gray-100">
      <span v-for="sub in subTabs" :key="sub.value" @click="curSubTab = sub.value"
        class="cursor-pointer text-xs px-3 py-1 rounded-full transition-all duration-200"
        :class="curSubTab === sub.value ? 'bg-blue-500 text-white font-medium shadow-sm' : 'text-gray-400 hover:text-gray-700 hover:bg-gray-100'">
        {{ sub.label }}
      </span>
    </div>

    <!-- 列表内容展示区 -->
    <div class="foot flex-1 flex flex-col gap-2 overflow-y-auto bg-gray-50 p-2">
      <template v-if="filteredMyCoupons.length > 0">
        <CouponItem v-for="item in filteredMyCoupons" :key="item.id" :item="item" :me="true"
          @use="coupon => $emit('use', coupon)" />
      </template>
      <div v-else class="flex flex-col items-center justify-center py-20 text-gray-400 text-sm gap-2">
        <span>暂无符合筛选的优惠券</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import CouponItem from '@/presentation/components/CouponItem.vue';
import type { CouponVO } from '@/services/coupon/coupon';
import { TimeUtils } from '@/utils/time';
import { CouponStatus } from '@/services/coupon/type';

const props = defineProps<{
  myCoupons: CouponVO[];
}>();

defineEmits<{
  (e: 'use', coupon: CouponVO): void;
}>();

const subTabs = [
  { label: '全部', value: 'all' as const },
  { label: '未使用', value: 'unused' as const },
  { label: '已使用', value: 'used' as const },
  { label: '已过期', value: 'expired' as const }
];
const curSubTab = ref<'all' | 'unused' | 'used' | 'expired'>('all');

// 我的优惠券过滤逻辑
const filteredMyCoupons = computed(() => {
  const list = props.myCoupons;
  if (curSubTab.value === 'all') {
    return list;
  }
  return list.filter(coupon => {
    if (curSubTab.value === 'unused') {
      return coupon.status === CouponStatus.UNUSED && coupon.isAvailable === true;
    } else if (curSubTab.value === 'used') {
      return coupon.status === CouponStatus.USED;
    } else if (curSubTab.value === 'expired') {
      return coupon.isAvailable === false;
    }
    return true;
  });
});

// 重置二级 Tab 为 'all'
function resetSubTab() {
  curSubTab.value = 'all';
}

defineExpose({
  resetSubTab
});
</script>
