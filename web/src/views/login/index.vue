<template>
  <div class="login-container">
    <canvas ref="canvasRef" class="login-bg-canvas"></canvas>
    
    <!-- Theme Toggle Button -->
    <div class="theme-toggle-container" @click="toggleTheme">
      <el-icon :size="24" :color="isDark ? '#fff' : '#303133'">
        <Sunny v-if="isDark" />
        <Moon v-else />
      </el-icon>
    </div>

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
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Sunny, Moon } from '@element-plus/icons-vue';
import { login } from '@/api/user';
import { useLogStore } from '@/store/log';
import { useThemeStore } from '@/store/theme';
import { showErrorMessage } from '@/utils/error';

const router = useRouter();
const logStore = useLogStore();
const themeStore = useThemeStore();
const loginFormRef = ref(null);
const loading = ref(false);
const canvasRef = ref(null);

const loginForm = ref({
  username: 'admin',
  password: '',
});

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const isDark = computed(() => themeStore.theme === 'dark');

const toggleTheme = () => {
  themeStore.toggleTheme();
  if (canvasRef.value) {
      initParticles();
  }
};

// --- Moderate Particle Effect ---
let ctx = null;
let animationFrameId = null;
let particles = [];
let mouse = { x: undefined, y: undefined };

class Particle {
  constructor(x, y) {
    this.x = x || mouse.x;
    this.y = y || mouse.y;
    this.size = Math.random() * 1.5 + 1;
    this.speedX = (Math.random() - 0.5) * 1.2;
    this.speedY = (Math.random() - 0.5) * 1.2;
    this.opacity = Math.random() * 0.4 + 0.3;
    this.fadeSpeed = Math.random() * 0.008 + 0.004;
    
    if (isDark.value) {
        this.color = `130, 190, 220`;
    } else {
        this.color = `70, 140, 180`;
    }
  }

  update() {
    this.x += this.speedX;
    this.y += this.speedY;
    this.opacity -= this.fadeSpeed;
  }

  draw() {
    if (this.opacity <= 0) return;
    
    ctx.fillStyle = `rgba(${this.color}, ${this.opacity})`;
    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
    ctx.shadowBlur = 5;
    ctx.shadowColor = `rgba(${this.color}, ${this.opacity * 0.5})`;
    ctx.fill();
    ctx.shadowBlur = 0;
  }
}

const handleParticles = () => {
  for (let i = 0; i < particles.length; i++) {
    particles[i].update();
    particles[i].draw();
    
    if (particles[i].opacity <= 0) {
      particles.splice(i, 1);
      i--;
    }
  }
};

const animate = () => {
  if (!ctx || !canvasRef.value) return;
  const canvas = canvasRef.value;
  
  ctx.fillStyle = isDark.value ? 'rgba(0, 0, 0, 0.12)' : 'rgba(255, 255, 255, 0.12)';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  handleParticles();
  
  animationFrameId = requestAnimationFrame(animate);
};

const handleResize = () => {
  if (canvasRef.value) {
    canvasRef.value.width = window.innerWidth;
    canvasRef.value.height = window.innerHeight;
  }
};

const handleMouseMove = (e) => {
  mouse.x = e.x;
  mouse.y = e.y;
  for (let i = 0; i < 2; i++) {
      if (Math.random() > 0.3) {
          particles.push(new Particle());
      }
  }
};

const initDemoBurst = () => {
    const centerX = window.innerWidth / 2;
    const centerY = window.innerHeight / 2;
    for (let i = 0; i < 40; i++) {
        particles.push(new Particle(centerX, centerY));
    }
};

const initParticles = () => {
    initDemoBurst();
};

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;

      login({
        username: loginForm.value.username,
        passwordHash: loginForm.value.password
      }).then(response => {
        const token = response.token;
        const userData = response.user;
        ElMessage.success(`登录成功，欢迎 ${userData.username}`);
        sessionStorage.setItem('admin-token', token);
        sessionStorage.setItem('user-info', JSON.stringify(userData));
        logStore.addLog(`用户 ${userData.username} 登录系统`, 'system');
        router.push('/')
      }).catch(error => {
        showErrorMessage(error, '登录失败，请重试')
      }).finally(() => {
        loading.value = false
      })
    } else {
      return false;
    }
  });
};

onMounted(() => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d');
    canvasRef.value.width = window.innerWidth;
    canvasRef.value.height = window.innerHeight;
    
    animate();
    initDemoBurst();

    window.addEventListener('resize', handleResize);
    window.addEventListener('mousemove', handleMouseMove);
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  window.removeEventListener('mousemove', handleMouseMove);
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
  }
});
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: v-bind("isDark ? '#000000' : '#ffffff'"); 
  overflow: hidden;
  transition: background-color var(--transition-base);
}

.login-bg-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.theme-toggle-container {
  position: absolute;
  top: var(--space-6);
  right: var(--space-6);
  z-index: 10;
  cursor: pointer;
  padding: var(--space-3);
  border-radius: var(--radius-full);
  background-color: v-bind("isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)'");
  backdrop-filter: blur(10px);
  transition: all var(--transition-fast);
}

.theme-toggle-container:hover {
  background-color: v-bind("isDark ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.1)'");
  transform: scale(1.1) rotate(15deg);
}

.theme-toggle-container:active {
  transform: scale(0.95) rotate(15deg);
}

.login-card {
  width: 420px;
  border-radius: var(--radius-xl) !important;
  z-index: 1;
  background: v-bind("isDark ? 'var(--glass-bg-strong)' : 'var(--glass-bg-soft)'");
  backdrop-filter: blur(var(--glass-blur-lg));
  -webkit-backdrop-filter: blur(var(--glass-blur-lg));
  box-shadow: var(--glass-shadow-soft);
  border: 1px solid v-bind("isDark ? 'var(--glass-stroke-soft)' : 'var(--glass-stroke-muted)'");
  animation: slideUp 0.5s var(--transition-spring-subtle);
  overflow: hidden;
  position: relative;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--color-primary-gradient);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  text-align: center;
  padding: var(--space-6) 0 var(--space-4);
}

.card-header h2 {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  background: var(--color-primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.card-header span {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

/* Form Enhancements */
:deep(.el-form-item) {
  margin-bottom: var(--space-5);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
  border: 1px solid v-bind("isDark ? 'var(--color-border-medium)' : 'var(--color-border-light)'");
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-light);
  box-shadow: var(--shadow-md);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

:deep(.el-input__inner) {
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
}

:deep(.el-input__inner::placeholder) {
  color: var(--color-text-tertiary);
}

/* Button Enhancements */
:deep(.el-button--primary) {
  height: 48px;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-md);
  background: var(--color-primary-gradient);
  border: none;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-fast);
  letter-spacing: 0.5px;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #5f3f89 100%);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
  box-shadow: var(--shadow-md);
}

:deep(.el-button--primary.is-loading) {
  background: var(--color-primary-gradient);
}

/* Icon Styling */
:deep(.el-input__prefix) {
  color: var(--color-text-secondary);
  transition: color var(--transition-fast);
}

:deep(.el-input__wrapper.is-focus .el-input__prefix) {
  color: var(--color-primary);
}

/* Responsive */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    margin: 0 var(--space-4);
  }
  
  .card-header h2 {
    font-size: var(--font-size-2xl);
  }
}
</style>