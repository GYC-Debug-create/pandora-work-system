// web/src/utils/request.js
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const service = axios.create({
  baseURL: '/api', // <--- 修改这里！ 设置基础URL为/api
  timeout: 10000
})

// --- 请求拦截器部分保持不变 ---
service.interceptors.request.use( /* ... */ )

// --- 响应拦截器部分保持不变 ---
// 注意：当前的响应拦截器是基于 response.status !== 200 判断错误的。
// 我们后端的登录失败返回的是401，这里应该能正确处理。
// 但如果后端其他接口失败返回的是200 OK + 错误信息，则需要调整此拦截器逻辑。
service.interceptors.response.use(
    response => {
      const res = response.data;
      // 后端返回非2xx状态码会被axios自动识别为错误进入error处理
      // 如果后端API设计为：即使业务失败也返回200，但用特定字段表示错误（如 code !== 0），
      // 则需要在这里添加判断逻辑。
      // 目前看您的后端UserController设计是比较规范的（成功200，失败401），此拦截器暂不需大改。
      return res; // 直接返回 data 部分
    },
    error => {
      console.log('响应错误: ' + error); // for debug
      let message = '请求发生错误';
      if (error.response) {
        // 后端返回了错误状态码 (4xx, 5xx)
        // 尝试从后端返回的 response body 中获取错误信息
        message = typeof error.response.data === 'string' ? error.response.data : (error.response.data?.message || error.message);

        if (error.response.status === 401) {
          // 可以增加未授权时的处理，例如跳转登录页
          ElMessageBox.confirm('认证失败或已过期，请重新登录', '确认', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            localStorage.removeItem('admin-token'); // 清除旧的token
            window.location.href = '/login'; // 跳转到登录页
          });
        }
      } else if (error.request) {
        // 请求已发出，但没有收到响应
        message = '网络错误，无法连接到服务器';
      } else {
        // 设置请求时触发了一个错误
        message = error.message;
      }

      ElMessage({
        message: message,
        type: 'error',
        duration: 5 * 1000
      });
      return Promise.reject(error);
    }
)


export default service