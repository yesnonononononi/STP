<template>
    <div v-if="visible" class="absolute inset-0 z-100  flex items-center justify-center " :class="props.bgColor">
        <div class="flex items-center p-2 gap-2 w-auto  h-12">
            <div v-html="display"></div>
            <span class="animate-pulse text-gray-500">{{ prompt }}</span>
        </div>
    </div>
</template>
<script lang="ts" setup>
const visible = defineModel<boolean>()
const props = withDefaults(defineProps<
    {
        bgColor?: string,
        prompt?: string,
        display?: string
    }>(),
    {
        bgColor: 'bg-gray-100',
        prompt: '',
        display: '<div class="loading-text" data-text="STP">STP</div>'
    }
)
</script>



<style>
.loading-text {
    font-size: 36px;
    font-weight: bold;
    color: #ccc;
    /* 灰色（未填充部分的颜色） */
    position: relative;
    display: inline-block;
}

/* 伪元素作为填充层 */
.loading-text::before {
    content: attr(data-text);
    /* 复制文本内容 */
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    color: blue;
    /* 填充颜色（蓝色） */
    /* 关键：用 clip-path 控制显示区域，初始全隐藏 */
    clip-path: inset(0 100% 0 0);
    /* 循环动画：2 秒一次，ease-in-out 让两端慢中间快 */
    animation: fillLoop 0.5s ease-in-out infinite;
}

@keyframes fillLoop {
    0% {
        clip-path: inset(0 100% 0 0);
    }

    /* 完全隐藏（右边界在 100%） */
    50% {
        clip-path: inset(0 0% 0 0);
    }

    /* 完全显示（右边界在 0%） */
    100% {
        clip-path: inset(0 100% 0 0);
    }

    /* 回到隐藏 */
}
</style>