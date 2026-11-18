<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="订单号 / 客户" clearable style="width: 230px" @keyup.enter="search" @clear="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 130px" @change="search">
        <el-option v-for="(v, k) in ORDER_STATUS" :key="k" :label="v.label" :value="k" />
      </el-select>
      <span class="count">共 {{ total }} 张 · 本页合计 {{ money(pageAmount) }}</span>
      <div class="grow" />
      <el-button v-if="hasPerm('crm:order:add')" type="primary" :icon="Plus" @click="openCreate">新建订单</el-button>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list" @row-click="openDetail" row-class-name="clickable">
        <el-table-column label="订单号" width="105">
          <template #default="{ row }"><span class="mono">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="160" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }"><span class="num amount">{{ amount(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column label="毛利" width="120" align="right">
          <template #default="{ row }">
            <span class="num" :class="row.grossProfit >= 0 ? 'profit' : 'loss'">{{ amount(row.grossProfit) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusDot :value="row.status" :map="ORDER_STATUS" /></template>
        </el-table-column>
        <el-table-column label="回款日期" width="110">
          <template #default="{ row }"><span class="num muted">{{ day(row.lastPayDate) }}</span></template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column label="操作" width="210" align="right">
          <template #default="{ row }">
            <div class="ops" @click.stop>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-if="hasPerm('crm:order:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="hasPerm('crm:delivery:add')" link type="primary" @click="openDelivery(row)">发货</el-button>
              <el-button v-if="hasPerm('crm:payment:add')" link type="primary" @click="openPayment(row)">收款</el-button>
              <el-dropdown v-if="hasPerm('crm:order:remove')" @command="(c) => onCommand(row, c)">
                <el-button link type="danger">
                  更多<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="cancel">取消订单</el-dropdown-item>
                    <el-dropdown-item command="restore">恢复订单</el-dropdown-item>
                    <el-dropdown-item command="force" divided>彻底删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无订单" :image-size="72" />
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

  <!-- 新增 / 编辑 -->
  <el-dialog v-model="dialog" :title="form.id ? '编辑订单' : '新建订单'" width="800px" destroy-on-close>
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
          <el-form-item label="关联商机">
            <el-select v-model="form.oppId" clearable placeholder="可不选" style="width: 100%">
              <el-option v-for="o in opportunities" :key="o.id" :label="o.oppName" :value="o.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="签约日期">
            <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="section-title">
      商品明细
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
      <el-table-column label="小计" width="110" align="right">
        <template #default="{ row }"><span class="num">{{ amount(row.qty * row.price) }}</span></template>
      </el-table-column>
      <el-table-column width="60" align="center">
        <template #default="{ $index }">
          <el-button link type="danger" @click="form.items.splice($index, 1)">删</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="total-line">
      合计 <b class="num">{{ money(formTotal) }}</b>
    </div>

    <el-form label-position="top" style="margin-top: 8px">
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
  <el-drawer v-model="drawer" size="560px">
    <template #header>
      <div>
        <div class="d-title">{{ current.orderNo }}</div>
        <div class="d-sub">
          <span>{{ current.customerName }}</span>
          <StatusDot v-if="current.status" :value="current.status" :map="ORDER_STATUS" />
        </div>
      </div>
    </template>

    <div class="amount-big">
      <span class="cur">¥</span>
      <span class="num">{{ Number(current.amount || 0).toLocaleString('zh-CN') }}</span>
      <span class="gp">毛利 {{ money(current.grossProfit) }}</span>
    </div>

    <div class="info-grid" style="margin-top: 22px">
      <div class="info-item"><label>关联商机</label><span>{{ current.oppName || '—' }}</span></div>
      <div class="info-item"><label>负责人</label><span>{{ current.ownerName || '—' }}</span></div>
      <div class="info-item"><label>签约日期</label><span class="num">{{ day(current.signDate) }}</span></div>
      <div class="info-item"><label>最近回款</label><span class="num">{{ day(current.lastPayDate) }}</span></div>
      <div class="info-item"><label>已收金额</label><span class="num">{{ amount(current.paidAmount) }}</span></div>
      <div class="info-item"><label>未收余额</label><span class="num">{{ amount(remainOf(current)) }}</span></div>
    </div>

    <div class="section-title">商品明细</div>
    <el-table :data="current.items || []" size="small">
      <el-table-column prop="productName" label="产品" min-width="150" />
      <el-table-column label="数量" width="90" align="right">
        <template #default="{ row }"><span class="num">{{ row.qty }} {{ row.unit }}</span></template>
      </el-table-column>
      <el-table-column label="单价" width="100" align="right">
        <template #default="{ row }"><span class="num">{{ amount(row.price) }}</span></template>
      </el-table-column>
      <el-table-column label="小计" width="100" align="right">
        <template #default="{ row }"><span class="num">{{ amount(row.amount) }}</span></template>
      </el-table-column>
    </el-table>

    <div v-if="current.remark" class="remark">{{ current.remark }}</div>
  </el-drawer>

  <!-- 发货 -->
  <el-dialog v-model="deliveryDialog" title="订单发货" width="460px">
    <el-form label-position="top">
      <el-row :gutter="14">
        <el-col :span="12">
          <el-form-item label="收货人"><el-input v-model="deliveryForm.receiver" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话"><el-input v-model="deliveryForm.phone" /></el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="收货地址"><el-input v-model="deliveryForm.address" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="deliveryDialog = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmitDelivery">确认发货</el-button>
    </template>
  </el-dialog>

  <!-- 收款 -->
  <el-dialog v-model="paymentDialog" title="登记收款" width="460px">
    <div class="remain-tip">
      订单金额 <b class="num">{{ money(current.amount) }}</b>
      ，已收 <b class="num">{{ money(current.paidAmount) }}</b>
      ，未收 <b class="num strong">{{ money(remainOf(current)) }}</b>
    </div>
    <el-form label-position="top">
      <el-row :gutter="14">
        <el-col :span="12">
          <el-form-item label="收款金额（元）">
            <el-input-number v-model="paymentForm.amount" :min="0" :step="1000" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收款日期">
            <el-date-picker v-model="paymentForm.payDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="收款方式">
        <el-select v-model="paymentForm.payType" style="width: 100%">
          <el-option v-for="t in PAY_TYPES" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="paymentForm.remark" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="paymentDialog = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmitPayment">确认收款</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'
import { ORDER_STATUS } from '@/utils/dict'
import { day, money, amount } from '@/utils/format'
import StatusDot from '@/components/StatusDot.vue'

const PAY_TYPES = ['银行转账', '现金', '支票', '承兑汇票', '微信支付']

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const customers = ref([])
const oppAll = ref([])
const opportunities = ref([])
const products = ref([])
const categories = ref([])
const dialog = ref(false)
const drawer = ref(false)
const deliveryDialog = ref(false)
const paymentDialog = ref(false)
const current = ref({})
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null })

