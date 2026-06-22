<template>
  <div class=" w-200 h-150 border rounded-md z-14 p-6 relative" :style="{
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
    <div class="main w-full h-120 p-2 bg-[#F7F9FC]  shadow-md mt-4 rounded-md">
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
        <div v-if="!loading" class="   items w-full flex items-center h-40">
          <div v-for="item in items" :key="item.id"
            class="charge-card relative flex-1 flex flex-col items-center justify-center m-4 hover:border-transparent mb-2 cursor-pointer group rounded-2xl min-h-35  bg-linear-to-br from-white via-[#fffdf4] to-[#fffbeb] border border-amber-500/20 shadow-sm text-slate-800 hover:shadow-[0_16px_32px_-8px_rgba(245,158,11,0.25)] transition-all duration-300 hover:-translate-y-1.5"
            @click="toPay(item.id)">
            <div class="name-container text-center my-1 z-10">
              <span class="font-bold tracking-wide transition-colors duration-300 text-amber-900">
                {{ item.name }}
              </span>
            </div>

            <div
              class="text-center my-2 z-10 font-bold flex items-baseline justify-center bg-clip-text text-transparent bg-linear-to-r from-orange-600 to-amber-700">
              <span class="text-xs font-semibold mr-0.5">￥</span>
              <span class="text-3xl font-extrabold tracking-tight">{{ item.price }}</span>
            </div>

            <div class="desc-container text-center my-1 z-10">
              <span class="text-xs transition-opacity duration-300 text-stone-500'">
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
      <div class="avail_introduce w-full h-40 my-2">
        <div class="flex items-center justify-between p-2">
          <span class="text-lg font-bold">会员权益介绍</span>
          <span class="hover:text-blue-300 cursor-pointer">更多详情 ></span>
        </div>
        <div class="main flex items-center h-30 mt-2 justify-around">
          <div v-for="item in MemberBenefits" :key="item.id" class="flex-1 h-30">
            <div class="w-full h-full flex flex-col items-center">
              <img class="w-12 h-12 rounded-md bg-gray-200" :src="item.url" alt="" />
              <span
                class="text-sm my-2 font-bold bg-clip-text text-transparent bg-linear-to-r from-yellow-200  to-yellow-400">{{
                  item.content }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>
<script lang="ts" setup>
import {
  StarFilled,
  Loading,
} from '@element-plus/icons-vue'
import { onMounted, ref, computed } from 'vue'
import { MemberAPI, type MemberConfig } from '@/services/member'
import { useUserInfoStore } from '@/stores/userInfo'
import router from '@/router'

defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  close: []
}>()
const memberType = ref<{ id: string; name: string; description: string }[]>()
const curTab = ref<string | null>(null)
const loading = ref(true)
const userStore = useUserInfoStore()
const user = computed(() => userStore.user)

const items = ref<MemberConfig[]>([])
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
  if (memberType.value) return;
  memberType.value = (await MemberAPI.queryMemberType()).data
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
  emit('close')
  router.push({ name: 'payment', query: { id: id, quantity: 1, typeId: curTab.value } })
}

async function loadMemberInfo(typeId: string) {
  loading.value = true
  try {
    items.value = (await MemberAPI.queryMemberConfig(typeId)).data
  } finally {
    loading.value = false
  }
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



/* 价格字体排版 */
.price-val {
  font-size: 1.8rem;
  letter-spacing: -0.05em;
}
</style>
