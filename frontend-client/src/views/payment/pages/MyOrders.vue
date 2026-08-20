<template>
    <div class="relative z-12 w-full h-auto bg-slate-50 mt-1 select-none">
        <div class="md:w-1/2 w-full flex flex-col shadow-sm m-auto min-h-screen bg-white rounded-lg border border-slate-100 p-6">
            <!-- Breadcrumb Header -->
            <div class="flex items-center gap-1.5 text-xs text-slate-400 pb-4 border-b border-slate-100">
                <span class="cursor-pointer hover:text-slate-600 transition-colors" @click="router.push({ name: 'home' })">首页</span>
                <span>/</span>
                <span>个人</span>
                <span>/</span>
                <span class="text-slate-800 font-semibold">我的订单</span>
            </div>

            <!-- Page Title -->
            <div class="py-6 flex items-center justify-between">
                <h1 class="text-xl font-bold text-slate-800 tracking-wide">我的订单</h1>
                <span class="text-xs text-slate-400 font-medium">共 {{ orderList.length }} 条记录</span>
            </div>

            <!-- Status Tabs -->
            <div class="flex border-b border-slate-150 text-sm gap-8 mb-6">
                <button v-for="tab in tabs" :key="tab.value" 
                    @click="switchTab(tab.value)"
                    class="pb-3 font-bold relative transition-all cursor-pointer"
                    :class="curStatusTab === tab.value ? 'text-slate-800' : 'text-slate-400 hover:text-slate-600'">
                    {{ tab.name }}
                    <span v-if="curStatusTab === tab.value" class="absolute bottom-0 left-0 right-0 h-[2px] bg-slate-800"></span>
                </button>
            </div>

            <!-- Orders List -->
            <div class="flex-1 flex flex-col gap-4">
                <!-- Loading State -->
                <div v-if="loading && orderList.length === 0" class="flex flex-col items-center justify-center py-20 text-slate-400">
                    <span class="text-xs font-semibold animate-pulse">正在加载订单...</span>
                </div>

                <!-- Empty State -->
                <div v-else-if="orderList.length === 0" class="flex flex-col items-center justify-center py-20 text-slate-450 gap-3">
                    <svg class="w-12 h-12 text-slate-200" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 022 2h2a2 2 0 022-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                    <span class="text-xs font-medium">暂无相关订单</span>
                </div>

                <!-- List Items -->
                <div v-else v-for="order in orderList" :key="order.orderId" 
                    class="border border-slate-200 border-l-4 rounded-lg p-5 bg-gradient-to-r from-slate-50/40 via-white to-white hover:border-slate-400 hover:shadow-xs transition-all flex flex-col gap-4 relative overflow-hidden"
                    :class="[
                        OrderStatus.fromCode(order.status) === OrderStatus.PENDING ? 'border-l-amber-500/80' : '',
                        OrderStatus.fromCode(order.status) === OrderStatus.PAID ? 'border-l-blue-500/80' : '',
                        OrderStatus.fromCode(order.status) === OrderStatus.COMPLETED ? 'border-l-emerald-500/80' : '',
                        OrderStatus.fromCode(order.status) === OrderStatus.CANCELLED ? 'border-l-slate-400/80' : ''
                    ]">
                    
                    <!-- Card Header -->
                    <div class="flex justify-between items-center text-xs text-slate-450">
                        <div class="flex items-center gap-1.5">
                            <span class="font-medium">{{ TimeUtils.timestampToDate(order.createTime) }}</span>
                            <span class="text-slate-200">|</span>
                            <span class="font-mono font-medium">订单号: {{ order.orderId }}</span>
                            <button @click="copyOrderId(String(order.orderId))" 
                                class="text-slate-400 hover:text-slate-700 transition-colors p-0.5 rounded cursor-pointer"
                                title="复制订单号">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3" />
                                </svg>
                            </button>
                        </div>
                        <div class="flex items-center gap-2">
                            <span v-if="OrderStatus.fromCode(order.status) === OrderStatus.PENDING"
                                class="text-[10px] font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-md border border-amber-200/60 flex items-center gap-1">
                                <svg class="w-3 h-3 text-amber-500 animate-spin" style="animation-duration: 3s;" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                剩余 {{ getOrderCountdown(order) }}
                            </span>
                            <span class="text-[10px] font-extrabold uppercase tracking-wider px-2.5 py-0.5 rounded-md"
                                :class="[
                                    OrderStatus.fromCode(order.status) === OrderStatus.PENDING ? 'bg-amber-50 text-amber-700 border border-amber-200/50' : '',
                                    OrderStatus.fromCode(order.status) === OrderStatus.PAID ? 'bg-blue-50 text-blue-700 border border-blue-200/50' : '',
                                    OrderStatus.fromCode(order.status) === OrderStatus.COMPLETED ? 'bg-emerald-50 text-emerald-700 border border-emerald-200/50' : '',
                                    OrderStatus.fromCode(order.status) === OrderStatus.CANCELLED ? 'bg-slate-100 text-slate-600 border border-slate-200/50' : ''
                                ]">
                                {{ getStatusText(order.status) }}
                            </span>
                        </div>
                    </div>

                    <!-- Card Body -->
                    <div class="flex justify-between items-center py-1">
                        <div class="flex flex-col gap-1.5 align-start text-left">
                            <span class="font-bold text-slate-800 text-sm tracking-tight">{{ order.memberName || '会员商品' }}</span>
                            <span class="text-[11px] text-slate-400 font-semibold bg-slate-100/60 px-2 py-0.5 rounded-md w-max">数量：{{ order.memberId ? 1 : '1' }}</span>
                        </div>
                        <div class="text-right flex flex-col justify-center">
                            <span class="font-mono text-slate-850 font-black text-lg">¥{{ Number(order.amount).toFixed(2) }}</span>
                        </div>
                    </div>

                    <!-- Card Footer -->
                    <div class="flex justify-between items-center pt-3 border-t border-slate-100">
                        <div class="text-xs text-slate-400 font-medium">
                            <span v-if="OrderStatus.fromCode(order.status) === OrderStatus.PAID || OrderStatus.fromCode(order.status) === OrderStatus.COMPLETED">支付方式: {{ PayType.getDescription(order.payTypeName) }}</span>
                            <span v-else>待完成支付</span>
                        </div>
                        <div class="flex gap-2 items-center">
                            <!-- 订单详情按钮 -->
                            <button @click="goToOrderDetail(order.orderId)" 
                                class="px-3 py-1.5 text-xs font-bold rounded-md border border-slate-200 text-slate-600 bg-white hover:bg-slate-50 active:scale-95 transition-all cursor-pointer">
                                订单详情
                            </button>

                            <!-- Action Buttons -->
                            <template v-if="OrderStatus.fromCode(order.status) === OrderStatus.PENDING">
                                <button @click="cancelOrder(order.orderId)" 
                                    class="px-3 py-1.5 text-xs font-bold rounded-md border border-slate-200 text-slate-600 bg-white hover:bg-slate-50 active:scale-95 transition-all cursor-pointer">
                                    取消订单
                                </button>
                                <button @click="goToPay(order.orderId)" 
                                    class="px-3 py-1.5 text-xs font-bold rounded-md bg-amber-500 text-white hover:bg-amber-600 active:scale-95 transition-all cursor-pointer">
                                    去支付
                                </button>
                            </template>
                            <template v-else-if="OrderStatus.fromCode(order.status) === OrderStatus.PAID">
                                <button @click="ackOrder(order.orderId)" 
                                    class="px-3 py-1.5 text-xs font-bold rounded-md bg-slate-800 text-white hover:bg-slate-700 active:scale-95 transition-all cursor-pointer">
                                    确认订单
                                </button>
                            </template>
                            <template v-else>
                                <button @click="deleteOrderRecord(order.orderId)" 
                                    class="px-3 py-1.5 text-xs font-bold rounded-md border border-slate-200 text-slate-600 bg-white hover:bg-slate-50 active:scale-95 transition-all cursor-pointer">
                                    删除订单
                                </button>
                            </template>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Page Pagination -->
            <div v-if="orderList.length > 0" class="flex justify-between items-center mt-8 pt-4 border-t border-slate-100 text-xs">
                <button @click="prevPage" :disabled="page === 1"
                    class="px-3 py-2 border border-slate-200 rounded-md font-bold text-slate-600 disabled:opacity-40 hover:bg-slate-50 cursor-pointer transition-colors disabled:cursor-not-allowed">
                    上一页
                </button>
                <span class="font-bold text-slate-400">第 {{ page }} 页</span>
                <button @click="nextPage" :disabled="!hasMore"
                    class="px-3 py-2 border border-slate-200 rounded-md font-bold text-slate-600 disabled:opacity-40 hover:bg-slate-50 cursor-pointer transition-colors disabled:cursor-not-allowed">
                    下一页
                </button>
            </div>
        </div>
        <Loading v-model="loading" />
    </div>
