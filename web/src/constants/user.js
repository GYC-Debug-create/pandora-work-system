/**
 * 用户相关常量配置
 */

// 用户状态枚举
export const USER_STATUS = {
  ACTIVE: '活跃',
  INACTIVE: '禁用'
}

// 用户状态选项（用于下拉选择）
export const USER_STATUS_OPTIONS = [
  { label: USER_STATUS.ACTIVE, value: USER_STATUS.ACTIVE },
  { label: USER_STATUS.INACTIVE, value: USER_STATUS.INACTIVE }
]

/**
 * 获取用户状态对应的 Element Plus Tag 类型
 * @param {string} status - 用户状态
 * @returns {string} Tag 类型
 */
export function getUserStatusTagType(status) {
  return status === USER_STATUS.ACTIVE ? 'success' : 'danger'
}

