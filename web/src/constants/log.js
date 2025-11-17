/**
 * 日志相关常量配置
 */

// 日志类型枚举
export const LOG_TYPES = {
  OPERATION: 'operation', // 操作日志
  SYSTEM: 'system',       // 系统日志
  ERROR: 'error'          // 错误日志
}

// 日志类型配置（包含标签类型、时间线类型、标签文本）
export const LOG_TYPE_CONFIG = {
  [LOG_TYPES.OPERATION]: {
    label: '操作',
    tagType: 'info',
    timelineType: 'primary'
  },
  [LOG_TYPES.SYSTEM]: {
    label: '系统',
    tagType: 'success',
    timelineType: 'success'
  },
  [LOG_TYPES.ERROR]: {
    label: '错误',
    tagType: 'danger',
    timelineType: 'danger'
  }
}


// 日志保留天数
export const LOG_RETENTION_DAYS = 7

// 日志保留时长（毫秒）
export const LOG_RETENTION_MS = LOG_RETENTION_DAYS * 24 * 60 * 60 * 1000

/**
 * 获取日志类型配置
 * @param {string} type - 日志类型
 * @param {string} key - 配置键名 (label/tagType/timelineType)
 * @returns {string} 配置值
 */
export function getLogTypeConfig(type, key) {
  const config = LOG_TYPE_CONFIG[type] || LOG_TYPE_CONFIG[LOG_TYPES.OPERATION]
  return config[key]
}

/**
 * 获取日志类型标签文本
 * @param {string} type - 日志类型
 * @returns {string} 标签文本
 */
export function getLogTypeLabel(type) {
  return getLogTypeConfig(type, 'label')
}

/**
 * 获取日志类型对应的 Element Plus Tag 类型
 * @param {string} type - 日志类型
 * @returns {string} Tag 类型
 */
export function getLogTagType(type) {
  return getLogTypeConfig(type, 'tagType')
}

/**
 * 获取日志类型对应的 Element Plus Timeline 类型
 * @param {string} type - 日志类型
 * @returns {string} Timeline 类型
 */
export function getLogTimelineType(type) {
  return getLogTypeConfig(type, 'timelineType')
}

