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
        v-if="taskList.length > 0"
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
          align="center"
        >
          <template #default="scope">
            <el-tag type="info" size="small" effect="plain">
              #{{ scope.row.id }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="title"
          label="事项标题"
          min-width="200"
        >
          <template #default="scope">
            <div style="font-weight: 600; color: var(--text-color-primary);">
              {{ scope.row.title }}
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          label="详细内容"
          min-width="300"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div style="color: var(--text-color-regular);">
              {{ scope.row.content }}
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
          align="center"
        >
          <template #default="scope">
            <div style="display: flex; align-items: center; justify-content: center; gap: 4px;">
              <el-icon><Clock /></el-icon>
              <span>{{ scope.row.createTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="180"
          fixed="right"
          align="center"
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

      <!-- 空状态 -->
      <el-empty
        v-else-if="!loading && taskList.length === 0"
        description="当前还没有公司事项"
        style="margin-top: 40px;"
      >
        <template #image>
          <el-icon :size="100" color="var(--el-color-info)">
            <Document />
          </el-icon>
        </template>
        <el-button type="primary" @click="handleAddTask">
          <el-icon><Plus /></el-icon>
          新增事项
        </el-button>
        <div style="margin-top: 12px; font-size: 13px; color: var(--text-color-secondary);">
          点击上方按钮开始维护公司十大事项信息
        </div>
      </el-empty>

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
import { Plus, Clock, Document } from '@element-plus/icons-vue'
import { useLogStore } from '@/store/log'
import { formatDateTime } from '@/utils/format'
import { showErrorMessage } from '@/utils/error'
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
const validateTaskTitleUnique = (rule, value, callback) => {
  const normalized = (value || '').trim()
  if (!normalized) {
    return callback()
  }
  const hasDuplicate = taskList.value.some((item) => {
    if (!item.title) return false
    const sameTitle = item.title.trim() === normalized
    const isSelf = taskForm.value.matterId != null && item.id === taskForm.value.matterId
    return sameTitle && !isSelf
  })
  if (hasDuplicate) {
    callback(new Error('已存在同名事项标题'))
  } else {
    callback()
  }
}

const taskRules = {
  title: [
    { required: true, message: '请输入事项标题', trigger: 'blur' },
    { validator: validateTaskTitleUnique, trigger: ['blur', 'change'] }
  ],
  content: [{ required: true, message: '请输入详细内容', trigger: 'blur' }]
}

// 获取事项列表
const getTaskList = () => {
  loading.value = true
  getCompanyMatters().then(response => {
    const matters = Array.isArray(response) ? response : []
    taskList.value = matters.map(matter => {
      // 处理创建时间
      let timeStr = '未知时间'

      if (matter.createdAt) {
        try {
          // 如果是字符串格式（ISO 8601），直接格式化
          if (typeof matter.createdAt === 'string') {
            timeStr = formatDateTime(matter.createdAt)
          }
          // 如果是数组格式 [year, month, day, hour, minute, second]
          else if (Array.isArray(matter.createdAt) && matter.createdAt.length >= 3) {
            const year = matter.createdAt[0]
            const month = String(matter.createdAt[1]).padStart(2, '0')
            const day = String(matter.createdAt[2]).padStart(2, '0')
            const hour = String(matter.createdAt[3] || 0).padStart(2, '0')
            const minute = String(matter.createdAt[4] || 0).padStart(2, '0')
            const second = String(matter.createdAt[5] || 0).padStart(2, '0')
            timeStr = `${year}-${month}-${day} ${hour}:${minute}:${second}`
          }
        } catch (e) {
          console.error('时间格式化失败:', matter.createdAt, e)
        }
      }

      return {
        id: matter.matterId,
        title: matter.title,
        content: matter.content,
        createTime: timeStr
      }
    }).sort((a, b) => a.id - b.id)
    loading.value = false
  }).catch(error => {
    console.error('获取事项列表失败:', error)
    logStore.addLog('获取事项列表失败', 'error')
    showErrorMessage(error, '获取事项列表失败')
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

    // 统一做一次前端标题唯一性校验：
    // - 同一列表中不允许存在两个完全相同的标题
    // - 编辑场景下排除当前这条记录本身
    const normalizedTitle = (dataToSend.title || '').trim()
    const hasDuplicate = taskList.value.some(item => {
      if (!item.title) return false
      const sameTitle = item.title.trim() === normalizedTitle
      const isSameRecord = dataToSend.matterId != null && item.id === dataToSend.matterId
      return sameTitle && !isSameRecord
    })

    if (hasDuplicate) {
      ElMessage.warning({
        message: '已存在同名事项，请修改标题以保持唯一',
        duration: 3000
      })
      return
    }

    dataToSend.title = normalizedTitle

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
      showErrorMessage(error, `${action}失败`)
    })
  })
}

// 删除事项
const handleDeleteTask = (row) => {
  ElMessageBox.confirm(
    `确定要删除事项 "${row.title}" 吗？删除后该事项的所有信息将被永久移除，此操作不可恢复。`,
    '删除事项确认',
    {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      distinguishCancelAndClose: true,
      confirmButtonClass: 'el-button--danger'
    }
  ).then(() => {
    deleteCompanyMatter(row.id).then(() => {
      getTaskList()
      logStore.addLog(`删除了事项: "${row.title}" (ID: ${row.id})`)
      ElMessage.success('删除成功')
    }).catch(error => {
      console.error('删除事项失败:', error)
      logStore.addLog(`删除事项 "${row.title}" 失败`, 'error')
      ElMessage.error('删除失败，请稍后重试')
    })
  }).catch(() => {
    // 用户取消删除，不显示提示
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