<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索客户名称 / 联系人 / 电话"
        clearable
        style="width: 250px"
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select v-model="query.level" placeholder="客户级别" clearable style="width: 120px" @change="search">
        <el-option v-for="(v, k) in CUSTOMER_LEVEL" :key="k" :label="`${k} · ${v.label}`" :value="k" />
      </el-select>

      <span class="count">共 {{ total }} 家</span>
      <div class="grow" />

      <el-button v-if="hasPerm('crm:customer:add')" type="primary" :icon="Plus" @click="openCreate">新增客户</el-button>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list" @row-click="openDetail" row-class-name="clickable">
        <el-table-column label="客户名称" min-width="200">
          <template #default="{ row }">
            <div class="row-main">{{ row.customerName }}</div>
            <div class="row-sub">{{ row.contact || '—' }}<span v-if="row.phone"> · {{ row.phone }}</span></div>
          </template>
        </el-table-column>

        <el-table-column label="编号" width="96">
          <template #default="{ row }"><span class="mono muted">{{ row.customerNo }}</span></template>
        </el-table-column>

        <el-table-column label="级别" width="90">
          <template #default="{ row }"><StatusDot :value="row.level" :map="CUSTOMER_LEVEL" /></template>
        </el-table-column>

        <el-table-column label="行业" width="110">
          <template #default="{ row }"><span class="muted">{{ row.industry || '—' }}</span></template>
        </el-table-column>

        <el-table-column label="关联" width="120">
          <template #default="{ row }">
            <span class="muted num">商机 {{ row.oppCount }} · 订单 {{ row.orderCount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="负责人" width="118">
          <template #default="{ row }">
            <span v-if="row.ownerName" class="owner">
              <i class="avatar">{{ initial(row.ownerName) }}</i>{{ row.ownerName }}
            </span>
            <span v-else class="muted">未分配</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="170" align="right">
          <template #default="{ row }">
            <div class="ops" @click.stop>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-if="hasPerm('crm:customer:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-dropdown v-if="hasPerm('crm:customer:remove')" @command="(c) => onRemove(row, c)">
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
          <el-empty description="暂无客户数据" :image-size="72" />
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

  <el-dialog v-model="dialog" :title="form.id ? '编辑客户' : '新增客户'" width="600px" destroy-on-close>
    <el-form :model="form" label-position="top">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="客户名称">
            <el-input v-model="form.customerName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系人">
            <el-input v-model="form.contact" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话">
            <el-input v-model="form.phone" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户级别">
            <el-select v-model="form.level" style="width: 100%">
              <el-option v-for="(v, k) in CUSTOMER_LEVEL" :key="k" :label="`${k} · ${v.label}`" :value="k" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属行业">
            <el-input v-model="form.industry" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="门店编号">
            <el-input v-model="form.storeNo" placeholder="如 MD-ZJ-001" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属区域">
            <el-input v-model="form.region" placeholder="如 华东 / 郑州" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="签约日期">
            <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="首单日期">
            <el-date-picker v-model="form.firstOrderDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="营业面积（平米）">
            <el-input-number v-model="form.storeArea" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="来源">
            <el-select v-model="form.source" clearable style="width: 100%">
              <el-option v-for="s in SOURCES" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人">
            <el-select v-model="form.ownerId" placeholder="选择员工" style="width: 100%">
              <el-option v-for="e in employees" :key="e.id" :label="`${e.empName}（${e.deptName || '未分配部门'}）`" :value="e.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="地址">
            <el-input v-model="form.address" />
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
      <div class="info-item"><label>邮箱</label><span>{{ current.email || '—' }}</span></div>
      <div class="info-item"><label>所属行业</label><span>{{ current.industry || '—' }}</span></div>
      <div class="info-item"><label>门店编号</label><span>{{ current.storeNo || '—' }}</span></div>
      <div class="info-item"><label>所属区域</label><span>{{ current.region || '—' }}</span></div>
      <div class="info-item"><label>签约日期</label><span class="num">{{ day(current.signDate) }}</span></div>
      <div class="info-item"><label>首单日期</label><span class="num">{{ day(current.firstOrderDate) }}</span></div>
      <div class="info-item"><label>营业面积</label><span class="num">{{ current.storeArea ? current.storeArea + ' ㎡' : '—' }}</span></div>
      <div class="info-item"><label>来源</label><span>{{ current.source || '—' }}</span></div>
      <div class="info-item"><label>负责人</label><span>{{ current.ownerName || '未分配' }}</span></div>
      <div class="info-item"><label>商机数</label><span class="num">{{ current.oppCount ?? 0 }}</span></div>
      <div class="info-item"><label>订单数</label><span class="num">{{ current.orderCount ?? 0 }}</span></div>
      <div class="info-item" style="grid-column: 1 / -1"><label>地址</label><span>{{ current.address || '—' }}</span></div>
    </div>

    <div v-if="current.remark" class="remark">{{ current.remark }}</div>

    <div class="section-title">跟进记录</div>
    <FollowPanel v-if="current.id" biz-type="customer" :biz-id="current.id" />
  </el-drawer>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'
import { CUSTOMER_LEVEL, SOURCES } from '@/utils/dict'
import { day, initial } from '@/utils/format'
import { useTable } from '@/composables/useTable'
import StatusDot from '@/components/StatusDot.vue'
import FollowPanel from '@/components/FollowPanel.vue'

const { loading, list, total, query, load, search } = useTable('/customer', {
  keyword: '',
  level: null
})

const employees = ref([])
const dialog = ref(false)
const drawer = ref(false)
const saving = ref(false)
const current = ref({})

const empty = {
  id: null, customerName: '', contact: '', phone: '', email: '',
  level: 'C', industry: '', address: '', source: '', ownerId: null, remark: '',
  storeNo: '', signDate: null, region: '', firstOrderDate: null, storeArea: null
}
const form = reactive({ ...empty })

async function init() {
  employees.value = await request.get('/employee/options')
  await load()
}

function openCreate() {
  Object.assign(form, { ...empty })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    customerName: row.customerName,
    contact: row.contact,
    phone: row.phone,
    email: row.email,
    level: row.level,
    industry: row.industry,
    storeNo: row.storeNo,
    signDate: row.signDate,
    region: row.region,
    firstOrderDate: row.firstOrderDate,
    storeArea: row.storeArea,
    address: row.address,
    source: row.source,
    ownerId: row.ownerId,
    remark: row.remark
  })
  dialog.value = true
}

async function onSubmit() {
  if (!form.customerName) {
    ElMessage.warning('请填写客户名称')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await request.put('/customer', form)
    } else {
      await request.post('/customer', form)
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

async function onRemove(row, type) {
  const label = type === 'force' ? '彻底删除' : '删除'
  try {
    await ElMessageBox.confirm(`确认${label}客户「${row.customerName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(type === 'force' ? `/customer/${row.id}/force` : `/customer/${row.id}`)
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
