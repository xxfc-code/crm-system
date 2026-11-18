<template>
  <div class="dash">
    <div class="hello">
      <h1>{{ greet }}，{{ user.empName }}</h1>
      <p>这是你权限范围内的业务概览 · {{ today }}</p>
    </div>

    <div class="metrics">
      <div v-for="m in metrics" :key="m.label" class="metric">
        <div class="m-label">{{ m.label }}</div>
        <div class="m-value">{{ m.value }}</div>
        <div class="m-sub">{{ m.sub }}</div>
      </div>
    </div>

    <el-card v-if="keyCustomers.length" shadow="never" class="key-card">
      <div class="p-head">
        A 类重点客户
        <span class="p-sub">长期没跟进的排前面，需要重点关注</span>
      </div>
      <div class="key-list">
        <div v-for="c in keyCustomers" :key="c.id" class="key-item">
          <div class="k-main">
            <div class="row-main">{{ c.customerName }}</div>
            <div class="row-sub">{{ c.contact || '—' }}<span v-if="c.phone"> · {{ c.phone }}</span></div>
          </div>
          <span class="k-time">最近跟进 {{ shortTime(c.lastFollowTime) }}</span>
        </div>
      </div>
    </el-card>

    <div class="grid2">
      <el-card shadow="never" class="panel">
        <div class="p-head">
          商机阶段分布
          <span class="p-sub">
            进行中 {{ activeCount }} 个 · {{ money(activeAmount) }}
            <template v-if="closedStat"> · 赢单 {{ closedStat.win }} / 输单 {{ closedStat.lose }}</template>
          </span>
        </div>
        <Chart v-if="funnel.length" :option="funnelOption" height="320px" />
        <el-empty v-else description="还没有商机" :image-size="60" />
      </el-card>

      <el-card shadow="never" class="panel">
        <div class="p-head">
          线索来源
          <span class="p-sub">{{ sources.length }} 个渠道</span>
        </div>
        <Chart v-if="sources.length" :option="sourceOption" height="270px" />
        <el-empty v-else description="还没有线索" :image-size="60" />
      </el-card>
    </div>

    <el-card shadow="never" class="panel">
      <div class="p-head">
        近 12 个月成交趋势
        <span class="p-sub">订单签约额与实际回款额对比</span>
      </div>
      <Chart v-if="trend.months.length" :option="trendOption" height="240px" />
      <el-empty v-else description="还没有成交数据" :image-size="60" />
    </el-card>

    <div class="grid2 wide">
      <el-card shadow="never" class="panel">
        <div class="p-head">
          待跟进
          <span class="p-sub">三天内到期或已逾期</span>
        </div>
        <el-table v-if="todo.length" :data="todo" size="small" @row-click="goLead" row-class-name="clickable">
          <el-table-column label="线索" min-width="150">
            <template #default="{ row }">
              <div class="row-main">{{ row.leadName }}</div>
              <div class="row-sub">{{ row.company || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="ownerName" label="负责人" width="90" />
          <el-table-column label="跟进时间" width="130" align="right">
            <template #default="{ row }">
              <span class="num" :class="isOverdue(row.nextTime) ? 'overdue' : 'muted'">
                {{ shortTime(row.nextTime) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="近期没有待跟进事项" :image-size="60" />
      </el-card>

      <el-card shadow="never" class="panel">
        <div class="p-head">最近动态</div>
        <div v-if="recent.length" class="feed">
          <div v-for="r in recent" :key="r.id" class="f-item">
            <div class="avatar">{{ initial(r.createByName) }}</div>
            <div class="f-main">
              <div class="f-head">
                <span class="who">{{ r.createByName }}</span>
                <span class="what">{{ BIZ_LABEL[r.bizType] }}</span>
                <span class="when num">{{ shortTime(r.createTime) }}</span>
              </div>
              <p class="f-text">{{ r.content }}</p>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无跟进记录" :image-size="60" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { user } from '@/store/user'
import { OPP_STAGES } from '@/utils/dict'
import { initial, money, shortTime } from '@/utils/format'
import { CATEGORY_COLOR, funnelOption as buildFunnel, sourceOption as buildSource, trendOption as buildTrend } from '@/utils/chart'
import Chart from '@/components/Chart.vue'

const router = useRouter()
const data = ref({ funnel: [], sources: [], todo: [], recent: [], keyCustomers: [] })
const sales = ref({ orders: [], payments: [] })

const BIZ_LABEL = { lead: '跟进了线索', customer: '跟进了客户', opportunity: '跟进了商机' }

const hour = new Date().getHours()
const greet = hour < 6 ? '夜深了' : hour < 12 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
const today = new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })

const metrics = computed(() => [
  { label: '本月新增线索', value: data.value.leadNew ?? 0, sub: '按创建时间统计' },
  { label: '客户总数', value: data.value.customerTotal ?? 0, sub: '不含已删除' },
  { label: '进行中商机', value: money(data.value.oppAmount), sub: '赢单与输单除外' },
  { label: '已回款金额', value: money(data.value.orderPaid), sub: '已付款与已完成订单' }
])

// 后端按 group by 返回，顺序不定，前端按业务顺序排一遍
const funnel = computed(() => {
  const rows = data.value.funnel || []
  return OPP_STAGES.map((stage) => {
    const hit = rows.find((r) => r.stage === stage) || { cnt: 0, amt: 0 }
    return {
      stage,
      cnt: Number(hit.cnt),
      amt: Number(hit.amt),
      closed: ['赢单', '输单'].includes(stage)
    }
  }).filter((f) => f.cnt > 0)
})

/** 柱状图只画进行中的阶段，赢单/输单是结果不是流程的一环，混进去转化率会失真 */
const funnelStages = computed(() => funnel.value.filter((f) => !f.closed))

/** 赢单 / 输单单独统计 */
const closedStat = computed(() => {
  const win = funnel.value.find((f) => f.stage === '赢单')
  const lose = funnel.value.find((f) => f.stage === '输单')
  if (!win && !lose) return null
  return { win: win ? win.cnt : 0, lose: lose ? lose.cnt : 0 }
})

/** 阶段分布的折线画商机数量，不画转化率（金额不可比，会算出 500% 这种值） */
const funnelOption = computed(() =>
  buildFunnel(
    funnelStages.value.map((f) => f.stage),
    funnelStages.value.map((f) => f.amt),
    funnelStages.value.map((f) => f.cnt)
  )
)

const sources = computed(() => {
  const rows = data.value.sources || []
  const total = rows.reduce((s, r) => s + Number(r.cnt), 0) || 1
  return rows.map((r, i) => ({
    name: r.source,
    value: Number(r.cnt),
    ratio: Math.round((Number(r.cnt) / total) * 100),
    itemStyle: { color: CATEGORY_COLOR[i % CATEGORY_COLOR.length] }
  }))
})

const sourceOption = computed(() => buildSource(sources.value))

/** 近 12 个月趋势：后端已补齐 12 个月并按时间正序 */
const trend = computed(() => {
  const orders = sales.value.orders || []
  const pays = sales.value.payments || []
  return {
    months: orders.map((o) => o.month),
    orderAmounts: orders.map((o) => Number(o.orderAmount || 0)),
    payAmounts: orders.map((o) => {
      const p = pays.find((x) => x.month === o.month)
      return Number(p ? p.payAmount : 0)
    })
  }
})

const trendOption = computed(() =>
  buildTrend(trend.value.months, trend.value.orderAmounts, trend.value.payAmounts)
)

const todo = computed(() => data.value.todo || [])
const recent = computed(() => data.value.recent || [])
const keyCustomers = computed(() => data.value.keyCustomers || [])

const activeCount = computed(() => funnel.value.filter((f) => !['赢单', '输单'].includes(f.stage)).reduce((s, f) => s + f.cnt, 0))
const activeAmount = computed(() => funnel.value.filter((f) => !['赢单', '输单'].includes(f.stage)).reduce((s, f) => s + f.amt, 0))

const isOverdue = (t) => t && new Date(t).getTime() < Date.now()
const goLead = () => router.push('/lead')

onMounted(async () => {
  const [overview, report] = await Promise.all([
    request.get('/dashboard/overview'),
    request.get('/report/sales')
  ])
  data.value = overview
  sales.value = report
})
</script>

<style scoped>
.dash {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hello h1 {
  font-size: 20px;
  font-weight: 500;
  letter-spacing: -0.3px;
}

.hello p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-3);
}

/* 指标卡 */
.metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 18px 20px;
  box-shadow: 0 1px 2px rgba(31, 35, 41, 0.03);
}

