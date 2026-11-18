<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索客户名称 / 联系人" clearable style="width: 240px" @keyup.enter="search" @clear="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <span class="count">超过 <b>{{ poolDays }}</b> 天没有跟进的客户会自动回收到这里，先到先得</span>
      <div class="grow" />
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list" @row-click="openDetail" row-class-name="clickable">
        <el-table-column label="客户名称" min-width="190">
          <template #default="{ row }">
            <div class="row-main">{{ row.customerName }}</div>
            <div class="row-sub">{{ row.contact || '—' }}<span v-if="row.phone"> · {{ row.phone }}</span></div>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="90">
          <template #default="{ row }"><StatusDot :value="row.level" :map="CUSTOMER_LEVEL" /></template>
        </el-table-column>
        <el-table-column label="行业" width="110">
          <template #default="{ row }"><span class="muted">{{ row.industry || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="历史订单" width="100" align="right">
          <template #default="{ row }"><span class="num muted">{{ row.orderCount }} 笔</span></template>
        </el-table-column>
        <el-table-column label="原负责人" width="110">
          <template #default="{ row }"><span class="muted">{{ row.preOwnerName || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="进池时间" width="130">
          <template #default="{ row }"><span class="num muted">{{ shortTime(row.poolTime) }}</span></template>
        </el-table-column>
        <el-table-column label="进池原因" width="110">
          <template #default="{ row }"><span class="muted">{{ row.poolReason || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <div class="ops" @click.stop>
              <el-button link type="primary" @click="onClaim(row)">领取</el-button>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="公海是空的，说明大家都跟得很紧" :image-size="72" />
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

  <el-drawer v-model="drawer" size="480px">
    <template #header>
      <div>
        <div class="d-title">{{ current.customerName }}</div>
        <div class="d-sub">
          <span class="mono">{{ current.customerNo }}</span>
          <StatusDot v-if="current.level" :value="current.level" :map="CUSTOMER_LEVEL" />
        </div>
      </div>
    </template>
    <div class="info-grid">
      <div class="info-item"><label>联系人</label><span>{{ current.contact || '—' }}</span></div>
      <div class="info-item"><label>联系电话</label><span class="num">{{ current.phone || '—' }}</span></div>
      <div class="info-item"><label>行业</label><span>{{ current.industry || '—' }}</span></div>
      <div class="info-item"><label>地址</label><span>{{ current.address || '—' }}</span></div>
      <div class="info-item"><label>来源</label><span>{{ current.source || '—' }}</span></div>
      <div class="info-item"><label>历史订单</label><span class="num">{{ current.orderCount }} 笔</span></div>
      <div class="info-item"><label>原负责人</label><span>{{ current.preOwnerName || '—' }}</span></div>
      <div class="info-item"><label>进池时间</label><span class="num">{{ shortTime(current.poolTime) }}</span></div>
    </div>
    <div v-if="current.remark" class="remark">{{ current.remark }}</div>
    <div class="section-title">跟进记录</div>
    <FollowPanel v-if="current.id" biz-type="customer" :biz-id="current.id" />
  </el-drawer>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { CUSTOMER_LEVEL } from '@/utils/dict'
import { shortTime } from '@/utils/format'
import StatusDot from '@/components/StatusDot.vue'
import FollowPanel from '@/components/FollowPanel.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const poolDays = ref(60)
const drawer = ref(false)
const current = ref({})
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.keyword) delete params.keyword
    const data = await request.get('/customer/pool', { params })
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

function openDetail(row) {
  current.value = row
  drawer.value = true
}

async function onClaim(row) {
  try {
    await ElMessageBox.confirm(`领取客户「${row.customerName}」后由你负责维护，记得及时跟进。`, '领取客户', {
      confirmButtonText: '确认领取'
    })
  } catch {
    return
  }
  await request.put(`/customer/${row.id}/claim`)
  ElMessage.success('领取成功，已转到你的客户档案')
  load()
}

onMounted(async () => {
  poolDays.value = await request.get('/customer/pool/days')
  await load()
})
</script>

<style scoped>
.count {
  font-size: 12px;
  color: var(--text-3);
}

.count b {
  color: var(--text-2);
  font-weight: 500;
}

.ops {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
  white-space: nowrap;
}

.ops :deep(.el-button) {
  padding: 0 4px;
  font-size: 13px;
}

:deep(.clickable) {
  cursor: pointer;
}

.remark {
  margin-top: 18px;
  padding: 12px 14px;
  background: #fafbfc;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.7;
}
</style>
