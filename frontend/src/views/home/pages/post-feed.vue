<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0  w-screen h-screen bg-gray-400/50 z-40">
      <div class="postInfo w-2/5 m-auto h-full ">
        <postInfo v-if="curPost" @close="curPost = null; visible = false" :visible="visible" :post="curPost" />
      </div>
    </div>
  </Teleport>
  <div class="main relative flex min-h-screen flex-col items-center w-full">
    <div class="mid flex items-center justify-around gap-4 w-full max-w-300 px-4 h-24 m-4">
      <div
        class="flex-1 bg-linear-to-br from-white to-amber-50/10 p-4 cursor-pointer group rounded-xl border border-gray-100 hover:border-amber-200 hover:shadow-[0_8px_30px_rgb(251,191,36,0.1)] hover:-translate-y-0.5 hover:scale-[1.01] transition-all duration-300"
        @click="router.push({ name: 'coupon' })">
        <div class="flex items-center gap-3">
          <div
            class="p-2 bg-amber-50 rounded-xl group-hover:scale-110 group-hover:bg-amber-100 transition-all duration-300 shrink-0 flex items-center justify-center">
            <svg t="1781768147155" class="icon w-8 h-8" viewBox="0 0 1024 1024" version="1.1"
              xmlns="http://www.w3.org/2000/svg" p-id="5006">
              <path
                d="M836.152889 224.009481a75.851852 75.851852 0 0 1 75.851852 75.851852v116.129186a96.009481 96.009481 0 0 0 0 192v116.167111a75.851852 75.851852 0 0 1-75.851852 75.851851H187.847111a75.851852 75.851852 0 0 1-75.851852-75.851851v-116.167111a96.009481 96.009481 0 0 0 0-191.981038v-116.148148a75.851852 75.851852 0 0 1 75.851852-75.851852h648.305778z m-383.469037 138.733038a24.007111 24.007111 0 0 0-33.943704 33.943703l51.313778 51.313778h-46.061037a24.007111 24.007111 0 1 0 0 47.995259h64v32.009482h-64a24.007111 24.007111 0 1 0 0 47.995259h64v80.004741a24.007111 24.007111 0 1 0 48.014222 0l-0.018963-80.023704 64.018963 0.018963a24.007111 24.007111 0 1 0 0-47.995259h-64.018963v-32.009482h64.018963a24.007111 24.007111 0 1 0 0-47.995259h-46.08l51.332741-51.313778 1.744592-1.953185a24.007111 24.007111 0 0 0-35.688296-31.990518l-56.566518 56.566518-1.744593 1.953185-0.986074 1.365334a24.139852 24.139852 0 0 0-2.768593-3.318519z"
                fill="#f59e0b" p-id="5007"></path>
            </svg>
          </div>
          <div class="flex flex-col gap-0.5 justify-start">
            <span
              class="text-gray-800 font-semibold group-hover:text-amber-500 transition-colors duration-200 text-sm">优惠券</span>
            <span class="text-gray-400 text-xs">更多优惠福利</span>
          </div>
          <div
            class="ml-auto opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all duration-300 text-amber-500 shrink-0">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5"
              stroke="currentColor" class="w-4 h-4">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
            </svg>
          </div>
        </div>
      </div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
    </div>
    <div class="flex items-start w-full max-w-300 px-4 gap-4 relative">
      <div class="left w-[75%] min-h-125 rounded-lg">
        <div
          class="tab flex items-center p-4 h-12 border-b-gray-200 border-b justify-between sticky top-16  bg-white z-9">
          <div class="grid grid-cols-6 p-2 w-[70%] sticky top-0 font-semibold ">
            <span class="flex-1 " v-for="item in items" :key="item.id"
              :class="curTab == item.id ? 'bg-clip-text text-transparent bg-linear-to-r transition-all duration-200 from-blue-200 to-blue-400' : ''">
              <span class="cursor-pointer" @click="handleTabClick(item.id)">
                {{ item.name }}
              </span>
            </span>
          </div>
          <div class="flex items-center gap-4 text-xs font-semibold text-gray-400 select-none mr-2">
            <span class="cursor-pointer px-2.5 py-1 rounded-md transition-all duration-200"
              :class="orderType === 'new' ? 'text-blue-500 bg-blue-50/80 scale-105 shadow-xs' : 'hover:text-gray-600 hover:bg-gray-50'"
              @click="toggleOrder('new')">
              最新
            </span>
            <span class="cursor-pointer px-2.5 py-1 rounded-md transition-all duration-200"
              :class="orderType === 'hot' ? 'text-amber-500 bg-amber-50/80 scale-105 shadow-xs' : 'hover:text-gray-600 hover:bg-gray-50'"
              @click="toggleOrder('hot')">
              最热
            </span>
          </div>
        </div>
        <div class="container-comments min-h-400 bg-white  w-full h-full flex flex-col">
          <PostItem v-for="item in topicList" :key="item.id" :post="item" @like="like" @collect="collect"
            @comment-click="curPost = $event; visible = true;" />
        </div>

      </div>
      <div class="right  w-[25%] ">
        <home-feed></home-feed>
      </div>
    </div>
  </div>



