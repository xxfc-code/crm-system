<template>
  <el-card shadow="never">
    <el-tabs v-model="tab" class="tabs" @tab-change="onTabChange">
      <el-tab-pane label="我的线索" name="mine" />
      <el-tab-pane name="pool">
        <template #label>
          公海池
          <span v-if="pool.total" class="badge">{{ pool.total }}</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 我的线索 -->
    <div v-if="tab === 'mine'" class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索名称 / 公司 / 电话"
        clearable
        style="width: 240px"
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select v-model="query.status" placeholder="状态" clearable style="width: 118px" @change="search">
        <el-option v-for="(v, k) in LEAD_STATUS" :key="k" :label="v.label" :value="Number(k)" />
      </el-select>

      <el-select v-model="query.source" placeholder="来源" clearable style="width: 128px" @change="search">
        <el-option v-for="s in SOURCES" :key="s" :label="s" :value="s" />
      </el-select>

      <span class="count">共 {{ total }} 条</span>
      <div class="grow" />
      <el-button v-if="hasPerm('crm:lead:add')" type="primary" :icon="Plus" @click="openCreate">新增线索</el-button>
    </div>

    <!-- 公海池 -->
    <div v-else class="toolbar">
      <el-input v-model="pool.keyword" placeholder="搜索名称 / 公司" clearable style="width: 240px" @keyup.enter="loadPool" @clear="loadPool">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <span class="count">超过 <b>{{ poolDays }}</b> 天没有跟进的线索会自动回收到这里，先到先得</span>
      <div class="grow" />
    </div>

    <div class="table-wrap">
      <!-- 我的线索 -->
      <el-table v-if="tab === 'mine'" v-loading="loading" :data="list" @row-click="openDetail" row-class-name="clickable">
        <el-table-column label="线索" min-width="190">
          <template #default="{ row }">
            <div class="row-main">{{ row.leadName }}</div>
            <div class="row-sub">{{ row.company || '—' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="编号" width="96">
          <template #default="{ row }"><span class="mono muted">{{ row.leadNo }}</span></template>
        </el-table-column>

        <el-table-column label="来源" width="120">
          <template #default="{ row }"><span class="muted">{{ row.source || '—' }}</span></template>
        </el-table-column>

        <el-table-column label="状态" width="96">
          <template #default="{ row }"><StatusDot :value="row.status" :map="LEAD_STATUS" /></template>
        </el-table-column>

        <el-table-column label="负责人" width="118">
          <template #default="{ row }">
            <span v-if="row.ownerName" class="owner">
              <i class="avatar">{{ initial(row.ownerName) }}</i>{{ row.ownerName }}
            </span>
            <span v-else class="muted">未分配</span>
          </template>
        </el-table-column>

        <el-table-column label="下次跟进" width="120">
          <template #default="{ row }">
            <span class="num muted">{{ shortTime(row.nextTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="212" align="right">
          <template #default="{ row }">
            <div class="ops" @click.stop>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-if="hasPerm('crm:lead:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button
                v-if="hasPerm('crm:lead:convert') && (row.status === 0 || row.status === 1)"
                link
                type="success"
                @click="onConvert(row)"
              >转化</el-button>
              <el-dropdown v-if="hasPerm('crm:lead:remove')" @command="(c) => onRemove(row, c)">
                <el-button link type="primary" class="more">更多<el-icon><ArrowDown /></el-icon></el-button>
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
          <el-empty description="暂无线索，试试调整筛选条件" :image-size="72" />
        </template>
      </el-table>

      <!-- 公海池 -->
      <el-table v-else v-loading="pool.loading" :data="pool.list" @row-click="openDetail" row-class-name="clickable">
        <el-table-column label="线索" min-width="200">
          <template #default="{ row }">
            <div class="row-main">{{ row.leadName }}</div>
            <div class="row-sub">{{ row.company || '—' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="编号" width="96">
          <template #default="{ row }"><span class="mono muted">{{ row.leadNo }}</span></template>
        </el-table-column>

        <el-table-column label="来源" width="120">
          <template #default="{ row }"><span class="muted">{{ row.source || '—' }}</span></template>
        </el-table-column>

        <el-table-column label="原负责人" width="120">
          <template #default="{ row }"><span class="muted">{{ row.preOwnerName || '—' }}</span></template>
        </el-table-column>

        <el-table-column label="进池时间" width="130">
          <template #default="{ row }"><span class="num muted">{{ shortTime(row.poolTime) }}</span></template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="right">
          <template #default="{ row }">
            <div class="ops" @click.stop>
              <el-button v-if="hasPerm('crm:lead:edit')" link type="primary" @click="onClaim(row)">领取</el-button>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="公海是空的，说明大家跟进得都挺勤" :image-size="72" />
        </template>
      </el-table>
    </div>

    <div class="pager">
      <el-pagination
        v-if="tab === 'mine'"
        small
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="(p) => { query.pageNum = p; load() }"
      />
      <el-pagination
        v-else
        small
        layout="total, prev, pager, next"
        :total="pool.total"
        :page-size="pool.pageSize"
        :current-page="pool.pageNum"
        @current-change="(p) => { pool.pageNum = p; loadPool() }"
      />
    </div>
  </el-card>

  <!-- 新增 / 编辑 -->
  <el-dialog v-model="dialog" :title="form.id ? '编辑线索' : '新增线索'" width="560px" destroy-on-close>
    <el-form :model="form" label-position="top">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="线索名称">
            <el-input v-model="form.leadName" placeholder="联系人姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公司名称">
            <el-input v-model="form.company" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话">
            <el-input v-model="form.phone" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="来源">
            <el-select v-model="form.source" style="width: 100%">
              <el-option v-for="s in SOURCES" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="(v, k) in LEAD_STATUS" :key="k" :label="v.label" :value="Number(k)" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人">
            <el-select v-model="form.ownerId" placeholder="默认归自己" style="width: 100%">
              <el-option v-for="e in employees" :key="e.id" :label="`${e.empName}（${e.deptName || '未分配部门'}）`" :value="e.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="下次跟进">
            <el-date-picker v-model="form.nextTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
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

  <!-- 详情 -->
  <el-drawer v-model="drawer" size="480px">
    <template #header>
      <div>
        <div class="d-title">{{ current.leadName }}</div>
        <div class="d-sub">
          <span class="mono">{{ current.leadNo }}</span>
          <StatusDot :value="current.status" :map="LEAD_STATUS" />
        </div>
      </div>
    </template>

    <div class="info-grid">
      <div class="info-item"><label>公司名称</label><span>{{ current.company || '—' }}</span></div>
      <div class="info-item"><label>联系电话</label><span class="num">{{ current.phone || '—' }}</span></div>
      <div class="info-item"><label>来源</label><span>{{ current.source || '—' }}</span></div>
      <div class="info-item"><label>负责人</label><span>{{ current.ownerName || '公海（无主）' }}</span></div>
      <div class="info-item"><label>所属部门</label><span>{{ current.deptName || '—' }}</span></div>
      <div class="info-item"><label>下次跟进</label><span class="num">{{ shortTime(current.nextTime) }}</span></div>
      <div class="info-item"><label>最近跟进</label><span class="num">{{ shortTime(current.lastFollowTime) }}</span></div>
      <div class="info-item"><label>进池时间</label><span class="num">{{ current.poolTime ? shortTime(current.poolTime) : '—' }}</span></div>
    </div>

    <div v-if="current.remark" class="remark">{{ current.remark }}</div>

    <div class="section-title">跟进记录</div>
    <FollowPanel v-if="current.id" biz-type="lead" :biz-id="current.id" />
  </el-drawer>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'
import { LEAD_STATUS, SOURCES } from '@/utils/dict'
import { initial, shortTime } from '@/utils/format'
import { useTable } from '@/composables/useTable'
import StatusDot from '@/components/StatusDot.vue'
import FollowPanel from '@/components/FollowPanel.vue'

const tab = ref('mine')

const { loading, list, total, query, load, search } = useTable('/lead', {
  keyword: '',
  status: null,
  source: null
})

const pool = reactive({ loading: false, list: [], total: 0, pageNum: 1, pageSize: 10, keyword: '' })
const poolDays = ref(15)

const employees = ref([])
const dialog = ref(false)
const drawer = ref(false)
const saving = ref(false)
const current = ref({})

const empty = {
  id: null, leadName: '', company: '', phone: '', source: '官网咨询',
  status: 0, ownerId: null, deptId: null, nextTime: null, remark: ''
}
const form = reactive({ ...empty })

// 选负责人自动带出部门
watch(() => form.ownerId, (id) => {
  const emp = employees.value.find((e) => e.id === id)
  if (emp) form.deptId = emp.deptId
})

async function loadPool() {
  pool.loading = true
  try {
    const data = await request.get('/lead/pool', {
      params: { pageNum: pool.pageNum, pageSize: pool.pageSize, keyword: pool.keyword || undefined }
    })
    pool.list = data.list
    pool.total = data.total
  } finally {
    pool.loading = false
  }
}

function onTabChange(name) {
  name === 'pool' ? loadPool() : load()
}

async function init() {
  employees.value = await request.get('/employee/options')
  poolDays.value = await request.get('/lead/pool/days')
  await load()
  // 顺手拿一下公海数量，好在标签上显示
  await loadPool()
}

function openCreate() {
  Object.assign(form, { ...empty })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    leadName: row.leadName,
    company: row.company,
    phone: row.phone,
    source: row.source,
    status: row.status,
    ownerId: row.ownerId,
    deptId: row.deptId,
    nextTime: row.nextTime,
    remark: row.remark
  })
  dialog.value = true
}

async function onSubmit() {
  if (!form.leadName) {
    ElMessage.warning('请填写线索名称')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await request.put('/lead', form)
    } else {
      await request.post('/lead', form)
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openDetail(row) {
  current.value = row
  drawer.value = true
}

async function onConvert(row) {
  try {
    await ElMessageBox.confirm(
      `将「${row.leadName}」转化为客户？系统会自动创建客户档案并回写线索状态。`,
      '线索转化',
      { confirmButtonText: '确认转化', cancelButtonText: '再想想' }
    )
  } catch {
    return
  }
  await request.put(`/lead/${row.id}/convert`)
  ElMessage.success('已转化为客户，可在客户模块查看')
  load()
}

async function onClaim(row) {
  try {
    await ElMessageBox.confirm(`领取「${row.leadName}」后线索归你负责，记得及时跟进。`, '领取线索', {
      confirmButtonText: '确认领取'
    })
  } catch {
    return
  }
  await request.put(`/lead/${row.id}/claim`)
  ElMessage.success('领取成功，已转到我的线索')
  loadPool()
  load()
}

async function onRemove(row, type) {
  const label = type === 'force' ? '彻底删除' : '删除'
  try {
    await ElMessageBox.confirm(`确认${label}线索「${row.leadName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(type === 'force' ? `/lead/${row.id}/force` : `/lead/${row.id}`)
  ElMessage.success('已删除')
  tab.value === 'pool' ? loadPool() : load()
}

onMounted(init)
</script>

<style scoped>
.tabs {
  padding: 4px 20px 0;
}

.tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--line);
}

.tabs :deep(.el-tabs__item) {
  font-size: 13.5px;
}

.badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  margin-left: 5px;
  border-radius: 9px;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}

.count {
  font-size: 12px;
  color: var(--text-3);
  padding-left: 4px;
}

.count b {
  color: var(--text-2);
  font-weight: 500;
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

.more {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

:deep(.clickable) {
  cursor: pointer;
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
