<template>
    <div class="w-full h-full flex bg-white p-2">
        <div class="tab md:w-1/3 w-1/2 flex flex-col  rounded-lg">
            <div class="flex w-full h-8 justify-center items-center sticky top-0">
                <span class="text-gray-400">全部消息</span>
            </div>
            <div class="flex flex-col grow overflow-y-auto">
                <!-- 互动消息固定会话栏 -->
                <div class="p-2 flex w-full min-h-18 gap-3 items-center hover:bg-gray-100 cursor-pointer transition-all duration-150 border-b border-gray-50/70"
                    @click="clickInteractionMessage()" :class="isCurSessionInteraction ? 'bg-gray-100' : ''">
                    <div class="relative size-12 shrink-0 flex items-center justify-center">
                        <svg t="1784882544725" class="icon size-12" viewBox="0 0 1024 1024" version="1.1"
                            xmlns="http://www.w3.org/2000/svg" p-id="6749" width="200" height="200">
                            <path d="M512 512m-512 0a512 512 0 1 0 1024 0 512 512 0 1 0-1024 0Z" fill="#7CBCF7"
                                p-id="6750"></path>
                            <path
                                d="M505.856 225.28C350.208 225.28 225.28 333.824 225.28 468.992c0 81.92 47.104 155.648 118.784 200.704v75.776c0 10.24 6.144 16.384 14.336 16.384 4.096 0 6.144-2.048 8.192-2.048l77.824-51.2c20.48 4.096 38.912 4.096 59.392 4.096 153.6 0 280.576-108.544 280.576-243.712 0-135.168-124.928-243.712-278.528-243.712z m0 446.464c-18.432 0-34.816-2.048-53.248-4.096l-8.192-2.048c-4.096-2.048-10.24 0-14.336 4.096l-6.144 4.096-36.864 22.528v-38.912c0-6.144-4.096-14.336-10.24-18.432l-10.24-6.144c-61.44-36.864-100.352-100.352-100.352-165.888 0-110.592 106.496-200.704 239.616-200.704s239.616 90.112 239.616 200.704c-2.048 112.64-110.592 204.8-239.616 204.8z m-159.744-196.608c0 18.432 14.336 32.768 32.768 32.768 18.432 0 32.768-14.336 32.768-32.768s-14.336-32.768-32.768-32.768c-18.432 0-32.768 14.336-32.768 32.768z m126.976 0c0 12.288 6.144 22.528 16.384 28.672 10.24 6.144 22.528 6.144 32.768 0s16.384-16.384 16.384-28.672c0-18.432-14.336-32.768-32.768-32.768-16.384 0-32.768 14.336-32.768 32.768z m122.88 0c0 18.432 14.336 32.768 32.768 32.768 18.432 0 32.768-14.336 32.768-32.768s-14.336-32.768-32.768-32.768c-16.384 0-32.768 14.336-32.768 32.768z m192.512 53.248c-6.144 14.336-12.288 30.72-20.48 45.056 0 0 0 2.048 2.048 2.048 4.096 12.288 8.192 26.624 8.192 38.912 0 36.864-20.48 71.68-57.344 92.16l-4.096 4.096c-4.096 2.048-6.144 4.096-6.144 10.24v22.528l-20.48-12.288-4.096-4.096c-4.096-2.048-4.096-2.048-8.192-2.048l-4.096 2.048c-10.24 2.048-20.48 4.096-28.672 4.096-22.528 0-45.056-4.096-63.488-14.336-20.48 4.096-38.912 6.144-59.392 8.192 30.72 28.672 75.776 45.056 124.928 45.056 12.288 0 24.576 0 36.864-4.096l47.104 32.768c2.048 0 4.096 2.048 4.096 2.048 4.096 0 10.24-4.096 10.24-10.24v-47.104c45.056-28.672 75.776-73.728 75.776-126.976-2.048-34.816-12.288-65.536-32.768-88.064z"
                                fill="#FFFFFF" p-id="6751"></path>
                        </svg>
                    </div>
                    <div class="flex flex-col gap-0.5 flex-1 min-w-0 h-full justify-center">
                        <span class="text-sm font-semibold text-gray-800">互动消息</span>
                        <span class="text-xs text-gray-400 truncate">收到的赞、评论、关注和回复</span>
                    </div>
                </div>

                <div v-for="session in sessionList" :key="session.id"
                    class="p-2 flex w-full min-h-18 gap-3 items-center hover:bg-gray-100 cursor-pointer transition-colors duration-150"
                    @click="curSession = session"
                    :class="((curSession) && (curSession.id === session.id)) ? 'bg-gray-100' : ''">
                    <div class="relative size-12 shrink-0">
                        <img :src="session.targetAvatar" class="size-12 bg-gray-400 rounded-lg relative"
                            referrerpolicy="no-referrer" alt="" />
                        <div v-if="session.unreadCount > 0"
                            class="absolute -top-1.5 -right-1.5 rounded-full min-w-5 h-5 px-1 bg-red-500 text-white text-[10px] flex items-center justify-center border-2 border-white font-bold">
                            {{ session.unreadCount }}
                        </div>
                    </div>

                    <div class="flex flex-col gap-1 flex-1 min-w-0 h-full justify-start">
                        <div class="flex items-center justify-between w-full h-1/2">
                            <span class="truncate text-sm font-medium text-gray-800">{{ session.targetNickName }}</span>
                            <span class="shrink-0 text-gray-400 text-[11px] ml-2">{{
                                TimeUtils.timestampToDate(session.lastTime) }}</span>
                        </div>
                        <div class="w-full truncate flex items-center gap-2 text-gray-500 text-sm">
                            <span class="truncate flex-1 flex items-center">
                                <span v-if="session.draft && session.draft.length > 0" class="text-blue-400">{{
                                    `[草稿]${session.draft}`
                                }}</span>
                                <div v-else-if="session.lastMessageContent" class="truncate">
                                    <span v-if="session.unreadCount">{{ `[${session.unreadCount}条]` }}</span>
                                    <span v-html="session.lastMessageContent"></span>
                                </div>

                            </span>
                        </div>
                    </div>

                </div>
            </div>

        </div>
        <div v-if="!isCurSessionInteraction" class="body relative z-0 md:w-2/3 w-1/2 flex flex-col ">
            <div class="top flex w-full justify-center  h-1/5 max-h-12 gap-2 items-center">
                <span class="truncate shrink-0">{{ curSession?.targetNickName }}</span>
                <Tooltip v-slot placement="bottom" content="消息免打扰" theme="glass">
                    <div v-if="curSession" class="flex justify-end cursor-pointer"
                        @click="curSession.isMute = curSession.isMute === 1 ? 0 : 1">
                        <svg t="1782120041738" class="icon size-4" viewBox="0 0 1024 1024" version="1.1"
                            v-if="curSession?.isMute === 1" xmlns="http://www.w3.org/2000/svg" p-id="4956">
                            <path
                                d="M639.459818 762.7715h-450.568536c-27.844123-0.506257-40.500543-4.303183-45.056854-6.32821 1.771899-5.568825 9.36575-17.718987 20.756528-29.109765l3.796926-4.303182c0.506257-1.265642 2.025027-2.025027 2.531284-2.531284l1.771899-1.265642c27.337866-25.312839 89.860579-84.798011 97.454431-354.886005 1.265642-27.844123 4.556311-53.663219 10.125135-76.444774 5.062568-18.225244-7.087595-37.463002-24.806582-41.006799-8.859494-2.025027-17.718987-1.265642-25.819096 3.796926-8.100109 5.062568-13.415805 12.150163-15.187704 21.515913-6.834467 27.844123-11.390778 58.21953-11.390777 91.126221C194.713235 605.072512 144.340685 652.407522 125.102927 670.885894c-3.037541 2.531284-6.32821 6.32821-9.36575 10.125136-13.162676 13.415805-53.916347 58.21953-36.703617 103.276383C98.018189 832.381808 165.60347 832.888065 188.385025 832.888065H639.712947c8.859494 0 17.718987-3.796926 24.806582-10.125136 6.32821-6.32821 10.125136-14.681447 10.125136-24.806582 0-8.859494-3.796926-17.718987-10.125136-24.806583-7.340723-6.581338-16.200217-10.378264-25.059711-10.378264zM409.619239 865.794755c0 51.131935 43.538083 93.151248 96.948174 93.151248s96.695045-41.766185 96.695046-93.151248v-8.100108h-193.64322v8.100108z"
                                p-id="4957" fill="#7F7F7F"></path>
                        </svg>
                        <svg t="1782120296065" class="icon size-4" viewBox="0 0 1024 1024" version="1.1"
                            v-if="curSession?.isMute !== 1" xmlns="http://www.w3.org/2000/svg" p-id="5004">
                            <path
                                d="M512 959.730047a80.774557 80.774557 0 0 0 72.492978-45.898611A80.774557 80.774557 0 0 1 512 959.730047z"
                                fill="#707070" p-id="5005"></path>
                            <path
                                d="M512 959.730047a80.774557 80.774557 0 0 1-72.492978-45.898611A80.774557 80.774557 0 0 0 512 959.730047zM512 1023.999767a145.80245 145.80245 0 0 0 140.378599-110.226652A145.80245 145.80245 0 0 1 512 1023.999767z"
                                fill="#707070" p-id="5006"></path>
                            <path
                                d="M512 1023.999767a142.478154 142.478154 0 0 1-52.488882-9.914567 142.478154 142.478154 0 0 0 52.488882 9.914567zM433.208356 1000.37977a146.793907 146.793907 0 0 1-11.955801-8.748147 146.677265 146.677265 0 0 0 11.955801 8.748147zM459.511118 1014.0852a144.63603 144.63603 0 0 1-20.120738-9.797925 144.63603 144.63603 0 0 0 20.120738 9.797925zM939.609425 850.43653L854.110869 659.027074V401.773231a342.635757 342.635757 0 0 0-266.760163-333.596005C578.427596 29.16049 548.100687 0 512 0S445.572404 29.16049 436.649294 68.177226a342.635757 342.635757 0 0 0-266.760163-333.596005V659.027074L84.390575 850.378209a45.023797 45.023797 0 0 0 40.824686 63.394906h773.336194a45.023797 45.023797 0 0 0 40.824686-63.394906zM512 45.898611a29.16049 29.16049 0 0 1 23.328392 14.638566c-7.698369-0.524889-15.45506-0.874815-23.328392-0.874814s-15.571702 0.349926-23.328392 0.874814a29.16049 29.16049 0 0 1 23.328392-14.638566z m112.442849 803.604784H155.192244l75.409028-168.780917a40.824686 40.824686 0 0 0 3.557579-16.796442V401.773231a277.841149 277.841149 0 0 1 555.623977 0v262.094484a40.824686 40.824686 0 0 0 3.6159 16.854763l75.409028 168.780917z"
                                fill="#707070" p-id="5007"></path>
                            <path
                                d="M512 959.730047a80.774557 80.774557 0 0 1-72.492978-45.898611H371.621401a145.044277 145.044277 0 0 0 33.534564 62.578411 147.085512 147.085512 0 0 0 28.052391 23.969923q3.091012 2.041234 6.298666 3.907505a144.63603 144.63603 0 0 0 20.004096 9.797925 142.478154 142.478154 0 0 0 52.488882 9.914567 145.80245 145.80245 0 0 0 140.378599-110.226652H584.492978A80.774557 80.774557 0 0 1 512 959.730047z"
                                fill="#707070" p-id="5008"></path>
                        </svg>
                    </div>
                </Tooltip>
            </div>
            <div ref="chatContainer" class="grow overflow-y-auto bg-gray-100/50 flex flex-col gap-4 p-2">
                <!-- 顶部 Loading 加载器 -->
                <div v-if="loading && hasMore" class="w-full flex justify-center py-2 shrink-0">
                    <div class="flex items-center gap-2 text-blue-400 text-xs">
                        <svg class="animate-spin size-4" xmlns="http://www.w3.org/2000/svg" fill="none"
                            viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4">
                            </circle>
                            <path class="opacity-75" fill="currentColor"
                                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                            </path>
                        </svg>
                        <span>加载历史消息中...</span>
                    </div>
                </div>
                <UserMsgItem v-for="(message, index) in msgList" :key="message.id" :message="message"
                    :lastMsgTime="index > 0 ? msgList[index - 1]!.sendTime : undefined" @delete="handleDeleteMsg"
                    @resend="handleResend"
                    :is-last="msgList.length > 0 && message.id === msgList[msgList.length - 1]?.id">
                </UserMsgItem>
            </div>
            <div class="input h-2/5 bg-white flex flex-col border-t border-gray-150/50">
                <div class="flex items-center gap-4 px-4 pt-3 pb-1 shrink-0">

                    <Emoji place-holder="" :textarea-ref="chatContainer" v-model="sendForm.content"></Emoji>

                    <Tooltip placement="top" content="发送图片">
                        <el-upload class="size-6" action="#" :auto-upload="false" :show-file-list="false"
                            :on-change="handleImageChange" multiple>
                            <svg t="1782123262004"
                                class="icon size-6 cursor-pointer hover:fill-gray-700 transition-colors"
                                viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4988">
                                <path
                                    d="M192 106.666667a85.333333 85.333333 0 0 0-85.333333 85.333333v640a85.333333 85.333333 0 0 0 85.333333 85.333333h640a85.333333 85.333333 0 0 0 85.333333-85.333333V192a85.333333 85.333333 0 0 0-85.333333-85.333333H192z m0 85.333333h640v640H192V192z m213.333333 85.333333h-128v128h128v-128z m341.333334 469.333334V409.002667L516.010667 639.658667 437.333333 538.496 275.413333 746.666667H746.666667z"
                                    fill="#8a8a8a" p-id="4989"></path>
                            </svg>
                        </el-upload>
                    </Tooltip>
                </div>

                <!-- 选中的图片缩略图预览列表 -->
                <div v-if="imageInput.length > 0"
                    class="flex flex-wrap gap-2 px-4 py-2 bg-gray-50/70 border-b border-gray-150/30">
                    <div v-for="(file, idx) in imageInput" :key="idx"
                        class="relative group size-16 rounded-lg overflow-hidden border border-gray-200/80 shadow-xs shrink-0 bg-white">
                        <img :src="getFileUrl(file)" class="w-full h-full object-cover" />
                        <!-- 悬浮删除图片 -->
                        <div
                            class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                            <svg @click="removeImage(idx)" class="size-5 text-white cursor-pointer hover:text-red-400"
                                viewBox="0 0 1024 1024">
                                <path fill="currentColor"
                                    d="M352 192V128a64 64 0 0 1 64-64h192a64 64 0 0 1 64 64v64h192a32 32 0 1 1 0 64H96a32 32 0 1 1 0-64h256zm64 0h192V128H416v64zm-96 128h384v512a128 128 0 0 1-128 128H352a128 128 0 0 1-128-128V320zm128 96a32 32 0 0 0-64 0v384a32 32 0 0 0 64 0V416zm192 0a32 32 0 0 0-64 0v384a32 32 0 0 0 64 0V416z">
                                </path>
                            </svg>
                        </div>
                    </div>
                </div>

                <div class="flex-1 px-4 py-1">
                    <textarea
                        class="w-full h-full border-none focus:outline-hidden focus:ring-0 p-1 resize-none text-sm text-gray-700 placeholder-gray-400 bg-transparent"
                        placeholder="请输入聊天内容,按Enter发送,按Ctrl+Enter换行。" v-model="sendForm.content"
                        @keydown.enter.exact.prevent="sendMsg" @keydown.ctrl.enter="handleNewLine">
                    </textarea>
                </div>


                <div class="flex justify-end px-4 pb-3 shrink-0">
                    <button
                        class="px-5 py-1.5 rounded-lg text-sm font-medium text-white bg-blue-400 hover:bg-blue-600 active:scale-95 transition-all duration-150 shadow-xs cursor-pointer"
                        @click="sendMsg">
                        发送
                    </button>
                </div>
            </div>
        </div>

        <!-- 互动消息详情页面 (列表模式) -->
        <div v-if="isCurSessionInteraction && !showPostDetail"
            class="body relative z-0 md:w-2/3 w-1/2 flex flex-col bg-slate-50/50">
            <div
                class="top flex w-full justify-center h-12 gap-2 items-center border-b border-gray-150/40 bg-white shrink-0">
                <span class="font-semibold text-gray-800">收到的互动消息</span>
            </div>

            <div @scroll="handleInteractionScroll" class="grow overflow-y-auto flex flex-col gap-4.5 p-4">
                <div v-if="interactionList.length === 0 && !interactionLoading"
                    class="flex flex-col items-center justify-center grow py-24 text-gray-400 gap-3">
                    <svg class="size-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"
                        stroke-width="1.2">
                        <path stroke-linecap="round" stroke-linejoin="round"
                            d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                    </svg>
                    <span class="text-sm font-medium">暂无任何互动消息哦</span>
                </div>

                <div v-else class="flex flex-col gap-3.5">
                    <div v-for="msg in interactionList" :key="msg.id"
                        class="relative group bg-white p-4.5 rounded-xl border border-gray-100/80 shadow-xs flex gap-3.5 hover:shadow-md transition-all duration-200 animate-fade-in">

                        <!-- 互动者头像 (左侧) -->
                        <div class="avatar shrink-0 size-10.5 rounded-full overflow-hidden bg-slate-100 border border-gray-100 cursor-pointer"
                            @click="goToUserProfile(msg.senderId)">
                            <img :src="msg.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                                class="w-full h-full object-cover" />
                        </div>

                        <!-- 中间内容与动作 -->
                        <div class="flex-1 min-w-0 flex flex-col gap-1.5">
                            <div class="flex flex-wrap items-baseline gap-1.5 text-sm">
                                <span class="font-bold text-gray-800 hover:text-blue-500 cursor-pointer"
                                    @click="goToUserProfile(msg.senderId)">
                                    {{ msg.senderName || '匿名用户' }}
                                </span>
                                <span class="text-gray-500 font-medium text-xs">
                                    {{ getActionDesc(msg.messageType) }}
                                </span>
                            </div>

                            <!-- 回复/评论的内容显示 (支持 v-html) -->
                            <div v-if="msg.content"
                                class="text-gray-700 text-sm bg-slate-50 p-2.5 rounded-lg  wrap-break-word leading-relaxed"
                                v-html="msg.content">
                            </div>

                            <!-- 如果是回复消息，下方增加两个互动图标 -->
                            <div v-if="msg.messageType === 5 || msg.messageType === 2"
                                class="flex items-center gap-4 mt-1 select-none">
                                <!-- 爱心点赞 -->
                                <div @click="toggleLike(msg)"
                                    class="flex items-center gap-1 text-xs cursor-pointer transition-colors"
                                    :class="msg.isLike ? 'text-red-500' : 'text-gray-400 hover:text-red-500'">
                                    <svg class="size-3.5" viewBox="0 0 24 24" :fill="msg.isLike ? '#f56c6c' : 'none'"
                                        :stroke="msg.isLike ? '#f56c6c' : 'currentColor'">
                                        <path
                                            d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
                                    </svg>
                                    <span>赞</span>
                                </div>
                                <!-- 头像回复评论 -->
                                <div class="flex items-center gap-1 text-xs text-gray-400 cursor-pointer hover:text-blue-500 transition-colors"
                                    @click="toggleReplyInput(msg.id)">
                                    <img :src="msg.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                                        class="size-3.5 rounded-full object-cover" />
                                    <span>{{ currentReplyMsgId === msg.id ? '收起' : '回复评论' }}</span>
                                </div>
                            </div>

                            <!-- 内联回复输入框 -->
                            <div v-if="currentReplyMsgId === msg.id"
                                class="reply-input w-full transition-all duration-300 mt-2">
                                <el-input ref="replyInputRef" :placeholder="`回复@${msg.senderName}`"
                                    v-model="replyInputContent" class="w-full" size="small"
                                    @keydown.enter.exact.prevent="submitInteractionReply(msg)">
                                    <template #suffix>
                                        <div class="relative cursor-pointer mr-2">
                                            <Emoji v-model="replyInputContent" :place-holder="' '"
                                                :textarea-ref="replyInputRef" />
                                        </div>
                                        <span class="cursor-pointer hover:text-blue-300 text-xs" v-if="!replyErrMsg"
                                            @click="submitInteractionReply(msg)">回复</span>
                                        <span v-else class="text-xs">
                                            <span class="text-red-400">{{ replyErrMsg }}</span>
                                        </span>
                                    </template>
                                </el-input>
                            </div>

                            <span class="text-gray-400 text-[10px]">{{ TimeUtils.timestampToDate(msg.createTime)
                            }}</span>
                        </div>

                        <!-- 互动对象快照 (右侧) - 点击进入帖子详情与评论区 -->
                        <div v-if="msg.associateContentTitle"
                            class="associate-right shrink-0 w-24 max-h-16 bg-slate-50 border border-gray-100 p-2.5 rounded-lg text-xs text-gray-400 hover:text-blue-500 transition-colors cursor-pointer line-clamp-3 select-none pr-6"
                            @click="clickAssociateContent(msg)">
                            {{ msg.associateContentTitle }}
                        </div>

                        <!-- 删除单个消息按钮 -->
                        <div class="absolute right-3 top-3 opacity-0 group-hover:opacity-100 transition-all duration-150 cursor-pointer text-gray-300 hover:text-red-500"
                            @click.stop="handleDeleteInteraction(msg.id)">
                            <svg class="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"
                                stroke-width="1.8">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </div>
                    </div>
                </div>

                <!-- 加载中 -->
                <div v-if="interactionLoading"
                    class="w-full flex justify-center py-2 text-blue-400 text-xs shrink-0 animate-pulse">
                    <svg class="animate-spin size-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4">
                        </circle>
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                        </path>
                    </svg>
                    <span>正在加载更多消息...</span>
                </div>
            </div>
        </div>

        <!-- 互动消息 -> 帖子详情与评论区 (内嵌模式) -->
        <div v-if="isCurSessionInteraction && showPostDetail"
            class="body relative z-0 md:w-2/3 w-1/2 flex flex-col bg-white">
            <div
                class="top flex w-full justify-start h-12 gap-2 items-center border-b border-gray-150/40 bg-white shrink-0 px-4">
                <svg class="size-5 cursor-pointer hover:text-blue-500 text-gray-500 transition-colors shrink-0"
                    @click="backToInteractionList" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                    stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
                </svg>
                <span class="font-semibold text-gray-800 truncate">{{ curPostData?.title || '帖子详情' }}</span>
            </div>
            <div class="grow overflow-y-auto bg-white">
                <postInfo v-if="curPostData" @close="backToInteractionList" :visible="true" :post="curPostData" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import Emoji from '@/presentation/components/emoji.vue';
