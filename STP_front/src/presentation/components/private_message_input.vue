<template>
    <div class="w-125 h-auto bg-white flex flex-col gap-4 shadow-xl rounded-2xl p-5 border border-gray-100">
        <!-- 头部 -->
        <div class="text-lg font-bold flex items-center justify-between pb-2">
            <span>发送私信</span>
            <svg @click="emit('close')"
                class="icon size-5 text-gray-400 cursor-pointer hover:text-gray-600 transition-colors"
                viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5150">
                <path
                    d="M1024 930.133333L930.133333 1024 512 605.866667 93.866667 1024 0 930.133333 418.133333 512 0 93.866667 93.866667 0 512 418.133333 930.133333 0 1024 93.866667 605.866667 512z"
                    fill="currentColor" p-id="5151"></path>
            </svg>
        </div>

        <!-- 接收人 -->
        <div class="flex w-full items-center gap-4 text-sm text-gray-600">
            <span class="font-medium shrink-0">发给：</span>
            <div class="flex items-center gap-2 bg-slate-50 px-3 py-1 rounded-full border border-slate-100">
                <img :src="props.user.avatar" class="size-6 rounded-full object-cover" referrerpolicy="no-referrer"
                    alt="">
                <span class="text-gray-700 font-semibold"> {{ props.user.nick }}</span>
            </div>
        </div>

        <!-- 发送内容 -->
        <div class="flex w-full gap-4 text-sm text-gray-600">
            <span class="font-medium shrink-0 pt-1.5">内容：</span>
            <div class="flex-1 flex flex-col gap-3">
                <textarea ref="textareaRef" v-model="content"
                    class="w-full h-28 rounded-xl border-gray-200/80 border p-3 resize-none text-sm text-gray-700 placeholder-gray-400 focus:outline-hidden focus:border-blue-400 focus:ring-1 focus:ring-blue-100 transition-all"
                    placeholder="请输入私信内容..."></textarea>

                <!-- 工具栏 -->
                <div class="flex items-center gap-4">
                    <Emoji v-model="content" place-holder="表情" :textarea-ref="textareaRef"></Emoji>

                    <Tooltip placement="top" content="发送图片">
                        <el-upload class="size-8" action="#" :auto-upload="false" :show-file-list="false"
                            :on-change="handleImageChange" multiple>
                            <div
                                class="flex hover:text-blue-400 items-center cursor-pointer gap-1 transition-colors text-gray-600 group">
                                <svg class="icon size-8 text-gray-400 group-hover:text-blue-400 transition-colors"
                                    viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
                                    p-id="4988">
                                    <path
                                        d="M192 106.666667a85.333333 85.333333 0 0 0-85.333333 85.333333v640a85.333333 85.333333 0 0 0 85.333333 85.333333h640a85.333333 85.333333 0 0 0 85.333333-85.333333V192a85.333333 85.333333 0 0 0-85.333333-85.333333H192z m0 85.333333h640v640H192V192z m213.333333 85.333333h-128v128h128v-128z m341.333334 469.333334V409.002667L516.010667 639.658667 437.333333 538.496 275.413333 746.666667H746.666667z"
                                        fill="currentColor" p-id="4989"></path>
                                </svg>
                                <span class="text-sm font-medium shrink-0">图片</span>
                            </div>
                        </el-upload>
                    </Tooltip>
                </div>

                <!-- 选中的图片本地预览 -->
                <div v-if="imageInput.length > 0"
                    class="flex flex-wrap gap-2 p-2 bg-slate-50 rounded-xl border border-dashed border-gray-200">
                    <div v-for="(file, idx) in imageInput" :key="idx"
                        class="relative group size-14 rounded-lg overflow-hidden border border-gray-200 shadow-xs shrink-0 bg-white">
                        <img :src="getFileUrl(file)" class="w-full h-full object-cover" />
                        <div
                            class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                            <svg @click="removeImage(idx)" class="size-4 text-white cursor-pointer hover:text-red-400"
                                viewBox="0 0 1024 1024">
                                <path fill="currentColor"
                                    d="M352 192V128a64 64 0 0 1 64-64h192a64 64 0 0 1 64 64v64h192a32 32 0 1 1 0 64H96a32 32 0 1 1 0-64h256zm64 0h192V128H416v64zm-96 128h384v512a128 128 0 0 1-128 128H352a128 128 0 0 1-128-128V320zm128 96a32 32 0 0 0-64 0v384a32 32 0 0 0 64 0V416zm192 0a32 32 0 0 0-64 0v384a32 32 0 0 0 64 0V416z">
                                </path>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 底部控制按钮 -->
        <div class="flex justify-end items-center gap-3 pt-2 ">
            <el-button @click="emit('close')" class="rounded-lg">取消</el-button>
            <el-button type="primary" :loading="loading" @click="handleSend" class="rounded-lg">发送</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import type { UserSimpleData } from '@/services/user';
import { ref } from 'vue';
import Emoji from './emoji.vue';
import Tooltip from './Tooltip.vue';
import { CommonAPI } from '@/services/common/api';
import { ElMessage } from 'element-plus';
import { MessageAPI, messageType, SessionAPI } from '@/services/message/message';
import { TimeUtils } from '@/utils/time';
import { getSnowflakeId } from '@/utils/snowflake';
import { parseEmoji } from '@/utils/emoji';
import { XssUtils } from '@/utils/xss';

const content = defineModel<string>({ required: true });
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const imageInput = ref<any[]>([]);
const loading = ref(false);

const props = defineProps<{
    user: UserSimpleData
    visible: boolean
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'success'): void;
}>();

function handleImageChange(uploadFile: any) {
    if (uploadFile && uploadFile.raw) {
        imageInput.value.push(uploadFile);
    }
}

function removeImage(idx: number) {
    imageInput.value.splice(idx, 1);
}

function getFileUrl(file: any) {
    const rawFile = file.raw || file;
    return URL.createObjectURL(rawFile);
}

async function handleSend() {
    if (!content.value.trim() && imageInput.value.length === 0) {
        ElMessage.warning("请输入聊天内容或选择图片");
        return;
    }

    loading.value = true;
    try {
        let imageUrl = '';
        let type = messageType.TEXT;
        if (imageInput.value.length > 0) {
            const firstFile = imageInput.value[0].raw || imageInput.value[0];
            const res = await CommonAPI.upload(firstFile, 'message-media');
            if (res.code === 1 && res.data?.url) {
                imageUrl = res.data.url;
                type = messageType.IMAGE;
            } else {
                throw new Error(res.errMsg || "图片上传失败");
            }
        }

        try {
            await SessionAPI.saveSession({
                targetId: props.user.id,
                targetNickName: props.user.nick,
                targetAvatar: props.user.avatar
            });
        } catch (sessionErr) {
            console.log("Session might already exist:", sessionErr);
        }

        await MessageAPI.sendMessage({
            msgId: getSnowflakeId(),
            receiverId: props.user.id,
            content: XssUtils.filter(parseEmoji(content.value)),
            image: imageUrl,
            audio: '',
            video: '',
            sendTime: String(Date.now()),
            type: type
        });

        ElMessage.success("私信发送成功！");
        content.value = '';
        imageInput.value = [];

        emit('success');
        emit('close');
    } catch (e: any) {
        console.error("私信发送失败:", e);
        ElMessage.error(e.message || "发送失败，请稍后重试");
    } finally {
        loading.value = false;
    }
}

</script>