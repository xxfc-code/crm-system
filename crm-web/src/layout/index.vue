<template>
  <div class="shell">
    <aside class="side">
      <div class="brand">
        <div class="mark">茶</div>
        <div class="btext">
          <div class="bname">CRM</div>
          <div class="bsub">客户关系管理</div>
        </div>
      </div>

      <el-menu
        :default-active="route.path"
        :default-openeds="openeds"
        router
        class="menu"
      >
        <template v-for="m in menus" :key="m.id">
          <el-sub-menu v-if="m.children && m.children.length" :index="m.path">
            <template #title>
              <el-icon><component :is="m.icon || 'Menu'" /></el-icon>
              <span>{{ m.menuName }}</span>
            </template>
            <el-menu-item v-for="c in m.children" :key="c.id" :index="join(m.path, c.path)">
              <el-icon><component :is="c.icon || 'Document'" /></el-icon>
              <span>{{ c.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="m.path">
            <el-icon><component :is="m.icon || 'Menu'" /></el-icon>
            <span>{{ m.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>

      <div class="side-foot">
        <span class="dot"><i style="background:#3ddc97"></i> 系统运行中</span>
      </div>
    </aside>

    <section class="body">
      <header class="topbar">
        <div class="crumb">
          <span v-if="parentName" class="c1">{{ parentName }}</span>
          <span v-if="parentName" class="sep">/</span>
          <span class="c2">{{ route.meta.title }}</span>
        </div>

        <el-dropdown trigger="click" @command="onCommand">
          <div class="me">
            <div class="avatar">{{ initial(user.empName) }}</div>
            <div class="meta">
              <div class="name">{{ user.empName }}</div>
              <div class="role">{{ user.roleName }}</div>
            </div>
            <el-icon class="caret"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>{{ user.empNo }}</el-dropdown-item>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearLogin, menus, user } from '@/store/user'
import { initial } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const openeds = computed(() =>
  menus.value.filter((m) => m.path && route.path.startsWith(`${m.path}/`)).map((m) => m.path)
)

// 面包屑的一级名称，从菜单树里反查
const parentName = computed(() => {
  const hit = menus.value.find((m) => m.path && route.path.startsWith(`${m.path}/`))
  return hit ? hit.menuName : ''
})

const join = (parent, child) => `${parent}/${child}`.replace(/\/+/g, '/')

function onCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
    return
  }
  if (cmd === 'logout') {
    clearLogin()
    router.push('/login')
  }
}
</script>

<style scoped>
.shell {
  display: flex;
  height: 100%;
  overflow: hidden;
}

/* ---------------- 侧边栏 ---------------- */
.side {
  width: 220px;
  flex-shrink: 0;
  background: #FFFFFF;
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
}

.mark {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: linear-gradient(135deg, #F5558B, #D93776);
  box-shadow: 0 4px 10px rgba(240, 78, 139, 0.26);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bname {
  color: #2B2529;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.bsub {
  color: #A9A3AC;
  font-size: 11px;
  margin-top: 2px;
}

.menu {
  flex: 1;
  overflow-y: auto;
  padding: 6px 10px;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #6B6570;
  --el-menu-hover-bg-color: rgba(240, 78, 139, 0.07);
  --el-menu-active-color: #ffffff;
  --el-menu-item-height: 42px;
  --el-menu-sub-item-height: 38px;
  border-right: none;
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  border-radius: 7px;
  margin-bottom: 2px;
  font-size: 13.5px;
}

.menu :deep(.el-menu-item.is-active) {
  background: var(--brand);
  color: #fff;
}

.menu :deep(.el-menu-item.is-active:hover) {
  background: var(--brand);
}

.menu :deep(.el-sub-menu .el-menu-item) {
  font-size: 13px;
  padding-left: 46px !important;
}

.side-foot {
  padding: 14px 18px;
  border-top: 1px solid #F2EAEE;
  font-size: 11px;
  color: #A9A3AC;
}

/* ---------------- 右侧 ---------------- */
.body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 60px;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.c1 {
  color: var(--text-3);
}

.sep {
  color: #d0d4da;
}

.c2 {
  color: var(--text);
  font-weight: 500;
}

.me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 20px;
  transition: background 0.15s;
  outline: none;
}

.me:hover {
  background: #FBF6F9;
}

.meta {
  line-height: 1.25;
}

.name {
  font-size: 13px;
  color: var(--text);
}

.role {
  font-size: 11px;
  color: var(--text-3);
}

.caret {
  color: var(--text-3);
  font-size: 12px;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px 28px;
}
</style>
