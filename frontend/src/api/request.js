/**
 * ============================================================================
 * 模块：Axios 统一请求封装（全站 HTTP 出口）
 * ============================================================================
 *
 * 职责：
 * - 所有业务 API（api/index.js）最终都通过本文件的 request 实例发请求
 * - 自动携带 JWT Token
 * - 统一处理后端 Result 包装（code !== 200 视为业务失败）
 * - 401 时清理登录态并跳转登录页
 *
 * 完整调用链（以任意业务请求为例）：
 * 页面按钮/生命周期
 *   → views/*.vue 中的业务函数（如 load、doCheckout）
 *   → api/index.js 中的封装函数（如 getCart、checkout）
 *   → 本文件 request 实例
 *   → Vite 代理转发到 Spring Boot :8080
 *   → AuthInterceptor 鉴权（除白名单接口）
 *   → 对应 Controller → Service → Mapper → MySQL
 *   → 返回 JSON { code, message, data }
 *   → 响应拦截器解包后 return data 给页面
 *   → 页面赋值 ref/reactive → Vue 自动重渲染
 *
 * FIXME：401 时仅跳转 /login，未记录 redirect 回跳地址，后续可优化体验。
 * ============================================================================
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getToken, clearAuth } from '../utils/authStorage'

/** 全局 Axios 实例；baseURL 为空表示走 Vite 同源代理（见 vite.config.js） */
const request = axios.create({ baseURL: '', timeout: 15000 })

/**
 * 请求拦截器：为每个请求附加 Authorization 头
 *
 * 触发时机：api/index.js 中任意 request.get/post 调用前自动执行
 * 数据来源：sessionStorage（authStorage.getToken），由登录成功后 setAuth 写入
 * 后端对应：AuthInterceptor.preHandle 解析 Bearer Token → UserContext
 */
request.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

/**
 * 响应拦截器：统一业务码校验 + 全局错误提示
 *
 * 成功路径：code === 200 时返回整个 res.data（含 data 字段），页面通常用 res.data
 * 失败路径：ElMessage 提示 + Promise.reject，由页面 catch 或忽略
 *
 * 401 链路：
 * 后端返回 HTTP 401 → clearAuth() 清空 sessionStorage
 * → router.push('/login') → 路由守卫配合 meta.auth 阻止未登录访问
 */
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
      clearAuth()
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
