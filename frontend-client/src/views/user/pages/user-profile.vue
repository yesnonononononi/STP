<template>
    <div class="relative z-12 w-full h-[calc(100vh-64px)] py-4 overflow-hidden box-border bg-cover bg-center bg-no-repeat"
        :class="userInfo?.bgImage ? '' : 'bg-linear-to-br from-indigo-50 via-blue-100 to-purple-50'"
        :style="userInfo?.bgImage ? `background-image: url('${userInfo.bgImage}');  ` : ''">

        <!-- 账号设置和更换背景按钮 -->
        <div class="w-2/3   h-12 m-auto flex text-white font-semibold items-center justify-end gap-4 px-4 select-none"
            :class="isme ? 'opacity-100' : 'opacity-0'">
            <div class="cursor-pointer hover:underline flex items-center gap-1 overflow-hidden " @click="handleResetBg">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z">
                    </path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
                <span>重置背景</span>
            </div>
            <div class="cursor-pointer ">
                <el-upload class=" flex items-center justify-center" :show-file-list="false" :auto-upload="false"
                    :on-change="handleBgImageChange" accept=".png,.jpg,.jpeg">
                    <template v-slot:trigger>
                        <div class="cursor-pointer  flex items-center gap-1">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z">
                                </path>
                            </svg>
                            <span>更换背景</span>
                        </div>
                    </template>
                </el-upload>
            </div>
        </div>
        <div class="w-2/3 h-[calc(100vh-160px)] bg-white/70 rounded-2xl   m-auto  flex flex-col">
            <!-- 个人主页卡片 -->
            <div
                class="rounded-2xl shadow-[0_8px_32px_0_rgba(31,38,135,0.06)] bg-white/45 backdrop-blur-xl border border-white/25 relative overflow-visible">
                <div class="top">
                    <!-- 凸出头像组件 -->
                    <div
                        class="avatar-wrapper absolute left-8 top-0 w-28 h-28 cursor-pointer -translate-y-1/2 rounded-full bg-white/45 backdrop-blur-xl border border-white/25 p-1.5 flex items-center justify-center z-10 shadow-[0_8px_32px_0_rgba(31,38,135,0.06)]">
                        <img v-avatar-skeleton class="w-full h-full rounded-full object-cover"
                            :src="userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                            alt="Avatar">
                    </div>

                    <!-- 卡片信息区 -->
                    <div class="avatar-introduce   flex  justify-between px-8 pt-4">
                        <div class="left ml-32 h-12 flex items-center gap-1 select-none">
                            <span class="font-bold text-gray-800 text-xl">{{ userInfo?.nick || '' }}</span>
                            <div v-if="userInfo && userInfo.memberLevel"
                                class="bg-blue-200 h-5 w-20 rounded-2xl flex items-center justify-center ">
                                <img class="size-4" v-if="userInfo?.vipConfigIcon" :src="userInfo?.vipConfigIcon"
                                    alt="">
                                <span class="text-xs shrink-0 text-blue-500 p-1">{{
                                    userInfo.memberLevel }}</span>
                            </div>
                            <div v-if="userInfo && userInfo.vipConfigIcon"
                                class="bg-linear-to-br from-yellow-200 via-amber-300 to-yellow-400 text-stone-500 rounded-full px-2 h-5 font-semibold  text-xs flex items-center justify-center">
                                <span>{{ userInfo.vipType }}</span>
                            </div>
                        </div>
                        <div class="right w-1/3 h-full flex items-center ">
                            <div
                                class="liked flex-1  border-r border-gray-300 flex flex-col items-center justify-center gap-1">
                                <div>获赞</div>
                                <div>{{ formatNum(Number(userInfo?.liked || 0)) }}</div>
                            </div>
                            <div @click="isme && openFansDialog()"
                                class="fans flex-1 border-r border-gray-300 flex flex-col items-center justify-center gap-1 transition-all duration-300"
                                :class="isme ? 'cursor-pointer hover:bg-slate-100/50 hover:rounded-xl' : ''">
                                <div>粉丝</div>
                                <div>{{ formatNum(Number(userInfo?.fans || 0)) }}</div>
                            </div>
                            <div class="topic flex-1 flex flex-col items-center justify-center gap-1">
                                <div>话题</div>
                                <div>{{ formatNum(Number(userInfo?.topic || 0)) }}</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tag flex flex-wrap items-center gap-2.5 mx-4 px-4 pb-2 select-none">
                    <div class="w-auto text-[11px] font-semibold h-7 px-3.5 rounded-full flex items-center justify-center bg-gradient-to-br backdrop-blur-xs border hover:scale-[1.03] transition-all duration-300 cursor-default shadow-xs"
                        :class="getTagClass(index)"
                        v-for="(item, index) in userTag" :key="index">
                        <span>{{ item.name }}</span>
                    </div>
                </div>
                <div class="introduce w-full flex justify-between items-center">
                    <div class="left text-gray-500 mx-4 p-4 w-1/2 select-none">
                        {{ userInfo?.introduction || (isme ? '添加简介,让大家认识你' : '该用户暂无简介') }}
                    </div>
                    <div class="right w-1/2 text-right flex justify-center items-center gap-6 mb-6" v-if="!isme">
                        <button @click="handleFollow"
                            class="rounded-md shadow-md w-24 h-10 cursor-pointer hover:scale-[1.05] transition-all duration-300 font-semibold"
                            :class="userInfo?.followed
                                ? 'bg-gray-100 text-gray-500 border border-gray-300 hover:bg-gray-200'
                                : 'bg-linear-to-bl from-blue-400 to-blue-500 text-white hover:shadow-lg'">
                            {{ userInfo?.followed ? '已关注' : '关注' }}
                        </button>
                        <button @click="handleMessage"
                            class="bg-linear-to-bl rounded-md shadow-md from-blue-100 to-blue-200 w-24 h-10 cursor-pointer hover:scale-[1.05]">私信</button>
                        <button @click="showReportDialog = true"
                            class="text-sm text-slate-400 hover:text-rose-500 transition-colors cursor-pointer font-medium px-2 py-1">
                            举报
                        </button>
                    </div>
                    <div v-else class="flex items-center gap-4 px-4 mb-6">
                        <span>个人信息完善度</span>
                        <span class="text-blue-300">{{ profileCompleteness }}%</span>
                        <span class="cursor-pointer hover:text-blue-600 text-blue-500"
                            @click="edit_profile = true">编辑资料></span>
                    </div>
                </div>
            </div>

            <!-- 足迹 组件 -->
            <div class="flex-1 min-h-0 pb-3 bg-gay-100 rounded-md flex">
                <div class="left p-2 pb-0 h-full transition-all duration-300" :class="isme ? 'w-[75%]' : 'w-full'">
                    <div
                        class="w-full h-[10%] rounded-md bg-white p-3 px-4 flex items-center justify-between text-xl text-gray-500 font-semibold select-none">
                        <div class="flex gap-12 items-center">
                            <span
                                class="hover:text-blue-400 transition-all duration-300 cursor-pointer flex items-center justify-center"
                                :class="curTab === 0 ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-400 to-blue-600' : ''"
                                @click="handleTabClick(0)">帖子</span>
                            <span
                                class="hover:text-blue-400 transition-all duration-300 cursor-pointer flex items-center justify-center"
                                :class="curTab === PostStatus.LIKED ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-400 to-blue-600' : ''"
                                @click="handleTabClick(PostStatus.LIKED)">赞过</span>
                            <span
                                class="hover:text-blue-400 transition-all duration-300 cursor-pointer flex items-center justify-center"
                                :class="curTab === PostStatus.COLLECTED ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-400 to-blue-600' : ''"
                                @click="handleTabClick(PostStatus.COLLECTED)">收藏</span>
                        </div>

                        <!-- 仅在查看自己且位于“帖子”Tab时显示审核状态筛选器 (简约风格动画下拉框) -->
                        <div v-if="isme && curTab === 0" class="relative inline-block text-xs font-normal select-none" ref="dropdownRef">
                            <div class="flex items-center gap-2">
                                <span class="text-slate-400 font-medium">状态:</span>
                                <button
                                    @click.stop="isDropdownOpen = !isDropdownOpen"
                                    class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-50 hover:bg-slate-100/90 border border-slate-200/80 text-slate-700 font-medium shadow-2xs transition-all duration-200 active:scale-95 cursor-pointer"
                                >
                                    <span class="size-2 rounded-full shrink-0" :class="activeOption.dotClass"></span>
                                    <span>{{ activeOption.label }}</span>
                                    <svg
                                        class="size-3.5 text-slate-400 transition-transform duration-300"
                                        :class="isDropdownOpen ? 'rotate-180 text-blue-500' : ''"
                                        fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"
                                    >
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
                                    </svg>
                                </button>

                                <span class="text-[10px] text-slate-400 font-medium bg-slate-50 px-1.5 py-0.5 rounded border border-slate-100 shrink-0">
                                    仅自己可见
                                </span>
                            </div>

                            <!-- 过渡动画下拉菜单列表 -->
                            <Transition
                                enter-active-class="transition duration-200 ease-out"
                                enter-from-class="transform opacity-0 scale-95 -translate-y-2"
                                enter-to-class="transform opacity-100 scale-100 translate-y-0"
                                leave-active-class="transition duration-150 ease-in"
                                leave-from-class="transform opacity-100 scale-100 translate-y-0"
                                leave-to-class="transform opacity-0 scale-95 -translate-y-2"
                            >
                                <div
                                    v-if="isDropdownOpen"
                                    class="absolute right-12 top-full mt-2 w-48 rounded-2xl bg-white/95 backdrop-blur-xl border border-slate-100 shadow-2xl p-1.5 z-50 overflow-hidden"
                                >
                                    <div
                                        v-for="opt in auditOptions"
                                        :key="opt.value"
                                        @click="selectAuditOption(opt.value)"
                                        class="flex items-center justify-between px-3 py-2 rounded-xl cursor-pointer text-xs font-medium transition-all duration-200"
                                        :class="auditFilter === opt.value ? 'bg-blue-50/80 text-blue-600 font-semibold' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'"
                                    >
                                        <div class="flex items-center gap-2">
                                            <span class="size-2 rounded-full shrink-0" :class="opt.dotClass"></span>
                                            <span>{{ opt.label }}</span>
                                        </div>
                                        <svg v-if="auditFilter === opt.value" class="size-4 text-blue-500 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                                            <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                                        </svg>
                                    </div>
                                </div>
                            </Transition>
                        </div>
                    </div>
                    <div class="w-full mt-2 h-[90%] rounded-xl">
                        <UserProfileTabPublish ref="publishTabRef" :status="curTab" :creator-id="uid" :self="isme"
                            :audit-filter="auditFilter"
                            @delete="handleDelete" />
                    </div>
                </div>
                <div v-if="isme" class="right w-[25%] h-full mt-2 flex flex-col gap-3">
                    <!-- VIP 会员卡片 -->
                    <div v-if="isme" class="w-full bg-linear-to-r from-white via-blue-50/50 to-blue-100/60 p-4 border border-blue-100/40 rounded-2xl shadow-xs flex flex-col justify-center select-none">
                        <div class="flex w-full items-center justify-between">
                            <div class="left flex flex-col items-start text-left">
                                <div class="flex items-center text-slate-800 font-bold text-sm">
                                    <span>{{ userInfo?.vipType || 'STP会员' }}</span>
                                    <span class="px-1.5 py-0.5 rounded-md ml-2 text-[9px] text-center font-bold text-white shadow-xs"
                                        :class="userInfo?.vipExpireDate ? 'bg-yellow-500' : 'bg-slate-450'">
                                        {{ userInfo?.vipExpireDate ? '已开通' : '未开通' }}
                                    </span>
                                </div>
                                <span class="text-slate-400 text-[10px] mt-1 font-medium">
                                    {{ userInfo?.vipExpireDate ? '到期时间: ' + userInfo.vipExpireDate.split(' ')[0] : '您还未开通会员服务' }}
                                </span>
                            </div>
                            <button
                                @click="showMemberBuy = true"
                                class="cursor-pointer shadow-sm hover:shadow-md px-3 py-1.5 rounded-lg bg-gradient-to-r from-yellow-300 via-yellow-100 to-yellow-400 text-xs font-bold text-stone-600 hover:scale-[1.02] active:scale-95 transition-all shrink-0">
                                立即{{ userInfo?.vipExpireDate ? "续期" : "开通" }}
                            </button>
                        </div>
                    </div>

                    <!-- 工具箱 -->
                    <div class="bg-white p-4 border border-slate-100 rounded-2xl shadow-xs flex flex-col select-none" :class="isme ? 'flex-1' : 'h-full'">
                        <span class="font-semibold p-2">工具箱</span>
                        <div class="flex flex-wrap items-center gap-2 p-1">
                            <div class="daily-signIn flex flex-col items-center cursor-pointer group"
                                @click="showDailySignIn = true" v-if="isme">
                            <div class="relative size-10 rounded-full  ">
                                <svg t="1784189983423" class="icon relative size-10" viewBox="0 0 1024 1024"
                                    version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1917">
                                    <path
                                        d="M961.184 316.768V205.44c0-20.736-17.28-37.728-38.4-37.728h-205.44V102.4a38.4 38.4 0 0 0-76.768 0v65.344H352.64V102.4a38.4 38.4 0 0 0-76.8 0v65.344H70.4c-21.12 0-38.368 16.96-38.368 37.76v111.264h929.184zM32 392.224V922.24C32 943.04 49.28 960 70.4 960h852.384c21.12 0 38.4-16.96 38.4-37.728V392.224H32zM448.608 814.72c-7.68 5.664-15.36 9.44-24.96 9.44-9.6 0-19.2-3.776-26.88-11.328l-170.88-167.872a37.024 37.024 0 0 1 0-52.8 38.336 38.336 0 0 1 53.76 0l145.92 143.36 287.36-232.768a38.08 38.08 0 0 1 53.408 5.44 37.6 37.6 0 0 1-5.376 53.088l-312.352 253.44z"
                                        fill="#FCB90A" p-id="1918"></path>
                                </svg>
                                <svg t="1784190051010" v-if="!state.isSignIn"
                                    class="icon absolute top-0 right-0 size-4 group-hover:animate-pulse duration-300"
                                    viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
                                    p-id="5205">
                                    <path
                                        d="M512 0C229.003636 0 0 229.003636 0 512s229.003636 512 512 512 512-229.003636 512-512S794.996364 0 512 0z m2.792727 791.272727c-33.512727 0-61.44-26.996364-61.44-59.578182 0-32.581818 26.996364-59.578182 61.44-59.578181 33.512727 0 61.44 26.996364 61.44 59.578181 0 33.512727-27.927273 59.578182-61.44 59.578182z m58.647273-352.814545c-10.24 82.850909-27.927273 151.738182-42.821818 174.08-2.792727 6.516364-9.309091 11.170909-16.756364 11.170909-8.378182 0-14.894545-5.585455-16.756363-13.032727-13.963636-23.272727-30.72-91.229091-40.96-172.218182-7.447273-56.785455-9.309091-108.916364-7.447273-143.36 0-2.792727-0.930909-6.516364-0.930909-9.309091 0-35.374545 29.789091-64.232727 66.094545-64.232727 23.272727 0 43.752727 12.101818 55.854546 29.789091 2.792727 1.861818 4.654545 5.585455 6.516363 12.101818 2.792727 7.447273 3.723636 14.894545 3.723637 22.341818v4.654545c3.723636 35.374545 0.930909 88.436364-6.516364 148.014546z"
                                        fill="#E63C33" p-id="5206"></path>
                                </svg>
                            </div>


                            <span class="text-gray-400 text-sm">每日打卡</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="absolute inset-0 z-11 bg-gray-400/50 flex flex-col items-center justify-center"
            v-show="pendingDeleteId">
            <div class="relative w-1/3 h-1/3 rounded-md flex flex-col items-center justify-center bg-white">
                <div class="absolute w-full top-2 right-8 flex justify-end items-center h-12 cursor-pointer"
                    @click="pendingDeleteId = null">X</div>
                <span class="text-2xl font-bold">你确定要删除此内容吗?</span>
                <div class="p-12 text-gray-500 flex   justify-between w-full gap-4">
                    <span
                        class="w-1/2 text-center font-semibold bg-blue-500 shadow-md rounded-md cursor-pointer text-white py-1 hover:bg-blue-700"
                        @click="handleConfirmDelete">确认</span>
                    <span
                        class="w-1/2 text-center font-semibold hover:bg-gray-200   shadow-md rounded-md cursor-pointer py-1"
                        @click="pendingDeleteId = null">取消</span>
                </div>
            </div>
        </div>
        </div>
        <Teleport to="body">
            <div class="profile-edit fixed inset-0 z-100 transition-all duration-500"
                :class="edit_profile ? 'pointer-events-auto' : 'pointer-events-none'">
                <!-- 原地淡入淡出遮罩层 -->
                <div class="absolute inset-0 bg-black/30 backdrop-blur-xs transition-opacity duration-500"
                    :class="edit_profile ? 'opacity-100' : 'opacity-0 pointer-events-none'"
                    @click="edit_profile = false"></div>

                <!-- 滑出式面板 -->
                <div class="absolute right-0 top-0 bg-white/95 backdrop-blur-md w-full max-w-[400px] h-full flex flex-col justify-between p-6 shadow-2xl border-l border-slate-100 transition-transform duration-500"
                    :class="edit_profile ? 'translate-x-0' : 'translate-x-full'">
                    <div class="flex-1 overflow-y-auto pr-1">
                        <!-- 头部标题 -->
                        <div class="flex items-center justify-between pb-4 mb-6 border-b border-slate-100">
                            <span class="text-lg font-bold text-slate-800 flex items-center gap-1.5">
                                <svg class="w-5 h-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"
                                    stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round"
                                        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                </svg>
                                个人基本资料
                            </span>
                            <div class="flex items-center gap-3">
                                <span v-if="!is_editing"
                                    class="px-3 py-1 text-xs font-semibold text-white bg-gradient-to-r from-blue-500 to-indigo-600 rounded-full shadow-xs hover:shadow-md cursor-pointer transition-all duration-300"
                                    @click="is_editing = true">
                                    编辑资料
                                </span>
                                <!-- X 关闭按钮 -->
                                <button @click="edit_profile = false"
                                    class="text-stone-400 hover:text-stone-600 hover:rotate-90 transition-all duration-300 cursor-pointer">
                                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"
                                        stroke-width="2">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>
                        </div>

                        <!-- 资料完善度进度条 -->
                        <div
                            class="w-full flex flex-col gap-1.5 mb-6 bg-slate-50/50 border border-slate-100 p-3 rounded-xl">
                            <div class="flex justify-between items-center text-xs text-slate-500 font-medium">
                                <span>信息完善度</span>
                                <span class="text-blue-500 font-bold">{{ profileCompleteness }}%</span>
                            </div>
                            <div class="w-full h-2 bg-slate-150 rounded-full overflow-hidden">
                                <div class="h-full bg-gradient-to-r from-blue-400 via-blue-500 to-indigo-500 transition-all duration-500"
                                    :style="`width: ${profileCompleteness}%`"></div>
                            </div>
                        </div>

                        <!-- 表单主体卡片列表 -->
                        <div class="flex flex-col gap-4">
                            <!-- 1. 头像卡片 -->
                            <div
                                class="flex items-center justify-between bg-slate-50/50 hover:bg-slate-50/80 border border-slate-100/80 rounded-xl p-4 transition-all duration-300">
                                <div class="flex flex-col gap-1">
                                    <span class="text-sm font-semibold text-slate-600">我的头像</span>
                                    <span v-if="is_editing"
                                        class="text-[11px] text-blue-500 hover:underline cursor-pointer select-none"
                                        @click="triggerAvatarSelect">更换头像</span>
                                    <span v-else class="text-[11px] text-slate-400">当前展示头像</span>
                                </div>
                                <div class="relative w-12 h-12 rounded-full overflow-hidden border-2 border-white shadow-sm shrink-0"
                                    :class="is_editing ? 'cursor-pointer hover:border-blue-500 transition-colors' : ''"
                                    @click="is_editing && triggerAvatarSelect()">
                                    <el-image
                                        :src="is_editing ? (previewAvatarUrl || editProfileForm.avatar) : userInfo?.avatar"
                                        class="w-full h-full rounded-full object-cover" alt="avatar">
                                        <template #placeholder>
                                            <div class="w-full h-full bg-slate-200 animate-pulse rounded-full"></div>
                                        </template>
                                        <template #error>
                                            <div
                                                class="w-full h-full rounded-full flex items-center justify-center bg-slate-100 text-slate-400">
                                                <el-icon class="text-lg">
                                                    <Plus />
                                                </el-icon>
                                            </div>
                                        </template>
                                    </el-image>
                                </div>
                                <input v-if="is_editing" type="file" ref="avatarInput" accept="image/*" class="hidden"
                                    @change="handleAvatarChange" />
                            </div>

                            <!-- 2. 昵称卡片 -->
                            <div
                                class="flex flex-col gap-2 bg-slate-50/50 hover:bg-slate-50/80 border border-slate-100/80 rounded-xl p-4 transition-all duration-300">
                                <span class="text-sm font-semibold text-slate-600">我的昵称</span>
                                <span v-if="!is_editing" class="text-sm text-slate-700 font-medium px-1">{{
                                    currentUser?.nick ||
                                    '未设置昵称' }}</span>
                                <el-input v-else v-model="editProfileForm.nick" :placeholder="userInfo?.nick || '请输入昵称'"
                                    class="custom-edit-input" />
                            </div>

                            <!-- 3. 性别卡片 -->
                            <div
                                class="flex flex-col gap-2 bg-slate-50/50 hover:bg-slate-50/80 border border-slate-100/80 rounded-xl p-4 transition-all duration-300">
                                <span class="text-sm font-semibold text-slate-600">我的性别</span>
                                <span v-if="!is_editing" class="text-sm text-slate-700 font-medium px-1">{{
                                    currentUser?.gender
                                        == 1 ? '男' : '女' }}</span>
                                <div v-else class="flex gap-3 pt-1">
                                    <button
                                        class="flex-1 py-1.5 rounded-lg text-xs font-semibold cursor-pointer border transition-all duration-300"
                                        :class="editProfileForm.gender == 1
                                            ? 'bg-blue-500 text-white border-blue-500 shadow-sm'
                                            : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'"
                                        @click="editProfileForm.gender = 1">男</button>
                                    <button
                                        class="flex-1 py-1.5 rounded-lg text-xs font-semibold cursor-pointer border transition-all duration-300"
                                        :class="editProfileForm.gender == 0
                                            ? 'bg-pink-500 text-white border-pink-500 shadow-sm'
                                            : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'"
                                        @click="editProfileForm.gender = 0">女</button>
                                </div>
                            </div>

                            <!-- 4. 年龄卡片 -->
                            <div
                                class="flex flex-col gap-2 bg-slate-50/50 hover:bg-slate-50/80 border border-slate-100/80 rounded-xl p-4 transition-all duration-300">
                                <span class="text-sm font-semibold text-slate-600">我的年龄</span>
                                <span v-if="!is_editing" class="text-sm text-slate-700 font-medium px-1">{{
                                    currentUser?.age ||
                                    '未填写' }}</span>
                                <el-input v-else v-model="editProfileForm.age" :placeholder="userInfo?.age || '请输入年龄'"
                                    class="custom-edit-input" />
                            </div>

                            <!-- 5. 简介卡片 -->
                            <div
                                class="flex flex-col gap-2 bg-slate-50/50 hover:bg-slate-50/80 border border-slate-100/80 rounded-xl p-4 transition-all duration-300">
                                <span class="text-sm font-semibold text-slate-600">我的简介</span>
                                <span v-if="!is_editing"
                                    class="text-sm text-slate-500 leading-relaxed px-1 whitespace-pre-line">{{
                                        currentUser?.introduction || '暂无简介' }}</span>
                                <el-input v-else type="textarea" :rows="3" max="50"
                                    v-model="editProfileForm.introduction"
                                    :placeholder="userInfo?.introduction || '请输入简介'" class="custom-edit-input"
                                    resize="none" />
                            </div>
                        </div>
                    </div>

                    <!-- 底层操作按钮 -->
                    <div class="flex gap-3 pt-4 border-t border-slate-100 mt-6 shrink-0" v-if="is_editing">
                        <button
                            class="flex-1 py-2.5 rounded-lg text-sm font-semibold text-white bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 active:scale-95 transition-all cursor-pointer shadow-md shadow-blue-500/10"
                            @click="saveProfile">保存修改</button>
                        <button
                            class="flex-1 py-2.5 rounded-lg text-sm font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 active:scale-95 transition-all cursor-pointer"
                            @click="is_editing = false">取消</button>
                    </div>
                </div>
            </div>
        </Teleport>
    </div>
    <!-- 页面中心私信输入弹窗 -->
    <Teleport to="body">
        <div v-if="showMessageInput && userInfo"
            class="fixed inset-0 z-1000 flex items-center justify-center bg-black/40 backdrop-blur-xs">
            <PrivateMessageInput :user="messageTargetUser" :visible="showMessageInput" v-model="messageContent"
                @close="showMessageInput = false" @success="handleMessageSuccess" />
        </div>
    </Teleport>
    <!-- 玻璃磨砂质感粉丝弹窗 -->
    <Teleport to="body">
        <Transition name="fade">
            <div v-if="showFansDialog"
                class="fixed inset-0 z-1000 flex items-center justify-center bg-black/40 backdrop-blur-xs"
                @click.self="showFansDialog = false">
                <div
                    class="relative w-120 max-h-[85vh] rounded-3xl bg-white/80 backdrop-blur-xl border border-white/40 shadow-2xl p-6 flex flex-col transition-all duration-300">

                    <!-- 弹窗头部 -->
                    <div class="flex justify-between items-center pb-4 border-b border-gray-200/50">
                        <span
                            class="text-xl font-bold bg-clip-text text-transparent bg-linear-to-r from-gray-800 to-gray-600 flex items-center gap-2">
                            <svg class="size-6 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                            </svg>
                            我的粉丝 ({{ userInfo?.fans || 0 }})
                        </span>
                        <button
                            class="size-8 rounded-full flex items-center justify-center hover:bg-gray-200/50 text-gray-400 hover:text-gray-600 transition-all duration-300 hover:rotate-90 cursor-pointer"
                            @click="showFansDialog = false">
                            <span class="text-lg font-semibold">✕</span>
                        </button>
                    </div>

                    <!-- 粉丝列表区 -->
                    <div class="flex-1 overflow-y-auto my-4 pr-1 flex flex-col gap-2.5 max-h-[50vh]"
                        v-loading="fansLoading">
                        <div v-if="fansList.length === 0 && !fansLoading"
                            class="flex flex-col items-center justify-center py-12 text-gray-400 gap-2 select-none">
                            <svg class="size-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                    d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <span>暂无粉丝，多发帖可以吸引更多关注哦</span>
                        </div>

                        <div v-for="fans in fansList" :key="fans.id"
                            class="flex items-center justify-between p-3 rounded-2xl bg-white/40 hover:bg-white/80 border border-transparent hover:border-white/50 hover:shadow-xs transition-colors duration-200 group">

                            <div class="flex items-center gap-3.5">
                                <img v-avatar-skeleton :src="fans.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                                    @click="handleGoToUser(fans.userId)"
                                    class="size-11 rounded-full border-2 border-white shadow-xs object-cover select-none cursor-pointer hover:scale-105 transition-transform duration-200"
                                    referrerpolicy="no-referrer" alt="" />
                                <span @click="handleGoToUser(fans.userId)"
                                    class="font-semibold text-gray-800 text-sm cursor-pointer hover:text-blue-600 hover:underline transition-colors select-all">
                                    {{ fans.nick }}
                                </span>
                            </div>

                            <span v-if="fans.status === 3"
                                class="h-8 px-4 rounded-xl font-semibold text-xs flex items-center justify-center bg-gray-100 text-gray-400 cursor-default select-none">
                                已互关
                            </span>
                            <button v-else @click="handleFansFollowToggle(fans)"
                                class="h-8 px-4 rounded-xl font-semibold text-xs transition-all duration-300 cursor-pointer flex items-center justify-center hover:scale-[1.05]"
                                :class="fans.status === 2
                                    ? 'bg-linear-to-r from-blue-500 to-indigo-500 text-white hover:shadow-md hover:shadow-blue-500/20 active:scale-95'
                                    : 'bg-gray-200/80 text-gray-500 hover:bg-gray-300/80'">
                                {{ fans.status === 2 ? '回关' : '取消关注' }}
                            </button>
                        </div>
                    </div>

                    <!-- 分页导航 -->
                    <div class="flex justify-center pt-2 border-t border-gray-200/50" v-if="fansTotal > fansPageSize">
                        <el-pagination size="small" background layout="prev, pager, next" :current-page="fansPage"
                            :page-size="fansPageSize" :total="fansTotal" @current-change="handleFansPageChange" />
                    </div>

                </div>
            </div>
        </Transition>
    </Teleport>
    <!-- 每日打卡弹窗 -->
    <Daily_signIn :visible="showDailySignIn" @close="showDailySignIn = false; loadUserSignInStatus()"></Daily_signIn>
    <Teleport to="body">
        <div class="fixed inset-0 z-1000 bg-black/40 backdrop-blur-xs flex flex-col items-center justify-center"
            v-if="showMemberBuy" @click.self="showMemberBuy = false">
            <div class="relative scale-90 md:scale-100 transition-all duration-355">
                <Member :visible="showMemberBuy" @close="showMemberBuy = false" />
            </div>
        </div>
    </Teleport>

    <!-- 举报弹窗 -->
    <UserReportDialog
        v-if="userInfo && userInfo.id"
        v-model="showReportDialog"
        :reported-id="userInfo.id"
        :target-nick="userInfo.nick"
    />