.m-label {
  font-size: 12.5px;
  color: var(--text-3);
}

.m-value {
  font-size: 26px;
  font-weight: 500;
  letter-spacing: -0.5px;
  margin: 10px 0 6px;
  font-variant-numeric: tabular-nums;
}

.m-sub {
  font-size: 12px;
  color: #BDB5BC;
}

/* 两栏 */
.grid2 {
  display: grid;
  grid-template-columns: 1.45fr 1fr;
  gap: 16px;
}

.panel :deep(.el-card__body) {
  padding: 18px 20px;
}

/* A 类重点客户 */
.key-card :deep(.el-card__body) {
  padding: 0;
}

.key-card .p-head {
  padding: 16px 20px 0;
  margin-bottom: 10px;
}

.key-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 0 24px;
  padding: 6px 20px 14px;
}

.key-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid #f7f2f5;
}

.k-main {
  min-width: 0;
}

.k-time {
  font-size: 12px;
  color: var(--text-3);
  white-space: nowrap;
}

.p-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 16px;
}

.p-sub {
  font-size: 12px;
  color: var(--text-3);
  font-weight: 400;
}

/* 动态 */
.feed {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 300px;
  overflow-y: auto;
}

.f-item {
  display: flex;
  gap: 10px;
}

.f-head {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
}

.who {
  color: var(--text);
  font-weight: 500;
}

.what {
  color: var(--text-3);
}

.when {
  margin-left: auto;
  font-size: 11.5px;
  color: #BDB5BC;
}

.f-text {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--text-2);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.overdue {
  color: var(--danger);
}

:deep(.clickable) {
  cursor: pointer;
}
</style>
