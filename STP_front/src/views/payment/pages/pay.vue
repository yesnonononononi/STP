<template>
    <div
        class="relative z-1 w-full min-h-screen bg-linear-to-br from-blue-50/70 via-indigo-50/50 to-purple-50/70 flex items-center justify-center p-4 md:p-6 overflow-hidden select-none">
        <!-- Background decorative glowing spots -->
        <div
            class="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-linear-to-br from-blue-400/20 to-indigo-400/20 rounded-full filter blur-3xl opacity-70 pointer-events-none">
        </div>
        <div
            class="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-linear-to-tr from-purple-400/20 to-pink-400/20 rounded-full filter blur-3xl opacity-70 pointer-events-none">
        </div>

        <!-- Main pay card -->
        <div
            class="w-full max-w-120 bg-white/85 backdrop-blur-xl border border-white/60 shadow-2xl rounded-3xl p-6 md:p-8 flex flex-col gap-6 transition-all duration-300 hover:shadow-indigo-150/40 relative z-10">
            <!-- Header -->
            <div class="flex items-center justify-between pb-2 border-b border-slate-100/80">
                <button @click="cancelPay"
                    class="p-2 -ml-2 rounded-xl text-slate-400 hover:text-slate-650 hover:bg-slate-100/50 transition-all active:scale-95 cursor-pointer">
                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
                    </svg>
                </button>
                <span class="font-bold text-xl text-slate-800 tracking-wide">确认订单</span>
                <div class="w-9 h-9"></div> <!-- spacer to balance the back button -->
            </div>

            <!-- Step Indicators -->
            <div class="flex items-center justify-center gap-2 text-xs font-semibold text-slate-400 py-1">
                <span class="text-indigo-650 font-bold flex items-center gap-1">
                    <span class="w-1.5 h-1.5 rounded-full bg-indigo-600"></span>确认订单
                </span>
                <span class="w-8 h-px bg-slate-200"></span>
                <span class="flex items-center gap-1">
                    <span class="w-1.5 h-1.5 rounded-full bg-slate-300"></span>在线支付
                </span>
                <span class="w-8 h-px bg-slate-200"></span>
                <span class="flex items-center gap-1">
                    <span class="w-1.5 h-1.5 rounded-full bg-slate-300"></span>支付成功
                </span>
            </div>

            <!-- Commodity Info Card -->
            <div
                class="bg-linear-to-r from-slate-50 to-indigo-50/30 border border-slate-100 rounded-2xl p-4 flex items-center justify-between gap-4">
                <div class="flex gap-3 items-center">
                    <div
                        class="relative flex items-center justify-center w-14 h-14 bg-amber-500/10 rounded-xl border border-amber-500/20 shadow-inner">
                        <!-- Shiny gold Jewel/Diamond SVG -->
                        <svg class="w-8 h-8 drop-shadow-[0_2px_8px_rgba(242,203,81,0.4)] animate-pulse"
                            viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
                            <path d="M512 128L192 384h640L512 128z" fill="#F59E0B"></path>
                            <path d="M192 384l320 512 320-512H192z" fill="#D97706"></path>
                            <path d="M512 128v768L832 384H512z" fill="#FBBF24" opacity="0.3"></path>
                        </svg>
                    </div>
                    <div class="flex flex-col gap-1 justify-start">
                        <span class="font-bold text-slate-800 text-base leading-snug">{{ commodityInfo?.name ||
                            '未获取到商品信息' }}</span>
                        <span
                            class="text-slate-400 text-xs font-semibold bg-slate-150/50 px-2 py-0.5 rounded-md w-max">数量：{{
                                quantity || 0 }}</span>
                    </div>
                </div>
                <div class="flex flex-col items-end gap-1.5">
                    <span class="text-xs text-slate-450 line-through">原价 ¥{{ commodityInfo?.price || '0.00' }}</span>
                    <div
                        class="bg-amber-500/10 border border-amber-500/20 text-amber-700 text-[11px] font-bold px-2 py-0.5 rounded-full">
                        已节省约 ¥{{ discount }}
                    </div>
                </div>
            </div>

            <!-- Details list & pricing calculation -->
            <div class="flex flex-col gap-3.5 bg-slate-50/50 border border-slate-100 rounded-2xl p-4">
                <div class="flex items-center justify-between text-sm">
                    <span class="text-slate-500">商品总额</span>
                    <span class="font-semibold text-slate-800">
                        ¥{{ commodityInfo?.price || 0 }}
                    </span>
                </div>
                <div class="flex items-center justify-between text-sm">
                    <span class="text-slate-500">优惠券</span>
                    <div class="cursor-pointer flex items-center gap-1.5 text-xs" @click="moreCoupon = true">
                        <span v-if="curCoupon"
                            class="bg-rose-50 border border-rose-100 text-rose-600 font-semibold px-2.5 py-1 rounded-lg flex items-center gap-1 shadow-xs">
                            <span class="w-1.5 h-1.5 rounded-full bg-rose-500 animate-ping"></span>
                            -¥{{ discount }} ({{ curCoupon.name }})
                        </span>
                        <span v-else-if="canUseCoupon.length > 0"
                            class="bg-amber-50 border border-amber-100 text-amber-750 font-semibold px-2.5 py-1 rounded-lg flex items-center gap-1 animate-pulse">
                            <span class="w-1.5 h-1.5 rounded-full bg-amber-500"></span>
                            {{ canUseCoupon.length }}张可用券
                        </span>
                        <span v-else class="text-slate-400 font-medium">无可用券</span>
                        <svg class="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"
                            stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                        </svg>
                    </div>
                </div>
                <div class="h-px bg-slate-100 my-1"></div>
                <div class="flex items-center justify-between">
                    <span class="text-slate-800 font-bold text-sm">实付金额</span>
                    <span class="text-rose-500 font-extrabold text-xl tracking-tight">
                        ¥{{ calculatePrice().toFixed(2) }}
                    </span>
                </div>
            </div>

            <!-- Payment Methods Grid -->
            <div class="flex flex-col gap-3">
                <span class="text-slate-800 font-bold text-sm">选择支付方式</span>
                <div class="grid grid-cols-2 gap-3">
                    <div v-for="(item, index) in payTypes" :key="index"
                        class="relative flex flex-col items-center justify-center p-4 rounded-2xl border-2 cursor-pointer transition-all duration-300 group overflow-hidden"
                        :class="item === payType
                            ? (item === PayType.WX_PAY ? 'border-emerald-500 bg-emerald-50/20 shadow-lg shadow-emerald-100/30' : 'border-blue-500 bg-blue-50/20 shadow-lg shadow-blue-100/30')
                            : 'border-slate-100 bg-white hover:border-slate-200 hover:shadow-md'"
                        @click="payType = item;">

                        <!-- Top-right check indicator -->
                        <div class="absolute top-2.5 right-2.5 text-base" :class="item === payType
                            ? (item === PayType.WX_PAY ? 'text-emerald-500' : 'text-blue-500')
                            : 'text-slate-300'">
                            <el-icon v-if="item === payType">
                                <CircleCheckFilled />
                            </el-icon>
                            <el-icon v-else>
                                <CircleCheck />
                            </el-icon>
                        </div>

                        <!-- Brand Icon -->
                        <div class="mb-2">
                            <!-- WeChat Pay Icon SVG -->
                            <svg class="w-10 h-10 transition-transform duration-300 group-hover:scale-105"
                                viewBox="0 0 1228 1024" version="1.1" v-if="item === PayType.WX_PAY"
                                xmlns="http://www.w3.org/2000/svg">
                                <path
                                    d="M530.8928 703.1296a41.472 41.472 0 0 1-35.7376-19.8144l-2.7136-5.5808L278.272 394.752a18.7392 18.7392 0 0 1-2.048-8.1408 19.968 19.968 0 0 1 20.48-19.3536c4.608 0 8.8576 1.4336 12.288 3.84l234.3936 139.9296a64.4096 64.4096 0 0 0 54.528 5.9392L1116.2624 204.8C1004.9536 80.896 821.76 0 614.4 0 275.0464 0 0 216.576 0 483.6352c0 145.7152 82.7392 276.8896 212.2752 365.5168a38.1952 38.1952 0 0 1 17.2032 31.488 44.4928 44.4928 0 0 1-2.1504 12.3904l-27.6992 97.4848c-1.3312 4.608-3.328 9.3696-3.328 14.1312 0 10.752 9.216 19.3536 20.48 19.3536 4.4032 0 8.0384-1.536 11.776-3.584l134.5536-73.3184c10.1376-5.5296 20.7872-8.96 32.6144-8.96 6.2976 0 12.288 0.9216 18.0736 2.5088 62.72 17.0496 130.4576 26.5728 200.5504 26.5728C953.7024 967.168 1228.8 750.592 1228.8 483.6352c0-80.9472-25.4464-157.1328-70.0416-224.1024l-604.9792 436.992-4.4544 2.4064a42.1376 42.1376 0 0 1-18.432 4.1984z"
                                    fill="#15BA11"></path>
                            </svg>
                            <!-- Alipay Icon SVG -->
                            <svg class="w-10 h-10 transition-transform duration-300 group-hover:scale-105"
                                viewBox="0 0 1024 1024" version="1.1" v-if="item === PayType.ALI_PAY"
                                xmlns="http://www.w3.org/2000/svg">
                                <path
                                    d="M1024.0512 701.0304V196.864A196.9664 196.9664 0 0 0 827.136 0H196.864A196.9664 196.9664 0 0 0 0 196.864v630.272A196.9152 196.9152 0 0 0 196.864 1024h630.272a197.12 197.12 0 0 0 193.8432-162.0992c-52.224-22.6304-278.528-120.32-396.4416-176.64-89.7024 108.6976-183.7056 173.9264-325.3248 173.9264s-236.1856-87.2448-224.8192-194.048c7.4752-70.0416 55.552-184.576 264.2944-164.9664 110.08 10.3424 160.4096 30.8736 250.1632 60.5184 23.1936-42.5984 42.496-89.4464 57.1392-139.264H248.064v-39.424h196.9152V311.1424H204.8V267.776h240.128V165.632s2.1504-15.9744 19.8144-15.9744h98.4576V267.776h256v43.4176h-256V381.952h208.8448a805.9904 805.9904 0 0 1-84.8384 212.6848c60.672 22.016 336.7936 106.3936 336.7936 106.3936zM283.5456 791.6032c-149.6576 0-173.312-94.464-165.376-133.9392 7.8336-39.3216 51.2-90.624 134.4-90.624 95.5904 0 181.248 24.4736 284.0576 74.5472-72.192 94.0032-160.9216 150.016-253.0816 150.016z"
                                    fill="#009FE8"></path>
                            </svg>
                        </div>
                        <span class="text-sm font-bold text-slate-700">{{ PayType.getDescription(item) }}</span>
                    </div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex items-center gap-4 mt-4 pt-2">
                <button
                    class="w-1/2 cursor-pointer border border-slate-200 text-slate-550 bg-white hover:bg-slate-50 hover:text-slate-800 p-3 rounded-2xl font-bold shadow-xs transition-all duration-200 active:scale-95 flex items-center justify-center gap-1.5"
                    @click="cancelPay">
                    取消订单
                </button>
                <button
                    class="w-1/2 bg-gradient-to-r from-amber-400 via-yellow-400 to-amber-500 text-slate-900 font-bold p-3 rounded-2xl shadow-lg shadow-amber-200/50 hover:shadow-amber-300/60 active:scale-95 transition-all duration-200 cursor-pointer flex items-center justify-center gap-1.5 hover:brightness-105"
                    @click="toPay">
                    去支付
                    <svg class="w-4 h-4 stroke-current" fill="none" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5"
                            d="M14 5l7 7m0 0l-7 7m7-7H3" />
                    </svg>
                </button>
            </div>
        </div>
    </div>

    <!-- Sidebar Overlay -->
    <div class="fixed inset-0 bg-slate-900/40 backdrop-blur-xs z-45 transition-opacity duration-300 pointer-events-none"
        :class="moreCoupon ? 'opacity-100 pointer-events-auto' : 'opacity-0'" @click="moreCoupon = false"></div>

    <!-- Sidebar Coupon Drawer -->
    <div class="fixed inset-y-0 right-0 shadow-2xl w-full md:w-100 z-50 bg-white transition-all duration-500 overflow-y-auto"
        :class="moreCoupon ? 'translate-x-0' : 'translate-x-full'">
        <div class="flex flex-col gap-6 p-6 h-full">
            <div class="flex flex-col gap-4 sticky top-0 bg-white z-30 pb-4 border-b border-slate-100">
                <div class="title flex items-center justify-between">
                    <span class="text-lg font-bold text-slate-850">选择优惠券</span>
                    <button
                        class="w-8 h-8 rounded-full flex items-center justify-center bg-slate-50 text-slate-400 hover:text-slate-650 hover:bg-slate-100 transition-all active:scale-90 cursor-pointer"
                        @click="moreCoupon = false">
                        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <!-- Tab switching capsule -->
                <div class="flex bg-slate-100 p-1 rounded-xl">
                    <button
                        class="flex-1 py-2 text-xs font-bold rounded-lg cursor-pointer transition-all duration-200 flex items-center justify-center gap-1"
                        @click="curCouponTab = true"
                        :class="curCouponTab ? 'bg-white text-indigo-650 shadow-xs' : 'text-slate-550 hover:text-slate-800'">
                        可用券 ({{ canUseCoupon.length || 0 }})
                    </button>
                    <button
                        class="flex-1 py-2 text-xs font-bold rounded-lg cursor-pointer transition-all duration-200 flex items-center justify-center gap-1"
                        @click="curCouponTab = false"
                        :class="!curCouponTab ? 'bg-white text-indigo-650 shadow-xs' : 'text-slate-550 hover:text-slate-800'">
                        不可用券 ({{ unUseCoupon.length || 0 }})
                    </button>
                </div>

                <div class="flex justify-between items-center text-sm pt-1">
                    <span class="text-slate-500 font-semibold">预计共减免</span>
                    <span class="text-rose-500 font-extrabold text-base">-¥{{ discount }}</span>
                </div>
            </div>

            <!-- Coupon Cards List -->
            <div class="flex flex-col gap-4 overflow-y-auto grow pb-6">
                <!-- If empty -->
                <div v-if="(curCouponTab ? canUseCoupon : unUseCoupon).length === 0"
                    class="flex flex-col items-center justify-center py-12 text-slate-400 gap-2">
                    <svg class="w-12 h-12 text-slate-200" fill="none" viewBox="0 0 24 24" stroke="currentColor"
                        stroke-width="1.5">
                        <path stroke-linecap="round" stroke-linejoin="round"
                            d="M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span class="text-xs font-medium">暂无优惠券</span>
                </div>

                <!-- Cards list -->
                <div class="relative flex items-center h-28 rounded-2xl overflow-hidden shadow-sm transition-all duration-200 hover:scale-[1.01]"
                    :class="coupon.isAvailable
                        ? 'bg-linear-to-r from-amber-400 via-amber-550 to-orange-500 text-white cursor-pointer hover:shadow-md hover:shadow-amber-100/50'
                        : 'bg-slate-50 border border-slate-200/80 text-slate-400'"
                    v-for="coupon in (curCouponTab ? canUseCoupon : unUseCoupon)" :key="coupon.id"
                    @click="coupon.isAvailable && toggleCoupon(coupon)">

                    <!-- Left cutout notched punch holes -->
                    <div class="absolute top-0 left-[30%] -translate-y-1/2 w-3.5 h-3.5 bg-white rounded-full z-20">
                    </div>
                    <div class="absolute bottom-0 left-[30%] translate-y-1/2 w-3.5 h-3.5 bg-white rounded-full z-20">
                    </div>

                    <!-- Coupon value section (Left) -->
                    <div class="w-[30%] flex flex-col items-center justify-center border-r border-dashed border-white/30 h-full relative"
                        :class="!coupon.isAvailable ? 'border-slate-200' : ''">
                        <div class="flex items-baseline gap-0.5">
                            <span class="text-xs font-semibold">¥</span>
                            <span class="text-3xl font-black tracking-tight">{{ coupon.amount || 0 }}</span>
                        </div>
                    </div>

                    <!-- Coupon info section (Right) -->
                    <div class="w-[70%] flex items-center justify-between p-4 h-full">
                        <div class="flex flex-col gap-1 justify-center text-xs grow pr-2">
                            <span class="font-bold text-sm leading-tight"
                                :class="coupon.isAvailable ? 'text-white' : 'text-slate-650'">{{ coupon.name }}</span>
                            <span class="font-semibold text-[10px]"
                                :class="coupon.isAvailable ? 'text-amber-150/90' : 'text-slate-400'">{{
                                    substractTime(coupon.endTime || '') }} 后过期</span>
                            <!-- Ineligibility Reason -->
                            <span
                                class="text-[10px] text-red-500 font-semibold mt-1 bg-red-50 border border-red-100/50 px-2 py-0.5 rounded-md w-max"
                                v-if="!coupon.isAvailable">{{ coupon.reason || '当前商品不可用' }}</span>
                        </div>

                        <div class="shrink-0 flex items-center" v-if="coupon.isAvailable">
                            <svg class="w-6 h-6 transition-all duration-200"
                                :fill="(curCoupon?.id || undefined) == coupon.id ? '#ffffff' : 'rgba(255,255,255,0.4)'"
                                viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
                                <path
                                    d="M512 64C262.4 64 64 262.4 64 512s198.4 448 448 448 448-198.4 448-448S761.6 64 512 64z m236.8 326.4L454.4 684.8c-12.8 12.8-32 12.8-44.8 0L275.2 544c-12.8-12.8-12.8-32 0-44.8 12.8-12.8 32-12.8 44.8 0l115.2 115.2L704 339.2c12.8-12.8 32-12.8 44.8 0 12.8 12.8 12.8 38.4 0 51.2z">
                                </path>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { PayType } from '../types/payType';