import Tooltip from '@/presentation/components/Tooltip.vue';
import UserMsgItem from '@/presentation/components/userMsgItem.vue';
import {
  type InteractionMessageVO,
  MessageAPI,
  messageStatus,
  messageType,
  type messageVO,
  type SendMessageDto,
  SessionAPI,
  type SessionVO
} from '@/services/message/message';
import {TimeUtils} from '@/utils/time';
import {computed, nextTick, onMounted, onUnmounted, reactive, ref, watch} from 'vue';
import {CommentAPI} from '@/services/comment/api';
import {scrollerFromTop} from '@/utils/scollerbar';
import {CommonAPI} from '@/services/common/api';
import {useUserInfoStore} from '@/stores/userInfo';
import {log} from '@/utils/log';
import {getSnowflakeId} from '@/utils/snowflake';
import {formatMessage} from '@/utils/messageFormat';
import {WsEventName} from '@/services/ws/config/config';

import {useRouter} from 'vue-router';
import postInfo from '@/views/post/pages/postInfo.vue';
import {PostAPI, type PostVO} from '@/services/post';
import {XssUtils} from '@/utils/xss';
import {parseEmoji} from '@/utils/emoji';
import {IM, removeIMListener} from '@/services/ws/im/init';

const router = useRouter();

const isCurSessionInteraction = ref(false);
const interactionList = ref<InteractionMessageVO[]>([]);
const interactionLoading = ref(false);
const interactionHasMore = ref(true);

