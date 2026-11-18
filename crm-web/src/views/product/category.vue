<template>
  <CrudPage
    title="产品分类"
    api="/productCategory"
    :columns="columns"
    :fields="fields"
    :searchable="false"
    :no-paging="true"
    perms="crm:productCategory"
    add-text="新增分类"
    :action-width="120"
  />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'

const STATUS = { 1: { label: '正常', color: '#2FB98A' }, 0: { label: '停用', color: '#9B949E' } }

const columns = [
  { prop: 'categoryName', label: '分类名称', minWidth: 200 },
  { prop: 'productCount', label: '产品数', width: 110, align: 'right' },
  { prop: 'sort', label: '排序', width: 90, align: 'center' },
  { prop: 'status', label: '状态', width: 100, type: 'status', map: STATUS }
]

const fields = [
  { prop: 'categoryName', label: '分类名称' },
  { prop: 'sort', label: '排序', type: 'number' },
  { prop: 'status', label: '状态', type: 'radio', default: 1, options: [{ label: '正常', value: 1 }, { label: '停用', value: 0 }] }
]
</script>
