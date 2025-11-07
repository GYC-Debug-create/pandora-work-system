<template>
  <div class="app-container">
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
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">用户列表</h2>
      </div>

      <el-card class="filter-container" shadow="never">
        <el-form :inline="true" :model="queryParams" ref="queryFormRef" class="demo-form-inline">
          <el-form-item label="用户名" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入用户名" clearable style="width: 180px;"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="queryParams.email" placeholder="请输入邮箱" clearable style="width: 180px;"></el-input>
          </el-form-item>
          <el-form-item label="角色" prop="role">
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

      <el-table
          v-loading="loading"
          :data="userList"
          border
          stripe
          style="width: 100%; margin-top: 20px;">
        <el-table-column prop="userId" label="用户ID" width="180"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="fullName" label="全名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="roleName" label="角色" width="120"></el-table-column>
        <el-table-column prop="departmentName" label="部门" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '活跃' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" :formatter="formatDateTime"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

      <el-dialog
          :title="dialogTitle"
          v-model="dialogVisible"
          width="600px"
          append-to-body
          @close="resetForm">
        <el-form
            ref="userFormRef"
            :model="userForm"
            :rules="rules"
            label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item v-if="!userForm.userId" label="初始密码" prop="passwordHash">
            <el-input v-model="userForm.passwordHash" type="password" placeholder="请输入初始密码" show-password></el-input>
          </el-form-item>
          <el-form-item label="全名" prop="fullName">
            <el-input v-model="userForm.fullName" placeholder="请输入全名"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
          </el-form-item>
          <el-form-item label="MBTI" prop="mbtiType">
            <el-select 
              v-model="userForm.mbtiType" 
              placeholder="请选择MBTI类型" 
              clearable 
              style="width: 100%;"
              popper-class="mbti-select-dropdown">
              <el-option 
                v-for="mbti in mbtiOptions" 
                :key="mbti.value" 
                :label="`${mbti.icon} ${mbti.value} - ${mbti.name}`" 
                :value="mbti.value">
                <el-tooltip placement="right" effect="light" :show-after="300">
                  <template #content>
                    <div class="mbti-tooltip">
                      <div class="mbti-tooltip-header" :style="{background: mbti.color}">
                        <span class="mbti-icon">{{ mbti.icon }}</span>
                        <div>
                          <div class="mbti-code">{{ mbti.value }}</div>
                          <div class="mbti-name">{{ mbti.name }}</div>
                        </div>
                      </div>
                      <div class="mbti-tooltip-body">
                        <div class="mbti-trait">{{ mbti.trait }}</div>
                        <div class="mbti-desc">{{ mbti.description }}</div>
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
          <el-form-item label="部门" prop="departmentId">
            <el-select v-model="userForm.departmentId" placeholder="请选择部门" style="width: 100%;">
              <el-option
                  v-for="item in departmentOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="角色" prop="roleId">
            <el-select v-model="userForm.roleId" placeholder="请选择角色" style="width: 100%;">
              <el-option
                  v-for="item in roleOptionsForForm"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="userForm.status" placeholder="请选择状态" style="width: 100%;">
              <el-option label="活跃" value="活跃"></el-option>
              <el-option label="禁用" value="禁用"></el-option>
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
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { User, CircleCheck, CircleClose, Plus } from '@element-plus/icons-vue';
import {
  getUserList as fetchUsers,
  createUser,
  updateUser,
  deleteUser
} from '@/api/user';
import { getDepartments } from '@/api/department';
import { getRoles } from '@/api/role'; // <--- 确保引入 getRoles
import { useLogStore } from '@/store/log';

const logStore = useLogStore();

