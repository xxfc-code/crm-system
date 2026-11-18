<template>
  <el-card shadow="never">
    <el-empty :description="`「${route.meta.title || '该模块'}」正在开发中`" />
  </el-card>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>
