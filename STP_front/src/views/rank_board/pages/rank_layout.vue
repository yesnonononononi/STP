<template>
    <div class="absolute inset-0 flex justify-center items-center bg-linear-to-br from-indigo-100 via-blue-50 to-emerald-50">
        <div class="w-full md:w-[450px] h-dvh md:h-[calc(100vh-2rem)] flex items-center flex-col gap-2 bg-white md:rounded-2xl md:shadow-2xl md:border md:border-gray-100/50 overflow-hidden">
            <div class="background h-1/4 w-full relative overflow-hidden shrink-0">
                <img :src="curBoard?.bgImg || 'https://static.nowcoder.com/fe/file/oss/1721890297524YMZKN.png'"
                    alt="热度排行" class="w-full h-full object-cover bg-gray-300">
                <div class="absolute inset-0 bg-linear-to-t from-black/60 via-black/25 to-transparent"></div>
                <div class="absolute bottom-4 left-6 text-white font-bold text-2xl tracking-wider drop-shadow-md">
                    {{ curBoard?.name || '热度排行' }}
                </div>
            </div>
            <div class="body grow w-full flex flex-col gap-1 overflow-hidden p-2">
                <div
                    class="relative z-10 min-h-12 w-full flex items-center justify-evenly border-b border-gray-100 pb-3 pt-1 transition-all duration-300">
                    <span v-for="(tab, index) in tabList" :key="index" class="cursor-pointer text-sm font-semibold transition-all duration-300 pb-2 relative shrink-0"
                        :class="tab.title === curTab ? 'text-blue-600 scale-[1.03]' : 'text-gray-400 hover:text-gray-600'"
                        @click="curTab = tab.title">
                        {{ tab.title }}
                        <span v-if="tab.title === curTab" class="absolute bottom-0 left-0 right-0 h-1 bg-linear-to-r from-blue-500 to-indigo-600 rounded-full"></span>
                    </span>
                </div>
                <div class="flex-1 w-full overflow-hidden relative flex flex-col pt-2">
                    <Loading v-model="loading" :bg-color="'bg-white'" />
                    <Common_rank v-model="(rankList as MappedCommonRank[])" v-if="curTab !== '创作者周榜'" />
                    <Creator_rank v-else v-model="(rankList as MappedCreatorRank[])" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import Loading from '@/presentation/components/loading.vue';
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import Common_rank from './common_rank.vue';
import Creator_rank from './creator_rank.vue';
import { RankBoardAPI } from '@/services/rank';
import type { MappedCommonRank, MappedCreatorRank } from '@/services/rank/types';

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

</script>
