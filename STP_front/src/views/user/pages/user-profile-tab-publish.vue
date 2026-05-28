<template>
    <div class="relative w-full h-full ">
        <!-- 视频放大预览弹窗 -->
        <div v-if="videoDialogVisible"
            class="w-full flex flex-col justify-center fixed z-[9999] inset-0 bg-gray-500/50 pointer-events-auto">
            <div class="w-full flex justify-end items-center p-8">
                <div @click="videoDialogVisible = false"
                    class="flex text-white cursor-pointer font-semibold items-center justify-center w-12 h-12 rounded-full bg-linear-to-l from-blue-300 to-blue-200 shadow-md hover:scale-[1.05]">
                    关闭
                </div>
            </div>
            <div class="w-auto m-auto h-auto">
                <video :src="previewVideoUrl" controls autoplay class="max-w-full max-h-[55vh] object-contain"></video>
            </div>
        </div>

        <div v-if="topicList.length > 0" v-loading="loading"
            class="w-full h-full flex bg-gray-50 overflow-y-auto flex-col gap-4 p-4 scrollbar-thin">
            <div class="w-full bg-white rounded-xl p-5 shadow-sm hover:shadow-md transition-all duration-300 border border-gray-100 shrink-0 flex flex-col gap-3"
                v-for="(topic, index) in topicList" :key="index">
                <!-- 发布者信息 -->
                <div class="profile flex items-center gap-3">
                    <img class="w-11 h-11 rounded-full object-cover border border-gray-100"
                        :src="topic.publisher?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                        alt="avatar">
                    <div class="flex flex-col justify-center">
                        <span class="font-medium text-gray-800 text-sm">{{ topic.publisher?.nick || '匿名用户' }}</span>
                        <span class="text-gray-400 text-xs mt-0.5">
                            {{ TimeUtils.timestampToDate(topic.createTime) }}
                            <span class="mx-1">·</span>
                            IP: {{ topic.publisher?.ip || '未知' }}
                        </span>
                    </div>
                </div>

                <!-- 帖子内容区 -->
                <div class="content flex flex-col gap-2 pl-14">
                    <div class="title font-bold text-gray-900 text-base leading-snug">{{ topic.title }}</div>
                    <div :class="[expandedPosts[topic.id!] ? '' : 'line-clamp-3', 'content-text', 'text-gray-700', 'text-sm', 'leading-relaxed', 'whitespace-pre-wrap', 'break-all']">{{ topic.content }}</div>
                    <div v-if="shouldShowExpand(topic.content || '')" class="mt-0.5">
                        <span @click="toggleExpand(topic.id!)" class="text-blue-400 hover:text-blue-500 text-xs font-semibold cursor-pointer select-none">
                            {{ expandedPosts[topic.id!] ? '收起' : '展开' }}
                        </span>
                    </div>

                    <!-- 媒体图片展示 -->
                    <div v-if="topic.mediaUrls && topic.mediaUrls.length > 0"
                        class="content-media flex flex-wrap gap-2 mt-2">
                        <div v-for="(imageUrl, imgIdx) in topic.mediaUrls" :key="imgIdx"
                            class="overflow-hidden rounded-lg">
                            <el-image
                                class="w-32 h-32 rounded-lg object-cover shadow-xs hover:scale-[1.02] transition-transform duration-200 cursor-pointer"
                                :src="imageUrl.imageUrl" :preview-src-list="topic.mediaUrls.map(m => m.imageUrl)"
                                :initial-index="imgIdx" fit="cover" preview-teleported />
                        </div>
                    </div>

                    <!-- 其它媒体展示 -->
                    <div v-else-if="topic.type != PostType.TEXT && topic.type != PostType.IMAGE && topic.extraMediaUrl"
                        class="other-media mt-2">
                        <div v-if="topic.type == PostType.VIDEO"
                            class="video max-w-72 w-auto relative group cursor-pointer overflow-hidden rounded-lg  "
                            @click="openVideoPreview(topic.extraMediaUrl)" @mouseenter="onVideoMouseEnter"
                            @mouseleave="onVideoMouseLeave">
                            <video :src="topic.extraMediaUrl" class="w-auto max-h-64 object-contain rounded-lg"
                                preload="metadata" muted loop controlslist="nodownload"></video>
                        </div>
                        <audio v-if="topic.type == PostType.AUDIO" class="" :src="topic.extraMediaUrl" controls></audio>
                        <span v-if="topic.type == PostType.LINK" class="" :src="topic.extraMediaUrl"></span>
                        <div v-if="topic.type == PostType.FILE" class=""></div>
                    </div>
                </div>

                <!-- 底部互动栏 -->
                <div class="flex items-center gap-6 pl-14 mt-1 text-gray-400 text-sm">
                    <div class="flex items-center gap-1.5 hover:text-blue-500 transition-colors cursor-pointer">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                        </svg>
                        <span>{{ formatNum(topic.likeCount || 0) }}</span>
                    </div>
                    <div class="flex items-center gap-1.5 hover:text-blue-500 transition-colors cursor-pointer">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                        </svg>
                        <span>{{ formatNum(topic.replyCount || 0) }}</span>
                    </div>
                    <div class="flex items-center gap-1.5 hover:text-blue-500 transition-colors cursor-pointer">
                        <el-icon>
                            <Star />
                        </el-icon>
                        <span>{{ formatNum(topic.collectCount || 0) }}</span>
                    </div>
                    <div class="flex items-center gap-1.5 hover:text-blue-500 transition-colors cursor-pointer">
                        <el-icon>
                            <Share />
                        </el-icon>
                    </div>
                    <div class="w-12"></div>
                    <div class="flex items-center text-blue-300 gap-8">
                        <span class="hover:text-blue-600 cursor-pointer">置顶</span>
                        <span class="hover:text-blue-600 cursor-pointer"
                            @click="router.push({ name: 'post', query: { 'postId': topic.id } })">编辑</span>
                        <span v-if="topic.status !== PostStatus.DELETED" class="hover:text-blue-600 cursor-pointer"
                            @click="emit('delete', topic.id!)">删除</span>
                    </div>
                </div>
            </div>
        </div>
        <div v-else class="absolute inset-0 z-10 bg-white flex justify-center items-center">
            <div class="flex flex-col items-center gap-4 ">
                <span>你还没有发布过内容哦</span>
                <div
                    class="bg-linear-to-bl from-blue-400 via-blue-200 to-blue-300 px-2 rounded-md py-1 shadow-md hover:scale-105 transition-all cursor-pointer">
                    去发布
                </div>
            </div>
        </div>
    </div>

