<template>
  <div class="app-container">
    <div class="dashboard-container">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>用户总数</span>
              </div>
            </template>
            <div class="card-body">
              <div class="card-value">{{ userStats.total }}</div>
              <div class="card-footer">
                <span>当前系统注册用户总数</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>部门老总</span>
              </div>
            </template>
            <div class="card-body">
              <div class="card-value">{{ userStats.roles['DEPARTMENT_HEAD'] || 0 }}</div>
              <div class="card-footer">
                <span>部门老总数量</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>系统状态</span>
              </div>
            </template>
            <div class="card-body">
              <div class="card-value" style="color: #67c23a;">正常</div>
              <div class="card-footer">
                <span>系统服务运行中</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>最近操作日志</span>
                <div>
                  <el-button size="small" @click="handleExportCSV">导出CSV</el-button>
                  <el-button size="small" @click="handleExportJSON">导出JSON</el-button>
                  <el-button size="small" type="danger" @click="handleClearAll">清空全部</el-button>
                </div>
              </div>
            </template>
            
            <!-- 筛选区域 -->
            <el-form :inline="true" style="margin-bottom: 20px;">
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  size="small"
                />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="filterType" placeholder="全部" clearable size="small" style="width: 120px;">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="操作" value="operation"></el-option>
                  <el-option label="系统" value="system"></el-option>
                  <el-option label="错误" value="error"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input v-model="filterKeyword" placeholder="搜索日志内容" clearable size="small" style="width: 200px;" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="applyFilter">应用筛选</el-button>
                <el-button size="small" @click="resetFilter">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 日志列表 -->
            <div v-if="filteredActivities.length > 0">
              <el-checkbox v-model="selectAll" @change="handleSelectAll" style="margin-bottom: 10px;">全选</el-checkbox>
              <el-timeline>
                <el-timeline-item
                  v-for="(activity, index) in filteredActivities"
                  :key="activity.originalIndex"
                  :timestamp="activity.timestamp"
                  :type="getTimelineType(activity.type)"
                >
                  <el-checkbox 
                    :model-value="activity.selected" 
                    @change="toggleSelect(activity)"
                    style="margin-right: 10px;">
                  </el-checkbox>
                  {{ activity.content }}
                  <el-tag v-if="activity.type" size="small" :type="getTagType(activity.type)" style="margin-left: 10px;">
                    {{ getTypeLabel(activity.type) }}
                  </el-tag>
                </el-timeline-item>
              </el-timeline>
              
              <!-- 批量操作 -->
              <div style="margin-top: 20px;" v-if="selectedCount > 0">
                <el-alert :title="`已选中 ${selectedCount} 条日志`" type="info" :closable="false">
                  <template #default>
                    <el-button size="small" type="danger" @click="handleDeleteSelected">删除选中</el-button>
                    <el-button size="small" @click="handleCancelSelect">取消选择</el-button>
                  </template>
                </el-alert>
              </div>
            </div>
            <el-empty v-else description="暂无日志记录" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getUserList } from '@/api/user'
import { useLogStore } from '@/store/log'
import { ElMessage, ElMessageBox } from 'element-plus'

const logStore = useLogStore()

// 用户统计数据
const userStats = ref({
  total: 0,
  roles: {}
})

// 日志筛选相关
const dateRange = ref(null)
const filterType = ref('')
const filterKeyword = ref('')
const selectAll = ref(false)
const isFilterActive = ref(false)
const selectedIndexes = ref(new Set()) // 使用 Set 存储选中的日志索引

// 最近操作日志 - 直接从Pinia store中获取
const activities = computed(() => logStore.logs)