</template>

<script lang="ts" setup>
import {computed, onMounted, onUnmounted, reactive, ref, watch} from 'vue';
import UserReportDialog from '@/presentation/components/UserReportDialog.vue';
import {useUserInfoStore} from '@/stores/userInfo';
import router from '@/router';
import {useRoute} from 'vue-router';
import {
  UserAPI,
  type UserFansData,
  type UserProfileData,
  type UserProfileUpdateForm,
  type UserSimpleData
} from '@/services/user';
import {CommonAPI} from '@/services/common/api';
import {useAuthStore} from '@/views/auth/store';
import {log} from '@/utils/log';
import {PostStatus} from '@/services/post';
import {PostAPI} from '@/services/post/api';
import {formatNum} from '@/utils/page';
import UserProfileTabPublish from './user-profile-tab-publish.vue';
import {Plus} from '@element-plus/icons-vue';
import PrivateMessageInput from '@/presentation/components/private_message_input.vue';
import Daily_signIn from '@/presentation/components/daily_signIn.vue';
import Member from '@/views/member/pages/member.vue';
import {ToolBoxAPI} from '@/services/toolbox';

const route = useRoute();
const uid = computed(() => route.params.id as string);
const userStore = useUserInfoStore();
const showDailySignIn = ref(false);
const showReportDialog = ref(false);
const showMemberBuy = ref(false);
const auditFilter = ref<number>(-1);
const isDropdownOpen = ref(false);
const dropdownRef = ref<HTMLElement | null>(null);

