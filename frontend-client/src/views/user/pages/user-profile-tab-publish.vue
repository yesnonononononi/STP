<template>
    <div class="relative w-full h-full">
        <!-- 评论详情弹窗 -->
        <Teleport to="body">
            <div v-if="visible" class="fixed inset-0 w-screen h-screen bg-gray-400/50 z-40">
                <div class="postInfo w-2/5 m-auto h-full">
                    <postInfo v-if="curPost" @close="curPost = null; visible = false" :visible="visible"
                        :post="curPost" />
                </div>
            </div>
        </Teleport>
        <Loading v-model="loading" />

        <div v-if="filteredTopicList.length > 0"
            class="w-full h-full flex bg-gray-50 overflow-y-auto flex-col gap-4 p-4 scrollbar-thin rounded-xl">
            <PostItem v-for="(topic, index) in filteredTopicList" :key="index" :post="topic" :self="props.self" :is-card="true"
                :hide-top="props.status === PostStatus.COLLECTED"
                @like="like" @collect="collect" @comment-click="curPost = $event; visible = true;" @top="handleTopClick"
                @delete="emit('delete', $event)" />
        </div>

        <div v-else-if="filteredTopicList.length == 0 && !loading"
            class="w-full h-full bg-white flex flex-col justify-center items-center rounded-xl p-8 shadow-xs">
            <div class="flex flex-col items-center gap-3 text-slate-400">
                <svg class="size-12 text-slate-300 stroke-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                </svg>
                <span class="text-sm font-medium">暂无对应状态的帖子记录~~</span>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import {PostAPI, PostStatus, type PostVO} from '@/services/post';
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import {scrollerFromBottom} from '@/utils/scollerbar';
import {usePostInteractions} from '@/views/post/composables/usePostInteractions';
import postInfo from '@/views/post/pages/postInfo.vue';
import PostItem from '@/views/post/pages/postItem.vue';
import Loading from '@/presentation/components/loading.vue';
import {useUserInfoStore} from '@/stores/userInfo';

const curPost = ref<PostVO | null>(null);
const visible = ref(false);
const route = useRoute();

const props = withDefaults(defineProps<{
    status: number
    creatorId: string | number
    self: boolean
    auditFilter?: number
}>(), {
    auditFilter: -1
})

const cursor = ref('');
const emit = defineEmits(['delete']);
const loading = ref(false)
const hasMore = ref(true)
const topicList = ref<PostVO[]>([])
const userStore = useUserInfoStore()

const filteredTopicList = computed(() => {
    let list = topicList.value;
    if (userStore.settings?.showDelPost === 0) {
        list = list.filter(topic => Number(topic.status) !== PostStatus.DELETED);
    }

    // 他人访问主页时，只保留正常通过状态 (状态 1)
    if (!props.self) {
        return list.filter(topic => {
            const statusVal = Number(topic.status);
            return statusVal === PostStatus.NORMAL || statusVal === 1;
        });
    }

    // 看自己主页时，若选择了特定审核条件进行精细化前端双重防御过滤
    if (props.auditFilter !== -1) {
        list = list.filter(topic => {
            const statusVal = Number(topic.status);

            if (props.auditFilter === PostStatus.CHECK) { // 7
                return statusVal === PostStatus.CHECK || statusVal === 7 || String(topic.status) === '7';
            } else if (props.auditFilter === PostStatus.UNPASS) { // 8
                return statusVal === PostStatus.UNPASS || statusVal === 8 || String(topic.status) === '8';
            } else if (props.auditFilter === PostStatus.NORMAL) { // 1
                return statusVal === PostStatus.NORMAL || statusVal === 1;
            }
            return true;
        });
    }

    return list;
})

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

// 监听状态与选单 filter 变化，自动重载列表
watch(
    () => [props.status, props.creatorId, props.auditFilter],
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
        // 根据所选下拉框精准透传 status 给后端 (7 为待审核, 8 为审核未通过, 1 为正常)
        const targetStatus = props.self && props.status === 0
            ? (props.auditFilter !== -1 ? props.auditFilter : undefined)
            : (s === 0 ? undefined : s);

        const res = (await PostAPI.getPage({
            cursor: cursor.value,
            self: props.self,
            creatorId: props.creatorId,
            status: targetStatus
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
    loadData: load,
    topicList
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