const form = reactive({ id: null, customerId: null, oppId: null, signDate: null, remark: '', items: [] })
const deliveryForm = reactive({ orderId: null, receiver: '', phone: '', address: '' })
const paymentForm = reactive({ orderId: null, amount: 0, payDate: null, payType: '银行转账', remark: '' })

const groupedProducts = computed(() =>
  categories.value.map((c) => ({
    label: c.categoryName,
    options: products.value.filter((p) => p.categoryId === c.id)
  }))
)

const formTotal = computed(() => form.items.reduce((s, i) => s + (i.qty || 0) * (i.price || 0), 0))

/** 列表顶部：本页订单金额合计（跟表单明细是两回事，别混用） */
const pageAmount = computed(() => list.value.reduce((s, r) => s + Number(r.amount || 0), 0))
const remainOf = (o) => Number(o.amount || 0) - Number(o.paidAmount || 0)

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach((k) => {
      if (!params[k]) delete params[k]
    })
    const data = await request.get('/order/list', { params })
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
  Object.assign(form, { id: null, customerId: null, oppId: null, signDate: null, remark: '', items: [] })
  addItem()
}

function addItem() {
  form.items.push({ productId: null, qty: 1, price: 0 })
}

function onProductChange(row, id) {
  const p = products.value.find((x) => x.id === id)
  if (p) row.price = Number(p.price)
}

