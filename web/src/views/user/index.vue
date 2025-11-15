<template>
  <div class="app-container">
    <el-row
      :gutter="20"
      class="summary-cards"
    >
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div
              class="card-icon"
              style="background: #409EFF;"
            >
              <el-icon><User /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-value">
                {{ userStats.total }}
              </div>
              <div class="card-label">
                用户总数
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div
              class="card-icon"
              style="background: #67C23A;"
            >
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-value">
                {{ userStats.active }}
              </div>
              <div class="card-label">
                活跃用户
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div
              class="card-icon"
              style="background: #F56C6C;"
            >
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-value">
                {{ userStats.inactive }}
              </div>
              <div class="card-label">
                禁用用户
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div
              class="card-icon"
              style="background: #E6A23C;"
            >
              <el-icon><Plus /></el-icon>
            </div>
            <div class="card-text">
              <el-button
                type="primary"
                style="width: 100%; height: 100%; font-size: 16px;"
                @click="handleAdd"
              >
                添加新用户
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">
          用户列表
        </h2>
      </div>

      <el-card
        class="filter-container"
        shadow="never"
      >
        <el-form
          ref="queryFormRef"
          :inline="true"
          :model="queryParams"
          class="demo-form-inline"
        >
          <el-form-item
            label="用户名"
            prop="name"
          >
            <el-input
              v-model="queryParams.name"
              placeholder="请输入用户名"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item
            label="邮箱"
            prop="email"
          >
            <el-input
              v-model="queryParams.email"
              placeholder="请输入邮箱"
              clearable
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item
            label="角色"
            prop="role"
          >
            <el-select
              v-model="queryParams.role"
              placeholder="请选择角色"
              clearable
              style="width: 180px;"
            >
              <el-option
                v-for="item in roleOptionsForFilter"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleQuery"
            >
              搜索
            </el-button>
            <el-button @click="resetQuery">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-table
        v-loading="loading"
        :data="userList"
        border
        stripe
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column
          prop="userId"
          label="用户ID"
          width="180"
        />
        <el-table-column
          prop="username"
          label="用户名"
          width="120"
        />
        <el-table-column
          prop="fullName"
          label="全名"
          width="120"
        />
        <el-table-column
          prop="email"
          label="邮箱"
        />
        <el-table-column
          prop="roleName"
          label="角色"
          width="120"
        />
        <el-table-column
          prop="departmentName"
          label="部门"
          width="120"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="100"
        >
          <template #default="scope">
            <el-tag :type="getUserStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createdAt"
          label="创建时间"
          width="180"
          :formatter="formatDateTime"
        />
        <el-table-column
          label="操作"
          width="180"
        >
          <template #default="scope">
            <el-button
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        append-to-body
        @close="resetForm"
      >
        <el-form
          ref="userFormRef"
          :model="userForm"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item
            label="用户名"
            prop="username"
          >
            <el-input
              v-model="userForm.username"
              placeholder="请输入用户名"
            />
          </el-form-item>
          <el-form-item
            v-if="!userForm.userId"
            label="初始密码"
            prop="passwordHash"
          >
            <el-input
              v-model="userForm.passwordHash"
              type="password"
              placeholder="请输入初始密码"
              show-password
            />
          </el-form-item>
          <el-form-item
            label="全名"
            prop="fullName"
          >
            <el-input
              v-model="userForm.fullName"
              placeholder="请输入全名"
            />
          </el-form-item>
          <el-form-item
            label="邮箱"
            prop="email"
          >
            <el-input
              v-model="userForm.email"
              placeholder="请输入邮箱"
            />
          </el-form-item>
          <el-form-item
            label="MBTI"
            prop="mbtiType"
          >
            <el-select 
              v-model="userForm.mbtiType" 
              placeholder="请选择MBTI类型" 
              clearable 
              style="width: 100%;"
              popper-class="mbti-select-dropdown"
            >
              <el-option 
                v-for="mbti in mbtiOptions" 
                :key="mbti.value" 
                :label="`${mbti.icon} ${mbti.value} - ${mbti.name}`" 
                :value="mbti.value"
              >
                <el-tooltip
                  placement="right"
                  effect="light"
                  :show-after="300"
                >
                  <template #content>
                    <div class="mbti-tooltip">
                      <div
                        class="mbti-tooltip-header"
                        :style="{background: mbti.color}"
                      >
                        <span class="mbti-icon">{{ mbti.icon }}</span>
                        <div>
                          <div class="mbti-code">
                            {{ mbti.value }}
                          </div>
                          <div class="mbti-name">
                            {{ mbti.name }}
                          </div>
                        </div>
                      </div>
                      <div class="mbti-tooltip-body">
                        <div class="mbti-trait">
                          {{ mbti.trait }}
                        </div>
                        <div class="mbti-desc">
                          {{ mbti.description }}
                        </div>
                        <div class="mbti-famous">
                          <strong>代表人物：</strong>{{ mbti.famous }}
                        </div>
                      </div>
                    </div>
                  </template>
                  <span>{{ mbti.icon }} {{ mbti.value }} - {{ mbti.name }}</span>
                </el-tooltip>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            label="部门"
            prop="departmentId"
          >
            <el-select
              v-model="userForm.departmentId"
              placeholder="请选择部门"
              style="width: 100%;"
            >
              <el-option
                v-for="item in departmentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label="角色"
            prop="roleId"
          >
            <el-select
              v-model="userForm.roleId"
              placeholder="请选择角色"
              style="width: 100%;"
            >
              <el-option
                v-for="item in roleOptionsForForm"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label="状态"
            prop="status"
          >
            <el-select
              v-model="userForm.status"
              placeholder="请选择状态"
              style="width: 100%;"
            >
              <el-option
                v-for="item in USER_STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, CircleCheck, CircleClose, Plus } from '@element-plus/icons-vue'
