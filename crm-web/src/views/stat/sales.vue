<template>
  <el-card shadow="never">
    <div class="panel-head">
      <div>
        <h3>销售统计</h3>
        <p>近 12 个月的签约额与回款额，按签约 / 回款月份归集</p>
      </div>
      <div class="summary">
        <div class="s-item"><span>累计签约</span><b class="num">{{ money(sumOrders) }}</b></div>
        <div class="s-item"><span>累计回款</span><b class="num">{{ money(sumPayments) }}</b></div>
        <div class="s-item"><span>回款率</span><b class="num">{{ rate }}%</b></div>
      </div>
    </div>

    <div class="chart-box">
      <Chart v-if="rows.length" :option="chartOption" height="320px" />
      <el-empty v-else description="还没有成交数据" :image-size="72" />
    </div>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'
import { money } from '@/utils/format'
import { trendOption } from '@/utils/chart'
import Chart from '@/components/Chart.vue'

const orders = ref([])
const payments = ref([])

// 后端已补齐 12 个月并按时间正序，这里直接用
const rows = computed(() =>
  orders.value.map((o) => ({
    month: o.month,
    orderAmount: Number(o.orderAmount || 0),
    payAmount: Number(
      (payments.value.find((p) => p.month === o.month) || {}).payAmount || 0
    )
  }))
)

const chartOption = computed(() =>
  trendOption(
    rows.value.map((r) => r.month),
    rows.value.map((r) => r.orderAmount),
    rows.value.map((r) => r.payAmount)
  )
)

const sumOrders = computed(() => rows.value.reduce((s, o) => s + o.orderAmount, 0))
const sumPayments = computed(() => rows.value.reduce((s, p) => s + p.payAmount, 0))
const rate = computed(() => (sumOrders.value ? ((sumPayments.value / sumOrders.value) * 100).toFixed(1) : '0.0'))

onMounted(async () => {
  const data = await request.get('/report/sales')
  orders.value = data.orders || []
  payments.value = data.payments || []
})
</script>

<style scoped>
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 18px 20px;
  border-bottom: 1px solid var(--line);
}

h3 {
  font-size: 15px;
  font-weight: 500;
}

p {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 4px;
}

.summary {
  display: flex;
  gap: 32px;
}

.s-item {
  text-align: right;
}

.s-item span {
  display: block;
  font-size: 12px;
  color: var(--text-3);
}

.s-item b {
  font-size: 18px;
  font-weight: 500;
}

.chart-box {
  padding: 12px 20px 18px;
}
</style>