</template>

<script lang="ts" setup>
import { PostAPI, PostStatus, PostType, type PostVO } from '@/services/post';
import { TimeUtils } from '@/utils/time';
import { Share, Star } from '@element-plus/icons-vue';
import { onMounted, ref, watch } from 'vue';
import { formatNum } from '@/utils/page';
import router from '@/router';

const status = defineProps<{
    status: number
}>()
const emit = defineEmits(['delete']);
const loading = ref(false)
const previewVideoUrl = ref('')
const videoDialogVisible = ref(false)

function openVideoPreview(url: string) {
    previewVideoUrl.value = url
    videoDialogVisible.value = true
}

function onVideoMouseEnter(event: MouseEvent) {
    const container = event.currentTarget as HTMLElement
    const video = container.querySelector('video')
    if (video) {
        video.play().catch(err => {
            console.warn('视频播放被阻止或失败:', err)
        })
    }
}

function onVideoMouseLeave(event: MouseEvent) {
    const container = event.currentTarget as HTMLElement
    const video = container.querySelector('video')
    if (video) {
        video.pause()
    }
}

watch(() => status.status, (newStatus) => {
    loadData(newStatus)
})
onMounted(async () => {
    await loadData(status.status)
})
const topicList = ref<import('@/services/post').PostVO[]>([])
async function loadData(s: number) {
    loading.value = true
    try {
        topicList.value = (await PostAPI.getPage({ cursor: '', self: true, status: s })).data
    } catch (e) {
        console.error('加载数据失败:', e)
    } finally {
        loading.value = false
    }
}

const expandedPosts = ref<Record<string | number, boolean>>({})

function shouldShowExpand(content: string) {
    if (!content) return false
    return content.length > 150 || content.split('\n').length > 3
}

function toggleExpand(id: string | number) {
    expandedPosts.value[id] = !expandedPosts.value[id]
}

defineExpose({
    loadData
})
</script>