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
      <div
        class="flex-end align-top text-xl text-white cursor-pointer hover:text-red-300 hover:rotate-90 transition-all duration-300 p-1"
        @click="Close()">
        <el-icon>
          <CloseIcon />
        </el-icon>
      </div>
    </div>
    <!-- 会员与超级会员Tab栏 -->
    <div class="category flex items-end w-full h-14 mt-4  select-none relative z-10">
      <div v-for="(item, index) in memberType" :key="item.id"
        class="tab-btn relative px-6 h-11 flex items-center justify-center cursor-pointer transition-all duration-300"
        :class="[
          curTab === item.id ? 'z-20' : 'z-10',
          index === 1 ? '-ml-3' : ''
        ]" @click="
          loadMemberInfo(item.id);
        curTab = item.id
          ">
        <!-- Tab背景层 -->
        <div class="absolute inset-0 transition-all duration-300" :class="[
          index === 0 ? 'rounded-t-xl' : 'rounded-t-xl skew-x-[-15deg] origin-bottom-left',
          curTab === item.id
            ? (index === 0 ? 'bg-[#F7F9FC]' : 'bg-[#FFFDF4]')
            : (index === 0 ? 'bg-linear-to-b from-[#1e293b] to-[#0f172a]' : 'bg-linear-to-b from-[#2e2416] to-[#14100a] border-t border-[#d97706]/20')
        ]"></div>
        <!-- Tab内容层 -->
        <div class="relative z-10 flex items-baseline gap-2" :class="curTab === item.id
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
        <div v-if="!loading" class="items relative w-full flex items-center h-40">
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
              <span class="text-3xl font-extrabold tracking-tight">{{ calcPrice(item)
              }}</span>
            </div>
            <div class="flex gap-2 items-center">
              <div v-if="item.discount < 1" class="text-gray-400 line-through">￥{{ item.price }}</div>
              <div
                class="text-sm bg-clip-text text-transparent bg-linear-to-br from-amber-300 via-yellow-400 to-amber-400">
                {{
                  item.discount * 10 }}折优惠</div>
            </div>


            <div class="desc-container text-center my-1 z-10">
              <span class="text-xs transition-opacity duration-300 text-stone-500 tracking-wider">
                平均每天约{{ calcAveragePriceForDays(item) }}元
              </span>
            </div>
          </div>
          <div v-if="items && items.length == 0" class="flex items-center justify-center w-full">
            <div class="flex flex-col items-center gap-2  ">
              <svg t="1786091018186" class="icon size-24" viewBox="0 0 1024 1024" version="1.1"
                xmlns="http://www.w3.org/2000/svg" p-id="5828">
                <path
                  d="M843.776 402.432c9.216 16.384-12.288-30.72-48.128-30.72H227.328c-21.504 0-35.84 9.216-48.128 28.672-11.264 18.432-116.736 179.2-116.736 211.968V829.44c0 33.792 28.672 62.464 62.464 62.464h774.144c33.792 0 62.464-26.624 62.464-62.464V612.352c0-30.72-117.76-209.92-117.76-209.92s-16.384-30.72 0 0zM641.024 598.016c-14.336 0-26.624 7.168-28.672 21.504v5.12c0 55.296-45.056 100.352-100.352 100.352s-100.352-45.056-100.352-100.352v-7.168c-7.168-19.456-28.672-19.456-28.672-19.456H117.76l91.136-164.864s16.384-28.672 40.96-28.672h535.552c19.456 0 21.504 7.168 35.84 28.672L911.36 598.016H641.024zM318.464 285.696l-69.632-66.56c-7.168-5.12-7.168-16.384 0-23.552 7.168-7.168 19.456-7.168 26.624 0l69.632 64.512c7.168 7.168 7.168 19.456 0 26.624-7.168 3.072-19.456 6.144-26.624-1.024m188.416-28.672c-2.048-2.048-5.12-7.168-5.12-12.288l-2.048-93.184c0-9.216 7.168-19.456 16.384-19.456 9.216-2.048 19.456 5.12 19.456 14.336l2.048 93.184c0 7.168-5.12 14.336-12.288 16.384-6.144 5.12-13.312 5.12-18.432 1.024m177.152 23.552c-2.048-2.048-5.12-7.168-5.12-12.288s2.048-9.216 5.12-12.288l64.512-69.632c7.168-7.168 19.456-7.168 26.624 0s7.168 19.456 0 26.624l-64.512 69.632c-2.048 2.048-7.168 5.12-12.288 5.12-7.168-2.048-12.288-2.048-14.336-7.168"
                  fill="#999999" p-id="5829"></path>
              </svg>
              <span class="text-md text-gray-500">暂无会员套餐</span>
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
import { Close as CloseIcon, Loading, } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
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
function calcPrice(item: MemberConfig) {
  return (Number(item.price) * item.discount).toFixed(2)
}


function calcAveragePriceForDays(item: MemberConfig) {
  let price = calcPrice(item);
  return item.duration ? (Number(price) / item.duration).toFixed(2) : '0.00'
}

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
