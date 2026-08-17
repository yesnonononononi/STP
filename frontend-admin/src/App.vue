<script setup lang="ts">
// Root Component
</script>

<template>
  <div class="app min-w-screen min-h-screen bg-slate-50 text-slate-800 font-sans" id="app">
    <router-view :key="$route.fullPath"></router-view>
  </div>
</template>

<style scoped></style>
