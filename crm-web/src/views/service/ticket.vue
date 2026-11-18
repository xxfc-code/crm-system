<template>
  <CrudPage
    title="售后工单"
    api="/ticket"
    :columns="columns"
    :fields="fields"
    :filters="filters"
    perms="crm:ticket"
    search-placeholder="标题 / 工单号 / 客户"
    add-text="新建工单"
    :action-width="110"
    dialog-width="620px"
  >
    <template #priority="{ row }">
      <StatusDot :value="row.priority" :map="PRIORITY" />
    </template>
    <template #status="{ row }">
      <StatusDot :value="row.status" :map="STATUS" />
    </template>
    <template #actions="{ row }">
      <el-button link type="primary" @click="changeStatus(row)">改状态</el-button>
    </template>
  </CrudPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CrudPage from '@/components/CrudPage.vue'
import StatusDot from '@/components/StatusDot.vue'
import request from '@/utils/request'

const PRIORITY = {
  高: { label: '高', color: '#E0635B' },
  中: { label: '中', color: '#F0A04E' },
  低: { label: '低', color: '#9B949E' }
}

const STATUS = {
  待受理: { label: '待受理', color: '#F0A04E' },
  处理中: { label: '处理中', color: '#F04E8B' },
  已解决: { label: '已解决', color: '#2FB98A' },
  已关闭: { label: '已关闭', color: '#9B949E' }
}

const customers = ref([])
const orders = ref([])
const products = ref([])

onMounted(async () => {
  const [cs, os, ps] = await Promise.all([
    request.get('/customer/options'),
    request.get('/order/options'),
    request.get('/product/options')
  ])
  customers.value = cs
  orders.value = os
  products.value = ps
})

const columns = [
  { prop: 'ticketNo', label: '工单号', width: 100, type: 'mono' },
  { prop: 'title', label: '标题', minWidth: 200 },
  { prop: 'customerName', label: '客户', minWidth: 160 },
  { prop: 'ticketType', label: '类型', width: 80, muted: true },
  { prop: 'priority', label: '优先级', width: 90 },
  { prop: 'status', label: '状态', width: 100 },
  { prop: 'ownerName', label: '处理人', width: 100, muted: true }
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
  {
    prop: 'productId',
    label: '关联产品批次',
    type: 'select',
    full: true,
    options: products.value.map((p) => ({ label: `${p.productName}（${p.productNo}）`, value: p.id }))
  },
  { prop: 'title', label: '标题', full: true },
  {
    prop: 'ticketType',
    label: '类型',
    type: 'select',
    options: ['咨询', '维修', '投诉', '建议'].map((t) => ({ label: t, value: t }))
  },
  {
    prop: 'priority',
    label: '优先级',
    type: 'select',
    default: '中',
    options: Object.keys(PRIORITY).map((k) => ({ label: k, value: k }))
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    default: '待受理',
    options: Object.keys(STATUS).map((k) => ({ label: k, value: k }))
  },
  { prop: 'content', label: '问题描述', type: 'textarea', full: true },
  { prop: 'solution', label: '解决方案', type: 'textarea', full: true }
])

async function changeStatus(row) {
  let status
  try {
    const res = await ElMessageBox.prompt('输入新状态：待受理 / 处理中 / 已解决 / 已关闭', `工单 ${row.ticketNo}`, {
      inputValue: row.status,
      confirmButtonText: '确认'
    })
    status = res.value
  } catch {
    return
  }
  if (!Object.keys(STATUS).includes(status)) {
    ElMessage.warning('状态值不正确')
    return
  }
  await request.put(`/ticket/${row.id}/status`, null, { params: { status } })
  ElMessage.success('状态已更新')
  location.reload()
}
</script>
