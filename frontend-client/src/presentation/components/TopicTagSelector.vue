<template>
    <div :class="props.visible ? 'h-72 p-2' : 'h-0 p-0'"
        class="absolute z-10 transition-all duration-300 top-6 w-96  bg-white flex flex-col gap-2 shadow-md rounded-lg overflow-hidden">
        <div class="search">
            <el-input ref="searchInputRef" placeholder="搜索话题" class="" v-model="userInput" @keydown.enter="handleEnter">
                <template #prefix>
                    <svg t="1780034859072" class="icon w-6 h-6" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="5257">
                        <path
                            d="M644.096 251.904a277.333333 277.333333 0 1 0-392.192 392.192 277.333333 277.333333 0 0 0 392.192-392.192zM191.573333 191.573333a362.666667 362.666667 0 0 1 541.269334 480.938667l228.053333 228.053333-60.330667 60.330667-228.053333-228.053333A362.709333 362.709333 0 0 1 191.573333 191.573333z"
                            fill="#bfbfbf" p-id="5258"></path>
                    </svg>
                </template>
            </el-input>
        </div>
        <div class="tab flex gap-2 items-center">
            <span :class="curTab ? 'border-b border-blue-500' : ''"
                class="text-gray-400 hover:text-blue-200 cursor-pointer" @click="curTab = true">热门话题</span>
            <span :class="curTab ? '' : 'border-b border-blue-500'"
                class="text-gray-400 hover:text-blue-200 cursor-pointer" @click="curTab = false">最近使用</span>
        </div>
        <div class="selectItem w-full max-h-4/5 overflow-y-auto">
            <div v-for="(item, index) in suggestList" :key="index"
                class="flex justify-between items-center p-2 w-full hover:bg-gray-100 cursor-pointer"
                @click="selectTag(item)">
                <div class="name text-blue-500">#{{ item.keyword }}#</div>
                <div class="like">{{ item.extra == -1 ? '新话题' : `${formatNum(item.extra)} 讨论` }}</div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import {TagAPI} from '@/services/post';
import type {TopicTag} from '@/services/post/types';
import {formatNum} from '@/utils/page';
import {nextTick, onMounted, ref, watch} from 'vue';

const props = defineProps<{
    newTag?: string | null
    visible: boolean;
    keyword?: string | null
}>()
const emit = defineEmits<{
    (e: 'addTag', tag: TopicTag): void;
}>()
const curTab = ref<boolean>(true);
const userInput = ref<string>('');
const suggestList = ref<TopicTag[]>();
const searchInputRef = ref<any>(null);

onMounted(async () => {
    await loadData()
    if (props.visible) {
        nextTick(() => {
            searchInputRef.value?.focus();
        });
    }
})

watch(() => props.visible, (visible) => {
    if (visible) {
        userInput.value = '';
        nextTick(() => {
            searchInputRef.value?.focus();
        });
    }
})
watch(() => props.newTag, async (newTag) => {
    if (newTag) {
        await addTag(newTag);
    }
})
watch(() => props.keyword, async (keyword) => {
    if (!keyword) return;
    await acquireSearchSuggest(keyword)
})
watch(() => userInput.value, async () => {
    if (curTab.value) {
        await loadData()
    }
})

watch(() => curTab.value, async () => {
    await loadData()
})

async function loadData() {
    if (curTab.value) {
        await acquireSearchSuggest(userInput.value)
    } else {
        const res = await TagAPI.getRecentTags(10);
        suggestList.value = res.data.map(tag => ({
            id: tag.id as number,
            keyword: tag.tagName,
            extra: tag.useCount
        }));
    }
}
async function addTag(content: string) {
    if (content.length === 0) return;
    await TagAPI.create({ tagName: content });
}
async function acquireSearchSuggest(keyWord: string, limit: number = 10) {
    const data = (await TagAPI.getSearchSuggest(keyWord, limit)).data.suggestList;
    if (data.length == 0) {
        suggestList.value = [{ id: 0, keyword: keyWord, extra: -1 }];
    } else {
        suggestList.value = data;
    }
}

function selectTag(item: TopicTag) {
    emit('addTag', item);
}

function handleEnter(e: KeyboardEvent) {
    e.preventDefault();
    const keyword = userInput.value.trim();
    if (suggestList.value && suggestList.value.length > 0 && suggestList.value[0]) {
        selectTag(suggestList.value[0]);
    } else if (keyword) {
        selectTag({ id: 0, keyword: keyword, extra: -1 });
    }
}
</script>