// 互动消息内联回复评论
const currentReplyMsgId = ref<string | null>(null);
const replyInputContent = ref('');
const replyInputRef = ref();
const replyErrMsg = ref('');

const showPostDetail = ref(false);
const curPostData = ref<PostVO | null>(null);

function getActionDesc(type: number) {
    switch (type) {
        case 1: return '赞了你的帖子';
        case 2: return '评论了你的帖子';
        case 3: return '关注了你';
        case 4: return '提及了你';
        case 5: return '回复了你的评论';
        case 6: return '收藏了你的帖子';
        default: return '与你进行了互动';
    }
}

async function loadInteractions(isFirst = false) {
    if (interactionLoading.value) return;
    if (!isFirst && !interactionHasMore.value) return;

    try {
        interactionLoading.value = true;
        if (isFirst) {
            interactionList.value = [];
            interactionHasMore.value = true;
        }

        const lastPublicId = isFirst || interactionList.value.length === 0
            ? null
            : interactionList.value[interactionList.value.length - 1]?.id;

        const res = await MessageAPI.loadInteractionMessages(lastPublicId!, 10);
        if (res.code === 1 && res.data) {
            if (isFirst) {
                interactionList.value = res.data;
            } else {
                interactionList.value.push(...res.data);
            }
            if (res.data.length < 10) {
                interactionHasMore.value = false;
            }
        } else {
            interactionHasMore.value = false;
        }
    } catch (e) {
        console.error('加载互动消息失败:', e);
    } finally {
        interactionLoading.value = false;
    }
}

