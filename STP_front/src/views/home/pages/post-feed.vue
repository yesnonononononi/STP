<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0  w-screen h-screen bg-gray-400/50 z-40">
      <div class="postInfo w-2/5 m-auto h-full ">
        <postInfo v-if="curPost" @close="curPost = null; visible = false" :visible="visible" :post="curPost" />
      </div>
    </div>
  </Teleport>

  <div class="container-comments min-h-400   w-full h-full flex flex-col">
    <PostItem
      v-for="item in topicList"
      :key="item.id"
      :post="item"
      @like="like"
      @collect="collect"
      @comment-click="curPost = $event; visible = true;"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

import { PostAPI, type PostVO } from '@/services/post'
import postInfo from '@/views/post/pages/postInfo.vue'
import PostItem from '@/views/post/pages/postItem.vue'
import { scrollerFromBottom } from '@/utils/scollerbar'
import { usePostInteractions } from '@/views/post/composables/usePostInteractions'

const cursor = ref<string | number | null>(null);
const loading = ref(false);
const noMore = ref(false);
const props = defineProps<{
  tabId?: string
}>()

const curPost = ref<PostVO | null>(null)
const visible = ref(false)
const topicList = ref<PostVO[]>([])

const {
  like,
  collect
} = usePostInteractions(topicList)

const route = useRoute()

let remove: () => void;
onMounted(async () => {
  remove = scrollerFromBottom(async () => {
    await loadData(true)
  }, window) || (() => { })
  await loadData();

  // 检查是否在 URL 携带了 postId
  const queryPostId = route.query.postId
  if (queryPostId) {
    try {
      const res = await PostAPI.getById(queryPostId.toString())
      if (res.code === 1 && res.data) {
        curPost.value = res.data
        visible.value = true
      }
    } catch (e) {
      console.error('Failed to load shared post in feed: ', e)
    }
  }
})

onUnmounted(() => {
  if (remove) remove()
})

async function loadData(append = false) {
  if (loading.value || (append && noMore.value)) return;
  loading.value = true;
  try {
    const currentCursor = append ? cursor.value : null;
    const res = await PostAPI.getPage({ cursor: currentCursor, self: false });
    if (res.code === 1 && res.data) {
      if (append) {
        topicList.value = [...topicList.value, ...res.data];
      } else {
        topicList.value = res.data;
        noMore.value = false;
      }
      if (res.data.length === 0) {
        noMore.value = true;
      } else {
        cursor.value = res.data[res.data.length - 1]?.id || null;
      }
    }
  } catch (e) {
    console.error('加载动态列表异常: ', e);
  } finally {
    loading.value = false;
  }
}

watch(() => props.tabId, () => {
  loadData();
})
</script>

