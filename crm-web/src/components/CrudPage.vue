<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-if="searchable"
        v-model="query.keyword"
        :placeholder="searchPlaceholder || '搜索关键字'"
        clearable
        style="width: 230px"
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select
        v-for="f in filters"
        :key="f.prop"
        v-model="query[f.prop]"
        :placeholder="f.label"
        clearable
        style="width: 130px"
        @change="search"
      >
        <el-option v-for="o in f.options" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>

      <span class="count">{{ countText }}</span>
      <div class="grow" />
      <el-button v-if="canAdd" type="primary" :icon="Plus" @click="openCreate">{{ addBtnText }}</el-button>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list">
        <el-table-column
          v-for="c in columns"
          :key="c.prop"
          :label="c.label"
          :width="c.width"
          :min-width="c.minWidth"
          :align="c.align || 'left'"
        >
          <template #default="{ row }">
            <slot :name="c.prop" :row="row">
              <span v-if="c.type === 'money'" class="num">{{ money(row[c.prop]) }}</span>
              <StatusDot v-else-if="c.type === 'status'" :value="row[c.prop]" :map="c.map" />
              <span v-else-if="c.type === 'date'" class="num muted">{{ day(row[c.prop]) }}</span>
              <span v-else-if="c.type === 'mono'" class="mono">{{ row[c.prop] || '—' }}</span>
              <span v-else :class="c.muted ? 'muted' : ''">{{ row[c.prop] ?? '—' }}</span>
            </slot>
          </template>
        </el-table-column>

        <el-table-column label="操作" :width="actionWidth" align="right">
          <template #default="{ row }">
            <div class="ops">
              <slot name="actions" :row="row" />
              <el-button v-if="canEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="canRemove" link type="danger" @click="onRemove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty :description="emptyText || '暂无数据'" :image-size="72" />
        </template>
      </el-table>
    </div>

    <div v-if="!noPaging" class="pager">
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

  <el-dialog v-model="dialog" :title="(form.id ? '编辑' : '新增') + title" :width="dialogWidth" destroy-on-close>
    <el-form :model="form" label-position="top">
      <el-row :gutter="16">
        <el-col v-for="f in fields" :key="f.prop" :span="f.full ? 24 : 12">
          <el-form-item :label="f.label">
            <el-input v-if="!f.type || f.type === 'input'" v-model="form[f.prop]" :placeholder="f.placeholder" />
            <el-input
              v-else-if="f.type === 'textarea'"
              v-model="form[f.prop]"
              type="textarea"
              :rows="3"
              :placeholder="f.placeholder"
            />
            <el-select v-else-if="f.type === 'select'" v-model="form[f.prop]" clearable :placeholder="f.placeholder || '请选择'" style="width: 100%">
              <el-option v-for="o in f.options || []" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-date-picker
              v-else-if="f.type === 'date'"
              v-model="form[f.prop]"
              type="date"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
            <el-input-number
              v-else-if="f.type === 'number'"
              v-model="form[f.prop]"
              :min="0"
              :step="f.step || 1"
              controls-position="right"
              style="width: 100%"
            />
            <el-radio-group v-else-if="f.type === 'radio'" v-model="form[f.prop]">
              <el-radio v-for="o in f.options || []" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'
import { day, money } from '@/utils/format'
import StatusDot from '@/components/StatusDot.vue'

const props = defineProps({
  title: { type: String, required: true },
  api: { type: String, required: true },
  columns: { type: Array, required: true },
  fields: { type: Array, default: () => [] },
  filters: { type: Array, default: () => [] },
  perms: { type: String, default: '' },
  searchable: { type: Boolean, default: true },
  searchPlaceholder: String,
  noPaging: { type: Boolean, default: false },
  addText: String,
  emptyText: String,
  countText: { type: String, default: '' },
  dialogWidth: { type: String, default: '580px' },
  actionWidth: { type: Number, default: 140 },
  defaults: { type: Object, default: () => ({}) },
  /** 保存前改写提交数据 */
  beforeSubmit: { type: Function, default: null }
})

const emit = defineEmits(['saved', 'row-action'])

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const dialog = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', ...props.defaults })
const form = reactive({})

const canAdd = computed(() => !props.perms || hasPerm(`${props.perms}:add`))
const canEdit = computed(() => !props.perms || hasPerm(`${props.perms}:edit`))
const canRemove = computed(() => !props.perms || hasPerm(`${props.perms}:remove`))
// 没单独指定按钮文案时，用「新增 + 标题」
const addBtnText = computed(() => props.addText || `新增${props.title}`)

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach((k) => {
      if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
    })
    const data = await request.get(`${props.api}/list`, { params })
    if (Array.isArray(data)) {
      list.value = data
      total.value = data.length
    } else {
      list.value = data.list
      total.value = data.total
    }
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function openCreate() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { id: null }, props.defaults)
  props.fields.forEach((f) => {
    if (form[f.prop] === undefined) form[f.prop] = f.default ?? null
  })
  dialog.value = true
}

function openEdit(row) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { ...row })
  dialog.value = true
}

async function onSubmit() {
  saving.value = true
  try {
    const payload = props.beforeSubmit ? props.beforeSubmit({ ...form }) : form
    if (form.id) {
      await request.put(props.api, payload)
    } else {
      await request.post(props.api, payload)
    }
    ElMessage.success('已保存')
    dialog.value = false
    load()
    emit('saved')
  } finally {
    saving.value = false
  }
}

async function onRemove(row) {
  try {
    await ElMessageBox.confirm(`确认删除这条${props.title}？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(`${props.api}/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)

defineExpose({ load, search })
</script>

<style scoped>
.count {
  font-size: 12px;
  color: var(--text-3);
  padding-left: 4px;
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
</style>
