<script setup lang="ts">
import {onMounted, onUnmounted, watch} from 'vue';
import {loadEmojiCache} from '@/utils/emoji';
import {useAuthStore} from '@/views/auth/store';
import {removeIMListener, start} from './services/ws/im/init';

const authStore = useAuthStore();

onMounted(() => {
  loadEmojiCache();
  start();   //ws
});
onUnmounted(() => {
  removeIMListener();
})


watch(() => authStore.token, (token) => {
  if (token) {
    loadEmojiCache();
    start();
  }
});
</script>

<template>
  <div class="app min-w-screen min-h-screen" id="app">
    <router-view :key="$route.fullPath"></router-view>
  </div>
  <div id="overlay"></div>
</template>

<style scoped></style>
