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
      <div class="login-tips">
        <p>用户名: admin</p>
        <p>密码: 任意输入</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  username: 'admin',
  password: '',
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      // 模拟登录请求
      setTimeout(() => {
        if (loginForm.value.username === 'admin') {
          ElMessage.success('登录成功')
          // 设置一个模拟的token表示已登录
          localStorage.setItem('admin-token', 'a-mock-token-that-should-be-real')
          router.push('/')
        } else {
          ElMessage.error('用户名或密码错误')
        }
        loading.value = false
      }, 500)
    }
  })
}
</script>

<style scoped>
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
