<template>
    <!-- 左上 -> 右 -->
    <div class="absolute left-top py-py w-full left-0 h-0.5 top-0 scale-x-0 group-hover:scale-x-100 origin-left bg-linear-to-r transition duration-5000 ease-in-out brightness-150"
        :class="isDIY ? (bg.from, bg.via, bg.to) : ' from-blue-200 via-blue-300 to-blue-400'">

    </div>
    <!-- 左上 -> 下 -->
    <div class="absolute left-top px-px h-full left-0 w-0.5 top-0 scale-y-0 group-hover:scale-y-100 origin-top  bg-linear-to-b   brightness-150 transition duration-5000 ease-in-out"
        :class="isDIY ? (bg.from, bg.via, bg.to) : ' from-blue-200 via-blue-300 to-blue-400'">
    </div>
    <!-- 右下 -> 上 -->
    <div class="absolute right-bottom h-full w-0.5 px-px  right-0 bottom-0 scale-y-0 group-hover:scale-y-100 origin-bottom   brightness-150 bg-linear-to-t transition duration-5000 ease-in-out"
        :class="isDIY ? (bg.from, bg.via, bg.to) : ' from-blue-200 via-blue-300 to-blue-400'">
    </div>
    <!-- 右下 -> 左 -->
    <div class="absolute right-bottom w-full h-0.5 py-py  right-0 bottom-0 scale-x-0 group-hover:scale-x-100 origin-right  brightness-150 bg-linear-to-l  transition duration-5000 ease-in-out"
        :class="isDIY ? (bg.from, bg.via, bg.to) : ' from-blue-200 via-blue-300 to-blue-400'">
    </div>
</template>

<script lang="ts" setup>

const bg = defineProps<{
    isDIY: boolean,
    from?: string,
    via?: string,
    to?: string
}>()
</script>