const auditOptions = [
    { value: -1, label: '全部动态', dotClass: 'bg-slate-400' },
    { value: PostStatus.NORMAL, label: '正常发布', dotClass: 'bg-emerald-500' },
    { value: PostStatus.CHECK, label: '待审核', dotClass: 'bg-amber-500' },
    { value: PostStatus.UNPASS, label: '审核未通过', dotClass: 'bg-rose-500' },
    { value: PostStatus.BLOCKED, label: '已被封禁', dotClass: 'bg-purple-500' },
    { value: PostStatus.DRAFT, label: '草稿 / 私密', dotClass: 'bg-indigo-500' },
    { value: PostStatus.DELETED, label: '已删除', dotClass: 'bg-zinc-400' },
];

const activeOption = computed(() => {
    return auditOptions.find(o => o.value === auditFilter.value) || auditOptions[0]!;
});

const selectAuditOption = (val: number) => {
    auditFilter.value = val;
    isDropdownOpen.value = false;
};

const handleDocumentClick = (e: MouseEvent) => {
    if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
        isDropdownOpen.value = false;
    }
};

onMounted(() => {
    document.addEventListener('click', handleDocumentClick);
});

onUnmounted(() => {
    document.removeEventListener('click', handleDocumentClick);
});
const state = reactive({
    isSignIn: false
})