function clickInteractionMessage() {
    isCurSessionInteraction.value = true;
    curSession.value = null;
    loadInteractions(true);
}

function handleInteractionScroll(e: Event) {
    const target = e.target as HTMLDivElement;
    if (target.scrollHeight - target.scrollTop - target.clientHeight < 30) {
        loadInteractions();
    }
}

function goToUserProfile(userId: string | number) {
    router.push({ name: 'userProfile', params: { id: String(userId) } });
}

async function clickAssociateContent(msg: InteractionMessageVO) {
    if (!msg.postId) return;
    try {
        const res = await PostAPI.getById(msg.postId);
        if (res.code === 1 && res.data) {
            curPostData.value = res.data;
            showPostDetail.value = true;
        }
    } catch (e) {
        console.error('加载帖子详情失败:', e);
    }
}

function backToInteractionList() {
    showPostDetail.value = false;
    curPostData.value = null;
}

async function toggleLike(msg: InteractionMessageVO) {
    if (!msg.associateContent) console.log('msg.associateContent is null');
    try {
        const res = await CommentAPI.like(msg.associateContent);
        if (res.code === 1) {
            msg.isLike = !msg.isLike;
        }
    } catch (e) {
        console.error('点赞失败:', e);
    }
}

function toggleReplyInput(id: string) {
    if (currentReplyMsgId.value === id) {
        currentReplyMsgId.value = null;
        replyInputContent.value = '';
        replyErrMsg.value = '';
    } else {
        currentReplyMsgId.value = id;
        replyInputContent.value = '';
        replyErrMsg.value = '';
        nextTick(() => {
            replyInputRef.value?.focus?.();
        });
    }
}

