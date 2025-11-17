import { ElMessage } from 'element-plus'

/**
 * 解析错误对象，提取可读的错误信息
 * @param {Error} error - axios 错误对象
 * @param {string} fallback - 默认错误信息
 * @returns {string} 解析后的错误信息
 */
export function parseErrorMessage(error, fallback = '操作失败，请重试') {
  // 如果没有 error 对象，返回默认信息
  if (!error) {
    return fallback
  }

  // 优先从 error.response.data 中提取
  if (error.response && error.response.data) {
    const data = error.response.data

    // 情况1：后端返回纯字符串
    if (typeof data === 'string') {
      return data || fallback
    }

    // 情况2：后端返回 JSON 对象
    if (typeof data === 'object') {
      // 尝试常见的错误字段名
      return data.message || data.error || data.msg || fallback
    }
  }

  // 如果 response.data 不存在，尝试 error.message
  if (error.message) {
    return error.message
  }

  // 最后返回默认信息
  return fallback
}

/**
 * 显示错误提示消息（封装 ElMessage.error）
 * @param {Error} error - axios 错误对象
 * @param {string} fallback - 默认错误信息
 * @param {number} duration - 提示持续时间（毫秒）
 */
export function showErrorMessage(error, fallback = '操作失败，请重试', duration = 3000) {
  const message = parseErrorMessage(error, fallback)
  ElMessage.error({
    message,
    duration
  })
}