// --- MBTI 数据配置 ---
const mbtiOptions = ref([
  { 
    value: 'ISTJ', name: '物流师', icon: '📋', color: '#5B9BD5',
    trait: '靠谱·务实·传统',
    description: '超级靠谱的实干家！做事有条不紊，说到做到。喜欢按部就班，是团队里最值得信赖的人。虽然不太爱变化，但稳定性max！',
    famous: '华盛顿、杜鲁门、巴菲特'
  },
  { 
    value: 'ISFJ', name: '守卫者', icon: '🛡️', color: '#70AD47',
    trait: '温暖·细心·奉献',
    description: '温柔贴心的守护天使！总是默默关心身边的人，记得每个人的小细节。愿意为他人付出，是最值得珍惜的伙伴。',
    famous: '泰勒·斯威夫特、贝多芬、特蕾莎修女'
  },
  { 
    value: 'INFJ', name: '提倡者', icon: '🌟', color: '#9B59B6',
    trait: '理想·神秘·洞察',
    description: '神秘的理想主义者！拥有超强的洞察力，仿佛能看透人心。既温柔又坚定，致力于让世界变得更美好。最稀有的人格类型哦！',
    famous: '甘地、马丁·路德·金、尼尔森·曼德拉'
  },
  { 
    value: 'INTJ', name: '建筑师', icon: '🧠', color: '#8E44AD',
    trait: '独立·战略·完美',
    description: '超级大脑拥有者！逻辑思维爆表，总能看到别人看不到的可能性。追求完美，喜欢独立思考。是天生的战略家和创新者！',
    famous: '埃隆·马斯克、马克·扎克伯格、尼采'
  },
  { 
    value: 'ISTP', name: '鉴赏家', icon: '🔧', color: '#95A5A6',
    trait: '冷静·灵巧·探索',
    description: '超酷的实用主义者！动手能力超强，什么都能捯饬明白。喜欢独处，但行动力爆表。解决问题的高手，永远保持冷静！',
    famous: '克林特·伊斯特伍德、汤姆·克鲁斯、迈克尔·乔丹'
  },
  { 
    value: 'ISFP', name: '探险家', icon: '🎨', color: '#E74C3C',
    trait: '温柔·艺术·自由',
    description: '温柔的艺术家灵魂！热爱美的一切，活在当下。不喜欢被约束，追求自由自在的生活。用独特的方式感受世界！',
    famous: '迈克尔·杰克逊、奥黛丽·赫本、布兰妮'
  },
  { 
    value: 'INFP', name: '调停者', icon: '🦄', color: '#3498DB',
    trait: '理想·浪漫·治愈',
    description: '纯真的梦想家！内心住着一个小王子/小公主。富有同情心，用想象力创造自己的世界。虽然看起来柔软，但坚守内心的原则！',
    famous: 'J.K.罗琳、莎士比亚、约翰尼·德普'
  },
  { 
    value: 'INTP', name: '逻辑学家', icon: '🔬', color: '#2ECC71',
    trait: '聪明·好奇·创新',
    description: '脑洞大开的思考者！永远在思考"为什么"。热爱探索新知识，能把复杂的东西讲得超简单。有时候会沉浸在自己的思维世界里～',
    famous: '爱因斯坦、比尔·盖茨、达尔文'
  },
  { 
    value: 'ESTP', name: '企业家', icon: '⚡', color: '#F39C12',
    trait: '大胆·活力·实干',
    description: '超级行动派！精力充沛，敢想敢做。喜欢冒险和刺激，讨厌纸上谈兵。社交达人，走到哪儿都是焦点。今朝有酒今朝醉！',
    famous: '海明威、唐纳德·特朗普、布鲁斯·威利斯'
  },
  { 
    value: 'ESFP', name: '表演者', icon: '🎭', color: '#E67E22',
    trait: '热情·幽默·阳光',
    description: '人间小太阳！走到哪儿都带来欢乐。热爱生活，享受当下每一刻。天生的社交王者，派对上最受欢迎的人就是你！',
    famous: '玛丽莲·梦露、贾斯汀·比伯、麦莉·赛勒斯'
  },
  { 
    value: 'ENFP', name: '竞选者', icon: '🎪', color: '#1ABC9C',
    trait: '热情·创意·鼓舞',
    description: '超级能量球！充满热情和创意，总有新点子。善于激励他人，是天生的啦啦队长。虽然有点三分钟热度，但魅力无人能挡！',
    famous: '小罗伯特·唐尼、罗宾·威廉姆斯、安妮·海瑟薇'
  },
  { 
    value: 'ENTP', name: '辩论家', icon: '💡', color: '#16A085',
    trait: '聪明·挑战·创新',
    description: '鬼才般的辩手！脑子转得飞快，喜欢挑战权威。能把黑的说成白的，白的说成黑的。创新想法一堆，永远不走寻常路！',
    famous: '托马斯·爱迪生、本杰明·富兰克林、马克·吐温'
  },
  { 
    value: 'ESTJ', name: '总经理', icon: '👔', color: '#2C3E50',
    trait: '高效·果断·领导',
    description: '天生的领导者！执行力超强，说一不二。喜欢制定规则和标准，是团队的主心骨。虽然有点严格，但绝对能把事情搞定！',
    famous: '米歇尔·奥巴马、约翰·洛克菲勒、亨利·福特'
  },
  { 
    value: 'ESFJ', name: '执政官', icon: '🤝', color: '#27AE60',
    trait: '热心·和谐·负责',
    description: '贴心的大管家！超级会照顾人，总能营造温暖的氛围。重视传统和关系，是最受欢迎的组织者。朋友圈的万能胶！',
    famous: '泰勒·斯威夫特（也是ISFJ）、詹妮弗·加纳'
  },
  { 
    value: 'ENFJ', name: '主人公', icon: '🌈', color: '#9B59B6',
    trait: '魅力·鼓舞·理想',
    description: '天生的人生导师！超有感染力，能激发他人的潜能。既有领导力又富有同情心。是那种能改变世界的人，闪闪发光！',
    famous: '奥巴马、奥普拉·温弗瑞、本·阿弗莱克'
  },
  { 
    value: 'ENTJ', name: '指挥官', icon: '👑', color: '#C0392B',
    trait: '果断·高效·战略',
    description: '超强CEO气质！目标明确，执行力爆表。天生的战略家和领导者。虽然有时候太强势，但确实能带领团队走向成功！',
    famous: '史蒂夫·乔布斯、玛格丽特·撒切尔、戈登·拉姆齐'
  }
]);

