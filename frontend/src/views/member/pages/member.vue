<template>
  <div class=" w-200 h-150 border rounded-md z-14 p-6 relative" :style="{
    backgroundImage: `url(https://static.nowcoder.com/fe/file/site/vip/vip-bg.png)`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  }">
    <div class="userInfo flex items-center h-12">
      <div class="avatar">
        <img v-avatar-skeleton class="w-12 h-12 rounded-full bg-gray-300" :src="user?.avatar" alt="头像" />
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
      <div class="flex-end align-top text-xl text-white cursor-pointer hover:text-red-300 hover:rotate-90 transition-all duration-300 p-1" @click="Close()">
        <el-icon>
          <CloseIcon />
        </el-icon>
      </div>
    </div>
    <!-- 会员与超级会员Tab栏 -->
    <div class="category flex items-end w-full h-14 mt-4 px-1 select-none relative z-10">
      <div v-for="(item, index) in memberType" :key="item.id"
        class="tab-btn relative px-6 h-11 flex items-center justify-center cursor-pointer transition-all duration-300"
        :class="[
          curTab === item.id ? 'z-20' : 'z-10',
          index === 1 ? 'ml-[-12px]' : ''
        ]"
        @click="
          loadMemberInfo(item.id);
          curTab = item.id
        ">
        <!-- Tab背景层 -->
        <div class="absolute inset-0 transition-all duration-300"
          :class="[
            index === 0 ? 'rounded-t-xl' : 'rounded-t-xl skew-x-[-15deg] origin-bottom-left',
            curTab === item.id
              ? (index === 0 ? 'bg-[#F7F9FC]' : 'bg-[#FFFDF4]')
              : (index === 0 ? 'bg-gradient-to-b from-[#1e293b] to-[#0f172a]' : 'bg-gradient-to-b from-[#2e2416] to-[#14100a] border-t border-[#d97706]/20')
          ]"></div>
        <!-- Tab内容层 -->
        <div class="relative z-10 flex items-baseline gap-2"
          :class="curTab === item.id
            ? (index === 0 ? 'text-[#1e293b] font-bold' : 'text-amber-800 font-bold')
            : 'text-white/80'">
          <span class="text-base font-bold">{{ item.name }}</span>
          <span class="text-[10px] opacity-70 truncate max-w-28">{{ item.description }}</span>
        </div>
      </div>
    </div>

    <div class="main w-full h-106 p-2 shadow-md rounded-b-md rounded-tr-md transition-colors duration-300"
      :class="curTab === (memberType?.[0]?.id) ? 'bg-[#F7F9FC]' : 'bg-[#FFFDF4]'">
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
                每天仅{{ item.duration ? (Number(item.price) / item.duration).toFixed(2) : '0.00' }}元
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
          <span class="text-sm font-bold text-slate-800">会员权益介绍</span>
          <span class="text-xs text-slate-400 hover:text-blue-500 cursor-pointer">更多详情 ></span>
        </div>
        <div class="main flex items-center h-30 mt-2 justify-around">
          <div v-for="item in MemberBenefits" :key="item.id" class="flex-1 h-30">
            <div class="w-full h-full flex flex-col items-center">
              <img class="w-12 h-12 rounded-md bg-gray-200" :src="item.url" alt="" />
              <span class="text-xs my-2 font-bold text-amber-700">{{ item.content }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>
<script lang="ts" setup>
import {Close as CloseIcon, Loading,} from '@element-plus/icons-vue'
import {computed, onMounted, ref} from 'vue'
import {MemberAPI, type MemberConfig} from '@/services/member'
import {useUserInfoStore} from '@/stores/userInfo'
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
