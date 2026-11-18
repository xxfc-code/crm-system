<template>
  <div class="todo">
    <el-card shadow="never" class="block">
      <div class="b-head">
        <span>待跟进线索</span>
        <span class="b-sub">三天内到期或已逾期</span>
      </div>
      <div v-if="data.leads?.length" class="items">
        <div v-for="l in data.leads" :key="l.id" class="item">
          <div class="i-main">
            <div class="row-main">{{ l.leadName }}</div>
            <div class="row-sub">{{ l.company || '—' }}</div>
          </div>
          <span class="i-meta" :class="{ overdue: isOverdue(l.nextTime) }">{{ shortTime(l.nextTime) }}</span>
        </div>
      </div>
      <el-empty v-else description="没有待跟进的线索" :image-size="56" />
    </el-card>

    <el-card shadow="never" class="block">
      <div class="b-head">
        <span>待处理工单</span>
        <span class="b-sub">待受理和处理中的售后</span>
      </div>
      <div v-if="data.tickets?.length" class="items">
        <div v-for="t in data.tickets" :key="t.id" class="item">
          <div class="i-main">
            <div class="row-main">{{ t.title }}</div>
            <div class="row-sub">{{ t.customerName }} · {{ t.priority }}优先级</div>
          </div>
          <span class="tag">{{ t.status }}</span>
        </div>
      </div>
      <el-empty v-else description="没有待处理工单" :image-size="56" />
    </el-card>

    <el-card shadow="never" class="block">
      <div class="b-head">
        <span>待处理退换货</span>
        <span class="b-sub">客户提交的退换申请</span>
      </div>
      <div v-if="data.returns?.length" class="items">
        <div v-for="r in data.returns" :key="r.id" class="item">
          <div class="i-main">
            <div class="row-main">{{ r.returnNo }} · {{ r.returnType }}</div>
            <div class="row-sub">{{ r.customerName }} · {{ r.reason || '—' }}</div>
          </div>
          <span class="i-meta money">{{ money(r.amount) }}</span>
        </div>
      </div>
      <el-empty v-else description="没有待处理退换货" :image-size="56" />
    </el-card>

    <el-card shadow="never" class="block">
      <div class="b-head">
        <span>待收账款</span>
        <span class="b-sub">按未收金额排序</span>
      </div>
      <div v-if="data.receivables?.length" class="items">
        <div v-for="r in data.receivables" :key="r.id" class="item">
          <div class="i-main">
            <div class="row-main">{{ r.customerName }}</div>
            <div class="row-sub">{{ r.orderNo }}</div>
          </div>
          <span class="i-meta remain">{{ money(r.remain) }}</span>
        </div>
      </div>
      <el-empty v-else description="没有未收款项" :image-size="56" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'
import { money, shortTime } from '@/utils/format'

const data = ref({})
const isOverdue = (t) => t && new Date(t).getTime() < Date.now()

onMounted(async () => {
  data.value = await request.get('/dashboard/todo')
})
</script>

<style scoped>
.todo {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.block :deep(.el-card__body) {
  padding: 0;
}

.b-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
  font-size: 14px;
  font-weight: 500;
}

.b-sub {
  font-size: 12px;
  color: var(--text-3);
  font-weight: 400;
}

.items {
  padding: 4px 20px 8px;
}

.item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #FBF6F9;
}

.item:last-child {
  border-bottom: none;
}

.i-main {
  flex: 1;
  min-width: 0;
}

.i-meta {
  font-size: 12px;
  color: var(--text-3);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.i-meta.overdue {
  color: var(--danger);
}

.i-meta.money {
  color: var(--text-2);
}

.i-meta.remain {
  color: var(--warn);
  font-weight: 500;
}

.tag {
  font-size: 11px;
  color: var(--warn);
  background: #fdf3ea;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}
</style>