// --- Refs and Reactive Variables ---
const queryParams = ref({ name: '', email: '', role: '', pageNum: 1, pageSize: 10 });
const userList = ref([]);
const total = ref(0);
const loading = ref(false);
const departmentOptions = ref([]); // 部门选项 (value: id, label: name)
const roleOptionsForForm = ref([]); // 角色选项 - 用于表单 (value: id, label: name)
const roleOptionsForFilter = ref([]); // 角色选项 - 用于筛选 (value: name, label: name)

const dialogVisible = ref(false);
const dialogTitle = ref('');
const initialFormState = { userId: undefined, username: '', passwordHash: '', fullName: '', email: '', roleId: null, status: '活跃', mbtiType: '', departmentId: null };
const userForm = ref({ ...initialFormState });

const userFormRef = ref();
const queryFormRef = ref();

// --- Computed Properties ---
const userStats = computed(() => {
  // 确保 userList 是数组再进行计算
  const currentUsers = Array.isArray(userList.value) ? userList.value : [];
  return {
    total: total.value, // 使用后端返回的总数
    // 注意：状态字段需要后端返回才能准确统计
    active: currentUsers.filter(user => user.status === '活跃').length,
    inactive: currentUsers.filter(user => user.status === '禁用').length,
  }
});

// --- Validation Rules ---
const rules = {
  username: [ { required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' } ],
  passwordHash: [ { required: true, message: '请输入初始密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur'} ],
  email: [ { required: true, message: '请输入邮箱地址', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] } ],
  roleId: [ { required: true, message: '请选择用户角色', trigger: 'change' } ], // 校验 roleId
  departmentId: [ { required: true, message: '请选择部门', trigger: 'change' } ]
};

// --- API Call Functions ---

// 获取部门列表
const fetchDepartments = () => {
  getDepartments().then(response => {
    // 假设 response 是 DepartmentPojo 数组
    departmentOptions.value = response.map(dep => ({ value: dep.departmentId, label: dep.departmentName }));
    // 设置新增时的默认部门
    if (!userForm.value.userId && departmentOptions.value.length > 0) {
      userForm.value.departmentId = departmentOptions.value[0].value;
    }
  }).catch(error => {
    console.error('获取部门列表失败:', error);
    ElMessage.error('无法加载部门列表');
    logStore.addLog('获取部门列表失败', 'error');
  });
};

// 获取角色列表 (添加此函数)
const fetchRoles = () => {
  getRoles().then(response => {
    // 假设 response 是 RolePojo 数组 [{ roleId: 1, roleName: 'ADMIN' }, ...]
    roleOptionsForForm.value = response.map(role => ({ value: role.roleId, label: role.roleName }));
    roleOptionsForFilter.value = response.map(role => ({ value: role.roleName, label: role.roleName }));
    // 设置新增时的默认角色
    if (!userForm.value.userId && roleOptionsForForm.value.length > 0) {
      userForm.value.roleId = roleOptionsForForm.value[0].value;
    }
  }).catch(error => {
    console.error('获取角色列表失败:', error);
    ElMessage.error('无法加载角色列表');
    logStore.addLog('获取角色列表失败', 'error');
  });
};

// 获取用户列表
const getList = () => {
  loading.value = true;
  fetchUsers(queryParams.value).then(response => {
    // 确保后端返回了预期的分页结构 { records: [...], total: xx }
    const users = Array.isArray(response?.records) ? response.records : [];
    userList.value = users;
    total.value = response?.total || 0; // 使用后端返回的总数
    loading.value = false;
  }).catch((error) => {
    console.error('获取用户列表失败:', error);
    logStore.addLog('获取用户列表失败', 'error');
    userList.value = []; // 清空列表以防显示旧数据
    total.value = 0;
    loading.value = false;
  });
};

// --- Event Handlers ---

// 查询按钮
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

// 重置查询
const resetQuery = () => {
  if (queryFormRef.value) {
    queryFormRef.value.resetFields();
  }
  // 手动重置一下 role，因为 resetFields 可能对 select 有问题
  queryParams.value.name = '';
  queryParams.value.email = '';
  queryParams.value.role = '';
  queryParams.value.pageNum = 1;
  getList();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val;
  getList();
};

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val;
  queryParams.value.pageNum = 1;
  getList();
};

