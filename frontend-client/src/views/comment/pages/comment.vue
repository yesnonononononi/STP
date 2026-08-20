<template>
    <div class="relative body flex flex-col gap-2 px-4 py-2" @mouseenter="moreSelect = true"
        @mouseleave="moreSelect = false">
        <div class="introduction flex items-center gap-2">
            <div class="avatar">
                <UserHoverCard :publisher="comment.comment.publisher" size="small" />
            </div>
            <div class="content flex flex-col gap-1 items-start justify-start">
                <div class="flex gap-2 items-center justify-center ">
                    <span>{{ comment.comment.publisher.nick }}</span>
                    <div v-if="comment.comment.publisher.memberLevelName"
                        class="flex gap-2 items-center justify-center">
                        <span class="flex bg-blue-200 rounded-xl">
                            <img :src="comment.comment.publisher.vipConfigIcon" alt="" class="w-4 h-4">
                            <span class="text-xs text-stone-600 pr-1">{{ comment.comment.publisher.memberLevelName
                                }}</span>
                        </span>
                    </div>
                    <div class="text-xs p-0.5 bg-gray-200  rounded-md text-gray-400"
                        v-if="comment.comment.publisher.followed && comment.comment.publisher.id !== useUserInfoStore().userId">
                        你的关注
                    </div>
                    <span v-if="acquireCommentTag()"
                        class="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold bg-gray-500/10 text-gray-500 shadow-sm border border-gray-200/30 transition-all duration-300">
                        {{ acquireCommentTag() }}
                    </span>
                    <div class="flex items-center gap-1"
                        v-if="comment.comment.parentId && comment.comment.parentId !== comment.comment.rootId">
                        <svg t="1780284657465" class="icon w-4 h-4" viewBox="0 0 1024 1024" version="1.1"
                            xmlns="http://www.w3.org/2000/svg" p-id="5124">
                            <path d="M950.857 512L73.143 1023.999V0z" fill="#bfbfbf" p-id="5125"></path>
                        </svg>
                        <span class="text-gray-400 text-sm">
                            {{ attemptAcquireRepliedComment(comment.comment.parentId, comment.comment.rootId!) }}</span>
                    </div>

                </div>
                <div class="text-xs text-gray-400 flex gap-2">
                    <span>{{ TimeUtils.timestampToDate(comment.comment.createTime) }}</span>
                    <span>{{ comment.comment.publisher.ip }}</span>

                </div>
            </div>

        </div>
        <div class="content flex flex-col gap-2">
            <div class="text-sm" v-html="parseEmoji(comment.comment.content)"> </div>
            <!-- 评论图片展示 -->
            <div v-if="checkCommentType(comment.comment, CommentType.IMAGE)"
                class="comment-images-grid mt-1 flex flex-wrap gap-2 w-full">
                <el-image v-for="(img, idx) in comment.comment.extra?.imageMoments || []" :key="idx" :src="img.imageUrl"
                    :preview-src-list="comment.comment.extra?.imageMoments?.map(i => i.imageUrl) || []"
                    :initial-index="idx" fit="cover" :preview-teleported="true"
                    class="rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                    :style="imageGridStyle(comment.comment.extra?.imageMoments?.length || 0)" />
            </div>
            <!-- 评论视频展示 -->
            <div v-else-if="checkCommentType(comment.comment, CommentType.VIDEO)"
                class="comment-video mt-1 max-w-64 relative group cursor-pointer overflow-hidden rounded-lg"
                @click="openVideoPreview(comment.comment.extra.mediaUrl || '')" @mouseenter="onVideoMouseEnter"
                @mouseleave="onVideoMouseLeave">
                <video :src="comment.comment.extra.mediaUrl"
                    class="w-auto max-w-full max-h-64 object-contain bg-black rounded-lg" preload="metadata" muted loop
                    controlsList="nodownload" />
            </div>
            <!-- 评论音频展示 -->
            <div v-else-if="checkCommentType(comment.comment, CommentType.AUDIO)" class="comment-audio mt-1 max-w-sm">
                <audio :src="comment.comment.extra.mediaUrl" controls class="w-full" />
            </div>
        </div>
        <div class="items flex gap-8 items-center">
            <div class="like flex items-center gap-2 text-md cursor-pointer" @click="comment.like(comment.comment)">
                <svg class="w-4 h-4" :fill="comment.comment.item.isLike ? '#f56c6c' : 'none'"
                    :stroke="comment.comment.item.isLike ? '#f56c6c' : 'currentColor'" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
                <span class="text-gray-500">{{ formatNum(comment.comment.item.likeCount) }}</span>
            </div>
            <div class="comment flex items-center gap-2 text-md">
                <span class="text-gray-500 cursor-pointer" @click="isReply = !isReply">回复</span>
            </div>
            <div class="shared flex items-center gap-2 text-md">
                <el-icon>
                    <Share />
                </el-icon>
                <span class="text-gray-500 cursor-pointer">分享</span>
            </div>
            <div class="top flex items-center  gap-2 text-md">
                <span class="text-gray-400 cursor-pointer" @click="comment.top(comment.comment)" v-if="canTop">{{
                    comment.comment.isTop ? '取消置顶' : '置顶' }}</span>
            </div>
            <div class=" flex items-center gap-2" v-if="moreSelect">
                <span class="text-red-400 cursor-pointer" @click="handleOpenReport">举报</span>

            </div>
        </div>

        <div class="reply-input w-full tranistion-all duration-300 relative z-41"
            :class="isReply ? 'h-12 p-4' : 'h-0 overflow-hidden'">
            <el-input ref="commentInputRef" :placeholder="`回复@${comment.comment.publisher.nick}`" v-model="userInput" class="w-full">
                <template #suffix>
                    <div class="relative cursor-pointer mr-4 ">
                        <Emoji v-model="userInput" :place-holder="''" :textarea-ref="commentInputRef" />
                    </div>
                    <span class="cursor-pointer hover:text-blue-300" v-if="!errMsg"
                        @click="reply(comment.comment)">回复</span>
                    <span v-else>
                        <span class="text-red-400">{{ errMsg }}</span>
                    </span>
                </template>

            </el-input>
        </div>
    </div>

    <!-- 举报弹窗 -->
    <CommentReportDialog
        v-model:visible="reportDialogVisible"
        :comment-id="comment.comment.id"
        :comment-content="comment.comment.content"
    />

    <!-- 视频放大预览弹窗 -->
    <Teleport to="body">
        <div v-if="videoDialogVisible" @click="videoDialogVisible = false"
            class="w-full flex flex-col justify-center fixed z-50 inset-0 bg-gray-500/50 pointer-events-auto">
            <div class="w-auto m-auto h-auto">
                <video :src="previewVideoUrl" controls autoplay class="max-w-full max-h-[55vh] object-contain"></video>
            </div>
        </div>
    </Teleport>
