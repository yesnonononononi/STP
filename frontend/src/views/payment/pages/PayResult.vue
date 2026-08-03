<template>
    <div class="relative z-1 w-full min-h-screen bg-linear-to-br from-blue-50/70 via-indigo-50/50 to-purple-50/70 flex items-center justify-center p-4 md:p-6 overflow-hidden select-none">
        <!-- Background decorative glowing spots -->
        <div class="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-gradient-to-br from-blue-400/20 to-indigo-400/20 rounded-full filter blur-3xl opacity-70 pointer-events-none"></div>
        <div class="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-gradient-to-tr from-purple-400/20 to-pink-400/20 rounded-full filter blur-3xl opacity-70 pointer-events-none"></div>

        <!-- Main card container -->
        <div class="w-full max-w-[480px] bg-white/85 backdrop-blur-xl border border-white/60 shadow-2xl rounded-3xl p-6 md:p-8 flex flex-col gap-6 relative z-10 transition-all duration-300 hover:shadow-indigo-150/40">
            <!-- Header -->
            <div class="text-center pb-4 border-b border-slate-100">
                <h1 class="font-extrabold text-xl text-slate-800 tracking-wide">支付结果</h1>
            </div>

            <!-- Status Visual Section -->
            <div class="flex flex-col items-center justify-center py-4 gap-3">
                <!-- Success State -->
                <div v-if="info?.status === OrderStatus.PAID || info?.status === OrderStatus.COMPLETED" class="relative flex items-center justify-center w-20 h-20 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-500 shadow-md">
                    <!-- Dynamic checkmark drawing SVG -->
                    <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                </div>

                <!-- Pending / Polling State -->
                <div v-else-if="info?.status === OrderStatus.PENDING" class="relative flex items-center justify-center w-20 h-20 rounded-full bg-indigo-50 border border-indigo-100 text-indigo-550 shadow-md">
                    <!-- Spinning loading circle SVG -->
                    <svg class="w-10 h-10 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                </div>

                <!-- Cancelled State -->
                <div v-else-if="info?.status === OrderStatus.CANCELLED" class="relative flex items-center justify-center w-20 h-20 rounded-full bg-slate-50 border border-slate-200 text-slate-500 shadow-md">
                    <!-- Cancel symbol SVG -->
                    <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </div>

                <!-- Error / Exception State -->
                <div v-else class="relative flex items-center justify-center w-20 h-20 rounded-full bg-rose-50 border border-rose-100 text-rose-500 shadow-md">
                    <!-- Warning / Alert sign SVG -->
                    <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                </div>

                <!-- Status Text -->
                <h2 class="text-lg font-bold text-slate-800 mt-2">
                    <span v-if="info?.status === OrderStatus.PAID">订单支付成功</span>
                    <span v-else-if="info?.status === OrderStatus.CANCELLED">订单已取消</span>
                    <span v-else-if="info?.status === OrderStatus.COMPLETED">订单已完成</span>
                    <span v-else-if="info?.status === OrderStatus.PENDING">
                        {{ isPolling ? '支付结果查询中...' : '暂未查询到支付状态' }}
                    </span>
                    <span v-else>订单状态异常</span>
                </h2>

                <p v-if="info?.status === OrderStatus.PENDING" class="text-xs text-slate-400 font-semibold text-center max-w-[280px]">
                    {{ isPolling ? '我们正在为您同步支付状态，请稍后...' : '暂未查询到最新的支付状态，您可以点击下方按钮手动同步' }}
                </p>
                <p v-else-if="info?.status === OrderStatus.PAID || info?.status === OrderStatus.COMPLETED" class="text-xs text-emerald-600 font-bold bg-emerald-50 px-3 py-1 rounded-full border border-emerald-100/50">
                    感谢您的购买，会员权益已开通
                </p>
            </div>

            <!-- Detail List Card -->
            <div class="flex flex-col bg-slate-50/50 border border-slate-100 rounded-2xl p-4 gap-3">
                <div class="flex justify-between items-center py-1.5 text-xs border-b border-slate-100 last:border-0"
                    v-for="(item, index) in infoList" :key="index">
                    <div class="font-semibold text-slate-450">{{ item[0] }}</div>
                    <div class="flex items-center gap-1">
                        <span class="font-bold text-slate-700 text-right">{{ item[1] || '无' }}</span>
                        <!-- Custom copy button for Order ID -->
                        <button v-if="item[0] === '订单流水号' && item[1] && item[1] !== '无'" 
                            @click="copyText(String(item[1]))"
                            class="p-1 rounded-md text-slate-400 hover:text-indigo-650 hover:bg-slate-150 transition-all active:scale-90 cursor-pointer">
                            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3" />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Actions Footer Buttons -->
            <div class="flex items-center gap-4 mt-2">
                <button @click="router.push({ name: 'home' })"
                    class="flex-1 cursor-pointer border border-slate-200 text-slate-550 bg-white hover:bg-slate-50 hover:text-slate-800 p-3 rounded-2xl font-bold shadow-xs transition-all duration-200 active:scale-95 text-center text-sm">
                    返回首页
                </button>
                <button v-if="info?.status === OrderStatus.PENDING" @click="handleReconcile"
                    class="flex-1 bg-gradient-to-r from-indigo-500 to-indigo-600 text-white font-bold p-3 rounded-2xl shadow-lg shadow-indigo-150/30 hover:brightness-105 active:scale-95 transition-all duration-200 cursor-pointer text-center text-sm">
                    手动刷新
                </button>
            </div>
        </div>
    </div>
    <Loading v-model="showLoading" :prompt="loadingPrompt" />
</template>

<script lang="ts" setup>
import router from '@/router'
import Loading from '@/presentation/components/loading.vue'
import {usePayResult} from '../composables/usePayResult'
import {log} from '@/utils/log'

const {
    info,
    isPolling,
    showLoading,
    loadingPrompt,
    infoList,
    handleReconcile,
    OrderStatus,
} = usePayResult()

function copyText(text: string) {
    navigator.clipboard.writeText(text).then(() => {
        log.success('已复制订单号到剪贴板')
    }).catch(() => {
        log.error('复制失败，请手动选择复制')
    })
}
</script>
