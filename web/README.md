# 潘多拉智能掌上工作系统 - 前端原型

这是"潘多拉智能掌上工作系统"的前端原型项目，使用Vue 3和Element Plus构建，提供完整的UI界面和交互功能。

## 项目说明

本项目是一个纯前端原型，使用Vue 3和Element Plus构建，不需要连接后端API或数据库。所有数据均为前端模拟数据(mock data)。

## 功能页面

1. **仪表盘**: 展示系统概览和关键数据
2. **用户管理**: 展示用户列表，支持添加、编辑、删除用户等操作
3. **内容管理**: 展示公司重要事项列表，支持编辑操作
4. **审核流程**: 展示待办事项审核队列，支持审核操作

## 技术栈

- Vue 3：渐进式JavaScript框架
- Vite：现代前端构建工具
- Vue Router：路由管理
- Element Plus：基于Vue 3的组件库
- Axios：HTTP请求库
- Pinia：状态管理库

## 如何运行

1. 安装依赖：
```bash
npm install
```

2. 启动开发服务器：
```bash
npm run dev
```

3. 构建生产版本：
```bash
npm run build
```

## 项目结构

```
├── public/                  # 静态资源
├── src/                     # 源代码
│   ├── api/                 # API请求
│   ├── assets/              # 资源文件
│   ├── layout/              # 布局组件
│   ├── router/              # 路由配置
│   ├── utils/               # 工具函数
│   ├── views/               # 页面组件
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
├── index.html               # HTML模板
├── vite.config.js           # Vite配置
└── package.json             # 项目依赖
```

## 与SpringBoot后端集成

当需要与SpringBoot后端集成时：

1. 修改 `vite.config.js` 中的代理配置，指向正确的后端API地址
2. 将 `src/utils/request.js` 中的 `useMock` 变量设置为 `false`
3. 构建前端代码并部署到SpringBoot项目的静态资源目录

```js
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // SpringBoot后端地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 模拟数据

项目使用前端模拟数据进行开发和演示，数据定义在 `src/utils/request.js` 文件中。