</template>

<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref, watch} from 'vue';
import {OrderAPI, type OrderQueryVO} from '@/services/order';
import {PayAPI} from '@/services/payment';
import {PayFormBuilder} from '@/views/payment/utils/PayFormBuilder';
import {OrderStatus} from '@/views/payment/types/orderStatus';
import Loading from '@/presentation/components/loading.vue';
import {log} from '@/utils/log';
import router from '@/router';
import {PayType} from '@/views/payment/types/payType';
import {TimeUtils} from '@/utils/time';

const loading = ref<boolean>(false);
const page = ref<number>(1);
const pageSize = 10;
const orderList = ref<OrderQueryVO[]>([]);
const nowTick = ref<number>(Date.now());
let countdownTimer: any = null;

const tabs: { name: string; value: number | 'all' }[] = [
    { name: '全部订单', value: 'all' },
    { name: '待支付', value: OrderStatus.PENDING },
    { name: '已支付', value: OrderStatus.PAID },
    { name: '已完成', value: OrderStatus.COMPLETED },
    { name: '已取消', value: OrderStatus.CANCELLED }
];
const curStatusTab = ref<number | 'all'>('all');

const hasMore = computed(() => {
    return orderList.value.length === pageSize;
});

function getOrderCountdown(order: OrderQueryVO): string {
    let targetMs = 0;
    if (order.timeoutTime) {
        targetMs = new Date(order.timeoutTime).getTime();
    } else if (order.createTime) {
        targetMs = new Date(order.createTime).getTime() + 15 * 60 * 1000;
    } else {
        return '15:00';
    }

    const diffMs = targetMs - nowTick.value;
    if (diffMs <= 0) return '已超时';

    const totalSeconds = Math.floor(diffMs / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

function switchTab(statusVal: number | 'all') {
    if (curStatusTab.value === statusVal) return;
    curStatusTab.value = statusVal;
    page.value = 1;
    loadOrders();
}

watch(page, () => {
    loadOrders();
});

onMounted(() => {
    loadOrders();
    countdownTimer = setInterval(() => {
        nowTick.value = Date.now();
    }, 1000);
});

onUnmounted(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
});

async function loadOrders() {
    if (loading.value) return;
    loading.value = true;
    try {
        const statusParam = curStatusTab.value === 'all' ? undefined : Number(curStatusTab.value);
        const res = await OrderAPI.queryHistory(page.value, pageSize, statusParam);
        orderList.value = res.data || [];
    } catch {
        // 响应拦截器统一提示错误
    } finally {
        loading.value = false;
    }
}

function goToOrderDetail(orderId: string | number) {
    router.push({ name: 'payment', query: { orderId: String(orderId) } });
}

function getStatusText(status: any): string {
    const s = Number(status);
    switch (s) {
        case OrderStatus.PENDING:
            return '待支付';
        case OrderStatus.PAID:
            return '已支付';
        case OrderStatus.COMPLETED:
            return '已完成';
        case OrderStatus.CANCELLED:
            return '已取消';
        default:
            return '未知状态';
    }
}

async function cancelOrder(orderId: string | number) {
    try {
        await OrderAPI.deleteOrder(orderId);
        log.success('订单已成功取消');
        await loadOrders();
    } catch {
        // 响应拦截器统一提示错误
    }
}

async function goToPay(orderId: string | number) {
    try {
        loading.value = true;
        const res = await PayAPI.toPay(orderId);
        if (res && res.data) {
            if (typeof res.data === 'string') {
                if (res.data.startsWith('http://') || res.data.startsWith('https://') || res.data.startsWith('/')) {
                    window.location.href = res.data;
                } else {
                    PayFormBuilder.submitPayment(res.data);
                }
            }
        }
    } catch {
        // 响应拦截器统一提示错误
    } finally {
        loading.value = false;
    }
}

async function ackOrder(orderId: string | number) {
    try {
        loading.value = true;
        await OrderAPI.ackOrder(orderId);
        log.success('订单已确认');
        await loadOrders();
    } catch {
        // 响应拦截器统一提示错误
    } finally {
        loading.value = false;
    }
}

async function deleteOrderRecord(orderId: string | number) {
    try {
        await OrderAPI.deleteOrder(orderId);
        log.success('订单记录已删除');
        await loadOrders();
    } catch {
        // 响应拦截器统一提示错误
    }
}

function copyOrderId(id: string) {
    navigator.clipboard.writeText(id).then(() => {
        log.success('订单号已复制到剪贴板');
    }).catch(() => {
        log.error('复制失败，请手动选择复制');
    });
}

function prevPage() {
    if (page.value > 1) {
        page.value--;
    }
}

function nextPage() {
    if (hasMore.value) {
        page.value++;
    }
}
</script>
