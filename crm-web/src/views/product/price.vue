<template>
  <CrudPage
    title="客户价格"
    api="/price"
    :columns="columns"
    :fields="fields"
    :searchable="false"
    perms="crm:price"
    add-text="新增协议价"
    empty-text="还没有设置客户协议价"
    dialog-width="520px"
  >
    <template #customerName="{ row }">{{ row.customerName || '—' }}</template>
    <template #discount="{ row }">
      <span class="num" :class="row.standardPrice && row.price < row.standardPrice ? 'cut' : 'muted'">
        {{ discount(row) }}
      </span>
    </template>
  </CrudPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import request from '@/utils/request'

const customers = ref([])
const products = ref([])
const categories = ref([])

onMounted(async () => {
  const [cs, ps, cats] = await Promise.all([
    request.get('/customer/options'),
    request.get('/product/options'),
    request.get('/productCategory/list')
  ])
  customers.value = cs
  products.value = ps
  categories.value = cats
})

const categoryOf = (id) => categories.value.find((c) => c.id === id)?.categoryName || '其他'

function discount(row) {
  if (!row.standardPrice || Number(row.standardPrice) === 0) return '—'
  const rate = (Number(row.price) / Number(row.standardPrice)) * 100
  return rate.toFixed(1) + '%'
}

const columns = [
  { prop: 'customerName', label: '客户', minWidth: 180 },
  { prop: 'productName', label: '产品', minWidth: 160 },
  { prop: 'standardPrice', label: '标准价', width: 120, align: 'right', type: 'money' },
  { prop: 'price', label: '协议价', width: 120, align: 'right', type: 'money' },
  { prop: 'discount', label: '折扣', width: 90, align: 'right' },
  { prop: 'remark', label: '备注', minWidth: 140, muted: true }
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
    prop: 'productId',
    label: '产品',
    type: 'select',
    full: true,
    options: products.value.map((p) => ({ label: `【${categoryOf(p.categoryId)}】${p.productName}`, value: p.id }))
  },
  { prop: 'price', label: '协议价（元）', type: 'number', step: 1000 },
  { prop: 'remark', label: '备注' }
])
</script>

<style scoped>
.cut {
  color: var(--ok);
}
</style>
