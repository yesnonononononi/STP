<template>
    <div class="relative z-12 w-full h-auto bg-linear-to-br from-blue-200 to-blue-100 mt-1">
        <div class="md:w-1/2 w-full flex flex-col shadow-md m-auto h-screen bg-white rounded-md">
            <!-- 头部导航 -->
            <div class="body h-auto flex flex-col">
                <div class="head h-auto flex items-center gap-1 p-2">
                    <span class="cursor-pointer text-gray-400" @click="router.push({ name: 'home' })">首页 /</span>
                    <span class="text-gray-400">福利 /</span>
                    <span>优惠券</span>
                </div>
                <div class="body h-auto flex flex-col gap-2 pt-2">
                    <div class="img w-full md:h-36">
                        <div
                            class="w-full h-full bg-linear-to-r from-blue-400 to-indigo-500 flex items-center justify-center text-white font-bold text-lg tracking-wider">
                            STP 福利特惠专区
                        </div>
                    </div>
                    <!-- 一级 Tab 栏 -->
                    <div
                        class="tab text-lg font-semibold flex items-center  w-full shadow-xs gap-6 rounded-lg px-4 pb-2 mt-2">
                        <span v-for="tab in tabList" :key="tab.id"
                            class="cursor-pointer transition-all duration-300 pb-1 border-b-2" @click="curTab = tab"
                            :class="(curTab?.id || true) === tab.id ? 'text-blue-600 border-blue-600 font-bold scale-[1.03]' : 'text-gray-400 border-transparent'">
                            {{ tab.name }}
                        </span>
                    </div>
                </div>
            </div>

            <!-- 列表内容展示区 -->
            <div class="foot relative flex-1 overflow-y-auto bg-gray-50 p-2">
                <MyCouponTab v-if="curTab?.id === tabList[1]?.id" ref="myCouponTabRef" :loading="loading"
                    @use="handleUse" />
                <div v-if="curTab?.id === tabList[0]?.id" class="relative w-full  z-10  ">
                    <div class="w-1/2 m-auto flex items-center justify-center gap-6 sticky top-0 z-10">
                        <div class=" text-sm rounded-full p-1 shrink-0 transition-colors duration-200 ease-in-out "
                            v-for="item in couponTabList"
                            :class="curCouponTab?.id == item?.id ? 'bg-blue-600 text-white shadow-md select-none' : 'text-gray-400 cursor-pointer hover:bg-gray-100 '"
                            @click="curCouponTab = item" :key="item.id">
                            {{ item.name }}
                        </div>
                    </div>
                    <CouponItem v-for="activity in activitiesList" :key="activity.id" :item="activity" :me="false"
                        @receive="handleReceive" />
                </div>
                <div v-if="curTab?.id === tabList[0]?.id && activitiesList.length == 0 && !loading"
                    class="absolute inset-0 flex items-center justify-center text-gray-400 text-lg ">
                    暂无符合筛选条件的优惠券
                </div>
                <Loading v-model="loading" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import CouponItem from '@/presentation/components/CouponItem.vue';
import Loading from '@/presentation/components/loading.vue';
import router from '@/router';
import { CouponAPI, type CouponVO, type CouponActivityVO } from '@/services/coupon/coupon.ts';
import { scopeType } from '@/services/coupon/type.ts';
import { log } from '@/utils/log';
import { onMounted, ref, watch, defineAsyncComponent } from 'vue';

// 异步组件导入，实现按需分包懒加载，防止一次加载三个包
const MyCouponTab = defineAsyncComponent(() => import('../components/MyCouponTab.vue'));
const loading = ref(false);
const tabList = [
    { name: '优惠券', id: 1 },
    {
        name: '我的优惠券', id: 3,
    }
];
const couponTabList = [
    {
        name: '全场通用',
        id: scopeType.NORMAL
    },
    {
        name: '指定商品分类',
        id: scopeType.SPECIFY_TYPE
    },
    {
        name: '指定商品',
        id: scopeType.SPECIFY_COMMODITY
    }
]
const curTab = ref(tabList[0]);
const curCouponTab = ref(couponTabList[0]);
const activitiesList = ref<(CouponActivityVO & { isSeckill?: boolean })[]>([]);
const myCouponTabRef = ref<any>(null);

// 监听主 Tab 变动
watch(curTab, (newTab) => {
    loadCouponData(curCouponTab.value?.id || 1);
});
watch(curCouponTab, (newTab) => {
    loadCouponData(curCouponTab.value?.id || 1)
})
async function loadCouponData(scopeType?: number) {
    try {
        if (loading.value) return;
        loading.value = true;
        activitiesList.value = (await CouponAPI.getCouponActivitiesByScopeType(scopeType)).data;
    } finally {
        loading.value = false;
    }
}



async function handleReceive(activityId: string | number) {
    await CouponAPI.receiveCoupon(activityId);
    await loadCouponData();
}

function handleUse(coupon: CouponVO) {
    router.push({ name: 'home' });
}

onMounted(async () => {
    await loadCouponData();
});
</script>
