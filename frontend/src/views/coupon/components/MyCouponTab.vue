<template>
  <div class="flex flex-col h-full">
    <!-- 二级 Tab 栏 -->
    <div class="sub-tabs flex gap-4 p-2 justify-center bg-gray-50/90 backdrop-blur-xs border-b border-gray-100 shrink-0 z-10">
      <span v-for="(sub, index) in subTabs" :key="index" @click="state.curSubTab = sub.value"
        class="cursor-pointer text-xs px-3 py-1 rounded-full transition-all duration-200"
        :class="state.curSubTab === sub.value ? 'bg-blue-500 text-white font-medium shadow-sm' : 'text-gray-400 hover:text-gray-700 hover:bg-gray-100'">
        {{ sub.label }}
      </span>
    </div>

    <!-- 列表内容展示区 -->
    <div class="foot relative flex-1 flex flex-col gap-2 overflow-y-auto bg-gray-50 p-2" ref="container">
      <template v-if="couponList.length > 0">
        <CouponItem v-for="item in couponList" :key="item.id" :item="item" :me="true"
          @use="coupon => $emit('use', coupon)" />
      </template>
      <div v-else-if="!loading" class="flex flex-col items-center justify-center py-20 text-gray-400 text-sm gap-2">
        <span>暂无符合筛选的优惠券</span>
      </div>
      <Loading v-model="loading" bgColor="bg-gray-50/70 backdrop-blur-xs" />
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref, watch} from 'vue';
import CouponItem from '@/presentation/components/CouponItem.vue';
import Loading from '@/presentation/components/loading.vue';
import {CouponAPI, type CouponVO} from '@/services/coupon/coupon';
import {CouponStatus} from '@/services/coupon/type';
import {scrollerFromBottom} from '@/utils/scollerbar.ts';

const state = reactive({
  page: 1,
  pageSize: 10,
  curSubTab: ref<CouponStatus | null>(CouponStatus.UNUSED),
});

const container = ref();
const loading = ref(false);

defineEmits<{
  (e: 'use', coupon: CouponVO): void;
}>();

const couponList = ref<CouponVO[]>([]);
const subTabs = [
  { label: '全部', value: null },
  { label: '未使用', value: CouponStatus.UNUSED as const },
  { label: '已使用', value: CouponStatus.USED as const },
  { label: '已过期', value: CouponStatus.EXPIRED as const }
];

async function loadCouponData(status: CouponStatus | null, isAppend: boolean = false) {
  try {
    if (!isAppend) {
      loading.value = true;
    }
    const res = await CouponAPI.getCouponHistory(state.page, state.pageSize, status);
    if (isAppend) {
      couponList.value = [...couponList.value, ...res.data.records];
    } else {
      couponList.value = res.data.records;
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadCouponData(state.curSubTab);
});

watch(() => state.curSubTab, (newStatus) => {
  state.page = 1;
  loadCouponData(newStatus);
});

function resetSubTab() {
  state.curSubTab = CouponStatus.UNUSED;
}

scrollerFromBottom(() => {
  state.page++;
  loadCouponData(state.curSubTab, true);
});

defineExpose({
  resetSubTab
});
</script>
