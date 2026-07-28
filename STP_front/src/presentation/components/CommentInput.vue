<template>
    <div class="reply p-2 w-full flex flex-col gap-2 mt-4 bg-gray-100">
        <div>
            <span class="text-xl font-bold">留下你的评论</span>
        </div>
        <div class="flex items-center my-2 gap-3 overflow-x-auto" @click="handleInputClick">
            <span class="text-gray-600">快捷表情</span>
            <span class="cursor-pointer hover:bg-gray-300" v-html="emoji.url" @click="isLoggedIn && insertEmoji(emoji)"
                v-for="(emoji, index) in emojiList.slice(0, Math.min(emojiList.length, 10))" :key="index">
            </span>
        </div>
        <el-input ref="inputRef" type="textarea" :rows="3" v-model="model" 
            :placeholder="isLoggedIn ? '恶语结恶缘,善语暖人心' : '请先登录以发表评论...'"
            :readonly="!isLoggedIn"
            @click="handleInputClick"
            @keydown.enter="handleKeyDown" resize="none" />
        <div class="image-upload min-h-20 w-full mb-2" v-if="fileList.length != 0">
            <el-upload v-model:file-list="fileList" list-type="picture-card" :limit="6" :auto-upload="false"
                :on-change="handleChange">
                <el-icon>
                    <Plus />
                </el-icon>
            </el-upload>
        </div>
        <div class="flex items-center justify-between p-4 relative">
            <div class="flex items-center gap-8 " @click="handleInputClick">
                <el-upload class="flex items-center justify-center cursor-pointer" v-model:file-list="fileList"
                    :disabled="!isLoggedIn" :auto-upload="false" :show-file-list="false" accept="image/*,video/*,audio/*">
                    <template #trigger>
                        <div
                            class="flex items-center justify-center gap-2 cursor-pointer text-gray-600 rounded-md hover:text-blue-300">
                            <svg t="1780462240406" class="icon w-5 h-5" viewBox="0 0 1024 1024" version="1.1"
                                xmlns="http://www.w3.org/2000/svg" p-id="5015">
                                <path
                                    d="M831.792397 82.404802 191.548594 82.404802c-60.676941 0-110.042255 49.364291-110.042255 110.042255l0 640.245849c0 60.677964 49.364291 110.042255 110.042255 110.042255l640.244826 0c60.677964 0 110.042255-49.364291 110.042255-110.042255L941.835675 192.447057C941.834652 131.769093 892.470361 82.404802 831.792397 82.404802zM191.548594 122.420167l640.244826 0c38.612413 0 70.02689 31.414477 70.02689 70.02689l0 134.349871c-144.759965 4.953825-280.06151 63.59234-382.864898 166.396751-48.28061 48.28061-86.814228 103.732549-114.628714 163.962306-80.588433-68.744687-197.638289-73.051783-282.803971-12.938684L121.522728 192.447057C121.521704 153.834644 152.935158 122.420167 191.548594 122.420167zM121.521704 832.691883l0-136.601144c74.040297-72.025407 192.529945-71.925123 266.451538 0.301875-23.496134 62.998823-35.762505 130.383536-35.762505 199.672622 0 2.336208 0.420579 4.569062 1.157359 6.652514L191.548594 902.717749C152.935158 902.718773 121.521704 871.304296 121.521704 832.691883zM831.792397 902.718773 391.068743 902.718773c0.735757-2.084475 1.157359-4.317329 1.157359-6.652514 0-141.581576 55.054897-274.608312 155.023726-374.578164 95.245248-95.245248 220.499973-149.720953 354.570481-154.655336l0 465.860147C901.819287 871.304296 870.40481 902.718773 831.792397 902.718773z"
                                    fill="#707070" p-id="5016"></path>
                                <path
                                    d="M349.471346 477.533001c75.04723 0 136.102794-61.054541 136.102794-136.101771s-61.055564-136.102794-136.102794-136.102794-136.102794 61.055564-136.102794 136.102794S274.424116 477.533001 349.471346 477.533001zM349.471346 245.343801c52.982702 0 96.087429 43.104727 96.087429 96.087429 0 52.982702-43.104727 96.087429-96.087429 96.087429-52.982702 0-96.087429-43.104727-96.087429-96.087429C253.383918 288.448528 296.488645 245.343801 349.471346 245.343801z"
                                    fill="#707070" p-id="5017"></path>
                            </svg>
                            图片/媒体
                        </div>
                    </template>
                </el-upload>

                <emoji v-model="model" :place-holder="'表情'" :textarea-ref="inputRef" :disabled="!isLoggedIn" />
            </div>
            <span
                class="w-16 text-center bg-linear-to-r from-blue-200 via-blue-300 to-blue-400 rounded-lg p-1 select-none"
                :class="(isSubmitting || !isLoggedIn) ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer hover:scale-105'"
                @click="handlePublishClick">{{ isSubmitting ? '发表中' : '发表' }}</span>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, watch, computed } from 'vue';
import { useEmoji } from './composables/useEmoji';
import { useInputText } from './composables/useInputText';
import Emoji from './emoji.vue';
import { Plus } from '@element-plus/icons-vue';
import { useAuthStore } from '@/views/auth/store';
import { log } from '@/utils/log';
import router from '@/router';

const model = defineModel<string>({ required: true })
const fileList = ref<any[]>([])
const props = defineProps<{
    publish: (e: any, parentId?: number | string, rootId?: number | string, files?: any[]) => Promise<any>
}>();

const isSubmitting = ref(false)

const {
    publish: basePublish,
    inputRef
} = useInputText(model, async (e: any) => {
    if (isSubmitting.value) return;
    isSubmitting.value = true;
    try {
        await props.publish(e, undefined, undefined, fileList.value);
        fileList.value = [];
    } catch (err) {
        console.error('发表评论失败:', err);
    } finally {
        isSubmitting.value = false;
    }
});

const authStore = useAuthStore();
const isLoggedIn = computed(() => !!authStore.token);

function handleInputClick() {
    if (!isLoggedIn.value) {
        log.warning('请先登录后发表评论');
        authStore.showLoginDialog();
    }
}

function handlePublishClick(e: any) {
    if (!isLoggedIn.value) {
        log.warning('请先登录后发表评论');
        authStore.showLoginDialog();
        return;
    }
    publish(e);
}

function publish(e: any) {
    basePublish(e);
}

const {
    emojiList,
    insertEmoji,
} = useEmoji(model, inputRef);
function handleChange(file: any) { }

watch(fileList, (newVal) => {
    newVal.forEach((file) => {
        if (!file.url && file.raw) {
            file.url = URL.createObjectURL(file.raw);
        }
    });
}, { deep: true });

function handleKeyDown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        publish(e)
    }
}
</script>

<style scoped>
:deep(.el-upload--picture-card) {
    width: 72px;
    height: 72px;
    line-height: 70px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-thumbnail) {
    width: 72px;
    height: 72px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-actions) {
    height: 72px;
    width: 72px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
    height: 72px;
    width: 72px;
}
</style>