import {
  getUserList as fetchUsers,
  createUser,
  updateUser,
  deleteUser
} from '@/api/user'
import { getDepartments } from '@/api/department'
import { getRoles } from '@/api/role'
import { useLogStore } from '@/store/log'
import { formatDateTime } from '@/utils/format'
import { MBTI_OPTIONS } from '@/constants/mbti'
import { USER_STATUS, USER_STATUS_OPTIONS, getUserStatusTagType } from '@/constants/user'

const logStore = useLogStore()

// MBTI 数据配置
const mbtiOptions = ref(MBTI_OPTIONS)

// Refs and Reactive Variables
const queryParams = ref({ name: '', email: '', role: '', pageNum: 1, pageSize: 10 })
const userList = ref([])
const total = ref(0)
const loading = ref(false)
const departmentOptions = ref([])
const roleOptionsForForm = ref([])
const roleOptionsForFilter = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const initialFormState = {
  userId: undefined,
  username: '',
  passwordHash: '',
  fullName: '',
  email: '',
  roleId: null,
  status: USER_STATUS.ACTIVE,
  mbtiType: '',
  departmentId: null
}
const userForm = ref({ ...initialFormState })

const userFormRef = ref()
const queryFormRef = ref()

// Computed Properties
const userStats = computed(() => {
  const currentUsers = Array.isArray(userList.value) ? userList.value : []
  return {
    total: total.value,
    active: currentUsers.filter(user => user.status === USER_STATUS.ACTIVE).length,
    inactive: currentUsers.filter(user => user.status === USER_STATUS.INACTIVE).length
  }
})

// Validation Rules
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  passwordHash: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  roleId: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ]
}

// API Call Functions

// 获取部门列表
const fetchDepartments = () => {
  getDepartments().then(response => {
    departmentOptions.value = response.map(dep => ({
      value: dep.departmentId,
      label: dep.departmentName
    }))
    if (!userForm.value.userId && departmentOptions.value.length > 0) {
      userForm.value.departmentId = departmentOptions.value[0].value
    }
  }).catch(error => {
    console.error('获取部门列表失败:', error)
    ElMessage.error('无法加载部门列表')
    logStore.addLog('获取部门列表失败', 'error')
  })
}

// 获取角色列表
const fetchRoles = () => {
  getRoles().then(response => {
    roleOptionsForForm.value = response.map(role => ({
      value: role.roleId,
      label: role.roleName
    }))
    roleOptionsForFilter.value = response.map(role => ({
      value: role.roleName,
      label: role.roleName
    }))
    if (!userForm.value.userId && roleOptionsForForm.value.length > 0) {
      userForm.value.roleId = roleOptionsForForm.value[0].value
    }
  }).catch(error => {
    console.error('获取角色列表失败:', error)
    ElMessage.error('无法加载角色列表')
    logStore.addLog('获取角色列表失败', 'error')
  })
}

// 获取用户列表
const getList = () => {
  loading.value = true
  fetchUsers(queryParams.value).then(response => {
    const users = Array.isArray(response?.records) ? response.records : []
    userList.value = users
    total.value = response?.total || 0
    loading.value = false
  }).catch((error) => {
    console.error('获取用户列表失败:', error)
    logStore.addLog('获取用户列表失败', 'error')
    userList.value = []
    total.value = 0
    loading.value = false
  })
}

// Event Handlers

