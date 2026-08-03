<template>
  <div :class="[
    isCard
      ? 'w-full bg-white rounded-xl p-5 shadow-sm hover:shadow-md transition-all duration-300 border border-gray-100 shrink-0 flex flex-col gap-2'
      : 'p-8 pb-2 border-b border-gray-100 flex flex-col gap-2'
  ]">
    <!-- 发布者信息 -->
    <div class="publisher flex items-center gap-3">
      <UserHoverCard :publisher="post.publisher" />

      <div class="nick h-12 flex flex-col justify-center">
        <span class="text-md font-medium text-gray-800 truncate max-w-72 block">
          {{ post.publisher?.nick || '匿名用户' }}
        </span>
        <div class="h-[50%] flex items-center mt-0.5 text-xs text-gray-400">
          <span>{{ TimeUtils.timestampToDate(post.createTime) }}</span>
          <span class="mx-1">·</span>
          <span>IP: {{ post.publisher?.ip || '未知' }}</span>
        </div>
      </div>
    </div>

    <!-- 帖子内容区 -->
    <div :class="[isCard ? 'pl-14' : 'pl-0', 'content flex flex-col gap-2']">
      <div class="title font-bold text-gray-900 text-base leading-snug flex items-center">
        <div v-if="props.self">
          <el-tag v-if="post.isTop === 1 && !props.hideTop" size="small" type="danger" effect="dark" class="mr-1">置顶</el-tag>
          <el-tag v-if="self && Number(post.status) === PostStatus.DRAFT" size="small" type="info" effect="dark"
            class="mr-1 bg-amber-500 border-amber-500">草稿</el-tag>
          <el-tag v-if="self && Number(post.status) === PostStatus.DELETED" size="small" type="info" effect="dark"
            class="mr-1 bg-gray-500 border-gray-500">已删除</el-tag>
          <el-tag v-if="self && Number(post.status) === PostStatus.BLOCKED" size="small" type="danger" effect="dark"
            class="mr-1">禁用</el-tag>
          <el-tag v-if="self && Number(post.status) === PostStatus.REPORTED" size="small" type="warning" effect="dark"
            class="mr-1">被举报</el-tag>
        </div>

        {{ post.title }}
      </div>
      <p :class="[
        isExpanded ? '' : 'line-clamp-3',
        'text-gray-700',
        'text-sm',
        'leading-relaxed',
        'whitespace-pre-wrap',
        'break-all'
      ]" @click="handleTextClick" v-html="parseEmoji(parseTag(post.content || '', post.tags))">
      </p>
      <div v-if="shouldShowExpand(post.content || '')" class="mt-0.5">
        <span @click="toggleExpand"
          class="text-blue-400 hover:text-blue-500 text-xs font-semibold cursor-pointer select-none">
          {{ isExpanded ? '收起' : '展开' }}
        </span>
      </div>

      <!-- 媒体展示 -->
      <div class="extra mt-2">
        <!-- 图片类型 -->
        <div v-if="isImageType" class="img flex flex-wrap  gap-2 w-full">
          <el-image v-for="(imgUrl, index) in getPostImages(post)" :key="index" :class="[
            isCard ? 'max-w-32 max-h-32' : 'max-w-36 max-h-36',
            'rounded-lg object-cover   transition-transform duration-200 hover:scale-[1.02] cursor-pointer'
          ]" class="w-auto h-auto" :src="imgUrl" :preview-src-list="getPostImages(post)" :initial-index="index"
            fit="cover" preview-teleported>
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

        <!-- 视频类型 -->
        <div v-else-if="isVideoType" class="video max-w-72 relative group cursor-pointer overflow-hidden rounded-lg  "
          @click="openVideoPreview(getPostVideo(post) || 'https://www.w3schools.com/html/mov_bbb.mp4')"
          @mouseenter="onVideoMouseEnter" @mouseleave="onVideoMouseLeave">
          <video :src="getPostVideo(post) || 'https://www.w3schools.com/html/mov_bbb.mp4'"
            class="w-auto max-w-full max-h-64 object-contain bg-black rounded-lg" preload="metadata" muted loop
            controlsList="nodownload"></video>
        </div>

        <!-- 其他媒体类型 (音频/链接/文件) -->
        <div v-else-if="post.type !== PostType.TEXT && hasExtraMediaUrl" class="other-media">
          <audio v-if="isAudioType" :src="post.extraMediaUrl || ''" controls class="max-w-full"></audio>
          <span v-if="isLinkType" :src="post.extraMediaUrl || ''"></span>
          <div v-if="isFileType"></div>
        </div>
      </div>
    </div>

    <!-- 底部互动栏 -->
    <div :class="[
      isCard
        ? 'flex items-center gap-6 pl-14 mt-1 text-gray-400 text-sm'
        : 'items mt-4 w-72 h-4 grid grid-cols-4 gap-1 text-gray-400 text-sm'
    ]">
      <!-- 点赞 -->
      <div class="flex items-center gap-1.5 transition-colors"
        :class="[!self ? 'hover:text-blue-500 cursor-pointer' : 'cursor-default']"
        @click="!self && emit('like', post.id)">
        <svg class="w-4 h-4" :fill="post.isLike ? '#1890ff' : '#8a8a8a'" viewBox="0 0 1024 1024" version="1.1"
          xmlns="http://www.w3.org/2000/svg">
          <path
            d="M64 483.04V872c0 37.216 30.144 67.36 67.36 67.36H192V416.32l-60.64-0.64A67.36 67.36 0 0 0 64 483.04zM857.28 344.992l-267.808 1.696c12.576-44.256 18.944-83.584 18.944-118.208 0-78.56-68.832-155.488-137.568-145.504-60.608 8.8-67.264 61.184-67.264 126.816v59.264c0 76.064-63.84 140.864-137.856 148L256 416.96v522.4h527.552a102.72 102.72 0 0 0 100.928-83.584l73.728-388.96a102.72 102.72 0 0 0-100.928-121.824z" />
        </svg>
        <span :class="post.isLike ? 'text-blue-500' : 'text-gray-500'">{{ formatNum(post.likeCount || 0) }}</span>
      </div>

      <!-- 评论/详情 -->
      <div class="flex items-center gap-1.5 hover:text-blue-500 transition-colors cursor-pointer"
        @click="emit('comment-click', post)">
        <el-icon v-if="isCard">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </el-icon>
        <el-icon v-else>
          <ChatDotRound />
        </el-icon>
        <span class="text-gray-500">{{ formatNum(post.replyCount || 0) }}</span>
      </div>

      <!-- 收藏 -->
      <div class="flex items-center gap-1.5 transition-colors"
        :class="[!self ? 'hover:text-blue-500 cursor-pointer' : 'cursor-default']"
        @click="!self && emit('collect', post.id)">
        <el-icon :color="post.isCollect ? '#f7ba2a' : ''">
          <Star />
        </el-icon>
        <span :class="post.isCollect ? 'text-amber-500' : 'text-gray-500'">{{ formatNum(post.collectCount || 0)
          }}</span>
      </div>

      <!-- 分享 -->
      <div class="flex items-center gap-1.5 hover:text-blue-500 transition-colors cursor-pointer" @click="handleShare">
        <el-icon>
          <Share />
        </el-icon>
      </div>

      <!-- 管理员专有操作 -->
      <div v-if="self && isOwner && isCard" class="flex items-center text-blue-300 gap-8 ml-auto">
        <span class="hover:text-red-600 text-red-500 cursor-pointer text-xs" @click="emit('delete', post.id)"
          v-if="post.status !== PostStatus.DELETED">
          删除
        </span>
        <span class="hover:text-blue-600 cursor-pointer text-xs" @click="emit('top', post)">
          {{ post.isTop === 1 ? '取消置顶' : '置顶' }}
        </span>
        <span class="hover:text-blue-600 cursor-pointer text-xs"
          @click="router.push({ name: 'post', query: { 'postId': post.id } })">编辑</span>
        <span v-if="Number(post.status) === PostStatus.NORMAL"
          class="hover:text-blue-600 text-blue-500 font-medium cursor-pointer text-xs"
          @click="showSettingsModal = true">设置</span>
      </div>
    </div>

    <!-- 视频放大预览弹窗 -->
    <Teleport to="body">
      <div v-if="videoDialogVisible" @click="videoDialogVisible = false"
        class="w-full flex flex-col justify-center fixed z-50 inset-0 bg-gray-500/50 pointer-events-auto">
        <div class="w-auto m-auto h-auto">
          <video :src="previewVideoUrl" controls autoplay class="max-w-full max-h-[55vh] object-contain"></video>
        </div>
      </div>
    </Teleport>

    <PostVisibilitySettings :visible="showSettingsModal" :post="props.post" @close="showSettingsModal = false"
      @update-scope="props.post.visibleScope = $event" />

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
import {computed, ref} from 'vue'
import {ChatDotRound, Picture, Share, Star} from '@element-plus/icons-vue'
import {parseEmoji} from '@/utils/emoji'
import PostVisibilitySettings from '@/views/post/components/PostVisibilitySettings.vue'
import {PostStatus, PostType, type PostVO} from '@/services/post'
import {TimeUtils} from '@/utils/time'
import {formatNum} from '@/utils/page'
import {parseMediaUrls, parseTag} from '@/utils/post'
import UserHoverCard from '@/presentation/components/UserHoverCard.vue'
import router from '@/router'
import {useUserInfoStore} from '@/stores/userInfo'

