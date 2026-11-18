<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button v-if="hasPerm('crm:role:add')" type="primary" :icon="Plus" @click="openAdd">新增角色</el-button>
      <span class="hint">角色的权限通过「分配权限」勾选，控制左侧菜单和页面按钮</span>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list">
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="roleKey" label="角色标识" width="150" />
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="数据范围" width="130">
        <template #default="{ row }">
          <span class="muted">{{ SCOPE_LABEL[row.dataScope] || '仅本人' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="180" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button v-if="hasPerm('crm:role:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('crm:role:auth')" link type="success" @click="openAuth(row)">分配权限</el-button>
          <el-dropdown v-if="hasPerm('crm:role:remove')" @command="(c) => onDelete(row, c)">
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
  </el-card>

  <el-dialog v-model="dialog" :title="form.id ? '编辑角色' : '新增角色'" width="480px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="角色名称">
        <el-input v-model="form.roleName" />
      </el-form-item>
      <el-form-item label="角色标识">
        <el-input v-model="form.roleKey" placeholder="英文，如 sales_manager" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="数据范围">
        <el-select v-model="form.dataScope" style="width: 100%">
          <el-option v-for="(label, value) in SCOPE_LABEL" :key="value" :label="label" :value="Number(value)" />
        </el-select>
        <div class="tip">决定这个角色能看到哪些业务数据，只影响线索/客户/商机/订单</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="authDialog" :title="`分配权限 - ${current?.roleName || ''}`" width="460px">
    <el-tree
      ref="treeRef"
      :data="menuTree"
      node-key="id"
      show-checkbox
      default-expand-all
      :props="{ label: 'menuName', children: 'children' }"
    >
      <template #default="{ data }">
        <span>
          {{ data.menuName }}
          <el-tag v-if="data.menuType === 'F'" size="small" type="info" style="margin-left: 6px">按钮</el-tag>
        </span>
      </template>
    </el-tree>
    <template #footer>
      <el-button @click="authDialog = false">取消</el-button>
      <el-button type="primary" @click="onSaveAuth">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'

const loading = ref(false)
const list = ref([])
const dialog = ref(false)
const authDialog = ref(false)
const menuTree = ref([])
const current = ref(null)
const treeRef = ref()

const emptyForm = { id: null, roleName: '', roleKey: '', sort: 0, status: 1, dataScope: 4, remark: '' }
const form = reactive({ ...emptyForm })

const SCOPE_LABEL = { 1: '全部数据', 2: '本部门', 3: '本部门及以下', 4: '仅本人' }

// 只取叶子节点，回显时父级由组件自动算半选，否则会连带勾中所有子项
const leafIds = (nodes = []) =>
  nodes.flatMap((n) => (n.children?.length ? leafIds(n.children) : [n.id]))

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/role/list')
  } finally {
    loading.value = false
  }
}

function openAdd() {
  Object.assign(form, { ...emptyForm })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    roleName: row.roleName,
    roleKey: row.roleKey,
    sort: row.sort,
    status: row.status,
    dataScope: row.dataScope ?? 4,
    remark: row.remark
  })
  dialog.value = true
}

async function onSubmit() {
  if (!form.roleName || !form.roleKey) {
    ElMessage.warning('角色名称和标识都要填')
    return
  }
  if (form.id) {
    await request.put('/role', form)
  } else {
    await request.post('/role', form)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function openAuth(row) {
  current.value = row
  const [tree, granted] = await Promise.all([
    request.get('/menu/tree'),
    request.get(`/role/${row.id}/menus`)
  ])
  menuTree.value = tree
  authDialog.value = true
  await nextTick()
  const leafSet = new Set(leafIds(tree))
  treeRef.value.setCheckedKeys(granted.filter((id) => leafSet.has(id)))
}

async function onSaveAuth() {
  const ids = [...treeRef.value.getCheckedKeys(), ...treeRef.value.getHalfCheckedKeys()]
  await request.put(`/role/${current.value.id}/menus`, ids)
  ElMessage.success('权限已保存，重新登录生效')
  authDialog.value = false
}

async function onDelete(row, type) {
  const label = type === 'force' ? '彻底删除' : '逻辑删除'
  try {
    await ElMessageBox.confirm(`确认${label}角色「${row.roleName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(type === 'force' ? `/role/${row.id}/force` : `/role/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.5;
  width: 100%;
}
</style>
