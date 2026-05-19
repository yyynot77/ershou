import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({ baseURL: '', timeout: 15000 })

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    const msg = err.response?.data?.message
      || (err.code === 'ERR_NETWORK' ? '无法连接后端，请确认 Spring Boot 已在 8080 端口启动' : null)
      || (err.response?.status === 500 ? '服务器错误，请检查数据库是否已执行 sql/init.sql' : null)
      || err.message
      || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