// 重置表单方法
const resetForm = () => {
  userForm.value = { ...initialFormState };
  // 设置默认部门和角色
  if (departmentOptions.value.length > 0) {
    userForm.value.departmentId = departmentOptions.value[0].value;
  }
  if (roleOptionsForForm.value.length > 0) {
    userForm.value.roleId = roleOptionsForForm.value[0].value;
  }
  if (userFormRef.value) {
    userFormRef.value.clearValidate();
  }
};


// 添加用户弹窗
const handleAdd = () => {
  resetForm();
  dialogTitle.value = '添加用户';
  dialogVisible.value = true;
};

// 编辑用户弹窗
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户';
  // 从列表数据填充表单
  userForm.value = {
    userId: row.userId,
    username: row.username,
    fullName: row.fullName,
    email: row.email,
    roleId: row.roleId, // 使用 roleId
    status: row.status,
    mbtiType: row.mbtiType,
    departmentId: row.departmentId,
    passwordHash: '' // 编辑时不显示密码
  };
  dialogVisible.value = true;
};

// 删除用户
const handleDelete = (row) => {
  // 检查是否为创始人或特殊角色 (需要后端返回角色名或标识)
  // if (row.roleName === 'FOUNDER' || row.roleName === 'ADMIN') {
  //    ElMessage.warning('不允许删除创始人或管理员账号');
  //    return;
  // }
  ElMessageBox.confirm(
      `确定要删除用户 ${row.username} 吗？`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    deleteUser(row.userId).then(() => {
      getList();
      ElMessage({ type: 'success', message: '删除成功!' });
      logStore.addLog(`删除了用户 "${row.username}" (ID: ${row.userId})`);
    }).catch(error => { 
      logStore.addLog(`删除用户 "${row.username}" 失败`, 'error');
    });
  }).catch(() => {
    ElMessage({ type: 'info', message: '已取消删除' });
  });
};

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
      }).catch(error => { 
        const action = isEdit ? '更新' : '创建';
        logStore.addLog(`${action}用户 "${dataToSend.username}" 失败`, 'error');
      });

    } else {
      console.log('表单校验失败');
      return false;
    }
  });
};

// 格式化日期时间
const formatDateTime = (row, column, cellValue, index) => {
  if (!cellValue) return '';
  try {
    const date = new Date(cellValue);
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (e) {
    return cellValue;
  }
};


// --- Lifecycle Hook ---
onMounted(() => {
  fetchDepartments(); // 获取部门
  fetchRoles();       // 获取角色
  getList();          // 获取用户列表
});
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