async function submitInteractionReply(msg: InteractionMessageVO) {
    if (!replyInputContent.value.trim()) {
        replyErrMsg.value = '请输入内容';
        setTimeout(() => { replyErrMsg.value = '' }, 3000);
        return;
    }
    try {
        const content = replyInputContent.value;
        currentReplyMsgId.value = null;
        replyInputContent.value = '';
        const res = await CommentAPI.post({
            postId: msg.postId,
            rootId: msg.associateContent,
            parentId: msg.associateContent,
            content: XssUtils.filter(content),
            type: 1,
        });
        if (res.code === 1) {
            log.success('回复发表成功');
        } else {
            throw new Error(res.errMsg || '回复发表失败');
        }
    } catch (e: any) {
        console.error('回复失败:', e);
        replyErrMsg.value = e.message || '回复失败,请重试';
        setTimeout(() => { replyErrMsg.value = '' }, 3000);
    }
}

async function handleDeleteInteraction(id: string) {
    try {
        const res = await MessageAPI.deleteInteractionMessage(id);
        if (res.code === 1) {
            interactionList.value = interactionList.value.filter(msg => msg.id !== id);
            log.success('删除互动消息成功');
        } else {
            log.error(res.errMsg || '删除互动消息失败');
        }
    } catch (e) {
        console.error('删除互动消息失败:', e);
        log.error('删除互动消息失败');
    }
}
const imageInput = ref<any[]>([]);
const me = useUserInfoStore().user;

