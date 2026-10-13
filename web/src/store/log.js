import { defineStore } from 'pinia'
import { ref } from 'vue'
import { LOG_TYPES, LOG_RETENTION_MS } from '@/constants/log'

// 常量定义
const STORAGE_KEY = 'system_logs'

// 生成日志唯一 id：时间戳 + 随机数（同一毫秒内的多条日志也不会重复）
const generateLogId = () => `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`

// 旧版本存储的日志没有 id，读取时补上，保证按 id 删除/选择能兼容老数据
const ensureLogIds = (list) => {
  if (!Array.isArray(list)) return []
  return list.map(log => (log && log.id ? log : { ...log, id: generateLogId() }))
}

// CSV 单元格转义（RFC 4180）：整体用双引号包裹，内部的双引号写成两个双引号
const escapeCsvCell = (value) => {
  const text = value === null || value === undefined ? '' : String(value)
  return `"${text.replace(/"/g, '""')}"`
}

// 创建初始日志对象
const createInitialLog = () => ({
  id: generateLogId(),
  content: '系统初始化成功',
  timestamp: new Date().toLocaleString(),
  time: Date.now(),
  type: LOG_TYPES.SYSTEM
})

export const useLogStore = defineStore('log', () => {
  // 从 localStorage 恢复日志
  const loadLogsFromStorage = () => {
    const savedLogs = localStorage.getItem(STORAGE_KEY)
    if (savedLogs) {
      try {
        return ensureLogIds(JSON.parse(savedLogs))
      } catch (e) {
        console.error('解析日志失败:', e)
        return []
      }
    }
    return [createInitialLog()]
  }

  // state: 日志列表
  const logs = ref(loadLogsFromStorage())

  // 保存到 localStorage
  const saveToStorage = () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(logs.value))
  }

  // 自动清理过期日志
  const cleanOldLogs = () => {
    const cutoffTime = Date.now() - LOG_RETENTION_MS
    logs.value = logs.value.filter(log => log.time > cutoffTime)
  }

  // 添加日志
  const addLog = (message, type = 'operation') => {
    logs.value.unshift({
      id: generateLogId(),
      content: message,
      timestamp: new Date().toLocaleString(),
      time: Date.now(),
      type: type // 'operation'(操作), 'system'(系统), 'error'(错误)
    })

    // 清理旧日志
    cleanOldLogs()
    
    // 保存到 localStorage
    saveToStorage()
  }

  // 清空所有日志
  const clearAllLogs = () => {
    logs.value = [createInitialLog()]
    saveToStorage()
  }

  // 删除指定的日志（通过日志 id 数组）
  // 不用下标：导出等操作会在列表头部插入新日志，下标会整体错位
  const deleteLogsByIds = (ids) => {
    const idSet = new Set(ids)
    logs.value = logs.value.filter(log => !idSet.has(log.id))
    saveToStorage()
  }

  // 导出日志为CSV（每个单元格按 RFC 4180 转义；BOM 在 downloadFile 中统一加）
  const exportToCSV = (filteredLogs = null) => {
    const logsToExport = filteredLogs || logs.value
    const csvContent = [
      ['时间', '操作内容', '类型'].map(escapeCsvCell).join(','),
      ...logsToExport.map(log =>
        [log.timestamp, log.content, log.type].map(escapeCsvCell).join(',')
      )
    ].join('\r\n')
    
    downloadFile(csvContent, 'logs.csv', 'text/csv;charset=utf-8;')
  }

  // 导出日志为JSON
  const exportToJSON = (filteredLogs = null) => {
    const logsToExport = filteredLogs || logs.value
    const jsonContent = JSON.stringify(logsToExport, null, 2)
    downloadFile(jsonContent, 'logs.json', 'application/json')
  }

  // 下载文件辅助函数
  const downloadFile = (content, filename, mimeType) => {
    const blob = new Blob(['\ufeff' + content], { type: mimeType })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.click()
    URL.revokeObjectURL(url)
  }

  // 初始化时清理一次旧日志
  cleanOldLogs()
  saveToStorage()

  return { 
    logs, 
    addLog, 
    clearAllLogs,
    deleteLogsByIds,
    exportToCSV,
    exportToJSON
  }
})
