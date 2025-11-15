@echo off
chcp 65001 >nul
echo ========================================
echo   潘多拉系统 - 服务启动脚本
echo ========================================
echo.

echo [1/2] 正在启动后端服务...
start "后端服务 - Spring Boot" cmd /k "cd backend && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo [2/2] 正在启动前端服务...
start "前端服务 - Vite" cmd /k "cd web && npm run dev"

echo.
echo ========================================
echo   服务启动中，请等待...
echo ========================================
echo.
echo 后端服务: http://localhost:8080
echo 前端服务: http://localhost:5173
echo.
echo 请在新打开的窗口中查看启动日志
echo 按任意键关闭此窗口...
pause >nul