const curSession = ref<SessionVO | null>(null);
const chatContainer = ref<HTMLDivElement | null>(null);
const loading = ref(false);
const hasMore = ref(true);
const sessionId = ref('')
const msgList = ref<messageVO[]>([])
const sendForm = reactive<SendMessageDto>({
    receiverId: '',
    content: '',
    image: '',
    audio: '',
    video: '',
    type: messageType.TEXT,
    sendTime: '',
})
const sessionList = defineModel<SessionVO[]>();


function updateSessionAndTop(targetId: string, content: string, time: string, isSelfSend = false) {
    if (!sessionList.value) return;
    const session = sessionList.value.find((s: SessionVO) => String(s.targetId) === String(targetId));
    if (session) {
        session.lastMessageContent = content;
        session.lastTime = time;
        if (!isSelfSend) {
            if (curSession.value && String(curSession.value.targetId) === String(targetId)) {
                session.unreadCount = 0;
            } else {
                session.unreadCount = (session.unreadCount || 0) + 1;
            }
        }
        const idx = sessionList.value.indexOf(session);
        if (idx !== -1) {
            sessionList.value.splice(idx, 1);
            sessionList.value.unshift(session);
        }
    }
}

function scrollToBottom() {
    nextTick(() => {
        if (chatContainer.value) {
            chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
        }
    });
}

function handleImageChange(uploadFile: any) {
    if (uploadFile && uploadFile.raw) {
        imageInput.value.push(uploadFile);
    }
}

function removeImage(idx: number) {
    imageInput.value.splice(idx, 1);
}

function getFileUrl(file: any) {
    const rawFile = file.raw || file;
    return URL.createObjectURL(rawFile);
}

const pendingFilesMap = new Map<string, any>();

