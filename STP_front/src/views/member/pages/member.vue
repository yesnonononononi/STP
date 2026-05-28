<template>
  <div class="w-260 h-150 border rounded-md z-10 p-6 relative" :style="{
    backgroundImage: `url(https://static.nowcoder.com/fe/file/site/vip/vip-bg.png)`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  }">
    <div class="userInfo flex items-center h-12">
      <div class="avatar">
        <img class="w-12 h-12 rounded-full bg-gray-300" :src="user?.avatar" alt="头像" />
      </div>
      <div class="description w-full h-12 m-2">
        <div class="nick text-white">{{ user?.nick || 'NAN' }}</div>
        <div class="ismember text-white text-sm">
          {{
            !user?.vipExpireDate
              ? '你还不是会员，开通立享特权'
              : `会员 ${user?.vipExpireDate.split(" ")[0]} 到期`
          }}
        </div>
      </div>
      <div class="flex-end align-top text-xl text-white cursor-pointer hover:text-blue-200" @click="Close()">
        <el-icon>
          <StarFilled />
        </el-icon>
      </div>
    </div>
    <div class="main w-full h-120 p-2 bg-blue-100 shadow-md mt-4 rounded-md">
      <div class="category flex items-center gap-10 w-full h-16 p-4 border-b border-gray-300">
        <div v-for="item in memberType" :key="item.id"
          class="flex-1 flex justify-center items-center h-12 hover:text-blue-400 cursor-pointer pb-2"
          :class="{ 'border-b-2 border-blue-500 text-blue-500': curTab === item.id }" @click="
            loadMemberInfo(item.id);
          curTab = item.id
            ">
          <span class="text-xl">{{ item.name }}</span>
          <span class="text-xs text-gray-400 ml-2 truncate block max-w-30">{{
            item.description
            }}</span>
        </div>
      </div>
      <Transition name="fade" mode="out-in">
        <div v-if="!loading" class="items w-full flex items-center h-40">
          <div v-for="item in items" :key="item.id"
            class="flex-1 flex flex-col items-center justify-center rounded-md m-4 shadow-md bg-white mb-2 hover:bg-blue-100 cursor-pointer group"
            @click="toPay(item.id)">
            <div class="name text-center m-2">
              <span class="group-hover:text-blue-400">
                {{ item.name }}
              </span>
            </div>
            <div class="price text-center m-2">
              <span class="text-red-500 font-bold text-2xl"> ￥{{ item.price }} </span>
            </div>
            <div class="description text-center">
              <span class="text-gray-400 text-xs">
                每天仅{{ (Number(item.price) / item.duration).toFixed(2) }}元
              </span>
            </div>
          </div>
        </div>
        <div v-else class="items w-full flex items-center justify-center h-40">
          <el-icon class="is-loading text-3xl text-blue-500">
            <Loading />
          </el-icon>
        </div>
      </Transition>
      <div class="avail_coupon w-full px-1 flex justify-between items-center">
        <div class="description">
          <span class="text-gray-400 text-sm">当前使用优惠券</span>
        </div>
        <div class="moreCoupon flex items-center gap-2">
          <span class="text-sm text-gray-500"> {{ curOoupon ? `优惠金额: ${curOoupon}` : "未使用优惠券" }}</span>
          <span class="text-sm text-gray-500 cursor-pointer hover:text-blue-300">更多></span>
        </div>
      </div>
      <div class="avail_introduce w-full h-40 my-2">
        <div class="flex items-center justify-between p-2">
          <span class="text-lg font-bold">会员权益介绍</span>
          <span class="hover:text-blue-300 cursor-pointer">更多详情 ></span>
        </div>
        <div class="main flex items-center h-30 mt-2 justify-around">
          <div v-for="item in MemberBenefits" :key="item.id" class="flex-1 h-30">
            <div class="w-full h-full flex flex-col items-center">
              <img class="w-12 h-12 rounded-md bg-gray-200" :src="item.url" alt="" />
              <span class="text-sm my-2 font-bold">{{ item.content }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="bottom w-full h-10 text-center">
        <div class="w-50 h-6 m-auto flex items-center">
          <div class="text-center w-full">
            <span class="text-xs text-gray-400">当前支付方式: </span>
            <span class="text-xs text-green-300">
              {{ PayType.getDescription(PayType.fromCode(payType) as PayType) }}
            </span>
            <div class="text-gray-600 cursor-pointer" @click="
              morePayType = true;
            loadPayTypes()
              ">
              更多支付方式 >
            </div>
          </div>
        </div>
      </div>
    </div>
    <div
      class="moreTypes fixed bottom-0 m-auto left-0 right-0 transition-all duration-500 p-4 w-100 h-100 bg-white shadow-md rounded-md"
      :class="{
        'pointer-events-auto translate-y-0': morePayType,
        'pointer-events-none translate-y-full': !morePayType,
      }">
      <div class="flex justify-end items-center w-full h-4">
        <span class="text-gray-400 font-bold cursor-pointer" @click="morePayType = false"> X </span>
      </div>
      <div class="body flex h-96 items-center flex-col gap-4">
        <div class="w-full flex-1 text-center">
          <span class="text-xl font-bold mb-2">更多支付方式</span>
          <div class="max-h-90 overflow-auto">
            <div v-for="(item, index) in payTypes" :key="index"
              class="flex items-center justify-between p-4 border-b border-gray-300 cursor-pointer"
              @click="payType = item">
              <div class="text-xl text-gray-500">{{ PayType.getDescription(item) }}</div>
              <div class="text-xl text-blue-200">
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
      </div>
    </div>
  </div>
  <div class="fixed opacity-10 inset-0 bg-gray-200"></div>
