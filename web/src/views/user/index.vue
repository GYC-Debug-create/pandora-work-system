<template>
  <div class="app-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-icon" style="background: #409EFF;"><el-icon><User /></el-icon></div>
            <div class="card-text">
              <div class="card-value">{{ userStats.total }}</div>
              <div class="card-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-icon" style="background: #67C23A;"><el-icon><CircleCheck /></el-icon></div>
            <div class="card-text">
              <div class="card-value">{{ userStats.active }}</div>
              <div class="card-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-icon" style="background: #F56C6C;"><el-icon><CircleClose /></el-icon></div>
            <div class="card-text">
              <div class="card-value">{{ userStats.inactive }}</div>
              <div class="card-label">禁用用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-icon" style="background: #E6A23C;"><el-icon><Plus /></el-icon></div>
            <div class="card-text">
              <el-button type="primary" @click="handleAdd" style="width: 100%; height: 100%; font-size: 16px;">添加新用户</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户管理主容器 -->
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">用户列表</h2>
      </div>
      
      <!-- 搜索栏 -->
      <el-card class="filter-container" shadow="never">
        <el-form :inline="true" :model="queryParams" ref="queryForm" class="demo-form-inline">
          <el-form-item label="用户名" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入用户名" clearable style="width: 180px;"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="queryParams.email" placeholder="请输入邮箱" clearable style="width: 180px;"></el-input>
          </el-form-item>
          <el-form-item label="角色" prop="role">
            <el-select 
              v-model="queryParams.role" 
              :value="queryParams.role" 
              placeholder="请选择角色" 
              clearable
              style="width: 180px;"
            >
              <el-option 
                v-for="item in roleOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 用户表格 -->
      <el-table
        v-loading="loading"
        :data="userList"
        border
        stripe
        style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="姓名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="role" label="角色" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '活跃' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button 
              size="small" 
              @click="handleEdit(scope.row)"
              :disabled="scope.row.role === '创始人'"
            >编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
              :disabled="scope.row.role === '创始人'"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
      
      <!-- 添加/编辑用户对话框 -->
      <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="500px"
        append-to-body>
        <el-form
          ref="userFormRef"
          :model="userForm"
          :rules="rules"
          label-width="80px">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="userForm.name" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
          </el-form-item>
          <el-form-item label="角色" prop="role">
            <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%;">
              <el-option
                v-for="item in formRoleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="userForm.status" placeholder="请选择状态" style="width: 100%;">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUserList as fetchUsers,
  createUser,
  updateUser,
  deleteUser
} from '@/api/user'
import { useLogStore } from '@/store/log'

const logStore = useLogStore()

// 查询参数
const queryParams = ref({
  name: '',
  email: '',
  role: '',
  pageNum: 1,
  pageSize: 10
})

// 用户列表
const userList = ref([])
const total = ref(0)
const loading = ref(false)

// 用户统计
const userStats = computed(() => {
  return {
    total: userList.value.length,
    active: userList.value.filter(user => user.status === '活跃').length,
    inactive: userList.value.filter(user => user.status === '禁用').length,
  }
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userForm = ref({
  id: undefined,
  name: '',
  email: '',
  role: '',
  status: '活跃'
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入用户姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ]
}

// 表单引用
const userFormRef = ref()
const queryForm = ref()

// 角色选项
const roleOptions = [
  { value: '创始人', label: '创始人' },
  { value: '部门老总', label: '部门老总' },
  { value: '团队长', label: '团队长' },
  { value: '普通用户', label: '普通用户' }
]

// 用于表单的动态角色选项 (禁止创建第二个创始人，编辑时也不能改为创始人)
const formRoleOptions = computed(() => {
  const founderExists = userList.value.some(user => user.role === '创始人')
  const isEditingFounder = dialogTitle.value === '编辑用户' && userForm.value.role === '创始人'
  
  // 只有在以下情况才显示创始人选项：
  // 1. 创始人不存在（新增模式）
  // 2. 正在编辑的就是创始人本人
  if (!founderExists || isEditingFounder) {
    return roleOptions
  } else {
    return roleOptions.filter(option => option.value !== '创始人')
  }
})

// 状态选项
const statusOptions = [
  { value: '活跃', label: '活跃' },
  { value: '禁用', label: '禁用' }
]

// 获取用户列表
const getList = () => {
  loading.value = true
  fetchUsers(queryParams.value).then(response => {
    // 增加严格的数组类型检查，确保健壮性
    const users = Array.isArray(response.data) ? response.data : []
    
    // 按ID排序
    users.sort((a, b) => a.id - b.id)

    userList.value = users
    total.value = response.total || users.length
    loading.value = false
  }).catch((error) => {
    console.error('获取用户列表失败:', error)
    loading.value = false
  })
}

// 查询按钮
const handleQuery = () => {
  // 搜索后重置到第一页
  queryParams.value.pageNum = 1
  // 重新获取数据，让后端处理筛选
  getList()
}

// 重置查询
const resetQuery = () => {
  if (queryForm.value) {
    queryForm.value.resetFields()
  }
  // 重置查询参数并重新获取数据
  queryParams.value.pageNum = 1
  getList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  getList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  getList()
}

// 添加用户
const handleAdd = () => {
  dialogTitle.value = '添加用户'
  dialogVisible.value = true
  
  // 重置表单
  userForm.value.id = undefined
  userForm.value.name = ''
  userForm.value.email = ''
  userForm.value.role = ''
  userForm.value.status = '活跃'
}

// 编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
  
  // 填充表单
  userForm.value.id = row.id
  userForm.value.name = row.name
  userForm.value.email = row.email
  userForm.value.role = row.role
  userForm.value.status = row.status
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${row.name} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deleteUser(row.id).then(() => {
      // 重新获取数据
      getList()
      ElMessage({ type: 'success', message: '删除成功!' })
      logStore.addLog(`删除了用户 "${row.name}" (ID: ${row.id})`)
    }).catch(error => {
      console.error("删除用户失败:", error)
      ElMessage({ type: 'error', message: '删除失败，请查看控制台。' })
    })
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: '已取消删除'
    })
  })
}

// 提交表单
const submitForm = () => {
  userFormRef.value.validate((valid) => {
    if (valid) {
      if (userForm.value.id !== undefined) {
        // 更新用户 - 调用API
        updateUser(userForm.value.id, userForm.value).then(() => {
          // 重新获取数据
          getList()
          ElMessage({ type: 'success', message: '更新成功!' })
          logStore.addLog(`更新了用户 "${userForm.value.name}" (ID: ${userForm.value.id}) 的信息`)
          dialogVisible.value = false
        }).catch(error => {
          console.error("更新用户失败:", error)
          ElMessage({ type: 'error', message: '更新失败，请查看控制台。' })
        })
      } else {
        // 创建用户 - 调用API
        createUser(userForm.value).then((response) => {
          // 重新获取数据
          getList()
          ElMessage({ type: 'success', message: '添加成功!' })
          logStore.addLog(`创建了新用户 "${userForm.value.name}" (ID: ${response.data.id})`)
          dialogVisible.value = false
        }).catch(error => {
          console.error("创建用户失败:", error)
          ElMessage({ type: 'error', message: '添加失败，请查看控制台。' })
        })
      }
    }
  })
}

// 生命周期钩子
onMounted(() => {
  getList()
})
</script>

<style scoped>
.summary-cards {
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  font-size: 32px;
  color: #fff;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  margin-right: 20px;
}

.card-text {
  flex-grow: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
}

.card-label {
  font-size: 14px;
  color: #909399;
}
</style>
