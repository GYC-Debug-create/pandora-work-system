// web/src/api/user.js
import request from '@/utils/request' // 引入配置好的 Axios 实例

/**
 * 登录
 * @param {Object} data { username: 'xxx', passwordHash: 'xxx' }
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/user/login', // 对应后端的 @PostMapping("/login")
    method: 'post',
    data // axios会自动将JS对象转为JSON字符串
  })
}

/**
 * 获取用户列表 (后端需要实现此接口)
 * @param {Object} params 查询参数 { pageNum, pageSize, name, email, role }
 * @returns {Promise}
 */
export function getUserList(params) {
  // 假设后端接口为 GET /users
  return request({
    url: '/users', // <<--- 后端需要实现此 GET 接口
    method: 'get',
    params // GET请求的参数放在params里
  })
  // 注意：前端期望返回 { data: [...], total: xx } 格式
}

/**
 * 创建用户 (管理员)
 * @param {Object} data 用户数据 { username, passwordHash, fullName, email, mbtiType, departmentId }
 * @returns {Promise}
 */
export function createUser(data) {
  return request({
    url: '/user/registerByAdmin', // 对应后端的 @PostMapping("/registerByAdmin")
    method: 'post',
    data
  })
  // 注意：后端当前返回的是字符串 "用户创建成功" 或 "创建失败..."
  // 前端 createUser().then((response) => ...) 期望 response.data 里包含新用户ID，
  // 您可能需要调整后端，让它在成功时返回新创建的用户对象（或至少包含ID）。
}

/**
 * 更新用户 (后端需要实现此接口)
 * @param {number|string} id 用户ID
 * @param {Object} data 用户数据 { name, email, role, status }
 * @returns {Promise}
 */
export function updateUser(id, data) {
  // 假设后端接口为 PUT /users/{id}
  return request({
    url: `/users/${id}`, // <<--- 后端需要实现此 PUT 接口
    method: 'put',
    data
  })
}

/**
 * 删除用户 (后端需要实现此接口)
 * @param {number|string} id 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  // 假设后端接口为 DELETE /users/{id}
  return request({
    url: `/users/${id}`, // <<--- 后端需要实现此 DELETE 接口
    method: 'delete'
  })
}

// 注意：获取用户详情 (getUserDetail) 在前端代码中未使用，暂不实现对接