// 筛选后的日志
const filteredActivities = computed(() => {
  let result = activities.value.map((log, index) => ({
    ...log,
    originalIndex: index, // 保存原始索引
    selected: selectedIndexes.value.has(index) // 根据索引判断是否选中
  }))
  
  // 如果没有应用筛选，返回全部
  if (!isFilterActive.value) {
    return result
  }
  
  // 日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    result = result.filter(log => {
      const logDate = new Date(log.timestamp).toISOString().split('T')[0]
      return logDate >= startDate && logDate <= endDate
    })
  }
  
  // 类型筛选
  if (filterType.value) {
    result = result.filter(log => log.type === filterType.value)
  }
  
  // 关键词搜索
  if (filterKeyword.value) {
    const keyword = filterKeyword.value.toLowerCase()
    result = result.filter(log => 
      log.content.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

// 已选中数量
const selectedCount = computed(() => {
  return filteredActivities.value.filter(log => log.selected).length
})

// 切换选中状态
const toggleSelect = (log) => {
  if (selectedIndexes.value.has(log.originalIndex)) {
    selectedIndexes.value.delete(log.originalIndex)
  } else {
    selectedIndexes.value.add(log.originalIndex)
  }
  // 更新全选状态
  updateSelectAllState()
}

// 更新全选状态
const updateSelectAllState = () => {
  if (filteredActivities.value.length === 0) {
    selectAll.value = false
  } else {
    selectAll.value = filteredActivities.value.every(log => log.selected)
  }
}

// 应用筛选
const applyFilter = () => {
  isFilterActive.value = true
  ElMessage.success(`筛选完成，共 ${filteredActivities.value.length} 条日志`)
}

// 重置筛选
const resetFilter = () => {
  dateRange.value = null
  filterType.value = ''
  filterKeyword.value = ''
  isFilterActive.value = false
  selectAll.value = false
  ElMessage.info('已重置筛选条件')
}

// 全选/取消全选
const handleSelectAll = () => {
  if (selectAll.value) {
    // 全选：将筛选结果中的所有日志索引添加到选中集合
    filteredActivities.value.forEach(log => {
      selectedIndexes.value.add(log.originalIndex)
    })
  } else {
    // 取消全选：从选中集合中移除筛选结果的所有索引
    filteredActivities.value.forEach(log => {
      selectedIndexes.value.delete(log.originalIndex)
    })
  }
}

// 取消选择
const handleCancelSelect = () => {
  selectedIndexes.value.clear()
  selectAll.value = false
}

// 删除选中的日志
const handleDeleteSelected = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedCount.value} 条日志吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 将 Set 转换为数组并传递
    const indexesToDelete = Array.from(selectedIndexes.value)
    logStore.deleteLogsByIndexes(indexesToDelete)
    selectedIndexes.value.clear()
    selectAll.value = false
    ElMessage.success('删除成功')
  }).catch(() => {
    // 取消删除
  })
}

// 清空全部日志
const handleClearAll = () => {
  ElMessageBox.confirm(
    '确定要清空所有日志吗？此操作不可恢复！',
    '确认清空',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    logStore.clearAllLogs()
    resetFilter()
    ElMessage.success('已清空所有日志')
  }).catch(() => {
    // 取消
  })
}

// 导出CSV
const handleExportCSV = () => {
  const logsToExport = isFilterActive.value ? filteredActivities.value : null
  const count = logsToExport ? logsToExport.length : logStore.logs.length
  logStore.exportToCSV(logsToExport)
  logStore.addLog(`导出了 ${count} 条日志为 CSV 格式`, 'system')
  ElMessage.success('导出成功')
}

// 导出JSON
const handleExportJSON = () => {
  const logsToExport = isFilterActive.value ? filteredActivities.value : null
  const count = logsToExport ? logsToExport.length : logStore.logs.length
  logStore.exportToJSON(logsToExport)
  logStore.addLog(`导出了 ${count} 条日志为 JSON 格式`, 'system')
  ElMessage.success('导出成功')
}

// 获取时间线类型
const getTimelineType = (type) => {
  const typeMap = {
    'operation': 'primary',
    'system': 'success',
    'error': 'danger'
  }
  return typeMap[type] || 'primary'
}

// 获取标签类型
const getTagType = (type) => {
  const typeMap = {
    'operation': 'info',
    'system': 'success',
    'error': 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取类型标签
const getTypeLabel = (type) => {
  const labelMap = {
    'operation': '操作',
    'system': '系统',
    'error': '错误'
  }
  return labelMap[type] || '未知'
}

// 获取用户数据并进行统计
const fetchUserStats = async () => {
  try {
    const response = await getUserList({ pageNum: 1, pageSize: 100 })
    // 后端返回的是 MyBatis-Plus 的 Page 对象：{ records: [...], total: xx }
    const users = Array.isArray(response?.records) ? response.records : []
    
    userStats.value.total = response?.total || users.length
    
    const rolesCount = users.reduce((acc, user) => {
      acc[user.roleName] = (acc[user.roleName] || 0) + 1
      return acc
    }, {})
    userStats.value.roles = rolesCount
  } catch (error) {
    console.error("获取用户数据失败:", error)
    logStore.addLog('获取仪表盘统计数据失败', 'error')
  }
}

onMounted(() => {
  fetchUserStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-body {
  text-align: center;
  padding: 20px 0;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.card-footer {
  font-size: 14px;
  color: #909399;
}
</style>