function getTagClass(index: number) {
    const styles = [
        'from-slate-50/90 to-slate-100/70 text-slate-600 border-slate-200/40 hover:from-slate-100/90 hover:to-slate-200/70 hover:text-slate-700 shadow-slate-500/5',
        'from-blue-50/90 to-sky-100/70 text-blue-600 border-blue-200/40 hover:from-blue-100/90 hover:to-sky-200/70 hover:text-blue-700 shadow-blue-500/5',
        'from-indigo-50/90 to-indigo-100/70 text-indigo-600 border-indigo-200/40 hover:from-indigo-100/90 hover:to-indigo-200/70 hover:text-indigo-700 shadow-indigo-500/5',
        'from-zinc-50/90 to-zinc-100/70 text-zinc-600 border-zinc-200/40 hover:from-zinc-100/90 hover:to-zinc-200/70 hover:text-zinc-700 shadow-zinc-500/5'
    ];
    return styles[index % styles.length];
}

async function loadUserSignInStatus() {
    if (!isme.value) return;
    try {
        const res = await ToolBoxAPI.getSignInStatus();
        if (res.code === 1 && res.data) {
            state.isSignIn = res.data.todayChecked;
        }
    } catch (e) {
        console.error('加载个人签到状态失败:', e);
    }
}
const showMessageInput = ref(false);
const messageContent = ref('');
const messageTargetUser = computed<UserSimpleData>(() => {
    if (!userInfo.value) {
        return { id: '', nick: '', avatar: '', memberLevel: '', memberLevelName: '', vipType: '', vipConfigIcon: '', ip: '' };
    }
    return {
        id: String(userInfo.value.id),
        nick: userInfo.value.nick,
        avatar: userInfo.value.avatar,
        memberLevel: userInfo.value.memberLevel,
        memberLevelName: '',
        vipType: userInfo.value.vipType,
        vipConfigIcon: userInfo.value.vipConfigIcon,
        ip: userInfo.value.ip,
        introduction: userInfo.value.introduction || '',
        fans: userInfo.value.fans || 0,
        liked: userInfo.value.liked || 0,
        topic: userInfo.value.topic || 0,
        gender: userInfo.value.gender !== undefined ? String(userInfo.value.gender) : undefined,
        followed: userInfo.value.followed
    };
});
const currentUser = computed(() => userStore.user);
const bgImage = ref<File | null>(null);
const isme = computed(() => uid.value === currentUser?.value?.id.toString());
const profileCompleteness = computed(() => {
    if (!userInfo.value) return 0;
    const fields = [
        userInfo.value.avatar,
        userInfo.value.nick,
        userInfo.value.gender,
        userInfo.value.age,
        userInfo.value.introduction
    ];
    let completedCount = 0;
    fields.forEach(val => {
        if (val !== null && val !== undefined && val !== '') {
            // 如果是默认占位头像，不计入完善度统计
            if (typeof val === 'string' && val.includes('cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')) {
                return;
            }
            completedCount++;
        }
    });
    return Math.round((completedCount / fields.length) * 100);
});
const curTab = ref(0);
const userInfo = ref<UserProfileData | null>(null);
const userTag = ref<{ id: number, name: string }[]>([]);
const pendingDeleteId = ref<string | null>(null);
const publishTabRef = ref();
const edit_profile = ref(false);
const is_editing = ref(false);
const handleDelete = (id: string) => {
    pendingDeleteId.value = id;
};
const editProfileForm = ref<UserProfileUpdateForm>(
    {
        introduction: userInfo.value?.introduction || null,
        nick: userInfo.value?.nick,
        avatar: userInfo.value?.avatar,
        age: userInfo.value?.age || null,
        gender: userInfo.value?.gender || null,
        bgImage: null
    }
)

