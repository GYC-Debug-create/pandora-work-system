<template>
  <el-dialog
    :model-value="visible"
    title="个人中心"
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户名">admin</el-descriptions-item>
      <el-descriptions-item label="角色">系统管理员</el-descriptions-item>
    </el-descriptions>
    
    <el-divider />
    
    <h4>修改密码</h4>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          确认修改
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'

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
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
      { required: true, message: '请确认新密码', trigger: 'blur' },
      { validator: validatePass2, trigger: 'blur' }
  ],
})

const handleClose = () => {
  emit('update:visible', false)
  // 延迟重置表单，避免在关闭动画期间看到字段被清空
  setTimeout(() => {
    formRef.value.resetFields()
  }, 200)
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 模拟提交
      ElMessage.success('密码修改成功（模拟操作）')
      handleClose()
    }
  })
}
</script>
