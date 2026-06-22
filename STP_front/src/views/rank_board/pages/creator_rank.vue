<template>
    <div class="flex flex-col gap-2 grow overflow-y-auto">
        <!-- 前三名领奖台 -->
        <div class="tab w-full mt-6 flex px-4 md:px-8 py-4 pb-2 gap-3 justify-center">
            <div class="tab-item flex-1 p-3 bg-linear-to-b rounded-2xl transition-all duration-300 hover:scale-[1.02] border backdrop-blur-md relative"
                v-for="item in topThree" :key="item.id"
                :class="[
                    item.sort === 1 ? 'from-amber-100/90 via-amber-50/40 to-transparent border-amber-200/60 shadow-[0_12px_24px_rgba(245,158,11,0.15)] order-2 -translate-y-6 hover:-translate-y-8' :
                    item.sort === 2 ? 'from-slate-100/90 via-slate-50/40 to-transparent border-slate-200/50 shadow-[0_8px_16px_rgba(148,163,184,0.1)] order-1 hover:-translate-y-2' :
                    'from-orange-100/90 via-orange-50/40 to-transparent border-orange-200/40 shadow-[0_8px_16px_rgba(217,119,6,0.08)] order-3 hover:-translate-y-2'
                ]">
                <div class="box flex flex-col items-center justify-center -translate-y-1/4 gap-1.5 p-1">
                    <div class="size-12 relative z-1 mb-1">
                        <img :src="item.avatar || ''" alt="" class="size-12 rounded-full ring-2 ring-offset-2 transition-transform duration-300"
                            :class="[
                                item.sort === 1 ? 'ring-amber-400' :
                                item.sort === 2 ? 'ring-slate-300' :
                                'ring-orange-300'
                            ]">
                        <img :src="item.decoration" alt="" class="absolute inset-0 z-2 scale-150 size-12 pointer-events-none">
                    </div>
                    <div class="text-sm font-bold truncate max-w-full text-center"
                        :class="[
                            item.sort === 1 ? 'text-amber-900' :
                            item.sort === 2 ? 'text-slate-800' :
                            'text-orange-950'
                        ]">
                        {{ item.nickname }}
                    </div>
                    <div class="text-[10px] text-gray-400 text-center font-medium">{{ item.ip || '未知地区' }}</div>
                    <div class="flex items-center gap-1 mt-1 px-2 py-0.5 rounded-full border"
                        :class="[
                            item.sort === 1 ? 'bg-amber-100/60 text-amber-700 border-amber-200/40' :
                            item.sort === 2 ? 'bg-slate-100/60 text-slate-700 border-slate-200/40' :
                            'bg-orange-100/50 text-orange-700 border-orange-200/30'
                        ]">
                        <span class="text-[10px] font-bold">{{ formatNum(Number(item.score)) }}</span>
                        <svg t="1780575765923" class="icon size-3.5" viewBox="0 0 1024 1024" version="1.1"
                            xmlns="http://www.w3.org/2000/svg" p-id="5385">
                            <path
                                d="M442.514286 73.142857c82.529524 64.24381 140.239238 126.610286 173.129143 187.099429 31.158857 57.295238 43.666286 115.907048 37.546666 175.835428l-1.219047 9.996191 6.095238-4.973715a174.055619 174.055619 0 0 0 49.249524-69.607619l2.681904-7.411809 7.704381-23.04c82.285714 55.734857 123.440762 150.064762 123.440762 283.062857C841.142857 823.515429 665.795048 950.857143 521.654857 950.857143c-144.11581 0-308.224-85.333333-334.750476-263.875048-26.550857-178.541714 83.480381-261.90019 158.427429-378.197333C395.288381 231.253333 427.690667 152.697905 442.514286 73.142857z"
                                p-id="5386" :fill="item.sort === 1 ? '#eab308' : (item.sort === 2 ? '#94a3b8' : '#ea580c')"></path>
                        </svg>
                    </div>
                </div>
            </div>
        </div>

        <!-- 排名列表 -->
        <div class="items flex flex-col gap-2.5 px-4 pb-4 grow">
            <div class="group flex justify-between items-center bg-gray-50/40 hover:bg-linear-to-r hover:from-blue-50/50 hover:to-indigo-50/30 p-2.5 rounded-xl border border-transparent hover:border-blue-100/50 transition-all duration-200 cursor-pointer" 
                v-for="ranker in restRankers" :key="ranker.id">
                <div class="left flex-2 truncate flex justify-start items-center gap-3">
                    <div class="w-auto flex items-center gap-2">
                        <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs shrink-0 text-gray-400 bg-gray-100 transition-transform duration-200 group-hover:scale-105">
                            {{ ranker.sort }}
                        </span>
                        <img class="size-10 rounded-full border border-gray-100 group-hover:border-blue-200/50 transition-colors" :src="ranker.avatar" alt="">
                    </div>
                    <div class="flex flex-col min-w-0">
                        <div class="text-sm font-semibold text-gray-700 group-hover:text-blue-600 transition-colors duration-150 truncate">{{ ranker.nickname }}</div>
                        <div class="text-gray-400 text-[10px] font-medium mt-0.5">{{ ranker.ip || '未知地区' }}</div>
                    </div>
                </div>
                <div class="right flex items-center justify-end px-2 py-0.5 bg-red-50/40 rounded-full text-red-500 text-xs font-semibold border border-red-100/20 shrink-0 gap-0.5">
                    <svg t="1780575765923" class="icon size-3.5 fill-red-500" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="5385">
                        <path
                            d="M442.514286 73.142857c82.529524 64.24381 140.239238 126.610286 173.129143 187.099429 31.158857 57.295238 43.666286 115.907048 37.546666 175.835428l-1.219047 9.996191 6.095238-4.973715a174.055619 174.055619 0 0 0 49.249524-69.607619l2.681904-7.411809 7.704381-23.04c82.285714 55.734857 123.440762 150.064762 123.440762 283.062857C841.142857 823.515429 665.795048 950.857143 521.654857 950.857143c-144.11581 0-308.224-85.333333-334.750476-263.875048-26.550857-178.541714 83.480381-261.90019 158.427429-378.197333C395.288381 231.253333 427.690667 152.697905 442.514286 73.142857z"
                            p-id="5386"></path>
                    </svg>
                    <span class="font-bold text-[10px]">{{ formatNum(Number(ranker.score)) }}</span>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import { formatNum } from '@/utils/page';
import type { MappedCreatorRank } from '@/services/rank/types';

const rankList = defineModel<MappedCreatorRank[]>();

const topThree = computed(() => {
    if (!rankList.value) return [];
    return [...rankList.value].slice(0, 3);
});

const restRankers = computed(() => {
    if (!rankList.value) return [];
    return rankList.value.slice(3);
});
</script>