const selectedFile = ref<File | null>(null);
const previewAvatarUrl = ref<string>('');
const avatarInput = ref<HTMLInputElement | null>(null);

function triggerAvatarSelect() {
    avatarInput.value?.click();
}

function handleAvatarChange(e: Event) {
    const target = e.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
        const file = target.files[0];
        if (file) {
            selectedFile.value = file;
            if (previewAvatarUrl.value) {
                URL.revokeObjectURL(previewAvatarUrl.value);
            }
            previewAvatarUrl.value = URL.createObjectURL(file);
        }
    }
}

watch(is_editing, (newVal) => {
    if (!newVal) {
        selectedFile.value = null;
        if (previewAvatarUrl.value) {
            URL.revokeObjectURL(previewAvatarUrl.value);
            previewAvatarUrl.value = '';
        }
    } else {
        if (userInfo.value) {
            editProfileForm.value = {
                introduction: userInfo.value.introduction || '',
                nick: userInfo.value.nick || '',
                avatar: userInfo.value.avatar || '',
                age: userInfo.value.age || 0,
                gender: userInfo.value.gender || 0,
                bgImage: null
            };
        }
    }
});

watch(edit_profile, (newVal) => {
    if (!newVal) {
        is_editing.value = false;
    }
});

const handleConfirmDelete = async () => {
    if (pendingDeleteId.value === null || pendingDeleteId.value === undefined) return;
    try {
        const res = await PostAPI.delete(pendingDeleteId.value);
        if (res.code === 1) {
            if (publishTabRef.value?.topicList) {
                const post = publishTabRef.value.topicList.find(
                    (p: any) => p.id?.toString() === pendingDeleteId.value?.toString()
                )
                if (post) {
                    post.status = PostStatus.DELETED
                }
            }
        } else {
            log.error(res.errMsg || "删除失败");
        }
    } catch (e) {
        console.error('删除帖子异常:', e);
        log.error("删除失败");
    } finally {
        pendingDeleteId.value = null;
    }
};
async function saveProfile() {
    if (!editProfileForm.value) return;

    let avatarUrl = editProfileForm.value.avatar;
    if (selectedFile.value) {
        try {
            const uploadRes = await CommonAPI.upload(selectedFile.value, 'avatar');
            if (uploadRes.code === 1 && uploadRes.data?.url) {
                avatarUrl = uploadRes.data.url;
                editProfileForm.value.avatar = avatarUrl;
            } else {
                log.error(uploadRes.errMsg || "头像上传失败");
                return;
            }
        } catch (error: any) {
            log.error(error.message || "头像上传失败");
            return;
        }
    }
    if (bgImage.value) {
        const uploadRes = await CommonAPI.upload(bgImage.value, 'bgImage');
        if (uploadRes.code === 1 && uploadRes.data?.url) {
            editProfileForm.value.bgImage = uploadRes.data.url;
        }
    }
    try {
        const { code, errMsg } = await UserAPI.updateProfile(editProfileForm.value)
        if (code != 1) {
            log.error(errMsg || "更新失败");
        } else {
            is_editing.value = false;
            if (uid.value) {
                await loadUserProfile(uid.value);
            }
            await userStore.flush();
        }
    } catch (error: any) {
        log.error(error.message || "更新失败");
    }
}
async function handleBgImageChange(uploadFile: any) {
    if (!uploadFile || !uploadFile.raw) return;
    const file = uploadFile.raw;
    try {
        const uploadRes = await CommonAPI.upload(file, 'bgImage');
        if (uploadRes.code === 1 && uploadRes.data?.url) {
            const newBgUrl = uploadRes.data.url;
            const updateForm: UserProfileUpdateForm = {
                nick: userInfo.value?.nick,
                avatar: userInfo.value?.avatar,
                gender: userInfo.value?.gender,
                age: userInfo.value?.age,
                introduction: userInfo.value?.introduction,
                bgImage: newBgUrl
            };
            const { code, errMsg } = await UserAPI.updateProfile(updateForm);
            if (code !== 1) {
                log.error(errMsg || "更换背景失败");
            } else {
                if (uid.value) {
                    await loadUserProfile(uid.value);
                }
                await userStore.flush();
            }
        } else {
            log.error(uploadRes.errMsg || "图片上传失败");
        }
    } catch (error: any) {
        log.error(error.message || "更换背景失败");
    }
}
async function handleResetBg() {
    try {
        const updateForm: UserProfileUpdateForm = {
            nick: userInfo.value?.nick,
            avatar: userInfo.value?.avatar,
            gender: userInfo.value?.gender,
            age: userInfo.value?.age,
            introduction: userInfo.value?.introduction,
            bgImage: ""
        };
        const { code, errMsg } = await UserAPI.updateProfile(updateForm);
        if (code !== 1) {
            log.error(errMsg || "重置背景失败");
        } else {
            bgImage.value = null;
            if (uid.value) {
                await loadUserProfile(uid.value);
            }
            await userStore.flush();
        }
    } catch (error: any) {
        log.error(error.message || "重置背景失败");
    }
}
async function loadUserProfile(targetUid: string) {
    if (!targetUid) return;
    try {
        const res = await UserAPI.getUserById(targetUid);

        if (res.code === 1 && res.data) {
            userInfo.value = res.data;
            userTag.value = [
                {
                    id: 1,
                    name: res.data.ip || "未知",
                },
                {
                    id: 2,
                    name: res.data.age + ''
                },
                {
                    id: 3,
                    name: res.data.gender == 1 ? '男' : '女'
                }
            ];

        } else {
            log.error('未找到该用户信息');
            router.push({ name: 'home' });
        }
    } catch (e) {
        console.error('获取用户信息失败', e);
        log.error('未找到该用户信息');
        router.push({ name: 'home' });
    }
}

