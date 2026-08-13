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
          class="tab flex items-center p-4 h-12 border-b-gray-200 border-b justify-between sticky top-16 bg-white z-9">
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
        <div class="container-comments relative bg-white w-full h-full min-h-200 flex flex-col">
          <PostItem v-for="item in topicList" :key="item.id" :post="item" @like="like" @collect="collect"
            @comment-click="curPost = $event; visible = true;" />

          <!-- 触底加载更多状态 -->
          <div v-if="isAppending" class="flex items-center justify-center py-4 gap-2 text-gray-400 text-xs">
            <svg class="animate-spin size-4 text-blue-500" viewBox="0 0 24 24" fill="none">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path>
            </svg>
            <span>正在加载更多帖子...</span>
          </div>

          <!-- 没有更多内容提示 -->
          <div v-else-if="noMore && topicList.length > 0" class="flex items-center justify-center py-6 text-gray-300 text-xs select-none">
            <span>· 已经到底啦 ·</span>
          </div>

          <!-- 极简现代浅蓝风格空状态 -->
          <div v-if="!loading && topicList.length == 0" class="flex items-center min-h-160 justify-center py-12 select-none">
            <div class="flex items-center flex-col gap-4 text-center px-4 max-w-sm">
              <div class="relative flex items-center justify-center size-20 rounded-full bg-linear-to-b from-blue-50 to-blue-100/60 border border-blue-200/50 shadow-sm">
                <svg class="size-10 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m3.75 9v6m3-3H9m1.5-12H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                </svg>
                <div class="absolute -bottom-1 -right-1 size-6 rounded-full bg-blue-500 text-white flex items-center justify-center text-xs font-bold shadow-xs">
                  +
                </div>
              </div>
              <div class="flex flex-col gap-1">
                <span class="text-lg font-bold text-gray-800 tracking-wide">还没有发布过帖子哦</span>
                <span class="text-xs text-gray-400 leading-relaxed">探索有趣的话题，分享你的精彩生活与独特见解</span>
              </div>
              <button 
                @click="handlePublishClick" 
                class="mt-2 px-5 py-2.5 rounded-full bg-linear-to-r from-blue-500 to-indigo-500 hover:from-blue-600 hover:to-indigo-600 text-white font-medium text-sm shadow-md shadow-blue-500/20 active:scale-95 transition-all cursor-pointer flex items-center gap-1.5"
              >
                <svg class="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.5v15m7.5-7.5h-15" />
                </svg>
                <span>去发布第一篇帖子</span>
              </button>
            </div>
          </div>

          <!-- 首次或分类切换时居中加载遮罩 -->
          <Loading v-model="isInitialLoading" bgColor="bg-white/70 backdrop-blur-xs" />

        </div>
      </div>
      <div class="right w-[25%]">
        <home-feed></home-feed>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import homeFeed from './rankboard-home.vue'
import Loading from '@/presentation/components/loading.vue'
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
    cursor.value = null
    noMore.value = false
    topicList.value = []
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
  cursor.value = null
  noMore.value = false
  topicList.value = []
  if (route.query.keyword) {
    router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
  } else {
    loadData()
  }
}

const cursor = ref<string | number | null>(null);
const loading = ref(false);
const noMore = ref(false);

const isInitialLoading = computed({
  get: () => loading.value && topicList.value.length === 0,
  set: (val) => { loading.value = val }
});
const isAppending = computed(() => loading.value && topicList.value.length > 0);

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

watch(() => [route.query.keyword, route.query._t], () => {
  cursor.value = null;
  noMore.value = false;
  topicList.value = [];
  loadData();
});


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
  topicList.value = [];
  loadData();
})
</script>
