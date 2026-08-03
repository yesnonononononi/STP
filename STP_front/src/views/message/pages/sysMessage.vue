<template>
    <div v-if="noticeList && noticeList.length !== 0" class="w-full h-full flex flex-col gap-3 p-2 overflow-y-auto">
        <div class="rounded-xl bg-white shadow-md p-4 flex flex-col gap-2" v-for="notice in noticeList"
            :key="notice.id">
            <span class="text-gray-800 text-sm font-semibold" v-html="notice.content"></span>
            <div class="w-full flex flex-wrap gap-2">
                <el-image v-for="(image, index) in notice.images" :key="index" :src="image" class="size-24"
                    referrerpolicy="no-referrer"></el-image>
            </div>
            <span class="text-gray-400 text-xs mt-1">{{ TimeUtils.timestampToDate(notice.publicTime) }}</span>
        </div>
    </div>
    <div v-else class="flex bg-white w-full h-full items-center justify-center">
        <span class="text-gray-400   text-xl">暂无更多消息</span>
    </div>
</template>
<script lang="ts" setup>
import { MessageAPI, type sysNotice } from '@/services/message/message';
import { TimeUtils } from '@/utils/time';
import { onMounted, ref } from 'vue';
const props = defineProps<{
    loading: boolean;
}>();;
const noticeList = defineModel<sysNotice[]>();
</script>