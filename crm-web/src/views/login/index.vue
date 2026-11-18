<template>
  <div class="login-page">
    <div class="card">
      <div class="mark">茶</div>
      <h2>CRM 管理系统</h2>
      <p class="sub">客户关系管理平台 · 员工编号登录</p>

      <el-form :model="form" @keyup.enter="onLogin">
        <el-form-item>
          <el-input v-model="form.empNo" size="large" placeholder="员工编号，如 EMP0001" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" size="large" placeholder="密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" size="large" class="btn" :loading="loading" @click="onLogin">登 录</el-button>
      </el-form>

      <p class="tip">默认账号 EMP0001 / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { setLogin } from '@/store/user'

const router = useRouter()
const loading = ref(false)
const form = reactive({ empNo: 'EMP0001', password: '123456' })

async function onLogin() {
  if (!form.empNo || !form.password) {
    ElMessage.warning('请输入员工编号和密码')
    return
  }
  loading.value = true
  try {
    const data = await request.post('/auth/login', form)
    setLogin(data)
    router.push(firstPath(data.menus) || '/')
  } finally {
    loading.value = false
  }
}

// 每个角色能看到的一级菜单不同，登录后落到他第一个有权限的页面
function firstPath(list, prefix = '') {
  for (const m of list) {
    const full = `${prefix}/${m.path || ''}`.replace(/\/+/g, '/')
    if (m.children && m.children.length) {
      const found = firstPath(m.children, full)
      if (found) return found
    } else if (m.path) {
      return full
    }
  }
  return ''
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFEFF5 0%, #FBF6F9 100%);
}

.card {
  width: 400px;
  padding: 40px 36px 28px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(31, 35, 41, 0.1);
}

.mark {
  width: 42px;
  height: 42px;
  border-radius: 11px;
  background: var(--brand);
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

h2 {
  font-size: 22px;
  font-weight: 500;
  text-align: center;
  color: #303133;
}

.sub {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin: 6px 0 26px;
}

.btn {
  width: 100%;
  height: 42px;
  font-size: 14px;
}

.tip {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 18px;
}
</style>
