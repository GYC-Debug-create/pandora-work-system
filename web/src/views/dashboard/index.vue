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
              </div>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in activities"
                :key="index"
                :timestamp="activity.timestamp"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
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

const logStore = useLogStore()

// 用户统计数据
const userStats = ref({
  total: 0,
  roles: {}
})

// 最近操作日志 - 直接从Pinia store中获取
const activities = computed(() => logStore.logs)

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
