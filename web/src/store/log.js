import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLogStore = defineStore('log', () => {
  // state: a reactive array to store log activities
  const logs = ref([
    { content: '系统初始化成功', timestamp: new Date().toLocaleString() }
  ])

  // action: a function to add a new log entry
  const addLog = (message) => {
    // Prepend the new log to the beginning of the array
    logs.value.unshift({
      content: message,
      timestamp: new Date().toLocaleString()
    })

    // Optional: Keep the log list from growing too large
    if (logs.value.length > 20) {
      logs.value.pop()
    }
  }

  return { logs, addLog }
})
