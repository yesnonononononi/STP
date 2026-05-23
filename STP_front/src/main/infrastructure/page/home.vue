<template>
  <div class="min-h-screen w-full relative flex flex-col bg-green-100/40">
    <div
      class="member w-full fixed inset-0 z-10 opacity-0 transition-opacity duration-300 flex flex-col items-center justify-center"
      :class="{ 'opacity-100 pointer-events-auto': toMember, 'pointer-events-none': !toMember }">
      <member :visible="toMember" @close="toMember = false" />
    </div>
    <div class="tab bg-white shadow-md sticky top-0 z-10">
      <div class="w-350 h-16 m-auto flex items-center justify-around gap-8">
        <span class="flex w-72 items-center justify-between">
          <img src="" alt="logo" />
          <span>首页</span>
          <span>关注</span>
        </span>
        <div class="search w-96">
          <el-input placeholder="请输入内容" class="w-96">
            <template #suffix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
        </div>
        <div class="avatar w-12 h-12 rounded-full bg-gray-200">
          <img class="w-12 h-12 rounded-full" src="" alt="" />
        </div>
        <div class="right flex items-center justify-around cursor-pointer w-64">
          <div class="hover:text-blue-400 flex flex-col items-center jsutify-center" @click="toMember = true">
            <img class="w-6 h-6 rounded-full" src="https://static.nowcoder.com/fe/file/oss/1675240070182OPBSB.png"
              alt="" />
            <span class="">会员</span>
          </div>
          <div class="flex flex-col  items-center justify-center hover:text-blue-400 cursor-pointer">
            <svg focusable="false" viewBox="0 0 80 80" fill="currentColor" width="20" height="20" aria-hidden="true"
              data-v-79ba69ea="">
              <g fill="none" fill-rule="evenodd" stroke="currentColor">
                <rect width="67.2" height="55.2" x="6.4" y="12.4" stroke-width="4.8" rx="12"></rect>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="5.2"
                  d="m10 18 25.2696 18.531c2.8157 2.0649 6.6453 2.065 9.4611.0006L70.0067 18h0"></path>
              </g>
            </svg>
            <span>消息</span>
          </div>
          <span>
            <img src="" alt="" />
            <span>设置</span>
          </span>
          <span>
            <img src="" alt="" />
            <div>发布</div>
          </span>
        </div>
      </div>
    </div>
    <div class="w-full min-h-screen">
      <HomeTabMain />
    </div>
  </div>
</template>
<script setup lang="ts">
import router from '@/shared/router/router'
import HomeTabMain from './home-tab-main.vue'
import { ref } from 'vue'
import member from '@/member/infrastructure/page/member.vue'
const toMember = ref(false)
</script>