const showSettingsModal = ref(false)
const showShareTip = ref(false)

const handleShare = async () => {
  const title = props.post.title || ''
  const truncatedTitle = title.length > 30 ? title.substring(0, 30) + '...' : title
  const shareUrl = `${window.location.origin}/home/homeFeed?postId=${props.post.id}`
  const shareText = `${truncatedTitle} ${shareUrl}`

  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(shareText)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = shareText
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    showShareTip.value = true
    setTimeout(() => {
      showShareTip.value = false
    }, 2000)
  } catch (err) {
    console.error('Failed to copy share link:', err)
  }
}

const props = withDefaults(
  defineProps<{
    post: PostVO
    self?: boolean
    isCard?: boolean
    hideTop?: boolean
  }>(),
  {
    self: false,
    isCard: false,
    hideTop: false
  }
)

const userStore = useUserInfoStore()
const isOwner = computed(() => {
  const loginUser = userStore.user
  if (!loginUser || !props.post) return false
  return props.post.creatorId?.toString() === loginUser.id?.toString()
})

const emit = defineEmits<{
  (e: 'like', postId: string | number | undefined): void
  (e: 'collect', postId: string | number | undefined): void
  (e: 'comment-click', post: PostVO): void
  (e: 'delete', postId: string | number | undefined): void
  (e: 'top', post: PostVO): void
}>()

