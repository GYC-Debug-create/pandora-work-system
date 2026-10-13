# Pandora 智能掌上工作系统

Pandora 是一个后台管理系统项目，包含 Vue 3 前端和 Spring Boot 后端。项目用于组织后台工作台、用户管理、公司事项、日志记录等管理功能。

## 技术栈

- 前端：Vue 3、Vite 5、Vue Router 4、Pinia、Element Plus、Axios（见 `web/package.json`）
- 后端：Java 17、Spring Boot 3.5.5、Spring Security、MyBatis-Plus 3.5.7、H2 数据库、JWT（jjwt 0.11.5），使用 Maven 构建（见 `backend/pom.xml`）
- 工程：Maven Wrapper、npm、Vite dev server

## 项目结构

```text
backend/      Spring Boot 后端服务
web/          Vue 3 + Vite 前端项目（开发相关文档位于 web/docs/）
android/      Android 端历史目录
启动服务.bat  Windows 下同时启动前后端开发服务的脚本
```

## 核心功能

- 登录与 JWT 登录态
- Axios 请求封装与 401 会话过期处理
- Vue Router 路由表与登录态守卫
- 用户管理、公司事项、部门/角色相关接口
- 前端日志记录与主题切换

## 本地运行

后端（默认端口 8080）：

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

Windows 下也可以运行根目录的 `启动服务.bat`，它会分别启动后端与前端开发服务。

## 配置说明

仓库中的 `backend/src/main/resources/application.yml` 仅用于本地开发演示。以下敏感配置项在仓库中均为占位符 `<REDACTED>`，运行前需要通过外部配置或环境变量提供真实值：

| 配置项 | 用途 | 仓库中的值 |
| --- | --- | --- |
| `spring.datasource.password` | 数据库密码 | `<REDACTED>` |
| `jwt.secret` | JWT 签名密钥（由 `JwtUtil` 注入，启动时校验至少 32 字节） | `${JWT_SECRET:}`，只从环境变量 `JWT_SECRET` 读取，未设置时后端拒绝启动 |

生产环境应使用外部配置或环境变量替换数据库密码、JWT secret、证书等敏感配置。

## 仓库说明

- 本仓库为团队共同开发项目的公开副本。
- 历史提交中的配置口令与密钥已替换为 `<REDACTED>`。历史中曾出现的旧口令与密钥应视为已泄露，请勿复用，并在实际部署环境中更换。
- 数据库文件（H2）、keystore 证书库、构建产物（`backend/target/`）、IDE 配置（`.idea/`）、上传目录中的图片（`uploads/`）以及含个人资料的代码质量评审包（`代码质量评审包/`）已从仓库移除。
- 提交者邮箱已匿名化（提交者显示名保留）。
- 如需完整开发记录请联系团队成员。
