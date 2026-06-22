<template>
    <div class="relative w-full h-full ">
        <!-- 评论详情弹窗 -->
        <Teleport to="body">
            <div v-if="visible" class="fixed inset-0 w-screen h-screen bg-gray-400/50 z-40">
                <div class="postInfo w-2/5 m-auto h-full ">
                    <postInfo v-if="curPost" @close="curPost = null; visible = false" :visible="visible"
                        :post="curPost" />
                </div>
            </div>
        </Teleport>
        <Loading v-model="loading" />
        <div v-if="topicList.length > 0"
            class="w-full h-full flex bg-gray-50 overflow-y-auto flex-col gap-4 p-4 scrollbar-thin">
            <PostItem v-for="(topic, index) in topicList" :key="index" :post="topic" :self="props.self" :is-card="true"
                @like="like" @collect="collect" @comment-click="curPost = $event; visible = true;" @top="handleTopClick"
                @delete="emit('delete', $event)" />
        </div>

        <div v-else class="absolute inset-0 z-10 bg-white flex justify-center items-center">
            <div class="flex flex-col items-center gap-4 ">
                <span>这里空空如也~~</span>
            </div>
        </div>

    </div>

</template>

<script lang="ts" setup>
import { PostAPI, PostStatus, type PostVO } from '@/services/post';
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { scrollerFromBottom } from '@/utils/scollerbar';
import { usePostInteractions } from '@/views/post/composables/usePostInteractions';
import postInfo from '@/views/post/pages/postInfo.vue';
import PostItem from '@/views/post/pages/postItem.vue';
import Loading from '@/presentation/components/loading.vue';

const curPost = ref<PostVO | null>(null);
const visible = ref(false);
const route = useRoute();

const props = defineProps<{
    status: number
    creatorId: string | number
    self: boolean
}>()
const cursor = ref('');
const emit = defineEmits(['delete']);
const loading = ref(false)
const hasMore = ref(true)
const topicList = ref<PostVO[]>([])

const {
    like,
    collect,
} = usePostInteractions(topicList)

let removeScrollListener: (() => void) | null = null;

onMounted(() => {
    removeScrollListener = scrollerFromBottom(() => {
        nextTick(async () => {
            await load();
        })
    })
})

onUnmounted(() => {
    if (removeScrollListener) {
        removeScrollListener();
    }
})

watch(
    () => [props.status, props.creatorId],
    async () => {
        hasMore.value = true
        topicList.value = []
        cursor.value = ''
        await load()
    }
)

onMounted(async () => {
    await load()
    // 检查是否在 URL 携带了 postId
    const queryPostId = route.query.postId;
    if (queryPostId) {
        try {
            const res = await PostAPI.getById(queryPostId.toString());
            if (res.code === 1 && res.data) {
                curPost.value = res.data;
                visible.value = true;
            }
        } catch (e) {
            console.error('Failed to load shared post in profile:', e);
        }
    }
})

async function load() {

    if (loading.value || !hasMore.value) return;

    if (props.status === PostStatus.COLLECTED || props.status === PostStatus.LIKED) {
        await loadMyList(props.status);
    } else {
        await loadData(props.status);
    }
}

async function loadData(s: number) {
    loading.value = true
    cursor.value = topicList.value.length > 0 ? topicList.value[topicList.value.length - 1]?.id?.toString() as string : '';
    try {
        const res = (await PostAPI.getPage({
            cursor: cursor.value,
            self: props.self,
            creatorId: props.creatorId,
            status: s === 0 ? undefined : s
        })).data
        if (res.length == 0 || res.length < 10) {
            hasMore.value = false
        }
        topicList.value = [...topicList.value, ...res]
    } catch (e) {
        console.error('加载数据失败:', e)
    } finally {
        loading.value = false
    }
}

/**
 * 加载我的收藏,我的点赞的帖子
 */
async function loadMyList(s: number) {
    loading.value = true
    cursor.value = topicList.value.length > 0 ? topicList.value[topicList.value.length - 1]?.id?.toString() as string : '';
    try {
        //处理收藏和点赞状态
        if (s === PostStatus.COLLECTED) {
            const res = (await PostAPI.getMyCollectList(props.creatorId, cursor.value)).data
            if (res.length == 0 || res.length < 10) {
                hasMore.value = false;
            }
            topicList.value = [...topicList.value, ...res]
        }
        else if (s === PostStatus.LIKED) {
            const res = (await PostAPI.getMyLikeList(props.creatorId, cursor.value)).data
            if (res.length == 0 || res.length < 10) {
                hasMore.value = false;
            }
            topicList.value = [...topicList.value, ...res]
        }
    } catch (e) {
        console.error('加载我的列表失败:', e)
    } finally {
        loading.value = false
    }
}

defineExpose({
    loadData: load
})

const refresh = async () => {
    hasMore.value = true
    topicList.value = []
    cursor.value = ''
    await load()
}

const handleTopClick = async (topic: any) => {
    const targetTop = topic.isTop === 1 ? 0 : 1;
    try {
        const res = await PostAPI.top(topic.id, targetTop);
        if (res.code === 1) {
            topic.isTop = targetTop;
            await refresh();
        }
    } catch (err) {
        console.error('操作失败', err);
    }
}
</script>