// 展开/收起控制
const isExpanded = ref(false)

function shouldShowExpand(content: string) {
  if (!content) return false
  return content.length > 150 || content.split('\n').length > 3
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

function handleTextClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (target.tagName === 'A') {
    let tagText = target.innerText || ''
    tagText = tagText.replace(/^#|#$/g, '').trim()
    if (tagText) {
      router.push({ name: 'postTagInfo', params: { tagName: tagText } })
    }
  }
}

// 视频预览控制
const previewVideoUrl = ref('')
const videoDialogVisible = ref(false)

function openVideoPreview(url: string) {
  previewVideoUrl.value = url
  videoDialogVisible.value = true
}

// 视频悬停播放控制
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

// 媒体解析辅助
function getPostImages(item: PostVO): string[] {
  if (Array.isArray(item.mediaUrls)) {
    return item.mediaUrls.map((m) => m.imageUrl).filter(Boolean)
  }
  return parseMediaUrls(item.extraMediaUrl)
}

function getPostVideo(item: PostVO): string {
  return item.extraMediaUrl || ''
}

// 兼容媒体逻辑
const isImageType = computed(() => {
  return props.post.type === PostType.IMAGE || (props.post.mediaUrls && props.post.mediaUrls.length > 0)
})
const isVideoType = computed(() => props.post.type === PostType.VIDEO)
const isAudioType = computed(() => props.post.type === PostType.AUDIO)
const isLinkType = computed(() => props.post.type === PostType.LINK)
const isFileType = computed(() => props.post.type === PostType.FILE)
const hasExtraMediaUrl = computed(() => !!props.post.extraMediaUrl)
</script>