async function sendMsg() {
    if (!curSession.value) return;
    if (!sendForm.content.trim() && imageInput.value.length === 0) {
        log.warning('发送消息内容为空');
        return;
    }

    // 1. 提前组装独立的消息数据，实现非阻塞追加
    const currentMsgId = getSnowflakeId();
    const currentSendTime = TimeUtils.now();
    const submitContent = sendForm.content;
    const parsedContent = parseEmoji(submitContent);
    const submitImages = [...imageInput.value];

    // 重置输入状态与图片选择预览
    sendForm.content = '';
    imageInput.value = [];

    const pendingMsg: messageVO = {
        id: currentMsgId,
        user: me!,
        sendTime: currentSendTime,
        content: parsedContent,
        image: '',
        audio: '',
        video: '',
        type: submitImages.length > 0 ? messageType.IMAGE : messageType.TEXT,
        status: messageStatus.UNREAD,
        sessionId: sessionId.value,
        sending: true,
        failed: false
    };

    // 如果发送图片，在本地追加时生成本地的 blob 预览，保证零卡顿加载
    if (submitImages.length > 0) {
        const firstFile = submitImages[0].raw || submitImages[0];
        pendingMsg.image = getFileUrl(firstFile);
        // 暂存 File 用于失败重试
        pendingFilesMap.set(currentMsgId, firstFile);
    }

    msgList.value.push(pendingMsg);
    scrollToBottom();

    // 本地即时更新左侧会话列表的最后消息和时间，并置顶当前会话
    updateSessionAndTop(curSession.value.targetId, submitImages.length > 0 ? '[图片]' : parsedContent, currentSendTime, true);

    // 2. 异步处理发送请求
    try {
        const command: SendMessageDto = {
            msgId: currentMsgId,
            receiverId: curSession.value.targetId,
            content: XssUtils.filter(submitContent),
            image: '',
            audio: '',
            video: '',
            sendTime: currentSendTime,
            type: submitImages.length > 0 ? messageType.IMAGE : messageType.TEXT
        };

        if (submitImages.length > 0) {
            const file = pendingFilesMap.get(currentMsgId);
            const uploadRes = await CommonAPI.upload(file, 'message-media');
            if (uploadRes.code === 1 && uploadRes.data?.url) {
                command.image = uploadRes.data.url;
                const targetMsg = msgList.value.find(m => String(m.id) === String(currentMsgId));
                if (targetMsg) {
                    targetMsg.image = uploadRes.data.url;
                }
            } else {
                throw new Error(uploadRes.errMsg || "图片上传失败");
            }
        }

        await MessageAPI.sendMessage(command);
        const target = msgList.value.find(m => String(m.id) === String(currentMsgId));
        if (target) {
            target.sending = false;
            target.failed = false;
        }
        pendingFilesMap.delete(currentMsgId);
    } catch (e: any) {
        console.error("发送私信消息失败:", e);
        const target = msgList.value.find(m => String(m.id) === String(currentMsgId));
        if (target) {
            target.sending = false;
            target.failed = true;
        }
    }
}
function resolveContent() { }
async function handleResend(message: messageVO) {
    if (!curSession.value) return;
    const target = msgList.value.find(m => String(m.id) === String(message.id));
    if (target) {
        target.sending = true;
        target.failed = false;
    }

    try {
        const command: SendMessageDto = {
            msgId: message.id,
            receiverId: curSession.value.targetId,
            content: XssUtils.filter(message.content),
            image: '',
            audio: message.audio,
            video: message.video,
            sendTime: message.sendTime,
            type: message.type
        };

        const file = pendingFilesMap.get(message.id);
        if (file) {
            const uploadRes = await CommonAPI.upload(file, 'message-media');
            if (uploadRes.code === 1 && uploadRes.data?.url) {
                command.image = uploadRes.data.url;
                const targetMsg = msgList.value.find(m => String(m.id) === String(message.id));
                if (targetMsg) {
                    targetMsg.image = uploadRes.data.url;
                }
            } else {
                throw new Error(uploadRes.errMsg || "图片上传失败");
            }
        } else if (message.type === messageType.IMAGE) {
            // 若未找到暂存 File，但该消息本身已经是图片类型，说明先前可能已经上传成功了，直接取其 image
            command.image = message.image;
        }

        await MessageAPI.sendMessage(command);
        const targetSuccess = msgList.value.find(m => String(m.id) === String(message.id));
        if (targetSuccess) {
            targetSuccess.sending = false;
            targetSuccess.failed = false;
        }
        pendingFilesMap.delete(message.id);

        // 重发成功后更新左侧会话最后时间
        updateSessionAndTop(curSession.value.targetId, message.type === messageType.IMAGE ? '[图片]' : message.content, message.sendTime, true);
    } catch (e: any) {
        console.error("重试发送消息失败:", e);
        const targetFail = msgList.value.find(m => String(m.id) === String(message.id));
        if (targetFail) {
            targetFail.sending = false;
            targetFail.failed = true;
        }
    }
}

function handleNewLine() {
    sendForm.content += '\n';
}

async function handleDeleteMsg(messageId: string) {
    if (!curSession.value) return;
    await MessageAPI.deleteMessage(messageId, curSession.value.targetId);
    const idx = msgList.value.findIndex(m => m.id === messageId);
    if (idx !== -1) {
        msgList.value.splice(idx, 1);
    }
}
async function loadHistoryMessage(friendId: string): Promise<string | null> {
    if (loading.value || !hasMore.value || !curSession.value) return null;
    const container = chatContainer.value;
    const oldScrollHeight = container ? container.scrollHeight : 0;
    const oldScrollTop = container ? container.scrollTop : 0;
    const isFirstLoad = msgList.value.length === 0;

    const targetFriend = {
        id: curSession.value.targetId,
        nick: curSession.value.targetNickName,
        avatar: curSession.value.targetAvatar
    };

    try {
        loading.value = true;
        const cursorId = msgList.value.length > 0 ? (msgList.value[0]?.id ?? null) : null;
        const res = await MessageAPI.loadMessageHistoryByFriendId(friendId, cursorId, 10);
        const resList = res.data;
        if (!resList || !resList.messages) return null;

        const formatted = resList.messages.map((m: any) => {
            const fm = formatMessage(m, me, { user: targetFriend });
            fm.sending = false;
            fm.failed = false;
            return fm;
        }).reverse();
        msgList.value = [...formatted, ...msgList.value]
        hasMore.value = resList.hasMore

        if (container) {
            nextTick(() => {
                if (isFirstLoad) {
                    container.scrollTop = container.scrollHeight;
                } else {
                    container.scrollTop = oldScrollTop + (container.scrollHeight - oldScrollHeight);
                }
            });
        }
        return resList.sessionId;
    } finally {
        loading.value = false;
    }
}

