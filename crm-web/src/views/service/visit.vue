<template>
  <CrudPage
    title="客户回访"
    api="/visit"
    :columns="columns"
    :fields="fields"
    :filters="filters"
    perms="crm:visit"
    search-placeholder="客户 / 回访内容"
    add-text="登记回访"
  >
    <template #satisfaction="{ row }">
      <span class="stars">
        <span v-for="n in 5" :key="n" :class="n <= row.satisfaction ? 'on' : 'off'">★</span>
      </span>
    </template>
  </CrudPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import request from '@/utils/request'

const WAYS = ['电话', '上门', '微信']
const customers = ref([])

onMounted(async () => {
  customers.value = await request.get('/customer/options')
})

const columns = [
  { prop: 'customerName', label: '客户', minWidth: 180 },
  { prop: 'visitDate', label: '回访日期', width: 110, type: 'date' },
  { prop: 'visitorName', label: '回访人', width: 110 },
  { prop: 'way', label: '方式', width: 80, muted: true },
  { prop: 'content', label: '回访内容', minWidth: 260 },
  { prop: 'satisfaction', label: '满意度', width: 120 },
  { prop: 'nextTime', label: '下次回访', width: 110, type: 'date' }
]

const filters = [
  { prop: 'way', label: '方式', options: WAYS.map((w) => ({ label: w, value: w })) }
]

const fields = computed(() => [
  {
    prop: 'customerId',
    label: '客户',
    type: 'select',
    full: true,
    options: customers.value.map((c) => ({ label: c.customerName, value: c.id }))
  },
  { prop: 'visitDate', label: '回访日期', type: 'date' },
  {
    prop: 'way',
    label: '方式',
    type: 'select',
    default: '电话',
    options: WAYS.map((w) => ({ label: w, value: w }))
  },
  {
    prop: 'satisfaction',
    label: '满意度',
    type: 'select',
    default: 5,
    options: [1, 2, 3, 4, 5].map((n) => ({ label: `${n} 分`, value: n }))
  },
  { prop: 'nextTime', label: '下次回访', type: 'date' },
  { prop: 'content', label: '回访内容', type: 'textarea', full: true },
  { prop: 'remark', label: '备注', type: 'textarea', full: true }
])
</script>

<style scoped>
.stars {
  letter-spacing: 1px;
}

.on {
  color: #F5558B;
}

.off {
  color: #EDE4E9;
}
</style>
