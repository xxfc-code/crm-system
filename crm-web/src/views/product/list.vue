<template>
  <CrudPage
    title="产品"
    api="/product"
    :columns="columns"
    :fields="fields"
    :filters="filters"
    perms="crm:product"
    :action-width="120"
    search-placeholder="产品名称 / 编号"
  >
    <template #price="{ row }">
      <span class="num">¥{{ Number(row.price || 0).toLocaleString('zh-CN') }}</span>
    </template>
  </CrudPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import request from '@/utils/request'

const STATUS = { 1: { label: '在售', color: '#2FB98A' }, 0: { label: '停售', color: '#9B949E' } }
const categories = ref([])

onMounted(async () => {
  categories.value = await request.get('/productCategory/list')
})

const columns = [
  { prop: 'productNo', label: '编号', width: 96, type: 'mono' },
  { prop: 'productName', label: '产品名称', minWidth: 180 },
  { prop: 'categoryName', label: '分类', width: 120, muted: true },
  { prop: 'spec', label: '规格', minWidth: 140, muted: true },
  { prop: 'price', label: '标准价（元）', width: 130, align: 'right' },
  { prop: 'stock', label: '库存', width: 90, align: 'right' },
  { prop: 'status', label: '状态', width: 90, type: 'status', map: STATUS }
]

const filters = computed(() => [
  { prop: 'categoryId', label: '分类', options: categories.value.map((c) => ({ label: c.categoryName, value: c.id })) },
  { prop: 'status', label: '状态', options: [{ label: '在售', value: 1 }, { label: '停售', value: 0 }] }
])

const fields = computed(() => [
  { prop: 'productName', label: '产品名称', full: true },
  { prop: 'categoryId', label: '所属分类', type: 'select', options: categories.value.map((c) => ({ label: c.categoryName, value: c.id })) },
  { prop: 'spec', label: '规格型号' },
  { prop: 'price', label: '标准售价（元）', type: 'number', step: 1000 },
  { prop: 'cost', label: '成本价（元）', type: 'number', step: 1000 },
  { prop: 'unit', label: '单位' },
  { prop: 'stock', label: '库存', type: 'number' },
  { prop: 'status', label: '状态', type: 'radio', default: 1, options: [{ label: '在售', value: 1 }, { label: '停售', value: 0 }] },
  { prop: 'remark', label: '备注', type: 'textarea', full: true }
])
</script>
