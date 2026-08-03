<template>
    <div class="md:w-full flex flex-col gap-1">
        <!-- 聊天时间居中展示，如果是第一条消息或者距离上一条消息超过2分钟 (120秒) 就会显示 -->
        <div v-if="!props.lastMsgTime || TimeUtils.calculateTimeByNow(props.message.sendTime, props.lastMsgTime) > 120"
            class="flex justify-center my-2 w-full">
            <span class="text-xs text-gray-400 bg-slate-200/50 px-2.5 py-0.5 rounded-full select-none">
                {{ TimeUtils.timestampToDate(props.message.sendTime) }}
            </span>
        </div>

        <!-- 消息主体 -->
        <div class="flex w-full gap-3 group" :class="isSelf ? 'flex-row-reverse' : 'flex-row'">

            <div class="flex flex-col items-center justify-start shrink-0">
                <img :src="message.user.avatar" class="size-10 rounded-full border border-white shadow-xs object-cover"
                    referrerpolicy="no-referrer" alt="">
            </div>

            <div class="flex w-[75%] " :class="isSelf ? 'flex-row' : 'flex-row-reverse'">

                <div class="flex flex-col w-full" :class="isSelf ? 'items-end' : 'items-start'">
                    <div class="nick text-xs text-gray-400 mb-1 px-1">{{ message.user.nick }}</div>
                    <div class="flex" :class="isSelf ? 'flex-row-reverse' : 'flex-row'">
                        <div class="relative px-4 py-2.5 rounded-2xl shadow-xs text-sm flex flex-col gap-2 transition-all duration-200"
                            :class="isSelf
                                ? 'bg-linear-to-br from-blue-500 via-blue-600 to-indigo-500 text-white rounded-tr-none shadow-blue-500/10'
                                : 'bg-white text-gray-800   rounded-tl-none shadow-slate-100/50'">


                            <div class="leading-relaxed break-all select-text" v-html="message.content"></div>


                            <el-image v-if="message.type === messageType.IMAGE" :src="message.image"
                                class="size-24 rounded-lg shadow-xs overflow-hidden mt-1 cursor-zoom-in hover:brightness-95 transition-all"
                                :preview-src-list="[message.image]" :preview-teleported="true">
                            </el-image>

                            <audio v-else-if="message.type === messageType.AUDIO" :src="message.audio"
                                class="max-w-xs mt-1" controls>
                            </audio>

                            <video v-else-if="message.type === messageType.VIDEO" :src="message.video"
                                class="max-w-xs max-h-60 rounded-lg shadow-xs overflow-hidden mt-1" controls>
                            </video>
                        </div>
                        <div v-if="isSelf" class="flex items-end mr-2 text-xs select-none">
                            <!-- 1. 发送中状态 -->
                            <template v-if="props.message.sending">
                                <svg class="animate-spin size-4.5 text-blue-400 mb-1" xmlns="http://www.w3.org/2000/svg"
                                    fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                        stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor"
                                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                                    </path>
                                </svg>
                            </template>
                            <!-- 2. 发送失败状态（使用用户指定的感叹号 SVG） -->
                            <template v-else-if="props.message.failed">
                                <Tooltip placement="top" content="发送失败，点击重试" theme="glass">
                                    <svg @click="emit('resend', props.message)"
                                        class="size-5 mb-0.5 cursor-pointer active:scale-90 transition-transform"
                                        viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
                                        p-id="4949">
                                        <path
                                            d="M512 14.208c274.56 0 497.792 223.168 497.792 497.792 0 274.56-223.168 497.792-497.792 497.792C237.44 1009.792 14.208 786.56 14.208 512 14.208 237.44 237.44 14.208 512 14.208z m0 71.104A427.072 427.072 0 0 0 85.312 512 427.072 427.072 0 0 0 512 938.688 427.072 427.072 0 0 0 938.688 512 427.072 427.072 0 0 0 512 85.312z m35.584 628.16v71.104H476.416v-71.04h71.168z m0-474.048v402.944H476.416V239.424h71.168z"
                                            fill="#FF6E6E" p-id="4950"></path>
                                    </svg>
                                </Tooltip>
                            </template>
                            <!-- 3. 发送成功普通状态 -->
                            <template v-else>
                                <div class="flex flex-col items-center justify-end h-full">
                                    <span v-if="isLast" class="text-gray-400 group-hover:hidden mb-1">
                                        {{ props.message.status === messageStatus.READ ? '已读' : '未读' }}
                                    </span>
                                    <span
                                        class="hidden group-hover:inline-block text-gray-400 hover:text-red-500 cursor-pointer mb-1 transition-all duration-150"
                                        @click="emit('delete', props.message.id)">
                                        删除
                                    </span>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

            </div>



        </div>

    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted } from 'vue';
import { messageStatus, messageType, type messageVO } from '@/services/message/message';
import { useUserInfoStore } from '@/stores/userInfo';
import { TimeUtils } from '@/utils/time';
import Tooltip from '@/presentation/components/Tooltip.vue';
import { ref } from 'vue';
import router from '@/router';
import { useAuthStore } from '@/views/auth/store';

const props = defineProps<{
    message: messageVO;
    lastMsgTime?: string;
    isLast: boolean;
}>();


const emit = defineEmits<{
    (e: 'delete', messageId: string): void;
    (e: 'resend', message: messageVO): void;
}>();
const userStore = useUserInfoStore();
const authStore = useAuthStore();


const isSelf = computed(() => {
    return userStore.user && String(props.message.user.id) === String(userStore.user.id)
});


onMounted(() => {
    if (!userStore.user) {
        authStore.showLoginDialog();
        router.push('/');
    }
})
</script>