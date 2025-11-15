<template>
  <div class="app-container">
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">
          公司十大事项
        </h2>
        <el-button
          type="primary"
          :disabled="taskList.length >= 10"
          @click="handleAddTask"
        >
          <el-icon><Plus /></el-icon> 新增事项
          <span v-if="taskList.length >= 10">（已满）</span>
        </el-button>
      </div>

      <!-- 事项表格 -->
      <el-table
        v-loading="loading"
        :data="taskList"
        border
        stripe
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="title"
          label="事项标题"
        />
        <el-table-column
          prop="content"
          label="详细内容"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
        />
        <el-table-column
          label="操作"
          width="180"
        >
          <template #default="scope">
            <el-button
              size="small"
              @click="handleEditTask(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteTask(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 十大事项已满警告 -->
      <el-alert
        v-if="taskList.length >= 10"
        title="十大事项已满"
        type="warning"
        :closable="false"
        style="margin-top: 20px;"
      >
        十大事项已满（{{ taskList.length }}/10），如需添加请先删除现有事项
      </el-alert>

      <!-- 新增/编辑事项对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @close="resetForm"
      >
        <el-form
          ref="taskFormRef"
          :model="taskForm"
          :rules="taskRules"
          label-width="80px"
        >
          <el-form-item
            label="事项标题"
            prop="title"
          >
            <el-input
              v-model="taskForm.title"
              placeholder="请输入事项标题"
            />
          </el-form-item>
          <el-form-item
            label="详细内容"
            prop="content"
          >
            <el-input
              v-model="taskForm.content"
              type="textarea"
              :rows="5"
              placeholder="请输入事项详细内容"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button
              type="primary"
              @click="submitForm"
            >确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useLogStore } from '@/store/log'
import { formatDateTime } from '@/utils/format'
import {
  getCompanyMatters,
  createCompanyMatter,
  updateCompanyMatter,
  deleteCompanyMatter
} from '@/api/companyMatters'

const logStore = useLogStore()
const taskList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const taskFormRef = ref(null)

// 表单数据模型
const initialForm = {
  matterId: null,
  title: '',
  content: ''
}
const taskForm = ref({ ...initialForm })

// 表单校验规则
const taskRules = {
  title: [{ required: true, message: '请输入事项标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细内容', trigger: 'blur' }]
}

// 获取事项列表
const getTaskList = () => {
  loading.value = true
  getCompanyMatters().then(response => {
    const matters = Array.isArray(response) ? response : []
    taskList.value = matters.map(matter => ({
      id: matter.matterId,
      title: matter.title,
      content: matter.content,
      createTime: formatDateTime(matter.createdAt)
    })).sort((a, b) => a.id - b.id)
    loading.value = false
  }).catch(error => {
    console.error('获取事项列表失败:', error)
    logStore.addLog('获取事项列表失败', 'error')
    ElMessage.error('获取事项列表失败')
    taskList.value = []
    loading.value = false
  })
}

// 重置表单
const resetForm = () => {
  taskForm.value = { ...initialForm }
  if (taskFormRef.value) {
    try {
      taskFormRef.value.clearValidate()
    } catch {
      // 忽略错误
    }
  }
}

// 新增弹窗
const handleAddTask = () => {
  if (taskList.value.length >= 10) {
    ElMessage.warning({
      message: '十大事项已满（10/10），请先删除现有事项后再添加',
      duration: 3000
    })
    return
  }

  resetForm()
  dialogTitle.value = '新增事项'
  dialogVisible.value = true
}

// 编辑弹窗
const handleEditTask = (row) => {
  taskForm.value = {
    matterId: row.id,
    title: row.title,
    content: row.content
  }
  dialogTitle.value = '编辑事项'
  dialogVisible.value = true
}

// 提交表单
const submitForm = () => {
  taskFormRef.value.validate(valid => {
    if (!valid) return

    const dataToSend = {
      matterId: taskForm.value.matterId,
      title: taskForm.value.title,
      content: taskForm.value.content
    }

    const isEdit = dataToSend.matterId !== null
    const apiCall = isEdit
      ? updateCompanyMatter(dataToSend.matterId, dataToSend)
      : createCompanyMatter(dataToSend)
    const action = isEdit ? '更新' : '创建'

    apiCall.then(() => {
      getTaskList()
      logStore.addLog(`${action}了事项: "${dataToSend.title}"`)
      ElMessage.success(`${action}成功`)
      dialogVisible.value = false
    }).catch(error => {
      console.error(`${action}事项失败:`, error)
      logStore.addLog(`${action}事项 "${dataToSend.title}" 失败`, 'error')
      const errorMessage = error.response?.data || `${action}失败`
      ElMessage.error(errorMessage)
    })
  })
}

// 删除事项
const handleDeleteTask = (row) => {
  ElMessageBox.confirm(`确定要删除事项 "${row.title}" 吗?`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    deleteCompanyMatter(row.id).then(() => {
      getTaskList()
      logStore.addLog(`删除了事项: "${row.title}" (ID: ${row.id})`)
      ElMessage.success('删除成功')
    }).catch(error => {
      console.error('删除事项失败:', error)
      logStore.addLog(`删除事项 "${row.title}" 失败`, 'error')
    })
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 生命周期钩子
onMounted(() => {
  getTaskList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}
</style>