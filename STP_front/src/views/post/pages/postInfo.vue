<template>
    <div class="w-full relative flex flex-col shadow-md overflow-x-visible z-14 mb-1 p-4 pt-0 max-h-screen overflow-y-auto bg-white"
        v-if="postInfo">
        <div class="postInfo w-full flex flex-col ">
            <div class="tab-post sticky z-41 bg-white top-0 w-full p-2 flex justify-between">
                <div class="introduction flex items-center justify-between">
                    <div class="flex items-center gap-2 p-2">
                        <div class="">
                            <img class="w-12 h-12 rounded-full" :src="postInfo.publisher?.avatar" alt=""
                                referrerpolicy="no-referrer">
                        </div>
                        <div class="flex flex-col items-start gap-2">
                            <div class="text-lg font-semibold">{{ postInfo?.publisher?.nick }}</div>
                            <div class="text-sm text-gray-500 flex gap-2 items-center">
                                <span>{{ TimeUtils.timestampToDate(postInfo.createTime) }}</span>
                                <span>{{ postInfo.publisher?.ip }}</span>
                                <span v-if="isOwner" class="text-blue-500 hover:text-blue-700 font-medium cursor-pointer text-xs ml-2 select-none" @click="showSettingsModal = true">设置</span>
                            </div>
                        </div>
                    </div>
                    <div></div>
                </div>
                <div class="w-8 h-8 cursor-pointer" @click="emit('close')">
                    <svg t="1780145570300" class="icon" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="5629">
                        <path
                            d="M512 466.944l233.472-233.472a31.744 31.744 0 0 1 45.056 45.056L557.056 512l233.472 233.472a31.744 31.744 0 0 1-45.056 45.056L512 557.056l-233.472 233.472a31.744 31.744 0 0 1-45.056-45.056L466.944 512 233.472 278.528a31.744 31.744 0 0 1 45.056-45.056z"
                            fill="#5A5A68" p-id="5630"></path>
                    </svg>
                </div>
            </div>

            <div class="content px-4   w-full">
                <div class="title font-semibold">{{ postInfo?.title }}</div>
                <div class="content flex flex-col gap-2">
                    <div class="text flex flex-wrap whitespace-pre-wrap break-all">
                        {{ postInfo?.content }}
                    </div>
                    <div v-if="postInfo.type === PostType.IMAGE" class="img flex flex-wrap gap-2 w-full">
                        <div v-for="item in postInfo.mediaUrls" :key="item.id">
                            <el-image class="w-45 h-45 object-contain" :src="item.imageUrl" alt="" />
                        </div>
                    </div>
                    <div v-if="postInfo.type !== PostType.TEXT && postInfo.type !== PostType.IMAGE" class="extraMedia">
                        <video v-if="postInfo.type === PostType.VIDEO" class="w-72 h-45 object-contain"
                            :src="postInfo.extraMediaUrl || ''" controls></video>
                        <audio v-if="postInfo.type === PostType.AUDIO" class="w-72 h-45 object-contain"
                            :src="postInfo.extraMediaUrl || ''" controls></audio>
                    </div>
                </div>
                <div class="tag"></div>
            </div>
            <div class="items flex items-center justify-between p-2 w-full pr-4">
                <div class="item flex items-center gap-8 ">
                    <div class="comment flex items-center gap-2 text-md">
                        <el-icon>
                            <ChatDotRound />
                        </el-icon>
                        <span>{{ formatNum(comments.length) }}</span>
                    </div>

                    <div class="like flex items-center gap-2 text-md hover:text-blue-500 cursor-pointer" @click="handleLikePost">
                        <svg class="w-4 h-4" :fill="postInfo?.isLike ? '#1890ff' : '#8a8a8a'" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
                            <path d="M64 483.04V872c0 37.216 30.144 67.36 67.36 67.36H192V416.32l-60.64-0.64A67.36 67.36 0 0 0 64 483.04zM857.28 344.992l-267.808 1.696c12.576-44.256 18.944-83.584 18.944-118.208 0-78.56-68.832-155.488-137.568-145.504-60.608 8.8-67.264 61.184-67.264 126.816v59.264c0 76.064-63.84 140.864-137.856 148L256 416.96v522.4h527.552a102.72 102.72 0 0 0 100.928-83.584l73.728-388.96a102.72 102.72 0 0 0-100.928-121.824z" />
                        </svg>
                        <span :class="postInfo?.isLike ? 'text-blue-500' : 'text-gray-500'">{{ formatNum(postInfo?.likeCount || 0) }}</span>
                    </div>

                    <div class="collect flex items-center gap-2 text-md hover:text-amber-500 cursor-pointer" @click="handleCollectPost">
                        <el-icon :color="postInfo?.isCollect ? '#f7ba2a' : ''">
                            <Star />
                        </el-icon>
                        <span :class="postInfo?.isCollect ? 'text-amber-500' : 'text-gray-500'">{{ formatNum(postInfo?.collectCount || 0) }}</span>
                    </div>

                    <div class="shared flex items-center gap-2 text-md hover:text-blue-500 cursor-pointer" @click="handleShare">
                        <el-icon>
                            <Share />
                        </el-icon>
                    </div>
                </div>
                <div class="viewCount text-gray-500 text-sm ">
                    浏览 {{ formatNum(postInfo?.viewCount || 0) }}
                </div>
            </div>
        </div>

        <CommentInput v-model="replyInput" :publish="publish" />

        <div v-if="comments.length > 0" class="reply-area overscroll-contain  w-full flex flex-col   z-14 mb-1">
            <div class="comment-area flex flex-col p-2 gap-2 mt-2">
                <div class="title flex justify-between items-center w-full">
                    <span class="text-xl font-bold">全部评论</span>
                    <span></span>
                </div>
                <div class="body flex flex-col max-h-144 overflow-auto no-scrollbar gap-2 p-4" ref="commentArea">
                    <div v-for="comment in comments" :key="comment.id" class="w-full h-full">
                        <Comment :comment="comment" :reply="reply" :like="like" :post="post" :top="top"
                            :attempt-acquire-replied-comment="attemptAcquireRepliedComment" />
                        <div class="reply-area w-full flex flex-col items-center">
                            <!-- 未展开且有回复时显示展开按钮 -->
                            <div v-if="comment.FunctionField?.loading && !comment.FunctionField?.expandMore"
                                class="flex items-center gap-2 text-sm text-gray-400 py-1">
                                <svg class="animate-spin h-4 w-4 text-gray-400" xmlns="http://www.w3.org/2000/svg"
                                    fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                        stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor"
                                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                                    </path>
                                </svg>
                                <span>正在加载回复...</span>
                            </div>
                            <span v-else-if="expendMore(comment)"
                                class="text-gray-500 flex w-1/3 m-auto items-center text-center text-sm cursor-pointer py-1 hover:text-blue-500"
                                @click="comment.FunctionField!.expandMore = true; loadData(true, comment);" @click.stop>
                                展开共{{ comment.item.replyCount }}条回复
                                <svg t="1780317169159" class="icon w-4 h-4" viewBox="0 0 1024 1024" version="1.1"
                                    xmlns="http://www.w3.org/2000/svg" p-id="5630">
                                    <path
                                        d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z"
                                        p-id="5631" fill="#bfbfbf"></path>
                                </svg>
                            </span>
                        </div>
                        <Motion layout :initial="{ y: -10, height: 0, opacity: 0 }" :animate="{
                            y: 0,
                            height: comment.FunctionField?.expandMore ? 'auto' : 0,
                            opacity: comment.FunctionField?.expandMore ? 1 : 0
                        }" :transition="{
                            ease: 'easeInOut',
                            duration: 0.3
                        }" class="relative  overflow-hidden pl-12 w-full bg-gray-100 origin-top"
                            v-for="item in comment.FunctionField?.replys.slice(0, comment.FunctionField.showCount || 2)" :key="item.id">

                            <Comment :comment="item" :reply="reply" :top="top"
                                :attempt-acquire-replied-comment="attemptAcquireRepliedComment" :like="like"
                                :post="post" />

                        </Motion>
                        <div v-if="comment.FunctionField?.expandMore"
                            class="w-full flex flex-col items-center gap-1 mt-2 mb-4">
                            <!-- 加载回复时的动画提示 -->
                            <div v-if="comment.FunctionField?.loading"
                                class="flex items-center gap-2 text-xs text-gray-400 py-1">
                                <svg class="animate-spin h-3.5 w-3.5 text-gray-400" xmlns="http://www.w3.org/2000/svg"
                                    fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                        stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor"
                                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                                    </path>
                                </svg>
                                <span>正在加载回复...</span>
                            </div>
                            <template v-else>
                                <!-- 如果已展示的回复数小于总回复数，显示“查看更多回复” -->
                                <span v-if="(comment.FunctionField.showCount || 2) < (comment.item.replyCount || 0)"
                                    class="text-gray-400 text-xs cursor-pointer hover:text-blue-500 py-1"
                                    @click="handleLoadMoreReplies(comment)" @click.stop>
                                    查看更多回复 ({{ Math.min(comment.FunctionField.showCount || 2, comment.item.replyCount || 0) }}/{{ comment.item.replyCount }})
                                </span>

                                <!-- 收起回复 -->
                                <span class="text-gray-400 text-xs cursor-pointer hover:text-blue-500 py-1"
                                    @click="comment.FunctionField!.expandMore = false; comment.FunctionField!.showCount = 2" @click.stop>
                                    收起回复
                                </span>
                            </template>
                        </div>
                    </div>

                    <!-- 底部 Loading / 到底提示 -->
                    <div class="flex justify-center items-center py-4 w-full text-gray-400 text-sm">
                        <div v-if="loading" class="flex items-center gap-2">
                            <svg class="animate-spin h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg"
                                fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                    stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor"
                                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                                </path>
                            </svg>
                            <span>正在加载...</span>
                        </div>
                        <div v-else-if="!state.hasMore && comments.length > 0" class="text-gray-300 tracking-wider">
                            — 暂无更多评论 —
                        </div>
                    </div>

                </div>
            </div>
        </div>
        <div v-else class="flex-1 flex items-center justify-center">
            <span>这里空空如也~~</span>
        </div>

        <PostVisibilitySettings
            v-if="postInfo"
            :visible="showSettingsModal"
            :post="postInfo"
            @close="showSettingsModal = false"
            @update-scope="postInfo.visibleScope = $event"
        />

        <!-- 分享 Toast 提示 -->
        <Teleport to="body">
            <div v-if="showShareTip" class="fixed inset-0 z-50 flex items-center justify-center pointer-events-none">
                <div class="bg-gray-800/95 text-white rounded-md p-4 shadow-lg text-sm font-medium">
                    分享链接已复制到粘贴板
                </div>
            </div>
        </Teleport>
    </div>