function onCustomerChange(id) {
  form.oppId = null
  opportunities.value = oppAll.value.filter((o) => o.customerId === id)
}

function openCreate() {
  resetForm()
  opportunities.value = []
  dialog.value = true
}

async function openEdit(row) {
  const detail = await request.get(`/order/${row.id}`)
  Object.assign(form, {
    id: detail.id,
    customerId: detail.customerId,
    oppId: detail.oppId,
    signDate: detail.signDate,
    remark: detail.remark,
    items: (detail.items || []).map((i) => ({ ...i, price: Number(i.price) }))
  })
  if (!form.items.length) addItem()
  opportunities.value = oppAll.value.filter((o) => o.customerId === detail.customerId)
  dialog.value = true
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
    if (form.id) {
      await request.put('/order', form)
    } else {
      await request.post('/order', form)
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function openDetail(row) {
  current.value = await request.get(`/order/${row.id}`)
  drawer.value = true
}

function openDelivery(row) {
  Object.assign(deliveryForm, { orderId: row.id, receiver: '', phone: '', address: '' })
  deliveryDialog.value = true
}

async function onSubmitDelivery() {
  saving.value = true
  try {
    await request.post(`/order/${deliveryForm.orderId}/delivery`, deliveryForm)
    ElMessage.success('发货单已创建')
    deliveryDialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function openPayment(row) {
  const detail = await request.get(`/order/${row.id}`)
  current.value = detail
  Object.assign(paymentForm, {
    orderId: row.id,
    amount: Math.max(0, remainOf(detail)),
    payDate: new Date().toISOString().slice(0, 10),
    payType: '银行转账',
    remark: ''
  })
  paymentDialog.value = true
}

async function onSubmitPayment() {
  saving.value = true
  try {
    await request.post(`/order/${paymentForm.orderId}/payment`, paymentForm)
    ElMessage.success('收款已登记，订单状态自动更新')
    paymentDialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onCommand(row, cmd) {
  if (cmd === 'cancel' || cmd === 'restore') {
    const label = cmd === 'cancel' ? '取消' : '恢复'
    try {
      await ElMessageBox.confirm(`确认${label}订单「${row.orderNo}」？`, '提示', { type: 'warning' })
    } catch {
      return
    }
    await request.put(`/order/${row.id}/status`, null, { params: { status: cmd === 'cancel' ? '已取消' : '待付款' } })
    ElMessage.success(`订单已${label}`)
    load()
    return
  }
  try {
    await ElMessageBox.confirm(`确认彻底删除订单「${row.orderNo}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(`/order/${row.id}/force`)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const [cs, ops, ps, cats] = await Promise.all([
    request.get('/customer/options'),
    request.get('/opportunity/options'),
    request.get('/product/options'),
    request.get('/productCategory/list')
  ])
  customers.value = cs
  oppAll.value = ops
  opportunities.value = ops
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

.profit {
  color: var(--ok);
  font-weight: 500;
}

.loss {
  color: var(--danger);
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
  font-size: 26px;
  font-weight: 500;
  letter-spacing: -0.5px;
}

.gp {
  margin-left: 10px;
  font-size: 13px;
  color: var(--ok);
}

.remain-tip {
  padding: 10px 14px;
  margin-bottom: 16px;
  background: #FFF7FA;
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-2);
}

.remain-tip b {
  color: var(--text);
  margin: 0 2px;
}

.remain-tip b.strong {
  color: var(--warn);
  font-size: 15px;
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
