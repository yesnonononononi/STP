<template>
  <div class=" mb-2 self-stretch flex flex-col gap-4">
    <!-- 话题热榜 -->
    <div
      class="rank flex flex-col relative h-160 rounded-xl bg-white border border-gray-100/70 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
      <div class="flex items-center h-16 w-full justify-between border-b border-b-gray-150/40 p-4">
        <span
          class="text-xl font-bold flex items-center before:content-[''] before:inline-block before:w-1 before:h-5 before:bg-linear-to-b before:from-rose-400 before:to-red-500 before:rounded-full before:mr-2">话题热榜</span>
        <span class="text-xs text-gray-400 cursor-pointer hover:text-red-500 transition-colors"
          @click="goToRankBoard('topic')">更多></span>
      </div>
      <div class="flex flex-col gap-3 p-4 grow overflow-y-auto select-none">
        <div
          class="flex items-center justify-between hover:bg-slate-50/60 p-2 -mx-2 rounded-lg transition-all duration-200 cursor-pointer"
          v-for="item in topicRankList" :key="item.id" @click="goToRankBoard('topic')">
          <div class="left flex-3 flex items-center gap-3 truncate">
            <!-- 序号徽章 -->
            <div class="w-6 h-6 flex items-center justify-center shrink-0">
              <span v-if="item.sort === 1"
                class="bg-linear-to-br from-rose-500 to-red-600 text-white font-bold text-xs italic rounded-md size-5.5 flex items-center justify-center shadow-[0_2px_8px_rgba(239,68,68,0.25)]">1</span>
              <span v-else-if="item.sort === 2"
                class="bg-linear-to-br from-slate-400 to-slate-500 text-white font-bold text-xs italic rounded-md size-5.5 flex items-center justify-center shadow-[0_2px_8px_rgba(100,116,139,0.25)]">2</span>
              <span v-else-if="item.sort === 3"
                class="bg-linear-to-br from-amber-400 to-amber-600 text-white font-bold text-xs italic rounded-md size-5.5 flex items-center justify-center shadow-[0_2px_8px_rgba(217,119,6,0.25)]">3</span>
              <span v-else class="text-gray-400 font-semibold text-sm italic">{{ item.sort }}</span>
            </div>
            <div
              class="content text-sm text-wrap truncate text-gray-800 font-medium hover:text-blue-500 transition-colors">
              #{{ item.title }}#
            </div>
          </div>
          <div class="right flex-1 flex items-center justify-end gap-1.5 text-xs text-gray-500">
            <span>{{ formatNum(item.count || 0) }}</span>
            <svg t="1780667414716" class="icon size-3.5" viewBox="0 0 1024 1024" version="1.1"
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
    <div class="sticky rounded-xl top-17 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100/70">
      <div class="rank flex flex-col relative h-160 bg-white rounded-xl">
        <div class="flex items-center h-16 w-full justify-between border-b border-b-gray-150/40 p-4">
          <span
            class="text-xl font-bold flex items-center before:content-[''] before:inline-block before:w-1 before:h-5 before:bg-linear-to-b before:from-blue-400 before:to-indigo-500 before:rounded-full before:mr-2">创作者周榜</span>
          <span class="text-xs text-gray-400 cursor-pointer hover:text-blue-500 transition-colors"
            @click="goToRankBoard('creator')">更多></span>
        </div>
        <div class="flex flex-col gap-3 p-4 grow overflow-y-auto select-none">
          <div
            class="flex items-center justify-between hover:bg-slate-50/60 p-2   -mx-2 rounded-lg transition-all duration-200 cursor-pointer"
            v-for="ranker in creatorRankList" :key="ranker.id"
            @click="router.push({ name: 'userProfile', params: { id: ranker.id } })">
            <div class="left flex truncate items-center gap-3 max-w-[75%] min-w-0">
              <!-- 序号徽章 -->
              <div class="w-6 h-6 flex items-center justify-center shrink-0">
                <span v-if="ranker.sort === 1"
                  class="bg-linear-to-br from-rose-500 to-red-600 text-white font-bold text-xs italic rounded-md size-5.5 flex items-center justify-center shadow-[0_2px_8px_rgba(239,68,68,0.25)]">1</span>
                <span v-else-if="ranker.sort === 2"
                  class="bg-linear-to-br from-slate-400 to-slate-500 text-white font-bold text-xs italic rounded-md size-5.5 flex items-center justify-center shadow-[0_2px_8px_rgba(100,116,139,0.25)]">2</span>
                <span v-else-if="ranker.sort === 3"
                  class="bg-linear-to-br from-amber-400 to-amber-600 text-white font-bold text-xs italic rounded-md size-5.5 flex items-center justify-center shadow-[0_2px_8px_rgba(217,119,6,0.25)]">3</span>
                <span v-else class="text-gray-400 font-semibold text-sm italic">{{ ranker.sort }}</span>
              </div>
              <div v-if="ranker.sort <= 3" class="w-11 h-11 relative shrink-0">
                <div class="w-[38px] h-[38px] absolute top-[6px] left-1/2 -translate-x-1/2 rounded-full bg-slate-200/60   z-1 overflow-hidden">
                  <img v-if="ranker.avatar" :src="ranker.avatar" alt="avatar"
                    class="w-full h-full object-cover opacity-0 transition-opacity duration-300"
                    @load="e => (e.target as HTMLImageElement).classList.remove('opacity-0')"
                    @error="e => { (e.target as HTMLImageElement).style.display = 'none' }">
                </div>
                <svg class="absolute inset-0 w-full h-full z-10 pointer-events-none" 
                  :class="[
                    ranker.sort === 1 ? 'text-amber-500' :
                    ranker.sort === 2 ? 'text-slate-400' :
                    'text-orange-500'
                  ]" 
                  viewBox="0 0 1024 1024">
                  <use href="#rank-avatar-frame-v2" />
                </svg>
              </div>
              <div v-else class="relative w-10 h-10 shrink-0 rounded-full bg-slate-200/60  overflow-hidden">
                <img v-if="ranker.avatar" :src="ranker.avatar" alt="avatar"
                  class="w-full h-full object-cover opacity-0 transition-opacity duration-300"
                  @load="e => (e.target as HTMLImageElement).classList.remove('opacity-0')"
                  @error="e => { (e.target as HTMLImageElement).style.display = 'none' }">
              </div>
              <div class="flex flex-col truncate min-w-0">
                <span class="text-sm font-semibold truncate text-gray-800 hover:text-blue-500 transition-colors">{{
                  ranker.nickname }}</span>
                <span class="text-xs text-gray-400 truncate">{{ ranker.ip }}</span>
              </div>
            </div>
            <div class="right flex items-center justify-end gap-1.5 text-xs text-gray-500 shrink-0">
              <span class="font-medium text-slate-700">{{ formatNum(Number(ranker.score)) }}</span>
              <svg t="1780575765923" class="icon size-3.5" viewBox="0 0 1024 1024" version="1.1"
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

    <!-- 隐藏的 SVG 图标库，用于复用头像边框路径以满足禁止代码行重复的约束 -->
    <svg style="display: none;">
      <g id="rank-avatar-frame-v2">
        <!-- 第一条路径：内侧遮罩圈 -->
        <path d="M512 582.62069m-441.37931 0a441.37931 441.37931 0 1 0 882.75862 0 441.37931 441.37931 0 1 0-882.75862 0Z" fill="none"></path>
        <!-- 第二条路径：外圈大环 -->
        <path d="M512 1024C268.358621 1024 70.62069 826.262069 70.62069 582.62069S268.358621 141.241379 512 141.241379 953.37931 338.97931 953.37931 582.62069 755.641379 1024 512 1024z m0-847.448276C287.77931 176.551724 105.931034 358.4 105.931034 582.62069s181.848276 406.068966 406.068966 406.068965 406.068966-181.848276 406.068966-406.068965S736.22069 176.551724 512 176.551724z" fill="currentColor"></path>
        <!-- 第三条路径：王冠和星星装饰 -->
        <path d="M384.882759 160.662069L370.758621 75.917241l31.77931 17.655173 30.013793-33.544828 42.372414 22.951724 38.841379-45.903448 38.84138 45.903448 42.372413-22.951724 30.013793 33.544828 31.779311-17.655173-14.124138 82.979311C600.275862 148.303448 556.137931 141.241379 512 141.241379s-88.275862 7.062069-127.117241 19.42069z m174.786207-67.089655l21.186206 26.482758 30.013794-17.655172-21.186207-24.717241-30.013793 15.889655z m-95.337932 0l-31.77931-15.889655-21.186207 24.717241 30.013793 17.655172 22.951724-26.482758z m61.793104-79.448276c0 7.062069-5.296552 14.124138-14.124138 14.124138-7.062069 0-14.124138-7.062069-14.124138-14.124138s5.296552-14.124138 14.124138-14.124138c7.062069 0 14.124138 7.062069 14.124138 14.124138z" fill="currentColor"></path>
      </g>
    </svg>
  </div>
</template>

<script lang="ts" setup>


import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import Loading from '@/presentation/components/loading.vue'
import {RankBoardAPI} from '@/services/rank'
import type {MappedCommonRank, MappedCreatorRank} from '@/services/rank/types'
import {formatNum} from '@/utils/page'

const router = useRouter()

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
