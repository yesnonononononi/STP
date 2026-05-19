<script setup lang="ts">

</script>

<template>
  <div class="app" id="app">
    <router-view :key="$route.fullPath"></router-view>
  </div>
</template>

<style scoped></style>
