import { defineStore } from 'pinia'
import { ref } from 'vue'
import { LOG_TYPES, LOG_RETENTION_MS } from '@/constants/log'

// 常量定义
const STORAGE_KEY = 'system_logs'

// 创建初始日志对象
const createInitialLog = () => ({
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
        return JSON.parse(savedLogs)
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

  // 删除指定的日志（通过索引数组）
  const deleteLogsByIndexes = (indexes) => {
    logs.value = logs.value.filter((_, index) => !indexes.includes(index))
    saveToStorage()
  }

  // 导出日志为CSV
  const exportToCSV = (filteredLogs = null) => {
    const logsToExport = filteredLogs || logs.value
    const csvContent = [
      ['时间', '操作内容', '类型'].join(','),
      ...logsToExport.map(log => 
        [log.timestamp, `"${log.content}"`, log.type].join(',')
      )
    ].join('\n')
    
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
    deleteLogsByIndexes,
    exportToCSV,
    exportToJSON
  }
})
