@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo   侗乡医药数字展示平台 - 本地构建脚本
echo ==========================================
echo.

set PROJECT_ROOT=%~dp0..
set DEPLOY_DIR=%~dp0
set BACKEND_DIR=%PROJECT_ROOT%\dong-medicine-backend
set FRONTEND_DIR=%PROJECT_ROOT%\dong-medicine-frontend

echo [1/5] 清理旧的构建文件...
if exist "%DEPLOY_DIR%\dong-medicine-backend.jar" del "%DEPLOY_DIR%\dong-medicine-backend.jar"
if exist "%DEPLOY_DIR%\frontend-dist" rmdir /s /q "%DEPLOY_DIR%\frontend-dist"
if exist "%DEPLOY_DIR%\public" rmdir /s /q "%DEPLOY_DIR%\public"

echo [2/5] 构建后端项目...
cd /d "%BACKEND_DIR%"
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo 后端构建失败！
    pause
    exit /b 1
)

copy /y "%BACKEND_DIR%\target\dong-medicine-backend-1.0.0.jar" "%DEPLOY_DIR%\dong-medicine-backend.jar"
echo 后端构建完成！

echo [3/5] 构建前端项目...
cd /d "%FRONTEND_DIR%"
call npm install
if errorlevel 1 (
    echo npm install 失败！
    pause
    exit /b 1
)

call npm run build
if errorlevel 1 (
    echo 前端构建失败！
    pause
    exit /b 1
)

xcopy /e /i /y "%FRONTEND_DIR%\dist" "%DEPLOY_DIR%\frontend-dist"
echo 前端构建完成！

echo [4/5] 复制静态资源...
xcopy /e /i /y "%BACKEND_DIR%\public" "%DEPLOY_DIR%\public"
echo 静态资源复制完成！

echo [5/5] 导出数据库...
set MYSQL_DUMP=
where mysqldump >nul 2>&1
if errorlevel 1 (
    echo 警告: 未找到mysqldump命令，请手动导出数据库
) else (
    echo 请输入MySQL root密码:
    set /p DB_PASSWORD=
    mysqldump -u root -p!DB_PASSWORD! dong_medicine > "%DEPLOY_DIR%\dong_medicine_data.sql"
    echo 数据库导出完成！
)

echo.
echo ==========================================
echo   构建完成！
echo ==========================================
echo.
echo 部署文件已准备就绪，位于: %DEPLOY_DIR%
echo.
echo 文件列表:
echo   - dong-medicine-backend.jar (后端JAR包)
echo   - frontend-dist/ (前端构建产物)
echo   - public/ (静态资源)
echo   - .env.production (环境配置，请修改)
echo   - deploy.sh (部署脚本)
echo   - nginx.conf (Nginx配置)
echo   - dong-medicine-backend.service (服务配置)
echo.
echo 下一步:
echo   1. 编辑 .env.production 配置数据库密码和JWT密钥
echo   2. 将整个deploy目录上传到服务器 /opt/deploy/
echo   3. 在服务器执行: chmod +x deploy.sh ^&^& ./deploy.sh
echo.
pause
