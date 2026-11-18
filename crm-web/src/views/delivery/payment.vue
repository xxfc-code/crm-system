<template>
  <CrudPage
    title="收款记录"
    api="/payment"
    :columns="columns"
    :fields="fields"
    :filters="filters"
    perms="crm:payment"
    search-placeholder="收款单号 / 客户"
    add-text="登记收款"
  />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import request from '@/utils/request'

const PAY_TYPES = ['银行转账', '现金', '支票', '承兑汇票']

const customers = ref([])
const orders = ref([])

onMounted(async () => {
  const [cs, os] = await Promise.all([request.get('/customer/options'), request.get('/order/options')])
  customers.value = cs
  orders.value = os
})

const columns = [
  { prop: 'paymentNo', label: '收款单号', width: 110, type: 'mono' },
  { prop: 'customerName', label: '客户', minWidth: 170 },
  { prop: 'orderNo', label: '关联订单', width: 110, type: 'mono', muted: true },
  { prop: 'amount', label: '收款金额', width: 130, align: 'right', type: 'money' },
  { prop: 'payDate', label: '收款日期', width: 110, type: 'date' },
  { prop: 'payType', label: '收款方式', width: 120, muted: true },
  { prop: 'ownerName', label: '负责人', width: 100, muted: true }
]

const filters = [
  { prop: 'payType', label: '收款方式', options: PAY_TYPES.map((p) => ({ label: p, value: p })) }
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
  { prop: 'amount', label: '收款金额（元）', type: 'number', step: 1000 },
  { prop: 'payDate', label: '收款日期', type: 'date' },
  { prop: 'payType', label: '收款方式', type: 'select', options: PAY_TYPES.map((p) => ({ label: p, value: p })) },
  { prop: 'remark', label: '备注', type: 'textarea', full: true }
])
</script>
