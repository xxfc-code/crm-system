<template>
  <el-card shadow="never">
    <div class="panel-head">
      <div>
        <h3>客户采购分析</h3>
        <p>按累计订单金额排名，最多显示 20 家</p>
      </div>
      <div v-if="list.length" class="summary">
        <div class="s-item"><span>客户数</span><b class="num">{{ list.length }}</b></div>
        <div class="s-item"><span>采购总额</span><b class="num">{{ money(totalAmount) }}</b></div>
      </div>
    </div>

    <div class="chart-box">
      <Chart v-if="list.length" :option="chartOption" :height="chartHeight" />
      <el-empty v-else description="还没有订单数据" :image-size="72" />
    </div>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'
import { money } from '@/utils/format'
import { rankOption } from '@/utils/chart'
import Chart from '@/components/Chart.vue'

const list = ref([])

const chartOption = computed(() =>
  rankOption(
    list.value.map((r) => ({
      name: r.customerName,
      value: Number(r.totalAmount || 0),
      extra: `${r.orderCount} 单`
    }))
  )
)

// 图高跟着条数走，条少时别留一大片空白
const chartHeight = computed(() => Math.max(240, list.value.length * 30 + 70) + 'px')

const totalAmount = computed(() => list.value.reduce((s, r) => s + Number(r.totalAmount || 0), 0))

onMounted(async () => {
  list.value = await request.get('/report/customer')
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
