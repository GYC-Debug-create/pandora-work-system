// web/src/api/user.js
import request from '@/utils/request' // 引入配置好的 Axios 实例

/**
 * 登录
 * @param {Object} data { username: 'xxx', passwordHash: 'xxx' }
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/api/user/web-login',
    method: 'post',
    data
  })
}

/**
 * 获取用户列表 (后端需要实现此接口)
 * @param {Object} params 查询参数 { pageNum, pageSize, name, email, role }
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/api/users',
    method: 'get',
    params
  })
}

/**
 * 创建用户 (管理员)
 * @param {Object} data 用户数据 { username, passwordHash, fullName, email, mbtiType, departmentId }
 * @returns {Promise}
 */
export function createUser(data) {
  return request({
    url: '/api/user/registerByAdmin',
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
  return request({
    url: `/api/users/${id}`,
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
  return request({
    url: `/api/users/${id}`,
    method: 'delete'
  })
}

/**
 * 修改密码
 * @param {Object} data { oldPassword: 'xxx', newPassword: 'xxx' }
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/api/user/change-password',
    method: 'post',
    data
  })
}

// 注意：获取用户详情 (getUserDetail) 在前端代码中未使用，暂不实现对接