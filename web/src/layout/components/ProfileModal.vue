<template>
  <el-dialog
    :model-value="visible"
    title="个人中心"
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-descriptions
      :column="1"
      border
    >
      <el-descriptions-item label="用户名">
        admin
      </el-descriptions-item>
      <el-descriptions-item label="角色">
        系统管理员
      </el-descriptions-item>
    </el-descriptions>
    
    <el-divider />
    
    <h4>修改密码</h4>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item
        label="旧密码"
        prop="oldPassword"
      >
        <el-input
          v-model="form.oldPassword"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item
        label="新密码"
        prop="newPassword"
      >
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item
        label="确认新密码"
        prop="confirmPassword"
      >
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          确认修改
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user' // 引入修改密码 API

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible'])

const formRef = ref(null)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
// 加载状态，用于防止重复提交
const loading = ref(false)

// 自定义校验规则：新密码不能与旧密码相同
const validateNewPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else if (value === form.value.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    callback()
  }
}

// 自定义校验规则：确认密码
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.value.newPassword) {
    callback(new Error("两次输入的新密码不一致!"))
  } else {
    callback()
  }
}

const rules = ref({
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
      { required: true, message: '请确认新密码', trigger: 'blur' },
      { validator: validatePass2, trigger: 'blur' }
  ],
})

const handleClose = () => {
  emit('update:visible', false)
  // 延迟重置表单，避免在关闭动画期间看到字段被清空
  setTimeout(() => {
    // 增加安全检查，避免 formRef 为 null 导致错误
    if (formRef.value) {
      formRef.value.resetFields()
    }
  }, 200)
}

/**
 * 提交修改密码表单
 * 调用后端 /api/user/change-password 接口
 */
const handleSubmit = async () => {
  // 先进行前端表单校验
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  let success = false
  try {
    // 调用后端修改密码接口
    const response = await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    })

    // 成功提示
    ElMessage.success(response.message || '密码修改成功')
    success = true

    // 密码修改成功后，延迟1.5秒后自动退出登录
    setTimeout(() => {
      sessionStorage.removeItem('admin-token')
      sessionStorage.removeItem('user-info')
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    // 错误处理:后端返回的错误信息已经在 request.js 的拦截器中统一处理
    // 这里只需要处理特殊情况
    console.error('修改密码失败:', error)

    // 如果后端返回了具体的错误信息,优先展示
    if (error.response?.data?.error) {
      ElMessage.error(error.response.data.error)
    } else if (error.message && error.message !== '会话无效') {
      // 避免重复显示已经在拦截器中处理的错误
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
    // 成功后关闭弹窗，确保在 loading 状态重置之后执行
    if (success) {
      handleClose()
    }
  }
}
</script>
