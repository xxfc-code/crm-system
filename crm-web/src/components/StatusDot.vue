<template>
  <span class="dot">
    <i :style="{ background: dot.color }"></i>
    <span :style="{ color: dot.color }">{{ dot.label }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: [String, Number],
  map: { type: Object, required: true }
})

const dot = computed(() => props.map[props.value] || { label: props.value ?? '-', color: '#9B949E' })
</script>
