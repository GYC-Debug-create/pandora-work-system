import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '', // url = base url + request url
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    
    // 可以在这里添加token等认证信息
    
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果响应不是200，则判断为错误
    if (response.status !== 200) {
      ElMessage({
        message: res.message || '请求错误',
        type: 'error',
        duration: 5 * 1000
      })
      
      // 401: 未授权
      if (response.status === 401) {
        // 重新登录
        ElMessageBox.confirm('您已登出，请重新登录', '确认登出', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 重置token并重新登录
          // store.dispatch('user/resetToken').then(() => {
          //   location.reload()
          // })
        })
      }
      return Promise.reject(new Error(res.message || '请求错误'))
    } else {
      return res
    }
  },
  error => {
    console.log('请求错误: ' + error)
    ElMessage({
      message: error.message || '请求错误',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
