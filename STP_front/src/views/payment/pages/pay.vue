<template>
    <div class="relative z-1 w-full min-h-screen flex items-center justify-center">
        <div class="w-full md:w-1/3 min-h-screen md:min-h-200 bg-white shadow-md flex flex-col p-2">
            <span class="font-bold m-auto text-2xl">确认订单</span>
            <div class="commodity-info h-auto flex items-center justify-between ">
                <div class="info flex gap-2 items-center p-2">
                    <svg t="1781577117271" class="icon size-15 shadow-sm" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="9977">
                        <path
                            d="M510.955102 831.738776c-23.510204 0-45.453061-9.926531-61.64898-27.167347L138.971429 468.114286c-28.734694-31.346939-29.779592-79.412245-1.567347-111.804082l117.55102-135.314286c15.673469-18.285714 38.661224-28.734694 63.216327-28.734694H705.306122c24.032653 0 47.020408 10.44898 62.693878 28.734694l118.073469 135.314286c28.212245 32.391837 27.689796 80.457143-1.567347 111.804082L572.081633 804.571429c-15.673469 17.240816-38.138776 27.167347-61.126531 27.167347z"
                            fill="#F2CB51" p-id="9978"></path>
                        <path
                            d="M506.77551 642.612245c-5.22449 0-10.971429-2.089796-15.15102-6.269388l-203.755102-208.979592c-7.836735-8.359184-7.836735-21.420408 0.522449-29.779592 8.359184-7.836735 21.420408-7.836735 29.779592 0.522449l189.12653 193.828572 199.053061-194.351021c8.359184-7.836735 21.420408-7.836735 29.779592 0.522449 7.836735 8.359184 7.836735 21.420408-0.522449 29.779592l-214.204081 208.979592c-4.179592 3.657143-9.404082 5.746939-14.628572 5.746939z"
                            fill="#FFF7E1" p-id="9979"></path>
                    </svg>
                    <div class="flex flex-col gap-2 justify-start ">
                        <span>{{ commodityInfo?.name || '未获取到商品信息' }}</span>
                        <span class="text-gray-500">×{{ quantity || 0 }}</span>
                    </div>
                </div>
                <div class="price flex flex-col gap-2 items-end">
                    <span class="font-bold">{{ commodityInfo?.price || '0.00' }}</span>
                    <span class="font-bold text-red-500 ">
                        {{ `优惠后约 ¥${calculatePrice().toFixed(2)}` }}
                    </span>
                </div>
            </div>
            <div class="body flex flex-col gap-4 grow mt-6">
                <div class="flex items-center justify-between p-2">
                    <span>商品金额</span>
                    <span class="font-semibold">
                        ¥{{ commodityInfo?.price || 0 }}
                    </span>
                </div>
                <div class="flex items-center justify-between p-2">
                    <div class="flex items-center gap-2">
                        <span>优惠券</span>
                    </div>
                    <div class=" cursor-pointer" @click="moreCoupon = true">
                        <span class="hover:text-blue-400">{{ curCoupon ? `- ¥ ${discount || 0}` :
                            `${canUseCoupon.length ||
                            0}张可用` }}</span>
                        <span class="ml-1">></span>
                    </div>
                </div>
                <div class="pay-ways flex flex-col grow">
                    <div v-for="(item, index) in payTypes" :key="index"
                        class="flex items-center justify-between p-4 border-b border-gray-300 cursor-pointer"
                        @click="payType = item;">
                        <div class="text-lg text-gray-500 flex gap-2 items-center">
                            <svg t="1780491846482" class="icon w-6 h-6" viewBox="0 0 1228 1024" version="1.1"
                                v-if="item == PayType.WX_PAY" xmlns="http://www.w3.org/2000/svg" p-id="5083">
                                <path
                                    d="M530.8928 703.1296a41.472 41.472 0 0 1-35.7376-19.8144l-2.7136-5.5808L278.272 394.752a18.7392 18.7392 0 0 1-2.048-8.1408 19.968 19.968 0 0 1 20.48-19.3536c4.608 0 8.8576 1.4336 12.288 3.84l234.3936 139.9296a64.4096 64.4096 0 0 0 54.528 5.9392L1116.2624 204.8C1004.9536 80.896 821.76 0 614.4 0 275.0464 0 0 216.576 0 483.6352c0 145.7152 82.7392 276.8896 212.2752 365.5168a38.1952 38.1952 0 0 1 17.2032 31.488 44.4928 44.4928 0 0 1-2.1504 12.3904l-27.6992 97.4848c-1.3312 4.608-3.328 9.3696-3.328 14.1312 0 10.752 9.216 19.3536 20.48 19.3536 4.4032 0 8.0384-1.536 11.776-3.584l134.5536-73.3184c10.1376-5.5296 20.7872-8.96 32.6144-8.96 6.2976 0 12.288 0.9216 18.0736 2.5088 62.72 17.0496 130.4576 26.5728 200.5504 26.5728C953.7024 967.168 1228.8 750.592 1228.8 483.6352c0-80.9472-25.4464-157.1328-70.0416-224.1024l-604.9792 436.992-4.4544 2.4064a42.1376 42.1376 0 0 1-18.432 4.1984z"
                                    fill="#15BA11" p-id="5084"></path>
                            </svg>
                            <svg t="1780491951378" class="icon w-6 h-6" viewBox="0 0 1024 1024" version="1.1"
                                v-if="item == PayType.ALI_PAY" xmlns="http://www.w3.org/2000/svg" p-id="5111">
                                <path
                                    d="M1024.0512 701.0304V196.864A196.9664 196.9664 0 0 0 827.136 0H196.864A196.9664 196.9664 0 0 0 0 196.864v630.272A196.9152 196.9152 0 0 0 196.864 1024h630.272a197.12 197.12 0 0 0 193.8432-162.0992c-52.224-22.6304-278.528-120.32-396.4416-176.64-89.7024 108.6976-183.7056 173.9264-325.3248 173.9264s-236.1856-87.2448-224.8192-194.048c7.4752-70.0416 55.552-184.576 264.2944-164.9664 110.08 10.3424 160.4096 30.8736 250.1632 60.5184 23.1936-42.5984 42.496-89.4464 57.1392-139.264H248.064v-39.424h196.9152V311.1424H204.8V267.776h240.128V165.632s2.1504-15.9744 19.8144-15.9744h98.4576V267.776h256v43.4176h-256V381.952h208.8448a805.9904 805.9904 0 0 1-84.8384 212.6848c60.672 22.016 336.7936 106.3936 336.7936 106.3936zM283.5456 791.6032c-149.6576 0-173.312-94.464-165.376-133.9392 7.8336-39.3216 51.2-90.624 134.4-90.624 95.5904 0 181.248 24.4736 284.0576 74.5472-72.192 94.0032-160.9216 150.016-253.0816 150.016z"
                                    fill="#009FE8" p-id="5112"></path>
                            </svg>
                            <span>{{ PayType.getDescription(item) }}</span>
                        </div>
                        <div class="text-lg text-blue-200">
                            <div v-if="item === payType">
                                <el-icon>
                                    <CircleCheckFilled />
                                </el-icon>
                            </div>
                            <div v-else>
                                <el-icon>
                                    <CircleCheck />
                                </el-icon>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="btn-submit flex items-center ">
                <button class="w-1/2  cursor-pointer p-1 rounded-md" @click="cancelPay">取消支付</button>
                <button class="w-1/2 bg-green-300 cursor-pointer p-1 rounded-md" @click="toPay">去支付</button>
            </div>
        </div>
    </div>
    <div class="fixed inset-y-0 right-0 shadow-md w-full md:w-1/4 z-12 bg-white transition-all duration-500  overflow-y-auto"
        :class="moreCoupon ? 'translate-x-0' : 'translate-x-full'">
        <div class="flex flex-col gap-2 p-4 ">
            <div class="flex flex-col gap-2 p-4  sticky top-0 bg-white">
                <div class="title flex items-center justify-between">
                    <span class="text-xl font-semibold">优惠券</span>
                    <span class="cursor-pointer" @click="moreCoupon = false">X</span>
                </div>
                <div class="tab flex m-auto w-full bg-gray-300 p-0.5 rounded-lg mt-8">
                    <div class="flex-1 flex items-center justify-center cursor-pointer" @click="curCouponTab = true"
                        :class="curCouponTab ? 'bg-white' : ''">
                        可用券 {{ canUseCoupon.length || 0 }}
                    </div>
                    <div class="flex-1 flex items-center justify-center cursor-pointer" @click="curCouponTab = false"
                        :class="curCouponTab ? '' : 'bg-white'">
                        不可用券 {{ unUseCoupon.length || 0 }}
                    </div>
                </div>
                <div class="flex justify-between items-center ">
                    <span>优惠券共减</span>
                    <span class="text-red-500 font-semibold">- ¥{{ discount }}</span>
                </div>
            </div>

            <div class="body flex flex-col gap-2 p-2">
                <div class="flex items-center p-2 h-24 md:h-32 from-amber-200/50  rounded-lg "
                    :class="coupon.isAvailable ? 'bg-linear-to-br via-yellow-100 to-amber-200/50 border-amber-500/30 border-2' : 'bg-gray-200'"
                    v-for="coupon in (curCouponTab ? canUseCoupon : unUseCoupon)" :key="coupon.id">
                    <div class="flex-1 flex items-center justify-center">
                        <div class="flex items-center gap-2 text-amber-400">
                            <span>¥</span>
                            <span class="text-2xl md:text-4xl font-semibold ">{{ coupon.amount || 0 }}</span>
                        </div>
                    </div>
                    <div class="w-px h-full bg-amber-400"></div>
                    <div class="flex-3 flex items-center justify-between p-3">
                        <div class="flex flex-col gap-2 justify-start text-sm md:text-md">
                            <span class="md:text-md text-sm">{{ coupon.name }}</span>
                            <span class="text-red-400">{{ substractTime(coupon.endTime || '') }} 后过期</span>
                        </div>
                        <div class="">
                            <svg t="1781582323066" class="icon size-6 cursor-pointer transition-color duration-200"
                                @click="toggleCoupon(coupon)" v-if="coupon.isAvailable" viewBox="0 0 1024 1024"
                                version="1.1" :fill="(curCoupon?.id || undefined) == coupon.id ? '#d81e06' : '#8a8a8a'"
                                xmlns="http://www.w3.org/2000/svg" p-id="5844">
                                <path
                                    d="M512 64C262.4 64 64 262.4 64 512s198.4 448 448 448 448-198.4 448-448S761.6 64 512 64z m236.8 326.4L454.4 684.8c-12.8 12.8-32 12.8-44.8 0L275.2 544c-12.8-12.8-12.8-32 0-44.8 12.8-12.8 32-12.8 44.8 0l115.2 115.2L704 339.2c12.8-12.8 32-12.8 44.8 0 12.8 12.8 12.8 38.4 0 51.2z"
                                    p-id="5845"></path>
                            </svg>
                            <span class="text-xs text-red-400" v-if="!coupon.isAvailable">{{ coupon.reason ||
                                '该优惠券不适用于当前商品' }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>

import { onMounted, ref, computed, watch } from 'vue';
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
const payTypes = ref<PayType[]>([])
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
})
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
    // 计算价格
    const d = commodityInfo.value?.discount;
    const res = Number(commodityInfo.value.price) * (d || 1) * ((curCoupon.value && curCoupon.value.discount) || 1) - (curCoupon.value && curCoupon.value.amount || 0)
    console.log('res', res)
    return res > 0.01 ? Number(res.toFixed(2)) : 0.01
}
async function loadPayTypes() {
    const types: string[] = (await payer.getPayTypes()).data
    payTypes.value = types
        .map((type) => PayType.fromType(type))
        .filter((t): t is PayType => t !== undefined)
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