</template>

<script setup lang="ts">
import {formatNum} from '@/utils/page';
import {TimeUtils} from '@/utils/time';
import {type CommentVO, init} from '../types/comment';
import {checkCommentType, CommentType} from '../composables/useComment';
import {onMounted, ref, watch} from 'vue';
import {Share} from '@element-plus/icons-vue';
import type {PostVO} from '@/services/post';
import {useUserInfoStore} from '@/stores/userInfo';
import UserHoverCard from '@/presentation/components/UserHoverCard.vue';
import {parseEmoji} from '@/utils/emoji';
import Emoji from '@/presentation/components/emoji.vue';
import CommentReportDialog from '@/presentation/components/CommentReportDialog.vue';
import {useAuthStore} from '@/views/auth/store';
import {log} from '@/utils/log';

const reportDialogVisible = ref(false);

function handleOpenReport() {
    const authStore = useAuthStore();
    if (!authStore.token) {
        log.warning('请先登录后操作');
        authStore.showLoginDialog();
        return;
    }
    reportDialogVisible.value = true;
}

const errMsg = ref('');
const moreSelect = ref(false)
const isReply = ref(false)
const userInput = ref('')
const commentInputRef = ref();
const comment = defineProps<{
    comment: CommentVO,
    reply: (comment: CommentVO, content: string) => void,
    like: (comment: CommentVO) => void,
    attemptAcquireRepliedComment: (parentId: string, curId: string) => string
    post?: PostVO
    top: (comment: CommentVO) => void
}>()
const acquireCommentTag = () => {
    if (comment.comment.isTop == 1) {
        return '置顶'
    }
    if (comment.comment.postPublisherId === comment.comment.publisher.id) {
        return '作者'
    }

    if (comment.comment.item.authorIsPraised) {
        return '作者赞过'
    }
    else if (comment.comment.item.authorIsReplied) {
        return '作者回复过'
    }
}


onMounted(() => {
    init(comment.comment);
})

const canTop = comment.post && comment.post.creatorId === useUserInfoStore().user?.id && !comment.comment.rootId
watch(() => errMsg.value, (newVal) => {
    if (newVal) {
        setTimeout(() => {
            errMsg.value = ''
        }, 3000)
    }
})



function reply(target: CommentVO) {
    if (userInput.value) {
        comment.reply(target, userInput.value)
        userInput.value = ''
        isReply.value = false
    } else {
        errMsg.value = '请输入内容'
    }
}

const imageGridStyle = (count: number) => {
    if (count === 1) {
        return {
            width: '180px',
            height: '180px',
            maxWidth: '100%',
        }
    } else if (count === 2 || count === 4) {
        return {
            width: '110px',
            height: '110px',
        }
    } else {
        return {
            width: '80px',
            height: '80px',
        }
    }
}

const previewVideoUrl = ref('');
const videoDialogVisible = ref(false);

function openVideoPreview(url: string) {
    previewVideoUrl.value = url;
    videoDialogVisible.value = true;
}

function onVideoMouseEnter(event: MouseEvent) {
    const container = event.currentTarget as HTMLElement;
    const video = container.querySelector('video');
    if (video) {
        video.play().catch(err => {
            console.warn('视频播放被阻止或失败:', err);
        });
    }
}

function onVideoMouseLeave(event: MouseEvent) {
    const container = event.currentTarget as HTMLElement;
    const video = container.querySelector('video');
    if (video) {
        video.pause();
    }
}
</script>