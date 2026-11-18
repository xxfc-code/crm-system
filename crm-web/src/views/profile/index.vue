<template>
  <div v-loading="loading" class="profile">
    <!-- 身份卡 -->
    <div class="hero">
      <div class="avatar-lg">{{ initial(info.empName) }}</div>
      <div class="hero-meta">
        <div class="h-name">{{ info.empName || '-' }}</div>
        <div class="h-sub">
          <span class="mono">{{ info.empNo }}</span>
          <span class="sep">·</span>
          <span>{{ info.deptName || '未分配部门' }}</span>
          <span class="sep">·</span>
          <span>{{ info.roleName || '未分配角色' }}</span>
        </div>
      </div>
      <el-tag :type="info.status === 1 ? 'success' : 'info'" effect="light" size="small">
        {{ info.status === 1 ? '在职' : '离职' }}
      </el-tag>
    </div>

    <el-row :gutter="16">
      <el-col :span="15">
        <!-- 基本资料 -->
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-head">
              <span class="ch-title">基本资料</span>
              <span class="ch-sub">姓名、性别、联系方式由你自己维护</span>
            </div>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="姓名" prop="empName">
                  <el-input v-model="form.empName" placeholder="请输入姓名" maxlength="30" show-word-limit />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="form.gender">
                    <el-radio :value="0">未知</el-radio>
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="选填，11 位手机号" maxlength="11" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="选填，用于接收通知" clearable />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="foot">
              <el-button type="primary" :loading="saving" @click="onSave">保存资料</el-button>
              <el-button @click="resetForm">还原</el-button>
              <span class="foot-tip">部门、角色、账号状态由管理员在「员工管理」里维护</span>
            </div>
          </el-form>
        </el-card>

        <!-- 安全设置 -->
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-head">
              <span class="ch-title">安全设置</span>
              <span class="ch-sub">修改密码后需要重新登录</span>
            </div>
          </template>
          <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-position="top">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="pwd.oldPassword" type="password" placeholder="登录时用的密码" show-password />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwd.newPassword" type="password" placeholder="6-32 位" show-password />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwd.confirmPassword" type="password" placeholder="再输一遍" show-password />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="foot">
              <el-button type="primary" :loading="pwdSaving" @click="onChangePwd">修改密码</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="9">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span class="ch-title">账号信息</span>
            </div>
          </template>
          <div class="info-grid col-1">
            <div class="info-item">
              <label>工号（登录名）</label>
              <span class="mono">{{ info.empNo || '-' }}</span>
            </div>
            <div class="info-item">
              <label>所属部门</label>
              <span>{{ info.deptName || '未分配' }}</span>
            </div>
            <div class="info-item">
              <label>角色</label>
              <span>{{ info.roleName || '未分配' }}</span>
            </div>
            <div class="info-item">
              <label>账号状态</label>
              <span>{{ info.status === 1 ? '在职' : '离职' }}</span>
            </div>
            <div class="info-item">
              <label>创建时间</label>
              <span class="mono">{{ info.createTime ? info.createTime.replace('T', ' ').slice(0, 19) : '-' }}</span>
            </div>
          </div>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="工号是登录账号，密码由管理员重置；修改密码后其他设备上的登录会同时失效。"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { clearLogin, updateUser } from '@/store/user'
import { initial } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const pwdSaving = ref(false)
const formRef = ref()
const pwdRef = ref()

/** 只读的账号信息 */
const info = reactive({})
/** 可编辑的资料字段 */
const form = reactive({ empName: '', gender: 0, phone: '', email: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const rules = {
  empName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请填写正确的 11 位手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请填写正确的邮箱地址', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwd.newPassword) callback(new Error('两次输入的新密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/auth/profile')
    Object.assign(info, data)
    fillForm(data)
  } finally {
    loading.value = false
  }
}

function fillForm(data) {
  Object.assign(form, {
    empName: data.empName || '',
    gender: data.gender ?? 0,
    phone: data.phone || '',
    email: data.email || ''
  })
}

function resetForm() {
  fillForm(info)
  formRef.value?.clearValidate()
  ElMessage.info('已还原为当前生效的资料')
}

async function onSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // 只提交这四个字段，别的接口字段前端就算塞了也改不动
    const data = await request.put('/auth/profile', {
      empName: form.empName,
      gender: form.gender,
      phone: form.phone,
      email: form.email
    })
    Object.assign(info, data)
    fillForm(data)
    // 顶栏头像和姓名立刻跟着变，不用重新登录
    updateUser(data)
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

async function onChangePwd() {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await ElMessageBox.confirm('修改密码后当前登录会失效，需要用新密码重新登录，确定继续？', '提示', {
      type: 'warning'
    })
  } catch {
    return
  }

  pwdSaving.value = true
  try {
    await request.put('/auth/password', {
      oldPassword: pwd.oldPassword,
      newPassword: pwd.newPassword
    })
    ElMessage.success('密码已修改，请用新密码重新登录')
    clearLogin()
    router.push('/login')
  } finally {
    pwdSaving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.profile {
  max-width: 1180px;
}

/* ---------------- 身份卡 ---------------- */
.hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  margin-bottom: 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: linear-gradient(135deg, #FFF0F5 0%, #FFF9FB 100%);
}

.avatar-lg {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F5558B, #D93776);
  color: #fff;
  font-size: 22px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(240, 78, 139, 0.24);
}

.hero-meta {
  flex: 1;
  min-width: 0;
}

.h-name {
  font-size: 17px;
  font-weight: 500;
  color: var(--text);
}

.h-sub {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-3);
}

.sep {
  color: #e2d3da;
}

/* ---------------- 卡片 ---------------- */
.block {
  margin-bottom: 16px;
}

.card-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.ch-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.ch-sub {
  font-size: 12px;
  color: var(--text-3);
}

/* 全局把 card body 的 padding 清零了，这里按页面需要补回来 */
:deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid var(--line);
}

:deep(.el-card__body) {
  padding: 20px;
}

.foot {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}

.foot-tip {
  font-size: 12px;
  color: var(--text-3);
}

.info-grid.col-1 {
  grid-template-columns: 1fr;
  gap: 14px;
  margin-bottom: 16px;
}

:deep(.el-alert__title) {
  font-size: 12.5px;
  line-height: 1.6;
}
</style>