async function draft(oldSession: SessionVO) {
    if (((sendForm.content.length > 0 && (oldSession.draft || oldSession.draft.length == 0)) || (sendForm.content.length == 0 && oldSession.draft && oldSession.draft.length > 0)) && sessionId.value && oldSession.draft != sendForm.content) {
        XssUtils.filter(sendForm.content);
        SessionAPI.draft(sendForm.content, sessionId.value);
        if (oldSession) oldSession.draft = sendForm.content;
        sendForm.content = '';
    }
}
const unReadCount = computed(() => {
    let count = 0;
    if (!sessionList.value) return;
    sessionList.value.forEach(seesion => { count += seesion.unreadCount })
    return count;
})

watch(() => curSession.value, async (newVal, oldVal) => {
    if (newVal) {
        isCurSessionInteraction.value = false;
    }
    hasMore.value = true;
    msgList.value = []
    if (oldVal) await draft(oldVal);
    if (curSession.value) {
        sessionId.value = curSession.value.id || '';
        await loadHistoryMessage(curSession.value.targetId);
        if (sessionId.value) {
            await MessageAPI.read(sessionId.value);
            curSession.value.unreadCount = 0;
        }
        sendForm.content = curSession.value.draft || '';
        scrollToBottom();
        return;
    }
    console.log("当前会话为空");
})
const handleImMessage = (e: Event) => {
    const customEvent = e as CustomEvent;
    const { type, data } = customEvent.detail;
    console.log("收到消息", type);


    if (type === WsEventName.NORMAL_MESSAGE) {
        const msg = data;
        if (curSession.value &&
            (String(msg.userId) === String(curSession.value.targetId) || String(msg.receiverId) === String(curSession.value.targetId))) {

            const formatted = formatMessage(msg, me, {
                user: {
                    id: curSession.value.targetId,
                    nick: curSession.value.targetNickName,
                    avatar: curSession.value.targetAvatar
                }
            });
            formatted.sending = false;
            formatted.failed = false;
            if (!msgList.value.some(m => String(m.id) === String(formatted.id))) {
                msgList.value.push(formatted);
                scrollToBottom();

                // 如果是当前正在聊天的朋友发来的新消息，且存在会话 ID，则立刻上报已读状态更新
                if (String(msg.userId) === String(curSession.value.targetId) && sessionId.value) {
                    MessageAPI.read(sessionId.value);
                }
            }
        }

        const friendId = String(msg.userId) === String(me!.id) ? String(msg.receiverId) : String(msg.userId);
        updateSessionAndTop(friendId, msg.content || '[图片]', msg.sendTime, false);
    } else if (type === WsEventName.WITHDRAWN_MESSAGE) {
        const msgId = String(data.id || data);
        const idx = msgList.value.findIndex(m => String(m.id) === msgId);
        if (idx !== -1) {
            msgList.value.splice(idx, 1);
        }
    }

    else if (type === WsEventName.READ_MESSAGE) {
        console.log("已读消息:", data);
        // data.sessionId 代表单条消息所属的会话 ID
        // data.id 在会话已读通知中代表会话 ID，在单条消息已读中代表消息 ID
        const eventSessionId = data.sessionId || data.id;

        if (curSession.value && String(sessionId.value) === String(eventSessionId)) {
            if (data.sessionId) {
                // 单条消息已被读
                const msg = msgList.value.find(m => String(m.id) === String(data.id));
                if (msg) {
                    msg.status = messageStatus.READ;
                }
            } else {
                // 整个会话的消息已被读，更新我发出的所有消息状态
                msgList.value.forEach(m => {
                    if (String(m.user.id) === String(me!.id)) {
                        m.status = messageStatus.READ;
                    }
                });
            }
        }
    }
}

let unbindScroll: (() => void) | null = null;
const initScrollListener = () => {
    if (unbindScroll) {
        unbindScroll();
    }
    if (chatContainer.value) {
        unbindScroll = scrollerFromTop(() => {
            if (curSession.value) {
                loadHistoryMessage(curSession.value.targetId);
            }
        }, chatContainer.value);
    }
};

onMounted(async () => {
    removeIMListener();
    window.addEventListener(IM, handleImMessage);
    if (sessionList.value && sessionList.value.length > 0) {
        curSession.value = sessionList.value[0] as SessionVO;
    } else {
        clickInteractionMessage();
    }

    nextTick(() => {
        initScrollListener();
    });
});
onUnmounted(() => {
    window.removeEventListener(IM, handleImMessage);
    if (unbindScroll) {
        unbindScroll();
    }
    if (curSession.value) draft(curSession.value);
    hasMore.value = true;
    msgList.value = []
})


</script>