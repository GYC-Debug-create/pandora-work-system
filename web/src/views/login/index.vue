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
      <p id="message-area" style="color: red; text-align: center; min-height: 1.2em;"></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus'; // ElMessage 用于成功提示
import { User, Lock } from '@element-plus/icons-vue';
import { login } from '@/api/user'; // <--- 引入真实的 login API

const router = useRouter();
const loginFormRef = ref(null);
const loading = ref(false);
const messageArea = ref(null); // 用于显示错误信息

const loginForm = ref({
  username: 'admin', // 可以保留默认值方便测试
  password: '',
});

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

// 获取 message-area DOM 元素 (Vue 3 推荐方式)
import { onMounted } from 'vue';
onMounted(() => {
  messageArea.value = document.getElementById('message-area');
});

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      if (messageArea.value) messageArea.value.textContent = ''; // 清空之前的错误消息

      // 调用真实的登录 API
      login({
        username: loginForm.value.username,
        // 注意：根据后端 UserController，后端期望的是 passwordHash 字段接收原始密码
        passwordHash: loginForm.value.password
      }).then(userData => {
        // 登录成功，后端返回了用户信息 (不含密码)
        ElMessage.success(`登录成功，欢迎 ${userData.username}`);
        // 将用户信息（或token）存储到本地，用于后续请求认证
        // 这里简单存储一个标记，实际项目中应存储JWT Token
        localStorage.setItem('admin-token', 'logged-in'); // 使用简单标记
        localStorage.setItem('user-info', JSON.stringify(userData)); // 可选：存储用户信息

        router.push('/'); // 跳转到仪表盘或首页
      }).catch(error => {
        // 登录 API 调用失败（包括网络错误和后端返回的401等）
        // 错误消息已在 request.js 的拦截器中通过 ElMessage 显示
        // 我们也可以在这里额外在页面上显示错误
        if (messageArea.value) {
          messageArea.value.textContent = error.message || '登录失败，请检查网络或后台服务。';
        }
        console.error('登录处理失败:', error);
      }).finally(() => {
        loading.value = false; // 无论成功失败，结束加载状态
      });
    } else {
      // 表单校验失败
      console.log('表单校验失败');
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