import { Payer } from '../composables/payer';
import { CircleCheck, CircleCheckFilled } from '@element-plus/icons-vue';
import { CouponAPI, type CouponVO } from '@/services/coupon/coupon';
import { TimeUtils } from '@/utils/time';
import { MemberAPI, type MemberConfig } from '@/services/member';
import type { payForm } from '../types/pay';
import { PayFormBuilder } from '../utils/PayFormBuilder';
import router from '@/router';
import { useUserInfoStore } from '@/stores/userInfo';

const moreCoupon = ref<boolean>(false);
const commodityInfo = ref<MemberConfig>();
const couponList = ref<CouponVO[]>([]);
const payTypes = ref<PayType[]>([]);
const payer = new Payer();
const curCoupon = ref<CouponVO>();
const payType = ref<PayType>(PayType.WX_PAY);
const curCouponTab = ref<boolean>(true);
const commodityId = useRoute().query.id as string;
const quantity = useRoute().query.quantity as string;
const user = useUserInfoStore().user;
const typeId = useRoute().query.typeId as string;

const canUseCoupon = computed(() => {
    return couponList.value.filter(coupon => coupon.isAvailable === true);
});
const unUseCoupon = computed(() => {
    return couponList.value.filter(coupon => coupon.isAvailable === false);
});

