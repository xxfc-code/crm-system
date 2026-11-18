<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="报价单号 / 客户" clearable style="width: 230px" @keyup.enter="search" @clear="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
        <el-option v-for="s in STATUSES" :key="s" :label="s" :value="s" />
      </el-select>
      <span class="count">共 {{ total }} 张</span>
      <div class="grow" />
      <el-button v-if="hasPerm('crm:quotation:add')" type="primary" :icon="Plus" @click="openCreate">新建报价</el-button>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list" @row-click="openDetail" row-class-name="clickable">
        <el-table-column label="报价单号" width="110">
          <template #default="{ row }"><span class="mono">{{ row.quoteNo }}</span></template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="180" />
        <el-table-column label="报价日期" width="110">
          <template #default="{ row }"><span class="num muted">{{ day(row.quoteDate) }}</span></template>
        </el-table-column>
        <el-table-column label="有效期至" width="110">
          <template #default="{ row }"><span class="num muted">{{ day(row.validUntil) }}</span></template>
        </el-table-column>
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }"><span class="num amount">{{ amount(row.totalAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <StatusDot :value="row.status" :map="STATUS_MAP" />
            <el-tag v-if="row.expired === 1" size="small" type="danger" style="margin-left: 6px">已过期</el-tag>
            <el-tag v-if="row.approveStatus === 1" size="small" type="warning" style="margin-left: 6px">待审批</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <div class="ops" @click.stop>
              <el-button link type="primary" @click="openDetail(row)">明细</el-button>
              <el-button
                v-if="hasPerm('crm:quotation:edit') && row.approveStatus === 1"
                link
                type="warning"
                @click="onApprove(row)"
              >审批</el-button>
              <el-button
                v-if="hasPerm('crm:quotation:edit') && row.status === '已确认' && !row.convertedCount"
                link
                type="success"
                @click="onConvert(row)"
              >转订单</el-button>
              <el-button v-if="hasPerm('crm:quotation:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="hasPerm('crm:quotation:remove')" link type="danger" @click="onRemove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无报价单" :image-size="72" />
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

  <el-dialog v-model="dialog" :title="form.id ? '编辑报价单' : '新建报价单'" width="780px" destroy-on-close>
    <el-form :model="form" label-position="top">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="客户">
            <el-select v-model="form.customerId" filterable style="width: 100%" @change="onCustomerChange">
              <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="报价日期">
            <el-date-picker v-model="form.quoteDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="有效期至">
            <el-date-picker v-model="form.validUntil" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="section-title">
      报价明细
      <el-button link type="primary" :icon="Plus" @click="addItem">添加一行</el-button>
    </div>

    <el-table :data="form.items" size="small">
      <el-table-column label="产品" min-width="220">
        <template #default="{ row }">
          <el-select v-model="row.productId" filterable placeholder="选择产品" style="width: 100%" @change="(id) => onProductChange(row, id)">
            <el-option-group v-for="g in groupedProducts" :key="g.label" :label="g.label">
              <el-option v-for="p in g.options" :key="p.id" :label="`${p.productName} · ¥${p.price}`" :value="p.id" />
            </el-option-group>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="110">
        <template #default="{ row }">
          <el-input-number v-model="row.qty" :min="1" controls-position="right" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="单价" width="130">
        <template #default="{ row }">
          <el-input-number v-model="row.price" :min="0" :step="100" controls-position="right" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="金额" width="110" align="right">
        <template #default="{ row }"><span class="num">{{ amount(row.qty * row.price) }}</span></template>
      </el-table-column>
      <el-table-column label="折扣" width="90" align="right">
        <template #default="{ row }">
          <span v-if="row.standardPrice && row.price < row.standardPrice" class="cut">{{ discount(row) }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column width="60" align="center">
        <template #default="{ $index }">
          <el-button link type="danger" @click="form.items.splice($index, 1)">删</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="total-line">
      合计 <b class="num">{{ money(totalAmount) }}</b>
      <span class="gp">毛利 {{ money(grossProfit) }}</span>
    </div>

    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="drawer" size="560px">
    <template #header>
      <div>
        <div class="d-title">{{ current.quoteNo }}</div>
        <div class="d-sub">
          <span>{{ current.customerName }}</span>
          <StatusDot v-if="current.status" :value="current.status" :map="STATUS_MAP" />
        </div>
      </div>
    </template>

    <div class="amount-big">
      <span class="cur">¥</span>
      <span class="num">{{ Number(current.totalAmount || 0).toLocaleString('zh-CN') }}</span>
    </div>

    <div class="info-grid" style="margin-top: 22px">
      <div class="info-item"><label>报价日期</label><span class="num">{{ day(current.quoteDate) }}</span></div>
      <div class="info-item"><label>有效期至</label><span class="num">{{ day(current.validUntil) }}</span></div>
      <div class="info-item"><label>负责人</label><span>{{ current.ownerName || '—' }}</span></div>
      <div class="info-item"><label>创建时间</label><span class="num">{{ shortTime(current.createTime) }}</span></div>
    </div>

    <div class="section-title">明细</div>
    <el-table :data="current.items || []" size="small">
      <el-table-column prop="productName" label="产品" min-width="180" />
      <el-table-column prop="qty" label="数量" width="80" align="right" />
      <el-table-column label="单价" width="110" align="right">
        <template #default="{ row }"><span class="num">{{ amount(row.price) }}</span></template>
      </el-table-column>
      <el-table-column label="金额" width="110" align="right">
        <template #default="{ row }"><span class="num">{{ amount(row.amount) }}</span></template>
      </el-table-column>
    </el-table>

    <div v-if="current.remark" class="remark">{{ current.remark }}</div>
  </el-drawer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'
import { day, money, shortTime, amount } from '@/utils/format'
import StatusDot from '@/components/StatusDot.vue'

const STATUSES = ['草稿', '已发送', '已确认', '已作废']
const STATUS_MAP = {
  草稿: { label: '草稿', color: '#9B949E' },
  已发送: { label: '已发送', color: '#F04E8B' },
  已确认: { label: '已确认', color: '#2FB98A' },
  已作废: { label: '已作废', color: '#E0635B' }
}

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const customers = ref([])
const products = ref([])
const categories = ref([])
const priceMap = ref({})

const groupedProducts = computed(() =>
  categories.value.map((c) => ({
    label: c.categoryName,
    options: products.value.filter((p) => p.categoryId === c.id)
  }))
)
const dialog = ref(false)
const drawer = ref(false)
const current = ref({})
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null })

