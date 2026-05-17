# Pandora 智能掌上工作系统

Pandora 是一个后台管理系统项目，包含 Vue 3 前端和 Spring Boot 后端。项目用于组织后台工作台、用户管理、公司事项、日志记录等管理功能。

## 技术栈

- 前端：Vue 3、Vite、Vue Router、Pinia、Element Plus、Axios
- 后端：Spring Boot 3、Spring Security、MyBatis Plus、H2 Database、JWT
- 工程：Maven、npm、Vite dev server

## 项目结构

```text
backend/  Spring Boot 后端服务
web/      Vue 3 + Vite 前端项目
android/  Android 端历史目录
```

## 核心功能

- 登录与 JWT 登录态
- Axios 请求封装与 401 会话过期处理
- Vue Router 路由表与登录态守卫
- 用户管理、公司事项、部门/角色相关接口
- 前端日志记录与主题切换

## 本地运行

后端：

```bash
cd backend
./mvnw spring-boot:run
```

前端：

```bash
cd web
npm install
npm run dev
```

前端开发服务默认运行在 `http://127.0.0.1:3000`，并通过 Vite proxy 将 `/api` 请求转发到后端 `http://127.0.0.1:8080`。

## 配置说明

仓库中的 `backend/src/main/resources/application.yml` 仅用于本地开发演示。生产环境应使用外部配置或环境变量替换数据库密码、JWT secret、证书等敏感配置。
