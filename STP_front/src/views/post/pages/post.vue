<template>
    <div class="w-screen relative min-h-screen bg-linear-to-bl from-blue-200 via-blue-100 to-blue-300 ">
        <div class="tab sticky top-px left-0 right-0 h-12 bg-white   shadow-md flex items-center p-1 ">
            <span class="m-2 flex items-center gap-4 cursor-pointer " @click="router.push('/')">
                <img class="w-12 h-12" src="/logo.png" alt="">
                <span
                    class="bg-clip-text text-2xl font-semibold text-transparent bg-linear-to-r from-blue-200 via-blue-100 to-blue-300">STP</span>
            </span>
            <span class="m-4">
                <span class="text-gray-200 mr-2">|</span>
                发动态 >
            </span>
        </div>
        <div class="body w-full   md:w-1/2 m-auto bg-white  rounded-md shadow-md mt-2 md:h-auto flex flex-col  ">
            <input v-model="form.title"
                class="text-gray-400 flex-1 max-h-10 border-b w-full p-6 border-gray-300 font-semibold text-md md:text-2xl outline-none"
                maxlength="50" placeholder="好的标题会获得更多曝光欧~" />
            <div class="set-content flex-1   p-4 pb-1 flex flex-col gap-2  mt-2">
                <el-input type="textarea" :rows="8" max="400" autosize placeholder="此刻你想说什么"
                    class="custom-textarea w-full max-h-2/3 " v-model="userPostContent" />

                <div class="selected-tags flex gap-2 flex-wrap mb-2" v-if="selectedTags.length > 0">
                    <el-tag v-for="tag in selectedTags" :key="tag.id" closable @close="removeTag(tag)" type="primary"
                        effect="light">
                        #{{ tag.tagName }}
                    </el-tag>
                </div>

                <div class="file flex gap-2 ">
                    <el-upload class="w-auto" action="#" list-type="picture-card" :limit="10" :auto-upload="false"
                        :file-list="fileList" :on-change="handleChange" :on-remove="handleRemove">
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
                <div class="set-page/video w-full flex tems-center gap-2">
                    <div class="relative z-10">
                        <span class="cursor-pointer hover:text-blue-200 shrink-0 font-semibold">请选择话题</span>
                        <div class="absolute z-10 top-6 w-96 h-72 bg-blue-400/60 flex flex-col">
                            <div class="search"></div>
                            <div class="tab"></div>
                            <div class="selectItem w-full max-h-2/3 overflow-y-auto">
                                <div class="flex justify-between items-center">
                                    <div class="name"></div>
                                    <div class="like"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <span class="text-blue-300 cursor-pointer hover:text-blue-500" v-for="tag in tagList" :key="tag.id"
                        @click="toggleTag(tag)">
                        #{{ tag.tagName }}
                    </span>
                </div>
                <div class="extra w-full h-12 flex items-center gap-2">
                    <span class="flex items-center gap-2 cursor-pointer hover:text-blue-400">
                        <el-icon>
                            <Star />
                        </el-icon>
                        表情
                    </span>
                </div>
            </div>
            <div class="w-full h-12 flex items-center justify-end p-12">
                <span @click="submit(1)"
                    class="w-16 h-8 rounded-xl bg-linear-to-r hover:scale-[1.05] cursor-pointer shadow-md from-blue-100 via-blue-200 to-blue-300 flex items-center justify-center">发布</span>
            </div>
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
import router from '@/router';
import { Star, Plus, ZoomIn, Delete } from '@element-plus/icons-vue';
import { PostAPI, PostType, PostStatus, TagAPI, type CreatePostRequest, type TagPO, type TagVO } from '@/services/post';
import { CommonAPI } from '@/services/common/api';
import { onMounted, onUnmounted, ref, computed } from 'vue';
import { log } from '@/utils/log';
import { onBeforeRouteLeave, useRoute } from 'vue-router';
import { useUserInfoStore } from '@/stores/userInfo';
const route = useRoute();
const postId = route.query.postId as string;
const uploadError = ref('');
const dialogVisible = ref(false);
const previewImageUrl = ref('');
const previewVideoUrl = ref('');
const isSubmitSuccess = ref(false);
const user = useUserInfoStore().user;
const form = ref<CreatePostRequest>({
    title: '',
    type: PostType.TEXT,
    content: '',
    mediaUrls: [],
    status: PostStatus.NORMAL
});
const userPostContent = ref('');
const selectedTags = ref<TagVO[]>([]);
const tagList = ref<TagVO[]>();
const fileList = ref<any[]>([]);


