import { ref, type Ref, computed } from 'vue'
import { PostAPI, PostType, type PostVO } from '@/services/post'
import { parseMediaUrls } from '@/utils/post'
import { useUserInfoStore } from '@/stores/userInfo'
import { UserAPI } from '@/services/user'
import { log } from '@/utils/log'
import router from '@/router'

export function usePostInteractions(topicList: Ref<PostVO[]>) {
  const me = computed(() => useUserInfoStore().user);
  
  // 展开/收起控制
  const expandedPosts = ref<Record<string | number, boolean>>({})

  function shouldShowExpand(content: string) {
    if (!content) return false
    return content.length > 150 || content.split('\n').length > 3
  }

  function toggleExpand(id: string | number) {
    expandedPosts.value[id] = !expandedPosts.value[id]
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

  // 点赞逻辑
  async function like(topicId: string | undefined | number) {
    if (!topicId) return;
    const res = await PostAPI.like(topicId.toString());
    if (res.code === 1) {
      const topic = topicList.value.find(t => t.id == topicId)
      if (!topic) return;

      const isLikeBefore = topic.isLike;
      const publisherId = topic.publisher?.id;

      if (topic.isLike) {
        topic.likeCount = Math.max(0, Number(topic.likeCount || 0) - 1);
        topic.isLike = false;
      } else {
        topic.likeCount = Number(topic.likeCount || 0) + 1;
        topic.isLike = true;
      }

      // 遍历列表，将发帖者的获赞++或--
      if (publisherId) {
        topicList.value.forEach((post) => {
          if (post.publisher && post.publisher.id === publisherId) {
            const currentLiked = Number(post.publisher.liked || 0);
            post.publisher.liked = isLikeBefore ? Math.max(0, currentLiked - 1) : currentLiked + 1;
          }
        });
      }
    }
  }

  // 收藏逻辑
  async function collect(topicId: string | undefined | number) {
    if (!topicId) return;
    const res = await PostAPI.collect(topicId.toString());
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

  return {
    me,
    expandedPosts,
    shouldShowExpand,
    toggleExpand,
    previewVideoUrl,
    videoDialogVisible,
    openVideoPreview,
    onVideoMouseEnter,
    onVideoMouseLeave,
    getPostImages,
    getPostVideo,
    like,
    collect
  }
}
