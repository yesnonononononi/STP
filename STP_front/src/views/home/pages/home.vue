<template>

  <div class="min-h-screen w-full relative flex flex-col bg-linear-to-br from-blue-100/70 to-blue-200">
    <div
      class="member w-full fixed inset-0 z-50 opacity-0 transition-all duration-300 flex flex-col items-center justify-center"
      :class="{ 'opacity-100 pointer-events-auto translate-y-0': toMember, ' translate-y-full  pointer-events-none': !toMember }">
      <member :visible="toMember" @close="toMember = false" />
    </div>
    <div class="tab bg-white shadow-md sticky top-0 z-13">
      <div class="w-350 h-16 m-auto flex items-center  gap-8">
        <span class="flex-3 flex w-72 items-center justify-between">
          <div class="flex items-center gap-2">
            <img src="/logo.png" class="w-12 h-12" alt="logo" />
            <span
              class="bg-clip-text text-transparent bg-linear-to-r from-blue-300 via-blue-200 to-blue-300 font-semibold text-2xl  ">STP</span>
          </div>
          <span @click="router.push({ name: 'home' })" class="cursor-pointer hover:text-blue-200">首页</span>
          <span>关注</span>
        </span>
        <div class="flex-3 search p-2 w-96">
          <el-input placeholder="请输入内容" class="w-96">
            <template #suffix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
        </div>
        <div class="flex-1 avatar h-14 flex flex-col group items-center relative gap-2">
          <img class="h-12 w-12 rounded-full bg-gray-300 relative z-20 cursor-pointer object-cover"
            :src="userProfile?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            alt="Avatar" referrerpolicy="no-referrer" />
          <div
            class="scale-y-0 h-96 origin-top absolute w-96 top-14 bg-white opacity-0 rounded-md shadow-md group-hover:opacity-100 hover:opacity-100 pointer-events-none group-hover:scale-y-100 group-hover:pointer-events-auto hover:h-96 hover:pointer-events-auto hover:scale-y-100 transition-all duration-300 z-10">
            <div class="w-full h-full flex flex-col">
              <div class="introduce flex-2 flex items-center p-2">
                <div class="introduce-avatar w-12 h-12">
                  <img class="w-12 h-12 object-cover rounded-full bg-gray-300"
                    :src="userProfile?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                    alt="Avatar" referrerpolicy="no-referrer" />
                </div>
                <div class="introduce-info flex flex-col w-full p-2 m-2">
                  <div class="nick flex items-center gap-2">
                    <span class="font-semibold  text-gray-800">{{ userProfile?.nick || '未登录' }}</span>
                    <img v-if="userProfile?.vipConfigIcon" :src="userProfile?.vipConfigIcon" alt=""
                      class="vip-level w-5 h-5 ">
                    <span v-else
                      class="w-auto rounded-lg bg-linear-to-r from-blue-100 to-blue-200 text-xs text-stone-500 p-1">{{
                        userProfile?.memberLevel
                      }}</span>
                  </div>
                  <div class="auhor text-xs text-gray-400">
                    <span>IP: {{ userProfile?.ip || '未知' }}</span>
                  </div>
                  <div class="operation ">
                    <span class="text-md text-blue-300 cursor-pointer"
                      @click="toUserProfile(userProfile?.id)">个人主页</span>
                  </div>
                </div>
              </div>
              <div class="body flex-2 bg-linear-to-r from-white via-blue-100 to-blue-150 mx-2 shadow-md rounded-lg">
                <div class="flex w-full items-center justify-between">
                  <div class="left flex flex-col items-start m-2 p-2">
                    <div class="flex items-center text-stone-700 font-semibold text-sm">
                      <span>{{ userProfile?.vipType || 'STP会员' }}</span>
                      <div class="w-12 h-4 rounded-xs ml-2 text-xs text-center text-white"
                        :class="userProfile?.vipExpireDate ? 'bg-yellow-500' : 'bg-gray-500'">
                        {{ userProfile?.vipExpireDate ? '已开通' : '未开通' }}
                      </div>
                    </div>
                    <span class="text-gray-400 text-xs mt-1">
                      {{ userProfile?.vipExpireDate ? '到期时间: ' + userProfile.vipExpireDate.split(' ')[0] : 'STP一路长虹'
                      }}
                    </span>
                  </div>
                  <div
                    class="right w-16 mr-8 cursor-pointer shadow-md h-8 flex flex-col justify-center items-center rounded-md bg-linear-to-r from-yellow-300 via-yellow-100 to-yellow-400">
                    <span class="text-sm text-stone-600" @click="toMember = true">立即{{ userProfile?.vipExpireDate ? "续期"
                      :
                      "开通"
                    }}</span>
                  </div>
                </div>
              </div>
              <div class="items flex-3">
                <div class="grid grid-cols-4 w-full">
                  <div class="collected flex flex-col items-center">
                    <div class="w-12 h-12">
                      <img src="" alt="">
                    </div>
                    <div>
                      <span>收藏</span>
                    </div>
                  </div>
                  <div class="benifit flex flex-col items-center">
                    <div class="w-12 h-12">
                      <img src="" alt="">
                    </div>
                    <div>
                      <span>权益</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="settings flex-1 flex  border-t  border-gray-100 ">
                <div class="w-1/2 flex items-center justify-center hover:text-blue-200 cursor-pointer"
                  @click="router.push({ name: 'userSettings' })">账号设置</div>
                <div class="w-1/2 flex items-center justify-center hover:text-blue-200 cursor-pointer" @click="logout">
                  退出登录
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="flex-3 right flex items-center justify-around cursor-pointer w-64">
          <div class="hover:text-blue-400 flex flex-col group items-center jsutify-center" @click="toMember = true">
            <img class="w-6 h-6 rounded-full group-hover:animate-bounce "
              src="https://static.nowcoder.com/fe/file/oss/1675240070182OPBSB.png" alt="" />
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
          <div
            class="hover:text-white cursor-pointer rounded-lg px-3 py-1  bg-linear-to-r from-blue-200 via-blue-100 to-blue-300"
            @click="newPageWithId(undefined, 'post')">
            发布
          </div>
        </div>
      </div>
    </div>
    <div class="w-full min-h-screen">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>
<script setup lang="ts">

import { ref, onMounted, computed } from 'vue'
import member from '@/views/member/pages/member.vue'
import { UserAPI, type UserProfileData } from '@/services/user'
import { useUserInfoStore } from '@/stores/userInfo'
import { Auther } from '@/views/auth/composables/Auth'
import router from '@/router'
import { newPageWithId } from '@/utils/page'

const toMember = ref(false)
const user = useUserInfoStore();
const userProfile = computed(() => user.user)


onMounted(async () => {
  await loadUserInfo();
})

/**
* 加载用户信息
*/
async function loadUserInfo() {
  if (!user.user) {
    try {
      const res = await UserAPI.getCurrentUser()
      user.setUser(res.data);
    } catch (e) {
      console.error('获取当前用户信息失败', e)
    }
  }
}

/**
 * 退出登录
 */
async function logout() {
  await Auther.getInstance().logout();
  user.clearUser();
  router.push({ name: 'login' })
}

function toUserProfile(id: number | undefined) {
  if (!id) return;
  router.push({ name: 'userProfile', params: { id: id } })
}
</script>
