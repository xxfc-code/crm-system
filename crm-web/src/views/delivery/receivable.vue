<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="count">订单金额减去已收款，为正的部分就是还没收回来的钱</span>
      <div class="grow" />
      <div class="sum">
        <span>本页未收合计</span>
        <b class="num">{{ money(pageRemain) }}</b>
      </div>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list">
        <el-table-column label="订单号" width="110">
          <template #default="{ row }"><span class="mono">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="180" />
        <el-table-column label="订单金额" width="130" align="right">
          <template #default="{ row }"><span class="num">{{ money(row.total) }}</span></template>
        </el-table-column>
        <el-table-column label="已收款" width="130" align="right">
          <template #default="{ row }"><span class="num muted">{{ money(row.paid) }}</span></template>
        </el-table-column>
        <el-table-column label="未收款" width="130" align="right">
          <template #default="{ row }"><span class="num remain">{{ money(row.remain) }}</span></template>
        </el-table-column>
        <el-table-column label="签约日期" width="110">
          <template #default="{ row }"><span class="num muted">{{ day(row.signDate) }}</span></template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }"><span class="muted">{{ row.status }}</span></template>
        </el-table-column>
        <template #empty>
          <el-empty description="都收齐了，没有应收" :image-size="72" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { day, money } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const pageRemain = computed(() => list.value.reduce((s, r) => s + Number(r.remain || 0), 0))

async function load() {
  loading.value = true
  try {
    const data = await request.get('/report/receivable', { params: query })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.count {
  font-size: 12px;
  color: var(--text-3);
}

.sum {
  font-size: 12px;
  color: var(--text-3);
}

.sum b {
  font-size: 16px;
  color: var(--warn);
  margin-left: 8px;
}

.remain {
  color: var(--warn);
  font-weight: 500;
}
</style>
