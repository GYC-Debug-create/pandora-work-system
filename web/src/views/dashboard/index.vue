<template>
  <div class="app-container">
    <div class="dashboard-container">
      <!-- 今日概况卡片区 -->
      <div class="section-title">
        <el-icon><DataAnalysis /></el-icon>
        <span>今日概况</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <el-icon :size="32"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.total }}</div>
                <div class="stat-label">用户总数</div>
                <div class="stat-desc">当前系统注册用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <el-icon :size="32"><Avatar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.roles['DEPARTMENT_HEAD'] || 0 }}</div>
                <div class="stat-label">部门老总</div>
                <div class="stat-desc">部门负责人数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <el-icon :size="32"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ todayLogs }}</div>
                <div class="stat-label">今日日志</div>
                <div class="stat-desc">今天产生的日志数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
                <el-icon :size="32"><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">正常</div>
                <div class="stat-label">系统状态</div>
                <div class="stat-desc">服务运行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 趋势与分布区域 -->
      <div class="section-title" style="margin-top: 30px;">
        <el-icon><TrendCharts /></el-icon>
        <span>数据分布</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="distribution-card">
            <template #header>
              <div class="card-header">
                <el-icon><UserFilled /></el-icon>
                <span>用户状态分布</span>
              </div>
            </template>
            <div class="distribution-content">
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="success" size="small">活跃用户</el-tag>
                </div>
                <div class="distribution-bar">
                  <el-progress
                    :percentage="userDistribution.activePercent"
                    :color="'#67c23a'"
                    :stroke-width="20"
                  />
                </div>
                <div class="distribution-value">{{ userDistribution.active }} 人</div>
              </div>
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="danger" size="small">禁用用户</el-tag>
                </div>
                <div class="distribution-bar">
                  <el-progress
                    :percentage="userDistribution.inactivePercent"
                    :color="'#f56c6c'"
                    :stroke-width="20"
                  />
                </div>
                <div class="distribution-value">{{ userDistribution.inactive }} 人</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="distribution-card">
            <template #header>
              <div class="card-header">
                <el-icon><Tickets /></el-icon>
                <span>日志类型分布</span>
              </div>
            </template>
            <div class="distribution-content">
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="info" size="small">操作日志</el-tag>
                </div>
                <div class="distribution-bar">
                  <el-progress
                    :percentage="logDistribution.operationPercent"
                    :color="'#409eff'"
                    :stroke-width="20"
                  />
                </div>
                <div class="distribution-value">{{ logDistribution.operation }} 条</div>
              </div>
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="success" size="small">系统日志</el-tag>
                </div>
                <div class="distribution-bar">
                  <el-progress
                    :percentage="logDistribution.systemPercent"
                    :color="'#67c23a'"
                    :stroke-width="20"
                  />
                </div>
                <div class="distribution-value">{{ logDistribution.system }} 条</div>
              </div>
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="danger" size="small">错误日志</el-tag>
                </div>
                <div class="distribution-bar">
                  <el-progress
                    :percentage="logDistribution.errorPercent"
                    :color="'#f56c6c'"
                    :stroke-width="20"
                  />
                </div>
                <div class="distribution-value">{{ logDistribution.error }} 条</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 最近操作日志区域 -->
      <div class="section-title" style="margin-top: 30px;">
        <el-icon><List /></el-icon>
        <span>最近活动</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>最近操作日志</span>
                <div>
                  <el-button
                    size="small"
                    @click="handleExportCSV"
                  >
                    导出CSV
                  </el-button>
                  <el-button
                    size="small"
                    @click="handleExportJSON"
                  >
                    导出JSON
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="handleClearAll"
                  >
                    清空全部
                  </el-button>
                </div>
              </div>
            </template>
            
            <!-- 筛选区域 -->
            <el-form
              :inline="true"
              style="margin-bottom: 20px;"
            >
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
                <el-select
                  v-model="filterType"
                  placeholder="全部"
                  clearable
                  size="small"
                  style="width: 120px;"
                >
                  <el-option
                    label="全部"
                    value=""
                  />
                  <el-option
                    label="操作"
                    value="operation"
                  />
                  <el-option
                    label="系统"
                    value="system"
                  />
                  <el-option
                    label="错误"
                    value="error"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input
                  v-model="filterKeyword"
                  placeholder="搜索日志内容"
                  clearable
                  size="small"
                  style="width: 200px;"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="small"
                  @click="applyFilter"
                >
                  应用筛选
                </el-button>
                <el-button
                  size="small"
                  @click="resetFilter"
                >
                  重置
                </el-button>
              </el-form-item>
            </el-form>

            <!-- 日志列表 -->
            <div v-if="filteredActivities.length > 0">
              <el-checkbox
                v-model="selectAll"
                style="margin-bottom: 10px;"
                @change="handleSelectAll"
              >
                全选
              </el-checkbox>
              <el-timeline>
                <el-timeline-item
                  v-for="activity in filteredActivities"
                  :key="activity.originalIndex"
                  :timestamp="activity.timestamp"
                  :type="getLogTimelineType(activity.type)"
                >
                  <div class="log-item">
                    <el-checkbox
                      :model-value="activity.selected"
                      style="margin-right: 10px;"
                      @change="toggleSelect(activity)"
                    />
                    <el-icon
                      :size="16"
                      :class="'log-icon log-icon-' + activity.type"
                      style="margin-right: 8px;"
                    >
                      <Operation v-if="activity.type === 'operation'" />
                      <Setting v-else-if="activity.type === 'system'" />
                      <WarningFilled v-else-if="activity.type === 'error'" />
                      <InfoFilled v-else />
                    </el-icon>
                    <span class="log-content">{{ activity.content }}</span>
                    <el-tag
                      v-if="activity.type"
                      size="small"
                      :type="getLogTagType(activity.type)"
                      style="margin-left: 10px;"
                    >
                      {{ getLogTypeLabel(activity.type) }}
                    </el-tag>
                  </div>
                </el-timeline-item>
              </el-timeline>
              
              <!-- 批量操作 -->
              <div
                v-if="selectedCount > 0"
                style="margin-top: 20px;"
              >
                <el-alert
                  :title="`已选中 ${selectedCount} 条日志`"
                  type="info"
                  :closable="false"
                >
                  <template #default>
                    <el-button
                      size="small"
                      type="danger"
                      @click="handleDeleteSelected"
                    >
                      删除选中
                    </el-button>
                    <el-button
                      size="small"
                      @click="handleCancelSelect"
                    >
                      取消选择
                    </el-button>
                  </template>
                </el-alert>
              </div>
            </div>
            <el-empty
              v-else
              description="暂无日志记录"
            />
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
import { showErrorMessage } from '@/utils/error'
import {
  DataAnalysis,
  TrendCharts,
  User,
  Avatar,
  Document,
  CircleCheck,
  UserFilled,
  Tickets,
  List,
  Operation,
  Setting,
  WarningFilled,
  InfoFilled
} from '@element-plus/icons-vue'
import {
  LOG_TYPES,
  getLogTypeLabel,
  getLogTagType,
  getLogTimelineType
} from '@/constants/log'
import { USER_STATUS } from '@/constants/user'

