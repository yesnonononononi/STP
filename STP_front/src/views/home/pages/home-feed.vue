<template>
  <div class="main relative flex min-h-screen flex-col items-center w-full">
    <div class="mid flex items-center justify-around gap-4 w-full max-w-[1400px] px-4 h-24 m-4">
      <div class="flex-1 bg-linear-to-br from-white to-amber-50/10 p-4 cursor-pointer group rounded-xl border border-gray-100 hover:border-amber-200 hover:shadow-[0_8px_30px_rgb(251,191,36,0.1)] hover:-translate-y-0.5 hover:scale-[1.01] transition-all duration-300" @click="router.push({ name: 'coupon' })">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-amber-50 rounded-xl group-hover:scale-110 group-hover:bg-amber-100 transition-all duration-300 shrink-0 flex items-center justify-center">
            <svg t="1781768147155" class="icon w-8 h-8" viewBox="0 0 1024 1024" version="1.1"
              xmlns="http://www.w3.org/2000/svg" p-id="5006">
              <path
                d="M836.152889 224.009481a75.851852 75.851852 0 0 1 75.851852 75.851852v116.129186a96.009481 96.009481 0 0 0 0 192v116.167111a75.851852 75.851852 0 0 1-75.851852 75.851851H187.847111a75.851852 75.851852 0 0 1-75.851852-75.851851v-116.167111a96.009481 96.009481 0 0 0 0-191.981038v-116.148148a75.851852 75.851852 0 0 1 75.851852-75.851852h648.305778z m-383.469037 138.733038a24.007111 24.007111 0 0 0-33.943704 33.943703l51.313778 51.313778h-46.061037a24.007111 24.007111 0 1 0 0 47.995259h64v32.009482h-64a24.007111 24.007111 0 1 0 0 47.995259h64v80.004741a24.007111 24.007111 0 1 0 48.014222 0l-0.018963-80.023704 64.018963 0.018963a24.007111 24.007111 0 1 0 0-47.995259h-64.018963v-32.009482h64.018963a24.007111 24.007111 0 1 0 0-47.995259h-46.08l51.332741-51.313778 1.744592-1.953185a24.007111 24.007111 0 0 0-35.688296-31.990518l-56.566518 56.566518-1.744593 1.953185-0.986074 1.365334a24.139852 24.139852 0 0 0-2.768593-3.318519z"
                fill="#f59e0b" p-id="5007"></path>
            </svg>
          </div>
          <div class="flex flex-col gap-0.5 justify-start">
            <span class="text-gray-800 font-semibold group-hover:text-amber-500 transition-colors duration-200 text-sm">优惠券</span>
            <span class="text-gray-400 text-xs">更多优惠福利</span>
          </div>
          <div class="ml-auto opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all duration-300 text-amber-500 shrink-0">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-4 h-4">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
            </svg>
          </div>
        </div>
      </div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
    </div>
    <div class="flex items-start w-full max-w-[1400px] px-4 gap-4 relative">
      <div class="left w-[70%] min-h-125 bg-white rounded-lg">
        <div
          class="tab flex items-center p-4 h-12 border-b-gray-200 border-b justify-between sticky top-16  bg-white z-9">
          <div class="grid grid-cols-6 p-2 w-[70%] sticky top-0 font-semibold ">
            <span class="flex-1 " v-for="item in items" :key="item.id"
              :class="curTab == item.id ? 'bg-clip-text text-transparent bg-linear-to-r transition-all duration-200 from-blue-200 to-blue-400' : ''">
              <span class="cursor-pointer" @click="curTab = item.id">
                {{ item.name }}
              </span>
            </span>
          </div>
          <div>
            <span class="w-[30%]">
            </span>
          </div>
        </div>
        <post-feed :tab-id="curTab" />
      </div>
      <div class="right w-[30%] m-2 self-stretch flex flex-col gap-4">
        <!-- 话题热榜 -->
        <div class="rank flex flex-col relative h-160 rounded-xl bg-white">
          <div class="flex items-center h-16 w-full justify-between border-b border-b-gray-200 p-4">
            <span class="text-xl font-bold">话题热榜</span>
            <span class="text-xs text-gray-400 cursor-pointer" @click="goToRankBoard('topic')">更多></span>
          </div>
          <div class="flex flex-col gap-4 p-4 grow overflow-y-auto">
            <div class="flex items-center justify-between" v-for="item in topicRankList" :key="item.id">
              <div class="left flex-3 flex items-center gap-2 truncate">
                <div class="title font-bold text-xl w-6 text-center"
                  :class="item.sort === 1 ? 'text-red-500' : (item.sort === 2) ? 'text-slate-500' : (item.sort === 3) ? 'text-yellow-600' : 'text-gray-400'">
                  {{ item.sort }}
                </div>
                <div
                  class="content text-md text-wrap truncate text-gray-800 font-medium hover:text-blue-500 cursor-pointer">
                  #{{ item.title }}#
                </div>
              </div>
              <div class="right flex-1 flex items-center justify-end gap-1 text-xs text-gray-500">
                <span>{{ formatNum(item.count || 0) }}</span>
                <svg t="1780667414716" class="icon size-4" viewBox="0 0 1024 1024" version="1.1"
                  :fill="(item.sort <= 3) ? '#d81e06' : '#bfbfbf'" xmlns="http://www.w3.org/2000/svg" p-id="5824">
                  <path
                    d="M702.08 558.72a469.12 469.12 0 0 0-50.56-210.56 776.64 776.64 0 0 0-105.6-186.56A778.24 778.24 0 0 0 467.2 86.4c-10.88-9.6-37.76-27.2-58.88-44.16S384 28.16 384 50.88c22.72 248-217.92 433.28-261.44 540.16-83.2 208.32 27.2 366.4 224 397.12 26.24 4.16 29.44-4.8 9.92-20.16a192 192 0 0 1-75.52-224c29.44-86.08 103.04-111.04 131.52-250.56 4.48-22.4 22.08-27.52 40.64-11.2a768 768 0 0 1 173.44 234.88c25.92 74.88 38.4 151.36-101.44 248.96-20.48 14.4 8.64 27.52 35.2 24.96C746.88 972.8 930.56 800 928 653.44c0-53.76-51.2-168-112.32-256-13.76-19.52-28.8-16.32-32 6.4-6.08 64-8.32 110.72-56 164.16-15.04 18.88-26.88 13.44-25.6-9.28z"
                    p-id="5825"></path>
                </svg>
              </div>
            </div>
            <div v-if="!topicLoading && topicRankList.length === 0"
              class="flex items-center justify-center h-full text-gray-400 text-sm">
              暂无话题热榜数据
            </div>
          </div>
          <loading v-model="topicLoading" />
        </div>

        <!-- 创作者周榜 -->
        <div class="sticky rounded-xl top-17">
          <div class="rank flex flex-col relative h-160 bg-white rounded-xl">
            <div class="flex items-center h-16 w-full justify-between border-b border-b-gray-200 p-4">
              <span class="text-xl font-bold">创作者周榜</span>
              <span class="text-xs text-gray-400 cursor-pointer" @click="goToRankBoard('creator')">更多></span>
            </div>
            <div class="flex flex-col gap-4 p-4 grow overflow-y-auto">
              <div class="flex items-center justify-between" v-for="ranker in creatorRankList" :key="ranker.id">
                <div class="left flex truncate items-center gap-2 max-w-[75%] min-w-0">
                  <div class="w-6 shrink-0 text-center font-bold text-lg"
                    :class="ranker.sort === 1 ? 'text-red-500' : (ranker.sort === 2 ? 'text-slate-500' : (ranker.sort === 3 ? 'text-yellow-600' : 'text-gray-400'))">
                    {{ ranker.sort }}
                  </div>
                  <div class="relative w-10 h-10 shrink-0">
                    <img :src="ranker.avatar || ''" alt="avatar"
                      class="w-10 h-10 rounded-full object-cover bg-gray-100">
                    <img v-if="ranker.decoration" :src="ranker.decoration" alt="decoration"
                      class="absolute inset-0 scale-125 w-10 h-10 pointer-events-none">
                  </div>
                  <div class="flex flex-col truncate min-w-0">
                    <span class="text-sm font-semibold truncate text-gray-800 hover:text-blue-500 cursor-pointer">{{
                      ranker.nickname }}</span>
                    <span class="text-xs text-gray-400 truncate">{{ ranker.ip }}</span>
                  </div>
                </div>
                <div class="right flex items-center justify-end gap-1 text-xs text-gray-500 shrink-0">
                  <span>{{ formatNum(Number(ranker.score)) }}</span>
                  <svg t="1780575765923" class="icon size-4" viewBox="0 0 1024 1024" version="1.1"
                    xmlns="http://www.w3.org/2000/svg" p-id="5385">
                    <path
                      d="M442.514286 73.142857c82.529524 64.24381 140.239238 126.610286 173.129143 187.099429 31.158857 57.295238 43.666286 115.907048 37.546666 175.835428l-1.219047 9.996191 6.095238-4.973715a174.055619 174.055619 0 0 0 49.249524-69.607619l2.681904-7.411809 7.704381-23.04c82.285714 55.734857 123.440762 150.064762 123.440762 283.062857C841.142857 823.515429 665.795048 950.857143 521.654857 950.857143c-144.11581 0-308.224-85.333333-334.750476-263.875048-26.550857-178.541714 83.480381-261.90019 158.427429-378.197333C395.288381 231.253333 427.690667 152.697905 442.514286 73.142857z"
                      p-id="5386" :fill="ranker.sort <= 3 ? '#d81e06' : '#bfbfbf'"></path>
                  </svg>
                </div>
              </div>
              <div v-if="!creatorLoading && creatorRankList.length === 0"
                class="flex items-center justify-center h-full text-gray-400 text-sm">
                暂无创作者数据
              </div>
            </div>
            <loading v-model="creatorLoading" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { Symbols } from '@/views/home/types/statics'