onMounted(() => {
    loadUserProfile(uid.value);
    loadUserSignInStatus();
});

watch(uid, (newUid) => {
    if (newUid) {
        loadUserProfile(newUid);
        loadUserSignInStatus();
    }
});

async function handleFollow() {
    if (!currentUser.value?.id) {
        log.error("请先登录");
        const authStore = useAuthStore();
        authStore.showLoginDialog();
        return;
    }
    if (!userInfo.value?.id) return;
    if (String(userInfo.value.id) === String(currentUser.value.id)) {
        log.error("不能关注自己");
        return;
    }
    try {
        const followerId = currentUser.value.id;
        const followeeId = userInfo.value.id;
        const res = await UserAPI.toggleFollow(followerId, followeeId, 'profile');
        if (res.code === 1) {
            const oldFollowed = !!userInfo.value.followed;
            userInfo.value.followed = !oldFollowed;

            // 动态增加/减少当前显示的粉丝数，增加交互体验
            const fansNum = Number(userInfo.value.fans || 0);
            userInfo.value.fans = String(oldFollowed ? Math.max(0, fansNum - 1) : fansNum + 1);

        } else {
            log.error(res.errMsg || "操作失败");
        }
    } catch (e: any) {
        console.error('关注/取消关注异常:', e);
        log.error(e.message || "操作失败");
    }
}

