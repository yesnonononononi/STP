<template>
    <div class="flex flex-col gap-2 grow overflow-y-auto">
        <!-- 前三名领奖台 (横向拟物挂壁重叠版) -->
        <div v-if="firstPlace"
            class="tab w-full max-w-105 mx-auto mt-12 mb-3 flex items-end justify-center gap-3 px-4 select-none relative z-10">
            <!-- 第二名 (左侧，稍微矮一点) -->
            <div v-if="secondPlace"
                class="flex-1 max-w-36 pt-8 pb-3 px-3 bg-gradient-to-b from-sky-50/80 via-white/50 to-transparent border border-sky-100 rounded-xl shadow-xs text-center flex flex-col items-center relative transition-all duration-300 hover:-translate-y-1">
                <div class="absolute -top-7 left-1/2 -translate-x-1/2 flex flex-col items-center">
                    <div class="w-14 h-14 relative">
                        <div
                            class="w-[48px] h-[48px] absolute top-[8px] left-1/2 -translate-x-1/2 rounded-full bg-slate-200/60   z-1 overflow-hidden">
                            <img v-if="secondPlace.avatar" :src="secondPlace.avatar" alt=""
                                class="w-full h-full object-cover opacity-0 transition-opacity duration-300 cursor-pointer"
                                @load="e => (e.target as HTMLImageElement).classList.remove('opacity-0')"
                                @error="e => { (e.target as HTMLImageElement).style.display = 'none' }"
                                @click="goToUserProfile(secondPlace.id)">
                        </div>
                        <svg class="absolute inset-0 w-full h-full text-slate-400 z-10 pointer-events-none"
                            viewBox="0 0 1024 1024">
                            <use href="#rank-avatar-frame-v2" />
                        </svg>
                    </div>
                </div>
                <div class="text-xs font-bold truncate max-w-full text-center text-slate-800 cursor-pointer hover:text-blue-500 transition-colors"
                    @click="goToUserProfile(secondPlace.id)">
                    {{ secondPlace.nickname }}
                </div>
                <div class="text-[8.5px] text-gray-400 text-center font-medium mt-1 leading-none truncate max-w-full">{{
                    secondPlace.ip || '未知地区' }}</div>
                <div
                    class="flex items-center gap-0.5 mt-2 px-2 py-0.5 rounded-full border border-sky-100/50 bg-sky-50/50 text-sky-700 text-[10px] font-bold">
                    <span>{{ formatNum(Number(secondPlace.score)) }}</span>
                    <svg class="size-3 fill-sky-500" viewBox="0 0 1024 1024">
                        <path
                            d="M442.514286 73.142857c82.529524 64.24381 140.239238 126.610286 173.129143 187.099429 31.158857 57.295238 43.666286 115.907048 37.546666 175.835428l-1.219047 9.996191 6.095238-4.973715a174.055619 174.055619 0 0 0 49.249524-69.607619l2.681904-7.411809 7.704381-23.04c82.285714 55.734857 123.440762 150.064762 123.440762 283.062857C841.142857 823.515429 665.795048 950.857143 521.654857 950.857143c-144.11581 0-308.224-85.333333-334.750476-263.875048-26.550857-178.541714 83.480381-261.90019 158.427429-378.197333C395.288381 231.253333 427.690667 152.697905 442.514286 73.142857z">
                        </path>
                    </svg>
                </div>
            </div>
            <!-- 虚位以待 -->
            <div v-else
                class="flex-1 max-w-36 pt-8 pb-3 px-3 bg-gradient-to-b from-gray-50/80 via-white/50 to-transparent border border-gray-100 rounded-xl shadow-xs text-center flex flex-col items-center relative">
                <div class="absolute -top-7 left-1/2 -translate-x-1/2">
                    <div
                        class="size-11 rounded-full p-0.5 bg-gray-100 border border-gray-200 shadow-sm flex items-center justify-center">
                        <svg class="size-5 text-gray-300" viewBox="0 0 24 24" fill="currentColor">
                            <path
                                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                        </svg>
                    </div>
                </div>
                <span class="text-[9px] px-1.5 py-0.5 rounded bg-gray-50 text-gray-400 font-semibold mb-2">虚位以待</span>
                <span class="text-xs font-bold text-gray-300">- -</span>
            </div>

            <!-- 第一名 (居中，最高，稍微放大) -->
            <div v-if="firstPlace"
                class="flex-1 max-w-38 pt-10 pb-4 px-3 bg-gradient-to-b from-amber-50 to-white/70 border-2 border-amber-200 rounded-2xl shadow-sm text-center flex flex-col items-center scale-105 z-10 transition-all duration-300 hover:-translate-y-1">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 flex flex-col items-center">
                    <div class="w-16 h-16 relative">
                        <div
                            class="w-[55px] h-[55px] absolute top-[9px] left-1/2 -translate-x-1/2 rounded-full bg-slate-200/60  z-1 overflow-hidden">
                            <img v-if="firstPlace.avatar" :src="firstPlace.avatar" alt=""
                                class="w-full h-full object-cover opacity-0 transition-opacity duration-300 cursor-pointer"
                                @load="e => (e.target as HTMLImageElement).classList.remove('opacity-0')"
                                @error="e => { (e.target as HTMLImageElement).style.display = 'none' }"
                                @click="goToUserProfile(firstPlace.id)">
                        </div>
                        <svg class="absolute inset-0 w-full h-full text-amber-500 z-10 pointer-events-none"
                            viewBox="0 0 1024 1024">
                            <use href="#rank-avatar-frame-v2" />
                        </svg>
                    </div>
                </div>
                <div class="text-sm font-bold truncate max-w-full text-center text-amber-950 cursor-pointer hover:text-amber-600 transition-colors"
                    @click="goToUserProfile(firstPlace.id)">
                    {{ firstPlace.nickname }}
                </div>
                <div class="text-[9px] text-gray-400 text-center font-medium mt-1 leading-none truncate max-w-full">{{
                    firstPlace.ip || '未知地区' }}</div>
                <div
                    class="flex items-center gap-0.5 mt-2 px-2.5 py-0.5 rounded-full border border-amber-200 bg-amber-100/60 text-amber-700 text-xs font-bold shadow-2xs">
                    <span>{{ formatNum(Number(firstPlace.score)) }}</span>
                    <svg class="size-3.5 fill-amber-500" viewBox="0 0 1024 1024">
                        <path
                            d="M442.514286 73.142857c82.529524 64.24381 140.239238 126.610286 173.129143 187.099429 31.158857 57.295238 43.666286 115.907048 37.546666 175.835428l-1.219047 9.996191 6.095238-4.973715a174.055619 174.055619 0 0 0 49.249524-69.607619l2.681904-7.411809 7.704381-23.04c82.285714 55.734857 123.440762 150.064762 123.440762 283.062857C841.142857 823.515429 665.795048 950.857143 521.654857 950.857143c-144.11581 0-308.224-85.333333-334.750476-263.875048-26.550857-178.541714 83.480381-261.90019 158.427429-378.197333C395.288381 231.253333 427.690667 152.697905 442.514286 73.142857z">
                        </path>
                    </svg>
                </div>
            </div>

            <!-- 第三名 (右侧，最矮) -->
            <div v-if="thirdPlace"
                class="flex-1 max-w-36 pt-8 pb-3 px-3 bg-gradient-to-b from-orange-50/80 via-white/50 to-transparent border border-orange-100 rounded-xl shadow-xs text-center flex flex-col items-center relative transition-all duration-300 hover:-translate-y-1">
                <div class="absolute -top-7 left-1/2 -translate-x-1/2 flex flex-col items-center">
                    <div class="w-14 h-14 relative">
                        <div
                            class="w-[48px] h-[48px] absolute top-[8px] left-1/2 -translate-x-1/2 rounded-full bg-slate-200/60   z-1 overflow-hidden">
                            <img v-if="thirdPlace.avatar" :src="thirdPlace.avatar" alt=""
                                class="w-full h-full object-cover opacity-0 transition-opacity duration-300 cursor-pointer"
                                @load="e => (e.target as HTMLImageElement).classList.remove('opacity-0')"
                                @error="e => { (e.target as HTMLImageElement).style.display = 'none' }"
                                @click="goToUserProfile(thirdPlace.id)">
                        </div>
                        <svg class="absolute inset-0 w-full h-full text-orange-500 z-10 pointer-events-none"
                            viewBox="0 0 1024 1024">
                            <use href="#rank-avatar-frame-v2" />
                        </svg>
                    </div>
                </div>
                <div class="text-xs font-bold truncate max-w-full text-center text-orange-950 cursor-pointer hover:text-amber-700 transition-colors"
                    @click="goToUserProfile(thirdPlace.id)">
                    {{ thirdPlace.nickname }}
                </div>
                <div class="text-[8.5px] text-gray-400 text-center font-medium mt-1 leading-none truncate max-w-full">{{
                    thirdPlace.ip || '未知地区' }}</div>
                <div
                    class="flex items-center gap-0.5 mt-2 px-2 py-0.5 rounded-full border border-orange-100/50 bg-orange-50/50 text-orange-700 text-[10px] font-bold">
                    <span>{{ formatNum(Number(thirdPlace.score)) }}</span>
                    <svg class="size-3 fill-orange-500" viewBox="0 0 1024 1024">
                        <path
                            d="M442.514286 73.142857c82.529524 64.24381 140.239238 126.610286 173.129143 187.099429 31.158857 57.295238 43.666286 115.907048 37.546666 175.835428l-1.219047 9.996191 6.095238-4.973715a174.055619 174.055619 0 0 0 49.249524-69.607619l2.681904-7.411809 7.704381-23.04c82.285714 55.734857 123.440762 150.064762 123.440762 283.062857C841.142857 823.515429 665.795048 950.857143 521.654857 950.857143c-144.11581 0-308.224-85.333333-334.750476-263.875048-26.550857-178.541714 83.480381-261.90019 158.427429-378.197333C395.288381 231.253333 427.690667 152.697905 442.514286 73.142857z">
                        </path>
                    </svg>
                </div>
            </div>
            <!-- 虚位以待 -->
            <div v-else
                class="flex-1 max-w-36 pt-8 pb-3 px-3 bg-gradient-to-b from-gray-50/80 via-white/50 to-transparent border border-gray-100 rounded-xl shadow-xs text-center flex flex-col items-center relative">
                <div class="absolute -top-7 left-1/2 -translate-x-1/2">
                    <div
                        class="size-11 rounded-full p-0.5 bg-gray-100 border border-gray-200 shadow-sm flex items-center justify-center">
                        <svg class="size-5 text-gray-300" viewBox="0 0 24 24" fill="currentColor">
                            <path
                                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                        </svg>
                    </div>
                </div>
                <span class="text-[9px] px-1.5 py-0.5 rounded bg-gray-50 text-gray-400 font-semibold mb-2">虚位以待</span>
                <span class="text-xs font-bold text-gray-300">- -</span>
            </div>
        </div>

        <!-- 隐藏的 SVG 图标库，用于复用头像边框路径以满足禁止代码行重复的约束 -->
        <svg style="display: none;">
            <g id="rank-avatar-frame-v2">
                <!-- 第一条路径：内侧遮罩圈 -->
                <path
                    d="M512 582.62069m-441.37931 0a441.37931 441.37931 0 1 0 882.75862 0 441.37931 441.37931 0 1 0-882.75862 0Z"
                    fill="none"></path>
                <!-- 第二条路径：外圈大环 -->
                <path
                    d="M512 1024C268.358621 1024 70.62069 826.262069 70.62069 582.62069S268.358621 141.241379 512 141.241379 953.37931 338.97931 953.37931 582.62069 755.641379 1024 512 1024z m0-847.448276C287.77931 176.551724 105.931034 358.4 105.931034 582.62069s181.848276 406.068966 406.068966 406.068965 406.068966-181.848276 406.068966-406.068965S736.22069 176.551724 512 176.551724z"
                    fill="currentColor"></path>
                <!-- 第三条路径：王冠和星星装饰 -->
                <path
                    d="M384.882759 160.662069L370.758621 75.917241l31.77931 17.655173 30.013793-33.544828 42.372414 22.951724 38.841379-45.903448 38.84138 45.903448 42.372413-22.951724 30.013793 33.544828 31.779311-17.655173-14.124138 82.979311C600.275862 148.303448 556.137931 141.241379 512 141.241379s-88.275862 7.062069-127.117241 19.42069z m174.786207-67.089655l21.186206 26.482758 30.013794-17.655172-21.186207-24.717241-30.013793 15.889655z m-95.337932 0l-31.77931-15.889655-21.186207 24.717241 30.013793 17.655172 22.951724-26.482758z m61.793104-79.448276c0 7.062069-5.296552 14.124138-14.124138 14.124138-7.062069 0-14.124138-7.062069-14.124138-14.124138s5.296552-14.124138 14.124138-14.124138c7.062069 0 14.124138 7.062069 14.124138 14.124138z"
                    fill="currentColor"></path>
            </g>
        </svg>

        <!-- 排名列表 -->
        <div class="items flex flex-col gap-2.5 px-4 pb-4 grow">
            <div class="group flex justify-between items-center bg-gray-50/40 hover:bg-linear-to-r hover:from-blue-50/50 hover:to-indigo-50/30 p-2.5 rounded-xl border border-transparent hover:border-blue-100/50 transition-all duration-200 cursor-pointer"
                v-for="ranker in restRankers" :key="ranker.id" @click="goToUserProfile(ranker.id)">
                <div class="left flex-2 truncate flex justify-start items-center gap-3">
                    <div class="w-auto flex items-center gap-2">
                        <span
                            class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs shrink-0 text-gray-400 bg-gray-100 transition-transform duration-200 group-hover:scale-105">
                            {{ ranker.sort }}
                        </span>
                        <div
                            class="size-10 rounded-full border border-gray-100 group-hover:border-blue-200/50 transition-colors bg-slate-200/60 animate-pulse overflow-hidden">
                            <img v-if="ranker.avatar"
                                class="w-full h-full object-cover opacity-0 transition-opacity duration-300"
                                :src="ranker.avatar" alt=""
                                @load="e => (e.target as HTMLImageElement).classList.remove('opacity-0')"
                                @error="e => { (e.target as HTMLImageElement).style.display = 'none' }">
                        </div>
                    </div>
                    <div class="flex flex-col min-w-0">
                        <div
                            class="text-sm font-semibold text-gray-700 group-hover:text-blue-600 transition-colors duration-150 truncate">
                            {{ ranker.nickname }}</div>
                        <div class="text-gray-400 text-[10px] font-medium mt-0.5">{{ ranker.ip || '未知地区' }}</div>
                    </div>
                </div>
                <div
                    class="right flex items-center justify-end px-2 py-0.5 bg-red-50/40 rounded-full text-red-500 text-xs font-semibold border border-red-100/20 shrink-0 gap-0.5">
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
import { useRouter } from 'vue-router';
import { formatNum } from '@/utils/page';
import type { MappedCreatorRank } from '@/services/rank/types';

const router = useRouter();
const rankList = defineModel<MappedCreatorRank[]>();

const firstPlace = computed(() => {
    if (!rankList.value) return null;
    return rankList.value.find(item => item.sort === 1) || null;
});

const secondPlace = computed(() => {
    if (!rankList.value) return null;
    return rankList.value.find(item => item.sort === 2) || null;
});

const thirdPlace = computed(() => {
    if (!rankList.value) return null;
    return rankList.value.find(item => item.sort === 3) || null;
});

const restRankers = computed(() => {
    if (!rankList.value) return [];
    return rankList.value.slice(3);
});

function goToUserProfile(userId: string | number) {
    if (!userId) return;
    router.push({ name: 'userProfile', params: { id: String(userId) } });
}
</script>