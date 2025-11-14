<template>
  <div class="app-container">
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">
          公司十大事项
        </h2>
        <el-button
          type="primary"
          @click="handleAddTask"
        >
          <el-icon><Plus /></el-icon> 新增事项
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
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useLogStore } from '@/store/log';
import request from '@/utils/request'; // <--- 确保引入了真实的 request 实例

// --- 后端 API 调用函数 ---
// (注意：这里直接使用 request，不再需要包裹在 api 对象里)
const getTasks = (params = {}) => {
  return request({
    url: '/api/company-matters',
    method: 'get',
    params
  });
};

const createTask = (data) => {
  return request({
    url: '/api/company-matters',
    method: 'post',
    data
  });
};

const updateTask = (id, data) => {
  return request({
    url: `/api/company-matters/${id}`,
    method: 'put',
    data
  });
};

const deleteTask = (id) => {
  return request({
    url: `/api/company-matters/${id}`,
    method: 'delete'
  });
};
// --- API 调用函数结束 ---

const logStore = useLogStore();
const taskList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const taskFormRef = ref(null);

// 表单数据模型 (与后端 Pojo 对应)
const initialForm = {
  matterId: null, // 使用 matterId
  title: '',
  content: ''
};
const taskForm = ref({ ...initialForm });

// 表单校验规则
const taskRules = {
  title: [{ required: true, message: '请输入事项标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细内容', trigger: 'blur' }],
};

// 获取事项列表 (调用真实 API)
const getTaskList = () => {
  loading.value = true;
  getTasks().then(response => { // 直接调用 getTasks 函数
    // 后端直接返回 CompanyMattersPojo 数组
    const matters = Array.isArray(response) ? response : [];
    // 将后端字段映射到前端表格需要的字段
    taskList.value = matters.map(matter => ({
      id: matter.matterId,           // matterId -> id
      title: matter.title,
      content: matter.content,
      createTime: formatDateTime(matter.createdAt) // createdAt -> createTime 并格式化
    })).sort((a, b) => a.id - b.id); // 按ID排序
    loading.value = false;
  }).catch(error => {
    console.error('获取事项列表失败:', error);
    logStore.addLog('获取事项列表失败', 'error');
    ElMessage.error('获取事项列表失败');
    taskList.value = []; // 清空列表
    loading.value = false;
  });
};

// 重置表单
const resetForm = () => {
  taskForm.value = { ...initialForm };
  if (taskFormRef.value) {
    // Element Plus 2.3+ 使用 clearValidate
    try {
      taskFormRef.value.clearValidate();
    } catch {
      // 旧版本可能使用 resetFields，做个兼容或根据您的版本调整
      try {
        taskFormRef.value.resetFields();
      } catch {
        // 忽略错误
      }
    }
  }
};

// 新增弹窗
const handleAddTask = () => {
  resetForm();
  dialogTitle.value = '新增事项';
  dialogVisible.value = true;
};

// 编辑弹窗
const handleEditTask = (row) => {
  // 从表格行数据填充表单 (注意字段映射)
  taskForm.value = {
    matterId: row.id, // 表格的 id 是 matterId
    title: row.title,
    content: row.content
  };
  dialogTitle.value = '编辑事项';
  dialogVisible.value = true;
};

// 提交表单 (调用真实 API)
const submitForm = () => {
  taskFormRef.value.validate(valid => {
    if (!valid) return;

    // 准备发送给后端的数据 (只包含后端 Pojo 有的字段)
    const dataToSend = {
      matterId: taskForm.value.matterId,
      title: taskForm.value.title,
      content: taskForm.value.content
      // creatorId 由后端设置
    };

    const isEdit = dataToSend.matterId !== null;
    const apiCall = isEdit
        ? updateTask(dataToSend.matterId, dataToSend) // 调用 updateTask 函数
        : createTask(dataToSend);                     // 调用 createTask 函数
    const action = isEdit ? '更新' : '创建';

    apiCall.then(() => { // 后端操作成功后返回更新/创建的对象
      getTaskList(); // 成功后刷新列表
      logStore.addLog(`${action}了事项: "${dataToSend.title}"`);
      ElMessage.success(`${action}成功`);
      dialogVisible.value = false;
    }).catch(error => {
      console.error(`${action}事项失败:`, error);
      logStore.addLog(`${action}事项 "${dataToSend.title}" 失败`, 'error');
    });
  });
};

// 删除事项 (调用真实 API)
const handleDeleteTask = (row) => {
  ElMessageBox.confirm(`确定要删除事项 "${row.title}" 吗?`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(() => {
    deleteTask(row.id).then(() => { // 调用 deleteTask 函数，使用表格行的 id (matterId)
      getTaskList(); // 刷新列表
      logStore.addLog(`删除了事项: "${row.title}" (ID: ${row.id})`);
      ElMessage.success('删除成功');
    }).catch(error => {
      console.error('删除事项失败:', error);
      logStore.addLog(`删除事项 "${row.title}" 失败`, 'error');
    });
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 格式化日期时间函数 (辅助)
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  try {
    const date = new Date(dateTimeString);
    if (isNaN(date.getTime())) return dateTimeString; // 无效日期则返回原值

    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (e) {
    console.error("Error formatting date:", dateTimeString, e);
    return dateTimeString; // 返回原始值以防出错
  }
};


// --- 生命周期钩子 ---
onMounted(() => {
  getTaskList(); // 页面加载时获取列表
});
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