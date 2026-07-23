<template>

  <div class="min-h-screen w-full relative flex flex-col bg-linear-to-br from-blue-100/70 to-blue-200">
    <Teleport to="body">
      <div class="fixed inset-0 z-1000 bg-black/40 backdrop-blur-xs flex flex-col items-center justify-center"
        v-if="toMember" @click.self="toMember = false">
        <div class="relative scale-90 md:scale-100 transition-all duration-300">
          <member :visible="toMember" @close="toMember = false" />
        </div>
      </div>
    </Teleport>
    <div class="tab bg-white shadow-md sticky top-0 z-13">
      <div class="w-full max-w-300 px-2 md:px-4 h-16 m-auto flex items-center justify-between gap-2 md:gap-8">
        <span class="flex flex-row items-center w-auto gap-2 md:gap-4 shrink-0">
          <div class="flex items-center gap-1.5 md:gap-2">
            <img src="/logo.png" class="size-4 md:size-6" alt="logo" />
            <span class="  text-lg md:text-2xl hidden sm:inline">STP</span>
          </div>
          <span @click="toggleTab('home')" class="cursor-pointer hover:text-blue-200 text-xs md:text-base shrink-0"
            :class="curTab === 'home' ? 'text-blue-300 font-semibold' : ''">首页</span>
        </span>
        <div class="hidden lg:block flex-3 search p-2 w-96 shrink">
          <el-input v-model="keyword" placeholder="请输入内容" class="w-96" @keyup.enter="handleSearch">
            <template #suffix>
              <svg t="1781494755543" class="icon size-4 cursor-pointer " viewBox="0 0 1024 1024" version="1.1"
                @click="handleSearch" xmlns="http://www.w3.org/2000/svg" p-id="5766">
                <path
                  d="M644.096 251.904a277.333333 277.333333 0 1 0-392.192 392.192 277.333333 277.333333 0 0 0 392.192-392.192zM191.573333 191.573333a362.666667 362.666667 0 0 1 541.269334 480.938667l228.053333 228.053333-60.330667 60.330667-228.053333-228.053333A362.709333 362.709333 0 0 1 191.573333 191.573333z"
                  fill="#bfbfbf" p-id="5767"></path>
              </svg>
            </template>
          </el-input>
        </div>
        <div class="avatar h-14 flex flex-col group items-center justify-center relative gap-2 shrink-0">
          <img v-avatar-skeleton class="h-8 w-8 md:h-12 md:w-12 rounded-full bg-gray-300 relative z-20 cursor-pointer object-cover"
            :src="userProfile?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            alt="Avatar" referrerpolicy="no-referrer" @click="handleAvatarClick" />
          <div v-if="authStore.token"
            class="scale-y-0 h-96 origin-top absolute w-84 top-14 bg-white opacity-0 rounded-md shadow-md group-hover:opacity-100 hover:opacity-100 pointer-events-none group-hover:scale-y-100 group-hover:pointer-events-auto hover:h-96 hover:pointer-events-auto hover:scale-y-100 transition-all duration-300 z-10">
            <div class="w-full h-full flex flex-col">
              <div class="introduce flex-2 flex items-center p-2">
                <div class="introduce-avatar flex items-center justify-center w-1/4 ">
                  <img v-avatar-skeleton class="size-12 object-cover rounded-full bg-gray-300"
                    :src="userProfile?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                    alt="Avatar" referrerpolicy="no-referrer" />
                </div>
                <div class="introduce-info flex flex-col w-3/4 p-2 m-2">
                  <div class="nick flex items-center gap-2">
                    <span class="font-semibold  text-gray-800">{{ userProfile?.nick || '未登录' }}</span>

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
                      {{ userProfile?.vipExpireDate ? '到期时间: ' + userProfile.vipExpireDate.split(' ')[0] : '您还未开通会员服务'
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
              <div class="items flex-3 mx-2 mt-4 pb-2">
                <div class="grid grid-cols-4 w-full gap-2">
                  <!-- 每日签到 -->
                  <div class="signin flex flex-col items-center cursor-pointer group/item relative" @click="showDailySignIn = true">
                    <div class="relative size-12 flex items-center justify-center">
                      <svg t="1784189983423" class="icon size-8 group-hover/item:scale-105 transition-transform duration-300" viewBox="0 0 1024 1024"
                          version="1.1" xmlns="http://www.w3.org/2000/svg">
                          <path
                              d="M961.184 316.768V205.44c0-20.736-17.28-37.728-38.4-37.728h-205.44V102.4a38.4 38.4 0 0 0-76.768 0v65.344H352.64V102.4a38.4 38.4 0 0 0-76.8 0v65.344H70.4c-21.12 0-38.368 16.96-38.368 37.76v111.264h929.184zM32 392.224V922.24C32 943.04 49.28 960 70.4 960h852.384c21.12 0 38.4-16.96 38.4-37.728V392.224H32zM448.608 814.72c-7.68 5.664-15.36 9.44-24.96 9.44-9.6 0-19.2-3.776-26.88-11.328l-170.88-167.872a37.024 37.024 0 0 1 0-52.8 38.336 38.336 0 0 1 53.76 0l145.92 143.36 287.36-232.768a38.08 38.08 0 0 1 53.408 5.44 37.6 37.6 0 0 1-5.376 53.088l-312.352 253.44z"
                              fill="#FCB90A"></path>
                      </svg>
                      <!-- 未签到红点提示 -->
                      <span v-if="!isSignIn" class="absolute top-1.5 right-2.5 size-2 bg-red-500 rounded-full animate-ping"></span>
                      <span v-if="!isSignIn" class="absolute top-1.5 right-2.5 size-2 bg-red-500 rounded-full"></span>
                    </div>
                    <div class="text-[11px] text-gray-500 mt-1 font-medium group-hover/item:text-blue-500 transition-colors">每日签到</div>
                  </div>
                  <!-- 收藏 -->
                  <div class="collected flex flex-col items-center cursor-pointer group/item" @click="toUserProfile(userProfile?.id)">
                    <div class="size-12 flex items-center justify-center">
                      <svg class="w-6.5 h-6.5 group-hover/item:scale-110 transition-transform duration-300" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5336">
                        <path d="M219.428571 73.142857h585.142858a109.714286 109.714286 0 0 1 109.714285 109.714286v697.088a73.142857 73.142857 0 0 1-99.766857 68.132571c-181.577143-71.021714-282.441143-106.496-302.518857-106.496-20.114286 0-120.941714 35.474286-302.518857 106.496A73.142857 73.142857 0 0 1 109.714286 879.908571V182.857143a109.714286 109.714286 0 0 1 109.714285-109.714286z" fill="#FFB156" p-id="5337"></path>
                        <path d="M914.285714 182.857143v438.857143H512V73.142857h292.571429a109.714286 109.714286 0 0 1 109.714285 109.714286zM109.714286 292.571429h402.285714v402.285714H109.714286z" fill="#FEC965" p-id="5338"></path>
                        <path d="M512 292.571429h402.285714v402.285714H512z" fill="#FFB156" p-id="5339"></path>
                        <path d="M494.994286 630.674286l-79.177143 41.618285a18.285714 18.285714 0 0 1-26.514286-19.273142l15.104-88.137143a36.571429 36.571429 0 0 0-10.532571-32.402286l-64-62.427429A18.285714 18.285714 0 0 1 339.931429 438.857143l88.502857-12.836572a36.571429 36.571429 0 0 0 27.538285-20.004571l39.606858-80.237714a18.285714 18.285714 0 0 1 32.768 0l39.606857 80.237714a36.571429 36.571429 0 0 0 27.538285 20.004571l88.502858 12.836572a18.285714 18.285714 0 0 1 10.130285 31.195428L630.125714 532.48a36.571429 36.571429 0 0 0-10.532571 32.365714l15.104 88.137143a18.285714 18.285714 0 0 1-26.514286 19.309714l-79.177143-41.618285a36.571429 36.571429 0 0 0-34.011428 0z" fill="#FFFFFF" p-id="5340"></path>
                      </svg>
                    </div>
                    <div class="text-[11px] text-gray-500 mt-1 font-medium group-hover/item:text-blue-500 transition-colors">我的收藏</div>
                  </div>
                  <!-- 权益 -->
                  <div class="benifit flex flex-col items-center cursor-pointer group/item" @click="toMember = true">
                    <div class="size-12 flex items-center justify-center">
                      <svg class="w-6.5 h-6.5 group-hover/item:scale-110 transition-transform duration-300" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10128">
                        <path d="M148.72381 719.238095l-97.52381 156.038095c-4.87619 9.752381 0 21.942857 12.190476 21.942858l131.657143 7.314285c4.87619 0 7.314286 2.438095 9.752381 4.876191l80.457143 107.27619c7.314286 7.314286 19.504762 7.314286 24.380952 0l92.647619-153.6c4.87619-7.314286 2.438095-14.628571-4.87619-19.504762l-229.180953-129.219047c-7.314286-2.438095-14.628571 0-19.504761 4.87619zM875.27619 716.8l97.52381 156.038095c4.87619 9.752381 0 21.942857-12.190476 21.942857L828.952381 902.095238c-4.87619 0-7.314286 2.438095-9.752381 4.876191l-80.457143 107.27619c-7.314286 7.314286-19.504762 7.314286-24.380952 0l-92.647619-153.6c-4.87619-7.314286-2.438095-14.628571 4.87619-19.504762l229.180953-129.219047c7.314286-4.87619 14.628571-2.438095 19.504761 4.87619zM855.771429 185.295238L546.133333 9.752381c-21.942857-12.190476-51.2-12.190476-73.142857 0L165.790476 182.857143C143.847619 195.047619 129.219048 219.428571 129.219048 243.809524v348.647619c0 24.380952 14.628571 48.761905 36.571428 60.952381L475.428571 828.952381c21.942857 12.190476 51.2 12.190476 73.142858 0l307.2-170.666667c21.942857-12.190476 36.571429-36.571429 36.571428-60.952381V248.685714c0-26.819048-14.628571-51.2-36.571428-63.390476z m-146.285715 148.72381l-180.419047 260.87619c-4.87619 7.314286-14.628571 12.190476-24.380953 12.190476s-19.504762-4.87619-24.380952-12.190476l-173.104762-260.87619c-9.752381-12.190476-4.87619-31.695238 7.314286-39.009524 12.190476-7.314286 31.695238-4.87619 39.009524 7.314286l148.723809 224.304761 156.038095-226.742857c9.752381-12.190476 26.819048-17.066667 39.009524-7.314285 19.504762 12.190476 21.942857 29.257143 12.190476 41.447619z" fill="#f4ea2a" p-id="10129"></path>
                      </svg>
                    </div>
                    <div class="text-[11px] text-gray-500 mt-1 font-medium group-hover/item:text-blue-500 transition-colors">会员权益</div>
                  </div>
                  <!-- 我的订单 -->
                  <div class="orders flex flex-col items-center cursor-pointer group/item" @click="router.push({ name: 'orders' })">
                    <div class="size-12 flex items-center justify-center">
                      <svg class="w-6.5 h-6.5 group-hover/item:scale-110 transition-transform duration-300" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5352">
                        <path d="M768 128a85.333333 85.333333 0 0 1 85.12 78.933333L853.333333 213.333333v597.333334a85.333333 85.333333 0 0 1-78.933333 85.12L768 896H256a85.333333 85.333333 0 0 1-85.12-78.933333L170.666667 810.666667V213.333333a85.333333 85.333333 0 0 1 85.333333-85.333333 42.666667 42.666667 0 0 1 4.992 85.034667L256 213.333333v597.333334h512V213.333333l-4.992-0.298666A42.666667 42.666667 0 0 1 768 128z" fill="#049DEE" p-id="5353"></path>
                        <path d="M597.333333 85.333333h-170.666666a85.333333 85.333333 0 0 0-85.333334 85.333334v42.666666a85.333333 85.333333 0 0 0 85.333334 85.333334h170.666666a85.333333 85.333333 0 0 0 85.333334-85.333334V170.666667a85.333333 85.333333 0 0 0-85.333334-85.333334z m-170.666666 85.333334h170.666666v42.666666h-170.666666V170.666667z" fill="#049DEE" p-id="5354"></path>
                        <path d="M640 554.666667a42.666667 42.666667 0 0 1 4.992 85.034666L640 640H384a42.666667 42.666667 0 0 1-4.992-85.034667L384 554.666667h256z m0-170.666667a42.666667 42.666667 0 0 1 4.992 85.034667L640 469.333333H384a42.666667 42.666667 0 0 1-4.992-85.034666L384 384h256z" fill="#03CD8E" p-id="5355"></path>
                      </svg>
                    </div>
                    <div class="text-[11px] text-gray-500 mt-1 font-medium group-hover/item:text-blue-500 transition-colors">我的订单</div>
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
        <div class="right flex items-center justify-end gap-2 md:gap-6 cursor-pointer shrink-0">
          <div class="hover:text-blue-400 flex flex-col group items-center justify-center shrink-0"
            @click="toMember = true">
            <img class="w-5 h-5 md:w-6 md:h-6 rounded-full group-hover:animate-bounce "
              src="https://static.nowcoder.com/fe/file/oss/1675240070182OPBSB.png" alt="" />
            <span class="hidden sm:inline text-[10px] md:text-xs">会员</span>
          </div>
          <div class="flex flex-col  items-center justify-center hover:text-blue-400 cursor-pointer shrink-0 "
            @click="handleMessageClick">
            <svg focusable="false" viewBox="0 0 80 80" fill="currentColor" class="w-5 h-5 md:w-6 md:h-6 "
              aria-hidden="true" data-v-79ba69ea="">
              <g fill="none" fill-rule="evenodd" stroke="currentColor">
                <rect width="67.2" height="55.2" x="6.4" y="12.4" stroke-width="4.8" rx="12"></rect>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="5.2"
                  d="m10 18 25.2696 18.531c2.8157 2.0649 6.6453 2.065 9.4611.0006L70.0067 18h0"></path>
              </g>
            </svg>
            <span class="hidden sm:inline text-[10px] md:text-xs">消息</span>
          </div>
          <div class="flex flex-col items-center justify-center hover:text-blue-400 cursor-pointer shrink-0"
            @click="handleSettingsClick">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-5 h-5 md:w-6 md:h-6">
              <circle cx="12" cy="12" r="3"></circle>
              <path
                d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z">
              </path>
            </svg>
            <span class="hidden sm:inline text-[10px] md:text-xs">设置</span>
          </div>
          <div
            class="hover:animate-pulse cursor-pointer rounded-lg px-2 md:px-3 py-1 text-xs md:text-sm bg-linear-to-r from-blue-200 via-blue-300 to-blue-300 shrink-0"
            @click="handlePublishClick">
            发布
          </div>
        </div>
      </div>
    </div>
    <div class="w-full flex-1 flex flex-col min-h-0">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </div>

    <!-- 底部 Footer 页脚组件 -->
    <footer class="w-full bg-[#161719] text-gray-500 py-8 px-4 md:px-12 border-t border-stone-900 text-xs shrink-0 select-none mt-auto">
      <div class="w-full max-w-[1200px] m-auto flex flex-col gap-6">
        
        <!-- 第一行：链接、社交与挂件 -->
        <div class="flex flex-col lg:flex-row items-center justify-between gap-6 border-b border-stone-800/60 pb-6">
          <div class="flex flex-wrap items-center justify-center lg:justify-start gap-x-6 gap-y-3 text-stone-400">
            <span class="flex items-center transition-colors duration-200 hover:text-blue-400 cursor-pointer">
              <!-- 手机图标 -->
              <svg class="w-3.5 h-3.5 mr-1.5 fill-current text-stone-500 animate-pulse" viewBox="0 0 24 24">
                <path d="M17 1H7c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-2-2-2zm-5 21c-.83 0-1.5-.67-1.5-1.5S11.17 19 12 19s1.5.67 1.5 1.5S12.83 22 12 22zm5-4H7V4h10v14z"/>
              </svg>
              移动版
            </span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">关于我们</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">加入我们</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">意见反馈</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">企业服务</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">校企合作</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">联系我们</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">免责声明</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">友情链接</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">资源导航</span>
            <span class="transition-colors duration-200 hover:text-blue-400 cursor-pointer">付费咨询</span>
          </div>

          <div class="flex items-center gap-6">
            <!-- 社交图标圈 -->
            <div class="flex items-center gap-3">
              <!-- 微信 -->
              <span class="group flex items-center justify-center w-8 h-8 rounded-full border border-stone-800 hover:border-emerald-500 hover:bg-emerald-500/10 cursor-pointer transition-all duration-300">
                <svg class="w-4 h-4 text-stone-400 group-hover:text-emerald-400 transition-colors duration-300" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M8.2 14.5c.3 0 .5-.2.5-.5s-.2-.5-.5-.5-.5.2-.5.5.2.5.5.5zm3.8 0c.3 0 .5-.2.5-.5s-.2-.5-.5-.5-.5.2-.5.5.2.5.5.5zm-.3-8.8C7.2 5.7 3 8.7 3 12.6c0 2.1 1.3 3.9 3.3 4.9l.4.5-.3 1.1c-.1.2 0 .4.2.4.2 0 .8-.3 1.4-.7.4.1.7.1 1 .1 4.3 0 7.8-2.6 7.8-6.6.1-3.9-3.9-6.7-7.8-6.7zm8 10.3c1.4-.7 2.3-2 2.3-3.5 0-2.9-3-5-6.1-5-1.1 0-2.1.3-3 .7.7.8 1.3 1.7 1.5 2.8 3.3.4 6.3 2.5 6.3 5.7 0 1.5-1 2.8-2.5 3.5l-.3.4.2.9c0 .1 0 .2-.1.2h-.1c-.2 0-.7-.2-1.1-.5-.3.1-.6.1-.9.1 0 .6-.2 1.3-.6 1.9 2.5-.1 4.9-1.7 5.1-2.4zm-5.3-1.8c.2 0 .4-.2.4-.4s-.2-.4-.4-.4-.4.2-.4.4.2.4.4.4zm2.5 0c.2 0 .4-.2.4-.4s-.2-.4-.4-.4-.4.2-.4.4.2.4.4.4z"/>
                </svg>
              </span>
              <!-- 微博 -->
              <span class="group flex items-center justify-center w-8 h-8 rounded-full border border-stone-800 hover:border-red-500 hover:bg-red-500/10 cursor-pointer transition-all duration-300">
                <svg class="w-4 h-4 text-stone-400 group-hover:text-red-400 transition-colors duration-300" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.93 14.7c-2.45.19-4.88-.63-5.74-1.83-.87-1.2-.09-2.94 1.72-3.87 1.81-.93 4.14-.99 5.21.1 1.07 1.09.43 2.94-1.19 4.6zm1.19-6.3c-.3.07-.6-.08-.7-.37-.1-.28.05-.6.35-.67.3-.07.6.08.7.37.1.29-.05.6-.35.67zm.83-2.18c-.76.32-1.63-.03-1.92-.78-.29-.75.09-1.6.86-1.92.76-.32 1.63.03 1.92.78.29.76-.09 1.6-.86 1.92z"/>
                </svg>
              </span>
              <!-- QQ -->
              <span class="group flex items-center justify-center w-8 h-8 rounded-full border border-stone-800 hover:border-blue-500 hover:bg-blue-500/10 cursor-pointer transition-all duration-300">
                <svg class="w-4 h-4 text-stone-400 group-hover:text-blue-400 transition-colors duration-300" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.18 13.92c-.37.38-.85.58-1.33.58-.29 0-.58-.08-.84-.24-.26-.16-.48-.39-.63-.67a3.02 3.02 0 0 1-.36-.93 2.9 2.9 0 0 1 .05-1c.08-.29.22-.56.42-.78.37-.38.85-.58 1.33-.58.29 0 .58.08.84.24.26.16.48.39.63.67.15.28.27.59.36.93.09.34.07.68-.05 1-.08.29-.22.56-.42.78zM9.82 13.92c-.37-.38-.51-.9-.42-1.42.08-.34.2-.65.36-.93.15-.28.37-.51.63-.67.26-.16.55-.24.84-.24.48 0 .96.2 1.33.58.2.22.34.49.42.78.09.34.07.68-.05 1-.15.28-.37.51-.63.67-.26.16-.55.24-.84.24-.48 0-.96-.2-1.33-.58z"/>
                </svg>
              </span>
              <!-- Github -->
              <span class="group flex items-center justify-center w-8 h-8 rounded-full border border-stone-800 hover:border-stone-300 hover:bg-stone-300/10 cursor-pointer transition-all duration-300">
                <svg class="w-4 h-4 text-stone-400 group-hover:text-stone-200 transition-colors duration-300" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2A10 10 0 0 0 2 12c0 4.42 2.87 8.17 6.84 9.5.5.08.66-.23.66-.5v-1.69c-2.77.6-3.36-1.34-3.36-1.34-.46-1.16-1.11-1.47-1.11-1.47-.9-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.9 1.52 2.34 1.07 2.91.83.1-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.92 0-1.11.38-2 1.03-2.71-.1-.25-.45-1.29.1-2.64 0 0 .84-.27 2.75 1.02.79-.22 1.65-.33 2.5-.33.85 0 1.71.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.35.2 2.39.1 2.64.65.71 1.03 1.6 1.03 2.71 0 3.82-2.34 4.66-4.57 4.91.36.31.69.92.69 1.85V21c0 .27.16.59.67.5C19.14 20.16 22 16.42 22 12A10 10 0 0 0 12 2z"/>
                </svg>
              </span>
            </div>

            <!-- HR直投 (高级CSS动效徽章) -->
            <div class="hr-direct-badge group flex items-center justify-center bg-emerald-500 hover:bg-emerald-400 text-white font-semibold text-[11px] px-3.5 py-1.5 rounded-full cursor-pointer shadow-md select-none transition-all duration-300 transform hover:scale-105">
                <span class="flex items-center gap-1.5">
                    <span class="w-1.5 h-1.5 rounded-full bg-white animate-ping"></span>
                    HR 直投
                </span>
            </div>
          </div>
        </div>

        <!-- 第二行：公司地址及联系方式 -->
        <div class="flex flex-col md:flex-row flex-wrap items-center justify-center gap-y-2 gap-x-6 text-stone-500 text-center">
          <span>公司地址：成都市高新区天府二街抗重力科学中心A座12层-成都抗重力社交网络科技有限公司</span>
          <span class="hidden md:inline text-stone-700">|</span>
          <span>联系方式：028-86868888</span>
          <span class="hidden md:inline text-stone-700">|</span>
          <span>投诉举报电话：028-88889999（成都高新区人力社保局）</span>
        </div>

        <!-- 第三行：版权和各种资质 -->
        <div class="flex flex-col lg:flex-row items-center justify-between gap-4 text-stone-600 text-center lg:text-left pt-2 border-t border-stone-800/40">
          <div class="flex flex-wrap items-center justify-center lg:justify-start gap-x-2 gap-y-1">
            <span>STP 社交网络 ©2026 All rights reserved</span>
            <a href="mailto:contact@stp-social.com" class="hover:text-stone-400 transition-colors">contact@stp-social.com</a>
            <span class="text-stone-700 mx-1">|</span>
            <a href="https://beian.miit.gov.cn/" target="_blank" class="hover:text-stone-400 transition-colors">蜀ICP备20260716号-1</a>
          </div>
          <div class="flex flex-wrap items-center justify-center lg:justify-end gap-x-4 gap-y-2">
            <span class="hover:text-stone-400 cursor-pointer transition-colors">增值电信业务经营许可证</span>
            <span class="hover:text-stone-400 cursor-pointer transition-colors">营业执照</span>
            <span class="hover:text-stone-400 cursor-pointer transition-colors">人力资源服务许可证</span>
            <a href="http://www.beian.gov.cn/" target="_blank" class="flex items-center hover:text-stone-400 transition-colors">
              <span class="w-3 h-3 bg-amber-600/40 rounded-full inline-block mr-1"></span>
              蜀公网安备 51010902000666号
            </a>
          </div>
        </div>

      </div>
    </footer>

    <!-- 全局 401 登录弹窗 -->
    <LoginDialog />

    <!-- 每日打卡弹窗 -->
    <Daily_signIn :visible="showDailySignIn" @close="showDailySignIn = false; loadSignInStatus()" />
  </div>
</template>
<script setup lang="ts">

import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import member from '@/views/member/pages/member.vue'
import { UserAPI, type UserProfileData } from '@/services/user'
import { useUserInfoStore } from '@/stores/userInfo'
import { useAuthStore } from '@/views/auth/store'
import { Auther } from '@/views/auth/composables/Auth'
import router from '@/router'
import { newPageWithId } from '@/utils/page'
import { PostAPI } from '@/services/post'
import { log } from '@/utils/log'
import LoginDialog from '@/views/auth/components/LoginDialog.vue'
import Daily_signIn from '@/presentation/components/daily_signIn.vue'
import { ToolBoxAPI } from '@/services/toolbox'

const showDailySignIn = ref(false)
const isSignIn = ref(false)

async function loadSignInStatus() {
  if (!authStore.token) return
  try {
    const res = await ToolBoxAPI.getSignInStatus()
    if (res.code === 1 && res.data) {
      isSignIn.value = res.data.todayChecked
    }
  } catch (e) {
    console.error('加载签到状态失败:', e)
  }
}
const keyword = ref("");
const curTab = ref("home");
const toMember = ref(false)
const route = useRoute();

watch(() => route.query.keyword, (newVal) => {
  keyword.value = (newVal as string) || "";
}, { immediate: true });

function handleSearch() {
  router.push({ name: 'homeMain', query: { keyword: keyword.value || undefined } });
}

const user = useUserInfoStore();
const userProfile = computed(() => user.user)
const authStore = useAuthStore()

function handleAvatarClick() {
  if (!authStore.token) {
    authStore.showLoginDialog()
  }
}

function handlePublishClick() {
  if (!authStore.token) {
    log.warning('请先登录后操作')
    authStore.showLoginDialog()
    return
  }
  newPageWithId(undefined, 'post')
}

function handleMessageClick() {
  if (!authStore.token) {
    log.warning('请先登录后查看消息')
    authStore.showLoginDialog()
    return
  }
  router.push({ name: 'message' })
}

function handleSettingsClick() {
  if (!authStore.token) {
    log.warning('请先登录后访问该页面')
    authStore.showLoginDialog()
    return
  }
  router.push({ name: 'userSettings' })
}


onMounted(async () => {
  await loadUserInfo();
  await loadSignInStatus();
})

/**
* 加载用户信息
*/
async function loadUserInfo() {
  const authStore = useAuthStore()
  if (authStore.token && !user.user) {
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
  authStore.clearAuth();
  user.clearUser();
  isSignIn.value = false;
  router.push('/')
}

function toUserProfile(id: number | string | undefined) {
  if (!id) return;
  router.push({ name: 'userProfile', params: { id: id } })
}


async function toggleTab(name: string) {
  curTab.value = name;
  router.push({ name: name });
}
</script>
