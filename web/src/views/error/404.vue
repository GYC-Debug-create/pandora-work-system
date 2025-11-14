<template>
  <div class="not-found">
    <div class="container">
      <h1>404</h1>
      <h2>页面不存在</h2>
      <p>您访问的页面不存在或已被删除</p>
      <el-button
        type="primary"
        @click="goHome"
      >
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.container {
  text-align: center;
}

h1 {
  font-size: 120px;
  color: #409EFF;
  margin: 0;
}

h2 {
  font-size: 30px;
  color: #303133;
  margin: 20px 0;
}

p {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}
</style>
