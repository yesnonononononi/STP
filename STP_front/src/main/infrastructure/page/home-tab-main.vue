<template>
  <div class="main flex min-h-screen flex-col items-center w-full">
    <div class="mid flex itmes-center justify-around gap-4 w-350 h-24 m-4">
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
    </div>
    <div class="flex items-center w-350 h-400">
      <div class="left w-[80%] h-full bg-white">
        <div class="tab flex items-center p-4 h-12 border-b-gray-200 border-b justify-between">
          <div class="grid grid-cols-6 p-2 w-[70%]">
            <span class="flex-1" v-for="item in items" :key="item.id">{{ item.name }}</span>
          </div>
          <div>
            <span class="w-[30%]">
              <div>
                <img src="" alt="" />
                <span class="text-xs text-green-300">刷新</span>
              </div>
            </span>
          </div>
        </div>
        <home-tab-main-tab-main />
      </div>
      <div class="right w-[20%] h-full m-2">
        <div class="rank h-160 bg-white">
          <div class="flex items-center h-12 w-full justify-between border-b border-b-gray-200 p-2">
            <span class="text-md font-bold">话题热榜</span>
            <span class="text-xs text-gray-400">更多></span>
          </div>
        </div>
        <div>
          <div class="rank h-160 mt-4 bg-white">
            <div
              class="flex items-center h-12 w-full justify-between border-b border-b-gray-200 p-2"
            >
              <span class="text-md font-bold">创作者周榜</span>
              <span class="text-xs text-gray-400">更多></span>
            </div>
          </div>
        </div>
        <div class=""></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { Symbols } from '../../api/types/statics'
import homeTabMainTabMain from './home-tab-main-tab-main.vue'
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