onMounted(async () => {
    if (postId) {
        const res = await PostAPI.getById(Number(postId));
        const postData = res.data;

        // 复制基本属性，避免类型覆盖冲突
        form.value.title = postData.title;
        form.value.type = postData.type;
        form.value.content = postData.content || '';
        form.value.status = postData.status as PostStatus || PostStatus.NORMAL;

        userPostContent.value = postData.content || '';

        // 转换并回显媒体文件列表
        if (postData.type === PostType.IMAGE && postData.mediaUrls && Array.isArray(postData.mediaUrls)) {
            form.value.mediaUrls = postData.mediaUrls.map((item: any) => ({
                url: item.imageUrl || item.url,
                width: item.width,
                height: item.height
            }));
            fileList.value = postData.mediaUrls.map((item: any, index: number) => {
                return {
                    name: `file_${index}`,
                    url: item.imageUrl || item.url,
                    type: 'image/jpeg',
                    uid: Date.now() + index,
                    width: item.width,
                    height: item.height
                };
            });
        }
        else if (postData.type === PostType.VIDEO && postData.extraMediaUrl && fileList.value.length == 0) {
            fileList.value.push({
                name: ' video.mp4',
                url: postData.extraMediaUrl,
                type: 'video/mp4',
                uid: Date.now()
            });
            form.value.mediaUrls = [{
                url: postData.extraMediaUrl,
                width: 0,
                height: 0
            }];
        }
        else {
            form.value.mediaUrls = [];
            fileList.value = [];
        }

        if (postData.tags) {
            selectedTags.value = postData.tags;
        }
    }
    loadTags();
});

function isVideo(file: any): boolean {
    const rawFile = file.raw || file;
    if (rawFile.type) {
        return rawFile.type.startsWith('video/');
    }
    const name = rawFile.name || '';
    return /\.(mp4|webm|ogg|mov)$/i.test(name);
}

function handlePictureCardPreview(file: any) {
    previewImageUrl.value = '';
    previewVideoUrl.value = '';
    const url = file.url || (file.raw ? URL.createObjectURL(file.raw) : '');
    if (isVideo(file)) {
        previewVideoUrl.value = url;
    } else {
        previewImageUrl.value = url;
    }
    dialogVisible.value = true;
}



function handleChange(file: any, files: any[]) {
    const video = isVideo(file);
    if (fileList.value.length === 0) {
        form.value.type = video ? PostType.VIDEO : PostType.IMAGE;
    } else {
        if (form.value.type === PostType.VIDEO) {
            if (video) {
                log.warning("请勿上传多个视频文件");
            } else {
                log.warning("多种类型文件不能同时上传");
            }
            URL.revokeObjectURL(file.url);
            fileList.value = files.filter(f => f.uid !== file.uid);
            return;
        }
        if (form.value.type === PostType.IMAGE && video) {
            log.warning("多种类型文件不能同时上传");
            URL.revokeObjectURL(file.url);
            fileList.value = files.filter(f => f.uid !== file.uid);
            return;
        }
    }

    // 校验：文件大小
    const sizeMB = file.size / 1024 / 1024;
    if (video) {
        if (sizeMB > 50) {
            log.error(`视频文件 [${file.name}] 不能超过 50MB`);
            URL.revokeObjectURL(file.url);
            fileList.value = files.filter(f => f.uid !== file.uid);
            return;
        }
    } else {
        if (sizeMB > 10) {
            log.error(`图片文件 [${file.name}] 不能超过 10MB`);
            URL.revokeObjectURL(file.url);
            fileList.value = files.filter(f => f.uid !== file.uid);
            return;
        }
    }

    if (file.status === 'ready') {
        file.url = URL.createObjectURL(file.raw);
    }
    fileList.value = files;

}

function handleRemove(file: any) {
    if (file.url && file.url.startsWith('blob:')) {
        URL.revokeObjectURL(file.url);
    }
    fileList.value = fileList.value.filter((item) => item.uid !== file.uid);
    if (fileList.value.length === 0) {
        form.value.type = PostType.TEXT;
    }
}

const getImageDimensions = (file: File): Promise<{ width: number; height: number }> => {
    return new Promise((resolve) => {
        if (!file.type.startsWith('image/')) {
            resolve({ width: 0, height: 0 });
            return;
        }
        const img = new Image();
        const objectUrl = URL.createObjectURL(file);
        img.src = objectUrl;
        img.onload = () => {
            resolve({ width: img.naturalWidth, height: img.naturalHeight });
            URL.revokeObjectURL(objectUrl);
        };
        img.onerror = () => {
            resolve({ width: 0, height: 0 });
            URL.revokeObjectURL(objectUrl);
        };
    });
};