</template>
<script lang="ts" setup>
import { Payer } from '@/views/payment/composables/payer'

import {
  StarFilled,
  Loading,
  CircleCheckFilled,
  CircleCheck,
} from '@element-plus/icons-vue'
import { onActivated, onMounted, ref, computed } from 'vue'
import { PayType } from '@/views/payment/types/payType'
import type { memberConfig } from '@/views/member/types/member'
import { PayFormBuilder } from '@/views/payment/utils/PayFormBuilder'
import type { payForm } from '@/views/payment/types/pay'
import { log } from '@/utils/log'
import { MemberBase } from '@/views/member/composables/base'
import type { Coupon } from '@/views/payment/types/coupon'
import { useUserInfoStore } from '@/stores/userInfo'

defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  close: []
}>()
const memberType = ref<{ id: string; name: string; description: string }[]>()
const curTab = ref<string | null>(null)
const curOoupon = ref<Coupon | null>(null)
const loading = ref(true)
const userStore = useUserInfoStore()
const user = computed(() => userStore.user)
const payer = new Payer()
const payType = ref<number>(PayType.WX_PAY)
const payTypes = ref<PayType[]>([])
const base = new MemberBase()
const items = ref<memberConfig[]>([])
const morePayType = ref<boolean>(false)
const MemberBenefits = [
  {
    id: '1',
    url: 'https://static.nowcoder.com/fe/file/site/vip/icon/topic.png',
    content: '会员专属标识',
  },
  {
    id: '2',
    url: 'https://static.nowcoder.com/fe/file/site/vip/icon/exclusive-1.png',
    content: '等级成长加速',
  },
  {
    id: '3',
    url: 'https://static.nowcoder.com/fe/file/site/vip/icon/topic.png',
    content: '解锁专属资源',
  },
]

onMounted(async () => {
  if (memberType.value && memberType.value) return;
  memberType.value = (await base.queryMemberType()).data
  if (memberType.value && memberType.value.length !== 0 && memberType.value[0]) {
    curTab.value = memberType.value[0].id
    await loadMemberInfo(memberType.value[0].id)
  } else {
    loading.value = false
  }
})

function Close() {
  emit('close')
}

async function toPay(id: number) {
  try {
    const form: payForm = {
      packageId: id,
      payType: payType.value,
      quantity: 1,
      uname: 'admin',
      couponId: null,
    }

    const res = await payer.pay(form)

    if (res.code === 1 && res.data) {
      PayFormBuilder.submitPayment(res.data)
    } else {
      log.error(res.errMsg || '支付请求失败')
    }
  } catch (error) {
    log.error('支付请求失败，请稍后重试')
  }
}

async function loadMemberInfo(typeId: string) {
  loading.value = true
  try {
    items.value = (await base.queryMemberConfig(typeId)).data
  } finally {
    loading.value = false
  }
}
async function loadPayTypes() {
  const types: string[] = (await payer.getPayTypes()).data
  payTypes.value = types
    .map((type) => PayType.fromType(type))
    .filter((t): t is PayType => t !== undefined)
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
