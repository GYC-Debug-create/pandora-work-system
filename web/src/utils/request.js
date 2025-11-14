// 文件: src/utils/request.js
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 用于存储日志函数的引用（避免循环依赖）
let logStoreAddLog = null

// 导出设置日志函数的方法
export const setLogStore = (addLogFn) => {
  logStoreAddLog = addLogFn
}

const service = axios.create({
  baseURL: '', // 后端接口已经包含/api前缀，这里不需要baseURL
  timeout: 10000
})

// --- 请求拦截器 ---
service.interceptors.request.use(
    config => {
      // 从 localStorage 获取 token
      const token = localStorage.getItem('admin-token');
      if (token) {
        // 如果 token 存在，则添加到请求头
        config.headers['Authorization'] = 'Bearer ' + token; // <--- 添加 Authorization 头
      }
      return config;
    },
    error => {
      return Promise.reject(error);
    }
)

// --- 响应拦截器 ---
// (可以增加对 401 错误的处理，例如自动跳转登录页)
service.interceptors.response.use(
    response => {
      // ... (保持不变) ...
      return response.data; // 直接返回 data 部分
    },
    error => {
      let message = '请求发生错误';
      if (error.response) {
        message = typeof error.response.data === 'string' ? error.response.data : (error.response.data?.message || error.message);

        if (error.response.status === 401) {
          // 记录会话过期日志
          if (logStoreAddLog) {
            logStoreAddLog('会话已过期，用户被强制登出', 'system')
          }
          
          // 处理 401 未授权错误
          ElMessageBox.confirm('会话已过期或无效，请重新登录', '确认', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            localStorage.removeItem('admin-token'); // 清除旧 token
            localStorage.removeItem('user-info'); // 清除用户信息
            window.location.href = '/login'; // 跳转登录页 (或者使用 router.push('/login'))
          }).catch(() => {
            // 用户取消，可以不做处理或给出提示
          });
          // 阻止进一步的错误提示，因为我们已经处理了
          return Promise.reject(new Error("会话无效")); // 可以返回一个自定义错误
        }
        // 对于其他错误，显示消息
        else {
          ElMessage({ message: message, type: 'error', duration: 5 * 1000 });
        }

      } else if (error.request) {
        message = '网络错误，无法连接到服务器';
        // 记录网络错误日志
        if (logStoreAddLog) {
          logStoreAddLog('网络请求超时，无法连接到服务器', 'error')
        }
        ElMessage({ message: message, type: 'error', duration: 5 * 1000 });
      } else {
        message = error.message;
        ElMessage({ message: message, type: 'error', duration: 5 * 1000 });
      }

      // ElMessage({ message: message, type: 'error', duration: 5 * 1000 }); // 移动到上面，避免重复提示
      return Promise.reject(error);
    }
)

export default service