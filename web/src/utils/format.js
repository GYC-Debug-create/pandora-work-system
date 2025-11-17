/**
 * 日期时间格式化工具函数
 */

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm:ss
 * @param {string|Date} dateTimeString - 日期时间字符串或 Date 对象
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(dateTimeString) {
  if (!dateTimeString) return ''
  
  try {
    const date = new Date(dateTimeString)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.warn('无效的日期:', dateTimeString)
      return dateTimeString
    }
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    console.error('日期格式化失败:', dateTimeString, e)
    return dateTimeString
  }
}


