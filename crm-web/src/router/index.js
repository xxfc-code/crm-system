import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/views/login/index.vue') },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard/overview',
    children: [
      { path: 'dashboard/overview', component: () => import('@/views/dashboard/overview.vue'), meta: { title: '经营概览' } },
      { path: 'dashboard/todo', component: () => import('@/views/dashboard/todo.vue'), meta: { title: '我的待办' } },

      { path: 'customer/lead', component: () => import('@/views/customer/lead.vue'), meta: { title: '销售线索' } },
      { path: 'customer/pool', component: () => import('@/views/customer/pool.vue'), meta: { title: '客户公海' } },
      { path: 'customer/archive', component: () => import('@/views/customer/archive.vue'), meta: { title: '客户档案' } },
      { path: 'customer/contact', component: () => import('@/views/customer/contact.vue'), meta: { title: '联系人' } },
      { path: 'customer/follow', component: () => import('@/views/customer/follow.vue'), meta: { title: '跟进记录' } },

      { path: 'product/category', component: () => import('@/views/product/category.vue'), meta: { title: '产品分类' } },
      { path: 'product/list', component: () => import('@/views/product/list.vue'), meta: { title: '产品档案' } },
      { path: 'product/price', component: () => import('@/views/product/price.vue'), meta: { title: '客户价格' } },

      { path: 'sales/opportunity', component: () => import('@/views/sales/opportunity.vue'), meta: { title: '销售商机' } },
      { path: 'sales/quotation', component: () => import('@/views/sales/quotation.vue'), meta: { title: '报价单' } },
      { path: 'sales/order', component: () => import('@/views/sales/order.vue'), meta: { title: '销售订单' } },
      { path: 'sales/return', component: () => import('@/views/sales/return.vue'), meta: { title: '退换货单' } },

      { path: 'delivery/delivery', component: () => import('@/views/delivery/delivery.vue'), meta: { title: '发货记录' } },
      { path: 'delivery/payment', component: () => import('@/views/delivery/payment.vue'), meta: { title: '收款记录' } },
      { path: 'delivery/receivable', component: () => import('@/views/delivery/receivable.vue'), meta: { title: '应收账款' } },

      { path: 'service/ticket', component: () => import('@/views/service/ticket.vue'), meta: { title: '售后工单' } },
      { path: 'service/visit', component: () => import('@/views/service/visit.vue'), meta: { title: '客户回访' } },

      { path: 'stat/sales', component: () => import('@/views/stat/sales.vue'), meta: { title: '销售统计' } },
      { path: 'stat/customer', component: () => import('@/views/stat/customer.vue'), meta: { title: '客户采购分析' } },
      { path: 'stat/product', component: () => import('@/views/stat/product.vue'), meta: { title: '产品销售分析' } },

      { path: 'system/employee', component: () => import('@/views/system/employee/index.vue'), meta: { title: '员工管理' } },
      { path: 'system/dept', component: () => import('@/views/system/dept/index.vue'), meta: { title: '部门管理' } },
      { path: 'system/role', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', component: () => import('@/views/system/menu.vue'), meta: { title: '权限管理' } },
      { path: 'system/operLog', component: () => import('@/views/system/operLog.vue'), meta: { title: '操作日志' } },

      // 个人中心：不在菜单树里，但每个登录用户都能进，所以路由守卫要单独放行
      { path: 'profile', component: () => import('@/views/profile/index.vue'), meta: { title: '个人资料' } },

      { path: ':pathMatch(.*)*', component: () => import('@/views/placeholder/index.vue'), meta: { title: '开发中' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 从菜单树里收集所有可访问的页面路径 */
function collectPaths(list, prefix = '', out = []) {
  for (const m of list) {
    const full = `${prefix}/${m.path || ''}`.replace(/\/+/g, '/')
    if (m.children && m.children.length) {
      collectPaths(m.children, full, out)
    } else if (m.path) {
      out.push(full)
    }
  }
  return out
}

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token) return to.path === '/login' ? true : '/login'
  if (to.path === '/login') return '/'

  const menus = JSON.parse(localStorage.getItem('menus') || '[]')
  const allowed = collectPaths(menus)
  // /profile 是个人中心，不在菜单树里，属于所有登录用户的通用页，直接放行
  if (allowed.length && !allowed.includes(to.path) && to.path !== '/' && to.path !== '/profile') {
    return allowed[0]
  }
  return true
})

export default router