const logStore = useLogStore()

// 用户统计数据
const userStats = ref({
  total: 0,
  roles: {},
  active: 0,
  inactive: 0
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

// 今日日志数量（使用数值时间戳，避免依赖本地化日期解析）
const todayLogs = computed(() => {
  const now = new Date()
  const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  const endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

  return activities.value.filter(log => {
    const logTime = typeof log.time === 'number' ? log.time : Date.parse(log.timestamp)
    if (Number.isNaN(logTime)) return false
    return logTime >= startOfDay && logTime <= endOfDay
  }).length
})

// 用户状态分布
const userDistribution = computed(() => {
  const total = userStats.value.total || 1 // 避免除以0
  const active = userStats.value.active || 0
  const inactive = userStats.value.inactive || 0
  return {
    active,
    inactive,
    activePercent: Math.round((active / total) * 100),
    inactivePercent: Math.round((inactive / total) * 100)
  }
})

// 日志类型分布
const logDistribution = computed(() => {
  const logs = activities.value
  const total = logs.length || 1 // 避免除以0
  const operation = logs.filter(log => log.type === LOG_TYPES.OPERATION).length
  const system = logs.filter(log => log.type === LOG_TYPES.SYSTEM).length
  const error = logs.filter(log => log.type === LOG_TYPES.ERROR).length
  return {
    operation,
    system,
    error,
    operationPercent: Math.round((operation / total) * 100),
    systemPercent: Math.round((system / total) * 100),
    errorPercent: Math.round((error / total) * 100)
  }
})

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
  
  // 日期范围筛选（基于时间戳，避免依赖本地化日期解析）
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value

    // dateRange 使用 value-format="YYYY-MM-DD"，构造标准 ISO 字符串再转时间戳
    const startTime = new Date(`${startDate}T00:00:00`).getTime()
    const endTime = new Date(`${endDate}T23:59:59.999`).getTime()

    result = result.filter(log => {
      const logTime = typeof log.time === 'number' ? log.time : Date.parse(log.timestamp)
      if (Number.isNaN(logTime)) return false
      return logTime >= startTime && logTime <= endTime
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

// 获取用户数据并进行统计（防御性处理：兼容 Page 结构和直接数组）
const fetchUserStats = async () => {
  try {
    const response = await getUserList({ pageNum: 1, pageSize: 100 })

    // 兼容两种返回格式：
    // 1. MyBatis-Plus Page: { records: [...], total: xx }
    // 2. 直接数组: [...]
    let users = []
    let totalCount = 0

    if (Array.isArray(response?.records)) {
      // Page 结构
      users = response.records
      totalCount = response.total || users.length
    } else if (Array.isArray(response)) {
      // 直接数组
      users = response
      totalCount = users.length
    }

    userStats.value.total = totalCount

    const rolesCount = users.reduce((acc, user) => {
      acc[user.roleName] = (acc[user.roleName] || 0) + 1
      return acc
    }, {})
    userStats.value.roles = rolesCount

    // 统计用户状态分布
    userStats.value.active = users.filter(user => user.status === USER_STATUS.ACTIVE).length
    userStats.value.inactive = users.filter(user => user.status === USER_STATUS.INACTIVE).length
  } catch (error) {
    console.error("获取用户数据失败:", error)
    showErrorMessage(error, '获取仪表盘统计数据失败')
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

/* 区块标题 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--el-border-color-lighter);
}

.section-title .el-icon {
  font-size: 20px;
  color: var(--el-color-primary);
}

/* 统计卡片 */
.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s ease;
  border: 1px solid var(--el-border-color-lighter);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--text-color-primary);
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color-regular);
  margin-bottom: 2px;
}

.stat-desc {
  font-size: 12px;
  color: var(--text-color-secondary);
}

/* 分布卡片 */
.distribution-card {
  margin-bottom: 20px;
  height: 100%;
}

.distribution-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.distribution-content {
  padding: 10px 0;
}

.distribution-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
}

.distribution-item:last-child {
  margin-bottom: 0;
}

.distribution-label {
  width: 80px;
  flex-shrink: 0;
}

.distribution-bar {
  flex: 1;
}

.distribution-value {
  width: 60px;
  text-align: right;
  font-weight: 600;
  color: var(--text-color-primary);
  flex-shrink: 0;
}

/* 日志卡片 */
.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 日志项 */
.log-item {
  display: flex;
  align-items: center;
}

.log-content {
  flex: 1;
  color: var(--text-color-primary);
}

.log-icon {
  flex-shrink: 0;
}

.log-icon-operation {
  color: var(--el-color-info);
}

.log-icon-system {
  color: var(--el-color-success);
}

.log-icon-error {
  color: var(--el-color-danger);
}
</style>
