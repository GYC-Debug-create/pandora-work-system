import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, clearSession } from '@/utils/auth'

// 常量定义
const HTTP_STATUS = {
  UNAUTHORIZED: 401
}

const MESSAGE_DURATION = 5000
const REQUEST_TIMEOUT = 10000

// 用于存储日志函数的引用（避免循环依赖）
let logStoreAddLog = null
let isHandlingUnauthorized = false

// 导出设置日志函数的方法
export const setLogStore = (addLogFn) => {
  logStoreAddLog = addLogFn
}

const service = axios.create({
  baseURL: '',
  timeout: REQUEST_TIMEOUT
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    let message = '请求发生错误'

    if (error.response) {
      message = typeof error.response.data === 'string'
        ? error.response.data
        : (error.response.data?.message || error.response.data?.error || error.message)

      if (error.response.status === HTTP_STATUS.UNAUTHORIZED) {
        // 调用方声明自行处理 401（如退出登录接口），这里不弹窗、不跳转
        if (error.config?.skipAuthHandler) {
          return Promise.reject(error)
        }

        // 判断是否是登录请求（登录请求不会携带 token）
        const isLoginRequest = !error.config?.headers?.['Authorization']

        if (isLoginRequest) {
          // 没带 token 的 401 = 登录失败：统一提示账号或密码错误
          ElMessage({
            message: '账号或密码错误',
            type: 'error',
            duration: MESSAGE_DURATION
          })
          return Promise.reject(error)
        } else {
          // 真正的会话过期：统一强制登出并回到登录页
          if (isHandlingUnauthorized) {
            // 已在处理 401，避免弹出多个对话框
            return Promise.reject(error)
          }
          isHandlingUnauthorized = true

          if (logStoreAddLog) {
            logStoreAddLog('会话已过期，用户被强制登出', 'system')
          }

          // 清理本地会话信息
          clearSession()

          // 友好提示 + 重定向到登录页
          ElMessageBox.alert('会话已过期，请重新登录', '提示', {
            type: 'warning',
            confirmButtonText: '重新登录'
          }).finally(() => {
            // 不在这里重置标记：整页跳转会重新加载脚本、标记自然复位；
            // 若提前复位，跳转前陆续返回的 401 会再次弹窗
            window.location.href = '/login'
          })

          return Promise.reject(error)
        }
      } else {
        // 其他错误显示消息
        ElMessage({
          message,
          type: 'error',
          duration: MESSAGE_DURATION
        })
      }
    } else if (error.request) {
      message = '网络错误，无法连接到服务器'

      // 记录网络错误日志
      if (logStoreAddLog) {
        logStoreAddLog('网络请求超时，无法连接到服务器', 'error')
      }

      ElMessage({
        message,
        type: 'error',
        duration: MESSAGE_DURATION
      })
    } else {
      message = error.message
      ElMessage({
        message,
        type: 'error',
        duration: MESSAGE_DURATION
      })
    }

    return Promise.reject(error)
  }
)

export default service