<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button v-if="hasPerm('crm:menu:add')" type="primary" :icon="Plus" @click="openAdd()">新增目录</el-button>
      <span class="count">M 目录 / C 菜单 / F 按钮，只有超管建议动这里</span>
      <div class="grow" />
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="tree" row-key="id" :tree-props="{ children: 'children' }" default-expand-all>
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <span class="type" :class="row.menuType">{{ TYPE[row.menuType] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="路由" width="160">
          <template #default="{ row }"><span class="mono muted">{{ row.path || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="组件" min-width="170">
          <template #default="{ row }"><span class="mono muted">{{ row.component || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="权限标识" width="190">
          <template #default="{ row }"><span class="mono muted">{{ row.perms || '—' }}</span></template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="170" align="right">
          <template #default="{ row }">
            <div class="ops">
              <el-button v-if="hasPerm('crm:menu:add') && row.menuType !== 'F'" link type="primary" @click="openAdd(row)">新增下级</el-button>
              <el-button v-if="hasPerm('crm:menu:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="hasPerm('crm:menu:remove')" link type="danger" @click="onRemove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>

  <el-dialog v-model="dialog" :title="form.id ? '编辑菜单' : '新增菜单'" width="560px" destroy-on-close>
    <el-form :model="form" label-position="top">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="上级菜单">
            <el-tree-select v-model="form.parentId" :data="options" check-strictly clearable placeholder="顶级" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型">
            <el-select v-model="form.menuType" style="width: 100%">
              <el-option label="目录 M" value="M" />
              <el-option label="菜单 C" value="C" />
              <el-option label="按钮 F" value="F" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜单名称"><el-input v-model="form.menuName" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="路由地址"><el-input v-model="form.path" placeholder="如 /customer 或 lead" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="权限标识"><el-input v-model="form.perms" placeholder="如 crm:lead:add" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组件路径"><el-input v-model="form.component" placeholder="如 customer/lead" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element 图标名" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="0">停用</el-radio>
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'

const TYPE = { M: '目录', C: '菜单', F: '按钮' }

const loading = ref(false)
const saving = ref(false)
const tree = ref([])
const dialog = ref(false)
const form = reactive({ id: null, parentId: 0, menuName: '', menuType: 'M', path: '', component: '', perms: '', icon: '', sort: 0, status: 1 })

const toOptions = (nodes = []) => nodes.map((n) => ({ value: n.id, label: n.menuName, children: toOptions(n.children) }))
const options = computed(() => [{ value: 0, label: '顶级菜单', children: toOptions(tree.value) }])

async function load() {
  loading.value = true
  try {
    tree.value = await request.get('/menu/manage')
  } finally {
    loading.value = false
  }
}

function openAdd(parent) {
  Object.assign(form, {
    id: null,
    parentId: parent?.id || 0,
    menuName: '',
    menuType: parent ? 'C' : 'M',
    path: '',
    component: '',
    perms: '',
    icon: '',
    sort: 0,
    status: 1
  })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || 0,
    menuName: row.menuName,
    menuType: row.menuType,
    path: row.path,
    component: row.component,
    perms: row.perms,
    icon: row.icon,
    sort: row.sort,
    status: row.status
  })
  dialog.value = true
}

async function onSubmit() {
  if (!form.menuName) {
    ElMessage.warning('请填写菜单名称')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await request.put('/menu', form)
    } else {
      await request.post('/menu', form)
    }
    ElMessage.success('已保存，重新登录后菜单生效')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onRemove(row) {
  try {
    await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(`/menu/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.count {
  font-size: 12px;
  color: var(--text-3);
}

.type {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #F6F0F3;
  color: var(--text-3);
}

.type.M {
  background: var(--brand-soft);
  color: var(--brand);
}

.type.C {
  background: #eaf7f0;
  color: var(--ok);
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