import postFeed from './post-feed.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Loading from '@/presentation/components/loading.vue'
import { RankBoardAPI } from '@/services/rank'
import type { MappedCommonRank, MappedCreatorRank } from '@/services/rank/types'
import { formatNum } from '@/utils/page'

const router = useRouter()
const curTab = ref('1')
const items: Symbols[] = [
  {
    name: '娱乐',
    id: '1',
  },
  {
    name: '直播',
    id: '2',
  },
  {
    name: '生活',
    id: '3',
  },
]

const topicLoading = ref(false)
const creatorLoading = ref(false)
const topicRankList = ref<MappedCommonRank[]>([])
const creatorRankList = ref<MappedCreatorRank[]>([])

onMounted(async () => {
  // 加载话题热榜
  try {
    topicLoading.value = true
    topicRankList.value = await RankBoardAPI.getRankListByTabId('topic', 6)
  } catch (e) {
    console.error('Failed to load topic rank board:', e)
  } finally {
    topicLoading.value = false
  }

  // 加载创作者周榜
  try {
    creatorLoading.value = true
    creatorRankList.value = await RankBoardAPI.getRankListByTabId('creator', 6)
  } catch (e) {
    console.error('Failed to load creator rank board:', e)
  } finally {
    creatorLoading.value = false
  }
})

// 跳转至排行榜页面
function goToRankBoard(tabId: 'topic' | 'creator' | 'post') {
  const tabMap = {
    creator: '创作者周榜',
    post: '热点榜',
    topic: '话题榜'
  }
  router.push({
    path: '/rank',
    query: { tab: tabMap[tabId] }
  })
}
</script>