async function submit(status: PostStatus = PostStatus.NORMAL) {
    if (!beforeCreateCheck()) return;
    form.value!.status = status;
    form.value.tagIds = selectedTags.value.map(t => t.id).filter(id => id !== undefined) as number[];
    let res;
    if (fileList.value.length > 0) {
        uploadError.value = '';
        try {
            const uploadPromises = fileList.value.map(async (file) => {
                if (file.url && !file.url.startsWith('blob:')) {
                    return {
                        url: file.url,
                        width: file.width || 0,
                        height: file.height || 0
                    };
                }
                
                let dims = { width: 0, height: 0 };
                if (file.raw && file.raw.type.startsWith('image/')) {
                    dims = await getImageDimensions(file.raw);
                }

                //如果是视频且大小大于5mb
                if (file.raw.type.startsWith('video/') && file.size > 5 * 1024 * 1024) {
                    res = await CommonAPI.uploadLargeFile(file.raw, "post-media");
                } else {
                    res = await CommonAPI.upload(file.raw, "post-media");
                }
                if (res.code === 1 && res.data) {
                    return {
                        url: res.data.url,
                        width: dims.width,
                        height: dims.height
                    };
                } else {
                    throw new Error(res.errMsg || `${file.name} 上传失败`);
                }
            });

            const imageInfos = await Promise.all(uploadPromises);
            form.value.mediaUrls = imageInfos;

        } catch (err: any) {
            log.error('文件上传失败');
            console.error(err);
            return; // 拦截发帖
        }
    } else {
        form.value.type = PostType.TEXT;
        form.value.mediaUrls = [];
    }

    return PostAPI.create(form.value!).then(() => {
        if (status === PostStatus.NORMAL) {
            log.success('发布成功');
            isSubmitSuccess.value = true;
            router.push('/');
        } else {
            log.success('已自动保存至草稿');
        }
    });
}

function toggleTag(tag: TagVO) {
    const isExist = selectedTags.value.some(t => t.id === tag.id);
    if (!isExist) {
        selectTag(tag);
    } else {
        removeTag(tag);
    }
}

function selectTag(tag: TagVO) {
    if (!tag || !tag.id) return;
    if (selectedTags.value.some(t => t.id === tag.id)) return;
    selectedTags.value.push(tag);
}
async function loadTags() {
    tagList.value = (await TagAPI.getPage(1, 5)).data.records;
}
function removeTag(tag: TagVO) {
    if (!tag || !tag.id) return;
    selectedTags.value = selectedTags.value.filter(t => t.id !== tag.id);

    // 防御性清除可能没有带空格的话题文字
    userPostContent.value = userPostContent.value.replace(`#${tag.tagName}`, '').trim();
}

const beforeCreateCheck = () => {
    form.value.content = userPostContent.value;
    if (!form.value) {
        log.error('未输入任何内容');
        return false;
    } else if (!form.value.title) {
        log.error('请输入标题');
        return false;
    } else if (!form.value.content) {
        log.error('请输入内容');
        return false;
    }

    if (fileList.value.length > 10) {
        log.error('最多只能上传 10 个媒体文件');
        return false;
    }

    for (const file of fileList.value) {
        const rawFile = file.raw;
        if (!rawFile) continue;
        const isVideoFile = isVideo(file);
        const sizeMB = rawFile.size / 1024 / 1024;
        if (isVideoFile) {
            if (sizeMB > 50) {
                log.error(`视频文件 [${rawFile.name}] 不能超过 50MB`);
                return false;
            }
        } else {
            if (sizeMB > 10) {
                log.error(`图片文件 [${rawFile.name}] 不能超过 10MB`);
                return false;
            }
        }
    }
    return true;
}

const handleBeforeUnload = (e: BeforeUnloadEvent) => {
    if (isSubmitSuccess.value) return;
    const hasContent = (userPostContent.value && userPostContent.value.trim().length > 0) ||
        (form.value.title && form.value.title.trim().length > 0) ||
        (fileList.value && fileList.value.length > 0);
    if (hasContent) {
        e.preventDefault();
        e.returnValue = '';
    }
};

onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload);
});

onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload);
});

onBeforeRouteLeave(async (to, from, next) => {
    if (isSubmitSuccess.value) {
        next();
        return;
    }
    const hasContent = (userPostContent.value && userPostContent.value.trim().length > 0) ||
        (form.value.title && form.value.title.trim().length > 0) ||
        (fileList.value && fileList.value.length > 0);
    if (hasContent) {
        if (confirm('有未发布的内容，将会保存至草稿')) {
            try {
                await submit(PostStatus.DRAFT);
            } catch (err) {
                console.error('保存草稿失败:', err);
            }
            next();
        } else {
            next();
        }
    } else {
        next();
    }
})

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