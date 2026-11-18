<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button v-if="hasPerm('crm:dept:add')" type="primary" :icon="Plus" @click="openAdd()">新增部门</el-button>
      <span class="hint">支持多级，点行首箭头展开下级</span>
    </div>

    <div class="table-wrap">
      <el-table
        v-loading="loading"
        :data="tree"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
      >
      <el-table-column prop="deptName" label="部门名称" min-width="200" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button v-if="hasPerm('crm:dept:add')" link type="primary" @click="openAdd(row)">新增下级</el-button>
          <el-button v-if="hasPerm('crm:dept:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-dropdown v-if="hasPerm('crm:dept:remove')" @command="(c) => onDelete(row, c)">
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

  <el-dialog v-model="dialog" :title="form.id ? '编辑部门' : '新增部门'" width="480px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="上级部门">
        <el-tree-select
          v-model="form.parentId"
          :data="options"
          check-strictly
          clearable
          placeholder="不选则为顶级部门"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="部门名称">
        <el-input v-model="form.deptName" />
      </el-form-item>
      <el-form-item label="负责人">
        <el-input v-model="form.leader" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" />
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { hasPerm } from '@/store/user'

const loading = ref(false)
const tree = ref([])
const dialog = ref(false)

const form = reactive({
  id: null, parentId: null, deptName: '', leader: '', phone: '', sort: 0, status: 1
})

// 部门树 -> el-tree-select 需要的 { value, label, children }
const toOptions = (list = []) =>
  list.map((d) => ({ value: d.id, label: d.deptName, children: toOptions(d.children) }))
const options = computed(() => toOptions(tree.value))

async function load() {
  loading.value = true
  try {
    tree.value = await request.get('/dept/tree')
  } finally {
    loading.value = false
  }
}

function openAdd(row) {
  Object.assign(form, {
    id: null, parentId: row?.id ?? null, deptName: '', leader: '', phone: '', sort: 0, status: 1
  })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || null,
    deptName: row.deptName,
    leader: row.leader,
    phone: row.phone,
    sort: row.sort,
    status: row.status
  })
  dialog.value = true
}

async function onSubmit() {
  if (!form.deptName) {
    ElMessage.warning('请输入部门名称')
    return
  }
  const payload = { ...form, parentId: form.parentId || 0 }
  if (form.id) {
    await request.put('/dept', payload)
  } else {
    await request.post('/dept', payload)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function onDelete(row, type) {
  const label = type === 'force' ? '彻底删除' : '逻辑删除'
  try {
    await ElMessageBox.confirm(`确认${label}「${row.deptName}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(type === 'force' ? `/dept/${row.id}/force` : `/dept/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
