<template>
  <CrudPage
    title="发货记录"
    api="/delivery"
    :columns="columns"
    :fields="fields"
    :filters="filters"
    perms="crm:delivery"
    search-placeholder="发货单号 / 客户"
    add-text="新建发货"
  >
    <template #status="{ row }">
      <StatusDot :value="row.status" :map="STATUS" />
    </template>
  </CrudPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import StatusDot from '@/components/StatusDot.vue'
import request from '@/utils/request'

const STATUS = {
  待发货: { label: '待发货', color: '#F0A04E' },
  已发货: { label: '已发货', color: '#F04E8B' },
  已签收: { label: '已签收', color: '#2FB98A' }
}

const customers = ref([])
const orders = ref([])

onMounted(async () => {
  const [cs, os] = await Promise.all([request.get('/customer/options'), request.get('/order/options')])
  customers.value = cs
  orders.value = os
})

const columns = [
  { prop: 'deliveryNo', label: '发货单号', width: 110, type: 'mono' },
  { prop: 'customerName', label: '客户', minWidth: 170 },
  { prop: 'orderNo', label: '关联订单', width: 110, type: 'mono', muted: true },
  { prop: 'deliveryDate', label: '发货日期', width: 110, type: 'date' },
  { prop: 'receiver', label: '收货人', width: 100 },
  { prop: 'address', label: '收货地址', minWidth: 220, muted: true },
  { prop: 'status', label: '状态', width: 100 }
]

const filters = [
  { prop: 'status', label: '状态', options: Object.entries(STATUS).map(([k, v]) => ({ label: v.label, value: k })) }
]

const fields = computed(() => [
  {
    prop: 'customerId',
    label: '客户',
    type: 'select',
    full: true,
    options: customers.value.map((c) => ({ label: c.customerName, value: c.id }))
  },
  {
    prop: 'orderId',
    label: '关联订单',
    type: 'select',
    full: true,
    options: orders.value.map((o) => ({ label: `${o.orderNo}（${o.customerName}）`, value: o.id }))
  },
  { prop: 'deliveryDate', label: '发货日期', type: 'date' },
  { prop: 'status', label: '状态', type: 'select', options: Object.entries(STATUS).map(([k, v]) => ({ label: v.label, value: k })) },
  { prop: 'receiver', label: '收货人' },
  { prop: 'phone', label: '联系电话' },
  { prop: 'address', label: '收货地址', full: true },
  { prop: 'remark', label: '备注', type: 'textarea', full: true }
])
</script>
