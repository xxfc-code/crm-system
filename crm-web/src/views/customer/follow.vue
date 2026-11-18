<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索跟进内容" clearable style="width: 240px" @keyup.enter="search" @clear="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.bizType" placeholder="业务类型" clearable style="width: 130px" @change="search">
        <el-option label="线索" value="lead" />
        <el-option label="客户" value="customer" />
        <el-option label="商机" value="opportunity" />
      </el-select>
      <span class="count">共 {{ total }} 条</span>
      <div class="grow" />
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list">
        <el-table-column label="跟进对象" min-width="180">
          <template #default="{ row }">
            <div class="row-main">{{ row.bizName || '—' }}</div>
            <div class="row-sub">{{ BIZ[row.bizType]?.label }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="跟进内容" min-width="320" />
        <el-table-column label="下次跟进" width="130">
          <template #default="{ row }"><span class="num muted">{{ shortTime(row.nextTime) }}</span></template>
        </el-table-column>
        <el-table-column prop="createByName" label="记录人" width="110" />
        <el-table-column label="记录时间" width="130">
          <template #default="{ row }"><span class="num muted">{{ shortTime(row.createTime) }}</span></template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无跟进记录" :image-size="72" />
        </template>
      </el-table>
    </div>

    <div class="pager">
      <el-pagination
        small
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="(p) => { query.pageNum = p; load() }"
      />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { shortTime } from '@/utils/format'

const BIZ = { lead: { label: '线索' }, customer: { label: '客户' }, opportunity: { label: '商机' } }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', bizType: null })

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach((k) => {
      if (!params[k]) delete params[k]
    })
    const data = await request.get('/follow/page', { params })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

onMounted(load)
</script>

<style scoped>
.count {
  font-size: 12px;
  color: var(--text-3);
  padding-left: 4px;
}
</style>