</template>
<script setup lang="ts">
import { PostAPI, PostType, type PostVO } from '@/services/post';
import { useUserInfoStore } from '@/stores/userInfo';
import { Motion } from 'motion-v';
import { formatNum } from '@/utils/page';
import { TimeUtils } from '@/utils/time';
import { ChatDotRound, Share, Star } from '@element-plus/icons-vue';
import { onMounted, onUnmounted, ref, watch, computed } from 'vue';
import Comment from '../../comment/pages/comment.vue';
import type { CommentVO } from '@/views/comment/types/comment';
import { scrollerFromBottom } from '@/utils/scollerbar.ts';
import { useComment } from '@/views/comment/composables/useComment';
import CommentInput from '@/presentation/components/CommentInput.vue';
import PostVisibilitySettings from '@/views/post/components/PostVisibilitySettings.vue';

const commentArea = ref<HTMLDivElement>();
const user = useUserInfoStore().user;

const showSettingsModal = ref(false);
const showShareTip = ref(false);

const isOwner = computed(() => {
    return postInfo.value && user && postInfo.value.creatorId?.toString() === user.id?.toString();
});

const handleShare = async () => {
    if (!postInfo.value) return;
    const title = postInfo.value.title || '';
    const truncatedTitle = title.length > 30 ? title.substring(0, 30) + '...' : title;
    const shareUrl = `${window.location.origin}/home/homeFeed?postId=${postInfo.value.id}`;
    const shareText = `${truncatedTitle} ${shareUrl}`;

    try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(shareText);
        } else {
            const textarea = document.createElement('textarea');
            textarea.value = shareText;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
        }
        showShareTip.value = true;
        setTimeout(() => {
            showShareTip.value = false;
        }, 2000);
    } catch (err) {
        console.error('Failed to copy share link:', err);
    }
};
const props = defineProps<{
    visible: boolean,
    post: PostVO
}>();
const emit = defineEmits<{
    close: []
}>();
const postInfo = ref<PostVO>();

