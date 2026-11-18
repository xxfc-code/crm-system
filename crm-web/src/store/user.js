import { ref } from 'vue'

const read = (key, fallback) => JSON.parse(localStorage.getItem(key) || fallback)

export const user = ref(read('user', '{}'))
export const menus = ref(read('menus', '[]'))
export const perms = ref(read('perms', '[]'))

export function setLogin(data) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('user', JSON.stringify(data.user))
  localStorage.setItem('menus', JSON.stringify(data.menus))
  localStorage.setItem('perms', JSON.stringify(data.perms))
  user.value = data.user
  menus.value = data.menus
  perms.value = data.perms
}

export function clearLogin() {
  localStorage.clear()
  user.value = {}
  menus.value = []
  perms.value = []
}

/** 改了个人资料后同步顶栏显示，顺带落盘免得刷新又变回旧的 */
export function updateUser(patch) {
  const next = { ...user.value, ...patch }
  user.value = next
  localStorage.setItem('user', JSON.stringify(next))
}

/** 按钮级权限判断 */
export function hasPerm(code) {
  return perms.value.includes(code)
}