// 查询按钮
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  if (queryFormRef.value) {
    queryFormRef.value.resetFields()
  }
  queryParams.value.name = ''
  queryParams.value.email = ''
  queryParams.value.role = ''
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
  queryParams.value.pageNum = 1
  getList()
}

// 重置表单方法
const resetForm = () => {
  userForm.value = { ...initialFormState }
  if (departmentOptions.value.length > 0) {
    userForm.value.departmentId = departmentOptions.value[0].value
  }
  if (roleOptionsForForm.value.length > 0) {
    userForm.value.roleId = roleOptionsForForm.value[0].value
  }
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

// 添加用户弹窗
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '添加用户'
  dialogVisible.value = true
}

// 编辑用户弹窗
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  userForm.value = {
    userId: row.userId,
    username: row.username,
    fullName: row.fullName,
    email: row.email,
    roleId: row.roleId,
    status: row.status,
    mbtiType: row.mbtiType,
    departmentId: row.departmentId,
    passwordHash: ''
  }
  dialogVisible.value = true
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${row.username} 吗？`,
    '警告',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    deleteUser(row.userId).then(() => {
      getList()
      ElMessage({ type: 'success', message: '删除成功!' })
      logStore.addLog(`删除了用户 "${row.username}" (ID: ${row.userId})`)
    }).catch(() => {
      logStore.addLog(`删除用户 "${row.username}" 失败`, 'error')
    })
  }).catch(() => {
    ElMessage({ type: 'info', message: '已取消删除' })
  })
}

// 提交表单 (创建或更新)
const submitForm = () => {
  userFormRef.value.validate((valid) => {
    if (valid) {
      const isEdit = userForm.value.userId !== undefined;

      // 准备发送给后端的数据
      const dataToSend = {
        username: userForm.value.username,
        fullName: userForm.value.fullName,
        email: userForm.value.email,
        mbtiType: userForm.value.mbtiType,
        departmentId: userForm.value.departmentId,
        roleId: userForm.value.roleId, // 发送 roleId
        status: userForm.value.status // 发送 status 字段
      };

      let apiCall;
      let actionLog;
      let successMsg;

      if (isEdit) {
        apiCall = updateUser(userForm.value.userId, dataToSend);
        actionLog = `更新了用户 "${dataToSend.username}" (ID: ${userForm.value.userId}) 的信息`;
        successMsg = '更新成功!';
      } else {
        if (!userForm.value.passwordHash) {
          ElMessage.error('新增用户时必须提供初始密码');
          return;
        }
        dataToSend.passwordHash = userForm.value.passwordHash; // 添加密码
        apiCall = createUser(dataToSend);
        actionLog = `创建了新用户 "${dataToSend.username}"`;
        successMsg = '添加成功!';
      }

      apiCall.then(() => {
        getList();
        ElMessage({ type: 'success', message: successMsg });
        logStore.addLog(actionLog);
        dialogVisible.value = false;
      }).catch(() => {
        const action = isEdit ? '更新' : '创建';
        logStore.addLog(`${action}用户 "${dataToSend.username}" 失败`, 'error');
      });

    } else {
      console.log('表单校验失败');
      return false;
    }
  });
};

// Lifecycle Hook
onMounted(() => {
  fetchDepartments()
  fetchRoles()
  getList()
})
</script>



<style scoped>
/* 样式部分保持不变 */
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

.filter-container {
  margin-bottom: 20px;
  padding: 15px; /* Add some padding */
}

/* Ensure form items align nicely */
.demo-form-inline .el-form-item {
  margin-right: 10px;
  margin-bottom: 10px; /* Add bottom margin for spacing */
}

.pagination-container {
  margin-top: 20px;
  display: flex; /* Use flexbox */
  justify-content: flex-end; /* Align pagination to the right */
}

/* MBTI 悬浮卡片样式 */
.mbti-tooltip {
  max-width: 280px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.mbti-tooltip-header {
  padding: 16px;
  border-radius: 8px 8px 0 0;
  color: white;
  display: flex;
  align-items: center;
  gap: 12px;
}

.mbti-icon {
  font-size: 32px;
  line-height: 1;
}

.mbti-code {
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
}

.mbti-name {
  font-size: 14px;
  opacity: 0.95;
  margin-top: 2px;
}

.mbti-tooltip-body {
  padding: 16px;
  background: white;
  border-radius: 0 0 8px 8px;
}

.mbti-trait {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  font-weight: 500;
  letter-spacing: 1px;
}

.mbti-desc {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
}

.mbti-famous {
  font-size: 13px;
  color: #888;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.mbti-famous strong {
  color: #666;
}
</style>