<template>
  <div class="main relative flex min-h-screen flex-col items-center w-full">
    <div class="mid flex itmes-center justify-around gap-4 w-350 h-24 m-4">
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
    </div>
    <div class="flex items-start w-350  gap-4 relative">
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
              <div>
                <img src="" alt="" />
                <span class="text-sm text-blue-400 cursor-pointer ">刷新</span>
              </div>
            </span>
          </div>
        </div>
        <home-tab-main-tab-main :tab-id="curTab" />
      </div>
      <div class="right w-[30%] m-2 self-stretch relative">
        <div class="rank h-160 bg-white">
          <div class="flex items-center h-12 w-full justify-between border-b border-b-gray-200 p-2">
            <span class="text-md font-bold">话题热榜</span>
            <span class="text-xs text-gray-400">更多></span>
          </div>
        </div>
        <div class="sticky top-17">
          <div class="rank h-160 mt-4 bg-white">
            <div class="flex items-center h-12 w-full justify-between border-b border-b-gray-200 p-2">
              <span class="text-md font-bold">创作者周榜</span>
              <span class="text-xs text-gray-400">更多></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { Symbols } from '@/views/home/types/statics'
import homeTabMainTabMain from './home-tab-main-tab-main.vue'
import { ref } from 'vue';
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
</script>
