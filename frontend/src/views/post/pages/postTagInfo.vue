<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 w-screen h-screen bg-gray-400/50 z-40">
      <div class="postInfo w-2/5 m-auto h-full">
        <postInfo v-if="curPost" @close="curPost = null; visible = false" :visible="visible" :post="curPost" />
      </div>
    </div>
  </Teleport>

  <div class="main relative flex min-h-screen flex-col items-center w-full">
    <!-- 面包屑导航 -->
    <div class="w-full max-w-300 px-4 mt-4">
      <div class="flex items-center gap-2 text-xs text-stone-400 font-medium select-none">
        <span class="hover:text-blue-500 cursor-pointer transition-colors" @click="router.push('/')">首页</span>
        <span>/</span>
        <span class="text-stone-600 font-semibold">#{{ tagName }}#</span>
      </div>
    </div>

    <div class="flex items-start w-full max-w-300 px-4 gap-4 mt-3 relative">
      <!-- 左侧内容区 -->
      <div class="left w-[75%] min-h-125 rounded-lg flex flex-col gap-4">
        
        <!-- 话题头部卡片 -->
        <div class="topic-header bg-white rounded-xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.02)] flex flex-col md:flex-row items-center md:items-start gap-5">
          <!-- 拟物态渐变图标 -->
          <div class="size-16 rounded-2xl bg-gradient-to-br from-blue-400 to-indigo-500 flex items-center justify-center text-white text-3xl font-extrabold shadow-[0_8px_20px_-4px_rgba(59,130,246,0.3)] shrink-0 select-none">
            #
          </div>
          <!-- 话题文字信息 -->
          <div class="flex-1 text-center md:text-left">
            <div class="flex flex-col md:flex-row md:items-center gap-3">
              <h1 class="text-xl font-bold text-gray-900">#{{ tagName }}#</h1>
              <div class="flex items-center justify-center md:justify-start gap-2 text-xs text-stone-400 mt-1 md:mt-0 select-none">
                <span>{{ tagInfo?.extra || 0 }} 互动</span>
                <span>·</span>
                <span>{{ topicList.length }} 讨论</span>
              </div>
            </div>
            <p class="text-stone-500 text-sm mt-3 leading-relaxed">
              这是关于话题 #{{ tagName }}# 的专属讨论版块。在此您可以分享精心整理的学习路径、踩坑经验、精选工具或深度见解。
            </p>
          </div>
          <!-- 操作区 -->
          <div class="flex gap-2.5 shrink-0 self-stretch md:self-auto justify-center select-none">
            <el-button 
              :type="isFollowed ? 'info' : 'primary'"
              :plain="isFollowed"
              @click="toggleFollow" 
              class="font-semibold !rounded-lg"
            >
              {{ isFollowed ? '已关注话题' : '关注话题' }}
            </el-button>
            <el-button @click="shareTopic" class="font-semibold !rounded-lg">
              分享
            </el-button>
          </div>
        </div>

        <!-- 过滤器和帖子列表 -->
        <div class="feed-container bg-white rounded-xl border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.02)] overflow-hidden">
          <div class="tab flex items-center px-6 py-4 border-b border-gray-100 justify-between select-none">
            <span class="text-sm font-bold text-gray-800">话题讨论区</span>
            <div class="flex items-center gap-4 text-xs font-semibold text-gray-400">
              <span class="cursor-pointer px-2.5 py-1 rounded-md transition-all duration-200"
                :class="orderType === 'new' ? 'text-blue-600 bg-blue-50 scale-105 shadow-xs' : 'hover:text-gray-600 hover:bg-gray-50'"
                @click="orderType = 'new'">
                最新
              </span>
              <span class="cursor-pointer px-2.5 py-1 rounded-md transition-all duration-200"
                :class="orderType === 'hot' ? 'text-amber-500 bg-amber-50 scale-105 shadow-xs' : 'hover:text-gray-600 hover:bg-gray-50'"
                @click="orderType = 'hot'">
                最热
              </span>
            </div>
          </div>

          <div class="container-comments min-h-125 w-full flex flex-col">
            <template v-if="loading && topicList.length === 0">
              <div class="w-full flex justify-center py-20">
                <el-icon class="is-loading text-4xl text-blue-500">
                  <LoadingIcon />
                </el-icon>
              </div>
            </template>
            <template v-else-if="topicList.length > 0">
              <PostItem v-for="item in topicList" :key="item.id" :post="item" @like="like" @collect="collect"
                @comment-click="curPost = $event; visible = true;" />
              <!-- 加载更多 -->
              <div v-if="hasMore" class="flex justify-center py-6 border-t border-gray-50 select-none">
                <el-button 
                  :loading="loadingMore" 
                  @click="loadMore"
                  class="!rounded-lg !text-xs font-semibold hover:!text-blue-500 animate-pulse"
                >
                  加载更多帖子
                </el-button>
              </div>
              <div v-else class="text-center py-6 text-xs text-stone-400 select-none border-t border-gray-50">
                没有更多帖子了
              </div>
            </template>
            <template v-else>
              <div class="w-full py-20 flex flex-col items-center justify-center text-stone-400 select-none">
                <svg class="size-16 text-stone-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.663 17h4.673M12 3v1m6.364.364l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
                <span class="text-sm">暂无此话题相关的讨论帖子，快来抢沙发吧！</span>
              </div>
            </template>
          </div>
        </div>

      </div>

      <!-- 右侧侧边栏 -->
      <div class="right w-[25%] shrink-0">
        <home-feed></home-feed>
      </div>
    </div>

    <!-- 分享 Toast 提示 -->
    <Teleport to="body">
      <div v-if="showShareToast" class="fixed inset-0 z-50 flex items-center justify-center pointer-events-none">
        <div class="bg-gray-800/95 text-white rounded-md px-5 py-3 shadow-lg text-sm font-medium">
          话题分享链接已复制到剪贴板
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {Loading as LoadingIcon} from '@element-plus/icons-vue'
import homeFeed from '@/views/home/pages/rankboard-home.vue'
import {type PostVO, TagAPI} from '@/services/post'
import postInfo from '@/views/post/pages/postInfo.vue'
import PostItem from '@/views/post/pages/postItem.vue'
import {usePostInteractions} from '@/views/post/composables/usePostInteractions'
import {log} from '@/utils/log'
import router from '@/router'

