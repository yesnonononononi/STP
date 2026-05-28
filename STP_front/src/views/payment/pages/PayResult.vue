<template>
    <div class="fixed inset-0 flex items-center justify-center bg-gray-100">
        <div class="w-200 h-full flex flex-col items-center p-4">
            <div class="body w-full flex-2">
                <div class="text-xl text-center w-full">支付结果</div>
                <div class="w-full text-center mt-6 text-4xl font-bold flex justify-center items-center gap-4 my-8">
                    <img class="w-12 h-12 rounded-full" v-if="info?.status == OrderStatus.PAID"
                        src="https://gd-hbimg.huaban.com/c3b0757c769cbcad3bc7932ef121c4348a70e321668e-KbIkMC_fw658"
                        alt="" referrerpolicy="no-referrer" />
                    <svg v-if="info?.status == OrderStatus.PENDING" t="1779956224352"
                        class="icon w-12 h-12 animate-spin" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="5373" width="200" height="200">
                        <path
                            d="M511.882596 287.998081h-0.361244a31.998984 31.998984 0 0 1-31.659415-31.977309v-0.361244c0-0.104761 0.115598-11.722364 0.115598-63.658399V96.000564a31.998984 31.998984 0 1 1 64.001581 0V192.001129c0 52.586273-0.111986 63.88237-0.119211 64.337537a32.002596 32.002596 0 0 1-31.977309 31.659415zM511.998194 959.99842a31.998984 31.998984 0 0 1-31.998984-31.998984v-96.379871c0-51.610915-0.111986-63.174332-0.115598-63.286318s0-0.242033 0-0.361243a31.998984 31.998984 0 0 1 63.997968-0.314283c0 0.455167 0.11921 11.711527 0.11921 64.034093v96.307622a31.998984 31.998984 0 0 1-32.002596 31.998984zM330.899406 363.021212a31.897836 31.897836 0 0 1-22.866739-9.612699c-0.075861-0.075861-8.207461-8.370021-44.931515-45.094076L195.198137 240.429485a31.998984 31.998984 0 0 1 45.256635-45.253022L308.336112 263.057803c37.182834 37.182834 45.090463 45.253022 45.41197 45.578141A31.998984 31.998984 0 0 1 330.899406 363.021212zM806.137421 838.11473a31.901448 31.901448 0 0 1-22.628318-9.374279L715.624151 760.859111c-36.724054-36.724054-45.018214-44.859267-45.097687-44.93874a31.998984 31.998984 0 0 1 44.77618-45.729864c0.32512 0.317895 8.395308 8.229136 45.578142 45.411969l67.88134 67.88134a31.998984 31.998984 0 0 1-22.624705 54.630914zM224.000113 838.11473a31.901448 31.901448 0 0 0 22.628317-9.374279l67.88134-67.88134c36.724054-36.724054 45.021826-44.859267 45.097688-44.93874a31.998984 31.998984 0 0 0-44.776181-45.729864c-0.32512 0.317895-8.395308 8.229136-45.578142 45.411969l-67.88134 67.884953a31.998984 31.998984 0 0 0 22.628318 54.627301zM255.948523 544.058589h-0.361244c-0.104761 0-11.722364-0.115598-63.658399-0.115598H95.942765a31.998984 31.998984 0 1 1 0-64.00158h95.996952c52.586273 0 63.88237 0.111986 64.337538 0.11921a31.998984 31.998984 0 0 1 31.659414 31.97731v0.361244a32.002596 32.002596 0 0 1-31.988146 31.659414zM767.939492 544.058589a32.002596 32.002596 0 0 1-31.995372-31.666639v-0.361244a31.998984 31.998984 0 0 1 31.659415-31.970085c0.455167 0 11.754876-0.11921 64.34115-0.11921h96.000564a31.998984 31.998984 0 0 1 0 64.00158H831.944685c-51.936034 0-63.553638 0.111986-63.665624 0.115598h-0.335957zM692.999446 363.0176a31.998984 31.998984 0 0 1-22.863126-54.381656c0.317895-0.32512 8.229136-8.395308 45.41197-45.578141l67.88134-67.884953A31.998984 31.998984 0 1 1 828.693489 240.429485l-67.892177 67.88134c-31.020013 31.023625-41.644196 41.759794-44.241539 44.393262l-0.697201 0.722488a31.908673 31.908673 0 0 1-22.863126 9.591025z"
                            fill="" p-id="5374"></path>
                    </svg>
                    <span v-if="info?.status == OrderStatus.PAID" class="block h-12">订单支付成功</span>
                    <span v-else-if="info?.status == OrderStatus.CANCELLED" class="block h-12">订单已取消</span>
                    <span v-else-if="info?.status == OrderStatus.COMPLETED" class="block h-12">订单已完成</span>
                    <span v-else-if="info?.status == OrderStatus.PENDING" class="block h-12">支付结果查询中,请稍后 {{ timeout
                    }}</span>

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
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { PayType } from '@/views/payment/types/payType'
import { TimeUtils } from '@/utils/time'
import { OrderAPI, type OrderQueryVO } from '@/services/order'
import router from '@/router'
import { OrderStatus } from '@/views/payment/types/orderStatus'
import { log } from '@/utils/log'
import { useUserInfoStore } from '@/stores/userInfo'



import { UserAPI } from '@/services/user'

const route = useRoute()
const info = ref<OrderQueryVO | undefined>(undefined)
const loading = ref(false)
const timeout = ref(0)
const userStore = useUserInfoStore()
const isUpdated = ref(false)
let timerId: any = null
const orderId = route.query.orderId as string

watch(info, async (newVal) => {
    if (newVal) {
        // 如果订单状态已经不是待支付（如已支付、已完成、已取消等），则立即停止轮询
        if (newVal.status !== OrderStatus.PENDING) {
            stopTimeout()
        }
        
        // 支付成功时，同步更新本地用户信息
        if (newVal.status == OrderStatus.PAID && !isUpdated.value) {
            isUpdated.value = true
            try {
                // 从服务器重新获取最新的用户信息并更新本地 store
                const userRes = await UserAPI.getCurrentUser()
                if (userRes.code === 1 && userRes.data) {
                    userStore.setUser(userRes.data)
                }
            } catch (e) {
                console.error('获取/同步最新用户信息失败:', e)
            }
        }
    }
})
const startCountdown = () => {
    if (timerId) {
        stopTimeout();
    }
    timerId = setInterval(() => {

        if (timeout.value % 5 === 0) {
            loadOrder(orderId)
        }
        timeout.value = timeout.value + 1
        if (timeout.value > 60) {
            stopTimeout();
        }
    }, 1000);

}
const stopTimeout = () => {
    if (timerId) { clearInterval(timerId); timerId = null; timeout.value = 0 }
}
const loadOrder = (orderId: string) => {
    OrderAPI.queryOrder(orderId).then((res) => {
        info.value = res.data
    })
}

// 从路由参数中获取支付结果数据
onMounted(() => {
    if (!orderId) return
    loadOrder(orderId)
    startCountdown();
})

onUnmounted(() => {
    if (timerId) clearInterval(timerId)
})

const handleReconcile = async () => {
    if (!info.value || !info.value.orderId) return
    loading.value = true
    try {
        await OrderAPI.reconcileOrder(info.value.orderId)
        log.success('对账成功，支付状态已更新！')
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