const discount = computed(() => {
    if (!commodityInfo.value) return 0;
    const original = Number(commodityInfo.value.price);
    const finalPrice = calculatePrice();
    return Math.max(0, original - finalPrice).toFixed(2);
});

onMounted(async () => {
    if (!commodityId || !typeId) {
        router.back();
    }
    await loadPayTypes();
    await loadCouponList();
    commodityInfo.value = (await MemberAPI.queryMemberById(commodityId)).data;
});

function toggleCoupon(coupon: CouponVO) {
    if (curCoupon.value && coupon.id == curCoupon.value.id) {
        curCoupon.value = undefined;
        return;
    }
    curCoupon.value = coupon;
    moreCoupon.value = !moreCoupon.value;
}

function substractTime(time: string) {
    const date = TimeUtils.timestampToDate(time);
    return date;
}

async function loadCouponList() {
    couponList.value = (await CouponAPI.getCouponsForOrder(typeId, commodityId)).data;
}

function calculatePrice() {
    if (!commodityInfo.value) return 0;
    const d = commodityInfo.value?.discount;
    const res = Number(commodityInfo.value.price) * (d || 1) * ((curCoupon.value && curCoupon.value.discount) || 1) - (curCoupon.value && curCoupon.value.amount || 0);
    console.log('res', res);
    return res > 0.01 ? Number(res.toFixed(2)) : 0.01;
}

async function loadPayTypes() {
    const types: string[] = (await payer.getPayTypes()).data;
    payTypes.value = types
        .map((type) => PayType.fromType(type))
        .filter((t): t is PayType => t !== undefined);
}

async function toPay() {
    if (!commodityInfo.value) return;
    try {
        const form: payForm = {
            packageId: Number(commodityId),
            payType: payType.value,
            quantity: quantity ? Number(quantity) : 1,
            uname: user?.nick || '',
            couponId: curCoupon.value?.id ? Number(curCoupon.value.id) : null,
        };
        const res = await payer.pay(form);
        if (res.code === 1 && res.data) {
            PayFormBuilder.submitPayment(res.data);
        } else {
            console.error(res.errMsg || '支付请求失败');
        }
    } catch (error) {
        console.error('支付请求失败，请稍后重试', error);
    }
}

function cancelPay() {
    router.back();
}
</script>