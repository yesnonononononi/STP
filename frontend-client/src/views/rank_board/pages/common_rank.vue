<template>
    <div class="relative flex flex-col gap-2.5 overflow-y-auto px-3 py-1 grow">
        <div v-for="item in rankList" :key="item.sort" 
            @click="handleItemClick(item)"
            class="group flex justify-between items-center bg-gray-50/40 hover:bg-linear-to-r hover:from-blue-50/50 hover:to-indigo-50/30 p-2.5 rounded-xl border border-transparent hover:border-blue-100/50 transition-all duration-200 cursor-pointer">
            <div class="flex items-center gap-3 truncate grow">
                <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs shrink-0 transition-transform duration-200 group-hover:scale-105"
                    :class="[
                        item.sort === 1 ? 'bg-red-500 text-white shadow-[0_2px_8px_rgba(239,68,68,0.35)]' :
                        item.sort === 2 ? 'bg-slate-400 text-white shadow-[0_2px_8px_rgba(148,163,184,0.35)]' :
                        item.sort === 3 ? 'bg-amber-500 text-white shadow-[0_2px_8px_rgba(245,158,11,0.35)]' :
                        'text-gray-400 bg-gray-100'
                    ]">
                    {{ item.sort }}
                </span>
                <span class="text-gray-700 text-sm font-medium group-hover:text-blue-600 transition-colors duration-150 truncate">{{ item.title }}</span>
            </div>
            <div class="flex items-center justify-end px-2 py-0.5 bg-red-50/40 rounded-full text-red-500 text-xs font-semibold border border-red-100/20 shrink-0 gap-0.5">
                <svg t="1780565746914" class="icon size-3.5 fill-red-500" viewBox="0 0 1024 1024" version="1.1"
                    xmlns="http://www.w3.org/2000/svg" p-id="5170">
                    <path
                        d="M413.162667 42.666667c143.850667 74.986667 265.301333 213.418667 278.741333 371.733333 41.045333-30.4 77.013333-73.066667 77.013333-122.325333 121.856 109.333333 126.08 245.589333 126.976 285.098666l0.106667 7.082667c0 22.272-1.685333 44.885333-5.226667 67.648-23.146667 142.997333-121.066667 259.968-251.072 307.072l-1.578666 0.554667c-20.864 6.912-43.52 12.522667-68.16 16.405333a432.213333 432.213333 0 0 1-62.976 5.376c-170.709333-2.24-346.688-114.090667-373.845334-278.101333-21.973333-149.034667 0-319.210667 172.906667-484.821334l4.010667 25.493334c7.36 45.909333 16.256 93.632 29.589333 121.429333l13.056-24.106667 6.421333-12.16 6.485334-12.586666c37.333333-73.813333 66.005333-154.944 47.552-273.792z"></path>
                </svg>
                <span class="font-bold text-[10px]">{{ formatNum(item.count || 0) }}</span>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {formatNum} from '@/utils/page';
import {ref} from 'vue';
import type {MappedCommonRank} from '@/services/rank/types';

const rankList = defineModel<MappedCommonRank[]>()
const loading = ref(rankList.value && rankList.value.length === 0)

const emit = defineEmits<{
    (e: 'clickItem', item: MappedCommonRank): void
}>()

function handleItemClick(item: MappedCommonRank) {
    emit('clickItem', item)
}

function calculateColor(sort: number | string) {
    sort = Number(sort)
    if (sort === 1) {
        return 'text-red-500'
    }
    if (sort === 2) {
        return 'text-slate-400'
    }
    if (sort === 3) {
        return 'text-yellow-600'
    }
}
</script>