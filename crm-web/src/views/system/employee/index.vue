<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-tree-select
        v-model="query.deptId"
        :data="deptOptions"
        check-strictly
        clearable
        placeholder="按部门筛选"
        style="width: 180px"
        @change="search"
      />
      <el-input
        v-model="query.keyword"
        placeholder="姓名 / 工号"
        clearable
        style="width: 180px"
        @keyup.enter="search"
      />
      <el-button :icon="Search" @click="search">查询</el-button>
      <div class="spacer" />
      <el-button v-if="hasPerm('crm:employee:add')" type="primary" :icon="Plus" @click="openAdd">
        新增员工
      </el-button>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list">
      <el-table-column prop="empNo" label="工号" width="110" />
      <el-table-column prop="empName" label="姓名" width="110" />
      <el-table-column prop="deptName" label="所属部门" min-width="140" />
      <el-table-column prop="roleName" label="角色" width="130" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column label="性别" width="80" align="center">
        <template #default="{ row }">{{ ['未知', '男', '女'][row.gender] }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '在职' : '离职' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button v-if="hasPerm('crm:employee:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('crm:employee:resetPwd')" link type="warning" @click="onResetPwd(row)">
            重置密码
          </el-button>
          <el-dropdown v-if="hasPerm('crm:employee:remove')" @command="(c) => onDelete(row, c)">
            <el-button link type="danger">
              删除<el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logic">逻辑删除</el-dropdown-item>
                <el-dropdown-item command="force">彻底删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
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

  <el-dialog v-model="dialog" :title="form.id ? '编辑员工' : '新增员工'" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item v-if="!form.id" label="工号">
        <el-input model-value="保存后自动生成（EMP0001）" disabled />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.empName" />
      </el-form-item>
      <el-form-item label="所属部门">
        <el-tree-select v-model="form.deptId" :data="deptOptions" check-strictly default-expand-all style="width: 100%" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.roleId" style="width: 100%">
          <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="0">未知</el-radio>
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">在职</el-radio>
          <el-radio :value="0">离职</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'

const loading = ref(false)
const list = ref([])
const roles = ref([])
const deptTree = ref([])
const dialog = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, deptId: null, keyword: '' })
const total = ref(0)

const emptyForm = {
  id: null, empName: '', deptId: null, roleId: null, phone: '', email: '', gender: 0, status: 1
}
const form = reactive({ ...emptyForm })

const toOptions = (nodes = []) =>
  nodes.map((d) => ({ value: d.id, label: d.deptName, children: toOptions(d.children) }))
const deptOptions = computed(() => toOptions(deptTree.value))

async function load() {
  loading.value = true
  try {
    const data = await request.get('/employee/list', { params: query })
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

async function init() {
  const [depts, roleList] = await Promise.all([request.get('/dept/tree'), request.get('/role/list')])
  deptTree.value = depts
  roles.value = roleList
  await load()
}

function openAdd() {
  Object.assign(form, { ...emptyForm })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    empName: row.empName,
    deptId: row.deptId,
    roleId: row.roleId,
    phone: row.phone,
    email: row.email,
    gender: row.gender,
    status: row.status
  })
  dialog.value = true
}

async function onSubmit() {
  if (!form.empName) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (form.id) {
    await request.put('/employee', form)
  } else {
    await request.post('/employee', form)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function onResetPwd(row) {
  try {
    await ElMessageBox.confirm(`将「${row.empName}」的密码重置为 123456？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.put(`/employee/${row.id}/resetPwd`)
  ElMessage.success('已重置为 123456')
}

async function onDelete(row, type) {
  const label = type === 'force' ? '彻底删除' : '逻辑删除'
  try {
    await ElMessageBox.confirm(`确认${label}「${row.empName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(type === 'force' ? `/employee/${row.id}/force` : `/employee/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(init)
</script>