function handleMessage() {
    const authStore = useAuthStore();
    if (!authStore.token) {
        log.warning("请先登录后操作");
        authStore.showLoginDialog();
        return;
    }
    if (userInfo.value?.id && String(userInfo.value.id) === String(currentUser.value?.id)) {
        log.error("不能给自己发送私信");
        return;
    }
    showMessageInput.value = true;
}

function handleMessageSuccess() {
    router.push({ name: 'message' });
}

function handleTabClick(tab: number) {
    if (tab === PostStatus.LIKED || tab === PostStatus.COLLECTED) {
        const authStore = useAuthStore();
        if (!authStore.token) {
            log.warning("请先登录后查看该内容");
            authStore.showLoginDialog();
            return;
        }
    }
    curTab.value = tab;
}

// ======================== 粉丝列表弹窗功能 ========================
const showFansDialog = ref(false);
const fansList = ref<UserFansData[]>([]);
const fansPage = ref(1);
const fansPageSize = ref(10);
const fansTotal = ref(0);
const fansLoading = ref(false);

async function openFansDialog() {
    showFansDialog.value = true;
    fansPage.value = 1;
    fansList.value = [];
    await loadFansList();
}

async function loadFansList() {
    if (fansLoading.value) return;
    fansLoading.value = true;
    try {
        const res = await UserAPI.getMyFans(uid.value, fansPage.value, fansPageSize.value);
        if (res.code === 1 && res.data) {
            fansList.value = res.data.records || [];
            fansTotal.value = res.data.total || 0;
        } else {
            log.error(res.errMsg || "拉取粉丝列表失败");
        }
    } catch (e: any) {
        log.error(e.message || "拉取粉丝列表失败");
    } finally {
        fansLoading.value = false;
    }
}

