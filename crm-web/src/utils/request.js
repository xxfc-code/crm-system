import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 直接返回 data，页面里就不用每次 .data.data
request.interceptors.response.use(
  (res) => {
    const { code, msg, data } = res.data
    if (code !== 200) {
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }
    return data
  },
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      localStorage.clear()
      location.href = '/login'
    } else if (status === 403) {
      // 后端 @RequiresPerm 拦截，说明前端按钮没藏干净
      ElMessage.error(err.response?.data?.msg || '没有这个操作的权限')
    } else {
      ElMessage.error(err.message || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default request
