<template>
  <CrudPage
    title="联系人"
    api="/contact"
    :columns="columns"
    :fields="fields"
    :filters="filters"
    perms="crm:contact"
    search-placeholder="姓名 / 电话 / 客户"
  >
    <template #isPrimary="{ row }">
      <el-tag v-if="row.isPrimary === 1" size="small" type="success">主联系人</el-tag>
      <span v-else class="muted">—</span>
    </template>
  </CrudPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import request from '@/utils/request'

const customers = ref([])

onMounted(async () => {
  customers.value = await request.get('/customer/options')
})

const columns = [
  { prop: 'contactName', label: '姓名', width: 120 },
  { prop: 'customerName', label: '所属客户', minWidth: 180 },
  { prop: 'position', label: '职位', width: 120, muted: true },
  { prop: 'phone', label: '手机号', width: 140 },
  { prop: 'email', label: '邮箱', minWidth: 160, muted: true },
  { prop: 'wechat', label: '微信', width: 110, muted: true },
  { prop: 'isPrimary', label: '标记', width: 100 }
]

const filters = computed(() => [
  { prop: 'customerId', label: '按客户筛选', options: customers.value.map((c) => ({ label: c.customerName, value: c.id })) }
])

const fields = computed(() => [
  {
    prop: 'customerId',
    label: '所属客户',
    type: 'select',
    full: true,
    options: customers.value.map((c) => ({ label: c.customerName, value: c.id }))
  },
  { prop: 'contactName', label: '姓名' },
  { prop: 'position', label: '职位' },
  { prop: 'phone', label: '手机号' },
  { prop: 'email', label: '邮箱' },
  { prop: 'wechat', label: '微信' },
  { prop: 'isPrimary', label: '主联系人', type: 'radio', default: 0, options: [{ label: '是', value: 1 }, { label: '否', value: 0 }] },
  { prop: 'remark', label: '备注', type: 'textarea', full: true }
])
</script>
