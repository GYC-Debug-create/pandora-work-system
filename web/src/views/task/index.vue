<template>
  <div class="app-container">
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">公司十大事项</h2>
        <el-button type="primary" @click="handleAddTask">
          <el-icon><Plus /></el-icon> 新增事项
        </el-button>
      </div>

      <!-- 事项表格 -->
      <el-table :data="taskList" border stripe v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="事项标题" />
        <el-table-column prop="content" label="详细内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEditTask(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteTask(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 新增/编辑事项对话框 -->
      <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
        <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="80px">
          <el-form-item label="事项标题" prop="title">
            <el-input v-model="taskForm.title" placeholder="请输入事项标题" />
          </el-form-item>
          <el-form-item label="详细内容" prop="content">
            <el-input v-model="taskForm.content" type="textarea" :rows="5" placeholder="请输入事项详细内容" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitForm">确定</el-button>
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

// Mock API functions - replace with real API calls later
const api = {
  getTasks: async () => ({
    data: [
      { id: 1, title: '完成Q4季度财报', content: '按照公司要求完成季度财务报表的编写和提交', createTime: new Date().toLocaleString() },
      { id: 2, title: '启动"天王星"项目', content: '组织团队启动新一代产品研发项目', createTime: new Date().toLocaleString() },
      { id: 3, title: '全公司技术架构升级', content: '对现有系统进行全面升级，提高系统性能和安全性', createTime: new Date().toLocaleString() },
    ]
  }),
  createTask: async (data) => ({ 
    data: { 
      ...data, 
      id: Math.floor(Math.random() * 1000), 
      createTime: new Date().toLocaleString() 
    } 
  }),
  updateTask: async (id, data) => ({ data: { ...data, id } }),
  deleteTask: async (id) => ({}),
}

const logStore = useLogStore()
const taskList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const taskFormRef = ref(null)

const initialForm = {
  id: null,
  title: '',
  content: ''
}
const taskForm = ref({ ...initialForm })

const taskRules = {
  title: [{ required: true, message: '请输入事项标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细内容', trigger: 'blur' }],
}

const getTaskList = () => {
  loading.value = true
  api.getTasks().then(res => {
    taskList.value = res.data.sort((a, b) => a.id - b.id)
    loading.value = false
  }).catch(error => {
    console.error('获取事项列表失败:', error)
    loading.value = false
  })
}

onMounted(getTaskList)

const resetForm = () => {
  taskForm.value = { ...initialForm }
  taskFormRef.value?.resetFields()
}

const handleAddTask = () => {
  resetForm()
  dialogTitle.value = '新增事项'
  dialogVisible.value = true
}

const handleEditTask = (row) => {
  taskForm.value = { ...row }
  dialogTitle.value = '编辑事项'
  dialogVisible.value = true
}

const submitForm = () => {
  taskFormRef.value.validate(valid => {
    if (!valid) return

    const apiCall = taskForm.value.id ? api.updateTask(taskForm.value.id, taskForm.value) : api.createTask(taskForm.value)
    const action = taskForm.value.id ? '更新' : '创建'

    apiCall.then(() => {
      getTaskList()
      logStore.addLog(`${action}了事项: "${taskForm.value.title}"`)
      ElMessage.success(`${action}成功`)
      dialogVisible.value = false
    }).catch(error => {
      console.error(`${action}事项失败:`, error)
      ElMessage.error(`${action}失败，请重试`)
    })
  })
}

const handleDeleteTask = (row) => {
  ElMessageBox.confirm(`确定要删除事项 "${row.title}" 吗?`, '警告', {
    type: 'warning',
  }).then(() => {
    api.deleteTask(row.id).then(() => {
      getTaskList()
      logStore.addLog(`删除了事项: "${row.title}"`)
      ElMessage.success('删除成功')
    }).catch(error => {
      console.error('删除事项失败:', error)
      ElMessage.error('删除失败，请重试')
    })
  })
}
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