// 提取 postId 作为 Ref 传入 useComment 
const postIdRef = computed(() => props.post?.id);
const {
    comments,
    loading,
    replyInput,
    state,
    like,
    top,
    loadData,
    reply,
    expendMore,
    publish,
    attemptAcquireRepliedComment
} = useComment(postIdRef);

let removeListener: (() => void) | null = null;

// 使用 watch 监听 commentArea 的 DOM 挂载状态
watch(commentArea, (newEl) => {
    if (removeListener) {
        removeListener();
        removeListener = null;
    }
    if (newEl) {
        removeListener = scrollerFromBottom(async () => {
            await loadData(false)
        }, newEl)
    }
})

onMounted(async () => {
    if (!props.post) {
        return;
    }
    postInfo.value = props.post;
    await handleView(props.post.id || '');
})

watch(() => props.visible, (visible) => {
    if (visible) {
        document.body.style.overflow = 'hidden';
    } else {
        document.body.style.overflow = '';
    }
}, { immediate: true });

async function handleView(id: number | string) {
    if (!id || !props.post) return;
    await PostAPI.view(id.toString())
    props.post.viewCount = (Number(props.post.viewCount) || 0) + 1;
}

async function handleLikePost() {
    if (!postInfo.value || !postInfo.value.id) return;
    const topicId = postInfo.value.id;
    const res = await PostAPI.like(topicId.toString());
    if (res.code === 1) {
        if (postInfo.value.isLike) {
            postInfo.value.likeCount = Math.max(0, Number(postInfo.value.likeCount || 0) - 1);
            postInfo.value.isLike = false;
        } else {
            postInfo.value.likeCount = Number(postInfo.value.likeCount || 0) + 1;
            postInfo.value.isLike = true;
        }
        props.post.isLike = postInfo.value.isLike;
        props.post.likeCount = postInfo.value.likeCount;
    }
}

async function handleCollectPost() {
    if (!postInfo.value || !postInfo.value.id) return;
    const topicId = postInfo.value.id;
    const res = await PostAPI.collect(topicId.toString());
    if (res.code === 1) {
        if (postInfo.value.isCollect) {
            postInfo.value.collectCount = Math.max(0, Number(postInfo.value.collectCount || 0) - 1);
            postInfo.value.isCollect = false;
        } else {
            postInfo.value.collectCount = Number(postInfo.value.collectCount || 0) + 1;
            postInfo.value.isCollect = true;
        }
        props.post.isCollect = postInfo.value.isCollect;
        props.post.collectCount = postInfo.value.collectCount;
    }
}

onUnmounted(() => {
    document.body.style.overflow = '';
    if (removeListener) {
        removeListener();
    }
});

function handleLoadMoreReplies(comment: CommentVO) {
    if (!comment.FunctionField) return;
    comment.FunctionField.showCount = (comment.FunctionField.showCount || 2) + 5;
    if (comment.FunctionField.showCount > comment.FunctionField.replys.length && comment.FunctionField.hasMore) {
        loadData(true, comment);
    }
}
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
    display: none;
}

.no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>