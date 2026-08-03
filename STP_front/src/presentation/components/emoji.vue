<template>
    <div class="emoji-trigger-container">
        <el-popover v-model:visible="visible" trigger="click" :width="390" placement="top-start"
            popper-class="emoji-popover-popper"
            popper-style="padding: 12px; border-radius: 8px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12); border: 1px solid #e4e7ed; background: #fff;"
            :teleported="true">
            <template #reference>
                <div class="flex hover:text-blue-400 items-center cursor-pointer gap-1 transition-colors text-gray-600">
                    <svg t="1780462418882" class="icon w-6 h-6" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="5265">
                        <path
                            d="M512 832c-176.448 0-320-143.552-320-320S335.552 192 512 192s320 143.552 320 320-143.552 320-320 320m0-704C300.256 128 128 300.256 128 512s172.256 384 384 384 384-172.256 384-384S723.744 128 512 128"
                            fill="#8a8a8a" p-id="5266"></path>
                        <path
                            d="M700.64 580.288a32 32 0 0 0-43.712 11.68A160.608 160.608 0 0 1 518.304 672a160.576 160.576 0 0 1-138.592-80 32 32 0 0 0-55.424 32.032 224.896 224.896 0 0 0 194.016 112 224.768 224.768 0 0 0 194.016-112 32 32 0 0 0-11.68-43.744M384 512a32 32 0 0 0 32-32v-96a32 32 0 0 0-64 0v96a32 32 0 0 0 32 32M640 512a32 32 0 0 0 32-32v-96a32 32 0 0 0-64 0v96a32 32 0 0 0 32 32"
                            fill="#8a8a8a" p-id="5267"></path>
                    </svg>
                    <span v-if="placeHolder" class="text-sm font-medium">{{ placeHolder }}</span>
                </div>
            </template>

            <div class="emoji-panel-content flex flex-col bg-white overflow-hidden">
                <!-- Tab Categories -->
                <div class="tab-list flex border-b border-gray-100 pb-2 mb-2 overflow-x-auto custom-scrollbar gap-2">
                    <div v-for="(tab, index) in emojiTabList" :key="tab.id" @click="selectTab(tab.id)"
                        class="tab-item text-xs font-semibold py-1 px-3 rounded-md cursor-pointer whitespace-nowrap transition-colors"
                        :class="activeTabId === tab.id ? 'bg-blue-50 text-blue-500' : 'text-gray-500 hover:text-blue-400 hover:bg-gray-50'">
                        {{ tab.name }}
                    </div>
                </div>
                <!-- Emoji Grid -->
                <div
                    class="emoji-grid grid grid-cols-10 gap-2 auto-rows-[32px] overflow-y-auto h-56 pr-1 custom-scrollbar">
                    <div v-for="(item, index) in emojiList" :key="item.id" @click="insertEmoji(item)"
                        class="emoji-item flex items-center justify-center p-1 rounded-md hover:bg-gray-100 cursor-pointer transition-transform duration-100 hover:scale-110 active:scale-95 group relative hover:z-50">
                        <span v-html="item.url"
                            class="w-8 h-8 flex items-center justify-center object-contain relative z-40"></span>

                        <div class="absolute z-41 object-contain bg-white border border-gray-100 shadow-md rounded-md p-1 scale-[3] w-6 h-6 opacity-0 pointer-events-none group-hover:opacity-100 flex items-center justify-center transition-opacity duration-200"
                            :class="identifyDirection(index) ? 'right-14' : 'left-14'" v-html="item.url">
                        </div>
                    </div>
                </div>
            </div>
        </el-popover>
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useEmoji } from './composables/useEmoji';

const model = defineModel<string>({ required: true });
const visible = ref(false);
const props = defineProps<{
    placeHolder: string;
    textareaRef: any;
}>();

const {
    emojiList,
    emojiTabList,
    activeTabId,
    selectTab,
    identifyDirection,
    insertEmoji,
} = useEmoji(model, props.textareaRef);
</script>

<style scoped>
/* 自定义滚动条样式，使其更精致 */
.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
    height: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #e4e7ed;
    border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #c0c4cc;
}
</style>