const form = reactive({ id: null, customerId: null, quoteDate: null, validUntil: null, status: '草稿', remark: '', items: [] })

const totalAmount = computed(() => form.items.reduce((s, i) => s + (i.qty || 0) * (i.price || 0), 0))
const grossProfit = computed(() => totalAmount.value - form.items.reduce((s, i) => s + (i.qty || 0) * (i.cost || 0), 0))
const discount = (row) => ((row.price / row.standardPrice) * 10).toFixed(1) + ' 折'

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach((k) => {
      if (!params[k]) delete params[k]
    })
    const data = await request.get('/quotation/list', { params })
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

function resetForm() {
  Object.assign(form, { id: null, customerId: null, quoteDate: null, validUntil: null, status: '草稿', remark: '', items: [] })
  addItem()
}

function addItem() {
  form.items.push({ productId: null, productName: '', qty: 1, price: 0, standardPrice: 0, cost: 0, amount: 0 })
}

function onProductChange(row, id) {
  const p = products.value.find((x) => x.id === id)
  if (!p) return
  row.productName = p.productName
  row.cost = Number(p.cost || 0)
  // 有协议价优先用协议价，没有才用标准价
  row.price = Number(priceMap.value[id] ?? p.price)
  row.standardPrice = Number(p.price)
}

async function openCreate() {
  resetForm()
  dialog.value = true
}

async function openEdit(row) {
  const detail = await request.get(`/quotation/${row.id}`)
  Object.assign(form, {
    id: detail.id,
    customerId: detail.customerId,
    quoteDate: detail.quoteDate,
    validUntil: detail.validUntil,
    status: detail.status,
    remark: detail.remark,
    items: (detail.items || []).map((i) => ({ ...i, price: Number(i.price) }))
  })
  if (!form.items.length) addItem()
  dialog.value = true
}

async function openDetail(row) {
  current.value = await request.get(`/quotation/${row.id}`)
  drawer.value = true
}

/** 选客户后加载该客户的协议价，选产品时自动带出 */
async function onCustomerChange(customerId) {
  priceMap.value = {}
  if (!customerId) return
  const prices = await request.get('/price/list', { params: { customerId } })
  priceMap.value = Object.fromEntries(prices.list.map((p) => [p.productId, Number(p.price)]))
}

async function onSubmit() {
  if (!form.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!form.items.length || form.items.some((i) => !i.productId)) {
    ElMessage.warning('请至少添加一行有效明细')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, totalAmount: totalAmount.value }
    if (form.id) {
      await request.put('/quotation', payload)
    } else {
      await request.post('/quotation', payload)
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onApprove(row) {
  let approved
  try {
    const res = await ElMessageBox.prompt(
      `审批报价单「${row.quoteNo}」：输入「通过」或「驳回」`,
      '报价审批',
      { inputValue: '通过', confirmButtonText: '提交' }
    )
    approved = res.value.includes('驳回') ? false : true
  } catch {
    return
  }
  await request.put(`/quotation/${row.id}/approve`, null, { params: { approved } })
  ElMessage.success(approved ? '已审批通过' : '已驳回')
  load()
}

async function onConvert(row) {
  try {
    await ElMessageBox.confirm(`把报价单「${row.quoteNo}」转成订单？明细和金额会自动带过去。`, '转订单', {
      confirmButtonText: '确认转单'
    })
  } catch {
    return
  }
  await request.post(`/quotation/${row.id}/convert`)
  ElMessage.success('已生成订单，可在销售订单里查看')
  load()
}

async function onRemove(row) {
  try {
    await ElMessageBox.confirm(`确认删除报价单「${row.quoteNo}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(`/quotation/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const [cs, ps, cats] = await Promise.all([
    request.get('/customer/options'),
    request.get('/product/options'),
    request.get('/productCategory/list')
  ])
  customers.value = cs
  products.value = ps
  categories.value = cats
  await load()
})
</script>

<style scoped>
.count {
  font-size: 12px;
  color: var(--text-3);
  padding-left: 4px;
}

.amount {
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

.total-line {
  text-align: right;
  margin-top: 14px;
  font-size: 13px;
  color: var(--text-2);
}

.total-line b {
  font-size: 18px;
  color: var(--text);
  margin-left: 6px;
}

.gp {
  margin-left: 16px;
  font-size: 13px;
  color: var(--ok);
}

.cut {
  color: var(--ok);
  font-weight: 500;
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
