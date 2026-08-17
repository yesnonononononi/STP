<template>
    <div
        class="absolute inset-0 flex justify-center items-center bg-linear-to-br from-indigo-100 via-blue-50 to-emerald-50">
        <div
            class="w-full md:w-112.5 h-dvh md:h-[calc(100vh-2rem)] flex items-center flex-col gap-2 bg-white md:rounded-2xl md:shadow-2xl md:border md:border-gray-100/50 overflow-hidden">
            <div class="background h-1/4 w-full relative overflow-hidden shrink-0">
                <img :src="curBoard?.bgImg || 'https://static.nowcoder.com/fe/file/oss/1721890297524YMZKN.png'"
                    alt="热度排行" class="w-full h-full object-cover bg-gray-300">
                <div class="absolute inset-0 bg-linear-to-t from-black/60 via-black/25 to-transparent"></div>
                <div class="absolute bottom-4 left-6 text-white font-bold text-2xl tracking-wider drop-shadow-md">
                    {{ curBoard?.name || '热度排行' }}
                </div>
            </div>
            <div class="body grow w-full flex flex-col gap-1 overflow-hidden h-3/4 p-2">
                <div
                    class="  min-h-12 w-full flex items-center justify-evenly border-b border-gray-100 pb-3 pt-1 transition-all duration-300">
                    <span v-for="(tab, index) in tabList" :key="index"
                        class="cursor-pointer text-sm font-semibold transition-all duration-300 pb-2 relative shrink-0"
                        :class="tab.title === curTab ? 'text-blue-600 scale-[1.03]' : 'text-gray-400 hover:text-gray-600'"
                        @click="curTab = tab.title">
                        {{ tab.title }}
                        <span v-if="tab.title === curTab"
                            class="  h-1 bg-linear-to-r from-blue-500 to-indigo-600 rounded-full"></span>
                    </span>
                </div>
                <div class="flex-1 w-full overflow-hidden  flex flex-col mt-2 ">
                    <!-- 骨架屏加载状态 -->
                    <template v-if="loading">
                        <!-- 创作者周榜骨架屏 -->
                        <div v-if="curTab === '创作者周榜'" class="flex flex-col gap-3 w-full animate-pulse px-3 overflow-y-auto grow">
                            <div class="flex items-end justify-center gap-4 mt-10 mb-6 h-36 px-4 shrink-0">
                                <div class="flex-1 h-28 bg-gray-100 border border-gray-200/50 rounded-xl"></div>
                                <div class="flex-1 h-32 bg-gray-100 border-2 border-gray-200/50 rounded-2xl"></div>
                                <div class="flex-1 h-24 bg-gray-100 border border-gray-200/50 rounded-xl"></div>
                            </div>
                            <div class="flex flex-col gap-2.5 px-1 grow">
                                <div v-for="i in 5" :key="i" class="flex justify-between items-center p-2.5 bg-gray-50/50 rounded-xl border border-transparent">
                                    <div class="flex items-center gap-3 w-2/3">
                                        <div class="w-6 h-6 rounded-full bg-gray-200/80"></div>
                                        <div class="w-10 h-10 rounded-full bg-gray-200/80"></div>
                                        <div class="flex-1 flex flex-col gap-1.5">
                                            <div class="h-3.5 bg-gray-200/80 rounded w-1/2"></div>
                                            <div class="h-2.5 bg-gray-200/80 rounded w-1/3"></div>
                                        </div>
                                    </div>
                                    <div class="w-12 h-5 bg-gray-200/80 rounded-full"></div>
                                </div>
                            </div>
                        </div>
                        <!-- 普通榜单骨架屏 -->
                        <div v-else class="flex flex-col gap-2.5 w-full animate-pulse px-3 overflow-y-auto grow">
                            <div v-for="i in 8" :key="i" class="flex justify-between items-center p-2.5 bg-gray-50/50 rounded-xl">
                                <div class="flex items-center gap-3 w-2/3">
                                    <div class="w-6 h-6 rounded-full bg-gray-200/80"></div>
                                    <div class="h-4 bg-gray-200/80 rounded w-2/3"></div>
                                </div>
                                <div class="w-12 h-5 bg-gray-200/80 rounded-full"></div>
                            </div>
                        </div>
                    </template>

                    <!-- 空数据无榜单展示 -->
                    <template v-else-if="!loading && rankList.length === 0">
                        <div class="flex flex-col items-center justify-center py-16 px-4 text-center grow animate-fade-in">
                            <div class="relative w-36 h-36 mb-4 flex items-center justify-center">
                                <div class="absolute inset-0 bg-gradient-to-tr from-blue-100/30 to-indigo-100/30 rounded-full filter blur-xl animate-pulse"></div>
                                <svg class="w-20 h-20 text-gray-300/85 drop-shadow-xs" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M19 11H5C3.89543 11 3 11.8954 3 13V19C3 20.1046 3.89543 21 5 21H19C20.1046 21 21 20.1046 21 19V13C21 11.8954 20.1046 11 19 11Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
                                    <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                                    <path d="M12 15V17" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                                </svg>
                            </div>
                            <h3 class="text-sm font-semibold text-gray-600 mb-1.5">榜单正在孕育中</h3>
                            <p class="text-xs text-gray-400 max-w-xs leading-relaxed">当前暂无排行数据，互动起来，助你喜爱的作者冲上榜单吧！</p>
                        </div>
                    </template>

                    <!-- 真实数据渲染 -->
                    <template v-else>
                        <Common_rank v-model="(rankList as MappedCommonRank[])" v-if="curTab !== '创作者周榜'" @click-item="handleRankItemClick" />
                        <Creator_rank v-else v-model="(rankList as MappedCreatorRank[])" />
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import {onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import Common_rank from './common_rank.vue';
import Creator_rank from './creator_rank.vue';
import {RankBoardAPI} from '@/services/rank';
import type {MappedCommonRank, MappedCreatorRank} from '@/services/rank/types';

const router = useRouter();
const tabList = ref([
    { id: 'creator', title: '创作者周榜', name: '创作者周榜', bgImg: '' },
    { id: 'post', title: '全站热帖', name: '全站热帖榜', bgImg: '' },
    { id: 'topic', title: '话题榜', name: '话题榜', bgImg: '' }
]);
const curTab = ref(useRoute().query.tab as string);
const rankList = ref<(MappedCommonRank | MappedCreatorRank)[]>([]);
const curBoard = ref<{ id: string, title: string, name: string, bgImg: string }>();
const loading = ref(false);

onMounted(async () => {
    loadAllTab();
    await loadData();
})

watch(() => curTab.value, async () => {
    await loadData();
})

function loadAllTab() {
    if (tabList.value.length > 0 && !curTab.value) {
        curTab.value = tabList.value[0]?.title || '';
    }
}

async function loadData() {
    if (!tabList.value || tabList.value.length === 0) return;
    const matchedTab = tabList.value.find(t => t.title === curTab.value) || tabList.value[0];
    if (!matchedTab) return;
    curBoard.value = matchedTab;
    try {
        loading.value = true;
        const res = await RankBoardAPI.getRankListByTabId(matchedTab.id);
        rankList.value = res;
    } catch (e) {
        console.error('Failed to load ranking data:', e);
        rankList.value = [];
    } finally {
        loading.value = false;
    }
}

function handleRankItemClick(item: MappedCommonRank) {
    if (curTab.value === '话题榜') {
        router.push({ name: 'postTagInfo', params: { tagName: item.title } });
    }
}

</script>

<style scoped>
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(6px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
.animate-fade-in {
    animation: fadeIn 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
