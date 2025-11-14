module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  ignorePatterns: ['dist', 'node_modules'],
  rules: {
    // 关闭一些过于严格的规则
    'vue/multi-word-component-names': 'off',
    'no-unused-vars': 'warn',
    'no-console': 'off',
    'vue/no-unused-vars': 'warn',
    'vue/require-default-prop': 'warn',
    'vue/no-v-html': 'warn'
  }
}

