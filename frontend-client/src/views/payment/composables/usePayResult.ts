import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {PayType} from '@/views/payment/types/payType'
import {TimeUtils} from '@/utils/time'
import {OrderAPI, type OrderQueryVO} from '@/services/order'
import {OrderStatus} from '@/views/payment/types/orderStatus'
import {UserAPI} from '@/services/user'
import {useUserInfoStore} from '@/stores/userInfo'

export function usePayResult() {
  const route = useRoute()
  const orderId = route.query.orderId as string
  const userStore = useUserInfoStore()

  const info = ref<OrderQueryVO | undefined>(undefined)
  const loading = ref(false) // 针对手动对账
  const isPolling = ref(false) // 针对指数退避的轮询全屏
  const isUpdated = ref(false)

  let pollTimeoutId: any = null

  // 开启指数退避轮询，最多 5 次查询
  const startPolling = async () => {
    if (!orderId) return
    isPolling.value = true
    let attempt = 0
    const maxAttempts = 5

    const executePoll = async () => {
      attempt++
      try {
        const res = await OrderAPI.queryOrder(orderId)
        info.value = res.data

        // 一旦拿到非 PENDING 的结果（如支付成功、取消或已完成），即结束轮询
        if (info.value && info.value.status !== OrderStatus.PENDING) {
          isPolling.value = false
          return
        }
      } catch (err) {
        console.error(`第 ${attempt} 次查询订单支付状态失败:`, err)
      }

      if (attempt < maxAttempts) {
        // 计算指数退避延迟：1s, 2s, 4s, 8s
        const delay = 1000 * Math.pow(2, attempt - 1)
        pollTimeoutId = setTimeout(executePoll, delay)
      } else {
        // 达到 5 次，结束轮询
        isPolling.value = false
      }
    }

    await executePoll()
  }

  const stopPolling = () => {
    if (pollTimeoutId) {
      clearTimeout(pollTimeoutId)
      pollTimeoutId = null
    }
    isPolling.value = false
  }

  // 监视 info 的变化
  watch(info, async (newVal) => {
    if (newVal) {
      // 支付成功时且还未更新，则同步本地最新的用户信息
      if (newVal.status === OrderStatus.PAID && !isUpdated.value) {
        isUpdated.value = true
        try {
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

  onMounted(() => {
    startPolling()
  })

  onUnmounted(() => {
    stopPolling()
  })

  // 确认订单刷新
  const handleReconcile = async () => {
    if (!info.value || !info.value.orderId) return
    loading.value = true
    try {
      await OrderAPI.ackOrder(info.value.orderId)
      const res = await OrderAPI.queryOrder(String(info.value.orderId))
      info.value = res.data
    } catch (err: any) {
      console.error('确认订单执行异常:', err)
    } finally {
      loading.value = false
    }
  }

  const isSuccess = computed(() => {
    return info.value?.status === OrderStatus.PAID
  })

  const showLoading = computed(() => {
    return isPolling.value || loading.value
  })

  const loadingPrompt = computed(() => {
    return isPolling.value ? '支付结果查询中，请稍候...' : '正在同步支付状态...'
  })

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

  return {
    info,
    isPolling,
    showLoading,
    loadingPrompt,
    isSuccess,
    infoList,
    handleReconcile,
    OrderStatus,
  }
}
