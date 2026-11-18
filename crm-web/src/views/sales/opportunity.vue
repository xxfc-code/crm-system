<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索商机 / 客户"
        clearable
        style="width: 230px"
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select v-model="query.stage" placeholder="阶段" clearable style="width: 130px" @change="search">
        <el-option v-for="s in OPP_STAGES" :key="s" :label="s" :value="s" />
      </el-select>

      <span class="count">共 {{ total }} 个 · 本页合计 {{ money(totalAmount) }}</span>
      <div class="grow" />

      <el-button v-if="hasPerm('crm:opportunity:add')" type="primary" :icon="Plus" @click="openCreate">新增商机</el-button>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list" @row-click="openDetail" row-class-name="clickable">
        <el-table-column label="商机名称" min-width="200">
          <template #default="{ row }">
            <div class="row-main">{{ row.oppName }}</div>
            <div class="row-sub">{{ row.customerName || '—' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="编号" width="90">
          <template #default="{ row }"><span class="mono muted">{{ row.oppNo }}</span></template>
        </el-table-column>

        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="num amount">{{ money(row.amount) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="阶段" width="130">
          <template #default="{ row }">
            <span class="dot">
              <i :style="{ background: STAGE_COLOR[row.stage] || '#9B949E' }"></i>
              <span :style="{ color: STAGE_COLOR[row.stage] || '#9B949E' }">{{ row.stage }}</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column label="赢率" width="120">
          <template #default="{ row }">
            <div class="rate">
              <div class="bar"><i :style="{ width: row.winRate + '%', background: STAGE_COLOR[row.stage] || '#9B949E' }"></i></div>
              <span class="num muted">{{ row.winRate }}%</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="预计成交" width="110">
          <template #default="{ row }"><span class="muted num">{{ day(row.expectDate) }}</span></template>
        </el-table-column>

        <el-table-column label="负责人" width="110">
          <template #default="{ row }">
            <span v-if="row.ownerName" class="owner">
              <i class="avatar">{{ initial(row.ownerName) }}</i>{{ row.ownerName }}
            </span>
            <span v-else class="muted">未分配</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="222" align="right">
          <template #default="{ row }">
            <div class="ops" @click.stop>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-if="hasPerm('crm:opportunity:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="hasPerm('crm:opportunity:stage')" link type="success" @click="openStage(row)">推进</el-button>
              <el-dropdown v-if="hasPerm('crm:opportunity:remove')" @command="(c) => onRemove(row, c)">
                <el-button link type="primary" class="more">
                  更多<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="logic">删除</el-dropdown-item>
                    <el-dropdown-item command="force">彻底删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无商机" :image-size="72" />
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

  <el-dialog v-model="dialog" :title="form.id ? '编辑商机' : '新增商机'" width="600px" destroy-on-close>
    <el-form :model="form" label-position="top">
      <el-form-item label="商机名称">
        <el-input v-model="form.oppName" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属客户">
            <el-select v-model="form.customerId" placeholder="选择客户" filterable style="width: 100%">
              <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计金额（元）">
            <el-input-number v-model="form.amount" :min="0" :step="10000" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="阶段">
            <el-select v-model="form.stage" style="width: 100%" @change="(s) => (form.winRate = STAGE_RATE[s])">
              <el-option v-for="s in OPP_STAGES" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="赢单概率">
            <el-slider v-model="form.winRate" :step="5" show-input :show-input-controls="false" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计成交日期">
            <el-date-picker v-model="form.expectDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人">
            <el-select v-model="form.ownerId" placeholder="选择员工" style="width: 100%">
              <el-option v-for="e in employees" :key="e.id" :label="`${e.empName}（${e.deptName || '未分配部门'}）`" :value="e.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>

  <!-- 阶段推进 -->
  <el-dialog v-model="stageDialog" title="推进阶段" width="380px">
    <div class="stage-now">
      当前阶段
      <span class="dot" style="margin-left: 6px">
        <i :style="{ background: STAGE_COLOR[stageForm.current] }"></i>
        <span :style="{ color: STAGE_COLOR[stageForm.current] }">{{ stageForm.current }}</span>
      </span>
    </div>
    <el-form label-position="top" style="margin-top: 14px">
      <el-form-item label="推进到">
        <el-select v-model="stageForm.stage" style="width: 100%" @change="(s) => (stageForm.winRate = STAGE_RATE[s])">
          <el-option v-for="s in OPP_STAGES" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item :label="`赢单概率 ${stageForm.winRate}%`">
        <el-slider v-model="stageForm.winRate" :step="5" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="stageDialog = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSaveStage">确认推进</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="drawer" size="480px">
    <template #header>
      <div>
        <div class="d-title">{{ current.oppName }}</div>
        <div class="d-sub">
          <span class="mono">{{ current.oppNo }}</span>
          <span class="dot">
            <i :style="{ background: STAGE_COLOR[current.stage] }"></i>
            <span :style="{ color: STAGE_COLOR[current.stage] }">{{ current.stage }}</span>
          </span>
        </div>
      </div>
    </template>

    <div class="amount-big">
      <span class="cur">¥</span>
      <span class="num">{{ Number(current.amount || 0).toLocaleString('zh-CN') }}</span>
    </div>

    <div class="info-grid" style="margin-top: 22px">
      <div class="info-item"><label>所属客户</label><span>{{ current.customerName || '—' }}</span></div>
      <div class="info-item"><label>负责人</label><span>{{ current.ownerName || '未分配' }}</span></div>
      <div class="info-item"><label>赢单概率</label><span class="num">{{ current.winRate }}%</span></div>
      <div class="info-item"><label>预计成交</label><span class="num">{{ day(current.expectDate) }}</span></div>
      <div class="info-item"><label>创建时间</label><span class="num">{{ day(current.createTime) }}</span></div>
      <div class="info-item"><label>最近更新</label><span class="num">{{ shortTime(current.updateTime) }}</span></div>
    </div>

    <div v-if="current.remark" class="remark">{{ current.remark }}</div>

    <div class="section-title">阶段轨迹</div>
    <div v-if="current.stageLogs && current.stageLogs.length" class="trail">
      <div v-for="log in current.stageLogs" :key="log.id" class="t-item">
        <span class="t-dot" :style="{ background: STAGE_COLOR[log.toStage] || '#9B949E' }"></span>
        <div class="t-main">
          <div class="t-head">
            <span class="t-stage">
              <template v-if="log.fromStage">{{ log.fromStage }} → </template>{{ log.toStage }}
            </span>
            <span class="t-when num">{{ shortTime(log.createTime) }}</span>
          </div>
          <div class="t-meta">{{ log.operatorName || '—' }} · 赢率 {{ log.winRate }}%</div>
        </div>
      </div>
    </div>
    <div v-else class="t-empty">还没有阶段流转记录</div>

    <div class="section-title">跟进记录</div>
    <FollowPanel v-if="current.id" biz-type="opportunity" :biz-id="current.id" />
  </el-drawer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'
import { OPP_STAGES, STAGE_COLOR, STAGE_RATE } from '@/utils/dict'
import { day, initial, money, shortTime } from '@/utils/format'
import { useTable } from '@/composables/useTable'
import FollowPanel from '@/components/FollowPanel.vue'

const { loading, list, total, query, load, search } = useTable('/opportunity', {
  keyword: '',
  stage: null
})

const customers = ref([])
const employees = ref([])
const dialog = ref(false)
const stageDialog = ref(false)
const drawer = ref(false)
const saving = ref(false)
const current = ref({})

const empty = {
  id: null, oppName: '', customerId: null, amount: 0, stage: '初步接洽',
  winRate: 10, expectDate: null, ownerId: null, remark: ''
}
const form = reactive({ ...empty })
const stageForm = reactive({ id: null, current: '', stage: '', winRate: 10 })

const totalAmount = computed(() => list.value.reduce((s, r) => s + Number(r.amount || 0), 0))

async function init() {
  const [cs, es] = await Promise.all([request.get('/customer/options'), request.get('/employee/options')])
  customers.value = cs
  employees.value = es
  await load()
}

function openCreate() {
  Object.assign(form, { ...empty })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    oppName: row.oppName,
    customerId: row.customerId,
    amount: Number(row.amount),
    stage: row.stage,
    winRate: row.winRate,
    expectDate: row.expectDate,
    ownerId: row.ownerId,
    remark: row.remark
  })
  dialog.value = true
}

async function onSubmit() {
  if (!form.oppName || !form.customerId) {
    ElMessage.warning('商机名称和所属客户必填')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await request.put('/opportunity', form)
    } else {
      await request.post('/opportunity', form)
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openStage(row) {
  stageForm.id = row.id
  stageForm.current = row.stage
  stageForm.stage = row.stage
  stageForm.winRate = row.winRate
  stageDialog.value = true
}

async function onSaveStage() {
  saving.value = true
  try {
    await request.put(`/opportunity/${stageForm.id}/stage`, null, {
      params: { stage: stageForm.stage, winRate: stageForm.winRate }
    })
    ElMessage.success('阶段已更新')
    stageDialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openDetail(row) {
  current.value = row
  drawer.value = true
}

async function onRemove(row, type) {
  const label = type === 'force' ? '彻底删除' : '删除'
  try {
    await ElMessageBox.confirm(`确认${label}商机「${row.oppName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(type === 'force' ? `/opportunity/${row.id}/force` : `/opportunity/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(init)
</script>

<style scoped>
.count {
  font-size: 12px;
  color: var(--text-3);
  padding-left: 4px;
}

.amount {
  font-weight: 500;
  color: var(--text);
}

.rate {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bar {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: #F2EAEE;
  overflow: hidden;
}

.bar i {
  display: block;
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s;
}

.owner {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 12px;
  font-style: normal;
  display: inline-flex;
  align-items: center;
  justify-content: center;
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

/* 阶段轨迹 */
.trail {
  position: relative;
}

.t-item {
  display: flex;
  gap: 10px;
  padding-bottom: 14px;
  position: relative;
}

.t-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 3.5px;
  top: 12px;
  bottom: 0;
  width: 1px;
  background: var(--line);
}

.t-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
}

.t-main {
  flex: 1;
  min-width: 0;
}

.t-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.t-stage {
  font-size: 13px;
  color: var(--text);
}

.t-when {
  font-size: 12px;
  color: var(--text-3);
}

.t-meta {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 3px;
}

.t-empty {
  font-size: 12px;
  color: var(--text-3);
  padding: 6px 0 2px;
}

.more {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

:deep(.clickable) {
  cursor: pointer;
}

.stage-now {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--text-2);
}

.amount-big {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.amount-big .cur {
  font-size: 14px;
  color: var(--text-3);
}

.amount-big .num {
  font-size: 28px;
  font-weight: 500;
  letter-spacing: -0.5px;
}

.remark {
  margin-top: 18px;
  padding: 12px 14px;
  background: #FDFAFB;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.7;
}
</style>
