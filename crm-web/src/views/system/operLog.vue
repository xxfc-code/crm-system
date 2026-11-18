<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="操作人 / 接口" clearable style="width: 220px" @keyup.enter="search" @clear="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.module" placeholder="模块" clearable style="width: 130px" @change="search">
        <el-option v-for="m in MODULES" :key="m" :label="m" :value="m" />
      </el-select>
      <span class="count">共 {{ total }} 条</span>
      <div class="grow" />
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="operUserName" label="操作人" width="110" />
        <el-table-column label="模块" width="110">
          <template #default="{ row }"><span class="tag">{{ row.module }}</span></template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" min-width="200" />
        <el-table-column label="接口" min-width="240">
          <template #default="{ row }"><span class="mono muted">{{ row.method }}</span></template>
        </el-table-column>
        <el-table-column label="耗时" width="90" align="right">
          <template #default="{ row }"><span class="num muted">{{ row.costMs }} ms</span></template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <StatusDot :value="row.success === 1 ? '成功' : '失败'" :map="RESULT" />
          </template>
        </el-table-column>
        <el-table-column label="时间" width="150">
          <template #default="{ row }"><span class="num muted">{{ shortTime(row.createTime) }}</span></template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无操作日志" :image-size="72" />
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
import StatusDot from '@/components/StatusDot.vue'

const MODULES = ['lead', 'customer', 'opportunity', 'quotation', 'order', 'delivery', 'payment', 'ticket', 'visit', 'product', 'dept', 'employee', 'role', 'menu', 'follow', 'price', 'return']
const RESULT = {
  成功: { label: '成功', color: '#2FB98A' },
  失败: { label: '失败', color: '#E0635B' }
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', module: null })

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach((k) => {
      if (!params[k]) delete params[k]
    })
    const data = await request.get('/operLog/list', { params })
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

.tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f4f0f3;
  color: var(--text-2);
}
</style>