async function handleFansPageChange(newPage: number) {
    fansPage.value = newPage;
    await loadFansList();
}

async function handleFansFollowToggle(fans: UserFansData) {
    if (!currentUser.value?.id) {
        log.error("请先登录");
        return;
    }
    try {
        if (fans.status !== 2) {
            // 非删除状态（正常关注）：调用取消关注接口
            const res = await UserAPI.unfollowUser(fans.id);
            if (res.code === 1) {
                fans.status = 2;
            } else {
                log.error(res.errMsg || "取消关注失败");
            }
        } else {
            // 删除/已取消状态：调用关注接口
            const res = await UserAPI.followUser(currentUser.value.id, fans.userId, 'fans-dialog');
            if (res.code === 1) {
                fans.status = 1;
            } else {
                log.error(res.errMsg || "关注失败");
            }
        }
    } catch (e: any) {
        log.error(e.message || "操作失败");
    }
}

function handleGoToUser(userId: string | number) {
    showFansDialog.value = false;
    router.push({ name: 'userProfile', params: { id: String(userId) } });
}
</script>

<style scoped>
/* 弧度平滑向外凸起的反向圆角效果 */
.avatar-wrapper::before,
.avatar-wrapper::after {
    content: "";
    position: absolute;
    bottom: calc(50% - 0.5px);
    /* 微调贴合，确保与卡片顶部无缝拼合 */
    width: 32px;
    height: 32px;
    background: transparent;
    pointer-events: none;
    z-index: 10;
}

/* 左侧反向圆角 */
.avatar-wrapper::before {
    left: -30px;
    /* 稍微向右内收 2px，利用 avatar-wrapper 的圆形白底盖住伪元素的直边 */
    background: radial-gradient(circle at 0 0, transparent 34px, rgba(255, 255, 255, 0.3) 35px);
}

/* 右侧反向圆角 */
.avatar-wrapper::after {
    right: -30px;
    /* 稍微向左内收 2px，利用 avatar-wrapper 的圆形白底盖住伪元素的直边 */
    background: radial-gradient(circle at 100% 0, transparent 34px, rgba(255, 255, 255, 0.3) 35px);
}

.custom-edit-input :deep(.el-input__wrapper),
.custom-edit-input :deep(.el-textarea__inner) {
    border-radius: 8px;
    background-color: #f8fafc;
    border: 1px solid #e2e8f0;
    box-shadow: none !important;
    transition: all 0.3s;
    padding: 8px 12px;
}

.custom-edit-input :deep(.el-input__wrapper.is-focus),
.custom-edit-input :deep(.el-textarea__inner:focus) {
    background-color: #ffffff;
    border-color: #3b82f6;
    box-shadow: 0 0 0 1px #3b82f6 !important;
}
</style>