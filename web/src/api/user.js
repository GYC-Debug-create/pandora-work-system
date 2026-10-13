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
 * 退出登录：让后端把当前 token 加入黑名单
 * skipAuthHandler：即使 token 已失效返回 401，也不触发全局「会话过期」弹窗，由调用方自行清理会话
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/api/user/logout',
    method: 'post',
    skipAuthHandler: true
  })
}

/**
 * 获取用户列表（分页 + 筛选）
 * 页面使用的参数名与后端接口参数名不同，在这里统一做映射：
 *   pageNum → current, pageSize → size, name → username, email → email, role → roleName
 * 空字符串的筛选条件不传，避免后端按空串过滤
 * @param {Object} params 查询参数 { pageNum, pageSize, name, email, role }
 * @returns {Promise} MyBatis-Plus Page: { records, total, size, current, pages }
 */
export function getUserList(params = {}) {
  const query = {
    current: params.pageNum || 1,
    size: params.pageSize || 10
  }
  const filters = { username: params.name, email: params.email, roleName: params.role }
  Object.keys(filters).forEach((key) => {
    const value = typeof filters[key] === 'string' ? filters[key].trim() : filters[key]
    if (value) {
      query[key] = value
    }
  })
  return request({
    url: '/api/users',
    method: 'get',
    params: query
  })
}

/**
 * 获取全量用户统计（不受分页影响）
 * @returns {Promise} { total, active, inactive, roles: { 角色名: 人数 } }
 */
export function getUserStats() {
  return request({
    url: '/api/users/stats',
    method: 'get'
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