<template>
    <div class="w-screen relative min-h-screen bg-linear-to-bl from-blue-200 via-blue-100 to-blue-300 ">
        <div class="tab sticky top-px left-0 right-0 h-12 bg-white   shadow-md flex items-center p-1 ">
            <span class="m-2 flex items-center gap-4 cursor-pointer " @click="router.push('/')">
                <img class="size-8" src="/logo.png" alt="">
                <span
                    class="bg-clip-text text-2xl font-semibold text-transparent bg-linear-to-r from-blue-200 via-blue-100 to-blue-300">STP</span>
            </span>
            <span class="m-4">
                <span class="text-gray-200 mr-2">|</span>
                发动态 >
            </span>
        </div>
        <div class="body w-full   md:w-1/2 m-auto bg-white  rounded-md shadow-md mt-2 md:h-auto flex flex-col relative">
            <input v-model="form.title"
                class=" flex-1 max-h-10 border-b w-full p-6 border-gray-300 font-semibold text-md md:text-2xl outline-none"
                maxlength="50" placeholder="好的标题会获得更多曝光欧~" />
            <div class="set-content flex-1   p-4 pb-1 flex flex-col gap-2  mt-2">
                <div class="relative w-full">
                    <el-input ref="textareaRef" type="textarea" :rows="8" max="400" autosize placeholder="此刻你想说什么"
                        class="custom-textarea w-full max-h-2/3 " v-model="userPostContent"
                        @keydown="handleTextareaKeydown" />
                    <!-- 光标下方弹出的话题选择器 -->
                    <teleport to="body">
                        <div v-if="visibleCaretSelector"
                            :style="{ position: 'absolute', left: popupPos.x + 'px', top: popupPos.y + 'px', zIndex: 9999 }">
                            <TopicTagSelector :visible="visibleCaretSelector" :new-tag="newTag || null"
                                @add-tag="handleAddTag" @click.stop />
                        </div>
                    </teleport>
                </div>

                <div class="selected-tags flex gap-2 flex-wrap mb-2" v-if="selectedTags.length > 0">
                    <el-tag v-for="tag in selectedTags" :key="tag.id" closable @close="removeTag(tag)" type="primary"
                        effect="light">
                        #{{ tag.tagName }}
                    </el-tag>
                </div>

                <div class="file flex gap-2 ">
                    <el-upload class="w-auto" action="#" list-type="picture-card" :limit="10" :auto-upload="false"
                        multiple :file-list="fileList" :on-change="handleChange" :on-remove="handleRemove"
                        accept=".png,.jpg,.mp4,.jpeg">
                        <el-icon>
                            <Plus />
                        </el-icon>
                        <template #file="{ file }">
                            <div
                                class="w-full h-full relative flex items-center justify-center bg-black rounded-lg overflow-hidden group">
                                <!-- 视频预览 -->
                                <video v-if="isVideo(file)" class="w-full h-full object-cover" :src="file.url"></video>
                                <!-- 图片预览 -->
                                <img v-else-if="file.url" class="w-full h-full object-cover" :src="file.url" alt="" />
                                <!-- Hover 动作条 -->
                                <span
                                    class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-4 text-white z-10">
                                    <span class="cursor-pointer hover:text-blue-300 text-lg"
                                        @click="handlePictureCardPreview(file)">
                                        <el-icon>
                                            <ZoomIn />
                                        </el-icon>
                                    </span>
                                    <span class="cursor-pointer hover:text-red-400 text-lg" @click="handleRemove(file)">
                                        <el-icon>
                                            <Delete />
                                        </el-icon>
                                    </span>
                                </span>
                            </div>
                        </template>
                    </el-upload>
                    <span :class="uploadError ? 'text-red-400' : 'text-gray-400'"
                        class=" ml-4 mt-4 inline-block align-middle">{{
                            uploadError ||
                            `${fileList.length}/10` }}
                    </span>
                </div>
                <div class="set-page/video w-full flex items-center gap-2 justify-between">
                    <div class="flex items-center gap-2">
                        <div class="relative z-10 min-w-30  ">
                            <span
                                class="cursor-pointer rounded-lg hover:bg-gray-200 px-1 shrink-0 font-semibold flex items-center "
                                @click.stop="handleSelectTopicClick">
                                请选择话题
                                <svg :class="visibleLocalSelector ? '-rotate-90' : ''" t="1780036219758"
                                    class="icon w-4 h-4" viewBox="0 0 1024 1024" version="1.1"
                                    xmlns="http://www.w3.org/2000/svg" p-id="5990">
                                    <path
                                        d="M878.592 250.88q29.696 0 48.128 11.264t24.576 29.696 0 41.472-26.624 45.568q-82.944 92.16-159.744 180.224t-148.48 164.864q-19.456 20.48-45.568 31.744t-53.76 11.776-53.248-8.704-43.008-28.672q-39.936-44.032-82.944-90.112l-88.064-92.16q-43.008-46.08-85.504-90.624t-79.36-86.528q-17.408-19.456-22.528-40.448t1.024-38.4 23.552-28.672 45.056-11.264q35.84 0 98.816-0.512t137.728-0.512l153.6 0 150.528 0 125.952 0 79.872 0z"
                                        p-id="5991" fill="#707070"></path>
                                </svg>
                            </span>
                            <TopicTagSelector :visible="visibleLocalSelector" @add-tag="handleAddTag" @click.stop />
                        </div>
                        <span class=" text-blue-300 flex  shrink-0 items-center  cursor-pointer hover:text-blue-500"
                            v-for="tag in tagList?.slice(0, Math.min(tagList.length, 4))" :key="tag.id"
                            @click="toggleTag(tag)">
                            #{{ tag.tagName }}
                        </span>
                    </div>
                    <div class="flex items-center pr-2">
                        <svg t="1781687995848"
                            class="icon cursor-pointer w-6 h-6 text-gray-500 hover:text-blue-500 transition-colors"
                            viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5646"
                            width="200" height="200" @click="showSettingsModal = true">
                            <path
                                d="M439.264 208a16 16 0 0 0-16 16v67.968a239.744 239.744 0 0 0-46.496 26.896l-58.912-34a16 16 0 0 0-21.856 5.856l-80 138.56a16 16 0 0 0 5.856 21.856l58.896 34a242.624 242.624 0 0 0 0 53.728l-58.88 34a16 16 0 0 0-6.72 20.176l0.848 1.68 80 138.56a16 16 0 0 0 21.856 5.856l58.912-34a239.744 239.744 0 0 0 46.496 26.88V800a16 16 0 0 0 16 16h160a16 16 0 0 0 16-16v-67.968a239.744 239.744 0 0 0 46.512-26.896l58.912 34a16 16 0 0 0 21.856-5.856l80-138.56a16 16 0 0 0-4.288-20.832l-1.568-1.024-58.896-34a242.624 242.624 0 0 0 0-53.728l58.88-34a16 16 0 0 0 6.72-20.176l-0.848-1.68-80-138.56a16 16 0 0 0-21.856-5.856l-58.912 34a239.744 239.744 0 0 0-46.496-26.88V224a16 16 0 0 0-16-16h-160z m32 48h96v67.376l28.8 12.576c13.152 5.76 25.632 12.976 37.184 21.52l25.28 18.688 58.448-33.728 48 83.136-58.368 33.68 3.472 31.2a194.624 194.624 0 0 1 0 43.104l-3.472 31.2 58.368 33.68-48 83.136-58.432-33.728-25.296 18.688c-11.552 8.544-24.032 15.76-37.184 21.52l-28.8 12.576V768h-96v-67.376l-28.784-12.576c-13.152-5.76-25.632-12.976-37.184-21.52l-25.28-18.688-58.448 33.728-48-83.136 58.368-33.68-3.472-31.2a194.624 194.624 0 0 1 0-43.104l3.472-31.2-58.368-33.68 48-83.136 58.432 33.728 25.296-18.688a191.744 191.744 0 0 1 37.184-21.52l28.8-12.576V256z m47.28 144a112 112 0 1 0 0 224 112 112 0 0 0 0-224z m0 48a64 64 0 1 1 0 128 64 64 0 0 1 0-128z"
                                fill="#5A626A" p-id="5647"></path>
                        </svg>
                    </div>
                </div>
            </div>
            <div class="w-full h-12 flex items-center justify-end p-12">
                <span @click="submit(1)"
                    class="w-16 h-8 rounded-xl  bg-linear-to-r hover:scale-[1.05] cursor-pointer shadow-md from-blue-100 via-blue-200 to-blue-300 flex items-center justify-center">发布</span>
            </div>
            <PostVisibilitySettings :visible="showSettingsModal" :post="postFormVO" @close="showSettingsModal = false"
                @update-scope="form.visibleScope = $event" />
            <Loading v-model="submitting" prompt="正在发布..." bg-color="bg-white/70 rounded-md" />
        </div>

        <!-- 媒体预览弹窗 -->
        <el-dialog v-model="dialogVisible" width="50%" destroy-on-close align-center title="媒体预览">
            <div class="w-full flex items-center justify-center bg-black rounded-lg p-2 max-h-[60vh] overflow-hidden">
                <video v-if="previewVideoUrl" :src="previewVideoUrl" controls autoplay
                    class="max-w-full max-h-[55vh] object-contain"></video>
                <img v-else-if="previewImageUrl" :src="previewImageUrl" alt="Preview Image"
                    class="max-w-full max-h-[55vh] object-contain" />
            </div>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import router from '@/router';
