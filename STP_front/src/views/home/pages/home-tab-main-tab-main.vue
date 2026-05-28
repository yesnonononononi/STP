<template>
  <div v-if="visible" class="fixed inset-0  w-full min-h-screen bg-gray-600/50 z-11">
    <div class="postInfo w-1/2 m-auto h-full ">"
      <postInfo :id=curPostId @close="curPostId = null; visible = false" :visible="visible" />
    </div>
  </div>

  <!-- 视频放大预览弹窗 -->

  <div v-if="videoDialogVisible"
    class="w-full flex flex-col justify-center fixed z-41 inset-0 bg-gray-500/50 pointer-events-auto"
    @click="videoDialogVisible = false">
    <div class="w-auto m-auto h-auto">
      <video :src="previewVideoUrl" controls autoplay class="max-w-full max-h-[55vh] object-contain"></video>
    </div>
  </div>


  <div class="container-comments min-h-400  overflow-y-auto w-full h-full flex flex-col">
    <div v-for="item in topicList" :key="item.id" class="p-8 pb-2 ">
      <div class="publisher flex items-center">
        <div class="avatar relative hover:z-50">
          <div class="group w-14 h-14 cursor-pointer">
            <img class="w-12 h-12 rounded-full   bg-gray-400" :src="item.publisher?.avatar" alt=""
              @click="router.push({ name: 'userProfile', params: { 'id': item.publisher?.id } })" />
            <div
              class="publisher-introduce bg-linear-to-tl max-h-64 from-gray-200 via-blue-100 to-white rounded-md shadow-md  opacity-0 pointer-events-none hover:opacity-100 hover:pointer-events-auto group-hover:pointer-events-auto group-hover:opacity-100 transition-opacity duration-400   absolute top-14 w-96 h-auto   z-10 ">
              <div class="flex flex-col h-auto items-center gap-2 ">
                <div class="introduce w-full flex flex-col ">
                  <div class="h-14 w-full bg-linear-to-br from-blue-400 via-blue-200 to-blue-300"></div>
                  <div class="flex w-full">
                    <div class="-translate-y-1/3 w-20 h-16 p-2">
                      <img class="w-18 m-auto h-18 rounded-full bg-gray-200" :src="item.publisher?.avatar" alt="" />
                    </div>
                    <div class="flex-2 detail">
                      <div class="grid grid-cols-3 gap-4">
                        <div class="fans flex flex-col p-2 items-center">
                          <span class="text-xs mb-2">粉丝</span>
                          <div class="text-md">{{ item.publisher?.fans || 0 }}</div>
                        </div>
                        <div class="topic">
                          <div class="topic flex flex-col p-2 items-center">
                            <span class="text-xs mb-2">帖子</span>
                            <div class="text-md">{{ item.publisher?.topic || 0 }}</div>
                          </div>
                        </div>
                        <div class="liked">
                          <div class="liked flex flex-col p-2 items-center">
                            <span class="text-xs mb-2">获赞</span>
                            <div class="text-md">{{ item.publisher?.liked || 0 }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="mid w-full pl-5 flex gap-2">
                  <span class="truncate max-w-48 block">{{ item.publisher?.nick }}</span>
                  <div
                    class="tag w-auto px-1.5 h-6 rounded-md text-xs bg-linear-to-l from-blue-200 via-blue-100 to-blue-300 flex items-center justify-center gap-1">
                    <img class='w-4 h-4' :src="item.publisher?.vipConfigIcon || '/v1.png'" alt="">
                    <span class="text-xs  text-stone-600">{{ item.publisher?.memberLevelName }}</span>
                  </div>
                  <span
                    class="bg-linear-to-br from-yellow-300 text-xs via-yellow-100 to-yellow-400 p-1 text-stone-500 flex items-center justify-center h-6   rounded-md">{{
                      item.publisher?.vipType }}
                  </span>
                </div>
                <div class="habbit ml-8 w-full h-6   ">
                  <div class=" w-auto h-6 text-xs flex items-center gap-3 justify-start">
                    <span class="bg-gray-200 px-1.5 py-0.5 rounded-sm">{{ item.publisher?.ip }}</span>
                    <span class="bg-gray-200 px-1.5 py-0.5 rounded-sm" v-if="item.publisher?.age">
                      {{ new Date().getFullYear() - item.publisher.age }}
                    </span>
                  </div>
                </div>
                <div class="operation w-full pl-5 h-12 px-8">
                  <div class="flex items-center justify-between gap-2" v-if="item.publisher!.id != me!.id.toString()">
                    <button v-if="!item.publisher?.followed"
                      class="flex-1 text-center bg-linear-to-l from-blue-300 to-blue-200 shadow-md hover:scale-[1.05]">关注</button>
                    <button
                      class="flex-1 text-center bg-linear-to-l from-blue-300 to-blue-200 shadow-md hover:scale-[1.05]">私信</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>

        <div class="nick h-12">
          <span class="text-md truncate max-w-72 block">{{ item.publisher?.nick }}</span>
          <div class="h-[50%] mx-2">
            <span class="text-xs mr-2 text-gray-400">{{ TimeUtils.timestampToDate(item.createTime) }}</span>
            <span class="text-xs text-gray-500">{{ item.publisher?.ip }}</span>
          </div>
        </div>
      </div>
      <div class="content  pl-0">
        <p class="text-ls font-bold">{{ item.title }}</p>
        <p :class="[expandedPosts[item.id!] ? '' : 'line-clamp-3', 'text-gray-700', 'whitespace-pre-wrap', 'break-all']">{{ item.content }}</p>
        <div v-if="shouldShowExpand(item.content || '')" class="mt-1">
          <span @click="toggleExpand(item.id!)" class="text-blue-400 hover:text-blue-500 text-xs font-semibold cursor-pointer select-none">
            {{ expandedPosts[item.id!] ? '收起' : '展开' }}
          </span>
        </div>
        <div class="tags flex items-center">
          <span v-for="(tag, index) in item.tags" :key="tag.sort" class="text-blue-400">
            #{{ tag.tagName }}
          </span>
        </div>
        <div class="extra mt-3 max-h-64 flex items-center gap-2">
          <div v-if="item.type == PostType.IMAGE" class="img flex flex-wrap gap-2 w-full">
            <el-image v-for="(imgUrl, index) in getPostImages(item)" :key="index"
              class="h-36 w-36 rounded-lg object-cover shadow-sm transition-transform duration-200 hover:scale-[1.02] cursor-pointer"
              :src="imgUrl" :preview-src-list="getPostImages(item)" :initial-index="index" fit="cover"
              preview-teleported>
              <template #placeholder>
                <div
                  class="image-slot flex items-center justify-center bg-gray-100 h-full w-full text-gray-400 rounded-lg">
                  加载中...
                </div>
              </template>
              <template #error>
                <div
                  class="image-slot flex flex-col items-center justify-center bg-gray-100 h-full w-full text-gray-400 rounded-lg border border-dashed border-gray-300">
                  <el-icon class="text-xl">
                    <Picture />
                  </el-icon>
                  <span class="text-xs mt-1">图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
          <div v-if="item.type == PostType.VIDEO"
            class="video max-w-72 relative group cursor-pointer overflow-hidden rounded-lg shadow-sm"
            @click="openVideoPreview(getPostVideo(item) || 'https://www.w3schools.com/html/mov_bbb.mp4')"
            @mouseenter="onVideoMouseEnter" @mouseleave="onVideoMouseLeave">
            <video :src="getPostVideo(item) || 'https://www.w3schools.com/html/mov_bbb.mp4'"
              class="w-full max-h-64 object-contain bg-black rounded-lg" preload="metadata" muted loop
              controlsList="nodownload"></video>
          </div>
        </div>
      </div>
      <div class="items mt-4 w-72 h-4 grid grid-cols-4 gap-1">
        <div class="like flex items-center gap-1 hover:text-blue-300 cursor-pointer" @click="like(item.id)">
          <svg class="w-4 h-4" :fill="item.isLike ? '#f56c6c' : 'none'"
            :stroke="item.isLike ? '#f56c6c' : 'currentColor'" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
          </svg>
          <span class="like text-md" :class="item.isLike ? 'text-red-500' : 'text-gray-500'">{{ formatNum(item.likeCount
            || 0)
          }}</span>
        </div>
        <div class="comment flex items-center gap-1 hover:text-blue-300 cursor-pointer"
          @click="curPostId = Number(item.id); visible = true">
          <el-icon>
            <ChatDotRound />
          </el-icon>
          <span class="text-md text-gray-500">{{ formatNum(item.replyCount || 0) }}</span>
        </div>
        <div class="collect flex items-center gap-1 hover:text-blue-300 cursor-pointer" @click="collect(item.id)">
          <el-icon :color="item.isCollect ? '#f7ba2a' : ''">
            <Star />
          </el-icon>
          <span class="text-md" :class="item.isCollect ? 'text-amber-500' : 'text-gray-500'">{{
            formatNum(item.collectCount ||
              0) }}</span>
        </div>
        <div class="shared flex items-center gap-1 hover:text-blue-300 cursor-pointer">
          <el-icon>
            <Share />
          </el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import { Star, ChatDotRound, Share, Picture, VideoPlay } from '@element-plus/icons-vue'

import { PostAPI, PostType, type PostVO } from '@/services/post'
import { parseMediaUrls } from '@/utils/post'
import router from '@/router'
import postInfo from '@/views/post/pages/postInfo.vue'
import { TimeUtils } from '@/utils/time'
import { useUserInfoStore } from '@/stores/userInfo'
import { formatNum } from '@/utils/page'
const cursor = ref("");
const props = defineProps<{
  tabId?: string
}>()

const curPostId = ref<number | null>(null)
const visible = ref(false)
const topicList = ref<PostVO[]>([])
const me = useUserInfoStore().user;
const previewVideoUrl = ref('')
const videoDialogVisible = ref(false)

function openVideoPreview(url: string) {
  previewVideoUrl.value = url
  videoDialogVisible.value = true
}

function getPostImages(item: PostVO): string[] {
  if (Array.isArray(item.mediaUrls)) {
    return item.mediaUrls.map((m) => m.imageUrl).filter(Boolean)
  }
  return parseMediaUrls(item.extraMediaUrl)
}

function getPostVideo(item: PostVO): string {
  return item.extraMediaUrl || ''
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
async function like(topicId: string | undefined | number) {
  if (!topicId) return;
  const res = await PostAPI.like(Number(topicId));
  if (res.code === 1) {
    const topic = topicList.value.find(t => t.id == topicId)
    if (!topic) return;
    if (topic.isLike) {
      topic.likeCount = Math.max(0, Number(topic.likeCount || 0) - 1);
      topic.isLike = false;
    } else {
      topic.likeCount = Number(topic.likeCount || 0) + 1;
      topic.isLike = true;
    }
  }
}
async function collect(topicId: string | undefined | number) {
  if (!topicId) return;
  const res = await PostAPI.collect(Number(topicId));
  if (res.code === 1) {
    const topic = topicList.value.find(t => t.id == topicId)
    if (!topic) return;
    if (topic.isCollect) {
      topic.collectCount = Math.max(0, Number(topic.collectCount || 0) - 1);
      topic.isCollect = false;
    } else {
      topic.collectCount = Number(topic.collectCount || 0) + 1;
      topic.isCollect = true;
    }
  }
}
async function loadData() {
  try {
    const res = await PostAPI.getPage({ cursor: cursor.value, self: false });
    topicList.value = res.data;
    cursor.value = topicList.value[topicList.value.length - 1]?.createTime?.toString() || ""
  } catch (e) {
    console.error('加载动态列表异常: ', e);
  }
}

watch(() => props.tabId, () => {
  loadData();
})

const expandedPosts = ref<Record<string | number, boolean>>({})

function shouldShowExpand(content: string) {
  if (!content) return false
  return content.length > 150 || content.split('\n').length > 3
}

function toggleExpand(id: string | number) {
  expandedPosts.value[id] = !expandedPosts.value[id]
}

onMounted(() => {
  loadData();
})
</script>