const route = useRoute()
const tagName = computed(() => (route.params.tagName as string) || '')

const orderType = ref('new')
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const cursor = ref<string | null>(null)

const topicList = ref<PostVO[]>([])
const tagInfo = ref<any>(null)
const isFollowed = ref(false)
const showShareToast = ref(false)

const curPost = ref<PostVO | null>(null)
const visible = ref(false)

const { like, collect } = usePostInteractions(topicList)

// 获取话题基本属性
async function loadTagInfo() {
  if (!tagName.value) return
  try {
    const res = await TagAPI.getSearchSuggest(tagName.value, 5)
    if (res.code === 1 && res.data && res.data.suggestList) {
      const exactMatch = res.data.suggestList.find(t => t.keyword === tagName.value)
      if (exactMatch) {
        tagInfo.value = exactMatch
      } else if (res.data.suggestList.length > 0) {
        tagInfo.value = res.data.suggestList[0]
      }
    }
  } catch (e) {
    console.error('Failed to load tag info:', e)
  }
}

// 加载话题帖子列表 (第一页/刷新)
async function loadData() {
  if (!tagName.value || loading.value) return
  loading.value = true
  cursor.value = null
  hasMore.value = true
  try {
    const isHot = orderType.value === 'hot'
    const res = await TagAPI.getPostsByTag(tagName.value, null, 10, isHot)
    if (res.code === 1 && res.data && res.data.list) {
      topicList.value = res.data.list
      cursor.value = res.data.cursor
      hasMore.value = res.data.hasMore
    } else {
      topicList.value = []
      hasMore.value = false
    }
  } catch (e) {
    console.error('Failed to load tag posts:', e)
    topicList.value = []
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

// 加载下一页
async function loadMore() {
  if (!tagName.value || loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const isHot = orderType.value === 'hot'
    const res = await TagAPI.getPostsByTag(tagName.value, cursor.value, 10, isHot)
    if (res.code === 1 && res.data && res.data.list) {
      topicList.value.push(...res.data.list)
      cursor.value = res.data.cursor
      hasMore.value = res.data.hasMore
    } else {
      hasMore.value = false
    }
  } catch (e) {
    console.error('Failed to load more tag posts:', e)
  } finally {
    loadingMore.value = false
  }
}

// 监听排序切换
watch(orderType, () => {
  loadData()
})

// 关注话题切换
function toggleFollow() {
  isFollowed.value = !isFollowed.value
  log.success(isFollowed.value ? '关注话题成功' : '取消关注话题')
}

// 复制分享话题
function shareTopic() {
  const shareUrl = window.location.href
  const text = `#${tagName.value}# 话题讨论页: ${shareUrl}`
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = text
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    showShareToast.value = true
    setTimeout(() => {
      showShareToast.value = false
    }, 2000)
  } catch (e) {
    console.error(e)
  }
}

// 跳转发帖
function goToPublish() {
  router.push({ name: 'post', query: { tag: tagName.value } })
}

onMounted(async () => {
  await Promise.all([loadTagInfo(), loadData()])
})
</script>

<style scoped>
.size-16 {
  width: 4rem;
  height: 4rem;
}
.size-9 {
  width: 2.25rem;
  height: 2.25rem;
}
</style>