<script setup lang="ts"></script>

<template>
  <div class="app w-screen min-h-screen" id="app">
    <router-view :key="$route.fullPath"></router-view>
  </div>
</template>

<style scoped></style>
