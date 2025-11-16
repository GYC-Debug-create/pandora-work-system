import { defineStore } from 'pinia'
import { ref } from 'vue'

// 常量定义
const STORAGE_KEY = 'pandora-theme'
const THEME_LIGHT = 'light'
const THEME_DARK = 'dark'

/**
 * 获取系统主题偏好
 */
const getSystemTheme = () => {
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return THEME_DARK
  }
  return THEME_LIGHT
}

/**
 * 从 localStorage 加载主题
 */
const loadThemeFromStorage = () => {
  const savedTheme = localStorage.getItem(STORAGE_KEY)
  if (savedTheme && (savedTheme === THEME_LIGHT || savedTheme === THEME_DARK)) {
    return savedTheme
  }
  // 如果没有保存的主题，使用系统偏好
  return getSystemTheme()
}

export const useThemeStore = defineStore('theme', () => {
  // state: 当前主题
  const theme = ref(loadThemeFromStorage())

  /**
   * 设置主题
   * @param {string} value - 'light' 或 'dark'
   */
  const setTheme = (value) => {
    if (value !== THEME_LIGHT && value !== THEME_DARK) {
      console.warn(`无效的主题值: ${value}，使用默认主题 light`)
      value = THEME_LIGHT
    }
    theme.value = value
    // 保存到 localStorage
    localStorage.setItem(STORAGE_KEY, value)
  }

  /**
   * 切换主题
   */
  const toggleTheme = () => {
    const newTheme = theme.value === THEME_LIGHT ? THEME_DARK : THEME_LIGHT
    setTheme(newTheme)
  }

  return {
    theme,
    setTheme,
    toggleTheme
  }
})