import {Delete, Plus, ZoomIn} from '@element-plus/icons-vue';
import TopicTagSelector from '@/presentation/components/TopicTagSelector.vue';
import PostVisibilitySettings from '@/views/post/components/PostVisibilitySettings.vue';
import Loading from '@/presentation/components/loading.vue';
import {usePost} from '../composables/usePost';
import type {PostVO} from '@/services/post';

const showSettingsModal = ref(false);

const {
    form,
    userPostContent,
    selectedTags,
    tagList,
    fileList,
    uploadError,
    dialogVisible,
    previewImageUrl,
    previewVideoUrl,
    visibleLocalSelector,
    visibleCaretSelector,
    newTag,
    popupPos,
    textareaRef,
    handleAddTag,
    handleSelectTopicClick,
    handleTextareaKeydown,
    isVideo,
    handlePictureCardPreview,
    handleChange,
    handleRemove,
    submitting,
    submit,
    toggleTag,
    removeTag
} = usePost();

const postFormVO = computed(() => form.value as unknown as PostVO);
</script>

<style scoped>
.custom-textarea :deep(.el-textarea__inner) {
    outline: none !important;
    box-shadow: none !important;
    font-size: 1.0rem !important;
    line-height: 2rem !important;
    height: 100% !important;
    resize: none !important;
}

.custom-textarea :deep(.el-textarea__inner:focus) {
    outline: none !important;
    box-shadow: none !important;
}
</style>