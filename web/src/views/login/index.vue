<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>潘多拉系统</h2>
          <span>管理员登录</span>
        </div>
      </template>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            show-password
            :prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%;"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import { login } from '@/api/user';
import { useLogStore } from '@/store/log';
import { showErrorMessage } from '@/utils/error';

const router = useRouter();
const logStore = useLogStore();
const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = ref({
  username: 'admin', // 可以保留默认值方便测试
  password: '',
});

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;

      login({
        username: loginForm.value.username,
        passwordHash: loginForm.value.password // 内部用 password，发送时映射为 passwordHash
      }).then(response => {
        // 登录成功
        const token = response.token;
        const userData = response.user;

        ElMessage.success(`登录成功，欢迎 ${userData.username}`);

        // 存储 JWT Token 到 localStorage
        localStorage.setItem('admin-token', token);
        localStorage.setItem('user-info', JSON.stringify(userData));

        // 记录系统日志
        logStore.addLog(`用户 ${userData.username} 登录系统`, 'system');

        router.push('/')
      }).catch(error => {
        // 使用统一的错误处理工具
        showErrorMessage(error, '登录失败，请重试')
      }).finally(() => {
        loading.value = false
      })
    } else {
      return false;
    }
  });
};
</script>

<style scoped>
/* 样式部分保持不变 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  background-image: url('data:image/svg+xml;charset=UTF-8,%3csvg width="100%25" height="100%25" xmlns="http://www.w3.org/2000/svg"%3e%3crect width="100%25" height="100%25" fill="none" rx="20" ry="20" stroke="%23333" stroke-width="2" stroke-dasharray="6%2c 14" stroke-dashoffset="0" stroke-linecap="square"/%3e%3c/svg%3e');
}

.login-card {
  width: 400px;
  border-radius: 8px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
}

.card-header span {
  font-size: 14px;
  color: #909399;
}

.login-tips {
  margin-top: 20px;
  text-align: left;
  font-size: 14px;
  color: #909399;
}

.login-tips p {
  margin: 5px 0;
}
</style>