</template>

<script lang="ts" setup>
import {onMounted, onUnmounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import homeFeed from './rankboard-home.vue'
import {PostAPI, type PostVO} from '@/services/post'
import postInfo from '@/views/post/pages/postInfo.vue'
import PostItem from '@/views/post/pages/postItem.vue'
import {scrollerFromBottom} from '@/utils/scollerbar'
import {usePostInteractions} from '@/views/post/composables/usePostInteractions'
import router from '@/router'
import type {Symbols} from '../types/statics'
import {useAuthStore} from '@/views/auth/store'
import {log} from '@/utils/log'

const curTab = ref('1')
const orderType = ref('new')

function handleTabClick(tabId: string) {
  if (tabId === '2') {
    const authStore = useAuthStore()
    if (!authStore.token) {
      log.warning('请先登录后查看关注内容')
      authStore.showLoginDialog()
      return
    }
  }
  if (curTab.value === tabId) {
    if (route.query.keyword) {
      router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
    }
  } else {
    curTab.value = tabId
  }
}

function toggleOrder(type: string) {
  if (orderType.value === type) {
    if (route.query.keyword) {
      router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
    }
    return
  }
  orderType.value = type
  if (route.query.keyword) {
    router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
  } else {
    loadData()
  }
}


const cursor = ref<string | number | null>(null);
const loading = ref(false);
const noMore = ref(false);



const items: Symbols[] = [
  {
    name: '推荐',
    id: '1',
  },
  {
    name: '关注',
    id: '2',
  },

]
const curPost = ref<PostVO | null>(null)
const visible = ref(false)
const topicList = ref<PostVO[]>([])

const {
  like,
  collect
} = usePostInteractions(topicList)

const route = useRoute()

let remove: () => void;
onMounted(async () => {
  remove = scrollerFromBottom(async () => {
    await loadData(true)
  }, window) || (() => { })
  await loadData();
})

onUnmounted(() => {
  if (remove) remove()
})

async function loadData(append = false) {
  if (loading.value || (append && noMore.value)) return;
  loading.value = true;
  try {
    const keyword = route.query.keyword as string;
    if (keyword && keyword.trim()) {
      if (append) {
        noMore.value = true;
        return;
      }
      const res = await PostAPI.search(keyword.trim());
      if (res && res.code === 1 && res.data) {
        topicList.value = res.data;
        noMore.value = true;
        cursor.value = null;
      }
    } else {
      const currentCursor = append ? cursor.value : null;
      const res = await PostAPI.getPage({
        cursor: currentCursor,
        self: false,
        orderType: orderType.value
      });
      if (res.code === 1 && res.data) {
        if (append) {
          topicList.value = [...topicList.value, ...res.data];
        } else {
          topicList.value = res.data;
          noMore.value = false;
        }
        if (res.data.length === 0) {
          noMore.value = true;
        } else {
          cursor.value = res.data[res.data.length - 1]?.id || null;
        }
      }
    }
  } catch (e) {
    console.error('加载动态列表异常: ', e);
  } finally {
    loading.value = false;
  }
}

watch(() => curTab.value, () => {
  if (route.query.keyword) {
    router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
  } else {
    loadData();
  }
})

watch(() => route.query.keyword, () => {
  cursor.value = null;
  noMore.value = false;
  loadData();
})
</script>
