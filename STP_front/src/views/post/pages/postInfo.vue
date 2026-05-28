<template>
    <div class="w-full min-h-screen bg-gray-500">
        <button @click="emit('close'); console.log('clock')">close</button>
    </div>
</template>
<script setup lang="ts">
import { useRoute } from 'vue-router';

const postId = useRoute().params.id as string;
defineProps<{
    id: number | null,
    visible: boolean
}>();
const emit = defineEmits<{
    close: []
}>();
</script>