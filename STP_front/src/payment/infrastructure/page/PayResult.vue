<template>
    <div class="fixed inset-0 flex items-center justify-center bg-gray-100">
        <div class="w-200 h-full flex flex-col items-center p-4">
            <div class="body w-full flex-2">
                <div class="text-xl text-center w-full">支付结果</div>
                <div class="w-full text-center mt-6 text-4xl font-bold flex justify-center items-center gap-4 my-8">
                    <img class="w-12 h-12 rounded-full" v-if="info?.status == OrderStatus.PAID"
                        src="https://gd-hbimg.huaban.com/c3b0757c769cbcad3bc7932ef121c4348a70e321668e-KbIkMC_fw658"
                        alt="" referrerpolicy="no-referrer" />
                    <span v-if="info?.status == OrderStatus.PAID" class="block h-12">订单支付成功</span>
                    <span v-else-if="info?.status == OrderStatus.CANCELLED" class="block h-12">订单已取消</span>
                    <span v-else-if="info?.status == OrderStatus.COMPLETED" class="block h-12">订单已完成</span>
                    <span v-else-if="info?.status == OrderStatus.PENDING" class="block h-12">待支付</span>
                    <span v-else class="block h-12">订单异常</span>
                </div>



                <div class="w-full flex items-center justify-between h-10 border-b border-gray-300"
                    v-for="(item, index) in infoList" :key="index">
                    <div class="w-2/5 text-gray-500">{{ item[0] }}</div>
                    <div class="w-3/5 text-right">{{ item[1] }}</div>
                </div>
            </div>
            <div class="footer w-full h-16 flex items-center justify-between flex-1 p-8 gap-4">
                <div class="flex-1 text-center shadow-md rounded-lg cursor-pointer bg-blue-200 hover:scale-105"
                    @click="router.push({ name: 'home' })">
                    返回首页
                </div>
                <div v-if="info?.status == OrderStatus.PENDING" @click="handleReconcile"
                    class="flex-1 text-center shadow-md rounded-lg cursor-pointer bg-blue-200 hover:scale-105">
                    刷新
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { PayType } from '../types/PayType'
import { TimeUtils } from '@/shared/utils/time'
import { OrderAPI, type OrderQueryVO } from '@/api/order'
import router from '@/shared/router/router'
import { OrderStatus } from '@/utils/OrderStatus'
import { ElMessage } from 'element-plus'

const route = useRoute()
const info = ref<OrderQueryVO | undefined>(undefined)
const loading = ref(false)
const timeLeft = ref(0)
let timerId: any = null

const startCountdown = () => {
    if (timerId) clearInterval(timerId)

    const updateTimer = () => {
        if (!info.value || !info.value.createTime) {
            timeLeft.value = 0
            return
        }
        const createTimeMs = new Date(info.value.createTime).getTime()
        // 60秒支付超时限制
        const diff = Math.floor((createTimeMs + 60 * 1000 - Date.now()) / 1000)
        timeLeft.value = diff > 0 ? diff : 0
        if (timeLeft.value <= 0 && timerId) {
            clearInterval(timerId)
        }
    }

    updateTimer()
    timerId = setInterval(updateTimer, 1000)
}

const loadOrder = (orderId: string) => {
    OrderAPI.queryOrder(orderId).then((res) => {
        info.value = res.data
        if (info.value && info.value.status === OrderStatus.PENDING) {
            startCountdown()
        }
    })
}

// 从路由参数中获取支付结果数据
onMounted(() => {
    const orderId = route.query.orderId as string
    if (!orderId) return
    loadOrder(orderId)
})

onUnmounted(() => {
    if (timerId) clearInterval(timerId)
})

const handleReconcile = async () => {
    if (!info.value || !info.value.orderId) return
    loading.value = true
    try {
        await OrderAPI.reconcileOrder(info.value.orderId)
        ElMessage.success('对账成功，支付状态已更新！')
        loadOrder(String(info.value.orderId))
    } catch (err: any) {
        console.error(err)
    } finally {
        loading.value = false
    }
}

const infoList = computed(() => {
    if (!info.value) return []

    return [
        ['商品名称', info.value.memberName || '无'],
        ['订单流水号', info.value.orderId || '无'],
        ['应付金额', info.value.payableAmount || ''],
        ['实际支付', info.value.amount || '无'],
        ['支付时间', TimeUtils.timestampToDate(info.value.payTime || '')],
        ['支付方式', PayType.getDescription(info.value.payTypeName || '未知')],
    ]
})
</script>
