<template>
  <CrudPage
    title="退换货单"
    api="/return"
    :columns="columns"
    :fields="fields"
    :filters="filters"
    perms="crm:return"
    search-placeholder="单号 / 客户"
    add-text="新增退换货"
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
  待处理: { label: '待处理', color: '#F0A04E' },
  已处理: { label: '已处理', color: '#2FB98A' },
  已拒绝: { label: '已拒绝', color: '#E0635B' }
}

const customers = ref([])
const orders = ref([])

onMounted(async () => {
  const [cs, os] = await Promise.all([request.get('/customer/options'), request.get('/order/options')])
  customers.value = cs
  orders.value = os
})

const columns = [
  { prop: 'returnNo', label: '单号', width: 100, type: 'mono' },
  { prop: 'customerName', label: '客户', minWidth: 170 },
  { prop: 'returnType', label: '类型', width: 80 },
  { prop: 'reason', label: '原因', minWidth: 200, muted: true },
  { prop: 'amount', label: '金额', width: 110, align: 'right', type: 'money' },
  { prop: 'status', label: '状态', width: 100 },
  { prop: 'handleDate', label: '处理日期', width: 110, type: 'date' }
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
  { prop: 'returnType', label: '类型', type: 'radio', default: '退货', options: [{ label: '退货', value: '退货' }, { label: '换货', value: '换货' }] },
  { prop: 'amount', label: '金额（元）', type: 'number', step: 1000 },
  { prop: 'reason', label: '原因', full: true },
  { prop: 'remark', label: '备注', type: 'textarea', full: true }
])
</script>
